/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.refactor.RefactoringException;

/**
 *  Performs the extract method action
 *
 *@author    Chris Seguin
 */
public class ExtractMethodAction extends GenericAction {
	/**
	 *  Constructor for the ExtractMethodAction object
	 */
	public ExtractMethodAction()
	{
		super();

		putValue(NAME, "Extract Method");
		putValue(SHORT_DESCRIPTION, "Extract Method");
		putValue(LONG_DESCRIPTION, "Highlight the code to extract and select this menu option");
		putValue(ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK | Event.SHIFT_MASK));
	}


	/**
	 *  Gets the Enabled attribute of the ExtractMethodAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		return EditorOperations.get().isJavaFile();
	}


	/**
	 *  What to do when someone selects the extract method refactoring
	 *
	 *@param  e  the button event
	 */
	public void actionPerformed(ActionEvent e)
	{
		try {
			(new GenericExtractMethod()).show();
		}
		catch (RefactoringException re) {
			JOptionPane.showMessageDialog(null, re.getMessage(), "Refactoring Exception",
					JOptionPane.ERROR_MESSAGE);
		}
		CurrentSummary.get().reset();
	}
}
