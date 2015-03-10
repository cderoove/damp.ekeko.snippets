package org.acm.seguin.uml.refactor;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Description of the Class
 *
 *@author     Grant Watson
 *@created    November 30, 2000
 */
public class ExtractInterfaceListener extends DialogViewListener {
	private UMLPackage current;
	private TypeSummary[] typeArray;


	/**
	 *  Constructor for the ExtractInterfaceListener object
	 *
	 *@param  initPackage  Description of Parameter
	 *@param  initType     Description of Parameter
	 *@param  initMenu     Description of Parameter
	 *@param  initItem     Description of Parameter
	 */
	public ExtractInterfaceListener(UMLPackage initPackage, TypeSummary[] initTypes,
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		current = initPackage;
		typeArray = initTypes;
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input
	 *
	 *@return    the dialog box
	 */
	protected JDialog createDialog() {
		return new ExtractInterfaceDialog(current, typeArray);
	}
}
