/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.AddAbstractParent;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Creates a dialog box to prompt for the new parent name
 *
 *@author    Chris Seguin
 */
public class AddAbstractParentDialog extends ClassNameDialog
{
	private TypeSummary[] typeArray;


	/**
	 *  Constructor for AddAbstractParentDialog
	 *
	 *@param  init       The package where this operation is occuring
	 *@param  initPanel  Description of Parameter
	 */
	public AddAbstractParentDialog(UMLPackage init, TypeSummary[] initTypes)
	{
		super(init, 1);

		typeArray = initTypes;
	}


	/**
	 *  Returns the window title
	 *
	 *@return    the title
	 */
	public String getWindowTitle()
	{
		return "Add an abstract parent";
	}


	/**
	 *  Gets the label for the text
	 *
	 *@return    the text for the label
	 */
	public String getLabelText()
	{
		return "Parent class:";
	}


	/**
	 *  Adds an abstract parent class to all specified classes.
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		//  Create system
		AddAbstractParent aap = RefactoringFactory.get().addParent();
		aap.setParentName(getClassName());

		//  Add the types
		for (int ndx = 0; ndx < typeArray.length; ndx++)
		{
			aap.addChildClass(typeArray[ndx]);
		}

		return aap;
	}
}
