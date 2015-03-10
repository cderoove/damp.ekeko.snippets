package org.acm.seguin.ide.common;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 *  A little tool that allows an Icon to exist on a JPanel or other component
 *
 *@author    Chris Seguin
 */
class IconPanel extends JPanel {
	private Icon icon;


	/**
	 *  Constructor for the IconPanel object
	 *
	 *@param  init  Description of Parameter
	 */
	public IconPanel(Icon init) {
		icon = init;
	}


	/**
	 *  Gets the PreferredSize attribute of the IconPanel object
	 *
	 *@return    The PreferredSize value
	 */
	public Dimension getPreferredSize() {
		return new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2);
	}


	/**
	 *  Gets the MinimumSize attribute of the IconPanel object
	 *
	 *@return    The MinimumSize value
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of Parameter
	 */
	public void paint(Graphics g) {
		Dimension dim = getSize();

		int x = (dim.width - icon.getIconWidth()) / 2 - 1;
		int y = (dim.height - icon.getIconHeight()) / 2;

		icon.paintIcon(this, g, x, y);
	}
}
