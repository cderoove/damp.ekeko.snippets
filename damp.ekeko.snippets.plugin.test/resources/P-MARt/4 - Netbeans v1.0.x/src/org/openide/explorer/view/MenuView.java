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

package org.openide.explorer.view;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.TopManager;
import org.openide.explorer.*;
import org.openide.nodes.*;
import org.openide.util.WeakListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.awt.JPopupMenuUtils;

/** An explorer view that shows the context hierarchy in
* a popup menu. Initially, it shows a left button which opens a popup
* menu from the root context and a right button which opens a popup menu from the currently
* explored context.
*
* @author  Ian Formanek, Jaroslav Tulach
*/
public class MenuView extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4970665063421766904L;

    /** The explorerManager that manages this view */
    transient private ExplorerManager explorerManager;

    /** button to open root view */
    private JButton root;
    /** button to open view from current node */
    private JButton current;
    /** property change listener */
    transient private Listener listener;

    /* This is the constructor implementation
    * recommended by ExplorerView class that only calls the inherited
    * constructor and leaves the initialization for method initialize().
    * @see #initialize  */
    /** Construct a new menu view.
    */
    public MenuView () {
        setLayout (new java.awt.FlowLayout());

        root = new JButton(NbBundle.getBundle (MenuView.class).getString("MenuViewStartFromRoot"));
        add (root);

        current = new JButton(NbBundle.getBundle (MenuView.class).getString("MenuViewStartFromCurrent"));
        add (current);

        init ();
    }

    /** Initializes listeners */
    private void init () {
        root.addMouseListener (listener = new Listener (true));
        current.addMouseListener (new Listener (false));
    }

    private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject ();
        init ();
    }

    /* Initializes view.
    */
    public void addNotify() {
        super.addNotify ();
        explorerManager = ExplorerManager.find (this);
        explorerManager.addPropertyChangeListener (listener);
        doChecks ();
    }

    /* Deinitializes view.
    */
    public void removeNotify() {
        super.removeNotify ();
        explorerManager.removePropertyChangeListener (listener);
        explorerManager = null;
    }

    /** Does some checks */
    private void doChecks () {
        current.setEnabled (explorerManager.getSelectedNodes ().length == 1);
    }

    /** Listener that opens the menu and listens to its actions
    */
    private class Listener extends MouseAdapter
        implements Acceptor, PropertyChangeListener {
        /** from root */
        private boolean root;

        public Listener (boolean root) {
            this.root = root;
        }

        public void mousePressed (MouseEvent e) {

            if (e.getComponent ().isEnabled ()) {
                // open the popup menu
                Node context = null;
                if (!root) {
                    Node[] sel = explorerManager.getSelectedNodes ();
                    if (sel.length > 0) {
                        context = sel[0];
                    }
                }
                if (context == null) {
                    context = explorerManager.getRootContext();
                }

                Menu menu = new Menu (context, listener);

                JPopupMenu popupMenu = menu.getPopupMenu ();
                java.awt.Point p = new java.awt.Point (e.getX (), e.getY ());
                p.x = e.getX() - p.x;
                p.y = e.getY() - p.y;
                SwingUtilities.convertPointToScreen (p, e.getComponent ());
                Dimension popupSize = popupMenu.getPreferredSize ();
                Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
                if (p.x + popupSize.width > screenSize.width) p.x = screenSize.width - popupSize.width;
                if (p.y + popupSize.height > screenSize.height) p.y = screenSize.height - popupSize.height;
                SwingUtilities.convertPointFromScreen (p, e.getComponent ());
                popupMenu.show(e.getComponent (), p.x, p.y);
            }
        }

        public boolean accept (Node n) {
            try {
                Node parent = n.getParentNode ();
                if (parent != null) {
                    explorerManager.setExploredContext (parent);
                }
                explorerManager.setSelectedNodes (new Node[] { n });
                return true;
            } catch (PropertyVetoException ex) {
                return false;
            }
        }

        public void propertyChange (PropertyChangeEvent ev) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                doChecks ();
            }
        }
    }


    /** Menu item representing a node (with children) in a menu hierarchy.
    * One can attach an acceptor to the menu that will be informed
    * each time a user selects an item whether
    * to close the menu or not.
    */
    public static class Menu extends org.openide.awt.JMenuPlus {

        /** not null if the submenus has not been searched yet */
        private JMenuItem empty;
        /** node change listener */
        private Listener listener;
        /** map from Nodes to JMenuItems. (Node, JMenuItem) 
         * @associates JMenuItem*/
        private HashMap map;

        /** the visualizer for the given node */
        private VisualizerNode vis;

        /** The node represented. */
        protected Node node;

        /** Action listener to attach to all menu items. */
        protected Acceptor action;

        static final long serialVersionUID =-1505289666675423991L;
        /** Constuctor that assigns the node a default
        * action, e.g. to open the Explorer or a property sheet.
        * @param node node to represent
        */
        public Menu (Node node) {
            this (node, DEFAULT_LISTENER);
        }

        /** Constructor that permits specification of the action on the node.
        *
        * @param node node to represent
        * @param action action called when node is selected
        */
        public Menu (Node node, Acceptor action) {
            this (node, action, true);
        }

        /** Constructor that permits specification of the action on the node,
        * and permits overriding the name and icon of the menu.
        *
        * @param node node to represent
        * @param action action called when node selected
        * @param setName <code>true</code> to automatically set the name and icon of the item
        */
        public Menu (final Node node, Acceptor action, boolean setName) {
            this.node = node;
            this.action = action;
            this.vis = VisualizerNode.EMPTY;

            // initialize the visualizer
            Mutex.EVENT.readAccess (new Runnable () {
                                        public void run () {
                                            vis = VisualizerNode.getVisualizer (null, node);
                                        }
                                    });

            listener = new Listener ();
            vis.addNodeModel (listener);

            getPopupMenu ().addPopupMenuListener (listener);
            MenuSelectionManager.defaultManager ().addChangeListener (
                WeakListener.change (listener, MenuSelectionManager.defaultManager ())
            );

            if (setName) {
                MenuItem.initialize (this, node);
            }

            HelpCtx help = node.getHelpCtx ();
            if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
                HelpCtx.setHelpIDString (this, help.getHelpID ());
        }

        /** Checks for {@link MouseEvent#isPopupTrigger right click} to ask the acceptor whether
        * to accept the selection.
        * @param e the mouse event
        * @param path used by the superclass
        * @param manager used by the superclass
        */
        public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
            super.processMouseEvent (e, path, manager);
            if (e.isPopupTrigger () && action.accept (node)) {
                MenuSelectionManager.defaultManager ().clearSelectedPath ();
            }
        }

        /** Create a menu element for a node. The default implementation creates
        * {@link MenuView.MenuItem}s for leafs and <code>Menu</code> for other nodes.
        *
        * @param n node to create element for
        * @return the created node
        */
        protected JMenuItem createMenuItem (Node n) {
            return n.isLeaf () ?
                   (JMenuItem) new MenuItem (n, action) :
                   (JMenuItem) new Menu (n, action);
        }

        /** Changes elements in the menu */
        private void nodesChanged (final boolean check) {
            java.util.List list = vis.getChildren ();
            JPopupMenu popup = getPopupMenu();

            boolean usedToBeContained = JPopupMenuUtils.isPopupContained (popup);

            HashMap remove;
            if (map == null) {
                map = new HashMap (list.size ());
                remove = new HashMap (0);
            } else {
                // objects to remove
                remove = new HashMap (map);
            }

            Iterator it = list.iterator ();
            while (it.hasNext ()) {
                VisualizerNode v = (VisualizerNode)it.next ();
                JMenuItem menu = (JMenuItem)map.get (v);
                if (menu == null) {
                    menu = createMenuItem (Visualizer.findNode (v));
                    map.put (v, menu);
                    Menu.this.add (menu);
                } else {
                    // do not remove the node
                    remove.remove (v);
                }
            }

            it = remove.values ().iterator ();
            while (it.hasNext ()) {
                JMenuItem menu = (JMenuItem)it.next ();
                //            menu.setPopupMenuVisible (false);
                Menu.this.remove (menu);
            }

            // work with empty element
            JMenuItem mi = empty;
            if (mi != null) {
                if (getMenuComponentCount() > 1) {
                    Menu.this.remove (mi);
                    empty = null;
                }
            } else {
                if (getMenuComponentCount () == 0) {
                    empty = new JMenuItem(
                                NbBundle.getBundle(MenuView.class).getString("EmptySubMenu")
                            );
                    empty.setEnabled(false);
                    Menu.this.add (empty);
                }
            }

            popup.pack ();
            popup.invalidate ();
            Component c = popup.getParent ();
            if (c != null) {
                c.validate ();
            }

            JPopupMenuUtils.dynamicChangeToSubmenu(popup, usedToBeContained);

        } // end nodesChanged


        /** Method to check whether a component is part of selection.
        */
        private static boolean isPart (MenuElement me) {
            MenuSelectionManager msm = MenuSelectionManager.defaultManager ();
            MenuElement[] path = msm.getSelectedPath ();
            for (int i = 0; i < path.length; i++) {
                if (me == path[i]) {
                    return true;
                }
            }
            return false;
        }

        /** Listener to changes in nodes */
        private final class Listener extends Object
            implements PopupMenuListener, ChangeListener, NodeModel {
            public void stateChanged(javax.swing.event.ChangeEvent p1) {
                if (
                    Menu.this.isPopupMenuVisible () &&
                    !isPart (Menu.this)
                ) {
                    Menu.this.setPopupMenuVisible (false);
                }
            }

            /** Notification of children addded event. Modifies the list of nodes
            * and fires info to all listeners.
            */
            public void added(VisualizerEvent.Added ev) {
                getPopupMenu ().removePopupMenuListener (this);
                nodesChanged (true);
                getPopupMenu ().addPopupMenuListener (this);
            }
            /** Notification that children has been removed. Modifies the list of nodes
            * and fires info to all listeners.
            */
            public void removed(VisualizerEvent.Removed ev) {
                getPopupMenu ().removePopupMenuListener (this);
                nodesChanged (true);
                getPopupMenu ().addPopupMenuListener (this);
            }
            /** Notification that children has been reordered. Modifies the list of nodes
            * and fires info to all listeners.
            */
            public void reordered(VisualizerEvent.Reordered ev) {
            }

            /** Update a visualizer (change of name, icon, description, etc.)
            */
            public void update(VisualizerNode v) {
            }

            public void popupMenuWillBecomeInvisible(final javax.swing.event.PopupMenuEvent p1) {
            }

            public void popupMenuCanceled(final javax.swing.event.PopupMenuEvent p1) {
            }

            public void popupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent p1) {
                getPopupMenu ().removePopupMenuListener (this);
                nodesChanged (false);
                getPopupMenu ().addPopupMenuListener (this);
            }
        }
    }

    /** Acceptor that can be passed to constructor of {@link MenuView.Menu}.
    * It permits determination of which nodes should be accepted upon a click.
    */
    public static interface Acceptor {
        /** Test whether to accept the node or not. Can also perform some actions (such as opening the node, etc.).

        * @param n the node
        * @return true if the <code>menu</code> should close
        */
        public boolean accept (Node n);
    }

    // [PENDING] this should rather look for the node's default action! --jglick
    /** default listener that opens explorer */
    static final Acceptor DEFAULT_LISTENER = new Acceptor () {
                public boolean accept (Node n) {
                    TopManager.NodeOperation op = TopManager.getDefault ().getNodeOperation ();
                    if (n.isLeaf ()) {
                        op.showProperties (n);
                    } else {
                        op.explore (n);
                    }
                    return true;
                }
            };


    /** Menu item that can represent one node in the tree.
    */
    public static class MenuItem extends JMenuItem {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -918973978614344429L;

        /** The node represented. */
        protected Node node;

        /** The action listener to attach to all menu items. */
        protected Acceptor action;

        /** Construct item for given node with the node's default action.
        * @param node the node to represent
        */
        public MenuItem (Node node) {
            this (node, DEFAULT_LISTENER);
        }

        /** Construct item for given node, specifying an action.
        * @param node the node to represent
        * @param l the acceptor to decide whether to accept this node or not
        */
        public MenuItem (Node node, Acceptor l) {
            this (node, l, true);
        }

        /** Construct item for given node, specifying the action and whether to create the icon and name automatically.
        * @param node the node to represent
        * @param l the acceptor to decide whether to accept this node or not
        * @param setName <code>false</code> if the name and icon should not be set
        */
        public MenuItem (Node node, Acceptor l, boolean setName) {
            super ();

            this.node = node;
            this.action = l;

            if (setName) {
                initialize (this, node);
            }

            HelpCtx help = node.getHelpCtx ();
            if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
                HelpCtx.setHelpIDString (this, help.getHelpID ());
        }

        /** Inform the acceptor.
        * @param time see superclass
        */
        public void doClick (int time) {
            action.accept (node);
        }

        /** Initialize an item for a node.
        */
        static void initialize (JMenuItem item, Node node) {
            item.setIcon (new ImageIcon(node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)));
            item.setText (node.getDisplayName ());
            /*
                  item.setMargin(new java.awt.Insets(0, 0, 0, 0));
                  item.setHorizontalTextPosition(RIGHT);
                  item.setHorizontalAlignment(LEFT);
            */
        }
    }


}

/*
 * Log
 *  23   Gandalf   1.22        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  22   Gandalf   1.21        1/11/00  Ian Formanek    Removed 1.3 specific 
 *       code, as the menu problem in JDK 1.3 is now working with the same code
 *  21   Gandalf   1.20        12/2/99  Jaroslav Tulach On 1.3 does not use the 
 *       setVisible false/true hack.  
 *  20   Gandalf   1.19        11/26/99 Patrik Knakal   
 *  19   Gandalf   1.18        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        9/25/99  Jaroslav Tulach #3727
 *  16   Gandalf   1.15        9/24/99  Jaroslav Tulach Does not blick so often 
 *       when nodes are added/removed.
 *  15   Gandalf   1.14        9/6/99   Jaroslav Tulach Closing submenu when 
 *       menu is closed.
 *  14   Gandalf   1.13        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  13   Gandalf   1.12        7/27/99  Jaroslav Tulach Again and better
 *  12   Gandalf   1.11        7/27/99  Jaroslav Tulach Should close all popup 
 *       menus in the New from template
 *  11   Gandalf   1.10        7/22/99  Jaroslav Tulach Handles 
 *       lightweight/heavyweight state change.
 *  10   Gandalf   1.9         7/16/99  Jesse Glick     Context help.
 *  9    Gandalf   1.8         6/28/99  Ian Formanek    NbJMenu renamed to 
 *       JMenuPlus
 *  8    Gandalf   1.7         6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  7    Gandalf   1.6         6/10/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/20/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         2/5/99   Jaroslav Tulach Changed new from 
 *       template action
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    added readObject, made serializable (hopefully)
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    changed the predecessor from JPanel to Object, a new JPanel is
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    returned from getComponent
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    extends ExplorerViewSupport
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.31        --/--/98 Jan Formanek    got rid of Explorer.getExplorerBundle
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.41        --/--/98 Jan Formanek    reflecting changes in ExplorerView
 */
