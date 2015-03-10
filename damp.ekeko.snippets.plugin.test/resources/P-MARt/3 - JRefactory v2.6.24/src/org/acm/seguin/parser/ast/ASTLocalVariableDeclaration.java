/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.ast;

import org.acm.seguin.parser.JavaParser;
import org.acm.seguin.parser.JavaParserVisitor;

/**
 *  Declares a local variable in a method
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTLocalVariableDeclaration extends SimpleNode {
	//  Instance Variables
	private boolean usingFinal;


	/**
	 *  Constructor for the ASTLocalVariableDeclaration object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTLocalVariableDeclaration(int id) {
		super(id);
	}


	/**
	 *  Constructor for the ASTLocalVariableDeclaration object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTLocalVariableDeclaration(JavaParser p, int id) {
		super(p, id);
	}


	/**
	 *  Sets whether we are using the final modifier
	 *
	 *@param  way  the way we are setting
	 */
	public void setUsingFinal(boolean way) {
		usingFinal = way;
	}


	/**
	 *  Return true if we used the final modifier
	 *
	 *@return    true if we used final
	 */
	public boolean isUsingFinal() {
		return usingFinal;
	}


	/**
	 *  Converts this to a string
	 *
	 *@return    the string
	 */
	public String toString() {
		return super.toString() + " [Using final:  " + usingFinal + "]";
	}


	/**
	 *  Accept the visitor.
	 *
	 *@param  visitor  Description of Parameter
	 *@param  data     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
