/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;

/**
 *  A visitor that is able to identify the method that we are operating on
 *
 *@author    Chris Seguin
 */
abstract class IdentifyMethodVisitor extends ChildrenVisitor {
	/**
	 *  The method we are searching for
	 */
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the IdentifyMethodVisitor object
	 *
	 *@param  init  Description of Parameter
	 */
	public IdentifyMethodVisitor(MethodSummary init) {
		methodSummary = init;
	}


	/**
	 *  Have we found the method declaration that we are going to move?
	 *
	 *@param  next  Description of Parameter
	 *@return       The Found value
	 */
	protected boolean isFound(SimpleNode next) {
		if (next instanceof ASTMethodDeclaration) {
		return checkDeclaration((ASTMethodDeclarator) next.jjtGetChild(1));
		}
		if (next instanceof ASTConstructorDeclaration) {
		return checkDeclaration((ASTConstructorDeclaration) next);
		}

			return false;
	}


	/**
	 *  Checks a single variable declaration to see if it is the one we are
	 *  looking for
	 *
	 *@param  next  the method declaration that we are checking
	 *@return       true if we have found the method
	 */
	protected boolean checkDeclaration(ASTMethodDeclarator decl) {
		if (decl.getName().equals(methodSummary.getName())) {
			return checkParameters((ASTFormalParameters) decl.jjtGetChild(0));
		}

		return false;
	}


	/**
	 *  Checks a single variable declaration to see if it is the one we are
	 *  looking for
	 *
	 *@param  next  the method declaration that we are checking
	 *@return       true if we have found the method
	 */
	protected boolean checkDeclaration(ASTConstructorDeclaration decl) {
		if (methodSummary.isConstructor()) {
			return checkParameters((ASTFormalParameters) decl.jjtGetChild(0));
		}

		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  params  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	protected boolean checkParameters(ASTFormalParameters params) {
		int length = params.jjtGetNumChildren();
		Iterator iter = methodSummary.getParameters();

		if (iter == null) {
			return (length == 0);
		}

		int ndx;
		for (ndx = 0; (iter.hasNext()) && (ndx < length); ndx++) {
			ASTFormalParameter param = (ASTFormalParameter) params.jjtGetChild(ndx);
			ASTType type = (ASTType) param.jjtGetChild(0);
			String name;

			if (type.jjtGetChild(0) instanceof ASTPrimitiveType) {
				name = ((ASTPrimitiveType) type.jjtGetChild(0)).getName();
			}
			else {
				name = ((ASTName) type.jjtGetChild(0)).getName();
			}

			ParameterSummary paramSummary = (ParameterSummary) iter.next();
			String typeName = paramSummary.getTypeDecl().getLongName();

			if (!name.equals(typeName)) {
				return false;
			}
		}

		return (!iter.hasNext()) && (ndx == length);
	}
}
