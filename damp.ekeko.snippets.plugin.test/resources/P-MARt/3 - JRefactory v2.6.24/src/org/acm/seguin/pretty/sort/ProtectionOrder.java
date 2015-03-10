/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty.sort;

import org.acm.seguin.parser.ast.ASTClassBodyDeclaration;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTInterfaceMemberDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTNestedClassDeclaration;
import org.acm.seguin.parser.ast.ASTNestedInterfaceDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.ModifierHolder;


/**
 *  Orders the items in a class according to type.
 *
 *@author     Chris Seguin
 *@created    August 3, 1999
 */
public class ProtectionOrder extends Ordering {
	//  Instance variables
	private boolean publicFirst;

	private final static int PUBLIC = 1;
	private final static int PROTECTED = 2;
	private final static int PACKAGE = 3;
	private final static int PRIVATE = 4;



	/**
	 *  Constructor for the StaticOrder object <P>
	 *
	 *  The string should either be "instance", "static", or "class". "instance"
	 *  means that instance variables should go first. Either of the other two
	 *  ordering strings indicate that the class variables or methods should go
	 *  first.
	 *
	 *@param  ordering  A user specified string that describes the order.
	 */
	public ProtectionOrder(String ordering) {
		publicFirst = ordering.equalsIgnoreCase("public");
	}


	/**
	 *  Return the index of the item in the order array
	 *
	 *@param  object  the object we are checking
	 *@return         the objects index if it is found or 7 if it is not
	 */
	protected int getIndex(Object object) {
		Object data = ((SimpleNode) object).jjtGetChild(0);
		if (data instanceof ASTClassBodyDeclaration) {
			data = ((ASTClassBodyDeclaration) data).jjtGetChild(0);
		}
		else if (data instanceof ASTInterfaceMemberDeclaration) {
			data = ((ASTInterfaceMemberDeclaration) data).jjtGetChild(0);
		}

		int protection = 0;

		//  Now that we have data, determine the type of data
		if (data instanceof ASTFieldDeclaration) {
			protection = getProtection(((ASTFieldDeclaration) data).getModifiers());
		}
		else if (data instanceof ASTConstructorDeclaration) {
			protection = getProtection(((ASTConstructorDeclaration) data).getModifiers());
		}
		else if (data instanceof ASTMethodDeclaration) {
			protection = getProtection(((ASTMethodDeclaration) data).getModifiers());
		}
		else if (data instanceof ASTNestedInterfaceDeclaration) {
			protection = getProtection(((ASTNestedInterfaceDeclaration) data).getModifiers());
		}
		else if (data instanceof ASTNestedClassDeclaration) {
			protection = getProtection(((ASTNestedClassDeclaration) data).getModifiers());
		}
		else {
			return 100;
		}

		if (publicFirst) {
			return protection;
		}
		else {
			return -protection;
		}
	}


	/**
	 *  Gets the Protection attribute of the ProtectionOrder object
	 *
	 *@param  mods  Description of Parameter
	 *@return       The Protection value
	 */
	private int getProtection(ModifierHolder mods) {
		if (mods.isPrivate()) {
			return PRIVATE;
		}
		else if (mods.isProtected()) {
			return PROTECTED;
		}
		else if (mods.isPublic()) {
			return PUBLIC;
		}
		else {
			return PACKAGE;
		}
	}
}
