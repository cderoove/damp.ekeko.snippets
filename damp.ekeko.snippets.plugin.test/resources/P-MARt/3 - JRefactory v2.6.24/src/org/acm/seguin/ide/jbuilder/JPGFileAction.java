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
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.Node;
import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.uml.jpg.SaveAdapter;

/**
 *  Menu item to create a JPG file
 *
 *@author    Chris Seguin
 */
public class JPGFileAction extends JBuilderAction {
	/**
	 *  Constructor for the JPGFileAction object
	 */
	public JPGFileAction() {
		putValue(NAME, "JPG File");
		putValue(SHORT_DESCRIPTION, "JPG File");
		putValue(LONG_DESCRIPTION, "Creates a JPG file from the UML diagram");
	}


	/**
	 *  Determines if this menu item should be enabled
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled() {
		if (!enabled) {
			return false;
		}

		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		return (active instanceof UMLNode);
	}


	/**
	 *  Saves the UML diagram as a JPG file
	 *
	 *@param  evt  the action event
	 */
	public void actionPerformed(ActionEvent evt) {
		Browser browser = Browser.getActiveBrowser();
		UMLNode active = (UMLNode) browser.getActiveNode();
		UMLPackage diagram = active.getDiagram();

		SaveAdapter adapter = new SaveAdapter(diagram);
		adapter.actionPerformed(evt);
	}
}
