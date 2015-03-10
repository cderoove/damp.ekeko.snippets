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

import java.lang.ref.*;
import java.beans.*;
import java.util.*;


import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.windows.*;

/** Container for array of nodes.
* Can be {@link Node#Node associated} with a node and then
* all children in the array have that node set as a parent, and this list
* will be returned as the node's children.
*
* @author Jaroslav Tulach
*/
public abstract class Children extends Object {
    /** Lock for access to hierarchy of all node lists.
    * Anyone who needs to ensure that there will not
    * be shared accesses to hierarchy nodes can use this
    * mutex.
    * <P>
    * All operations on the hierarchy of nodes (add, remove, etc.) are
    * done in the {@link Mutex#writeAccess} method of this lock, so if someone
    * needs for a certain amount of time to forbid modification,
    * he can execute his code in {@link Mutex#readAccess}.
    */
    public static final Mutex MUTEX = new Mutex ();

    /** The object representing an empty set of children. Should
    * be used to represent the children of leaf nodes. The same
    * object may be used by all such nodes.
    */
    public static final Children LEAF = new Empty ();

    /** parent node for all nodes in this list (can be null) */
    private Node parent;

    /** mapping from entries to info about them (Entry, Info) 
     * @associates Info*/
    private java.util.Map map;
    /** collection of all entries */
    private Collection entries = Collections.EMPTY_LIST;
    /** array of children Reference (ChildrenArray) */
    Reference array = new WeakReference (null);

    /*
      private StringBuffer debug = new StringBuffer ();
      
      private void printStackTrace() {
        Exception e = new Exception ();
        java.io.StringWriter w1 = new java.io.StringWriter ();
        java.io.PrintWriter w = new java.io.PrintWriter (w1);
        e.printStackTrace(w);
        w.close ();
        debug.append (w1.toString ());
        debug.append ('\n');
      }
    */

    /** Constructor.
    */
    public Children () {
    }

    /** Setter of parent node for this list of children. Each children in the list
    * will have this node set as parent. The parent node will return nodes in
    * this list as its children.
    * <P>
    * This method is called from the Node constructor
    *
    * @param n node to attach to
    * @exception IllegalStateException when this object is already used with
    *    different node
    */
    final void attachTo (final Node n) throws IllegalStateException {
        // special treatment for LEAF object.
        if (this == LEAF) {
            // do not attaches the node because the LEAF cannot have children
            // and that is why it need not set parent node for them
            return;
        }

        synchronized (this) {
            if (parent != null) {
                // already used
                throw new IllegalStateException ();
            }

            // attach itself as a node list for given node
            parent = n;
        }


        // this is the only place where parent is changed,
        // but only under readAccess => double check if
        // it happened correctly
        MUTEX.readAccess (new Runnable () {
                              public void run () {
                                  Node[] nodes = testNodes ();
                                  if (nodes == null) return;


                                  // fire the change
                                  for (int i = 0; i < nodes.length; i++) {
                                      Node node = nodes[i];
                                      node.assignTo (Children.this, i);
                                      node.fireParentNodeChange (null, parent);
                                  }
                              }
                          });
    }

    /** Get the parent node of these children.
    * @return the node attached to this children object, or <code>null</code> if there is none yet
    */
    protected final Node getNode () {
        return parent;
    }


    /** Allows access to the clone method for Node.
    * @return cloned hierarchy
    * @exception CloneNotSupportedException if not supported
    */
    final Object cloneHierarchy () throws CloneNotSupportedException {
        return clone ();
    }

    /** Handles clonning in the right way, that can be later extended by
    * subclasses. Ofcourse each subclass that is about to support clonning
    * must implement Cloneable interface, otherwise this method throws
    * CloneNotSupportedException.
    *
    * @return cloned version of this object, with the same class, uninitialized and without
    *   a parent node
    * *exception CloneNotSupportedException if Cloneable interface is not implemented
    */
    protected Object clone () throws CloneNotSupportedException {
        Children ch = (Children)super.clone ();

        ch.parent = null;
        ch.map = null;
        ch.entries = Collections.EMPTY_LIST;
        ch.array = new WeakReference (null);

        return ch;
    }


    /** Add nodes this container.
    * The parent node of these nodes
    * is changed to the parent node of this list. Each node can be added
    * only once. If there is some reason a node cannot be added, for example
    * if the node expects only a special type of subnodes, the method should
    * do nothing and return <code>false</code> to signal that the addition has not been successful.
    * <P>
    * This method should be implemented by subclasses to filter some nodes, etc.
    *
    * @param nodes set of nodes to add to the list
    * @return <code>true</code> if successfully added
    */
    public abstract boolean add (final Node[] nodes);

    /** Remove nodes from the list. Only nodes that are present are
    * removed.
    *
    * @param nodes nodes to be removed
    * @return <code>true</code> if the nodes could be removed
    */
    public abstract boolean remove (final Node[] nodes);

    /** Get the nodes as an enumeration.
    * @return enumeration of {@link Node}s
    */
    public final Enumeration nodes () {
        return new ArrayEnumeration (getNodes ());
    }

    /** Find a child node by name.
    * This may be overridden in subclasses to provide a more advanced way of finding the 
    * child, but the default implementation simply scans through the list of nodes 
    * to find the first one with the requested name.
    *
    * @param name (system) name of child node to find
    * @return the node or <code>null</code> if it could not be found
    */
    public Node findChild (String name) {
        Node[] list = getNodes ();
        for (int i = 0; i < list.length; i++) {
            if (name.equals (list[i].getName ())) {
                // ok, we have found it
                return list[i];
            }
        }
        return null;
    }

    /** Method that can be used to test whether the children content has
    * ever been used or it is still not initalized.
    * @return true if children has been used before
    */
    protected final boolean isInitialized () {
        ChildrenArray arr = (ChildrenArray)array.get ();
        return arr != null && arr.isInitialized ();
    }

    /** Get a (sorted) array of nodes in this list.
    * @return array of nodes
    */
    //  private static String off = ""; // NOI18N

    public final Node[] getNodes () {
        //Thread.dumpStack();
        //System.err.println(off + "getNodes: " + getNode ());
        for (;;) {
            boolean initialized = isInitialized ();

            //System.err.println(off + "  initialized: " + initialized);
            //      off = off + "  "; // NOI18N
            // forbid any modifications to this hierarchy
            Node[] nodes = (Node[])MUTEX.readAccess (new Mutex.Action () {
                               public Object run () {
                                   return computeNodes ();
                               }
                           });
            //      off = off.substring (2);
            //System.err.println(off + "  length     : " + nodes.length);
            //System.err.println(off + "  entries    : " + entries);
            //System.err.println(off + "  init now   : " + isInitialized());
            // if not initialized that means that after
            // we computed the nodes, somebody changed them (as a
            // result of addNotify) => we have to compute them
            // again
            if (initialized) {
                // otherwise it is ok.
                return nodes;
            }
        }
    }

    /** Get the number of nodes in the list.
    * @return the count
    */
    public final int getNodesCount () {
        return getNodes ().length;
    }

    //
    // StateNotifications
    //

    /** Called when children are first asked for nodes.
    */
    protected void addNotify () {
    }

    /** Called when last children nodes disappeared.
    */
    protected void removeNotify () {
    }

    /** Method that can be overriden in subclasses to
    * do additional work and then call addNotify.
    */
    void callAddNotify () {
        addNotify ();
    }

    //
    // ChildrenArray operations call only under lock
    //

    /** @return either nodes associated with this children or null if
    * they are not created
    */
    private Node[] testNodes () {
        ChildrenArray arr = (ChildrenArray)array.get ();
        return arr == null ? null : arr.nodes ();
    }

    /** Getter for list of nodes. Called from getNodes ().
    * @return list of nodes associated with this object
    */
    final Node[] computeNodes () {
        return getArray ().nodes ();
    }

    /** Obtains references to array holder. If it does not exist, it is
    * created.
    */
    private ChildrenArray getArray () {
        ChildrenArray arr = (ChildrenArray)array.get ();
        if (arr == null) {
            // create new array
            arr = ChildrenArray.create (this);
        }
        return arr;
    }

    /** Clears the nodes
    */
    private void clearNodes () {
        ChildrenArray arr = (ChildrenArray)array.get ();
        //System.err.println(off + "  clearNodes: " + getNode ());
        if (arr != null) {
            // clear the array
            arr.clear ();
        }
    }

    /** Forces finalization of nodes for given info.
    * Called from finalizer of Info.
    */
    final void finalizeNodes () {
        ChildrenArray arr = (ChildrenArray)array.get ();
        if (arr != null) {
            arr.finalizeNodes ();
        }
    }


    /** Registration of ChildrenArray.
    * @param array use weak or hard references
    * @param weak use weak or hard reference
    */
    final void registerChildrenArray (final ChildrenArray array, boolean weak) {
        if (weak) {
            this.array = new WeakReference (array);
        } else {
            // hold the children hard
            this.array = new WeakReference (array) {
                             public Object get () {
                                 return array;
                             }
                         };
        }
    }

    /** Finalized.
    */
    final void finalizedChildrenArray () {
        // usually in removeNotify setKeys is called => better require write access
        MUTEX.writeAccess (new Runnable () {
                               public void run () {
                                   if (array.get () == null) {
                                       // really finalized and not reconstructed
                                       removeNotify ();
                                   }
                               }
                           });
    }

    /** Computes the nodes now.
    */
    final Node[] justComputeNodes () {
        if (map == null) {
            map = new HashMap (17);
            //      debug.append ("Map initialized\n"); // NOI18N
            //      printStackTrace();
        }

        LinkedList l = new LinkedList ();
        Iterator it = entries.iterator ();
        while (it.hasNext ()) {
            Entry entry = (Entry)it.next ();
            Info info = findInfo (entry);
            l.addAll (info.nodes ());
        }

        Node[] arr = (Node[])l.toArray (new Node[l.size ()]);

        // initialize parent nodes
        for (int i = 0; i < arr.length; i++) {
            Node n = arr[i];
            n.assignTo (this, i);
        }

        return arr;
    }

    /** Finds info for given entry, or registers
    * it, if not registered yet.
    */
    private Info findInfo (Entry entry) {
        Info info = (Info)map.get (entry);
        if (info == null) {
            info = new Info (entry);
            map.put (entry, info);
            //      debug.append ("Put: " + entry + " info: " + info); // NOI18N
            //      debug.append ('\n');
            //      printStackTrace();
        }
        return info;
    }

    //
    // Entries
    //

    /** Access to copy of current entries.
    * @return copy of entries in the objects
    */
    final LinkedList getEntries () {
        return new LinkedList (this.entries);
    }

    final void setEntries (Collection entries) {
        // current list of nodes
        ChildrenArray holder = (ChildrenArray)array.get ();
        if (holder == null) {
            //      debug.append ("Set1: " + entries); // NOI18N
            //      printStackTrace();
            this.entries = entries;
            return;
        }
        Node[] current = holder.nodes ();
        if (current == null) {
            // the initialization is not finished yet =>
            //      debug.append ("Set2: " + entries); // NOI18N
            //      printStackTrace();
            this.entries = entries;
            return;
        }

        // if there are old items in the map, remove them to
        // reflect current state
        map.keySet ().retainAll (this.entries);

        // what should be removed
        HashSet toRemove = new HashSet (map.keySet ());
        HashSet entriesSet = new HashSet (entries);
        toRemove.removeAll (entriesSet);

        if (!toRemove.isEmpty ()) {
            // notify removing, the set must be ready for
            // callbacks with questions
            updateRemove (current, toRemove);
            current = holder.nodes ();
        }

        // change the order of entries, notifies
        // it and again brings children to up-to-date state
        Collection toAdd = updateOrder (current, entries);

        if (!toAdd.isEmpty ()) {

            // toAdd contains Info objects that should
            // be added
            updateAdd (toAdd, entries);
        }
    }

    /** Removes the objects from the children.
    */
    private void updateRemove (Node[] current, Set toRemove) {
        LinkedList nodes = new LinkedList ();

        Iterator it = toRemove.iterator ();
        while (it.hasNext ()) {
            Entry en = (Entry)it.next ();
            Info info = (Info)map.remove (en);
            //debug.append ("Removed: " + en + " info: " + info); // NOI18N
            //debug.append ('\n');
            //printStackTrace();
            nodes.addAll (info.nodes ());
        }

        // modify the current set of entries and empty the list of nodes
        // so it has to be recreated again
        //debug.append ("Current : " + this.entries + '\n'); // NOI18N
        this.entries.removeAll (toRemove);
        //debug.append ("Removing: " + toRemove + '\n'); // NOI18N
        //debug.append ("New     : " + this.entries + '\n'); // NOI18N
        //printStackTrace();

        clearNodes ();

        notifyRemove (nodes, current);
    }

    /** Notifies that a set of nodes has been removed from
    * children. It is necessary that the system is already 
    * in consistent state, so any callbacks will return 
    * valid values.
    *
    * @param nodes list of removed nodes
    * @param current state of nodes
    */
    private void notifyRemove (Collection nodes, Node[] current) {
        //System.err.println("notifyRemove from: " + getNode ());
        //System.err.println("notifyRemove: " + nodes);
        //System.err.println("Current     : " + Arrays.asList (current));
        //Thread.dumpStack();
        //Keys.last.printStackTrace();

        // [TODO] Children do not have always a parent
        // see Services->FIRST ($SubLevel.class)
        // during a deserialization it may have parent == null
        if (parent == null) {
            return;
        }

        // fire change of nodes
        parent.fireSubNodesChange (
            false,  // remove
            (Node[])nodes.toArray (new Node[nodes.size ()]),
            current
        );

        // fire change of parent
        Iterator it = nodes.iterator ();
        while (it.hasNext ()) {
            Node n = (Node)it.next ();
            n.deassignFrom (this);
            n.fireParentNodeChange (parent, null);
        }
    }

    /** Updates the order of entries.
    * @param current current state of nodes
    * @param entries new set of entries
    * @return list of infos that should be added
    */
    private List updateOrder (Node[] current, Collection entries) {
        LinkedList toAdd = new LinkedList ();

        // that assignes entries their begining position in the array
        // of nodes
        HashMap offsets = new HashMap ();
        {
            int previousPos = 0;

            Iterator it = this.entries.iterator ();
            while (it.hasNext ()) {
                Entry entry = (Entry)it.next ();

                Info info = (Info)map.get (entry);


                offsets.put (info, new Integer (previousPos));

                previousPos += info.length ();
            }
        }

        // because map can contain some additional items,
        // that has not been garbage collected yet,
        // retain only those that are in current list of
        // entries
        map.keySet ().retainAll (this.entries);

        int[] perm = new int[current.length];
        int currentPos = 0;
        int permSize = 0;
        LinkedList reorderedEntries = null;

        Iterator it = entries.iterator ();
        while (it.hasNext ()) {
            Entry entry = (Entry)it.next ();

            Info info = (Info)map.get (entry);
            if (info == null) {
                // this info has to be added
                info = new Info (entry);
                toAdd.add (info);
            } else {
                int len = info.length ();
                if (reorderedEntries == null) {
                    reorderedEntries = new LinkedList ();
                }
                reorderedEntries.add (entry);

                // already there => test if it should not be reordered
                Integer previousInt = (Integer)offsets.get (info);
                /*
                        if (previousInt == null) {
                          System.err.println("Offsets: " + offsets);
                          System.err.println("Info: " + info);
                          System.err.println("Entry: " + info.entry);
                          System.err.println("This entries: " + this.entries);
                          System.err.println("Entries: " + entries);
                          System.err.println("Map: " + map);
                          
                          System.err.println("---------vvvvv");
                          System.err.println(debug);
                          System.err.println("---------^^^^^");
                          
                        }
                */        
                int previousPos = previousInt.intValue ();
                if (currentPos != previousPos) {
                    for (int i = 0; i < len; i++) {
                        perm[previousPos + i] = 1 + currentPos + i;
                    }
                    permSize += len;
                }
            }

            currentPos += info.length ();
        }

        if (permSize > 0) {
            // now the perm array contains numbers 1 to ... and
            // 0 one places where no permutation occures =>
            // decrease numbers, replace zeros

            for (int i = 0; i < perm.length; i++) {
                if (perm[i] == 0) {
                    // fixed point
                    perm[i] = i;
                } else {
                    // decrease
                    perm[i]--;
                }
            }

            // reorderedEntries are not null
            this.entries = reorderedEntries;
            //      debug.append ("Set3: " + this.entries); // NOI18N
            //      printStackTrace();

            // notify the permutation to the parent
            clearNodes ();
            //System.err.println("Paremutaiton! " + getNode ());
            parent.fireReorderChange (perm);
        }

        return toAdd;
    }

    /** Updates the state of children by adding given Infos.
    * @param infos list of Info objects to add
    * @param entries the final state of entries that should occur
    */
    private void updateAdd (Collection infos, Collection entries) {
        LinkedList nodes = new LinkedList ();
        Iterator it = infos.iterator ();
        while (it.hasNext ()) {
            Info info = (Info)it.next ();
            nodes.addAll (info.nodes ());
            map.put (info.entry, info);
            //      debug.append ("updateadd: " + info.entry + " info: " + info + '\n'); // NOI18N
            //      printStackTrace();
        }

        this.entries = entries;
        //      debug.append ("Set4: " + entries); // NOI18N
        //      printStackTrace();

        clearNodes ();

        notifyAdd (nodes);
    }

    /** Notifies that a set of nodes has been add to
    * children. It is necessary that the system is already 
    * in consistent state, so any callbacks will return 
    * valid values.
    *
    * @param nodes list of removed nodes
    */
    private void notifyAdd (Collection nodes) {
        // notify about parent change
        Iterator it = nodes.iterator ();
        while (it.hasNext ()) {
            Node n = (Node)it.next ();

            n.assignTo (this, -1);
            n.fireParentNodeChange (null, parent);
        }
        Node[] arr = (Node[])nodes.toArray (new Node[nodes.size ()]);

        Node n = parent;
        if (n != null) {
            n.fireSubNodesChange (
                true, arr, null
            );
        }
    }


    /** Refreshes content of one entry. Updates the state of children
    * appropriatelly.
    */
    final void refreshEntry (Entry entry) {
        // current list of nodes
        ChildrenArray holder = (ChildrenArray)array.get ();
        if (holder == null) {
            return;
        }

        Node[] current = holder.nodes ();
        if (current == null) {
            // the initialization is not finished yet =>
            return;
        }

        // because map can contain some additional items,
        // that has not been garbage collected yet,
        // retain only those that are in current list of
        // entries
        map.keySet ().retainAll (this.entries);

        Info info = (Info)map.get (entry);
        if (info == null) {
            // refresh of entry that is not present =>
            return;
        }


        Collection oldNodes = info.nodes ();
        Collection newNodes = info.entry.nodes ();

        if (oldNodes.equals (newNodes)) {
            // nodes are the same =>
            return;
        }

        HashSet toRemove = new HashSet (oldNodes);
        toRemove.removeAll (newNodes);

        if (!toRemove.isEmpty ()) {
            // notify removing, the set must be ready for
            // callbacks with questions

            // modifies the list associated with the info
            oldNodes.removeAll (toRemove);
            clearNodes ();

            // now everything should be consistent => notify the remove
            notifyRemove (toRemove, current);

            current = holder.nodes ();
        }

        List toAdd = refreshOrder (entry, oldNodes, newNodes);
        info.useNodes (newNodes);

        if (!toAdd.isEmpty ()) {
            // modifies the list associated with the info
            clearNodes ();
            notifyAdd (toAdd);
        }
    }

    /** Updates the order of nodes after a refresh.
    * @param entry the refreshed entry
    * @param oldNodes nodes that are currently in the list
    * @param newNodes new nodes (defining the order of oldNodes and some more)
    * @return list of infos that should be added
    */
    private List refreshOrder (Entry entry, Collection oldNodes, Collection newNodes) {
        LinkedList toAdd = new LinkedList ();

        int currentPos = 0;

        // cycle thru all entries to find index of the entry
        Iterator it = this.entries.iterator ();
        for (;;) {
            Entry e = (Entry)it.next ();
            if (e.equals (entry)) {
                break;
            }
            Info info = findInfo (e);
            currentPos += info.length ();
        }

        HashSet oldNodesSet = new HashSet (oldNodes);
        HashSet toProcess = (HashSet)oldNodesSet.clone ();

        Node[] permArray = new Node[oldNodes.size ()];
        it = newNodes.iterator ();
        int pos = 0;
        while (it.hasNext ()) {
            Node n = (Node)it.next ();
            if (oldNodesSet.remove (n)) {
                // the node is in the old set => test for permuation
                permArray[pos++] = n;
            } else {
                if (!toProcess.contains (n)) {
                    // if the node has not been processed yet
                    toAdd.add (n);
                } else {
                    it.remove ();
                }
            }
        }

        // JST: If you get IllegalArgumentException in following code
        // then it can be cause by wrong synchronization between
        // equals and hashCode methods. First of all check them!
        int[] perm = NodeOp.computePermutation (
                         (Node[])oldNodes.toArray (new Node[oldNodes.size ()]),
                         permArray
                     );
        if (perm != null) {
            // apply the permutation
            clearNodes ();
            // temporarily change the nodes the entry should use
            findInfo (entry).useNodes (Arrays.asList (permArray));
            Node p = parent;
            if (p != null) {
                p.fireReorderChange (perm);
            }
        }

        return toAdd;
    }


    /** Information about an entry. Contains number of nodes,
    * position in the array of nodes, etc.
    */
    final class Info extends Object {
        int length;
        Entry entry;

        public Info (Entry entry) {
            this.entry = entry;
        }

        /** Finalizes the content of ChildrenArray.
        */
        protected void finalize () {
            finalizeNodes ();
        }

        public Collection nodes () {
            // forces creation of the array
            ChildrenArray arr = getArray ();

            return arr.nodesFor (this);
        }

        public void useNodes (Collection nodes) {
            // forces creation of the array
            ChildrenArray arr = getArray ();

            arr.useNodes (this, nodes);
        }

        public int length () {
            return length;
        }
    }

    /** Interface that provides a set of nodes.
    */
    static interface Entry {
        /** Set of nodes associated with this entry.
        * @return list of Node objects
        */
        public Collection nodes ();
    }

    /** Empty list of children. Does not allow anybody to insert a node.
    * Treated especially in the attachTo method.
    */
    private static final class Empty extends Children {
        /** array of empty nodes */
        static final Node[] ARRAY = new Node[] {};

        /** @return false, does no action */
        public boolean add (Node[] nodes) {
            return false;
        }

        /** @return false, does no action */
        public boolean remove (Node[] nodes) {
            return false;
        }
    }

    /** Implements the storage of node children by an array.
    * Each new child is added at the end of the array. The nodes are
    * returned in the order they were inserted.
    */
    public static class Array extends Children implements Cloneable {
        /** the entry used for all nodes in the following collection */
        private Entry nodesEntry;
        /** vector of added children */
        protected Collection nodes;

        /** Constructs a new list and allows a subclass to
        * provide its own implementation of <code>Collection</code> to store
        * data in. The collection should be empty and should not
        * be directly accessed in any way after creation.
        *
        * @param c collection to store data in
        */
        protected Array (Collection c) {
            this ();
            nodes = c;
        }

        /** Constructs a new array children without any assigned collection.
        * The collection will be created by a call to method initCollection the
        * first time, children will be used.
        */
        public Array () {
            this.setEntries (Collections.singleton (getNodesEntry ()));
        }

        /** Clones all nodes that are contained in the children list.
        *
        * @return the cloned array for this children
        */
        public Object clone () {
            try {
                final Children.Array ar = (Array)super.clone ();

                MUTEX.readAccess (new Mutex.Action () {
                                      public Object run () {
                                          if (nodes != null) {
                                              // nodes already initilized

                                              // used to create the right type of collection
                                              // clears the content of the collection
                                              // JST: hack, but I have no better idea how to write this
                                              //     pls. notice that in initCollection you can test
                                              //     whether nodes == null => real initialization
                                              //             nodes != null => only create new empty collection
                                              ar.nodes = ar.initCollection ();
                                              ar.nodes.clear ();


                                              // insert copies of the nodes
                                              Iterator it = nodes.iterator ();
                                              while (it.hasNext ()) {
                                                  Node n = (Node)it.next ();
                                                  ar.nodes.add (n.cloneNode ());
                                              }
                                          }
                                          return null;
                                      }
                                  });
                return ar;
            } catch (CloneNotSupportedException e) {
                // this cannot happen
                throw new InternalError ();
            }
        }

        /** Allow subclasses to create a collection, the first time the
        * children are used. It is called only if the collection has not
        * been passed in the constructor.
        * <P>
        * The current implementation returns ArrayList.
        *
        * @return empty or initialized collection to use
        */
        protected Collection initCollection () {
            return new ArrayList ();
        }

        /** This method can be called by subclasses that
        * directly modify the nodes collection to update the 
        * state of the nodes appropriatelly.
        * This method should be called under 
        * MUTEX.writeAccess.
        */
        final void refreshImpl () {
            Array.this.refreshEntry (getNodesEntry ());
            super.computeNodes ();
        }

        /** This method can be called by subclasses that
        * directly modify the nodes collection to update the 
        * state of the nodes appropriatelly.
        */
        protected final void refresh () {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       refreshImpl ();
                                   }
                               });
        }

        /** Getter for the entry.
        */
        final Entry getNodesEntry () {
            if (nodesEntry != null) return nodesEntry;
            synchronized (this) {
                if (nodesEntry != null) return nodesEntry;
                nodesEntry = createNodesEntry ();
                return nodesEntry;
            }
        }

        /** This method allows subclasses (only in this package) to
        * provide own version of entry. Usefull for SortedArray.
        */
        Entry createNodesEntry () {
            return new AE ();
        }

        /** Getter for nodes.
        */
        final Collection getCollection () {
            if (nodes == null) {
                nodes = initCollection ();
            }
            return nodes;
        }

        /*
        * @param arr nodes to add
        * @return true
        */
        public boolean add (final Node[] arr) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       getCollection ().addAll (Arrays.asList (arr));
                                       refreshImpl ();
                                   }
                               });
            return true;
        }

        /*
        * @param arr nodes to remove
        * @return true
        */
        public boolean remove (final Node[] arr) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       getCollection ().removeAll (Arrays.asList (arr));
                                       refreshImpl ();
                                   }
                               });
            return true;
        }

        /** One entry that holds all the nodes in the collection
        * member called nodes.
        */
        private final class AE extends Object implements Entry {
            /** List of elements.
            */
            public Collection nodes () {
                return new LinkedList (getCollection ());
            }
        }
    }

    /** Implements the storage of node children by a map.
    * This class also permits
    * association of a key with any node and to remove nodes by key.
    * Subclasses should reasonably
    * implement {@link #add} and {@link #remove}.
    */
    public static class Map extends Children {
        /** A map to use to store children in.
        * Keys are <code>Object</code>s, values are {@link Node}s.
        * Do <em>not</em> modify elements in the map! Use it only for read access.
        * @associates Node
        */
        protected java.util.Map nodes;

        /** Constructs a new list with a supplied map object.
        * Should be used by subclasses desiring an alternate storage method.
        * The map must not be explicitly modified after creation.
        *
        * @param m the map to use for this list
        */
        protected Map (java.util.Map m) {
            nodes = m;
        }

        /** Constructs a new list using {@link HashMap}.
        */
        public Map () {
        }

        /** Getter for the map.
        * Ensures that the map has been initialized.
        */
        final java.util.Map getMap () {
            // package private only to simplify access from inner classes

            if (nodes == null) {
                nodes = initMap ();
            }
            return nodes;
        }

        /** Called on first use.
        */
        final void callAddNotify () {
            this.setEntries (createEntries (getMap ()));

            super.callAddNotify ();
        }

        /** Method that allows subclasses (SortedMap) to redefine
        * order of entries.
        * @param map the map (Object, Node)
        * @return collection of (Entry)
        */
        Collection createEntries (java.util.Map map) {
            LinkedList l = new LinkedList ();
            Iterator it = map.entrySet ().iterator ();
            while (it.hasNext ()) {
                java.util.Map.Entry e = (java.util.Map.Entry)it.next ();
                l.add (new ME (
                           e.getKey (),
                           (Node)e.getValue ()
                       ));
            }
            return l;
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        * This method should be called under 
        * MUTEX.writeAccess.
        */
        final void refreshImpl () {
            this.setEntries (createEntries (getMap ()));
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        */
        protected final void refresh () {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       refreshImpl ();
                                   }
                               });
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        * This method should be called under 
        * MUTEX.writeAccess.
        *
        * @param key the key that should be refreshed
        */
        final void refreshKeyImpl (Object key) {
            this.refreshEntry (new ME (key, null));
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        *
        * @param key the key that should be refreshed
        */
        protected final void refreshKey (final Object key) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       refreshKeyImpl (key);
                                   }
                               });
        }

        /** Add a collection of new key/value pairs into the map.
        * The supplied map may contain any keys, but the values must be {@link Node}s.
        *
        * @param map the map with pairs to add
        */
        protected final void putAll (final java.util.Map map) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       nodes.putAll (map);
                                       refreshImpl ();
                                       // PENDING sometime we should also call refreshKey...
                                   }
                               });
        }

        /** Add one key and one node to the list.
        * @param key the key
        * @param node the node
        */
        protected final void put (final Object key, final Node node) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       if (nodes.put (key, node) != null) {
                                           refreshKeyImpl (key);
                                       } else {
                                           refreshImpl ();
                                       }
                                   }
                               });
        }

        /** Remove some children from the list by key.
        * @param keys collection of keys to remove
        */
        protected final void removeAll (final Collection keys) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       nodes.keySet ().removeAll (keys);
                                       refreshImpl ();
                                   }
                               });
        }

        /** Remove a given child node from the list by its key.
        * @param key key to remove
        */
        protected void remove (final Object key) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       if (nodes.remove (key) != null) {
                                           refreshImpl ();
                                       }
                                   }
                               });
        }

        /** Initialize some nodes. Allows a subclass to
        * provide a default map to initialize the map with.
        * Called only if the map has not been provided in the constructor.
        *
        * <P>
        * The default implementation returns <code>new HashMap (7)</code>.
        *
        * @return a map from <code>Object</code>s to {@link Node}s
        */
        protected java.util.Map initMap () {
            return new HashMap (7);
        }

        /** Does nothing. Should be reimplemented in a subclass wishing
        * to support external addition of nodes.
        *
        * @param arr nodes to add
        * @return <code>false</code> in the default implementation
        */
        public boolean add (Node[] arr) {
            return false;
        }

        /** Does nothing. Should be reimplemented in a subclass wishing
        * to support external removal of nodes.
        * @param arr nodes to remove
        * @return <code>false</code> in the default implementation
        */
        public boolean remove (Node[] arr) {
            return false;
        }

        /** Entry mapping one key to the node.
        */
        final static class ME extends Object implements Entry {
            /** key */
            public Object key;
            /** node set */
            public Node node;

            /** Constructor.
            */
            public ME (Object key, Node node) {
                this.key = key;
                this.node = node;
            }

            /** Nodes */
            public Collection nodes () {
                return Collections.singleton (node);
            }

            /** Hash code.
            */
            public int hashCode () {
                return key.hashCode ();
            }

            /** Equals.
            */
            public boolean equals (Object o) {
                if (o instanceof ME) {
                    ME me = (ME)o;
                    return key.equals (me.key);
                }
                return false;
            }

            public String toString () {
                return "Key (" + key + ")"; // NOI18N
            }
        }
    }

    /** Maintains a list of children sorted by the provided comparator in an array.
    * The comparator can change during the lifetime of the children, in which case
    * the children are resorted.
    */
    public static class SortedArray extends Children.Array {
        /** comparator to use */
        private Comparator comp;

        /** Create an empty list of children. */
        public SortedArray() {
        }

        /** Create an empty list with a specified storage method.
        *
        * @param c collection to store data in
        * @see Children.Array#Children.Array(Collection)
        */
        protected SortedArray (Collection c) {
            super(c);
        }

        /** Set the comparator. The children will be resorted.
        * The comparator is used to compare Nodes, if no
        * comparator is used then nodes will be compared by
        * the use of natural ordering.
        *
        * @param c the new comparator
        */
        public void setComparator (final Comparator c) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       comp = c;
                                       refresh ();
                                   }
                               });
        }

        /** Get the current comparator.
        * @return the comparator
        */
        public Comparator getComparator () {
            return comp;
        }

        /** This method allows subclasses (only in this package) to
        * provide own version of entry. Usefull for SortedArray.
        */
        Entry createNodesEntry () {
            return new SAE ();
        }

        /** One entry that holds all the nodes in the collection
        * member called nodes.
        */
        private final class SAE extends Object implements Entry {
            /** List of elements.
            */
            public Collection nodes () {
                Comparator c = comp;
                if (c == null) {
                    // no sorting
                    return new TreeSet (getCollection ());
                } else {
                    TreeSet ts = new TreeSet (c);
                    ts.addAll (getCollection ());
                    return ts;
                }
            }
        }

    } // end of SortedArray

    /** Maintains a list of children sorted by the provided comparator in a map.
    * Similar to {@link Children.SortedArray}.
    */
    public static class SortedMap extends Children.Map {
        /** comparator to use */
        private Comparator comp;

        /** Create an empty list. */
        public SortedMap () {
        }

        /** Create an empty list with a specific storage method.
        *
        * @param m the map to use with this object
        * @see Children.Map#Children.Map(java.util.Map)
        */
        protected SortedMap (java.util.Map map) {
            super(map);
        }


        /** Set the comparator. The children will be resorted.
        * The comparator is used to compare Nodes, if no
        * comparator is used then values will be compared by
        * the use of natural ordering.
        *
        * @param c the new comparator that should compare nodes
        */
        public void setComparator (final Comparator c) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       comp = c;
                                       refresh ();
                                   }
                               });
        }

        /** Get the current comparator.
        * @return the comparator
        */
        public Comparator getComparator () {
            return comp;
        }

        /** Method that allows subclasses (SortedMap) to redefine
        * order of entries.
        * @param map the map (Object, Node)
        * @return collection of (Entry)
        */
        Collection createEntries (java.util.Map map) {
            // SME objects use natural ordering
            TreeSet l = new TreeSet (new SMComparator ());

            Iterator it = map.entrySet ().iterator ();
            while (it.hasNext ()) {
                java.util.Map.Entry e = (java.util.Map.Entry)it.next ();
                l.add (new ME (
                           e.getKey (),
                           (Node)e.getValue ()
                       ));
            }

            return l;
        }

        /** Sorted map entry can be used for comparing.
        */
        final class SMComparator implements Comparator {
            public int compare(Object o1, Object o2) {
                ME me1 = (ME)o1;
                ME me2 = (ME)o2;

                Comparator c = comp;
                if (c == null) {
                    // compare keys
                    return ((Comparable)me1.key).compareTo (me2.key);
                } else {
                    return c.compare (me1.node, me2.node);
                }
            }
        }

    } // end of SortedMap

    /** Implements an array of child nodes associated nonuniquely with keys and sorted by these keys.
    * There is a {@link #createNodes(Object) method} that should for each
    * key create an array of nodes that represents the key.
    */
    public static abstract class Keys extends Children.Array {
        /** add array children before or after keys ones */
        private boolean before;

        /** Special handling for clonning.
        */
        public Object clone () {
            Keys k = (Keys)super.clone ();
            return k;
        }

        /* Adds additional nodes to the children list.
        * Works same like Children.Array. 
        *
        * @param arr nodes to add
        * @return true
        */
        public boolean add (Node[] arr) {
            return super.add (arr);
        }

        /* Removes nodes added by add from the list.
        * @param arr nodes to remove
        * @return if nodes has been removed (they need not necessary be,
        *   because only nodes added by add can be removed, not those
        *   created for key objects)
        */
        public boolean remove (final Node[] arr) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       // expecting arr.length == 1, which is the usual case
                                       for (int i = 0; i < arr.length; i++) {
                                           if (!nodes.contains (arr[i])) {
                                               arr[i] = null;
                                           }
                                       }

                                       superRemove (arr);
                                   }
                               });

            return true;
        }

        /** Access method to super impl of remove.
        */
        final void superRemove (Node[] arr) {
            super.remove (arr);
        }

        /** Refresh the child nodes for a given key.
        *
        * @param key the key to refresh
        */
        protected final void refreshKey (final Object key) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       Keys.this.refreshEntry (new KE (key));
                                   }
                               });
        }

        /** Set new keys for this children object. Setting of keys
        * does not necessarily lead to the creation of nodes. It happens only
        * when the list has already been initialized.
        *
        * @param keysSet the keys for the nodes (collection of any objects)
        */
        protected final void setKeys (Collection keysSet) {
            final LinkedList l = new LinkedList ();
            Iterator it = keysSet.iterator ();
            while (it.hasNext ()) {
                l.add (new KE (it.next ()));
            }
            updateArrayEntry (l);

            MUTEX.postWriteRequest (new Runnable () {
                                        public void run () {
                                            //System.err.println("New entries: " + Keys.this.getEntries () + " for " + Keys.this.getNode ());
                                            Keys.this.setEntries (l);
                                        }
                                    });
        }

        /** Set keys for this list.
        *
        * @param keys the keys for the nodes
        * @see #setKeys(Collection)
        */
        protected final void setKeys (final Object[] keys) {
            final LinkedList l = new LinkedList ();

            int s = keys.length;
            for (int i = 0; i < s; i++) {
                l.add (new KE (keys[i]));
            }
            updateArrayEntry (l);

            MUTEX.postWriteRequest (new Runnable () {
                                        public void run () {
                                            //System.err.println("New enTRIES: " + Keys.this.getEntries () + " for " + Keys.this.getNode ());
                                            Keys.this.setEntries (l);
                                        }
                                    });
        }

        /** Set whether new nodes should be added to the beginning or end of sublists for a given key.
        *
        * @param b <code>true</code> if the children should be added before
        */
        protected final void setBefore (final boolean b) {
            MUTEX.postWriteRequest (new Runnable () {
                                        public void run () {
                                            if (before != b) {
                                                LinkedList l = Keys.this.getEntries ();
                                                l.remove (getNodesEntry ());
                                                before = b;
                                                updateArrayEntry (l);
                                                Keys.this.setEntries (l);
                                            }
                                        }
                                    });
        }

        /** Create nodes for a given key.
        * @param key the key
        * @return child nodes for this key
        */
        protected abstract Node[] createNodes (Object key);


        /** Updates the list of entries with the
        * entry displaying the nodes from Children.Array
        */
        private void updateArrayEntry (LinkedList l) {
            if (before) {
                l.addFirst (getNodesEntry ());
            } else {
                l.addLast (getNodesEntry ());
            }
        }

        /** Entry for a key
        */
        private final class KE extends Object implements Entry {
            private Object key;

            /** Constructor.
            */
            public KE (Object key) {
                this.key = key;
            }

            /** Nodes are taken from the create nodes.
            */
            public Collection nodes () {
                return new LinkedList (Arrays.asList (createNodes (key)));
            }

            public int hashCode () {
                return key.hashCode ();
            }

            public boolean equals (Object o) {
                if (o instanceof KE) {
                    o = ((KE)o).key;
                    return key.equals (o);
                }
                return false;
            }

            public String toString () {
                String s = key.toString ();
                if (s.length () > 80) {
                    s = s.substring (0, 80);
                }
                return "Key (" + s + ")"; // NOI18N
            }
        }
    } // end of Keys

    /*
      static void printNodes (Node[] n) {
        for (int i = 0; i < n.length; i++) {
          System.out.println ("  " + i + ". " + n[i].getName () + " number: " + System.identityHashCode (n[i]));
        }
        }
        */

    /* JST: Useful test routine ;-) *
    static {
     if (org.openide.TopManager.getDefault () != null) {
      final TopComponent.Registry r = TopComponent.getRegistry ();
      r.addPropertyChangeListener (new PropertyChangeListener () {
        Node last = new AbstractNode (LEAF);
        
        public void propertyChange (PropertyChangeEvent ev) {
          Node[] arr = r.getCurrentNodes ();
          if (arr != null && arr.length == 1) {
            last = arr[0];
          }
          org.openide.TopManager.getDefault ().setStatusText (
            "Activated node: " + last + " \nparent: " + last.getParentNode ()
          );
    //        System.out.println("Children list: " + last.getChildren ().getNodesCount ());
    //        javax.swing.tree.TreeNode v = org.openide.explorer.view.Visualizer.findVisualizer (last);
    //        System.out.println("Visualizer: " + v.getChildCount ());
        }
      });
     }
}
    */
}



/*
* Log
*  68   Gandalf-post-FCS1.66.2.0    3/24/00  Ales Novak      NullPointerException
*  67   Gandalf   1.66        1/13/00  Jesse Glick     NOI18N
*  66   Gandalf   1.65        1/12/00  Jesse Glick     NOI18N
*  65   Gandalf   1.64        12/27/99 Jaroslav Tulach #5042
*  64   Gandalf   1.63        12/27/99 Jaroslav Tulach 
*  63   Gandalf   1.62        12/21/99 Jaroslav Tulach Do not fire when no 
*       parent is there.
*  62   Gandalf   1.61        11/24/99 Jaroslav Tulach When a node is deleted, 
*       it is sooner removed from WeakHashMap
*  61   Gandalf   1.60        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  60   Gandalf   1.59        10/11/99 Jaroslav Tulach refreshEntry throws less 
*       exceptions.
*  59   Gandalf   1.58        10/10/99 Petr Hamernik   console debug messages 
*       removed.
*  58   Gandalf   1.57        10/5/99  Ales Novak      #4070
*  57   Gandalf   1.56        9/25/99  Jaroslav Tulach #3805
*  56   Gandalf   1.55        9/22/99  Jaroslav Tulach retainAll before 
*       setEntries
*  55   Gandalf   1.54        9/17/99  Jaroslav Tulach Reorder of nodes works.
*  54   Gandalf   1.53        9/10/99  Jaroslav Tulach Children.Keys does not 
*       have keys field.
*  53   Gandalf   1.52        9/6/99   Jaroslav Tulach updateOrder should not 
*       throw NullPointerExc.
*  52   Gandalf   1.51        9/3/99   Jaroslav Tulach Will print error 
*       description.
*  51   Gandalf   1.50        9/2/99   Jaroslav Tulach getNodes improvement.
*  50   Gandalf   1.49        9/1/99   Jaroslav Tulach Mutex.postWriteRequest
*  49   Gandalf   1.48        8/30/99  Jaroslav Tulach Less deadlocks?
*  48   Gandalf   1.47        8/30/99  Jaroslav Tulach Reorder problems fixed.
*  47   Gandalf   1.46        8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  46   Gandalf   1.45        8/19/99  Jaroslav Tulach Notifies permutation 
*       exception on if netbeans.debug.nodes is set.
*  45   Gandalf   1.44        8/18/99  Jaroslav Tulach writeAccess (Runnable) 
*       instead of Mutex.Action
*  44   Gandalf   1.43        8/18/99  Jaroslav Tulach Catching permutation 
*       exception.
*  43   Gandalf   1.42        8/17/99  Ian Formanek    Undone last change
*  42   Gandalf   1.41        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  41   Gandalf   1.40        8/12/99  Jaroslav Tulach addNotify is called with 
*       postReadRequest method.
*  40   Gandalf   1.39        8/9/99   Ian Formanek    Fixed to compile
*  39   Gandalf   1.38        8/9/99   Jaroslav Tulach Children.Map.remove works
*       better.
*  38   Gandalf   1.37        8/5/99   Jaroslav Tulach Does not create NodeSet 
*       for empty array.
*  37   Gandalf   1.36        8/4/99   Jaroslav Tulach Small improvement of 
*       synchronization in setKeys
*  36   Gandalf   1.35        8/3/99   Jaroslav Tulach Browser works.
*  35   Gandalf   1.34        7/23/99  Jaroslav Tulach Support for clonning.
*  34   Gandalf   1.33        7/19/99  Jaroslav Tulach Changed the time when 
*       addNotify is called in Children.Keys
*  33   Gandalf   1.32        7/18/99  Petr Hamernik   addNotify bugfix (I hope)
*  32   Gandalf   1.31        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  31   Gandalf   1.30        5/27/99  Jesse Glick     [JavaDoc]
*  30   Gandalf   1.29        5/25/99  Jaroslav Tulach Children.Keys.setKeys now
*       clones the collection, so no later modification matter
*  29   Gandalf   1.28        5/25/99  Jaroslav Tulach 
*  28   Gandalf   1.27        5/19/99  Jaroslav Tulach Cosmetic changes.
*  27   Gandalf   1.26        5/15/99  Jaroslav Tulach refreshKey works.
*  26   Gandalf   1.25        5/7/99   Jaroslav Tulach 
*  25   Gandalf   1.24        5/7/99   Jan Jancura     Bugfix (Jarda's idea)
*  24   Gandalf   1.23        5/7/99   Jaroslav Tulach setBefore is runned under
*       MUTEX.
*  23   Gandalf   1.22        5/6/99   Jaroslav Tulach setKeys allows nodes to 
*       be deleted.
*  22   Gandalf   1.21        4/23/99  Jaroslav Tulach Children.Map has lazy 
*       initializatio of the map
*  21   Gandalf   1.20        4/21/99  Jaroslav Tulach DataObjects can be 
*       finalized
*  20   Gandalf   1.19        4/20/99  Jaroslav Tulach 
*  19   Gandalf   1.18        4/20/99  Jaroslav Tulach Children supports weak 
*       references.
*  18   Gandalf   1.17        4/16/99  Jaroslav Tulach Changes in children.
*  17   Gandalf   1.16        4/16/99  Jan Jancura     
*  16   Gandalf   1.15        4/11/99  Jaroslav Tulach Bug fix #1507
*  15   Gandalf   1.14        4/7/99   Jan Jancura     Bug
*  14   Gandalf   1.13        4/2/99   Jesse Glick     [JavaDoc]
*  13   Gandalf   1.12        4/2/99   Jan Jancura     ObjectBrowser Support
*  12   Gandalf   1.11        3/18/99  Jesse Glick     [JavaDoc]
*  11   Gandalf   1.10        3/16/99  Jesse Glick     [JavaDoc]
*  10   Gandalf   1.9         3/12/99  Jaroslav Tulach 
*  9    Gandalf   1.8         3/12/99  Jaroslav Tulach Children.Keys have 
*       add/remove methods implemented
*  8    Gandalf   1.7         3/11/99  Jan Jancura     
*  7    Gandalf   1.6         2/16/99  David Simonek   
*  6    Gandalf   1.5         2/4/99   Jaroslav Tulach Back to normal Mutex
*  5    Gandalf   1.4         2/4/99   Jaroslav Tulach Children.MUTEX 
*       synchronizes on Component Tree Lock.
*  4    Gandalf   1.3         2/3/99   Jaroslav Tulach getNodes sometimes does 
*       not need readAccess lock
*  3    Gandalf   1.2         1/18/99  David Simonek   
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
