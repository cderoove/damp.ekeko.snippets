/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.MethodSummary;

/**
 *  Changes the scope of the method
 *
 *@author    Chris Seguin
 */
class ChangeMethodScopeTransform extends TransformAST {
	private int scope;
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the ChangeMethodScopeTransform object
	 *
	 *@param  init      Description of Parameter
	 *@param  changeTo  Description of Parameter
	 */
	ChangeMethodScopeTransform(MethodSummary init, int changeTo) {
		methodSummary = init;
		scope = changeTo;
	}


	/**
	 *  Updates the AST
	 *
	 *@param  root  the root of the AST
	 */
	public void update(SimpleNode root) {
		root.jjtAccept(new ChangeMethodScopeVisitor(methodSummary, scope), null);
	}
}
