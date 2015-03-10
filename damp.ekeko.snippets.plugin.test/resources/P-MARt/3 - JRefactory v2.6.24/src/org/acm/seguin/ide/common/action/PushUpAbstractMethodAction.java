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
import org.acm.seguin.uml.refactor.PushUpAbstractMethodListener;

/**
 *  Pushes an abstract signature of a method into the parent class
 *
 *@author    Chris Seguin
 */
public class PushUpAbstractMethodAction extends RefactoringAction {
	/**
	 *  Constructor for the PushUpAbstractMethodAction object
	 */
	public PushUpAbstractMethodAction()
	{
		super(new EmptySelectedFileSet());

		putValue(NAME, "Push Up Abstract Method");
		putValue(SHORT_DESCRIPTION, "Push Up Abstract Method");
		putValue(LONG_DESCRIPTION, "Put the signature of the method into the parent class");
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
		PushUpAbstractMethodListener listener = new PushUpAbstractMethodListener(null, methodSummary, null, null);
		listener.actionPerformed(null);
	}
}
