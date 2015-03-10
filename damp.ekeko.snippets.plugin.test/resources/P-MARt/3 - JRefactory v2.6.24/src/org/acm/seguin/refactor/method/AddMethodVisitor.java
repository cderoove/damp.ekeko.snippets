/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTInterfaceBody;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  Adds a method to the tree
 *
 *@author    Chris Seguin
 */
public class AddMethodVisitor extends ChildrenVisitor {
	private SimpleNode method;


	/**
	 *  Constructor for the AddMethodVisitor object
	 *
	 *@param  init  Description of Parameter
	 */
	public AddMethodVisitor(SimpleNode init) {
		method = init;
	}


	/**
	 *  Visit a class body
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       always returns null
	 */
	public Object visit(ASTClassBody node, Object data) {
		node.jjtInsertChild(method, node.jjtGetNumChildren());
		return null;
	}


	/**
	 *  Visit an interface body
	 *
	 *@param  node  the interface body node
	 *@param  data  data for the visitor
	 *@return       always returns null
	 */
	public Object visit(ASTInterfaceBody node, Object data) {
		node.jjtInsertChild(method, node.jjtGetNumChildren());
		return null;
	}
}
