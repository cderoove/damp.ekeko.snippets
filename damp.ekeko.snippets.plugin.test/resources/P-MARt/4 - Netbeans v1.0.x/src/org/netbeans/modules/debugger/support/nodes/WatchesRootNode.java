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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.Watch;
import org.openide.actions.AddWatchAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractWatch;
import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.VariableImpl;
import org.netbeans.modules.debugger.support.actions.DeleteAllCookie;
import org.netbeans.modules.debugger.support.actions.DeleteAllAction;
import org.netbeans.modules.debugger.support.util.Validator;


/**
* This class representates watches root as a Node.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class WatchesRootNode extends AbstractNode implements
    VariableHome, DeleteAllCookie {

    // static variables ..........................................................

    /** generated Serialized Version UID */
    static final long               serialVersionUID = -3518528079657369038L;

    private static final String     ICON_WATCHES =
        "/org/netbeans/core/resources/watches"; // NOI18N
    private static final String     ICON_PENDING_WATCHES =
        "/org/netbeans/modules/debugger/resources/pendingWatches"; // NOI18N

    /** Popup menu actions. */
    private static SystemAction[]   staticActions;

    private static ResourceBundle   bundle;


    // init ......................................................................
    /**
    * Creates empty WatchesRootNode.
    */
    public WatchesRootNode () {
        super (new WatchesRootChildren ());
        ((WatchesRootChildren) getChildren ()).variableHome = this;
        String name = getLocalizedString ("CTL_Watches_group_root");
        setDisplayName (name);
        setName (name);
        setIconBase (ICON_WATCHES);
        getCookieSet ().add (this);
        init ();
    }

    private DebuggerView getView () {
        return DebuggerModule.getViewFor (this);
    }

    private void init () {
        try {
            AbstractDebugger debugger =
                (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            final Validator validator = debugger.getValidator ();
            debugger.getValidator ().addPropertyChangeListener (
                new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent e) {
                        setIconBase (
                            validator.isValidated () ?
                            ICON_WATCHES :
                            ICON_PENDING_WATCHES
                        );
                        /*
                        setDisplayName (
                          validator.isValidated () ?
                          getLocalizedString ("CTL_Watches_group_root") :
                          getLocalizedString ("CTL_Watches_group_root_refresh")
                        );
                        */  

                        // PATCH, we must force the view to be repainted

                        Node [] nodes = getChildren ().getNodes ();
                        Node node = null;
                        String displayName = null;
                        if ((nodes != null) && (nodes.length > 0)) {
                            node = nodes [0];
                            displayName = node.getDisplayName ();
                            node.setDisplayName (displayName.concat (" ")); // NOI18N
                        }
                        DebuggerView view = getView ();
                        if (view != null) {
                            view.repaint (70);
                        }
                        if (node != null)
                            node.setDisplayName (displayName);

                        // end of PATCH

                    }
                }
            );
        } catch (DebuggerException e) {
        }
    }

    /** deserializes object */
    private void readObject(java.io.ObjectInputStream obis)
    throws java.io.IOException, ClassNotFoundException,
        java.io.NotActiveException {
        obis.defaultReadObject ();
        init ();
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
                                SystemAction.get(AddWatchAction.class),
                                null,
                                SystemAction.get (DeleteAllAction.class),
                                null,
                                SystemAction.get(ToolsAction.class),
                                SystemAction.get(PropertiesAction.class),
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
        return new WatchesHandle ();
    }

    /**
    * Returns default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (AddWatchAction.class);
    }


    // DeleteAllCookie implementation ............................................

    /**
    * Delete all breakpoints.
    */
    public void deleteAll () {
        try {
            // obtain reference to the debugger instance
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().
                                        getDebugger ();
            debugger.removeAllWatches ();
        } catch (DebuggerException exc) {
        }
    }


    // other methods .............................................................

    public void createVariable (AbstractVariable variable) {
        ((WatchesRootChildren) getChildren ()).addVariable (
            (AbstractVariable) ((VariableImpl) variable).clone ()
        );
    }

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (WatchesRootNode.class);
        return bundle.getString (s);
    }


    // innerclasses ..............................................................

    /** Special watches context subnodes (children) */
    static final class WatchesRootChildren extends Children.Array {

        /** Where to add variables. */
        private VariableHome            variableHome;

        /**
         * @associates WatchNode 
         */
        private Hashtable watches = new Hashtable ();

        /** Initializes children - creates nodes for existing watches */
        protected java.util.Collection initCollection () {
            try {
                // obtain reference to the debugger instance
                final AbstractDebugger debugger = (AbstractDebugger) TopManager.
                                                  getDefault ().getDebugger ();
                // create nodes for watches
                Watch[] watches = debugger.getWatches ();
                int i, k = watches.length;
                ArrayList n = new ArrayList (k);
                for (i = 0; i < k; i++) {
                    WatchNode watchNode = new WatchNode (
                                              variableHome,
                                              (AbstractWatch) watches [i]
                                          );
                    n.add (watchNode);
                    this.watches.put (watches [i], watchNode);
                }

                debugger.addDebuggerListener (new DebuggerAdapter () {
                                                  public void watchAdded (AbstractWatch watch) {
                                                      addWatch (watch);
                                                  }
                                                  public void watchRemoved (AbstractWatch watch) {
                                                      removeWatch (watch);
                                                  }
                                              });

                debugger.addPropertyChangeListener (new PropertyChangeListener () {
                                                        public void propertyChange (PropertyChangeEvent e) {
                                                            if (debugger.getState () != debugger.DEBUGGER_NOT_RUNNING) return;
                                                            // remove all variables
                                                            Node[] nn = getNodes ();
                                                            int j, l = nn.length;
                                                            Vector v = new Vector (l);
                                                            for (j = 0; j < l; j++)
                                                                if (!(nn [j] instanceof WatchNode))
                                                                    v.addElement (nn [j]);
                                                            nn = (Node[]) v.toArray (new Node [v.size ()]);
                                                            remove (nn);
                                                        }
                                                    });
                return n;
            } catch (DebuggerException exc) {
                return new ArrayList ();
            }
        }

        /** Adds new subnode representing given watch */
        private void addWatch (AbstractWatch watch) {
            WatchNode watchNode = new WatchNode (variableHome, watch);
            watches.put (watch, watchNode);
            add (new Node[] {watchNode});
        }

        /** Adds new subnode representing given watch */
        private void addVariable (AbstractVariable variable) {
            add (new Node[] {new VariableNode (variableHome, variable, true)});
        }

        /** Removes subnode which represents given watch */
        private void removeWatch (AbstractWatch watch) {
            WatchNode watchNode = (WatchNode) watches.remove (watch);
            if (watchNode == null) return;
            remove (new Node[] {watchNode});
        }

        /** Removes subnode which represents given watch */
        void removeVariable (VariableNode variable) {
            remove (new Node[] {variable});
        }
    } // end of WatchesRootNodeChildren inner class

    /**
    * Serializable node reference. 
    */
    private static class WatchesHandle implements Node.Handle {
        static final long serialVersionUID =-4518262478987259653L;

        /** Reconstitute the node for this handle.
        *
        * @return the node for this handle
        * @exception IOException if the node cannot be created
        */
        public Node getNode () {
            return DebuggerModule.getWatchesRootNode ();
        }
    }
}


/*
* Log
*  10   Gandalf-post-FCS1.8.3.0     3/28/00  Daniel Prusa    
*  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
*  8    Gandalf   1.7         1/3/00   Daniel Prusa    bugfix for fixed watches
*  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
*  6    Gandalf   1.5         11/5/99  Jan Jancura     Default action updated
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/13/99 Jan Jancura     Destroy action  bug in 
*       deleting watches  deserializing of main window
*  3    Gandalf   1.2         10/1/99  Jan Jancura     Bug 3677
*  2    Gandalf   1.1         9/2/99   Jan Jancura     
*  1    Gandalf   1.0         8/17/99  Jan Jancura     
* $
*/
