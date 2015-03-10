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

import java.beans.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.*;

import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.Children;

/** Model for displaying the nodes in tree.
*
* @author Jaroslav Tulach
*/
public class NodeTreeModel extends DefaultTreeModel {
    /** listener used to listen to changes in trees */
    private transient Listener listener;

    static final long serialVersionUID =1900670294524747212L;
    /** Creates new NodeTreeModel
    */
    public NodeTreeModel () {
        super (VisualizerNode.EMPTY, true);
    }

    /** Creates new NodeTreeModel
    * @param root the root of the model
    */
    public NodeTreeModel (Node root) {
        super (VisualizerNode.EMPTY, true);
        setNode (root);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode (final Node root) {
        Mutex.EVENT.readAccess (new Runnable () {
                                    public void run () {
                                        VisualizerNode v = (VisualizerNode)getRoot ();
                                        VisualizerNode nr = VisualizerNode.getVisualizer (null, root);

                                        if (v == nr) {
                                            // no change
                                            return;
                                        }

                                        v.removeNodeModel (listener ());

                                        nr.addNodeModel (listener ());
                                        setRoot (nr);
                                    }
                                });
    }

    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener () {
        if (listener == null) {
            listener = new Listener (this);
        }
        return listener;
    }

    /**
    * This sets the user object of the TreeNode identified by path
    * and posts a node changed.  If you use custom user objects in
    * the TreeModel you'returngoing to need to subclass this and
    * set the user object of the changed node to something meaningful.
    */
    public void valueForPathChanged(TreePath path, Object newValue) {
        Object o = path.getLastPathComponent();
        if (o instanceof VisualizerNode) {
            nodeChanged ((VisualizerNode)o);
            return;
        }
        MutableTreeNode   aNode = (MutableTreeNode)o;

        aNode.setUserObject(newValue);
        nodeChanged(aNode);
    }


    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference model;

        /** Constructor.
        */
        public Listener (NodeTreeModel m) {
            model = new WeakReference (m);
        }

        /** Getter for the model or null.
        */
        private NodeTreeModel get (VisualizerEvent ev) {
            NodeTreeModel m = (NodeTreeModel)model.get ();
            if (m == null && ev != null) {
                ev.getVisualizer ().removeNodeModel (this);
                return null;
            }
            return m;
        }

        /** Notification of children addded event. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void added (VisualizerEvent.Added ev) {
            NodeTreeModel m = get (ev);
            if (m == null) return;

            m.nodesWereInserted (ev.getVisualizer (), ev.getArray ());
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void removed (VisualizerEvent.Removed ev) {
            NodeTreeModel m = get (ev);
            if (m == null) return;

            m.nodesWereRemoved (ev.getVisualizer (), ev.getArray (), ev.removed.toArray ());
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void reordered (VisualizerEvent.Reordered ev) {
            NodeTreeModel m = get (ev);
            if (m == null) return;

            m.nodeStructureChanged (ev.getVisualizer ());
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        public void update(VisualizerNode v) {
            NodeTreeModel m = get (null);
            if (m == null) return;
            m.nodeChanged (v);
        }
    }

    /*
      
      public static void main (String[] args) {
        Node n = org.openide.TopManager.getDefault ().getPlaces ().nodes ().repository ();
        
        org.openide.windows.TopComponent c = new org.openide.windows.TopComponent ();
        c.setLayout (new java.awt.BorderLayout ());
        
        JTree tree = new JTree ();
        NodeTreeModel m = new NodeTreeModel (n);
        tree.setModel (m);
        tree.setCellRenderer (new NodeRenderer ());
        c.add ("Center", tree);
        
        JList list = new JList ();
        NodeListModel l = new NodeListModel (n);
        l.setDepth (3);
        list.setModel (l);
        list.setCellRenderer (new NodeRenderer ());
        
        c.add ("East", list);
        c.open ();
      }
      */
}

/*
* Log
*  6    Gandalf   1.5         11/26/99 Patrik Knakal   
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/22/99  Petr Hamernik   fixed #3808
*  3    Gandalf   1.2         9/14/99  Jaroslav Tulach Fires tree structure 
*       change on reorder.
*  2    Gandalf   1.1         8/27/99  Jaroslav Tulach List model can display 
*       more levels at once.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/