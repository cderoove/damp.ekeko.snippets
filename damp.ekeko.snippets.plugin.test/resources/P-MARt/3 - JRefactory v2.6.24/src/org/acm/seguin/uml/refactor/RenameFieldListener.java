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

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Adds a rename class listener
 *
 *@author    Chris Seguin
 */
public class RenameFieldListener extends DialogViewListener {
	private UMLPackage current;
	private FieldSummary fieldSummary;


	/**
	 *  Constructor for the RenameFieldListener object
	 *
	 *@param  initPackage  Description of Parameter
	 *@param  initField     Description of Parameter
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 */
	public RenameFieldListener(UMLPackage initPackage, FieldSummary initField,
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		current = initPackage;
		fieldSummary = initField;
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input
	 *
	 *@return    the dialog box
	 */
	protected JDialog createDialog() {
		return new RenameFieldDialog(current, fieldSummary);
	}
}
