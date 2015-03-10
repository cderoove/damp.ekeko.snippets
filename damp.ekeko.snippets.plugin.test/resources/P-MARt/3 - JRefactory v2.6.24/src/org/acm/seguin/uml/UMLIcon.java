package org.acm.seguin.uml;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 *  Base class for drawing icons on a UML diagram
 *
 *@author    Chris Seguin
 */
public abstract class UMLIcon implements Icon {
	/**
	 *  Description of the Field
	 */
	protected int iconHeight;
	/**
	 *  Description of the Field
	 */
	protected int iconWidth;
	/**
	 *  Description of the Field
	 */
	protected double scale = 1.0;


	/**
	 *  Constructor for the UMLIcon object
	 *
	 *@param  wide  the size of the icon
	 *@param  high  the size of the icon
	 */
	public UMLIcon(int wide, int high) {
		iconWidth = wide;
		iconHeight = high;
	}


	/**
	 *  Sets the Scale attribute of the UMLIcon object
	 *
	 *@param  value  The new Scale value
	 */
	public void setScale(double value) {
		scale = value;
	}


	/**
	 *  Gets the IconWidth attribute of the UMLIcon object
	 *
	 *@return    The IconWidth value
	 */
	public int getIconWidth() {
		return iconHeight;
	}


	/**
	 *  Gets the IconHeight attribute of the UMLIcon object
	 *
	 *@return    The IconHeight value
	 */
	public int getIconHeight() {
		return iconWidth;
	}


	/**
	 *  Draws the icon
	 *
	 *@param  c  The component on which we are drawing
	 *@param  g  The graphics object
	 *@param  x  the x location
	 *@param  y  the y location
	 */
	public abstract void paintIcon(Component c, Graphics g, int x, int y);
}
