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
import org.acm.seguin.uml.refactor.RenameFieldListener;

/**
 *  Pushes a field into the child classes
 *
 *@author    Chris Seguin
 */
public class RenameFieldAction extends RefactoringAction {
	/**
	 *  Constructor for the RenameFieldAction object
	 */
	public RenameFieldAction()
	{
		super(new EmptySelectedFileSet());

		putValue(NAME, "Rename Field");
		putValue(SHORT_DESCRIPTION, "Rename Field");
		putValue(LONG_DESCRIPTION, "Rename a field");
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
		RenameFieldListener pdfl = new RenameFieldListener(null, fieldSummary, null, null);
		pdfl.actionPerformed(null);
	}
}
