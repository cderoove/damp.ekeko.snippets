package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;

/**
 *  A transform that removes a specific field 
 *
 *@author    Chris Seguin 
 */
public class RemoveFieldTransform extends TransformAST {
	private SimpleNode fieldDecl;
	private String field;


	/**
	 *  Constructor for the RemoveFieldTransform object 
	 *
	 *@param  init  the name of the field 
	 */
	public RemoveFieldTransform(String init) {
		field = init;
	}


	/**
	 *  Gets the FieldDeclaration attribute of the RemoveFieldTransform object 
	 *
	 *@return    The FieldDeclaration value 
	 */
	public SimpleNode getFieldDeclaration() {
		return fieldDecl;
	}


	/**
	 *  Updates the root 
	 *
	 *@param  root  the ro 
	 */
	public void update(SimpleNode root) {
		RemoveFieldVisitor rfv = new RemoveFieldVisitor(field);
		rfv.visit(root, null);
		fieldDecl = rfv.getFieldDeclaration();
	}
}
