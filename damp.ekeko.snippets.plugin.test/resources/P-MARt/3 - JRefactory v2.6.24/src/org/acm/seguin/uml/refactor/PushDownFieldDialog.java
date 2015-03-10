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
import org.acm.seguin.refactor.field.PushDownFieldRefactoring;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Dialog for specifing where to push down the field into
 *
 *@author    Chris Seguin
 */
class PushDownFieldDialog extends ChildrenCheckboxDialog
{
	private String field;


	/**
	 *  Constructor for the PushDownFieldDialog object
	 *
	 *@param  init      Description of Parameter
	 *@param  name      Description of Parameter
	 *@param  initType  Description of Parameter
	 */
	public PushDownFieldDialog(UMLPackage init, TypeSummary initType, String name)
	{
		super(init, initType);

		field = name;

		setTitle("Push field " + field + " from " + parentType.getName() + " to:");
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		PushDownFieldRefactoring pushDown = RefactoringFactory.get().pushDownField();
		pushDown.setField(field);
		pushDown.setClass(parentType);

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
