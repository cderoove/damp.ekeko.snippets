/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.util.Iterator;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.method.PushDownMethodRefactoring;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Dialog for specifing where to push down the field into
 *
 *@author    Chris Seguin
 */
class PushDownMethodDialog extends ChildrenCheckboxDialog
{
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the PushDownMethodDialog object
	 *
	 *@param  init      Description of Parameter
	 *@param  initType  Description of Parameter
	 *@param  method    Description of Parameter
	 */
	public PushDownMethodDialog(UMLPackage init, TypeSummary initType, MethodSummary method)
	{
		super(init, initType);

		methodSummary = method;

		setTitle("Push field " + methodSummary.toString() + " from " + parentType.getName() + " to:");
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		PushDownMethodRefactoring pushDown = RefactoringFactory.get().pushDownMethod();
		pushDown.setMethod(methodSummary);

		Iterator iter = children.getCheckboxes();
		while (iter.hasNext())
		{
			TypeCheckbox next = (TypeCheckbox) iter.next();
			if (next.isSelected())
			{
				pushDown.addChild(next.getTypeSummary());
			}
		}

		return pushDown;
	}
}
