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
import org.acm.seguin.pretty.JavaDocComponent;
import org.acm.seguin.pretty.JavaDocable;
import org.acm.seguin.pretty.JavaDocableImpl;
import org.acm.seguin.pretty.PrintData;

/**
 *  Holds the package declaration at the beginning of the java file
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTPackageDeclaration extends SimpleNode implements JavaDocable {
	//  Instance Variables
	JavaDocableImpl jdi;


	/**
	 *  Constructor for the ASTPackageDeclaration object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTPackageDeclaration(int id) {
		super(id);
		jdi = new JavaDocableImpl();
	}


	/**
	 *  Constructor for the ASTPackageDeclaration object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTPackageDeclaration(JavaParser p, int id) {
		super(p, id);
		jdi = new JavaDocableImpl();
	}


	/**
	 *  Checks to see if it was printed
	 *
	 *@return    true if it still needs to be printed
	 */
	public boolean isRequired() {
		return false;
	}


	/**
	 *  Allows you to add a java doc component
	 *
	 *@param  component  the component that can be added
	 */
	public void addJavaDocComponent(JavaDocComponent component) {
		jdi.addJavaDocComponent(component);
	}


	/**
	 *  Prints all the java doc components
	 *
	 *@param  printData  the print data
	 */
	public void printJavaDocComponents(PrintData printData) {
		jdi.printJavaDocComponents(printData, "since");
	}


	/**
	 *  Makes sure all the java doc components are present
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
