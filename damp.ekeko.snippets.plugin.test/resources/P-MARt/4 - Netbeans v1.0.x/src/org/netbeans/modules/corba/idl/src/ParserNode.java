/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.corba.idl.src;

/* All AST nodes must implement this interface.  It provides basic
   machinery for constructing the parent and child relationships
   between nodes. */

public interface ParserNode {

    /** This method is called after the node has been made the current
      node.  It indicates that child nodes can now be added to it. */
    public void jjtOpen();

    /** This method is called after all the child nodes have been
      added. */
    public void jjtClose();

    /** This pair of methods are used to inform the node of its
      parent. */
    public void jjtSetParent(Node n);
    public Node jjtGetParent();

    /** This method tells the node to add its argument to the node's
      list of children.  */
    public void jjtAddChild(Node n, int i);

    /** This method returns a child node.  The children are numbered
       from zero, left to right. */
    public Node jjtGetChild(int i);

    /** Return the number of children the node has. */
    public int jjtGetNumChildren();
}
