/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTNestedClassDeclaration;
import org.acm.seguin.parser.ast.ASTNestedInterfaceDeclaration;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.MethodSummary;

/**
 *  Changes the scope of the method (from private to protected, etc.)
 *
 *@author    Chris Seguin
 */
class ChangeMethodScopeVisitor extends IdentifyMethodVisitor {
	private int changeTo;

	final static int PRIVATE = 1;
	final static int PACKAGE = 2;
	final static int PROTECTED = 3;
	final static int PUBLIC = 4;


	/**
	 *  Constructor for the ChangeMethodScopeVisitor object
	 *
	 *@param  init      Description of Parameter
	 *@param  newScope  Description of Parameter
	 */
	public ChangeMethodScopeVisitor(MethodSummary init, int newScope) {
		super(init);
		changeTo = newScope;
	}


	/**
	 *  Visit a class body
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the method if it is found
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		if (isFound(node)) {
			changeScope(node);
		}
		return null;
	}


	/**
	 *  Skip nested classes
	 *
	 *@param  node  the nested class
	 *@param  data  the data for the visitor
	 *@return       the method if it is found
	 */
	public Object visit(ASTNestedClassDeclaration node, Object data) {
		return null;
	}


	/**
	 *  Skip nested interfaces
	 *
	 *@param  node  the nested interface
	 *@param  data  the data for the visitor
	 *@return       the method if it is found
	 */
	public Object visit(ASTNestedInterfaceDeclaration node, Object data) {
		return null;
	}


	/**
	 *  Changes the scope on a method declaration
	 *
	 *@param  decl  the declaration to change scope on
	 */
	private void changeScope(ASTMethodDeclaration decl) {
		ModifierHolder holder = decl.getModifiers();

		switch (changeTo) {
			case PUBLIC:
				holder.setPrivate(false);
				holder.setProtected(false);
				holder.setPublic(true);
				break;
			case PROTECTED:
				holder.setPrivate(false);
				holder.setProtected(true);
				holder.setPublic(false);
				break;
			case PACKAGE:
				holder.setPrivate(false);
				holder.setProtected(false);
				holder.setPublic(false);
				break;
			case PRIVATE:
				holder.setPrivate(true);
				holder.setProtected(false);
				holder.setPublic(false);
				break;
		}
	}
}
