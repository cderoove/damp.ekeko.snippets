/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.awt.event.ActionEvent;

import org.acm.seguin.awt.AboutBox;

/**
 *  Pretty printer action button
 *
 *@author    Chris Seguin
 */
public class AboutAction extends JBuilderAction {
	/**
	 *  Constructor for the AboutAction object
	 */
	public AboutAction() {
		putValue(NAME, "About");
		putValue(SHORT_DESCRIPTION, "About JRefactory");
		putValue(LONG_DESCRIPTION, "Shows the about box");
	}


	/**
	 *  Determines if this menu item should be enabled
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled() {
		return true;
	}


	/**
	 *  The pretty printer action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt) {
		AboutBox.run();
	}
}
