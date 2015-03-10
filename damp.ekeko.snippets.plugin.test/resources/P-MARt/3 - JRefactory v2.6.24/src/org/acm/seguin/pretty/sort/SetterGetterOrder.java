/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty.sort;

import java.util.StringTokenizer;

import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  Orders the items in a class according to type.
 *
 *@author     Chris Seguin
 *@created    August 3, 1999
 */
public class SetterGetterOrder extends Ordering {
	//  Instance variables
	private int[] order;

	private final static int SETTER = 1;
	private final static int GETTER = 2;
	private final static int OTHER = 3;


	/**
	 *  Constructor for the SetterGetterOrder object <P>
	 *
	 *  The string should either be "instance", "static", or "class". "instance"
	 *  means that instance variables should go first. Either of the other two
	 *  ordering strings indicate that the class variables or methods should go
	 *  first.
	 *
	 *@param  ordering  A user specified string that describes the order.
	 */
	public SetterGetterOrder(String ordering) {
		order = new int[3];
		order[0] = SETTER;
		order[1] = GETTER;
		order[2] = OTHER;

		StringTokenizer tok = new StringTokenizer(ordering, ", \t");

		if (tok.hasMoreTokens()) {
			order[0] = getCode(tok.nextToken());
		}

		if (tok.hasMoreTokens()) {
			order[1] = getCode(tok.nextToken());
		}

		if (tok.hasMoreTokens()) {
			order[2] = getCode(tok.nextToken());
		}
	}


	/**
	 *  Return the index of the item in the order array
	 *
	 *@param  object  the object we are checking
	 *@return         the objects index if it is found or 7 if it is not
	 */
	protected int getIndex(Object object) {
		Object data = ((SimpleNode) object).jjtGetChild(0);

		//  Now that we have data, determine the type of data
		if (data instanceof ASTMethodDeclaration) {
			ASTMethodDeclaration declaration = (ASTMethodDeclaration) data;
			ASTMethodDeclarator declar = (ASTMethodDeclarator) (declaration.jjtGetChild(1));
			String name = declar.getName();
			return findCode(getCode(name));
		}
		else {
			return 100;
		}
	}


	/**
	 *  Gets the Code attribute of the SetterGetterOrder object
	 *
	 *@param  val  Description of Parameter
	 *@return      The Code value
	 */
	private int getCode(String val) {
		String shortValue;

		if (val.length() < 2) {
			return OTHER;
		}

		shortValue = val.substring(0, 2);
		if (shortValue.equals("is")) {
			return GETTER;
		}

		if (val.length() < 3) {
			return OTHER;
		}

		shortValue = val.substring(0, 3);
		if (shortValue.equalsIgnoreCase("set")) {
			return SETTER;
		}
		else if (shortValue.equalsIgnoreCase("get")) {
			return GETTER;
		}

		return OTHER;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  code  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private int findCode(int code) {
		for (int ndx = 0; ndx < 3; ndx++) {
			if (order[ndx] == code) {
				return ndx;
			}
		}

		return 100;
	}
}
