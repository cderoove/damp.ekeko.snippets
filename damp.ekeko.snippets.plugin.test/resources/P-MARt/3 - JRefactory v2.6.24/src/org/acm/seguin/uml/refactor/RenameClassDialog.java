/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.io.File;
import java.io.IOException;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.RenameClassRefactoring;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Creates a dialog box to prompt for the new package name
 *
 *@author    Chris Seguin
 */
public class RenameClassDialog extends ClassNameDialog
{
	//  Instance Variables
	private TypeSummary typeSummary;


	/**
	 *  Constructor for RenameClassDialog
	 *
	 *@param  init  Description of Parameter
	 *@param  type  Description of Parameter
	 */
	public RenameClassDialog(UMLPackage init, TypeSummary type)
	{
		super(init, 1);

		typeSummary = type;
		setTitle(getWindowTitle());
	}


	/**
	 *  Returns the window title
	 *
	 *@return    the title
	 */
	public String getWindowTitle()
	{
		if (typeSummary == null)
		{
			return "Rename class";
		}
		else
		{
			return "Rename class: " + typeSummary.getName();
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
		String oldClassName = typeSummary.getName();
		String newClassName = getClassName();

		File file = ((FileSummary) (typeSummary.getParent())).getFile();
		String path = file.getPath();
		try
		{
			path = file.getCanonicalPath();
		}
		catch (IOException ioe)
		{
		}
		file = new File(path);

		File initialStarting = file.getParentFile();
		RenameClassRefactoring rc = RefactoringFactory.get().renameClass();
		rc.setDirectory(initialStarting.getPath());
		rc.setOldClassName(oldClassName);
		rc.setNewClassName(newClassName);

		return rc;
	}


	/**
	 *  Rename the type summary that has been influenced
	 */
	protected void updateSummaries()
	{
		typeSummary.setName(getClassName());
	}
}
