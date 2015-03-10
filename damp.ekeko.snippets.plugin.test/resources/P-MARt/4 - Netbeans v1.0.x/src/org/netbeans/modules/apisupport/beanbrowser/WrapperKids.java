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

package org.netbeans.modules.apisupport.beanbrowser;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import org.openide.Places;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;

/** The fun stuff.
* Represents all the children of a wrapper node, including
* lots of special items for certain node types.
*/
class WrapperKids extends Children.Keys implements Cloneable {

    // Special keys:
    private static final Object normalKey = new Object () {
                                                public String toString () {
                                                    return "Key for normal children.";
                                                }
                                            };
    private static final Object instanceKey = new Object () {
                public String toString () {
                    return "Key for instance cookie.";
                }
            };

    private Node original;
    private NodeListener nListener = null;

    private Object cookieHolder;

    WrapperKids (Node orig) {
        original = orig;
    }
    // Probably not needed:
    public Object clone () {
        return new WrapperKids (original);
    }

    /** Update all keys.
    * Keys may be:
    * <ol>
    * <li> normalKey, for the original node's children.
    * <li> A node property set--i.e. Properties, Expert.
    * <li> instanceKey, if it is an instance.
    * <li> A {@link Method} for cookies, representing the method to get a cookie from the object.
    * <li> Itself (the instance) if a Node, Container, FileSystem, FileObject, or Clipboard.
    * </ol>
    */
    private void updateKeys () {
        //Thread.dumpStack ();
        //System.err.println ("original's class: " + original.getClass ().getName ());
        Children.MUTEX.postWriteRequest (new Runnable () { public void run () {
                                                 List newkeys = new ArrayList ();
                                                 // Don't make the original list for leaf or childless nodes:
                                                 if (original.getChildren ().getNodes ().length > 0) {
                                                     newkeys.add (normalKey);
                                                     //System.err.println ("updateKeys: " + original.getDisplayName () + " had some children");
                                                 } else {
                                                     //System.err.println ("updateKeys: " + original.getDisplayName () + " had no children");
                                                     //System.err.println ("children object of original: " + original.getChildren ());
                                                 }
                                                 newkeys.addAll (makePSKeys ());
                                                 // For BeanNode, we assume that we already are displaying the "instance" right here anyway.
                                                 if (! (original instanceof BeanNode) && original.getCookie (InstanceCookie.class) != null)
                                                     newkeys.add (instanceKey);
                                                 // BeanNode's which are actually representing interesting objects:
                                                 if (original instanceof BeanNode) {
                                                     try {
                                                         InstanceCookie cookie = (InstanceCookie) original.getCookie (InstanceCookie.class);
                                                         Class clazz = cookie.instanceClass ();
                                                         Class[] recognized = { Node.class, Container.class, FileSystem.class, FileObject.class, Clipboard.class, Places.class };
                                                         for (int i = 0; i < recognized.length; i++)
                                                             if (recognized[i].isAssignableFrom (clazz))
                                                                 newkeys.add (cookie.instanceCreate ());
                                                         Method m = getCookieHolderMethod (clazz);
                                                         if (m != null) {
                                                             newkeys.add (m);
                                                             cookieHolder = cookie.instanceCreate ();
                                                         }
                                                     } catch (Exception e) {
                                                         e.printStackTrace ();
                                                     }
                                                 } else {
                                                     newkeys.add (getCookieHolderMethod (Node.class));
                                                     cookieHolder = original;
                                                 }
                                                 _setKeys (newkeys);
                                                 //System.err.println ("Setting keys for wrapper of " + original.getDisplayName () + "; count: " + newkeys.size ());
                                             }});
    }
    private void _setKeys (Collection c) { setKeys (c); }

    private static Method getCookieHolderMethod (Class holderClass) {
        try {
            Method m = holderClass.getMethod ("getCookie", new Class[] { Class.class });
            if (Node.Cookie.class.equals (m.getReturnType ()))
                return m;
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Set the keys and attach a listener to the original node.
    * If its list of children is changed, the normalKey
    * may be added or removed.
    */
    protected void addNotify () {
        //System.err.println ("addNotify called for wrapper of " + original.getDisplayName ());
        updateKeys ();
        nListener = new NodeAdapter () {
                        public void propertyChange (PropertyChangeEvent ev) {
                            if (Node.PROP_PROPERTY_SETS.equals (ev.getPropertyName ())) {
                                updateKeys ();
                            }
                        }
                        // These two only really matter if adding the first child(ren), or removing the last.
                        public void childrenAdded (NodeMemberEvent ev) {
                            updateKeys ();
                        }
                        public void childrenRemoved (NodeMemberEvent ev) {
                            updateKeys ();
                        }
                        // Do not need to check childrenReordered because this class only cares if there are kids, or not.
                        // NormalKids will handle that.
                    };
        original.addNodeListener (nListener);
    }

    protected void removeNotify () {
        doUnListen ();
        setKeys (Collections.EMPTY_SET);
    }

    private void doUnListen () {
        if (nListener != null) {
            original.removeNodeListener (nListener);
            nListener = null;
        }
    }

    protected void finalize () {
        doUnListen ();
    }

    /** Make a list of property set keys.
    * One key (a Node.PropertySet) is added for every property set
    * which contains at least one property which is not a primitive
    * or of String or Class type.
    * <p> Note that it is possible for a property to be of e.g. Object type,
    * and have a displayed node, even though the actual value is a String, e.g.
    * @return a list of keys
    */
    private Collection makePSKeys () {
        Collection toret = new ArrayList ();
        Node.PropertySet[] pss = original.getPropertySets ();
        for (int i = 0; i < pss.length; i++) {
            Node.PropertySet ps = pss[i];
            Node.Property[] props = ps.getProperties ();
            boolean useme = false;
            for (int j = 0; j < props.length; j++) {
                Node.Property prop = props[j];
                if (prop.canRead ()) {
                    Class type = prop.getValueType ();
                    if (! (type.isPrimitive () || type == String.class || type == Class.class)) {
                        useme = true;
                    }
                }
            }
            if (useme) toret.add (ps);
        }
        return toret;
    }

    /** Actual interpret a key.
    * Creates a node representing each key, e.g. a BeanNode for instanceKey,
    * or for a Node.PropertySet, a PropSet node.
    * @param key the key to interpret
    * @return the (one) node to display for it
    */
    protected Node[] createNodes (Object key) {
        if (key == normalKey) {
            // Regular children of the node.
            AbstractNode n = new AbstractNode (new NormalKids (original)) {
                                 public HelpCtx getHelpCtx () {
                                     return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                 }
                             };
            n.setName ("Children...");
            n.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            return new Node[] { n };
        } else if (key instanceof Node.PropertySet) {
            // A property set with subnodes for the properties.
            return new Node[] { new PropSet (original, (Node.PropertySet) key) };
        } else if (key == instanceKey) {
            // Something which can provide an instance object--e.g. the deserialized object
            // from a .ser file.
            try {
                InstanceCookie inst = (InstanceCookie) original.getCookie (InstanceCookie.class);
                Node node = new RefinedBeanNode (inst.instanceCreate ());
                node.setShortDescription ("Instance name: `" + inst.instanceName () +
                                          "'; normal node name: `" + node.getDisplayName () + "'; normal description: `" +
                                          node.getShortDescription () + "'");
                node.setDisplayName ("Instance of class " + inst.instanceClass ().getName ());
                return new Node[] { Wrapper.make (node) };
            } catch (Exception e) {
                return new Node[] { Wrapper.make (PropSetKids.makeErrorNode (e)) };
            }
        } else if (key instanceof Node) {
            // Show the actual node itself.
            AbstractNode marker = new AbstractNode (new Children.Array ()) {
                                      public HelpCtx getHelpCtx () {
                                          return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                      }
                                  };
            marker.setName ("An actual node here:");
            marker.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            marker.getChildren ().add (new Node[] { Wrapper.make ((Node) key) });
            return new Node[] { marker };
        } else if (key instanceof Container) {
            // An AWT Container with its subcomponents.
            Children kids = new ContainerKids ((Container) key);
            AbstractNode n = new AbstractNode (kids) {
                                 public HelpCtx getHelpCtx () {
                                     return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                 }
                             };
            n.setName ("Components...");
            n.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            return new Node[] { n };
        } else if (key instanceof FileSystem) {
            // "root" is not a declared Bean property of FileSystem's, so specially display it.
            try {
                Node fsn = new RefinedBeanNode (((FileSystem) key).getRoot ());
                fsn.setDisplayName ("[root] " + fsn.getDisplayName ());
                return new Node[] { Wrapper.make (fsn) };
            } catch (IntrospectionException e) {
                return new Node[] { Wrapper.make (PropSetKids.makeErrorNode (e)) };
            }
        } else if (key instanceof FileObject) {
            Children kids = new FileAttrKids ((FileObject) key);
            AbstractNode attrnode = new AbstractNode (kids) {
                                        public HelpCtx getHelpCtx () {
                                            return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                        }
                                    };
            attrnode.setName ("Attributes...");
            attrnode.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            // Display the corresponding DataObject.
            // The node delegate is also available as a Bean property of the DO.
            try {
                Node fsn = new RefinedBeanNode (DataObject.find ((FileObject) key));
                fsn.setDisplayName ("[data object] " + fsn.getDisplayName ());
                return new Node[] { Wrapper.make (fsn), attrnode };
            } catch (Exception e) { // DataObjectNotFoundException, IntrospectionException
                return new Node[] { Wrapper.make (PropSetKids.makeErrorNode (e)), attrnode };
            }
        } else if (key instanceof Clipboard) {
            Children kids = new ClipboardKids ((Clipboard) key);
            AbstractNode n = new AbstractNode (kids) {
                                 public HelpCtx getHelpCtx () {
                                     return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                 }
                             };
            n.setName ("Transferables...");
            n.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            return new Node[] { n };
        } else if (key instanceof Places) {
            Places p = (Places) key;
            Places.Folders pf = p.folders ();
            Places.Nodes pn = p.nodes ();
            return new Node[] {
                       PropSetKids.makeObjectNode (pf.actions ()),
                       PropSetKids.makeObjectNode (pf.bookmarks ()),
                       PropSetKids.makeObjectNode (pf.menus ()),
                       PropSetKids.makeObjectNode (pf.projects ()),
                       PropSetKids.makeObjectNode (pf.startup ()),
                       PropSetKids.makeObjectNode (pf.templates ()),
                       PropSetKids.makeObjectNode (pf.toolbars ()),
                       PropSetKids.makeObjectNode (pn.controlPanel ()),
                       PropSetKids.makeObjectNode (pn.environment ()),
                       PropSetKids.makeObjectNode (pn.loaderPool ()),
                       PropSetKids.makeObjectNode (pn.project ()),
                       PropSetKids.makeObjectNode (pn.projectDesktop ()),
                       PropSetKids.makeObjectNode (pn.repository ()),
                       PropSetKids.makeObjectNode (pn.repositorySettings ()),
                       // We show roots in the main node anyway, this is just confusing:
                       // PropSetKids.makeObjectNode (pn.roots ()),
                       PropSetKids.makeObjectNode (pn.session ()),
                       PropSetKids.makeObjectNode (pn.workspaces ()),
                   };
        } else if (key instanceof Method) {
            Children kids = new CookieKids ((Method) key, cookieHolder);
            AbstractNode n = new AbstractNode (kids) {
                                 public HelpCtx getHelpCtx () {
                                     return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                 }
                             };
            n.setName ("Cookies...");
            n.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
            return new Node[] { n };
        } else {
            throw new RuntimeException ("Weird key: " + key);
        }
    }

}

/*
 * Log
 *  29   Gandalf-post-FCS1.27.1.0    3/30/00  Jesse Glick     Showing structure of 
 *       org.openide.Places.
 *  28   Gandalf   1.27        2/4/00   Jesse Glick     Clipboard support.
 *  27   Gandalf   1.26        12/29/99 Jesse Glick     Compiler bug workaround.
 *  26   Gandalf   1.25        12/23/99 Jesse Glick     Added FileObject 
 *       attributes and cookie browsing to BB.
 *  25   Gandalf   1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  23   Gandalf   1.22        10/7/99  Jesse Glick     Context help.
 *  22   Gandalf   1.21        10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  21   Gandalf   1.20        9/16/99  Jesse Glick     Fixed threading problem 
 *       with new nodes.
 *  20   Gandalf   1.19        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        5/28/99  Jesse Glick     Forgot to give node for 
 *       regular children a name.
 *  18   Gandalf   1.17        5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  17   Gandalf   1.16        5/26/99  Jesse Glick     
 *  16   Gandalf   1.15        5/26/99  Jesse Glick     Using more sensible 
 *       keys, and displaying the DataObject for a FileObject.
 *  15   Gandalf   1.14        5/26/99  Jesse Glick     Displaying file system 
 *       roots.
 *  14   Gandalf   1.13        5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  13   Gandalf   1.12        5/24/99  Jesse Glick     Using RefinedBeanNode 
 *       for CustomizeBeanAction.
 *  12   Gandalf   1.11        5/21/99  Jesse Glick     Handling containers.
 *  11   Gandalf   1.10        5/19/99  Jesse Glick     Arrays of primitives 
 *       handled, sort of.
 *  10   Gandalf   1.9         5/19/99  Jesse Glick     Node instance handling. 
 *       Don't ask, read the source, it is difficult to explain.
 *  9    Gandalf   1.8         5/19/99  Jesse Glick     Instance support.
 *  8    Gandalf   1.7         5/18/99  Jesse Glick     Split off much of it for
 *       new keys semantics.
 *  7    Gandalf   1.6         5/14/99  Jesse Glick     All well, but no 
 *       refreshes of keys after first loading.
 *  6    Gandalf   1.5         5/14/99  Jesse Glick     
 *  5    Gandalf   1.4         5/14/99  Jesse Glick     
 *  4    Gandalf   1.3         5/14/99  Jesse Glick     
 *  3    Gandalf   1.2         5/14/99  Jesse Glick     Mostly works now.
 *  2    Gandalf   1.1         5/13/99  Jesse Glick     
 *  1    Gandalf   1.0         5/13/99  Jesse Glick     
 * $
 */
