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

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Adds a child class listener
 *
 *@author    Chris Seguin
 */
public class PushDownFieldListener extends DialogViewListener {
	private UMLPackage current;
	private TypeSummary typeSummary;
	private String fieldName;


	/**
	 *  Constructor for the AddChildClassListener object
	 *
	 *@param  initPackage  Description of Parameter
	 *@param  initType     Description of Parameter
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 *@param  field        Description of Parameter
	 */
	public PushDownFieldListener(UMLPackage initPackage, TypeSummary initType,
			FieldSummary field,
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		current = initPackage;
		typeSummary = initType;
		fieldName = field.getName();
		if (typeSummary == null) {
			typeSummary = (TypeSummary) field.getParent();
		}
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input
	 *
	 *@return    the dialog box
	 */
	protected JDialog createDialog() {
		return new PushDownFieldDialog(current, typeSummary, fieldName);
	}
}
