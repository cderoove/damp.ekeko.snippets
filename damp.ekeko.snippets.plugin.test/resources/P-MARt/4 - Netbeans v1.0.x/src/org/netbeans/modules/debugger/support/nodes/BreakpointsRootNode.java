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

package org.netbeans.modules.debugger.support.nodes;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.DebuggerException;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.BeanNode;
import org.openide.nodes.AbstractNode;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.support.actions.BreakpointsRootCookie;
import org.netbeans.modules.debugger.support.actions.DeleteAllAction;
import org.netbeans.modules.debugger.support.actions.EnableAllBreakpointsAction;
import org.netbeans.modules.debugger.support.actions.DisableAllBreakpointsAction;


/**
* This class represents root of breakpoints.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class BreakpointsRootNode extends AbstractNode implements
    BreakpointsRootCookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6708027507109804744L;


    // static ....................................................................

    private static final String        ICON_BASE =
        "/org/netbeans/core/resources/breakpoints"; // NOI18N

    /** Popup menu actions. */
    private static SystemAction []     staticActions;

    private static ResourceBundle      bundle;


    // init ......................................................................

    /**
    * Creates empty BreakpointsRootNode.
    */
    public BreakpointsRootNode () {
        super (new BreakpointsContextChildren());
        String name = getLocalizedString ("CTL_Breakpoints_group_root");
        setDisplayName (name);
        setName (name);
        setIconBase (ICON_BASE);
        getCookieSet ().add (this);
        init ();
    }

    /** deserializes object */
    private void readObject(java.io.ObjectInputStream obis)
    throws java.io.IOException, ClassNotFoundException,
        java.io.NotActiveException {
        obis.defaultReadObject ();
        init ();
    }

    private void init () {
    }


    // Node implementation .......................................................

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        if (staticActions == null)
            staticActions = new SystemAction[] {
                                SystemAction.get (AddBreakpointAction.class),
                                null,
                                SystemAction.get (EnableAllBreakpointsAction.class),
                                SystemAction.get (DisableAllBreakpointsAction.class),
                                SystemAction.get (DeleteAllAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class),
                            };
        return staticActions;
    }

    /** Obtain handle for this node (for serialization).
    * The handle can be serialized and {@link Handle#getNode} used after
    * deserialization to obtain the original node.
    *
    * @return the handle, or <code>null</code> if this node is not persistable
    */
    public Node.Handle getHandle () {
        return new BreakpointsHandle ();
    }

    /**
    * Returns default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (AddBreakpointAction.class);
    }


    // BreakpointsRootCookie implementation ......................................

    /**
    * Delete all breakpoints.
    */
    public void deleteAll () {
        try {
            // obtain reference to the debugger instance
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().
                                        getDebugger ();
            debugger.removeAllBreakpoints ();
        } catch (DebuggerException exc) {
        }
    }

    /**
    * Enable all breakpoints.
    */
    public void enableAll () {
        try {
            // obtain reference to the debugger instance
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().
                                        getDebugger ();
            Breakpoint[] b = debugger.getBreakpoints ();
            int i, k = b.length;
            for (i = 0; i < k; i++)
                b [i].setEnabled (true);
        } catch (DebuggerException exc) {
        }
    }

    /**
    * Disable all breakpoints.
    */
    public void disableAll () {
        try {
            // obtain reference to the debugger instance
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().
                                        getDebugger ();
            Breakpoint[] b = debugger.getBreakpoints ();
            int i, k = b.length;
            for (i = 0; i < k; i++)
                b [i].setEnabled (false);
        } catch (DebuggerException exc) {
        }
    }


    // other methods .............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (BreakpointsRootNode.class);
        return bundle.getString (s);
    }


    // innerclasses ..............................................................

    /** Special breakpoints context subnodes (children) */
    private static final class BreakpointsContextChildren extends Children.Map {

        AbstractDebugger debugger = null;

        /** Initializes children - creates nodes for existing breakpoints */
        protected java.util.Map initMap () {
            // obtain reference to the debugger instance
            try {
                debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            } catch (DebuggerException exc) {
                return null;
            }
            // listen on adding/removing of breakpoints
            debugger.addDebuggerListener (new DebuggerAdapter () {
                                              public void breakpointAdded (CoreBreakpoint breakpoint) {
                                                  if (breakpoint.isHidden ()) return;
                                                  addBreakpoint(breakpoint);
                                              }
                                              public void breakpointRemoved (CoreBreakpoint breakpoint) {
                                                  if (breakpoint.isHidden ()) return;
                                                  removeBreakpoint(breakpoint);
                                              }
                                          });
            // create nodes
            Breakpoint[] breakpoints = debugger.getBreakpoints ();
            TreeMap map = new TreeMap (new BreakpointComparator ());
            for (int i = 0; i < breakpoints.length; i++)
                map.put(
                    breakpoints [i],
                    new BreakpointNode ((CoreBreakpoint) breakpoints[i])
                );
            return map;
        }

        /** Adds new subnode representing breakpoint */
        private void addBreakpoint (final CoreBreakpoint breakpoint) {
            put(breakpoint, new BreakpointNode (breakpoint));
        }

        /** Removes subnode which represents given breakpoint */
        private void removeBreakpoint (final CoreBreakpoint breakpoint) {
            remove(breakpoint);
        }


        private class BreakpointComparator implements Comparator {
            public int compare (Object o1, Object o2) {
                CoreBreakpoint b1 = (CoreBreakpoint) o1;
                CoreBreakpoint b2 = (CoreBreakpoint) o2;
                String s1 = "";
                try {
                    s1 = b1.getEvent (debugger).getDisplayName ();
                } catch (NullPointerException e) {
                }
                String s2 = "";
                try {
                    s2 = b2.getEvent (debugger).getDisplayName ();
                } catch (NullPointerException e) {
                }
                return s1.compareToIgnoreCase (s2);
            }
        }
    } // end of BreakpointsContextChildren inner class

    /**
    * Serializable node reference. 
    */
    private static class BreakpointsHandle implements Node.Handle {
        static final long serialVersionUID =-4518262935887259653L;

        /** Reconstitute the node for this handle.
        *
        * @return the node for this handle
        * @exception IOException if the node cannot be created
        */
        public Node getNode () {
            return DebuggerModule.getBreakpointsRootNode ();
        }
    }
}

/*
 * Log
 *  11   Jaga      1.8.1.1     3/22/00  Jan Jancura     
 *  10   Jaga      1.8.1.0     3/2/00   Jan Jancura     
 *  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         12/20/99 Daniel Prusa    Bug 4291 - Cancel in 
 *       AddBreakpoint Window had been deleting breakpoints.
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         11/5/99  Jan Jancura     Default action updated
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/1/99  Jan Jancura     Bug 3677
 *  3    Gandalf   1.2         9/28/99  Jan Jancura     
 *  2    Gandalf   1.1         9/2/99   Jan Jancura     
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 */
