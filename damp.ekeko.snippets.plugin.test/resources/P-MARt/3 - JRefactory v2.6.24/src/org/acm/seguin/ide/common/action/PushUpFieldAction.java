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

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.refactor.PushUpFieldListener;

/**
 *  Pushes a field into the parent class
 *
 *@author    Chris Seguin
 */
public class PushUpFieldAction extends RefactoringAction {
	/**
	 *  Constructor for the PushUpFieldAction object
	 */
	public PushUpFieldAction()
	{
		super(new EmptySelectedFileSet());

		putValue(NAME, "Push Up Field");
		putValue(SHORT_DESCRIPTION, "Push Up Field");
		putValue(LONG_DESCRIPTION, "Move a field into the parent class");
	}


	/**
	 *  Gets the Enabled attribute of the PushUpFieldAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		CurrentSummary cs = CurrentSummary.get();
		Summary summary = cs.getCurrentSummary();
		return (summary != null) && (summary instanceof FieldSummary);
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
		FieldSummary fieldSummary = (FieldSummary) cs.getCurrentSummary();
		PushUpFieldListener pufl = new PushUpFieldListener(null, null, fieldSummary, null, null);
		pufl.actionPerformed(null);
	}
}
