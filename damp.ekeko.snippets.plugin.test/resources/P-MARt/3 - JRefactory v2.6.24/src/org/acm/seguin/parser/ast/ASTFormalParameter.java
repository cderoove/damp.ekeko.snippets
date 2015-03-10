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
 *  Stores an individual formal parameter in a method declaration
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTFormalParameter extends SimpleNode {
	//  Instance Variable
	private boolean usingFinal;


	/**
	 *  Constructor for the ASTFormalParameter object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTFormalParameter(int id) {
		super(id);
	}


	/**
	 *  Constructor for the ASTFormalParameter object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTFormalParameter(JavaParser p, int id) {
		super(p, id);
	}


	/**
	 *  Mark that this formal parameter using the final modifier
	 *
	 *@param  way  the way to set the final modifier
	 */
	public void setUsingFinal(boolean way) {
		usingFinal = way;
	}


	/**
	 *  Return whether this formal parameter using the final modifier
	 *
	 *@return    if the formal modifier is used
	 */
	public boolean isUsingFinal() {
		return usingFinal;
	}


	/**
	 *  Converts this object to a string
	 *
	 *@return    a string representing this object
	 */
	public String toString() {
		return super.toString() + "[Using final:  " + usingFinal + "]";
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
