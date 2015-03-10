/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;

/**
 *  Refactoring to allow a user to rename a parameter
 *
 *@author    Chris Seguin
 */
public class RenameParameterRefactoring extends Refactoring
{
	private String newName;
	private ParameterSummary param;
	private MethodSummary method;


	/**
	 *  Constructor for the RenameParameterRefactoring object
	 */
	protected RenameParameterRefactoring()
	{
		newName = null;
		param = null;
		method = null;
	}


	/**
	 *  Sets the NewName attribute of the RenameParameterRefactoring object
	 *
	 *@param  value  The new NewName value
	 */
	public void setNewName(String value)
	{
		newName = value;
	}


	/**
	 *  Sets the ParameterSummary attribute of the RenameParameterRefactoring
	 *  object
	 *
	 *@param  value  The new ParameterSummary value
	 */
	public void setParameterSummary(ParameterSummary value)
	{
		param = value;
	}


	/**
	 *  Sets the MethodSummary attribute of the RenameParameterRefactoring object
	 *
	 *@param  value  The new MethodSummary value
	 */
	public void setMethodSummary(MethodSummary value)
	{
		method = value;
	}


	/**
	 *  Gets the Description attribute of the RenameParameterRefactoring object
	 *
	 *@return    The Description value
	 */
	public String getDescription()
	{
		return "Renaming " + param.getName() + " to " + newName + " in " + method.toString();
	}


	/**
	 *  Gets the ID attribute of the RenameParameterRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return RENAME_PARAMETER;
	}


	/**
	 *  Description of the Method
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void preconditions() throws RefactoringException
	{
		if ((newName == null) || (newName.length() == 0))
		{
			throw new RefactoringException("No new name specified");
		}

		if (param == null)
		{
			throw new RefactoringException("No parameter specified");
		}

		if (method == null)
		{
			throw new RefactoringException("No method specified");
		}
	}


	/**
	 *  Perform the transformation
	 */
	protected void transform()
	{
		//  Get the complex transformation
		ComplexTransform transform = getComplexTransform();

		//  Add the parameter rename transformation
		RenameParameterTransform rpt = new RenameParameterTransform();
		rpt.setMethod(method);
		rpt.setParameter(param);
		rpt.setNewName(newName);
		transform.add(rpt);

		//  Apply the refactoring
		Summary current = method;
		while (!(current instanceof FileSummary))
		{
			current = current.getParent();
		}
		FileSummary fileSummary = (FileSummary) current;
		transform.apply(fileSummary.getFile(), fileSummary.getFile());
	}
}
