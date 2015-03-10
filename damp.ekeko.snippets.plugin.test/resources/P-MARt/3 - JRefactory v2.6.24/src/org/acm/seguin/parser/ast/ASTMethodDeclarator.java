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
 *  Constains the name and the parameters associated with this method
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTMethodDeclarator extends SimpleNode {
	//  Instance Variables
	private String name;
	private int arrayCount;


	/**
	 *  Constructor for the ASTMethodDeclarator object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTMethodDeclarator(int id) {
		super(id);
		arrayCount = 0;
	}


	/**
	 *  Constructor for the ASTMethodDeclarator object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTMethodDeclarator(JavaParser p, int id) {
		super(p, id);
		arrayCount = 0;
	}


	/**
	 *  Set the object's name
	 *
	 *@param  newName  the new name
	 */
	public void setName(String newName) {
		name = newName.intern();
	}


	/**
	 *  Set the number of levels of array indirection
	 *
	 *@param  count  the number of []
	 */
	public void setArrayCount(int count) {
		arrayCount = count;
	}


	/**
	 *  Get the object's name
	 *
	 *@return    the name
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Return the number of array brackets
	 *
	 *@return    the levels of indirection
	 */
	public int getArrayCount() {
		return arrayCount;
	}


	/**
	 *  Convert this object to a string
	 *
	 *@return    a string representing this object
	 */
	public String toString() {
		//  Create the string
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append(" <");
		buf.append(getName());
		for (int ndx = 0; ndx < arrayCount; ndx++) {
			buf.append("[]");
		}
		buf.append(">");

		//  Return it
		return buf.toString();
	}


	/**
	 *  Makes sure all the java doc components are present. For methods and
	 *  constructors we need to do more work - checking parameters, return types,
	 *  and exceptions.
	 */
	public void finish() {
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
