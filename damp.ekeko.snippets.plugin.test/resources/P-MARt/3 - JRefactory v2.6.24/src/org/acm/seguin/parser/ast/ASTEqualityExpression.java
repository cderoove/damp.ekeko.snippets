/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.ast;

import java.util.Enumeration;
import java.util.Vector;

import org.acm.seguin.parser.JavaParser;
import org.acm.seguin.parser.JavaParserVisitor;

/**
 *  Expression that contains == or !=.
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTEqualityExpression extends SimpleNode {
	//  Instance Variables
	Vector names;


	/**
	 *  Constructor for the ASTEqualityExpression object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTEqualityExpression(int id) {
		super(id);
		names = new Vector();
	}


	/**
	 *  Constructor for the ASTEqualityExpression object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTEqualityExpression(JavaParser p, int id) {
		super(p, id);
		names = new Vector();
	}


	/**
	 *  Get the object's names
	 *
	 *@return    the names in an enumeration
	 */
	public Enumeration getNames() {
		return names.elements();
	}


	/**
	 *  Set the object's name
	 *
	 *@param  newName  the new name
	 */
	public void addName(String newName) {
		if (newName != null) {
			names.addElement(newName.intern());
		}
	}


	/**
	 *  Convert this object to a string
	 *
	 *@return    a string representing this object
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append(" [");
		Enumeration enum_ = getNames();
		while (enum_.hasMoreElements()) {
			buffer.append(enum_.nextElement().toString());
			if (enum_.hasMoreElements()) {
				buffer.append(", ");
			}
		}
		buffer.append("]");
		return buffer.toString();
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
