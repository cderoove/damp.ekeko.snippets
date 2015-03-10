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

import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Finds a field associated with a particular type summary. A permission is
 *  also specified to insure that we find something interesting.
 *
 *@author    Chris Seguin
 */
public class FieldQuery {
	/**
	 *  The field we are looking for can have any protection level
	 */
	public final static int PRIVATE = 1;
	/**
	 *  The field we are looking for must have default protection level or higher
	 */
	public final static int DEFAULT = 2;
	/**
	 *  The field we are looking for must have protected protection level or
	 *  higher
	 */
	public final static int PROTECTED = 3;
	/**
	 *  The field we are looking for must be public
	 */
	public final static int PUBLIC = 4;


	/**
	 *  Finds an associated field
	 *
	 *@param  typeSummary  the type
	 *@param  name         the name
	 *@return              the field found or null if none
	 */
	public static FieldSummary find(TypeSummary typeSummary, String name) {
		FieldSummary result = query(typeSummary, name, PRIVATE);
		if (result == null) {
			result = queryAncestors(typeSummary, name, PROTECTED);
		}
		return result;
	}


	/**
	 *  Finds the field associated with a type
	 *
	 *@param  typeSummary  the type to search
	 *@param  name         the name of the field
	 *@param  protection   the minimum protection level
	 *@return              the field summary if one is found, null if none is
	 *      found
	 */
	public static FieldSummary query(TypeSummary typeSummary,
			String name, int protection) {
		Iterator iter = typeSummary.getFields();
		if (iter != null) {
			while (iter.hasNext()) {
				FieldSummary next = (FieldSummary) iter.next();
				if (appropriate(next, name, protection)) {
					return next;
				}
			}
		}

		return null;
	}


	/**
	 *  Finds the field associated with a type in the ancestors of that type
	 *
	 *@param  typeSummary  the type to search
	 *@param  name         the name of the field
	 *@param  protection   the minimum protection level
	 *@return              the field summary if one is found, null if none is
	 *      found
	 */
	public static FieldSummary queryAncestors(TypeSummary typeSummary,
			String name, int protection) {
		TypeDeclSummary next = typeSummary.getParentClass();
		TypeSummary current = GetTypeSummary.query(next);

		while (current != null) {
			FieldSummary attempt = query(current, name, protection);
			if (attempt != null) {
				return attempt;
			}

			next = current.getParentClass();
			current = GetTypeSummary.query(next);
		}

		return null;
	}


	/**
	 *  Checks if the field we are considering is the correct type
	 *
	 *@param  fieldSummary  the summary of the field
	 *@param  name          the name of the field
	 *@param  protection    the protection level of the field
	 *@return               true if the field has the appropriate name and the
	 *      appropriate protection level.
	 */
	private static boolean appropriate(FieldSummary fieldSummary,
			String name, int protection) {
		if (fieldSummary.getName().equals(name)) {
			ModifierHolder mods = fieldSummary.getModifiers();
			if (protection == PRIVATE) {
				return true;
			}
			else if ((protection == DEFAULT) && !mods.isPrivate()) {
				return true;
			}
			else if ((protection == PROTECTED) && (mods.isPublic() || mods.isProtected())) {
				return true;
			}
			else if ((protection == PUBLIC) && mods.isPublic()) {
				return true;
			}
		}

		return false;
	}
}
