package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTInterfaceBody;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  Adds a field to the tree 
 *
 *@author    Chris Seguin 
 */
public class AddFieldVisitor extends ChildrenVisitor {
	private SimpleNode field;


	/**
	 *  Constructor for the AddFieldVisitor object 
	 *
	 *@param  init  Description of Parameter 
	 */
	public AddFieldVisitor(SimpleNode init) {
		field = init;
	}


	/**
	 *  Visit a class body 
	 *
	 *@param  node  the class body node 
	 *@param  data  the data for the visitor 
	 *@return       always returns null 
	 */
	public Object visit(ASTClassBody node, Object data) {
		node.jjtInsertChild(field, node.jjtGetNumChildren());
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
		node.jjtInsertChild(field, node.jjtGetNumChildren());
		return null;
	}
}
