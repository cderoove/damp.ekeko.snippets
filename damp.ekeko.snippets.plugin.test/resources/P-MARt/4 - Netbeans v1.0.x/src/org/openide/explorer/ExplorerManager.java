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

package org.openide.explorer;

import java.awt.Component;
import java.beans.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputValidation;
import java.util.*;

import org.openide.util.datatransfer.*;
import org.openide.TopManager;
import org.openide.util.*;
import org.openide.nodes.*;

/** Manages a selection and root context for a (set of) Explorer view(s).
* The views should register their {@link java.beans.VetoableChangeListener}s and {@link java.beans.PropertyChangeListener}s at
* the <code>ExplorerManager</code> of the Explorer they belong to. The manager listens on changes
* to the node hierarchy and updates the selection and root node.
*
* <P>
* Deserialization of this object is done with validation with priority 10. In readObject
* the ExplorerManager is temporarily initialized with void values. These values are 
* replaced with original ones when the validation proceeds.
*
* @author   Ian Formanek, Petr Hamernik, Jaroslav Tulach, Jan Jancura
*/
public final class ExplorerManager extends Object
    implements Serializable, Cloneable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4330330689803575792L;

    /** Name of property for the root context. */
    public static final String PROP_ROOT_CONTEXT = "rootContext"; // NOI18N
    /** Name of property for the explored context. */
    public static final String PROP_EXPLORED_CONTEXT = "exploredContext"; // NOI18N
    /** Name of property for the node selection. */
    public static final String PROP_SELECTED_NODES = "selectedNodes"; // NOI18N

    /** The support for VetoableChangeEvent */
    private transient VetoableChangeSupport vetoableSupport;
    /** The support for PropertyChangeEvent */
    private transient PropertyChangeSupport propertySupport;

    /** The current root context */
    private Node rootContext;
    /** The current explored context */
    private Node exploredContext;
    /** The currently selected beans */
    private Node[] selectedNodes;
    /** listener to destroy of root node */
    private transient Listener listener;
    /** weak listener */
    private transient NodeListener weakListener;

    /** The explorer's resource bundle */
    static java.util.ResourceBundle explorerBundle = NbBundle.getBundle (ExplorerManager.class);

    /** Construct a new manager. */
    public ExplorerManager () {
        init ();
    }

    /** Initializes the nodes.
    */
    private void init () {
        exploredContext = rootContext = Node.EMPTY;
        selectedNodes = new Node[0];
        listener = new Listener ();
        weakListener = WeakListener.node (listener, null);
    }

    /** Clones the manager.
    * @return manager with the same settings like this one
    */
    public Object clone () {
        ExplorerManager em = new ExplorerManager ();
        em.rootContext = rootContext;
        em.exploredContext = exploredContext;
        em.selectedNodes = selectedNodes;
        return em;
    }

    /** Get the set of selected nodes.
    * @return the selected nodes; empty (not <code>null</code>) if none are selected
    */
    public Node[] getSelectedNodes () {
        return selectedNodes;
    }

    /** Set the set of selected nodes.
    * @param value the nodes to select; empty (not <code>null</code>) if none are to be selected
    * @exception PropertyVetoException when the given nodes cannot be selected
    */
    public final void setSelectedNodes (Node[] value) throws PropertyVetoException {
        if (Arrays.equals (value, selectedNodes)) {
            return;
        }

        if (value.length != 0 && vetoableSupport != null) {
            // we send the vetoable change event only for non-empty selections
            vetoableSupport.fireVetoableChange(PROP_SELECTED_NODES, selectedNodes, value);
        }

        Node[] oldValue = selectedNodes;
        selectedNodes = value;

        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_SELECTED_NODES, oldValue, selectedNodes);
        }
    }

    /** Get the explored context.
    * @return the node being explored, or <code>null</code>
    */
    public final Node getExploredContext() {
        return exploredContext;
    }

    /** Set the explored context.
    * @param value the new node to explore, or <code>null</code> if none should be explored. The action is ignored if the node is above the current root context in the node hierarchy.
    */
    public final void setExploredContext(Node value) {
        if ((exploredContext != null) && (exploredContext.equals(value))) return;

        if (!isUnderRoot(value)) return; // invalid new exploredContext - above root
        try {
            setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
            throw new InternalError(explorerBundle.getString("ERR_MustNotVetoEmptySelection"));
        }
        Node oldValue = exploredContext;
        exploredContext = value;

        if (propertySupport != null)
            propertySupport.firePropertyChange(PROP_EXPLORED_CONTEXT, oldValue, exploredContext);
    }

    /** Get the root context.
    * @return the root context node
    */
    public final Node getRootContext() {
        return rootContext;
    }

    /** Set the root context.
    * @param value the new node to serve as a root
    */
    public final void setRootContext(Node value) {
        if ((rootContext != null) && (rootContext.equals (value))) return;
        Node oldValue = rootContext;
        rootContext = value;

        oldValue.removeNodeListener (weakListener);
        rootContext.addNodeListener (weakListener);

        if (propertySupport != null)
            propertySupport.firePropertyChange(PROP_ROOT_CONTEXT, oldValue, rootContext);
        setExploredContext(rootContext);
    }

    /** Add a <code>PropertyChangeListener</code> to the listener list.
    * @param l the listener to add
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertySupport == null)
            propertySupport = new PropertyChangeSupport(this);
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove a <code>PropertyChangeListener</code> from the listener list.
    * @param l the listener to remove
    */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertySupport != null)
            propertySupport.removePropertyChangeListener(l);
    }

    /** Add a <code>VetoableListener</code> to the listener list.
    * @param l the listener to add
    */
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        if (vetoableSupport == null)
            vetoableSupport = new VetoableChangeSupport(this);
        vetoableSupport.addVetoableChangeListener(l);
    }

    /** Remove a <code>VetoableChangeListener</code> from the listener list.
    * @param l the listener to remove
    */
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        if (vetoableSupport != null)
            vetoableSupport.removeVetoableChangeListener(l);
    }

    /** Checks whether given Node is a subnode of rootContext.
    * @return true if specified Node is under current rootContext
    */
    private boolean isUnderRoot(Node node) {
        while (node != null) {
            if (node.equals(rootContext)) return true;
            node = node.getParentNode();
        }
        return false;
    }

    /** serializes object
    */
    private void writeObject (java.io.ObjectOutputStream os)
    throws java.io.IOException {
        Node root = NodeOp.findRoot(rootContext);
        Node.Handle rootHandle = root.getHandle();
        //System.out.println("Root: " + root.getName()); // NOI18N

        os.writeObject(rootHandle);

        if (rootHandle != null) {
            os.writeObject(NodeOp.createPath(rootContext, root));
            os.writeObject(NodeOp.createPath(exploredContext, root));
            for (int i = selectedNodes.length; --i >= 0;) {
                os.writeObject(NodeOp.createPath(selectedNodes[i], root));
            }
            os.writeObject(null);
        }
    }

    /** Deserializes the view and initializes it
    *
    */
    private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        // perform initialization
        init();

        // read root handle
        Node.Handle h = (Node.Handle) ois.readObject();

        ObjectInputValidation oiv;

        if (h == null) {
            oiv = new MyValidation ();
        } else {
            String[] rootCtx = (String[])ois.readObject();
            String[] exploredCtx = (String[])ois.readObject ();
            LinkedList ll = new LinkedList ();
            for (;;) {
                String[] path = (String[])ois.readObject();
                if (path == null) break;
                ll.add(path);
            };
            oiv = new MyValidation (h.getNode (), rootCtx, exploredCtx, ll);
        }

        ois.registerValidation (oiv, 10);
    }


    /** Find the proper Explorer manager for a given component.
    * This is done by traversing the component hierarchy and
    * finding the first ancestor that implements {@link Provider}.
    * <P>
    * This method should be used in {@link Component#addNotify} of each component
    * that works with the Explorer manager, e.g.:
    * <p><CODE><pre>
    * private transient ExplorerManager explorer;
    * 
    * public void addNotify () {
    *   super.addNotify ();
    *   explorer = ExplorerManager.find (this);
    * }
    * </pre></CODE>
    *
    * @param comp component to find the manager for
    * @return the manager, or a new empty manager if no ancestor implements <code>Provider</code>
    *
    * @see Provider
    */
    public static ExplorerManager find (Component comp) {
        // start looking for manager from parent, not the component itself
        for (;;) {
            comp = comp.getParent ();
            if (comp == null) {
                // create new explorer because nothing has been found
                return new ExplorerManager ();
            }
            if (comp instanceof Provider) {
                // ok, found a provider, return its manager
                return ((Provider)comp).getExplorerManager ();
            }
        }
    }

    /** Finds node by given path */
    static Node findPath(Node r, String[] path) {
        try {
            return NodeOp.findPath(r, path);
        } catch (NodeNotFoundException ex) {
            return ex.getClosestNode();
        }
    }

    //
    // inner classes
    //

    /** Interface for components wishing to provide their own <code>ExplorerManager</code>.
    * @see ExplorerManager#find
    */
    public static interface Provider {
        /** Get the explorer manager.
        * @return the manager
        */
        public ExplorerManager getExplorerManager ();
    }

    /** Listener to be notified when root node has been destroyed.
    * Then the root node is changed to Node.EMPTY
    */
    private class Listener extends NodeAdapter {
        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
            if (ev.getNode ().equals (getRootContext ())) {
                // node has been deleted
                setRootContext (Node.EMPTY);
            }
        }
    }

    /** Validation after readObject */
    private final class MyValidation
        implements java.io.ObjectInputValidation, Runnable {
        Node root;
        String[] rootCtx;
        String[] exploredCtx;
        List selNodes;

        public MyValidation(Node r, String[] rc, String[] e, List list) {
            root = r;
            rootCtx = rc;
            exploredCtx = e;
            selNodes = list;
        }

        public MyValidation() {
        }

        public void validateObject() {
            synchronized (ExplorerManager.this) {
                if (rootCtx == null) {
                    Node rep = TopManager.getDefault().getPlaces().nodes().repository();
                    setRootContext(rep);
                    setExploredContext(rep);
                    return;
                }
            }

            run ();
            // posts request to be processed lazily
            //      RequestProcessor.postRequest (this);
        }

        public void run () {
            synchronized (ExplorerManager.this) {
                setRootContext(findPath(root, rootCtx));
                setExploredContext(findPath(root, exploredCtx));

                try {
                    // converts list of String[] to Node
                    ListIterator it = selNodes.listIterator ();
                    while (it.hasNext ()) {
                        String[] path = (String[])it.next ();
                        it.set (findPath(root, path));
                    }

                    setSelectedNodes((Node[]) selNodes.toArray(new Node[selNodes.size ()]));
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
}

/*
 * Log
 *  17   Gandalf   1.16        1/13/00  Ian Formanek    NOI18N
 *  16   Gandalf   1.15        1/12/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  12   Gandalf   1.11        8/13/99  Jaroslav Tulach Description.
 *  11   Gandalf   1.10        8/13/99  Jaroslav Tulach New Main Explorer
 *  10   Gandalf   1.9         8/12/99  David Simonek   deserialization fix
 *  9    Gandalf   1.8         8/3/99   Jan Jancura     System.out.. cleared
 *  8    Gandalf   1.7         7/30/99  David Simonek   serialization fixes
 *  7    Gandalf   1.6         7/21/99  David Simonek   properties switcher 
 *       fixed
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/16/99  Jaroslav Tulach Serializes the selection
 *       in the explorer panel.
 *  4    Gandalf   1.3         3/22/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/20/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     Localization moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 anonymous       added Window parameter to the constructor
 *  0    Tuborg    0.30        --/--/98 anonymous       added copy, cut, paste performers - not fully implemented
 *  0    Tuborg    0.32        --/--/98 anonymous       added default values for rootContext, exploredContext and selectedBeans
 *  0    Tuborg    0.33        --/--/98 Petr Hamernik   clipboard package moved...
 *  0    Tuborg    0.35        --/--/98 Petr Hamernik   many changes with clipboard
 *  0    Tuborg    0.36        --/--/98 Petr Hamernik   [Petr, jst] clipboard
 *  0    Tuborg    0.37        --/--/98 Petr Hamernik   small change
 *  0    Tuborg    0.38        --/--/98 Petr Hamernik   temporary solution of setTitle to ExplorerFrame
 *  0    Tuborg    0.39        --/--/98 Jan Formanek    setRootContext sets the exploredContext to the new rootContext
 *  0    Tuborg    0.39        --/--/98 Jan Formanek    as a side effect
 *  0    Tuborg    0.40        --/--/98 Petr Hamernik   setSelectedBeans doesn't do anything, when new Value equals old one (sel. beans).
 *  0    Tuborg    0.41        --/--/98 Ales Novak      made serializable
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    lazy initialization of property and vetoable support (due to serialization)
 *  0    Tuborg    0.44        --/--/98 Jan Formanek    everything made transient
 *  0    Tuborg    0.45        --/--/98 Jan Formanek    initializes rootContext, ... with empty context after deserialization
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    SWITCH TO NODES
 *  0    Tuborg    0.51        --/--/98 Jan Jancura     getDisplayName
 *  0    Tuborg    0.52        --/--/98 Petr Hamernik   Bug fixes...
 *  0    Tuborg    0.53        --/--/98 Jan Formanek    got rid of Explorer.getExplorerBundle
 *  0    Tuborg    0.54        --/--/98 Jan Formanek    added default constructor
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    added checking whether new exploredContext in setExploredContext
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    is under rootContext
 *  0    Tuborg    0.56        --/--/98 Ales Novak      rootContext, selectedBeans, exploredContext are serialized
 *  0    Tuborg    0.60        --/--/98 Jan Formanek    property name constants, nodeTracker and title setting removed
 *  0    Tuborg    0.61        --/--/98 Jaroslav Tulach if rootContext is not serialized then traverses to parent
 */
