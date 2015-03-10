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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeNode;

import org.openide.nodes.*;
import org.openide.util.Mutex;
import org.openide.util.WeakSet;
import org.openide.util.WeakListener;

/** Visual representation of one node. Holds necessary information about nodes
* like icon, name, description and also list of its children. 
* <P>
* There is at most one VisualizerNode for one node. All of them are hold in a cache.
* <P>
* The VisualizerNode level provides secure layer between Nodes and Swing AWT dispatch 
* thread.
*
* @author Jaroslav Tulach
*/
final class VisualizerNode extends EventListenerList
    implements NodeListener, TreeNode, Runnable {
    /** one template to use for searching for visualizers */
    private static final VisualizerNode TEMPLATE = new VisualizerNode (0);

    /** constant holding empty reference to children */
    private static final Reference NO_REF = new WeakReference (null);

    /** cache of visializers (VisualizerNode, Reference (VisualizerNode)) */
    private static WeakHashMap cache = new WeakHashMap ();

    /** empty visualizer */
    public static final VisualizerNode EMPTY = getVisualizer (null, Node.EMPTY);

    /** Finds VisualizerNode for given node. Can be called only from EventQueue.
    * @param ch the children this visualizer should belong to
    * @param n the node
    * @return the visualizer
    */
    public static VisualizerNode getVisualizer (VisualizerChildren ch, Node n) {
        TEMPLATE.hashCode = System.identityHashCode (n);

        Reference r = (Reference)cache.get (TEMPLATE);
        VisualizerNode v = r == null ? null : (VisualizerNode)r.get ();
        if (v == null) {
            v = new VisualizerNode (n);
            cache.put (v, new WeakReference (v));
        }
        if (ch != null) {
            v.parent = ch;
        }
        return v;
    }


    /** node */
    final Node node;
    /** system hashcode of the node */
    private int hashCode;
    /** visualizer children attached thru weak references Reference (VisualizerChildren) */
    private Reference children = NO_REF;
    /** the VisualizerChildren that contains this VisualizerNode or null */
    private VisualizerChildren parent;
    /** listener on UI */
    private PropertyChangeListener uiListener;

    /** cached name */
    public String name;
    /** cached display name */
    public String displayName;
    /** cached short description */
    public String shortDescription;


    /** renderer tree or null */
    private NodeRenderer.Tree rendererTree;
    /** renderer list or null */
    private NodeRenderer.List rendererList;
    /** renderer listpane or null */
    private NodeRenderer.Pane rendererPane;

    static final long serialVersionUID =3726728244698316872L;
    /** Constructor that creates template for the node.
    */
    private VisualizerNode (int hashCode) {
        this.hashCode = hashCode;
        this.node = null;
    }

    /** Creates new VisualizerNode
    * @param n node to refer to
    */
    private VisualizerNode(Node n) {
        node = n;
        hashCode = System.identityHashCode (node);

        // attach as a listener
        node.addNodeListener (WeakListener.node (this, node));
        uiListener = WeakListener.propertyChange (this, null);
        UIManager.addPropertyChangeListener (uiListener);

        name = node.getName ();
        displayName = node.getDisplayName ();
        shortDescription = node.getShortDescription ();
    }

    /** When finalized remove the ui listener */
    protected void finalize () {
        UIManager.removePropertyChangeListener (uiListener);
    }

    /** Getter for list of children of this visualizer.
    * @return list of VisualizerNode objects
    */
    public List getChildren () {
        VisualizerChildren ch = (VisualizerChildren)children.get ();
        if (ch == null && !node.isLeaf ()) {
            // go into lock to ensure that no childrenAdded, childrenRemoved,
            // childrenReordered notifications occures and that is why we do
            // not loose any changes
            ch = (VisualizerChildren)Children.MUTEX.readAccess (new Mutex.Action () {
                        public Object run () {
                            Node[] nodes = node.getChildren ().getNodes ();
                            VisualizerChildren vc = new VisualizerChildren (
                                                        VisualizerNode.this, nodes
                                                    );
                            notifyVisualizerChildrenChange (nodes.length, vc);
                            return vc;
                        }
                    });
        }
        return ch == null ? Collections.EMPTY_LIST : ch.list;
    }

    //
    // TreeNode interface (callable only from AWT-Event-Queue)
    //

    public int getIndex(final javax.swing.tree.TreeNode p1) {
        return getChildren ().indexOf (p1);
    }

    public boolean getAllowsChildren() {
        return !isLeaf ();
    }

    public javax.swing.tree.TreeNode getChildAt(int p1) {
        return (VisualizerNode)getChildren ().get (p1);
    }

    public int getChildCount() {
        return getChildren ().size ();
    }

    public java.util.Enumeration children() {
        return java.util.Collections.enumeration (getChildren ());
    }

    public boolean isLeaf() {
        return node.isLeaf ();
    }

    public javax.swing.tree.TreeNode getParent() {
        Node parent = node.getParentNode ();
        return parent == null ? null : getVisualizer (null, parent);
    }

    // ***************
    // Rendering stuff (only from AWT-QUEUE)
    // ***************

    /** Tree renderer for this node.
    */
    public NodeRenderer.Tree getTree () {
        if (rendererTree == null) {
            rendererTree = new NodeRenderer.Tree ();
            rendererTree.update (this);
        }
        return rendererTree;
    }

    /** List renderer for this node.
    */
    public NodeRenderer.List getList () {
        if (rendererList == null) {
            rendererList = new NodeRenderer.List ();
            rendererList.update (this);
        }
        return rendererList;
    }

    /** Pane renderer for this node.
    */
    public NodeRenderer.Pane getPane () {
        if (rendererPane == null) {
            rendererPane = new NodeRenderer.Pane ();
            rendererPane.update (this);
        }
        return rendererPane;
    }

    // **********************************************
    // Can be called under Children.MUTEX.writeAccess
    // **********************************************


    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
        VisualizerChildren ch = (VisualizerChildren)children.get ();
        if (ch == null) return;

        SwingUtilities.invokeLater (new VisualizerEvent.Added (
                                        ch, ev.getDelta (), ev.getDeltaIndices ()
                                    ));
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
        VisualizerChildren ch = (VisualizerChildren)children.get ();
        if (ch == null) return;

        SwingUtilities.invokeLater (new VisualizerEvent.Removed (
                                        ch, ev.getDeltaIndices ()
                                    ));
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
        VisualizerChildren ch = (VisualizerChildren)children.get ();
        if (ch == null) return;

        SwingUtilities.invokeLater (new VisualizerEvent.Reordered (
                                        ch, ev.getPermutation ()
                                    ));
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev) {
        // ignore for now
    }

    /** Change in the node properties (icon, etc.)
    */
    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        String name = evt.getPropertyName ();
        if (
            Node.PROP_NAME.equals (name) ||
            Node.PROP_DISPLAY_NAME.equals (name) ||
            Node.PROP_SHORT_DESCRIPTION.equals (name) ||
            Node.PROP_ICON.equals (name) ||
            Node.PROP_OPENED_ICON.equals (name)
        ) {
            SwingUtilities.invokeLater (this);
            return;
        }

        if (
            "lookAndFeel".equals (name) // NOI18N
        ) {
            rendererTree = null;
            rendererList = null;
            rendererPane = null;
            SwingUtilities.invokeLater (this);
        }
    }

    /** Update the state of this class by retrieving new name, etc.
    * And fire change to all listeners. Only by AWT-Event-Queue
    */
    public void run () {
        name = node.getName ();
        displayName = node.getDisplayName ();
        shortDescription = node.getShortDescription ();

        //
        // update renderers
        //
        NodeRenderer.Tree t = rendererTree;
        if (t != null) {
            t.update (this);
        }

        NodeRenderer.List l = rendererList;
        if (l != null) {
            l.update (this);
        }

        NodeRenderer.Pane p = rendererPane;
        if (p != null) {
            p.update (this);
        }

        //
        // notify models
        //
        VisualizerNode parent = this;
        while (parent != null) {
            Object[] listeners = parent.getListenerList ();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel)listeners[i]).update (this);
            }
            parent = (VisualizerNode)parent.getParent ();
        }
    }


    //
    // Access to VisualizerChildren
    //

    /** Notifies change in the amount of children. This is used to distinguish between
    * weak and hard reference. Called from VisualizerChildren
    * @param size amount of children
    * @param ch the children
    */
    void notifyVisualizerChildrenChange (int size, VisualizerChildren ch) {
        if (size == 0) {
            // hold the children hard
            children = new StrongReference (ch);
        } else {
            children = new WeakReference (ch);
        }
    }

    // ********************************
    // This can be called from anywhere
    // ********************************

    /** Adds visualizer listener.
    */
    public synchronized void addNodeModel (NodeModel l) {
        add (NodeModel.class, l);
    }

    /** Removes visualizer listener.
    */
    public synchronized void removeNodeModel (NodeModel l) {
        remove (NodeModel.class, l);
    }

    /** Hash code
    */
    public int hashCode () {
        return hashCode;
    }

    /** Equals two objects are equal if they have the same hash code
    */
    public boolean equals (Object o) {
        if (!(o instanceof VisualizerNode)) return false;
        VisualizerNode v = (VisualizerNode)o;
        return v.hashCode == hashCode;
    }

    /** String name is taken from the node.
    */
    public String toString () {
        return displayName;
    }

    /** Strong reference.
    */
    private static final class StrongReference extends WeakReference {
        private Object o;
        public StrongReference (Object o) {
            super (null);
            this.o = o;
        }

        public Object get () {
            return o;
        }
    }
}

/*
* Log
*  11   Gandalf   1.10        1/12/00  Ian Formanek    NOI18N
*  10   Gandalf   1.9         1/8/00   Jaroslav Tulach Memory leak fix.  
*  9    Gandalf   1.8         11/26/99 Patrik Knakal   
*  8    Gandalf   1.7         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  5    Gandalf   1.4         9/17/99  Jaroslav Tulach Change caching.
*  4    Gandalf   1.3         9/7/99   Jaroslav Tulach #2445
*  3    Gandalf   1.2         9/1/99   Jaroslav Tulach Holding of children is a 
*       bit stronger.
*  2    Gandalf   1.1         8/27/99  Jaroslav Tulach List model can display 
*       more levels at once.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/