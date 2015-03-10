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

import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;

/**
 *  Reloads class diagrams
 *
 *@author    Chris Seguin
 */
public class ReloadAction extends JBuilderAction {
	/**
	 *  Constructor for the PrintAction object
	 */
	public ReloadAction() {
		putValue(NAME, "Load Refactoring Metadata");
		putValue(SHORT_DESCRIPTION, "Load Refactoring Metadata");
		putValue(LONG_DESCRIPTION, "Reloads the metadata for the class diagrams");
	}


	/**
	 *  Gets the Enabled attribute of the PrettyPrinterAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled() {
		return enabled;
	}


	/**
	 *  The pretty printer action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt) {
		MultipleDirClassDiagramReloader reloader =
				UMLNodeViewerFactory.getFactory().getReloader();

		reloader.setNecessary(true);
		reloader.reload();

		putValue(NAME, "Reload Refactoring Metadata");
		putValue(SHORT_DESCRIPTION, "Reload Refactoring Metadata");
	}
}
