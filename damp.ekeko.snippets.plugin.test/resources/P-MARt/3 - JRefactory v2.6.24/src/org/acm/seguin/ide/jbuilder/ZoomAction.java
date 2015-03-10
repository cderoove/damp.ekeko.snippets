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

/**
 *  Zooms in on a diagram by the specified factor
 *
 *@author    Chris Seguin
 */
public class ZoomAction extends JBuilderAction {
	private double scalingFactor;


	/**
	 *  Constructor for the ZoomAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public ZoomAction(double init) {
		scalingFactor = init;

		int text = ((int) (scalingFactor * 100));

		putValue(NAME, "" + text + "%");
		putValue(SHORT_DESCRIPTION, "" + text + "%");
		putValue(LONG_DESCRIPTION, "Zooms in on a UML diagram to " + text + "% of the full diagram");
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
	 *  Updates the diagram when the user zooms to this level
	 *
	 *@param  evt  the action event
	 */
	public void actionPerformed(ActionEvent evt) {
		Browser browser = Browser.getActiveBrowser();
		UMLNode active = (UMLNode) browser.getActiveNode();
		UMLPackage diagram = active.getDiagram();
		diagram.scale(scalingFactor);
		diagram.repaint();
	}
}
