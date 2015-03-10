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

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.acm.seguin.metrics.TypeMetricsFrame;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Create a mouse listener for a type or a field or a title 
 *
 *@author     Chris Seguin 
 *@created    July 7, 1999 
 */
public class TypeMetricsListener extends PopupMenuListener {
	//  Instance Variables
	private TypeSummary typeSummary;


	/**
	 *  Constructor for the TypeMetricsListener object 
	 *
	 *@param  panel     Description of Parameter 
	 *@param  initMenu  Description of Parameter 
	 *@param  initItem  Description of Parameter 
	 */
	public TypeMetricsListener(JPanel panel, JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);

		if (panel instanceof UMLMethod) {
			typeSummary = (TypeSummary) ((UMLMethod) panel).getSummary().getParent();
		}
		else if (panel instanceof UMLField) {
			typeSummary = (TypeSummary) ((UMLField) panel).getSummary().getParent();
		}
		else if (panel instanceof UMLNestedType) {
			typeSummary = (TypeSummary) ((UMLNestedType) panel).getSummary().getParent();
		}
		else {
			typeSummary = (TypeSummary) ((UMLType) panel).getSummary();
		}
	}


	/**
	 *  A menu item has been selected 
	 *
	 *@param  evt   Description of Parameter 
	 */
	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt);
		new TypeMetricsFrame(typeSummary);
	}
}
