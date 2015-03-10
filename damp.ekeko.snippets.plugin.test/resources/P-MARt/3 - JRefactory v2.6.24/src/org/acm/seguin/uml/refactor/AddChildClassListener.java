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
import org.acm.seguin.uml.UMLPackage;

/**
 *  Adds a child class listener 
 *
 *@author    Chris Seguin 
 */
public class AddChildClassListener extends DialogViewListener {
	private UMLPackage current;
	private TypeSummary typeSummary;


	/**
	 *  Constructor for the AddChildClassListener object 
	 *
	 *@param  initPackage  Description of Parameter 
	 *@param  initType     Description of Parameter 
	 *@param  initMenu     The popup menu 
	 *@param  initItem     The current item 
	 */
	public AddChildClassListener(UMLPackage initPackage, TypeSummary initType, 
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		current = initPackage;
		typeSummary = initType;
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input 
	 *
	 *@return    the dialog box 
	 */
	protected JDialog createDialog() {
		return new AddChildClassDialog(current, typeSummary);
	}
}
