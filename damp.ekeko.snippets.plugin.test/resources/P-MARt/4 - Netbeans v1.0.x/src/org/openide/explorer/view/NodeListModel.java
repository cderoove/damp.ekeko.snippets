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
import java.util.*;

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

/** Model for displaying the nodes in list and choice.
*
* @author Jaroslav Tulach
*/
public class NodeListModel extends AbstractListModel implements ComboBoxModel {
    /** listener used to listen to changes in trees */
    private transient Listener listener;
    /** parent node */
    private transient VisualizerNode parent;
    /** originally selected item */
    private transient Object selectedObject;
    /** previous size */
    private transient int size;
    /** depth to display */
    private int depth = 1;

    /** map that assignes to each visualizer number of its children till
    * the specified depth. (VisualizerNode, Info)
    * @associates Info
    */
    private Map childrenCount;

    static final long serialVersionUID =-1926931095895356820L;
    /** Creates new NodeTreeModel
    */
    public NodeListModel () {
        parent = VisualizerNode.EMPTY;
        selectedObject = VisualizerNode.EMPTY;
        clearChildrenCount ();
    }

    /** Creates new NodeTreeModel
    * @param root the root of the model
    */
    public NodeListModel (Node root) {
        parent = VisualizerNode.EMPTY;
        setNode (root);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode (final Node root) {
        Mutex.EVENT.readAccess (new Runnable () {
                                    public void run () {
                                        VisualizerNode v = VisualizerNode.getVisualizer (null, root);

                                        if (v == parent) {
                                            // no change
                                            return;
                                        }

                                        removeAll ();
                                        parent.removeNodeModel (listener ());

                                        parent = v;
                                        selectedObject = v;
                                        clearChildrenCount ();

                                        parent.addNodeModel (listener ());
                                        addAll ();
                                    }
                                });
    }

    /** Depth of nodes to display.
    * @param depth the depth
    */
    public void setDepth (int depth) {
        if (depth != this.depth) {
            this.depth = depth;
            clearChildrenCount ();

            Mutex.EVENT.readAccess (new Runnable () {
                                        public void run () {
                                            removeAll ();
                                            addAll ();
                                        }
                                    });
        }
    }

    /** Getter for depth.
    * @return number of levels to display 
    */
    public int getDepth () {
        return depth;
    }


    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener () {
        if (listener == null) {
            listener = new Listener (this);
        }
        return listener;
    }

    //
    // model methods
    //

    /** Number of elements in the model.
    */
    public int getSize () {
        int s = findSize (parent, -1, depth);
        return s;
    }

    /** Child at given index.
    */
    public Object getElementAt (int i) {
        return findElementAt (parent, i, -1, depth);
    }

    /** Finds index of given object.
    * @param obj object produced by this model
    * @return if the object is not in the list, then return -1
    */
    public int getIndex (Object o) {
        getSize ();
        Info i = (Info)childrenCount.get (o);
        return i == null ? -1 : i.index;
    }

    /** Currently selected item.
    */
    public void setSelectedItem (Object anObject) {
        if (
            selectedObject != anObject
        ) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }
    }

    public Object getSelectedItem() {
        return selectedObject;
    }

    //
    // modification of the counting model
    //

    private void clearChildrenCount () {
        childrenCount = new HashMap (17);
    }

    /** Finds size of sub children excluding vis node.
    * 
    * @param vis the visualizer to find the size for
    * @param index the index that should be assigned to vis
    * @param depth the depth to scan
    * @return number of children
    */
    private int findSize (VisualizerNode vis, int index, int depth) {
        Info info = (Info)childrenCount.get (vis);
        if (info != null) {
            return info.childrenCount;
        }

        // only my children
        int size = 0;

        info = new Info ();
        info.depth = depth;
        info.index = index;


        if (depth-- > 0) {
            Iterator it = vis.getChildren ().iterator ();
            while (it.hasNext ()) {
                VisualizerNode v = (VisualizerNode)it.next ();
                // count node v
                size++;

                // now count children of node v
                size += findSize (v, index + size, depth);
            }
        }

        info.childrenCount = size;
        childrenCount.put (vis, info);
        return size;
    }

    /** Finds the child with requested index.
    * 
    * @param vis the visualizer to find the size for
    * @param indx the index of requested child
    * @param depth the depth to scan
    * @return the children
    */
    private VisualizerNode findElementAt (
        VisualizerNode vis, int indx, int realIndx, int depth
    ) {
        if (--depth == 0) {
            // last depth is handled in special way
            return (VisualizerNode)vis.getChildAt(indx);
        }

        Iterator it = vis.getChildren ().iterator ();
        while (it.hasNext ()) {
            VisualizerNode v = (VisualizerNode)it.next ();

            if (indx-- == 0) {
                return v;
            }

            int s = findSize (v, ++realIndx, depth);


            if (indx < s) {
                // search this child
                return findElementAt (v, indx, realIndx, depth);
            }

            // go to next child
            indx -= s;
            realIndx += s;
        }

        return vis;
    }

    /** Finds a depth for given model & object. Used from renderer.
    * @param m model
    * @param o the visualizer node
    * @return depth or 0 if not found
    */
    static int findVisualizerDepth (ListModel m, VisualizerNode o) {
        if (m instanceof NodeListModel) {
            NodeListModel n = (NodeListModel)m;
            Info i = (Info)n.childrenCount.get (o);
            if (i != null) {
                return n.depth - i.depth - 1;
            }
        }
        return 0;
    }

    //
    // Modifications
    //

    final void addAll () {
        size = getSize ();
        if (size > 0) {
            fireIntervalAdded (this, 0, size - 1);
        }
    }

    final void removeAll () {
        if (size > 0) {
            fireIntervalRemoved (this, 0, size - 1);
        }
    }

    final void changeAll () {
        if (size > 0) {
            fireContentsChanged (this, 0, size - 1);
        }
        clearChildrenCount ();
    }

    final void added (VisualizerEvent.Added ev) {
        int[] indices = ev.getArray ();
        if (indices.length == 1) {
            // special handling
            size++;
            fireIntervalAdded (this, indices[0], indices[0]);
        } else {
            removeAll ();
            clearChildrenCount ();
            addAll ();
        }
        clearChildrenCount ();
    }

    final void removed (VisualizerEvent.Removed ev) {
        int[] indices = ev.getArray ();
        if (indices.length == 1) {
            // special handling
            size--;
            fireIntervalRemoved (this, indices[0], indices[0]);
        } else {
            removeAll ();
            addAll ();
        }
        clearChildrenCount ();
    }

    final void update (VisualizerNode v) {
        // ensure the model is computed
        getSize ();
        Info i = (Info)childrenCount.get (v);
        if (i != null) {
            fireContentsChanged (this, i.index, i.index);
        }
    }

    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference model;

        /** Constructor.
        */
        public Listener (NodeListModel m) {
            model = new WeakReference (m);
        }

        /** Getter for the model or null.
        */
        private NodeListModel get (VisualizerEvent ev) {
            NodeListModel m = (NodeListModel)model.get ();
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
            NodeListModel m = get (ev);
            if (m == null) return;

            m.added (ev);
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void removed (VisualizerEvent.Removed ev) {
            NodeListModel m = get (ev);
            if (m == null) return;

            m.removed (ev);
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void reordered (VisualizerEvent.Reordered ev) {
            NodeListModel m = get (ev);
            if (m == null) return;

            m.changeAll ();
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        public void update(VisualizerNode v) {
            NodeListModel m = get (null);
            if (m == null) return;

            m.update (v);
        }
    }

    /** Info for a component in model
    */
    private static final class Info extends Object {
        public int childrenCount;
        public int depth;
        public int index;

        public String toString () {
            return "Info[childrenCount=" + childrenCount + ", depth=" + depth + // NOI18N
                   ", index=" + index; // NOI18N
        }
    }
}

/*
* Log
*  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
*  8    Gandalf   1.7         12/3/99  Jaroslav Tulach #4864
*  7    Gandalf   1.6         11/29/99 Jaroslav Tulach Initilized even no root 
*       set.
*  6    Gandalf   1.5         11/26/99 Patrik Knakal   
*  5    Gandalf   1.4         11/24/99 Jaroslav Tulach Keeps nodes in memory.
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         10/6/99  Jaroslav Tulach #4068
*  2    Gandalf   1.1         8/27/99  Jaroslav Tulach List model can display 
*       more levels at once.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/