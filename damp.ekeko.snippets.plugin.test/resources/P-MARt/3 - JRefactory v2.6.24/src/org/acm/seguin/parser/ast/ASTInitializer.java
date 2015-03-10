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
 *  Stores a static or dynamic initializer that is contained
 *  within the class.
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTInitializer extends SimpleNode {
	//  Instance Variables
	private boolean usingStatic;


	/**
	 *  Constructor for the ASTInitializer object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTInitializer(int id) {
		super(id);
	}


	/**
	 *  Constructor for the ASTInitializer object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTInitializer(JavaParser p, int id) {
		super(p, id);
	}


	/**
	 *  Sets whether we are using the static modifier
	 *
	 *@param  way  the way we are setting
	 */
	public void setUsingStatic(boolean way) {
		usingStatic = way;
	}


	/**
	 *  Return true if we used the static modifier
	 *
	 *@return    true if we used static
	 */
	public boolean isUsingStatic() {
		return usingStatic;
	}


	/**
	 *  Converts this to a string
	 *
	 *@return    the string
	 */
	public String toString() {
		return super.toString() + " [Using static:  " + usingStatic + "]";
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
