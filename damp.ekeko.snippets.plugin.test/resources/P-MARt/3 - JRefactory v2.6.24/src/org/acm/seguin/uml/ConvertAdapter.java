/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.acm.seguin.summary.TypeSummary;

/**
 *  Converts between associations and attributes 
 *
 *@author     Chris Seguin 
 *@created    August 17, 1999 
 */
public class ConvertAdapter implements ActionListener {
	private UMLPackage packagePanel;
	private UMLType typePanel;
	private UMLField fieldPanel;


	/**
	 *  Constructor for the ConvertAdapter object 
	 *
	 *@param  packagePanel  the package panel 
	 *@param  fieldPanel    the field panel 
	 */
	public ConvertAdapter(UMLPackage packagePanel, UMLField fieldPanel) {
		this.packagePanel = packagePanel;
		this.fieldPanel = fieldPanel;
		typePanel = packagePanel.findType((TypeSummary) fieldPanel.getSummary().getParent());
	}


	/**
	 *  Menu item is selected 
	 *
	 *@param  ev  selection event 
	 */
	public void actionPerformed(ActionEvent ev) {
		if (fieldPanel.isAssociation()) {
			fieldPanel.setAssociation(false);
			typePanel.convertToAttribute(packagePanel, fieldPanel);
		}
		else {
			fieldPanel.setAssociation(true);
			typePanel.convertToAssociation(packagePanel, fieldPanel);
		}
	}
}
