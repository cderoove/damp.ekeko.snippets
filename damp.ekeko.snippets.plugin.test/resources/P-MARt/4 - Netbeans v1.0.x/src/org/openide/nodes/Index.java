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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.util.Mutex;
import org.openide.util.NotImplementedException;


/** Index cookie providing operations useful for reordering
* child nodes. {@link IndexedNode} is the common implementation.
*
* @author Jaroslav Tulach, Dafe Simonek
*/
public interface Index extends Node.Cookie {
    /** Get the number of nodes.
    * @return the count
    */
    public int getNodesCount ();

    /** Get the child nodes.
    * @return array of nodes that can be sorted by this index
    */
    public Node[] getNodes ();

    /** Get the index of a given node.
    * @param node node to find index of
    * @return index of the node, or <code>-1</code> if no such node was found
    */
    public int indexOf (final Node node);

    /** Invoke a dialog for reordering the children.
    */
    public void reorder ();

    /** Reorder all children with a given permutation.
    * @param perm permutation with the length of current nodes
    * @exception IllegalArgumentException if the permutation is not valid
    * @see Children#reorder
    */
    public void reorder (int[] perm);

    /** Move the element at the <code>x</code>-th position to the <code>y</code>-th position. All
    * elements after the <code>y</code>-th position are moved down.
    *
    * @param x the position to remove the element from
    * @param y the position to insert the element to
    * @exception IndexOutOfBoundsException if an index is out of bounds
    */
    public void move (int x, int y);

    /** Exchange two elements.
    * @param x position of the first element
    * @param y position of the second element
    * @exception IndexOutOfBoundsException if an index is out of bounds
    */
    public void exchange (int x, int y);

    /** Move an element up.
    * @param x index of element to move up
    * @exception IndexOutOfBoundsException if an index is out of bounds
    */
    public void moveUp (int x);

    /** Move an element down.
    * @param x index of element to move down
    * @exception IndexOutOfBoundsException if an index is out of bounds
    */
    public void moveDown (int x);

    /** Add a new listener to the listener list. The listener will be notified of
    * any change in the order of the nodes.
    *
    * @param chl new listener
    */
    public void addChangeListener (final ChangeListener chl);

    /** Remove a listener from the listener list.
    *
    * @param chl listener to remove
    */
    public void removeChangeListener (final ChangeListener chl);

    /*********************** Inner classes ***********************/

    /** A support class implementing some methods of the <code>Index</code>
    * cookie.
    */
    public static abstract class Support implements Index {
        /** Registered listeners 
         * @associates ChangeListener*/
        private HashSet listeners;

        /** Default constructor. */
        public Support () {
        }

        /* Moves element at x-th position to y-th position. All
        * elements after the y-th position are moved down.
        *
        * @param x the position to remove the element from
        * @param y the position to insert the element to
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void move (final int x, final int y) {
            int[] perm = new int[getNodesCount()];
            for (int i = 0; i < perm.length; i++) {
                if (i >= x && i < y) {
                    perm[i] = i - 1;
                } else {
                    if (i >= y && i < x) {
                        perm[i] = i + 1;
                    } else {
                        perm[i] = i;
                    }
                }
            }
            perm[x] = y;
            perm[y] = x;

            reorder (perm);
        }

        /* Exchanges two elements.
        * @param x position of the first element
        * @param y position of the second element
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void exchange (final int x, final int y) {
            int[] perm = new int[getNodesCount ()];
            for (int i = 0; i < perm.length; i++) {
                perm[i] = i;
            }
            perm[x] = y;
            perm[y] = x;

            reorder (perm);
        }

        /* Moves element up.
        * @param x index of element to move up
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void moveUp (final int x) {
            exchange (x, x - 1);
        }

        /* Moves element down.
        * @param x index of element to move down
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void moveDown (final int x) {
            exchange (x, x + 1);
        }

        /* Adds new listener to the listener list. Listener is notified of
        * any change in ordering of nodes.
        *
        * @param chl new listener
        */
        public void addChangeListener (final ChangeListener chl) {
            if (listeners == null)
                listeners = new HashSet();
            listeners.add(chl);
        }

        /* Removes listener from the listener list.
        * Removed listener isn't notified no more.
        *
        * @param chl listener to remove
        */
        public void removeChangeListener (final ChangeListener chl) {
            if (listeners == null)
                return;
            listeners.remove(chl);
        }

        /** Fires notification about reordering to all
        * registered listeners.
        *
        * @param che change event to fire off
        */
        protected void fireChangeEvent (ChangeEvent che) {
            if (listeners == null)
                return;
            HashSet cloned;
            // clone listener list
            synchronized (this) {
                cloned = (HashSet)listeners.clone();
            }
            // fire on cloned list to prevent from modifications when firing
            for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
                ((ChangeListener)iter.next()).stateChanged(che);
            }
        }

        /** Get the nodes; should be overridden if needed.
        * @return the nodes
        */
        public abstract Node[] getNodes ();

        /** Get the index of a node. Simply scans through the array returned by {@link #getNodes}.
        * @param node the node
        * @return the index, or <code>-1</code> if the node was not found
        */
        public int indexOf (final Node node) {
            Node[] arr = getNodes ();
            for (int i = 0; i < arr.length; i++) {
                if (node.equals (arr[i])) {
                    return i;
                }
            }
            return -1;
        }

        /** Reorder the nodes with dialog; should be overridden if needed.
        */
        public void reorder () {
            IndexedCustomizer ic = new IndexedCustomizer ();
            ic.setObject (this);
            ic.show ();
        }

        /** Get the node count. Subclasses must provide this.
        * @return the count
        */
        public abstract int getNodesCount ();

        /** Reorder by permutation. Subclasses must provide this.
        * @param perm the permutation
        */
        public abstract void reorder (int[] perm);

    } // end of Support inner class

    /** Reorderable children list stored in an array.
    */
    public static class ArrayChildren extends Children.Array implements Index {
        /** Support instance for delegation of some <code>Index</code> methods. */
        protected Index support;

        /** Constructor for the support.
        */
        public ArrayChildren () {
            this (null);
        }

        /** Constructor.
        * @param ar the array
        */
        private ArrayChildren (List ar) {
            super (ar);
            // create support instance for delegation of common tasks
            support = new Support() {
                          public Node[] getNodes () {
                              return ArrayChildren.this.getNodes ();
                          }
                          public int getNodesCount () {
                              return ArrayChildren.this.getNodesCount();
                          }
                          public void reorder (int[] perm) {
                              ArrayChildren.this.reorder(perm);
                              fireChangeEvent(new ChangeEvent(ArrayChildren.this));
                          }
                      };
        }

        /** If default constructor is used, then this method is called to lazily create
        * the collection. Even it claims that it returns Collection only subclasses
        * of List are valid values. 
        * <P>
        * This implementation returns ArrayList.
        *
        * @return any List collection.
        */
        protected java.util.Collection initCollection () {
            return new ArrayList ();
        }

        /* Reorders all children with given permutation.
        * @param perm permutation with the length of current nodes
        * @exception IllegalArgumentException if the perm is not valid permutation
        */
        public void reorder (final int[] perm) {
            MUTEX.writeAccess (new Runnable () {
                                   public void run () {
                                       Object[] n = nodes.toArray ();
                                       List l = (List)nodes;
                                       for (int i = 0; i < n.length; i++) {
                                           l.set (perm[i], n[i]);
                                       }

                                       refresh ();
                                   }
                               });
        }

        /** Invokes a dialog for reordering children using {@link IndexedCustomizer}.
        */
        public void reorder () {
            IndexedCustomizer ic = new IndexedCustomizer();
            ic.setObject(this);
            ic.show();
        }

        /* Moves element at x-th position to y-th position. All
        * elements after the y-th position are moved down.
        * Delegates functionality to Index.Support.
        *
        * @param x the position to remove the element from
        * @param y the position to insert the element to
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void move (final int x, final int y) {
            support.move(x, y);
        }

        /* Exchanges two elements.
        * Delegates functionality to Index.Support.
        * @param x position of the first element
        * @param y position of the second element
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void exchange (final int x, final int y) {
            support.exchange(x, y);
        }

        /* Moves element up.
        * Delegates functionality to Index.Support.
        * @param x index of element to move up
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void moveUp (final int x) {
            support.exchange(x, x - 1);
        }

        /* Moves element down.
        * Delegates functionality to Index.Support.
        * @param x index of element to move down
        * @exception IndexOutOfBoundsException if an index is out of bounds
        */
        public void moveDown (final int x) {
            support.exchange(x, x + 1);
        }

        /* Returns the index of given node.
        * @param node Node to find index of.
        * @return Index of the node, -1 if no such node was found.
        */
        public int indexOf (final Node node) {
            Integer result =
                (Integer)MUTEX.writeAccess (new Mutex.Action () {
                                                public Object run () {
                                                    return new Integer(((List)nodes).indexOf(node));
                                                }
                                            });
            return result.intValue();
        }

        /* Adds new listener to the listener list. Listener is notified of
        * any change in ordering of nodes.
        *
        * @param chl new listener
        */
        public void addChangeListener (final ChangeListener chl) {
            support.addChangeListener(chl);
        }

        /* Removes listener from the listener list.
        * Removed listener isn't notified no more.
        *
        * @param chl listener to remove
        */
        public void removeChangeListener (final ChangeListener chl) {
            support.removeChangeListener(chl);
        }

    } // End of ArrayChildren inner class


}

/*
* Log
*  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  9    Gandalf   1.8         8/27/99  Jaroslav Tulach 
*  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    Gandalf   1.6         4/16/99  Jaroslav Tulach Changes in children.
*  6    Gandalf   1.5         3/22/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         3/21/99  Jaroslav Tulach Repository displayed ok.
*  4    Gandalf   1.3         3/17/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         3/17/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         2/16/99  David Simonek   
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
