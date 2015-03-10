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
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.awt.JPopupMenuPlus;

/** Utility class for operations on nodes.
*
* @author Jaroslav Tulach, Petr Hamernik, Dafe Simonek
*/
public final class NodeOp extends Object {
    private NodeOp() {}

    /** default node actions */
    private static SystemAction[] defaultActions;

    /** Set the default actions for all nodes.
    * @param def array of default actions
    * @exception SecurityException when the default actions have already been set
    */
    public static synchronized void setDefaultActions (SystemAction[] def) {
        if (defaultActions != null) throw new SecurityException ();

        defaultActions = def;
    }

    /** Get the default actions for all nodes.
    * @return array of default actions
    */
    public static SystemAction[] getDefaultActions () {
        return defaultActions == null ? new SystemAction[0] : defaultActions;
    }

    /** Compute common menu for specified nodes.
    * Provides only those actions supplied by all nodes in the list.
    * @param nodes the nodes
    * @return the menu for all nodes
    */
    public static JPopupMenu findContextMenu (Node[] nodes) {
        if (nodes.length != 1)
            return findContextMenuImpl(nodes);
        else
            return nodes[0].getContextMenu();
    }

    /** Method for finding popup menu for one or more nodes.
    *
    * @param nodes array of nodes
    * @return popup menu for this array
    */
    static JPopupMenu findContextMenuImpl (Node[] nodes) {
        JPopupMenu menu = new JPopupMenuPlus ();
        // hashtable: SystemAction -> Integer
        HashMap actions = new HashMap ();

        // counts the number of occurences for each action
        for (int n = 0; n < nodes.length; n++) {
            SystemAction[] arr = nodes[n].getActions ();
            if (arr == null) {
                // use default actions
                arr = defaultActions;
            }
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != null) {
                    Integer cntInt = (Integer)actions.get (arr[i]);
                    int cnt = cntInt == null ? 0 : cntInt.intValue ();
                    actions.put (arr[i], new Integer (cnt + 1));
                }
            }
        }

        // take all actions that are nodes.length number times
        if (!actions.isEmpty ()) {
            SystemAction[] arr = nodes[0].getActions ();
            if (arr == null) {
                // use default
                arr = defaultActions;
            }
            boolean canSep = false;
            for (int i = 0; i < arr.length; i++) {
                boolean addSep = true;
                if (arr[i] != null) {
                    Integer cntInt = (Integer)actions.get (arr[i]);
                    int cnt = cntInt == null ? 0 : cntInt.intValue ();
                    if (cnt == nodes.length) {
                        addSep = false;
                        canSep = true;
                        JMenuItem item;
                        if (arr[i] instanceof Presenter.Popup) {
                            item = ((Presenter.Popup)arr[i]).getPopupPresenter ();
                        } else {
                            item = new JMenuItem (arr[i].getName ());
                            item.setEnabled (false);
                        }
                        menu.add (item);
                    }
                }

                if (addSep && canSep) {
                    menu.addSeparator ();
                    canSep = false;
                }
            }
        }

        return menu;
    }

    /** Test whether the second node is a (direct) child of the first one.
    * @param parent parent node
    * @param son son node
    * @return <code>true</code> if so
    */
    public static boolean isSon (Node parent, Node son) {
        return son.getParentNode () == parent;
    }

    /** Find a path (by name) from one node to the root or a parent.
    * @param node the node to start in
    * @param parent parent node to stop in (can be <code>null</code> for the root)
    * @return list of child names--i.e. a path from the parent to the child node
    */
    public static String[] createPath (Node node, Node parent) {
        LinkedList ar = new LinkedList ();

        while (node != null && node != parent) {
            if (node.getName() == null)
                throw new NullPointerException ("" + node.getClass () + " : " + node.getDisplayName ()); // NOI18N
            ar.addFirst (node.getName ());
            node = node.getParentNode ();
        }

        String[] res = new String [ar.size ()];
        ar.toArray (res);
        return res;
    }

    /** Look for a node child of given name.
    * @param node node to search in
    * @param name name of child to look for
    * @return the found child, or <code>null</code> if there is no such child
    */
    public static Node findChild (Node node, String name) {
        return node.getChildren ().findChild (name);
    }

    /** Traverse a path from a parent node down, by an enumeration of names.
    * @param start node to start searching at
    * @param names enumeration of <code>String</code>s containing names of nodes
    *   along the path
    * @return the node with such a path from the start node
    * @exception NodeNotFoundException if the node with such name
    *   does not exists; the exception contains additional information
    *   about the failure.
    */
    public static Node findPath (Node start, Enumeration names)
    throws NodeNotFoundException {
        int depth = 0;

        while (names.hasMoreElements ()) {
            String name = (String)names.nextElement ();
            Node next = findChild (start, name);
            if (next == null) {
                // no element in list matched the name => fail
                // fire exception with the last accessed node and the
                // name of child that does not exists
                throw new NodeNotFoundException (start, name, depth);
            } else {
                // go on next node
                start = next;
            }

            // continue on next depth
            depth++;
        }
        return start;
    }

    /** Traverse a path from a parent node down, by an enumeration of names.
    * @param start node to start searching at
    * @param names names of nodes
    *   along the path
    * @return the node with such a path from the start node
    * @exception NodeNotFoundException if the node with such name
    *   does not exists; the exception contains additional information
    *   about the failure.
    */
    public static Node findPath (Node start, String[] names)
    throws NodeNotFoundException {
        return findPath (start, new ArrayEnumeration (names));
    }

    /** Find the root for a given node.
    * @param node the node
    * @return its root
    */
    public static Node findRoot (Node node) {
        for (;;) {
            Node parent = node.getParentNode ();
            if (parent == null) return node;
            node = parent;
        }
    }


    /** Package private utility method, support for sorting nodes
    * in children in natural ordering (without comparator).
    * Computes the permutation how to sort children.
    * @return permutation to use on children to achieve the right
    *  ordering
    */
    final static int[] computeSortingPermutation (Node[] nodes) {
        return computeSortingPermutation(nodes, null);
    }

    /** Package private utility method. Support for sorting nodes
    * in children.
    * Computes the permutation how to sort children with given comparator.
    * @return permutation to use on children to achieve the right
    *  ordering
    */
    final static int[] computeSortingPermutation (Node[] nodes, Comparator comp) {
        // creates sorted map that assignes to nodes their original
        // position
        TreeMap tm = comp == null ? new TreeMap () : new TreeMap (comp);
        for (int i = 0; i < nodes.length; i++) {
            tm.put (nodes[i], new Integer (i));
        }
        // takes nodes one by one in the new order and
        // creates permutation array
        int[] perm = new int[nodes.length];
        Iterator it = tm.entrySet ().iterator ();
        for (int i = 0; i < perm.length; i++) {
            Map.Entry entry = (Map.Entry)it.next ();
            // old position of the node that is now on i-th position
            int oldPos = ((Integer)entry.getValue ()).intValue ();
            // perm must move the object on the oldPos to new position i
            perm[oldPos] = i;
        }
        return perm;
    }

    /** Compute a permutation between two arrays of nodes. The arrays
    * must have the same size. The permutation then can be
    * applied to the first array to create the
    * second array.
    *
    * @param arr1 first array
    * @param arr2 second array
    * @return the permutation, or <code>null</code> if the arrays are the same
    * @exception IllegalArgumentException if the arrays cannot be permuted to each other. Either
    *    they have different sizes or they do not contain the same elements.
    * @see Children#reorder
    */
    public static int[] computePermutation (Node[] arr1, Node[] arr2)
    throws IllegalArgumentException {
        if (arr1.length != arr2.length) {
            int max = Math.max (arr1.length, arr2.length);
            StringBuffer sb = new StringBuffer ();
            for (int i = 0; i < max; i++) {
                sb.append (i + " "); // NOI18N
                if (i < arr1.length) {
                    sb.append (arr1[i].getName ());
                } else {
                    sb.append ("---"); // NOI18N
                }
                sb.append (" = "); // NOI18N
                if (i < arr2.length) {
                    sb.append (arr2[i].getName ());
                } else {
                    sb.append ("---"); // NOI18N
                }
                sb.append ('\n');
            }
            throw new IllegalArgumentException (sb.toString ());
        }

        // creates map that assignes to nodes their original
        // position
        HashMap map = new HashMap ();
        for (int i = 0; i < arr2.length; i++) {
            map.put (arr2[i], new Integer (i));
        }
        // takes nodes one by one in the new order and
        // creates permutation array
        int[] perm = new int[arr1.length];
        int diff = 0;

        for (int i = 0; i < arr1.length; i++) {
            // get the position of the i-th argument in the second array
            Integer newPos = (Integer)map.get (arr1[i]);
            if (newPos == null) {
                // not permutation i-th element is missing in the array
                throw new IllegalArgumentException ("Missing permutation index " + i); // NOI18N
            }
            // perm must move the object to the newPos
            perm[i] = newPos.intValue ();

            if (perm[i] != i) {
                diff++;
            }
        }
        return diff == 0 ? null : perm;
    }
}

/*
 * Log
 *  16   Gandalf   1.15        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  15   Gandalf   1.14        1/12/00  Jesse Glick     NOI18N
 *  14   Gandalf   1.13        1/5/00   Jaroslav Tulach Newly created objects 
 *       are selected in explorer
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  11   Gandalf   1.10        8/3/99   Jan Jancura     
 *  10   Gandalf   1.9         8/2/99   Ian Formanek    Removed comment
 *  9    Gandalf   1.8         7/30/99  David Simonek   serialization fixes
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/25/99  Jaroslav Tulach Children.Keys.setKeys 
 *       now clones the collection, so no later modification matter
 *  6    Gandalf   1.5         5/25/99  Jaroslav Tulach Fix #1889
 *  5    Gandalf   1.4         4/15/99  Jaroslav Tulach Faster 
 *       computeSortingPerm.
 *  4    Gandalf   1.3         3/17/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         1/11/99  Jan Jancura     
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    getMenuPresenter replaced by getPopupPresenter
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach isSon
 */
