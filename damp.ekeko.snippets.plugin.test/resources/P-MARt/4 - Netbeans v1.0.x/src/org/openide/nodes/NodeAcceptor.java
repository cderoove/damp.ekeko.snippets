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

/** Discriminator accepting only certain sets of nodes.
* <P>
* Currently used in {@link org.openide.TopManager.NodeOperation#explore}
* to find out if the currently selected beans are valid or not.
*
* @author Jaroslav Tulach
* @version 0.10, Jan 26, 1998
*/
public interface NodeAcceptor {
    /** Is the set of nodes acceptable?
    * @param nodes the nodes to consider
    * @return <CODE>true</CODE> if so
    */
    public boolean acceptNodes (Node[] nodes);
}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/18/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/17/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
