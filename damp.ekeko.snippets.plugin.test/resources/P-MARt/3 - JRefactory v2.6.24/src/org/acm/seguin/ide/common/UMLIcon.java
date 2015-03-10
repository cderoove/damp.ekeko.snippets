/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 *  Draws a UML icon for the project window 
 *
 *@author    Chris Seguin 
 */
public class UMLIcon implements Icon {
	/**
	 *  Returns the icon's height. 
	 *
	 *@return    The IconHeight value 
	 */
	public int getIconHeight() {
		return 16;
	}


	/**
	 *  Returns the icon's width. 
	 *
	 *@return    The IconWidth value 
	 */
	public int getIconWidth() {
		return 16;
	}


	/**
	 *  Draws the icon at a location 
	 *
	 *@param  c  The component 
	 *@param  g  The graphics area 
	 *@param  x  The x coordinate 
	 *@param  y  The y coordinate 
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		drawClassDiagram(g, x + 1, y + 1);
		drawClassDiagram(g, x + 9, y + 5);
		//g.drawLine(x+6,y+5,x+9,y+10);
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  g  Description of Parameter 
	 *@param  x  Description of Parameter 
	 *@param  y  Description of Parameter 
	 */
	private void drawClassDiagram(Graphics g, int x, int y) {
		g.setColor(Color.white);
		g.fillRect(x, y, 5, 9);
		g.setColor(Color.black);
		g.drawRect(x, y, 5, 9);
		g.drawLine(x, y + 3, x + 5, y + 3);
		g.drawLine(x, y + 6, x + 5, y + 6);
	}
}
