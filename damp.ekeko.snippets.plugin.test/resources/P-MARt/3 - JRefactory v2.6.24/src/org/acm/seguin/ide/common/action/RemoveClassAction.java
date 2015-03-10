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
import org.acm.seguin.uml.refactor.RemoveClassListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class RemoveClassAction extends RefactoringAction {
	/**
	 *  Constructor for the RemoveClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public RemoveClassAction(SelectedFileSet init)
	{
		super(init);
		initNames();
	}


	/**
	 *  Gets the Enabled attribute of the AddChildClassAction object
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
		RemoveClassListener rcl = new RemoveClassListener(null, typeSummaryArray[0], null, null);
		rcl.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Remove Class");
		putValue(SHORT_DESCRIPTION, "Remove Class");
		putValue(LONG_DESCRIPTION, "Allows the user to remove a class");
	}
}
