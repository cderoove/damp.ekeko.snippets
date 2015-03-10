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
 *  Contains void, a primitive type, or a named type and can
 *  be used as a return value from a method.
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTResultType extends SimpleNode {
	/**
	 *  Constructor for the ASTResultType object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTResultType(int id) {
		super(id);
	}


	/**
	 *  Constructor for the ASTResultType object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTResultType(JavaParser p, int id) {
		super(p, id);
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
