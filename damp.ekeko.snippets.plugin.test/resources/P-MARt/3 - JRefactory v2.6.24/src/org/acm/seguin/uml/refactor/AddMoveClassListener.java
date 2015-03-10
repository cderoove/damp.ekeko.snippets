/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.summary.TypeSummary;

/**
 *  Adds a move class listener
 *
 *@author    Chris Seguin
 */
public class AddMoveClassListener extends DialogViewListener {
	private TypeSummary[] typeArray;


	/**
	 *  Constructor for the AddMoveClassListener object
	 *
	 *@param  initPackage  Description of Parameter
	 *@param  initType     Description of Parameter
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 */
	public AddMoveClassListener(TypeSummary[] initTypes,
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		typeArray = initTypes;
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input
	 *
	 *@return    the dialog box
	 */
	protected JDialog createDialog() {
		return new NewPackageDialog(typeArray);
	}
}
