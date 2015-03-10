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

import java.util.*;

/** Holder of nodes for a children object. Communicates
* with children to notify when created/finalized.
*
* @author Jaroslav Tulach
*/
final class ChildrenArray extends NodeAdapter {
    /** children */
    private Children children;
    /** nodes associated */
    private Node[] nodes;
    /** mapping from the (Children.Info, Collection (Node)) */
    private WeakHashMap map;

    /** Creates new ChildrenArray */
    private ChildrenArray () {
    }

    /** When finalized notify the children.
    */
    protected void finalize () {
        children.finalizedChildrenArray ();
    }

    /** Create new instance of this object attached to the children.
    */
    public static ChildrenArray create (Children ch) {
        ChildrenArray a = new ChildrenArray ();

        // register the array with the children
        ch.registerChildrenArray (a, true);

        // this call can cause a lot of callbacks => be prepared
        // to handle them as clean as possible
        ch.callAddNotify ();

        // now attach to children, so when children == null => we are
        // not fully initialized!!!!
        a.children = ch;

        return a;
    }

    /** Getter method to receive a set of computed nodes.
    */
    public Node[] nodes () {
        if (children == null) {
            // not fully initialize
            return null;
        }

        if (nodes == null) {
            nodes = children.justComputeNodes ();
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].addNodeListener (this);
            }
            // if at least one node => be weak
            children.registerChildrenArray (this, nodes.length > 0);
        }

        return nodes;
    }

    /** Clears the array of nodes.
    */
    public void clear () {
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].removeNodeListener (this);
            }
            nodes = null;
            // register in the childrens to be hold by hard reference
            // because we keep no reference to nodes, we can be
            // hard holded by children
            children.registerChildrenArray (this, false);
        }
    }

    /** Finalizes nodes by calling get on weak hash map,
    * all references stored in the map, that are finalized
    * will be cleared.
    */
    public void finalizeNodes () {
        WeakHashMap m = map;
        if (m != null) {
            // processes the queue of garbage
            // collected keys
            m.remove (null);
        }
    }

    /** Initilized if has some nodes.
    */
    public boolean isInitialized () {
        return nodes != null;
    }

    /** Gets the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public Collection nodesFor (Children.Info info) {
        if (map == null) {
            map = new WeakHashMap (7);
        }

        Collection nodes = (Collection)map.get (info);
        if (nodes == null) {
            nodes = info.entry.nodes ();
            info.length = nodes.size ();
            map.put (info, nodes);
        }
        return nodes;
    }

    /** Refreshes the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public void useNodes (Children.Info info, Collection list) {
        if (map == null) {
            map = new WeakHashMap (7);
        }

        info.length = list.size ();

        map.put (info, list);
    }

}

/*
* Log
*  5    Gandalf   1.4         11/24/99 Jaroslav Tulach When a node is deleted, 
*       it is sooner removed from WeakHashMap
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  2    Gandalf   1.1         9/2/99   Jaroslav Tulach getNodes improvement.
*  1    Gandalf   1.0         8/27/99  Jaroslav Tulach 
* $
*/