/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.pretty.sort;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;

/**
 *  Sorts the items in a java file at the top level
 *
 *@author    Chris Seguin
 */
public class TopLevelOrdering extends Ordering {
	/**
	 *  Compare two items
	 *
	 *@param  one  the first item
	 *@param  two  the second item
	 *@return      1 if the first item is greater than the second, -1 if the
	 *      first item is less than the second, and 0 otherwise.
	 */
	public int compare(Object one, Object two) {
		int oneIndex = getIndex(one);
		int twoIndex = getIndex(two);

		if (oneIndex > twoIndex) {
			return 1;
		}
		else if (oneIndex < twoIndex) {
			return -1;
		}
		else {
			return fineCompare(one, two);
		}
	}


	/**
	 *  Return the index of the item in the order array
	 *
	 *@param  object  the object we are checking
	 *@return         the objects index if it is found or 7 if it is not
	 */
	protected int getIndex(Object object) {
		if (object instanceof ASTPackageDeclaration) {
			return 1;
		}
		else if (object instanceof ASTImportDeclaration) {
			return 2;
		}
		else if (object instanceof ASTTypeDeclaration) {
			ASTTypeDeclaration type = (ASTTypeDeclaration) object;
			Node child = type.jjtGetChild(0);
			if (child instanceof ASTClassDeclaration) {
				ASTClassDeclaration classDecl = (ASTClassDeclaration) child;
				if (classDecl.isPublic()) {
					return 3;
				}
				else {
					return 4;
				}
			}
			else {
				ASTInterfaceDeclaration interfaceDecl = (ASTInterfaceDeclaration) child;
				if (interfaceDecl.isPublic()) {
					return 3;
				}
				else {
					return 4;
				}
			}
		}

		return 5;
	}


	/**
	 *  Fine grain comparison based on knowing what the types are
	 *
	 *@param  obj1  the object
	 *@param  obj2  the second object
	 *@return       -1 if obj1 is less than obj2, 0 if they are the same, and +1 if
	 *  obj1 is greater than obj2
	 */
	private int fineCompare(Object obj1, Object obj2) {
		if (obj1 instanceof ASTImportDeclaration) {
			return compareImports((ASTImportDeclaration) obj1, (ASTImportDeclaration) obj2);
		}

		return 0;
	}


	/**
	 *  Compares two import statements
	 *
	 *@param  import1  the first statement
	 *@param  import2  the second statement
	 *@return          -1 if import1 is less than import2, 0 if they are the same, and +1 if
	 *  import1 is greater than import2
	 */
	private int compareImports(ASTImportDeclaration import1, ASTImportDeclaration import2) {
		ASTName firstName = (ASTName) import1.jjtGetChild(0);
		ASTName secondName = (ASTName) import2.jjtGetChild(0);

		return firstName.getName().compareTo(secondName.getName());
	}
}
