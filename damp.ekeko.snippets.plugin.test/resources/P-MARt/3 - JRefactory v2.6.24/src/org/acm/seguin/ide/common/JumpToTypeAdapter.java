package org.acm.seguin.ide.common;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Adapter that tells the UML diagram to jump to a specific type
 *
 *@author    Chris Seguin
 */
class JumpToTypeAdapter extends MouseAdapter {
	private UMLPackage umlPackage;
	private TypeSummary typeSummary;


	/**
	 *  Constructor for the JumpToTypeAdapter object
	 *
	 *@param  initPanel  Description of Parameter
	 *@param  initType   Description of Parameter
	 */
	public JumpToTypeAdapter(UMLPackage initPanel, TypeSummary initType) {
		umlPackage = initPanel;
		typeSummary = initType;
	}


	/**
	 *  The mouse has been clicked on the type
	 *
	 *@param  e  the mouse click
	 */
	public void mouseClicked(MouseEvent e) {
		if (umlPackage == null) return;

		umlPackage.jumpTo(typeSummary);
	}
}
