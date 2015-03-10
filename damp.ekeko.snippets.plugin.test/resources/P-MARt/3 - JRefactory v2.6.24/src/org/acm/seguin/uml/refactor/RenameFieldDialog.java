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
import org.acm.seguin.refactor.field.RenameFieldRefactoring;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Creates a dialog box to prompt for the new package name
 *
 *@author    Chris Seguin
 */
public class RenameFieldDialog extends ClassNameDialog
{
	//  Instance Variables
	private FieldSummary fieldSummary;


	/**
	 *  Constructor for RenameFieldDialog
	 *
	 *@param  init  Description of Parameter
	 *@param  type  Description of Parameter
	 */
	public RenameFieldDialog(UMLPackage init, FieldSummary field)
	{
		super(init, 1);

		fieldSummary = field;
		setTitle(getWindowTitle());
	}


	/**
	 *  Returns the window title
	 *
	 *@return    the title
	 */
	public String getWindowTitle()
	{
		if (fieldSummary == null)
		{
			return "Rename field";
		}
		else
		{
			return "Rename field: " + fieldSummary.getName();
		}
	}


	/**
	 *  Gets the label for the text
	 *
	 *@return    the text for the label
	 */
	public String getLabelText()
	{
		return "New Name:";
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		RenameFieldRefactoring rfr = RefactoringFactory.get().renameField();
		rfr.setClass((TypeSummary) fieldSummary.getParent());
		rfr.setField(fieldSummary.getName());
		rfr.setNewName(getClassName());

		return rfr;
	}


	/**
	 *  Rename the type summary that has been influenced
	 */
	protected void updateSummaries()
	{
		fieldSummary.setName(getClassName());
	}
}
