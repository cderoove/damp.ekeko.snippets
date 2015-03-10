/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Checks to see if one type is an ancestor of another one
 *
 *@author    Chris Seguin
 */
public class Ancestor {
	/**
	 *  Checks to see if one type is an ancestor of another one
	 *
	 *@param  node      the type summary in question
	 *@param  ancestor  the ancestor type summary
	 *@return           true if node is child (or grandchild or great grandchild
	 *      or ...) of ancestor
	 */
	public static boolean query(TypeSummary node, TypeSummary ancestor)
	{
		TypeSummary current = node;
		if ((ancestor == null) || (current == null)) {
			return false;
		}

		if (ancestor.getName().equals("Object")) {
			return true;
		}

		do {
			TypeDeclSummary decl = current.getParentClass();
			current = GetTypeSummary.query(decl);

			if (current == ancestor) {
				return true;
			}
		} while (current != null);

		return false;
	}
}
