package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Checks that two methods are the same. Also provides a search feature to
 *  find a method with a specific signature in a type
 *
 *@author    Chris Seguin
 */
public class SameMethod {
	private final static int SAME = 0;
	private final static int ONE_ANCESTOR = 1;
	private final static int TWO_ANCESTOR = 2;
	private final static int ERROR = 3;
	private final static int ANCESTOR = 4;


	/**
	 *  Checks if two methods are the same
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public static boolean query(MethodSummary one, MethodSummary two) {
		return check(one, two, SAME);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public static boolean conflict(MethodSummary one, MethodSummary two) {
		return check(one, two, ANCESTOR);
	}


	/**
	 *  Finds the method with the same signature in the other type
	 *
	 *@param  type    Description of Parameter
	 *@param  method  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	public static MethodSummary find(TypeSummary type, MethodSummary method) {
		Iterator iter = type.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				if (query(next, method)) {
					return next;
				}
			}
		}
		return null;
	}


	/**
	 *  Finds the method with a conflicting in the other type
	 *
	 *@param  type    Description of Parameter
	 *@param  method  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	public static MethodSummary findConflict(TypeSummary type, MethodSummary method) {
		Iterator iter = type.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				if (conflict(next, method)) {
					return next;
				}
			}
		}
		return null;
	}


	/**
	 *  Checks the types
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 *@param  way  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	private static int compareTypes(TypeSummary one, TypeSummary two, int way) {
		if (one == two) {
			return way;
		}

		if (way == ANCESTOR) {
			if (Ancestor.query(one, two)) {
				return ONE_ANCESTOR;
			}
			else if (Ancestor.query(two, one)) {
				return TWO_ANCESTOR;
			}
		}

		if ((ONE_ANCESTOR == way) && (Ancestor.query(two, one))) {
			return way;
		}

		if ((TWO_ANCESTOR == way) && (Ancestor.query(one, two))) {
			return way;
		}

		return ERROR;
	}


	/**
	 *  Work horse that actually checks the methods
	 *
	 *@param  one   Description of Parameter
	 *@param  two   Description of Parameter
	 *@param  test  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private static boolean check(MethodSummary one, MethodSummary two, int test) {
		if (!one.getName().equals(two.getName())) {
			return false;
		}

		Iterator oneIter = one.getParameters();
		Iterator twoIter = two.getParameters();

		if (oneIter == null) {
			return twoIter == null;
		}

		if (twoIter == null) {
			return false;
		}

		while (oneIter.hasNext() && twoIter.hasNext()) {
			ParameterSummary oneParam = (ParameterSummary) oneIter.next();
			ParameterSummary twoParam = (ParameterSummary) twoIter.next();

			TypeDeclSummary oneDecl = oneParam.getTypeDecl();
			TypeDeclSummary twoDecl = twoParam.getTypeDecl();

			TypeSummary typeOne = GetTypeSummary.query(oneDecl);
			TypeSummary typeTwo = GetTypeSummary.query(twoDecl);

			if (compareTypes(typeOne, typeTwo, test) == ERROR) {
				return false;
			}
		}

		return !(oneIter.hasNext() || twoIter.hasNext());
	}
}
