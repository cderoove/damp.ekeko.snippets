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
import org.acm.seguin.uml.refactor.ExtractInterfaceListener;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class ExtractInterfaceAction extends RefactoringAction {
	/**
	 *  Constructor for the ExtractInterfaceAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public ExtractInterfaceAction(SelectedFileSet init)
	{
		super(init);
		initNames();
	}


	/**
	 *  Gets the Enabled attribute of the ExtractInterfaceAction object
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
		ExtractInterfaceListener eil =
				new ExtractInterfaceListener(null, typeSummaryArray, null, null);
		eil.actionPerformed(evt);
	}


	/**
	 *  Description of the Method
	 */
	protected void initNames()
	{
		putValue(NAME, "Extract Interface");
		putValue(SHORT_DESCRIPTION, "Extract Interface");
		putValue(LONG_DESCRIPTION, "Allows the user to extract an interface");
	}
}
