/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Performs a local variable lookup
 *
 *@author    Chris Seguin
 */
public class LookupVariable {
	/**
	 *  Looks up the variable
	 *
	 *@param  method  the method summary
	 *@param  name    the name of the variable to find
	 *@return         the variable summary if found or null otherwise
	 */
	public static VariableSummary query(MethodSummary method, String name)
	{
		VariableSummary result = getLocal(method, name);
		if (result != null) {
			return result;
		}

		TypeSummary currentType = (TypeSummary) method.getParent();
		return queryFieldSummary(currentType, name);
	}


	/**
	 *  Get a field summary
	 *
	 *@param  currentType  the type to search in
	 *@param  name         the name of the field
	 *@return              the field summary found or null if none was found
	 */
	public static VariableSummary queryFieldSummary(TypeSummary currentType, String name)
	{
		VariableSummary result = getField(currentType, name, true);
		if (result != null) {
			return result;
		}

		TypeDeclSummary parentType = currentType.getParentClass();
		currentType = GetTypeSummary.query(parentType);
		while (currentType != null) {
			result = getField(currentType, name, false);
			if (result != null) {
				return result;
			}
		}

		return null;
	}


	/**
	 *  Finds a field in a type summary
	 *
	 *@param  type              the type to search
	 *@param  name              the name of the variable
	 *@param  isPrivateAllowed  is the field allowed to be private
	 *@return                   The FieldSummary if found, null otherwise
	 */
	private static VariableSummary getField(TypeSummary type, String name, boolean isPrivateAllowed)
	{
		Iterator iter = type.getFields();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			FieldSummary next = (FieldSummary) iter.next();
			if (!isPrivateAllowed || next.getModifiers().isPrivate()) {
				if (next.getName().equals(name)) {
					return next;
				}
			}
		}

		return null;
	}


	/**
	 *  Looks up the variable inside the method
	 *
	 *@param  method  the method summary
	 *@param  name    the name of the variable to find
	 *@return         the variable summary if found or null otherwise
	 */
	public static VariableSummary getLocal(MethodSummary method, String name)
	{
		Iterator iter = method.getParameters();
		if (iter != null) {
			while (iter.hasNext()) {
				ParameterSummary param = (ParameterSummary) iter.next();
				if (param.getName().equals(name)) {
					return param;
				}
			}
		}

		iter = method.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				if ((next instanceof LocalVariableSummary) && (next.getName().equals(name))) {
					return (VariableSummary) next;
				}
			}
		}

		return null;
	}
}
