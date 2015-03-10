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

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import org.openide.TopManager;
import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

public class NodeExploreAction extends NodeAction {

    private static final long serialVersionUID =9138110746394596949L;
    protected void performAction (Node[] nodes) {
        // do nothing -- should never be called
    }

    protected boolean enable (Node[] nodes) {
        return nodes.length == 1;
    }

    public String getName () {
        return "Explore node...";
    }

    protected String iconResource () {
        return "/org/netbeans/modules/apisupport/resources/NodeExploreIcon.gif";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser.nodeinfo");
    }

    public JMenuItem getMenuPresenter() {
        if (anythingToDisplay ())
            return new SpecialSubMenu (this, getModel (), false);
        else
            return new JInlineMenu (); // do not display at all
    }

    public JMenuItem getPopupPresenter() {
        if (anythingToDisplay ())
            return new SpecialSubMenu (this, getModel (), true);
        else
            return new JInlineMenu (); // do not display at all
    }

    private boolean anythingToDisplay () {
        // test whether or not there will be anything to display in the submenu
        // if false, whole item will just disappear
        return true;
    }

    private static ActSubMenuModel model = null;
    static ActSubMenuModel getModel () {
        if (model == null)
            model = new ActSubMenuModel ();
        return model;
    }

    private Node[] getActivatedNodes0 () {
        return getActivatedNodes ();
    }

    /** Special submenu which notifies model when it is added as a component.
    */
    private static final class SpecialSubMenu extends Actions.SubMenu {

        // private static final long serialVersionUID = ...;

        private static final long serialVersionUID =-8517802693603421361L;
        SpecialSubMenu (SystemAction action, Actions.SubMenuModel model, boolean popup) {
            super (action, model, popup);
        }

        public void addNotify () {
            NodeExploreAction.getModel ().addNotify ();
            super.addNotify ();
        }

        // removeNotify not useful--might be called before action is invoked

    }

    /** Model to use for the submenu.
    */
    private static final class ActSubMenuModel implements Actions.SubMenuModel {
        /**
         * @associates String 
         */
        private List displayNames;

        /**
         * @associates Explorer 
         */
        private List associatedInfo;

        /**
         * @associates ChangeListener 
         */
        private Set listeners = new HashSet (); // Set<ChangeListener>

        public int getCount() {
            return displayNames.size ();
        }

        public String getLabel (int index) {
            return (String) displayNames.get (index);
        }

        public HelpCtx getHelpCtx (int index) {
            return HelpCtx.DEFAULT_HELP; // could add something special here, or new HelpCtx (NodeExploreAction.class)
        }

        public void performActionAt (int index) {
            Object info = associatedInfo.get (index);
            Explorer e = (Explorer) info;
            Node[] nodes = ((NodeExploreAction) SystemAction.get (NodeExploreAction.class)).getActivatedNodes0 ();
            if (nodes.length != 1) throw new Error ();
            Node n = nodes[0];
            if (n instanceof Wrapper) n = ((Wrapper) n).getOriginal ();
            TopManager.getDefault ().getNodeOperation ().explore (e.explore (n));
        }

        public synchronized void addChangeListener (ChangeListener l) {
            listeners.add (l);
        }

        public synchronized void removeChangeListener (ChangeListener l) {
            listeners.remove (l);
        }

        /** You may use this is you have attached other listeners to things that will affect displayNames, for example. */
        private synchronized void fireStateChanged () {
            if (listeners.size () == 0) return;
            ChangeEvent ev = new ChangeEvent (this);
            Iterator it = listeners.iterator ();
            while (it.hasNext ())
                ((ChangeListener) it.next ()).stateChanged (ev);
        }

        void addNotify () {
            displayNames = new ArrayList (5);
            associatedInfo = new ArrayList (5);
            displayNames.add ("Itself");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        return n;
                                    }
                                });
            displayNames.add ("Its clone");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        return n.cloneNode ();
                                    }
                                });
            displayNames.add ("Its parent");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        Node p = n.getParentNode ();
                                        if (p != null) {
                                            return p;
                                        } else {
                                            AbstractNode toret = new AbstractNode (Children.LEAF) {
                                                                     public HelpCtx getHelpCtx () {
                                                                         return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser.nodeinfo");
                                                                     }
                                                                 };
                                            toret.setName ("<No Parent>");
                                            toret.setIconBase ("/org/netbeans/modules/apisupport/resources/NodeExploreIcon");
                                            return toret;
                                        }
                                    }
                                });
            displayNames.add ("Its parent's clone");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        Node p = n.getParentNode ();
                                        if (p != null) {
                                            return p.cloneNode ();
                                        } else {
                                            AbstractNode toret = new AbstractNode (Children.LEAF) {
                                                                     public HelpCtx getHelpCtx () {
                                                                         return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser.nodeinfo");
                                                                     }
                                                                 };
                                            toret.setName ("<No Parent>");
                                            toret.setIconBase ("/org/netbeans/modules/apisupport/resources/NodeExploreIcon");
                                            return toret;
                                        }
                                    }
                                });
            displayNames.add ("Its visualizer");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        return new VisualizerNode (n);
                                    }
                                });
            displayNames.add ("Itself as a bean");
            associatedInfo.add (new Explorer () {
                                    public Node explore (Node n) {
                                        return PropSetKids.makeObjectNode (n);
                                    }
                                });
        }

    }

    interface Explorer {
        // Always the original node here:
        Node explore (Node n);
    }

    private static class VisualizerNode extends AbstractNode {

        VisualizerNode (Node n) {
            this (Visualizer.findVisualizer (n));
        }

        VisualizerNode (TreeNode tn) {
            super (new VisualizerChildren (tn));
            setName ("Visualizer of " + tn);
            // [PENDING] does not work for some reason:
            setIconBase ("/org/netbeans/modules/apisupport/resources/NodeExploreIcon");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser.nodeinfo");
        }

    }

    private static final class VisualizerChildren extends Children.Keys {
        private TreeNode tree;

        public VisualizerChildren (TreeNode n) {
            this.tree = n;
        }

        protected void addNotify () {
            java.util.LinkedList list = new java.util.LinkedList ();
            java.util.Enumeration en = tree.children ();
            while (en.hasMoreElements ()) {
                list.add (en.nextElement ());
            }
            setKeys (list);
        }

        protected void removeNotify () {
            setKeys (java.util.Collections.EMPTY_SET);
        }

        protected Node[] createNodes (Object key) {
            return new Node[] { new VisualizerNode ((TreeNode) key) };
        }

    }

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  3    Gandalf   1.2         12/23/99 Jesse Glick     "Itself as a Bean" 
 *       exploration mode.
 *  2    Gandalf   1.1         10/27/99 Jesse Glick     Sun copyrights.
 *  1    Gandalf   1.0         10/25/99 Jesse Glick     
 * $
 */
