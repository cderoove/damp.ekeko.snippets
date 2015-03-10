package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;

/**
 *  Adds a field declaration to a AST 
 *
 *@author    Chris Seguin 
 */
public class AddFieldTransform extends TransformAST {
	private SimpleNode fieldDecl;


	/**
	 *  Constructor for the AddFieldTransform object 
	 *
	 *@param  init  the field declaration to add 
	 */
	public AddFieldTransform(SimpleNode init) {
		fieldDecl = init;
	}


	/**
	 *  Updates the AST 
	 *
	 *@param  root  the root of the AST 
	 */
	public void update(SimpleNode root) {
		//  Apply each individual transformation
		AddFieldVisitor afv = new AddFieldVisitor(fieldDecl);
		afv.visit(root, null);
	}
}
