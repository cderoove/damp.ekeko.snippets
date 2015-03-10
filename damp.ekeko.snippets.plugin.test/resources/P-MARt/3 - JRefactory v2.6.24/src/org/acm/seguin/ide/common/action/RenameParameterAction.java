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
import org.acm.seguin.uml.refactor.RenameParameterListener;

/**
 *  Rename a parameter of a method
 *
 *@author    Chris Seguin
 */
public class RenameParameterAction extends RefactoringAction {
	/**
	 *  Constructor for the RenameParameterAction object
	 */
	public RenameParameterAction()
	{
		super(new EmptySelectedFileSet());

		putValue(NAME, "Rename Parameter");
		putValue(SHORT_DESCRIPTION, "Rename Parameter");
		putValue(LONG_DESCRIPTION, "Rename a parameter of the method");
	}


	/**
	 *  Gets the Enabled attribute of the PushUpMethodAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		CurrentSummary cs = CurrentSummary.get();
		Summary summary = cs.getCurrentSummary();
		return (summary != null) && (summary instanceof MethodSummary)
				 && (((MethodSummary) summary).getParameterCount() > 0);
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
		Summary summary = cs.getCurrentSummary();
		RenameParameterListener rpl =
				new RenameParameterListener((MethodSummary) summary);

		rpl.actionPerformed(null);
	}
}
