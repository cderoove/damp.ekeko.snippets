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
 *  Stores an import declaration that appears at the beginning of
 *  a java file.
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTImportDeclaration extends SimpleNode {
	//  Instance Variables
	private boolean importPackage;


	/**
	 *  Constructor for the ASTImportDeclaration object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTImportDeclaration(int id) {
		super(id);
	}


	/**
	 *  Constructor for the ASTImportDeclaration object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTImportDeclaration(JavaParser p, int id) {
		super(p, id);
	}


	/**
	 *  Set when including everything in a package
	 *
	 *@param  way  whether we are importing the package
	 */
	public void setImportPackage(boolean way) {
		importPackage = way;
	}


	/**
	 *  Return whether we are importing a package
	 *
	 *@return    true if we are importing a package
	 */
	public boolean isImportingPackage() {
		return importPackage;
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
