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
import org.acm.seguin.summary.MethodSummary;

/**
 *  A transform that removes a specific method
 *
 *@author    Chris Seguin
 */
public class RemoveMethodTransform extends TransformAST {
	private SimpleNode methodDecl;
	private MethodSummary method;


	/**
	 *  Constructor for the RemoveMethodTransform object
	 *
	 *@param  init  the summary of the method
	 */
	public RemoveMethodTransform(MethodSummary init) {
		method = init;
	}


	/**
	 *  Gets the MethodDeclaration attribute of the RemoveMethodTransform object
	 *
	 *@return    The MethodDeclaration value
	 */
	public SimpleNode getMethodDeclaration() {
		return methodDecl;
	}


	/**
	 *  Updates the root
	 *
	 *@param  root  the ro
	 */
	public void update(SimpleNode root) {
		RemoveMethodVisitor rfv = new RemoveMethodVisitor(method);
		rfv.visit(root, null);
		methodDecl = rfv.getMethodDeclaration();
	}
}
