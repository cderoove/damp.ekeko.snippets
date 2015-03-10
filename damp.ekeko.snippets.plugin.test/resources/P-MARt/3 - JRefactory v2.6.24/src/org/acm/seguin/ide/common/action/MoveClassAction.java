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
import org.acm.seguin.uml.refactor.AddMoveClassListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class MoveClassAction extends RefactoringAction {
	/**
	 *  Constructor for the MoveClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public MoveClassAction(SelectedFileSet init)
	{
		super(init);
		initNames();
	}


	/**
	 *  Gets the Enabled attribute of the MoveClassAction object
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
		AddMoveClassListener mcl =
				new AddMoveClassListener(typeSummaryArray, null, null);
		mcl.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Repackage");
		putValue(SHORT_DESCRIPTION, "Move Class");
		putValue(LONG_DESCRIPTION, "Moves the class to a different package");
	}
}
