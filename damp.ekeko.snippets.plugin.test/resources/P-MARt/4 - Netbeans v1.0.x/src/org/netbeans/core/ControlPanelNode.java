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

package org.netbeans.core;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.options.*;
import org.openide.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;

/** This object represents all system options stored in ControlPanel.
*
* @author Petr Hamernik, Jaroslav Tulach, Dafe Simonek
*/
class ControlPanelNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 472235064406221888L;

    /** default instance */
    private static Node controlPanel;
    /** default project setggins */
    private static Node projectSettings;

    /** Default icon base for control panel. */
    private static final String CP_ICON_BASE =
        "/org/netbeans/core/resources/controlPanel"; // NOI18N

    /** used during deserialization */
    // private transient Node[] ret;

    /** Default no-arg constructor */
    ControlPanelNode () {
        this(NbControlPanel.getDefault());
    }

    /** Constructor
    * @param childrenDataRep Concrete implementation of Map interface used
    * for children representation.
    * @param options Option pool, which content we represent in nodes.
    */
    private ControlPanelNode (NbControlPanel options) {
        // maybe suggests sorted collection as children implementation,
        // so they can be sorted.
        super(new ControlPanelChildren(options));
        initialize();
        // setSerializeChildren (false); // ? serialization not yet known
    }

    /** Default instance of the node.
    * @return the node
    */
    public static synchronized Node getDefault () {
        if (controlPanel == null) {
            controlPanel = new ControlPanelNode ();
        }
        return controlPanel;
    }

    /** Getter for project settings.
    */
    public static synchronized Node getProjectSettings () {
        if (projectSettings == null) {
            Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();

            projectSettings = new ControlPanelNode ();
            projectSettings.getChildren ().add (new Node[] {
                                                    ns.repositorySettings ().cloneNode (),
                                                    ns.workspaces ().cloneNode ()
                                                });

            projectSettings.setName (Main.getString ("CTL_Project_Settings"));
            projectSettings.setShortDescription (Main.getString ("CTL_Project_Settings_Hint"));
        }
        return projectSettings;
    }


    private void initialize () {
        setName(NbBundle.getBundle(ControlPanelNode.class).
                getString("CTL_ControlPanel"));
        setIconBase(CP_ICON_BASE);
        getCookieSet ().add (new InstanceSupport.Instance (NbTopManager.getDefault ().getControlPanel ()));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ControlPanelNode.class);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class),
               };
    }


    /** @return a Node.Handle */
    public Node.Handle getHandle() {
        return new Han (this == getDefault ());
    }


    /***** Inner classes **************/

    private static final class Han implements Node.Handle {
        private boolean def;

        static final long serialVersionUID =-2211277830988549398L;
        /** Constructor.
        */
        public Han (boolean def) {
            this.def = def;
        }

        public Node getNode () {
            return def ? getDefault () : getProjectSettings ();
        }
    }

    /** Node representing one option in Control Panel*/
    private static class ControlPanelItemNode extends BeanNode {
        /**
        * Constructs BeanNode for the bean.
        *
        * @param theBean bean for which we can construct BeanNode
        * @param parent The parent of this node.
        */
        public ControlPanelItemNode(SystemOption option) throws IntrospectionException {
            super(option);
        }


        /** Cannot be removed
        */
        public boolean canDestroy () {
            return false;
        }

        /** Cannot be cut
        */
        public boolean canCut () {
            return false;
        }

        /** Clone the node. If the object implements {@link Cloneable},
        * that is used; otherwise a {@link FilterNode filter node}
        * is created.
        *
        * @return copy of this node
        */
        public Node cloneNode () {
            try {
                return new ControlPanelItemNode ((SystemOption)getBean ());
            } catch (IntrospectionException e) {
                return super.cloneNode ();
            }
        }

    } // end of ControlPanelItemNode

    /** Implementation of children for ControlPanel node in explorer.
    * Extends Children.Map implementation to easily achieve nodes to options mapping.
    * This class listens on changes of underlying options and add or
    * remove nodes if appropriate.
    *
    * @author Dafe Simonek
    * @version 1.00, Oct 26, 1998
    */
    private static final class ControlPanelChildren extends Children.Keys
        implements PropertyChangeListener, NodeListener, java.util.Comparator {
        /** Reference to system option pool */
        private NbControlPanel options;

        /** The only constructor. Non-public, called from ControlPanelNode.
        * Allows for different data representation through Map param.
        */
        ControlPanelChildren (final NbControlPanel options) {
            this.options = options;
            //System.out.println("Options: " +  options); // NOI18N
            // adds listener to changes in options pool
            options.addPropertyChangeListener(WeakListener.propertyChange (this, options));
            if (getNode () == projectSettings) {
                ServicesNode.getDefault ().addNodeListener (
                    WeakListener.node (this, ServicesNode.getDefault ())
                );
            }
            setBefore (true);
        }

        /** Updates options.
        */
        private void update () {
            // take system options from control panel
            List l = Arrays.asList (options.getSystemOptions());
            // use your self as comparator
            Set s = new TreeSet (this);
            s.addAll (l);

            if (getNode () == projectSettings) {
                s.add (ServicesNode.getDefault ());
            }

            setKeys (s);
        }

        /** Added into list
        */
        protected void addNotify () {
            update ();
        }

        /** Create default node array for options taken from NbControlPanel
        * Overrides abstract initMap from Children.Map class.
        */
        protected Node[] createNodes (Object key) {

            if (key == ServicesNode.getDefault ()) {
                Node[] arr = ServicesNode.getDefault ().getChildren ().getNodes ();
                arr = (Node[])arr.clone ();
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = arr[i].cloneNode ();
                }
                return arr;
            }

            SystemOption op = (SystemOption)key;
            try {
                Node n = new ControlPanelNode.ControlPanelItemNode(op);
                return new Node[] { n };
            } catch (IntrospectionException e) {
                // commented out only for testing purposes
                //TopManager.getDefault().notifyException(e);
                return new Node[] { };
            }
        }

        /** Property change listener. Adds and removes options.
        */
        public void propertyChange(final java.beans.PropertyChangeEvent p1) {
            if (p1.getSource () == ServicesNode.getDefault ()) {
                // changes in ServicesNode are cought by following 'children' methods
                return;
            } else {
                update ();
            }
        }

        /** Fired when a set of new children is added.
         * @param ev event describing the action
         */
        public void childrenAdded(NodeMemberEvent ev) {
            refreshKey (ServicesNode.getDefault ());
        }
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            refreshKey (ServicesNode.getDefault ());
        }
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         */
        public void childrenReordered(NodeReorderEvent ev) {
            refreshKey (ServicesNode.getDefault ());
        }

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
        }

        /** Comparator of objects */
        public int compare(Object oi, Object oj) {
            if (oi == oj) return 0;

            if (oi instanceof Node) return -1;
            if (oj instanceof Node) return 1;

            String namei = ((SystemOption) oi).displayName();
            String namej = ((SystemOption) oj).displayName();
            return namei.compareTo(namej);
        }


    } // end of ControlPanelChildren

}

/*
 * Log
 *  23   Gandalf   1.22        1/17/00  Jaroslav Tulach Project refreshes 
 *       children when new service type added/removed.
 *  22   Gandalf   1.21        1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  21   Gandalf   1.20        1/13/00  Jaroslav Tulach I18N
 *  20   Gandalf   1.19        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  19   Gandalf   1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        9/10/99  Jaroslav Tulach Services API.
 *  17   Gandalf   1.16        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  16   Gandalf   1.15        8/3/99   Jaroslav Tulach Project settings node.
 *  15   Gandalf   1.14        7/21/99  Ian Formanek    better cloning of 
 *       control panel items
 *  14   Gandalf   1.13        7/8/99   Jesse Glick     Context help.
 *  13   Gandalf   1.12        6/9/99   Ian Formanek    ToolsAction
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        5/9/99   Ian Formanek    Fixed bug 1655 - 
 *       Renaming of top level nodes is not persistent (removed the possibility 
 *       to rename).
 *  10   Gandalf   1.9         4/16/99  Jaroslav Tulach Changes in children.
 *  9    Gandalf   1.8         3/27/99  Jaroslav Tulach Support for serializing 
 *       beans into folder + implemented for control panel and repository
 *  8    Gandalf   1.7         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  7    Gandalf   1.6         3/18/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Fixed outerclass 
 *       specifiers uncompilable under JDK 1.2
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 anonymous       Temporary fix...
 *  0    Tuborg    0.12        --/--/98 anonymous       Changes to bean.
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach new nodes redesigned
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach extends SystemObject
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   dataobject -> node
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    icon change
 *  0    Tuborg    0.18        --/--/98 Petr Hamernik   renamecookie
 */
