/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.Ancestor;

/**
 *  Determines if there is a problem in performing this refactoring
 *  because of a near miss
 *
 *@author    Chris Seguin
 */
public class NearMissVisitor extends TraversalVisitor {
	private MethodSummary target;
	private TypeSummary ancestor;
	private TypeSummary notHere;
	private MethodSummary problem;


	/**
	 *  Constructor for the NearMissVisitor object
	 *
	 *@param  type  the ancestor type
	 *@param  init  the method
	 *@param  notThisOne a type to skip
	 */
	public NearMissVisitor(TypeSummary type, MethodSummary init, TypeSummary notThisOne) {
		target = init;
		ancestor = type;
		notHere = notThisOne;
		problem = null;
	}


	/**
	 *  Visits a type summary and updates it
	 *
	 *@param  typeSummary  Description of Parameter
	 *@param  data         Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public Object visit(TypeSummary typeSummary, Object data) {
		if ((typeSummary != notHere) && Ancestor.query(typeSummary, ancestor)) {
			Iterator iter = typeSummary.getMethods();
			if (iter != null) {
				while (iter.hasNext()) {
					visit((MethodSummary) iter.next(), data);
				}
			}
		}
		return data;
	}


	/**
	 *  Visits the method summary and determines if it should be removed.
	 *
	 *@param  methodSummary  Description of Parameter
	 *@param  data          Description of Parameter
	 *@return               Description of the Returned Value
	 */
	public Object visit(MethodSummary methodSummary, Object data) {
		if (methodSummary.isNearMiss(target)) {
			problem = methodSummary;
		}

		return data;
	}


	/**
	 *  Returns at least one near miss conflict
	 *
	 *@return the problem method
	 */
	public MethodSummary getProblem() {
		return problem;
	}
}
