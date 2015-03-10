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

import javax.swing.KeyStroke;

import org.acm.seguin.ide.common.EditorOperations;

/**
 *  Pretty printer action button
 *
 *@author    Chris Seguin
 */
public class PrettyPrinterAction extends GenericAction {
	/**
	 *  Constructor for the PrettyPrinterAction object
	 */
	public PrettyPrinterAction()
	{
		putValue(NAME, "Pretty Printer");
		putValue(SHORT_DESCRIPTION, "Pretty Printer");
		putValue(LONG_DESCRIPTION, "Reindents java source file\n" +
				"Adds intelligent javadoc comments");
		putValue(ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK | Event.SHIFT_MASK));
	}


	/**
	 *  Determines if this menu item should be enabled
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		if (!enabled) {
			return false;
		}

		return EditorOperations.get().isJavaFile();
	}


	/**
	 *  The pretty printer action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt)
	{
		GenericPrettyPrinter jbpp = new GenericPrettyPrinter();
		jbpp.prettyPrintCurrentWindow();
		CurrentSummary.get().reset();
	}
}
