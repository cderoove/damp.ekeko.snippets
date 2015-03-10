/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import java.awt.event.ActionEvent;

import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.refactor.AddRenameClassListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class RenameClassAction extends RefactoringAction {
	/**
	 *  Constructor for the RenameClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public RenameClassAction(SelectedFileSet init)
	{
		super(init);
		initNames();
	}


	/**
	 *  Gets the Enabled attribute of the RenameClassAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		return isSingleJavaFile();
	}


	/**
	 *  The listener to activate with the specified types
	 *
	 *@param  typeSummaryArray  Description of Parameter
	 *@param  evt               Description of Parameter
	 */
	protected void activateListener(TypeSummary[] typeSummaryArray, ActionEvent evt)
	{
		AddRenameClassListener rcl = new AddRenameClassListener(null, typeSummaryArray[0], null, null);
		rcl.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Rename Class");
		putValue(SHORT_DESCRIPTION, "Rename Class");
		putValue(LONG_DESCRIPTION, "Allows the user to rename the class");
	}
}
