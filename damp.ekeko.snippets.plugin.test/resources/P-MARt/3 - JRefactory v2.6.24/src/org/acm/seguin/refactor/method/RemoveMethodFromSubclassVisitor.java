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

import org.acm.seguin.awt.Question;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.Ancestor;

/**
 *  Removes the method from all subclasses of a particular class.
 *
 *@author    Chris Seguin
 */
public class RemoveMethodFromSubclassVisitor extends TraversalVisitor {
	private MethodSummary target;
	private TypeSummary ancestor;
	private ComplexTransform complex;
	private TypeSummary notHere;


	/**
	 *  Constructor for the RemoveMethodFromSubclassVisitor object
	 *
	 *@param  type  the ancestor type
	 *@param  init  the method
	 *@param  notThisOne a type to skip
	 */
	public RemoveMethodFromSubclassVisitor(TypeSummary type,
		MethodSummary init, TypeSummary notThisOne,
		ComplexTransform initComplex) {
		target = init;
		ancestor = type;
		notHere = notThisOne;
		complex = initComplex;
	}


	/**
	 *  Visits a file summary node and updates it if necessary
	 *
	 *@param  fileSummary  Description of Parameter
	 *@param  data         Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public Object visit(FileSummary fileSummary, Object data) {
		complex.clear();
		super.visit(fileSummary, data);
		if (complex.hasAnyChanges()) {
			String title = "Removing " + target.getName() + " from children of " + ancestor.getName();
			String question = "Would you like to remove\n" + target.toString() + "\nfrom " + fileSummary.getName();
			if (Question.isYes(title, question)) {
				complex.apply(fileSummary.getFile(), fileSummary.getFile());
			}
		}
		return data;
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
		if (methodSummary.equals(target)) {
			complex.add(new RemoveMethodTransform(target));
		}

		return data;
	}
}
