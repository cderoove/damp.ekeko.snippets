/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;

/**
 *  Adds a method declaration to a AST
 *
 *@author    Chris Seguin
 */
public class AddMethodTransform extends TransformAST {
	private SimpleNode methodDecl;


	/**
	 *  Constructor for the AddMethodTransform object
	 *
	 *@param  init  the method declaration to add
	 */
	public AddMethodTransform(SimpleNode init) {
		methodDecl = init;
	}


	/**
	 *  Updates the AST
	 *
	 *@param  root  the root of the AST
	 */
	public void update(SimpleNode root) {
		//  Apply each individual transformation
		AddMethodVisitor afv = new AddMethodVisitor(methodDecl);
		afv.visit(root, null);
	}
}
