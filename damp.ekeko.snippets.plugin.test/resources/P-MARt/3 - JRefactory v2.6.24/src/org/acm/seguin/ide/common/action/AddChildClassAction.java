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
import org.acm.seguin.uml.refactor.AddChildClassListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class AddChildClassAction extends RefactoringAction {
	/**
	 *  Constructor for the AddChildClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public AddChildClassAction(SelectedFileSet init)
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
	 *  Description of the Method
	 *
	 *@param  evt               Description of Parameter
	 *@param  typeSummaryArray  Description of Parameter
	 */
	protected void activateListener(TypeSummary[] typeSummaryArray, ActionEvent evt)
	{
		AddChildClassListener accl =
				new AddChildClassListener(null, typeSummaryArray[0], null, null);
		accl.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Add Child Class");
		putValue(SHORT_DESCRIPTION, "Add Child Class");
		putValue(LONG_DESCRIPTION, "Allows the user to add a child class");
	}
}
