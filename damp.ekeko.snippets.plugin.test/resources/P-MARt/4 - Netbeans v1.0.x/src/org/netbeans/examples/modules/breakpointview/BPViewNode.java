/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.examples.modules.breakpointview;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import org.openide.TopManager;
import org.openide.cookies.SourceCookie;
import org.openide.debugger.*;
import org.openide.loaders.DataFolder;
import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.text.Line;
import org.openide.util.*;

// [PENDING] s/MethodElement/ConstructorElement/g later when Hanz implements

public class BPViewNode extends FilterNode {

    private static int[] depth = new int[10];
    static void depthcharge (int i) {
        if (depth[i]++ > 500) {
            StringBuffer buf = new StringBuffer ("Depth charge:");
            for (int j = 0; j < depth.length; j++) {
                buf.append (' ');
                buf.append (depth[j]);
            }
            throw new RuntimeException (buf.toString ());
        }
    }

    /**
     * @associates Object 
     */
    private final Map vals = new HashMap ();
    private final PropertyChangeListener debuglist = new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent ev) {
                    System.err.println ("Event on debugger: " + ev);
                    if (ev.getPropertyName ().equals (Debugger.PROP_BREAKPOINTS)) {
                        System.err.println ("Breakpoints fired");
                        // old/new value not currently fired by std debugger impl, don't even bother
                        //refreshCount ();
                        // Cause all props to fire changes if applicable:
                        Iterator it = myProps.iterator ();
                        while (it.hasNext ()) {
                            Node.Property prop = (Node.Property) it.next ();
                            Object old = vals.get (prop);
                            try {
                                Object nue = prop.getValue ();
                                vals.put (prop, nue);
                                if (old != null && ! Utilities.compareObjects (old, nue)) {
                                    System.err.println ("Property changed: " + prop.getName ());
                                    firePropertyChange0 (prop.getName (), old, nue);
                                }
                            } catch (InvocationTargetException ite) {
                                ite.printStackTrace ();
                            } catch (IllegalAccessException iae) {
                                iae.printStackTrace ();
                            }
                        }
                    }
                }
            };
    private void firePropertyChange0 (String name, Object old, Object nue) {
        firePropertyChange (name, old, nue);
    }
    private final Set myProps = new HashSet (); // Set<Node.Property>

    public BPViewNode () {
        this (repo);
    }

    public BPViewNode (Node orig) {
        super (orig, orig.isLeaf () ? org.openide.nodes.Children.LEAF : new BPViewChildren (orig));
        depthcharge (0);
        if (! isInteresting (orig))
            throw new IllegalArgumentException ("uninteresting node for BPView: " + orig);
        //refreshDisplayName ();
        try {
            Debugger d = TopManager.getDefault ().getDebugger ();
            if (d != null) {
                System.err.println ("Adding debugger listener");
                d.addPropertyChangeListener (debuglist);
            }
        } catch (DebuggerNotFoundException e) {
        }
        TopManager.getDefault ().addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent ev) {
                        if (ev.getPropertyName ().equals (TopManager.PROP_DEBUGGER)) {
                            System.err.println ("Switching debugger");
                            Debugger old = (Debugger) ev.getOldValue ();
                            if (old != null) old.removePropertyChangeListener (debuglist);
                            Debugger nue = (Debugger) ev.getNewValue ();
                            if (nue != null) nue.addPropertyChangeListener (debuglist);
                        }
                    }
                });
        final MethodElement meth = (MethodElement) orig.getCookie (MethodElement.class);
        final ClassElement clazz = (ClassElement) orig.getCookie (ClassElement.class);
        if (meth != null) {
            myProps.add (new PropertySupport.ReadWrite ("broken", Boolean.TYPE, "Is Broken", null) {
                             public Object getValue () {
                                 try {
                                     Debugger d = TopManager.getDefault ().getDebugger ();
                                     if (d.findBreakpoint (meth) != null)
                                         return Boolean.TRUE;
                                     else
                                         return Boolean.FALSE;
                                 } catch (DebuggerNotFoundException e) {
                                     return Boolean.FALSE;
                                 } catch (NullPointerException e) {
                                     // XXX temp workaround for bug in JavaDebugger
                                     return Boolean.FALSE;
                                 }
                             }
                             public void setValue (Object o) throws IllegalArgumentException {
                                 if (! (o instanceof Boolean)) throw new IllegalArgumentException ();
                                 try {
                                     Debugger d = TopManager.getDefault ().getDebugger ();
                                     if (((Boolean) o).booleanValue ()) {
                                         if (d.findBreakpoint (meth) == null) {
                                             d.createBreakpoint (meth);
                                         }
                                     } else {
                                         Breakpoint bp = d.findBreakpoint (meth);
                                         if (bp != null) bp.remove ();
                                     }
                                 } catch (DebuggerNotFoundException e) {
                                     throw new IllegalArgumentException (e.getMessage ());
                                 } catch (NullPointerException e) {
                                     // XXX temp workaround for bug in JavaDebugger
                                     throw new IllegalArgumentException (e.toString ());
                                 }
                             }
                         });
        }
        if (clazz != null) {
            myProps.add (new PropertySupport.ReadOnly ("brokenLines", int[].class, "Broken Lines", null) {
                             public Object getValue () {
                                 try {
                                     Debugger d = TopManager.getDefault ().getDebugger ();
                                     Breakpoint[] bps = d.getBreakpoints ();
                                     String myName = clazz.getName ().getFullName ();
                                     List l = new ArrayList ();
                                     for (int i = 0; i < bps.length; i++) {
                                         Breakpoint bp = bps[i];
                                         if (bp.getClassName ().equals (myName)) {
                                             Line line = bp.getLine ();
                                             if (line != null)
                                                 l.add (new Integer (line.getLineNumber ()));
                                         }
                                     }
                                     int[] toret = new int[l.size ()];
                                     for (int i = 0; i < toret.length; i++)
                                         toret[i] = ((Integer) l.get (i)).intValue ();
                                     return toret;
                                 } catch (DebuggerNotFoundException e) {
                                     return new int[] { };
                                 }
                             }
                         });
        }
        /*
        myProps.add (new PropertySupport.ReadOnly ("recursiveBreaks", Integer.TYPE, "Recursively Broken", null) {
          public Object getValue () {
            return new Integer (countBreakpoints ());
          }
    });
        */
    }
    protected void finalize () {
        try {
            Debugger d = TopManager.getDefault ().getDebugger ();
            if (d != null) {
                System.err.println ("Removing debugger listener");
                d.removePropertyChangeListener (debuglist);
            }
        } catch (DebuggerNotFoundException e) {
        }
        super.finalize ();
    }
    public Node cloneNode () {
        System.err.println ("Cloning " + this);
        depthcharge (1);
        return new BPViewNode (getOriginal ());
    }

    private static final String setname = Sheet.PROPERTIES;
    // private static final String setname = Sheet.EXPERT;
    // private static final String setname = "myCustomSheet";
    public Node.PropertySet[] getPropertySets () {
        Node.PropertySet[] oldpss = getOriginal ().getPropertySets ();
        if (myProps.size () == 0) return oldpss;
        List l = new ArrayList ();
        boolean found = false;
        for (int i = 0; i < oldpss.length; i++) {
            final Node.PropertySet oldps = oldpss[i];
            if (! found && oldps.getName ().equals (setname)) {
                found = true;
                l.add (new Node.PropertySet (setname, oldps.getDisplayName (), oldps.getShortDescription ()) {
                           public Node.Property[] getProperties () {
                               Node.Property[] orig = oldps.getProperties ();
                               Node.Property[] toret = (Node.Property[]) myProps.toArray (new Node.Property[orig.length + myProps.size ()]);
                               System.arraycopy (orig, 0, toret, myProps.size (), orig.length);
                               return toret;
                           }
                       });
            } else {
                l.add (oldps);
            }
        }
        if (! found) {
            l.add (new Node.PropertySet (setname, /* could also give real display name + tool tip here */ setname, setname) {
                       public Node.Property[] getProperties () {
                           return (Node.Property[]) myProps.toArray (new Node.Property[myProps.size ()]);
                       }
                   });
        }
        return (Node.PropertySet[]) l.toArray (new Node.PropertySet[l.size ()]);
    }

    /*
    public boolean canRename () { return false; }

    public String getDisplayName () {
      depthcharge (2);
      String orig = getOriginal ().getDisplayName ();
      System.err.println ("Getting display name for " + getName () + "; orig = " + orig);
      int count = countBreakpoints ();
      return orig + " [" + (count == -1 ? "?" : "" + count) + "]";
}
    */

    /*
      private int bpCache = -1;
      private synchronized int countBreakpoints () {
        depthcharge (3);
        if (bpCache == -1 && getOriginal ().isLeaf ())
          refreshCount ();
        System.err.println ("countBreakpoints called on " + this + "; result = " + bpCache);
        return bpCache;
      }
      private synchronized void refreshCount () {
        depthcharge (4);
        int old = bpCache;
        bpCache = recountBreakpoints ();
        if (old != bpCache) {
          //refreshDisplayName ();
          // XXX does this work now??
          //Node p = getParentNode ();
          //if (p instanceof BPViewNode) ((BPViewNode) p).refreshCount ();
          // XXX these can change independently:
          // if (getOriginal ().getCookie (ClassElement.class) != null) firePropertyChange ("brokenLines", null, null);
          // if (getOriginal ().getCookie (MethodElement.class) != null) firePropertyChange ("broken", null, null);
          firePropertyChange ("recursiveBreaks", new Integer (old), new Integer (bpCache));
        }
      }
      private int recountBreakpoints () {
        depthcharge (5);
        int count = 0;
        Enumeration e = getChildren ().nodes ();
        while (e.hasMoreElements ()) {
          int sub = ((BPViewNode) e.nextElement ()).countBreakpoints ();
          if (sub == -1)
            return -1;
          else
            count += sub;
        }
        Node orig = getOriginal ();
        MethodElement meth = (MethodElement) orig.getCookie (MethodElement.class);
        if (meth != null) {
          try {
            Debugger d = TopManager.getDefault ().getDebugger ();
            if (d.findBreakpoint (meth) != null)
              count++;
          } catch (DebuggerNotFoundException dnfe1) {
            // Fine.
          } catch (NullPointerException npe) {
            // XXX bug in JavaDebugger
          }
        }
        ClassElement clazz = (ClassElement) orig.getCookie (ClassElement.class);
        if (clazz != null) {
          try {
            Debugger d = TopManager.getDefault ().getDebugger ();
            String thisName = clazz.getName ().getFullName ();
            Breakpoint[] bps = d.getBreakpoints ();
            for (int i = 0; i < bps.length; i++) {
              String name = bps[i].getClassName ();
              if (name.equals (thisName))
                count++;
            }
          } catch (DebuggerNotFoundException dnfe2) {
            // Fine.
          }
        }
        return count;
      }
      */

    private static Node repo = TopManager.getDefault ().getPlaces ().nodes ().repository ();
    static boolean isInteresting (Node n) {
        depthcharge (6);
        return n.equals (repo) ||
               n.getCookie (DataFolder.class) != null ||
               n.getCookie (SourceCookie.class) != null ||
               n.getCookie (SourceElement.class) != null ||
               n.getCookie (ClassElement.class) != null ||
               n.getCookie (MethodElement.class) != null ||
               // Hack--support new "category" nodes under classes, which themselves would not have an Element cookie.
               (n.getChildren ().getNodes ().length > 0 && n.getChildren ().getNodes ()[0].getCookie (MethodElement.class) != null);
    }

    public static void main (String[] ign) {
        try {
            Node ns[] = TopManager.getDefault ().getNodeOperation ().select
                        ("Select a node for Breakpoint view", "Repository",
                         repo, new NodeAcceptor () {
                             public boolean acceptNodes (Node[] ns) {
                                 return ns.length == 1 && isInteresting (ns[0]);
                             }
                         });
            TopManager.getDefault ().getNodeOperation ().explore (new BPViewNode (ns[0]));
        } catch (UserCancelException e) {
        }
    }
}
