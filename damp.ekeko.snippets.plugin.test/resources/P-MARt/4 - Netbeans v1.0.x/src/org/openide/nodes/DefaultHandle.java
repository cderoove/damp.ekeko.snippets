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

import org.openide.util.Mutex;

/** Simple implementation of <code>Node.Handle</code>. When created by
* {@link #createHandle} it
* looks for the root of the node and stores a path to it.
* When {@link #getNode} is then called, it tries to restore the
* root and then to walk along the path.
*
* @author Jaroslav Tulach
*/
public final class DefaultHandle extends Object implements Node.Handle {
    /** root handle */
    private Node.Handle root;
    /** path to the node */
    private String[] path;

    static final long serialVersionUID =-8739777664305986773L;
    /** Create a new handle.
    * @param root handle for the root node
    * @param path path to this node from the root, in components
    */
    DefaultHandle (Node.Handle root, String[] path) {
        this.path = path;
    }

    /** Find the node.
    * @return the found node
    * @exception IOException if the root cannot be created
    * @exception NodeNotFoundException if the path is not valid (exception may be examined for details)
    */
    public Node getNode () throws java.io.IOException {
        return NodeOp.findPath (root.getNode (), path);
    }

    /** Create a handle for a given node.
    * @param node the node to create a handler for
    * @return the handler, or <code>null</code> if the root of the node has no handle
    */
    public static DefaultHandle createHandle (final Node node) {
        // read access to ensure that the computed root and path
        // will be the same
        return (DefaultHandle)Children.MUTEX.readAccess (new Mutex.Action () {
                    public Object run () {
                        Node r = NodeOp.findRoot (node);
                        if (r == node) return null;
                        Node.Handle root = r.getHandle ();
                        // if the root has handle we can create our own
                        return root == null ? null : new DefaultHandle (
                                   root, NodeOp.createPath (node, null)
                               );
                    }
                });
    }

}

/*
* Log
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         3/18/99  Jaroslav Tulach Compiles under jikes.
*  3    Gandalf   1.2         3/17/99  Jesse Glick     Constructor ought not to 
*       have been public.
*  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
