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

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.refactor.MoveMethodListener;

/**
 *  Pushes a method into the parent class
 *
 *@author    Chris Seguin
 */
public class MoveMethodAction extends RefactoringAction {
	/**
	 *  Constructor for the MoveMethodAction object
	 */
	public MoveMethodAction()
	{
		super(null);

		putValue(NAME, "Move Method");
		putValue(SHORT_DESCRIPTION, "Move Method");
		putValue(LONG_DESCRIPTION, "Move a method into the class of an argument");
	}


	/**
	 *  Gets the Enabled attribute of the MoveMethodAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		CurrentSummary cs = CurrentSummary.get();
		Summary summary = cs.getCurrentSummary();
		return (summary != null) && (summary instanceof MethodSummary);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  evt               Description of Parameter
	 *@param  typeSummaryArray  Description of Parameter
	 */
	protected void activateListener(TypeSummary[] typeSummaryArray, ActionEvent evt)
	{
		CurrentSummary cs = CurrentSummary.get();
		MethodSummary methodSummary = (MethodSummary) cs.getCurrentSummary();
		MoveMethodListener listener = new MoveMethodListener(null, null, methodSummary, null, null);
		listener.actionPerformed(null);
	}
}
