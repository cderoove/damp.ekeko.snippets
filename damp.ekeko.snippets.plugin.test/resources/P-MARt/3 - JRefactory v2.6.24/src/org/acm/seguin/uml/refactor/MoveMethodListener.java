/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Listener for the move method menu item
 *
 *@author    Chris Seguin
 */
public class MoveMethodListener extends DialogViewListener {
	private UMLPackage current;
	private TypeSummary typeSummary;
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the MoveMethodListener object
	 *
	 *@param  initPackage  Description of Parameter
	 *@param  initType     Description of Parameter
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 *@param  method       Description of Parameter
	 */
	public MoveMethodListener(UMLPackage initPackage,
			TypeSummary initType,
			MethodSummary method,
			JPopupMenu initMenu,
			JMenuItem initItem) {
		super(initMenu, initItem);
		current = initPackage;
		typeSummary = initType;
		methodSummary = method;
		if (typeSummary == null) {
			typeSummary = (TypeSummary) methodSummary.getParent();
		}
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input
	 *
	 *@return    the dialog box
	 */
	protected JDialog createDialog() {
		return new MoveMethodDialog(current, methodSummary);
	}
}
