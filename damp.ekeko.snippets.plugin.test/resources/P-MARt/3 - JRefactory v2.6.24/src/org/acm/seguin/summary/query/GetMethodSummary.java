/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Looks up a method in a type
 *
 *@author     Chris Seguin
 *@created    April 5, 2000
 */
public class GetMethodSummary {
	/**
	 *  Looks up the method given a type and a name
	 *
	 *@param  type  the type
	 *@param  name  the name
	 *@return       the method summary
	 */
	public static MethodSummary query(TypeSummary type, String name)
	{
		Iterator iter = type.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				if (next.getName().equals(name)) {
					return next;
				}
			}
		}
		return null;
	}


	/**
	 *  Looks up the method given a type and a name
	 *
	 *@param  type  the type
	 *@param  node  Description of Parameter
	 *@return       the method summary
	 */
	public static MethodSummary query(TypeSummary type, ASTMethodDeclaration node)
	{
		Iterator iter = type.getMethods();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			if (isMatch(next, node)) {
				return next;
			}
		}

		return null;
	}


	/**
	 *  Looks up the method given a type and a name
	 *
	 *@param  type  the type
	 *@param  node  Description of Parameter
	 *@return       the method summary
	 */
	public static MethodSummary query(TypeSummary type, ASTConstructorDeclaration node)
	{
		Iterator iter = type.getMethods();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			if (next.isConstructor() &&
				isParameterMatch((ASTFormalParameters) node.jjtGetChild(0), next)) {
				return next;
			}
		}

		return null;
	}


	/**
	 *  Gets the Match attribute of the GetMethodSummary class
	 *
	 *@param  current  the current method
	 *@param  decl     the declaration
	 *@return          true if we have found the method summary
	 */
	private static boolean isMatch(MethodSummary current, ASTMethodDeclaration decl)
	{
		ASTMethodDeclarator declarator = (ASTMethodDeclarator) decl.jjtGetChild(1);
		if (!current.getName().equals(declarator.getName())) {
			return false;
		}

		ASTFormalParameters params = (ASTFormalParameters) declarator.jjtGetChild(0);
		return isParameterMatch(params, current);
	}


	/**
	 *  Gets the ParameterMatch attribute of the GetMethodSummary class
	 *
	 *@param  params   Description of Parameter
	 *@param  current  Description of Parameter
	 */
	private static boolean isParameterMatch(ASTFormalParameters params, MethodSummary current)
	{
		int childrenCount = params.jjtGetNumChildren();
		if (childrenCount != current.getParameterCount()) {
			return false;
		}

		if (childrenCount == 0) {
			return true;
		}

		Iterator iter = current.getParameters();
		for (int ndx = 0; ndx < childrenCount; ndx++) {
			ParameterSummary next = (ParameterSummary) iter.next();
			ASTFormalParameter nextParam = (ASTFormalParameter) params.jjtGetChild(ndx);
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) nextParam.jjtGetChild(1);
			if (!next.getName().equals(id.getName())) {
				return false;
			}
		}

		return true;
	}
}
