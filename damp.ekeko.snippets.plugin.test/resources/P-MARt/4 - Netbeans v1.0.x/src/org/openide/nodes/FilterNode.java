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

package org.openide.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.FeatureDescriptor;
import java.io.IOException;


import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import java.util.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

/** A proxy for another node.
* Unless otherwise mentioned, all methods of the original node are delegated to.
* If desired, you can disable delegation of certain methods which are concrete in <code>Node</code>
* by calling {@link #disableDelegation}.
*
* @author Jaroslav Tulach
*/
public class FilterNode extends Node {
    /** node to delegate to */
    private Node original;
    /** listener to property changes,
    * accessible thru getPropertyChangeListener
    */
    private PropertyChangeListener propL;
    /** listener to node changes
    * Accessible thru get node listener
    */
    private NodeListener nodeL;

    // Note: int (not long) to avoid need to ever synchronize when accessing it
    // (Java VM spec does not guarantee that long's will be stored atomically)
    /** @see #delegating */
    private int delegateMask;

    /** Whether to delegate <code>setName</code>. */
    protected static final int DELEGATE_SET_NAME = 1 << 0;
    /** Whether to delegate <code>getName</code>. */
    protected static final int DELEGATE_GET_NAME = 1 << 1;
    /** Whether to delegate <code>setDisplayName</code>. */
    protected static final int DELEGATE_SET_DISPLAY_NAME = 1 << 2;
    /** Whether to delegate <code>getDisplayName</code>. */
    protected static final int DELEGATE_GET_DISPLAY_NAME = 1 << 3;
    /** Whether to delegate <code>setShortDescription</code>. */
    protected static final int DELEGATE_SET_SHORT_DESCRIPTION = 1 << 4;
    /** Whether to delegate <code>getShortDescription</code>. */
    protected static final int DELEGATE_GET_SHORT_DESCRIPTION = 1 << 5;
    /** Whether to delegate <code>destroy</code>. */
    protected static final int DELEGATE_DESTROY = 1 << 6;
    /** Whether to delegate <code>getActions</code>. */
    protected static final int DELEGATE_GET_ACTIONS = 1 << 7;
    /** Whether to delegate <code>getContextActions</code>. */
    protected static final int DELEGATE_GET_CONTEXT_ACTIONS = 1 << 8;
    /** Mask indicating delegation of all possible methods. */
    private static final int DELEGATE_ALL = DELEGATE_SET_NAME |
                                            DELEGATE_GET_NAME |
                                            DELEGATE_SET_DISPLAY_NAME |
                                            DELEGATE_GET_DISPLAY_NAME |
                                            DELEGATE_SET_SHORT_DESCRIPTION |
                                            DELEGATE_GET_SHORT_DESCRIPTION |
                                            DELEGATE_DESTROY |
                                            DELEGATE_GET_ACTIONS |
                                            DELEGATE_GET_CONTEXT_ACTIONS;

    /** Create proxy.
    * @param original the node to delegate to
    */
    public FilterNode(Node original) {
        this (
            original,
            original.isLeaf () ?
            org.openide.nodes.Children.LEAF : new Children (original)
        );
    }

    /** Create proxy with a different set of children.
    *
    * @param original the node to delegate to
    * @param children a set of children for this node
    */
    public FilterNode(
        Node original,
        org.openide.nodes.Children children
    ) {
        super (children);
        this.original = original;
        init ();
    }

    /** Initializes the node.
    */
    private void init () {
        original.addPropertyChangeListener (getPropertyChangeListener ());
        original.addNodeListener (getNodeListener ());
        delegateMask = DELEGATE_ALL;
    }

    /** Removes all listeners (property and node) on
    * the original node. Called from {@link NodeListener#nodeDestroyed},
    * but can be called by any subclass to stop reflecting changes
    * in the original node.
    */
    protected void finalize () {
        original.removePropertyChangeListener (getPropertyChangeListener ());
        original.removeNodeListener (getNodeListener ());
    }

    /** Enable delegation of a set of methods.
    * These will be delegated to the original node.
    * Since all available methods are delegated by default, normally you will not need to call this.
    * @param mask bitwise disjunction of <code>DELEGATE_XXX</code> constants
    * @throws IllegalArgumentException if the mask is invalid
    */
    protected final void enableDelegation (int mask) {
        if ((mask & ~DELEGATE_ALL) != 0) throw new IllegalArgumentException ("Bad delegation mask: " + mask); // NOI18N
        delegateMask |= mask;
    }

    /** Disable delegation of a set of methods.
    * The methods will retain their behavior from {@link Node}.
    * <p>For example, if you wish to subclass <code>FilterNode</code>, giving your
    * node a distinctive display name and tooltip, and performing some special
    * action upon deletion, you may do so without risk of affecting the original
    * node as follows:
    * <br><code><pre>
    * public MyNode extends FilterNode {
    *   public MyNode (Node orig) {
    *     super (orig, new MyChildren (orig));
    *     disableDelegation (DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME |
    *                        DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION |
    *                        DELEGATE_DESTROY);
    *     // these will affect only the filter node:
    *     setDisplayName ("Linking -> " + orig.getDisplayName ());
    *     setShortDescription ("Something different.");
    *   }
    *   public boolean canRename () { return false; }
    *   public void destroy () throws IOException {
    *     doMyCleanup ();
    *     super.destroy (); // calls Node.destroy(), not orig.destroy()
    *   }
    * }
    * </pre></code>
    * <br>You may still manually delegate where desired using {@link #getOriginal}.
    * Other methods abstract in <code>Node</code> may simply be overridden without
    * any special handling.
    * @param mask bitwise disjunction of <code>DELEGATE_XXX</code> constants
    * @throws IllegalArgumentException if the mask is invalid
    */
    protected final void disableDelegation (int mask) {
        if ((mask & ~DELEGATE_ALL) != 0) throw new IllegalArgumentException ("Bad delegation mask: " + mask); // NOI18N
        delegateMask &= ~mask;
    }

    /** Test whether we are currently delegating to some method. */
    private final boolean delegating (int what) {
        return (delegateMask & what) != 0;
    }

    /** Create new filter node for the original.
    * Subclasses do not have to override this, but if they do not,
    * the default implementation will filter the subclass filter, which is not
    * very efficient.
    * @return copy of this node
    */
    public Node cloneNode () {
        if (isDefault ()) {
            // this is realy filter node without changed behaviour
            // with the normal children => use normal constructor for the
            // original node
            return new FilterNode (original);
        } else {
            // create filter node for this node to reflect changed
            // behaviour
            return new FilterNode (this);
        }
    }

    // ------------- START OF DELEGATED METHODS ------------

    /* Setter for system name. Fires info about property change.
    * @param s the string
    */
    public void setName (String s) {
        if (delegating (DELEGATE_SET_NAME))
            original.setName (s);
        else
            super.setName (s);
    }

    /* @return the name of the original node
    */
    public String getName () {
        if (delegating (DELEGATE_GET_NAME))
            return original.getName ();
        else
            return super.getName ();
    }

    /* Setter for display name. Fires info about property change.
    * @param s the string
    */
    public void setDisplayName (String s) {
        if (delegating (DELEGATE_SET_DISPLAY_NAME))
            original.setDisplayName (s);
        else
            super.setDisplayName (s);
    }

    /* @return the display name of the original node
    */
    public String getDisplayName () {
        if (delegating (DELEGATE_GET_DISPLAY_NAME))
            return original.getDisplayName ();
        else
            return super.getDisplayName ();
    }

    /* Setter for short description. Fires info about property change.
    * @param s the string
    */
    public void setShortDescription (String s) {
        if (delegating (DELEGATE_SET_SHORT_DESCRIPTION))
            original.setShortDescription (s);
        else
            super.setShortDescription (s);
    }

    /* @return the description of the original node
    */
    public String getShortDescription () {
        if (delegating (DELEGATE_GET_SHORT_DESCRIPTION))
            return original.getShortDescription ();
        else
            return super.getShortDescription ();
    }

    /* Finds an icon for this node. Delegates to the original.
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean
    */
    public Image getIcon (int type) {
        return original.getIcon (type);
    }

    /* Finds an icon for this node. This icon should represent the node
    * when it is opened (if it can have children). Delegates to original.
    *
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
        return original.getOpenedIcon (type);
    }

    public HelpCtx getHelpCtx () {
        return original.getHelpCtx ();
    }

    /* Can the original node be renamed?
    *
    * @return true if the node can be renamed
    */
    public boolean canRename () {
        return original.canRename ();
    }

    /* Can the original node be deleted?
    * @return <CODE>true</CODE> if can, <CODE>false</CODE> otherwise
    */
    public boolean canDestroy () {
        return original.canDestroy ();
    }

    /* Degelates the delete operation to original.
    */
    public void destroy () throws java.io.IOException {
        if (delegating (DELEGATE_DESTROY))
            original.destroy ();
        else
            super.destroy ();
    }

    /** Used to access the destroy method when original nodes
    * has been deleted
    */
    private final void originalDestroyed () {
        try {
            super.destroy ();
        } catch (IOException ex) {
        }
    }

    /* Getter for the list of property sets. Delegates to original.
    *
    * @return the array of property sets.
    */
    public PropertySet[] getPropertySets () {
        return original.getPropertySets ();
    }

    /* Called when an object is to be copied to clipboard.
    * @return the transferable object dedicated to represent the
    *    content of clipboard
    * @exception IOException is thrown when the
    *    operation cannot be performed
    */
    public Transferable clipboardCopy () throws IOException {
        return original.clipboardCopy ();
    }

    /* Called when an object is to be cut to clipboard.
    * @return the transferable object dedicated to represent the
    *    content of clipboard
    * @exception IOException is thrown when the
    *    operation cannot be performed
    */
    public Transferable clipboardCut () throws IOException {
        return original.clipboardCut ();
    }

    /* Returns true if this object allows copying.
    * @returns true if this object allows copying.
    */
    public boolean canCopy () {
        return original.canCopy ();
    }

    /* Returns true if this object allows cutting.
    * @returns true if this object allows cutting.
    */
    public boolean canCut () {
        return original.canCut ();
    }

    public Transferable drag () throws IOException {
        return original.drag ();
    }

    /* Default implementation that tries to delegate the implementation
    * to the createPasteTypes method. Simply calls the method and 
    * tries to take the first provided argument. Ignores the action
    * argument and index.
    *
    * @param t the transferable 
    * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
    * @param index index between children the drop occured at or -1 if not specified
    * @return null if the transferable cannot be accepted or the paste type
    *    to execute when the drop occures
    */
    public PasteType getDropType (Transferable t, int action, int index) {
        return original.getDropType (t, action, index);
    }

    /* Which paste operations are allowed when transferable t is in clipboard?
    * @param t the transferable in clipboard
    * @return array of operations that are allowed
    */
    public PasteType[] getPasteTypes (Transferable t) {
        return original.getPasteTypes (t);
    }

    /* Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        return original.getNewTypes ();
    }

    /* Delegates to original.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        if (delegating (DELEGATE_GET_ACTIONS))
            return original.getActions ();
        else
            return super.getActions ();
    }

    /* Delegates to original
    */
    public SystemAction[] getContextActions () {
        if (delegating (DELEGATE_GET_CONTEXT_ACTIONS))
            return original.getContextActions ();
        else
            return super.getContextActions ();
    }

    /*
    * @return default action of the original node or null
    */
    public SystemAction getDefaultAction () {
        return original.getDefaultAction ();
    }


    /*
    * @return <CODE>true</CODE> if the original has a customizer.
    */
    public boolean hasCustomizer () {
        return original.hasCustomizer ();
    }

    /* Returns the customizer component.
    * @return the component or <CODE>null</CODE> if there is no customizer
    */
    public java.awt.Component getCustomizer () {
        return original.getCustomizer ();
    }

    /* Delegates to original.
    *
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    */
    public Node.Cookie getCookie (Class type) {
        return original.getCookie (type);
    }

    /** If this is FilterNode without any changes (subclassed, changed children)
    * and the original provides handle, stores them and
    * returns a new handle for the proxy.
    * <p>Subclasses <strong>must</strong> override this if they wish for their nodes to be
    * properly serializable.
    *
    * @return the handle, or <code>null</code> if this node is subclassed or
    *    uses changed children
    */
    public Node.Handle getHandle () {
        if (!isDefault ()) {
            // subclasses has to implement the method by its own
            return null;
        }
        Node.Handle original = this.original.getHandle ();
        if (original == null) {
            // no original handle => no handle here
            return null;
        }

        return new FilterHandle (original);
    }

    /** Test equality of original nodes.
    * Note that for subclasses of <code>FilterNode</code>, or filter nodes with non-default children,
    * the test reverts to object identity.
    * @param o something to compare to, presumably a node or <code>FilterNode</code> of one
    * @return true if this node's original node is the same as the parameter (or original node of parameter)
    */
    public boolean equals (Object o) {
        // VERY DANGEROUS! Completely messes up visualizers and often original node is displayed rather than filter.
        // Jst: I know that it is dangerous, but some code probably depends on it
        //
        if (!isDefault ()) {
            return super.equals (o);
        }

        if (o instanceof FilterNode) {
            FilterNode fn = (FilterNode)o;
            return fn.isDefault () && original.equals (fn.original);
        }

        // o isn't filter node, compare with original
        return original.equals(o);
    }

    /** Hash by original nodes.
    * Note that for subclasses of <code>FilterNode</code>, or filter nodes with non-default children,
    * the hash reverts to the identity hash code.
    * @return the delegated hash code
    */
    public int hashCode () {
        // JST: Pretty, pretty please with suggar on the top, do not comment this
        // method out or I spend next two hours looking for why filternodes are
        // not removed from hashtable even they are equal!
        return isDefault () ? original.hashCode () : super.hashCode ();
    }

    //  public String toString () {
    //    return super.toString () + " original has children: " + original.getChildren ().getNodesCount (); // NOI18N
    //  }

    // ----------- END OF DELEGATED METHODS ------------

    /** Get the original node.
    * @return the node proxied to
    */
    protected Node getOriginal () {
        return original;
    }

    /** Create a property change listener that allows listening on the
    * original node properties (contained in property sets) and propagating
    * them to the proxy.
    * <P>
    * This method is called during initialization and allows subclasses
    * to modify the default behaviour.
    *
    * @return a {@link PropertyChangeAdapter} in the default implementation
    */
    protected PropertyChangeListener createPropertyChangeListener () {
        return new PropertyChangeAdapter (this);
    }

    /** Creates a node listener that allows listening on the
    * original node and propagating events to the proxy.
    * <p>Intended for overriding by subclasses, as with {@link #createPropertyChangeListener}.
    *
    * @return a {@link NodeAdapter} in the default implementation
    */
    protected NodeListener createNodeListener () {
        return new NodeAdapter (this);
    }

    /** Getter for property change listener.
    */
    synchronized PropertyChangeListener getPropertyChangeListener () {
        if (propL == null) {
            propL = createPropertyChangeListener ();
        }
        return propL;
    }

    /** Getter for node listener.
    */
    synchronized NodeListener getNodeListener () {
        if (nodeL == null) {
            nodeL = createNodeListener ();
        }
        return nodeL;
    }

    /** Check method whether the node has default behaviour or
    * if it is either subclass of uses different children.
    * @return true if it is default
    */
    private boolean isDefault () {
        if (getClass () != FilterNode.class) {
            return false;
        }

        org.openide.nodes.Children ch = getChildren ();
        if ((original.isLeaf () && ch == Children.LEAF) ||
                (! original.isLeaf () && ch.getClass () == /* FilterNode. */ Children.class && ((Children) ch).original == original)) {
            return true;
        } else {
            return false;
        }
    }

    /** Adapter that listens on changes in an original node
    * and refires them in a proxy.
    * This adapter is created during
    * initialization in  {@link FilterNode#createPropertyChangeListener}. The method
    * can be overriden and this class used as the super class for the
    * new implementation.
    * <P>
    * A reference to the proxy is stored by weak reference, so it does not
    * prevent the node from being finalized.
    */
    protected static class PropertyChangeAdapter extends Object implements PropertyChangeListener {
        /** weak reference to filter node */
        private WeakReference fn;

        /** Create a new adapter.
        * @param fn the proxy
        */
        public PropertyChangeAdapter (FilterNode fn) {
            this.fn = new WeakReference (fn);
        }

        /* Find the node we are attached to. If it is not null call property
        * change method with two arguments.
        */
        public final void propertyChange (PropertyChangeEvent ev) {
            FilterNode fn = (FilterNode)this.fn.get ();
            if (fn == null) {
                return;
            }

            propertyChange (fn, ev);
        }

        /** Actually propagate the event.
        * Intended for overriding.
        * @param fn the proxy
        * @param ev the event
        */
        protected void propertyChange (FilterNode fn, PropertyChangeEvent ev) {
            fn.firePropertyChange (
                ev.getPropertyName (), ev.getOldValue (), ev.getNewValue ()
            );
        }
    }

    /** Adapter that listens on changes in an original node and refires them
    * in a proxy. Created in {@link FilterNode#createNodeListener}.
    * @see FilterNode.PropertyChangeAdapter
    */
    protected static class NodeAdapter extends Object implements NodeListener {
        /** weak reference to filter node */
        private WeakReference fn;
        /** children object to notify about addition of children.
        * Can be null. Set from Children's initNodes method.
        */
        private WeakReference children;

        /** Create an adapter.
        * @param fn the proxy
        */
        public NodeAdapter (FilterNode fn) {
            this.fn = new WeakReference (fn);
        }

        /* Tests if the reference to the node provided in costructor is
        * still valid (it has not been finalized) and if so, calls propertyChange (Node, ev).
        */
        public final void propertyChange (PropertyChangeEvent ev) {
            FilterNode fn = (FilterNode)this.fn.get ();
            if (fn == null) {
                return;
            }
            propertyChange (fn, ev);
        }

        /** Actually refire the change event in a subclass.
        * The default implementation ignores changes of the <code>parentNode</code> property but refires
        * everything else.
        *
        * @param fn the filter node
        * @param ev the event to fire
        */
        protected void propertyChange (FilterNode fn, PropertyChangeEvent ev) {
            String n = ev.getPropertyName ();
            if (n.equals (Node.PROP_PARENT_NODE)) {
                // does nothing
                return;
            }
            if (n.equals (Node.PROP_DISPLAY_NAME)) {
                fn.fireOwnPropertyChange (
                    PROP_DISPLAY_NAME, (String)ev.getOldValue (), (String)ev.getNewValue ()
                );
                return;
            }
            if (n.equals (Node.PROP_NAME)) {
                fn.fireOwnPropertyChange (
                    PROP_NAME, (String)ev.getOldValue (), (String)ev.getNewValue ()
                );
                return;
            }
            if (n.equals (Node.PROP_SHORT_DESCRIPTION)) {
                fn.fireOwnPropertyChange (
                    PROP_SHORT_DESCRIPTION, (String)ev.getOldValue (), (String)ev.getNewValue ()
                );
                return;
            }
            if (n.equals (Node.PROP_ICON)) {
                fn.fireIconChange ();
                return;
            }
            if (n.equals (Node.PROP_OPENED_ICON)) {
                fn.fireOpenedIconChange ();
                return;
            }
            if (n.equals (Node.PROP_PROPERTY_SETS)) {
                fn.firePropertySetsChange ((PropertySet[])ev.getOldValue (), (PropertySet[])ev.getNewValue ());
                return;
            }
            if (n.equals (Node.PROP_COOKIE)) {
                fn.fireCookieChange ();
                return;
            }
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenAdded (NodeMemberEvent ev) {
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenRemoved (NodeMemberEvent ev) {
        }

        /** Does nothing.
        * @param ev event describing the action
        */
        public void childrenReordered (NodeReorderEvent ev) {
        }

        /* Does nothing.
        * @param ev event describing the node
        */
        public final void nodeDestroyed (NodeEvent ev) {
            FilterNode fn = (FilterNode)this.fn.get ();
            if (fn == null) {
                return;
            }
            fn.originalDestroyed ();
        }
    }

    /** Children for a filter node. Listens on changes in subnodes of
    * the original node and asks this filter node to creates representants for
    * these subnodes.
    * <P>
    * This class is used as the default for subnodes of filter node, but
    * subclasses may modify it or provide a totally different implementation.
    */
    public static class Children extends
                org.openide.nodes.Children.Keys
        implements Cloneable {
        /** Original node. Should not be modified. */
        protected Node original;
        /** node listener on original */
        private NodeListener nodeL;

        /** Create children.
         * @param or original node to take children from */
        public Children (Node or) {
            original = or;
        }

        /** Closes the listener, if any, on the original node.
        */
        protected void finalize () {
            if (nodeL != null) original.removeNodeListener (nodeL);
            nodeL = null;
        }

        /* Clones the children object.
        */
        public Object clone () {
            return new Children (original);
        }

        /** Initializes listening to changes in original node.
        */
        protected void addNotify () {
            // add itself to reflect to changes children of original node
            nodeL = new ChildrenAdapter (this);
            original.addNodeListener (nodeL);

            updateKeys ();
        }

        /** Clears current keys, because all mirrored nodes disappeared.
        */
        protected void removeNotify () {
            setKeys (Collections.EMPTY_SET);

            if (nodeL != null) {
                original.removeNodeListener (nodeL);
                nodeL = null;
            }
        }



        /** Allows subclasses to override
        * creation of node representants for nodes in the mirrored children
        * list. The default implementation simply uses {@link Node#cloneNode}.
        * <p>Note that this method is only suitable for a 1-to-1 mirroring.
        *
        * @param node node to create copy of
        * @return copy of the original node
        */
        protected Node copyNode (Node node) {
            return node.cloneNode ();
        }


        /* Implements find of child by finding the original child and then [PENDING]
        * @param name of node to find
        * @return the node or null
        */
        public Node findChild (String name) {
            original.getChildren ().findChild (name);
            return super.findChild (name);
        }


        /** Create nodes representing copies of the original node's children.
        * The default implementation returns exactly one representative for each original node,
        * as returned by {@link #copyNode}.
        * Subclasses may override this to avoid displaying a copy of an original child at all,
        * or even to display multiple nodes representing the original.
        * @param key the original child node
        * @return zero or more nodes representing the original child node
        */
        protected Node[] createNodes (Object key) {
            Node n = (Node)key;
            // is run under read access lock so nobody can change children
            return new Node[] { copyNode (n) };
        }

        /* Delegates to children of the original node.
        *
        * @param arr nodes to add
        * @return true/false
        */
        public boolean add (Node[] arr) {
            return original.getChildren ().add (arr);
        }

        /* Delegates to filter node.
        * @param arr nodes to remove
        * @return true/false
        */
        public boolean remove (Node[] arr) {
            return original.getChildren ().remove (arr);
        }

        /** Called when the filter node adds a new child.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenAdded (NodeMemberEvent ev) {
            updateKeys ();
        }

        /** Called when the filter node removes a child.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenRemoved (NodeMemberEvent ev) {
            updateKeys ();
        }

        /** Called when the filter node reorders its children.
        * The default implementation makes a corresponding change.
        * @param ev info about the change
        */
        protected void filterChildrenReordered (NodeReorderEvent ev) {
            updateKeys ();
        }

        /** variable to notify that there is a cyclic update.
        * Used only in updateKeys method
        */
        //    private transient boolean cyclic;

        /** Update keys from original nodes */
        private void updateKeys () {
            //      if (!cyclic) {
            //        cyclic = true;
            Children.MUTEX.postWriteRequest (new Runnable () {
                                                 public void run () {
                                                     Node[] arr = original.getChildren ().getNodes ();
                                                     setKeys (arr);
                                                 }
                                             });
            //        cyclic = false;
            //      }
        }
    }

    /** Adapter that listens on changes in the original node and fires them
    * in this node.
    * Used as the default listener in {@link FilterNode.Children},
    * and is intended for refinement by its subclasses.
    */
    private static class ChildrenAdapter extends Object
        implements NodeListener {
        /** children object to notify about addition of children.
        * Can be null. Set from Children's initNodes method.
        */
        private WeakReference children;

        /** Create a new adapter.
        * @param ch the children list
        */
        public ChildrenAdapter (Children ch) {
            this.children = new WeakReference (ch);
        }

        /** Does nothing.
        * @param ev the event
        */
        public void propertyChange (PropertyChangeEvent ev) {
        }

        /* Informs that a set of new children has been added.
        * @param ev event describing the action
        */
        public void childrenAdded (NodeMemberEvent ev) {
            Children children = (Children)this.children.get ();
            if (children == null) return;

            children.filterChildrenAdded (ev);
        }

        /* Informs that a set of children has been removed.
        * @param ev event describing the action
        */
        public void childrenRemoved (NodeMemberEvent ev) {
            Children children = (Children)this.children.get ();
            if (children == null) return;

            children.filterChildrenRemoved (ev);
        }

        /* Informs that a set of children has been reordered.
        * @param ev event describing the action
        */
        public void childrenReordered (NodeReorderEvent ev) {
            Children children = (Children)this.children.get ();
            if (children == null) return;

            children.filterChildrenReordered (ev);
        }


        /** Does nothing.
        * @param ev the event
        */
        public void nodeDestroyed (NodeEvent ev) {
        }
    }

    /** Filter node handle.
    */
    private static final class FilterHandle implements Node.Handle {
        private Node.Handle original;

        static final long serialVersionUID =7928908039428333839L;

        public FilterHandle (Node.Handle original) {
            this.original = original;
        }

        public Node getNode () throws IOException {
            return new FilterNode (original.getNode ());
        }
    }
}

/*
* Log
*  41   Gandalf   1.40        1/15/00  Jaroslav Tulach Equal is symetric.
*  40   Gandalf   1.39        1/13/00  Jesse Glick     NOI18N
*  39   Gandalf   1.38        1/12/00  Jesse Glick     NOI18N
*  38   Gandalf   1.37        12/27/99 Jaroslav Tulach #5042
*  37   Gandalf   1.36        12/27/99 Jaroslav Tulach #5042
*  36   Gandalf   1.35        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  35   Gandalf   1.34        10/6/99  Jaroslav Tulach #1787
*  34   Gandalf   1.33        10/4/99  Jesse Glick     [JavaDoc]
*  33   Gandalf   1.32        9/16/99  Jesse Glick     [JavaDoc]
*  32   Gandalf   1.31        9/16/99  Jaroslav Tulach equals and hashcode 
*       redefined for isDefault only.
*  31   Gandalf   1.30        9/15/99  Jesse Glick     Removed delegation of 
*       equals() and hashCode(), which break many things. Also fixed another 
*       potential bug in isDefault().
*  30   Gandalf   1.29        9/15/99  Jesse Glick     Added configurable 
*       delegation masks, solving many problems with display names, deletion, 
*       etc.  Also fixed possible bug: isDefault () would incorrectly return 
*       true if Children.LEAF (resp. FilterChildren) were used when the original
*       was not (resp. was) a leaf.
*  29   Gandalf   1.28        9/2/99   Jaroslav Tulach getNodes improvement.
*  28   Gandalf   1.27        8/27/99  Jaroslav Tulach 
*  27   Gandalf   1.26        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  26   Gandalf   1.25        7/29/99  Jaroslav Tulach Handle implemented.
*  25   Gandalf   1.24        7/12/99  Jesse Glick     FilterNode.Children.createNodes
*        non-final.
*  24   Gandalf   1.23        7/2/99   Jesse Glick     Node.drag abstract.
*  23   Gandalf   1.22        6/30/99  Ian Formanek    Fixed to compile with new
*       Drag'n'drop datatransfer
*  22   Gandalf   1.21        6/28/99  Ian Formanek    Removed obsoleted import
*  21   Gandalf   1.20        6/24/99  Jesse Glick     Nodes can specify context
*       help (not yet retrieved by anything, though).
*  20   Gandalf   1.19        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  19   Gandalf   1.18        6/8/99   Jaroslav Tulach Filter node updates 
*       correctly when change order is performed.
*  18   Gandalf   1.17        6/5/99   Jesse Glick     [JavaDoc]
*  17   Gandalf   1.16        5/27/99  Jesse Glick     [JavaDoc]
*  16   Gandalf   1.15        5/25/99  Jaroslav Tulach Fix #1889
*  15   Gandalf   1.14        5/15/99  Jesse Glick     [JavaDoc], and 
*       ChildrenAdapter made private since it is useless to subclass.
*  14   Gandalf   1.13        4/21/99  Jaroslav Tulach DataObjects can be 
*       finalized
*  13   Gandalf   1.12        4/21/99  Jesse Glick     [JavaDoc]
*  12   Gandalf   1.11        4/20/99  Jaroslav Tulach 
*  11   Gandalf   1.10        4/20/99  Jaroslav Tulach Children supports weak 
*       references.
*  10   Gandalf   1.9         4/9/99   Ian Formanek    Removed debug printlns
*  9    Gandalf   1.8         3/24/99  Jan Jancura     getOriginal must be 
*       protected
*  8    Gandalf   1.7         3/17/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         3/12/99  Jan Jancura     Bug in method destroy ()
*  6    Gandalf   1.5         2/26/99  David Simonek   
*  5    Gandalf   1.4         2/25/99  Jaroslav Tulach Change of clipboard 
*       management  
*  4    Gandalf   1.3         2/16/99  David Simonek   
*  3    Gandalf   1.2         2/5/99   Jaroslav Tulach Changed new from template
*       action
*  2    Gandalf   1.1         1/7/99   Jaroslav Tulach 
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
