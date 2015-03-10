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

/** Event describing a change in a node.
*
*/
public class NodeEvent extends java.util.EventObject {
    static final long serialVersionUID =3504069382061188226L;
    /** Create a new event.
    * @param n origin node
    */
    public NodeEvent(Node n) {
        super (n);
    }

    /** Get the node where the change occurred.
    * @return the node
    */
    public final Node getNode () {
        return (Node)getSource ();
    }
}

/*
 * Log
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
