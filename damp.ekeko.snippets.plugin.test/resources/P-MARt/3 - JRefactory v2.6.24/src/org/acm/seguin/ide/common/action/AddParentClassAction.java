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
import org.acm.seguin.uml.refactor.AddParentClassListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class AddParentClassAction extends RefactoringAction {
	/**
	 *  Constructor for the AddParentClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public AddParentClassAction(SelectedFileSet init)
	{
		super(init);
		initNames();
	}


	/**
	 *  Gets the Enabled attribute of the AddParentClassAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		return isAllJava();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  typeSummaryArray  Description of Parameter
	 *@param  evt               Description of Parameter
	 */
	protected void activateListener(TypeSummary[] typeSummaryArray, ActionEvent evt)
	{
		AddParentClassListener apcl =
				new AddParentClassListener(null, typeSummaryArray, null, null);
		apcl.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Add Parent Class");
		putValue(SHORT_DESCRIPTION, "Add Parent Class");
		putValue(LONG_DESCRIPTION, "Allows the user to add an abstract parent class");
	}
}
