/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

public class ContainsStatic {
	public static boolean query(TypeSummary type, String name, boolean isMethod) {
		if (type == null) {
			return true;
		}

		if (isMethod) {
			return searchMethods(type, name);
		}
		else {
			return searchFields(type, name);
		}
	}

	private static boolean searchMethods(TypeSummary type, String name) {
		Iterator iter = type.getMethods();
		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			if (next.getName().equals(name)) {
				return true;
			}
		}

		TypeDeclSummary parentDecl = type.getParentClass();
		TypeSummary parent = GetTypeSummary.query(parentDecl);

		if (parent != null) {
			return searchMethods(parent, name);
		}

		return false;
	}

	private static boolean searchFields(TypeSummary type, String name) {
		Iterator iter = type.getFields();
		while (iter.hasNext()) {
			FieldSummary next = (FieldSummary) iter.next();
			if (next.getName().equals(name)) {
				return true;
			}
		}

		TypeDeclSummary parentDecl = type.getParentClass();
		TypeSummary parent = GetTypeSummary.query(parentDecl);

		if (parent != null) {
			return searchFields(parent, name);
		}

		return false;
	}
}
