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

import java.util.Arrays;
import java.util.HashSet;

/** Event describing change in the list of a node's children.
*
* @author Jaroslav Tulach
*/
public class NodeMemberEvent extends NodeEvent {
    /** is this add event? */
    private boolean add;
    /** list of changed nodes */
    private Node[] delta;
    /** list of nodes to find indices in, null if one should use current
    * node's list
    */
    private Node[] from;
    /** list of nodes indexes, can be null if it should be computed lazily */
    private int[] indices;

    static final long serialVersionUID =-3973509253579305102L;
    /** Package private constructor to allow construction only
    * @param n node that should fire change
    * @param add true if nodes has been added
    * @param delta array of nodes that have changed
    * @param from nodes to find indices in
    */
    NodeMemberEvent(Node n, boolean add, Node[] delta, Node[] from) {
        super (n);
        this.add = add;
        this.delta = delta;
        this.from = from;
    }

    /** Get the type of action.
    * @return <CODE>true</CODE> if children were added,
    *    <CODE>false</CODE> if removed
    */
    public final boolean isAddEvent () {
        return add;
    }

    /** Get a list of children that changed.
    * @return array of nodes that changed
    */
    public final Node[] getDelta () {
        return delta;
    }

    /** Get an array of indices of the changed nodes.
    * @return array with the same length as {@link #getDelta}
    */
    public synchronized int[] getDeltaIndices () {
        if (indices != null) return indices;

        // compute indices
        if (from == null) {
            // use current node subnodes
            from = getNode ().getChildren ().getNodes ();
        }

        java.util.List list = Arrays.asList (delta);
        HashSet set = new HashSet (list);

        indices = new int[delta.length];

        int j = 0;
        for (int i = 0; i < from.length; i++) {
            if (set.contains (from[i])) {
                indices[j++] = i;
            }
        }

        if (j != delta.length) {
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                System.out.println("This: " + this); // NOI18N
                System.err.println("Current state:\n");
                System.err.println(Arrays.asList (from));
                System.err.println("Delta:\n");
                System.err.println(list);
            }
            throw new IllegalStateException ("Some of deleted nodes are not present in original ones"); // NOI18N
        }

        return indices;
    }

    /** Human presentable information about the event */
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        sb.append (getClass ().getName ());
        sb.append ("[node="); // NOI18N
        sb.append (getSource ());
        sb.append (", add="); // NOI18N
        sb.append (isAddEvent ());

        Node[] deltaNodes = getDelta ();
        int[] deltaIndices = getDeltaIndices ();

        for (int i = 0; i < deltaNodes.length; i++) {
            sb.append ("\n  "); // NOI18N
            sb.append (i);
            sb.append (" at "); // NOI18N
            sb.append (deltaIndices[i]);
            sb.append (" = "); // NOI18N
            sb.append (deltaNodes[i]);
        }
        sb.append ("\n]"); // NOI18N

        return sb.toString ();
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Jesse Glick     NOI18N
 *  9    Gandalf   1.8         1/12/00  Jesse Glick     NOI18N
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  6    Gandalf   1.5         8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/6/99   Jaroslav Tulach setKeys allows nodes to 
 *       be deleted.
 *  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
