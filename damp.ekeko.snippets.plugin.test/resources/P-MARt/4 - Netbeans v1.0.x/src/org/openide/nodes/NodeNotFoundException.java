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

import java.io.IOException;

/** Exception indicating that a node could not be found while
* traversing a path from the root.
*
* @author Jaroslav Tulach
*/
public final class NodeNotFoundException extends IOException {
    static final long serialVersionUID =1493446763320691906L;

    /** closest node */
    private Node node;
    /** name of child not found */
    private String name;
    /** depth of not founded node. */
    private int depth;

    /** Constructor.
    * @param node closest found node to the one being looked for
    * @param name name of child not found in that node
    * @param depth depth of the node that was found
    */
    NodeNotFoundException (Node node, String name, int depth) {
        this.node = node;
        this.name = name;
    }

    /** Get the closest node to the target that was able to be found.
     * @return the closest node
    */
    public Node getClosestNode () {
        return node;
    }

    /** Get the name of the missing child of the closest node.
     * @return the name of the missing child
    */
    public String getMissingChildName () {
        return name;
    }

    /** Getter for the depth of the closest node found.
    * @return the depth (0 for the start node, 1 for its child, etc.)
    */
    public int getClosestNodeDepth () {
        return depth;
    }
}

/*
* Log
*  6    Gandalf   1.5         1/5/00   Jaroslav Tulach Newly created objects are
*       selected in explorer
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
