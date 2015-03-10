/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 *  Prints a single line 
 *
 *@author    Chris Seguin 
 */
public class LinePrinter {
	/**
	 *  Description of the Field 
	 */
	protected int fontSize = -1;
	private Font font = null;


	/**
	 *  Sets the FontSize attribute of the LinePrinter object 
	 *
	 *@param  value  The new FontSize value 
	 */
	public void setFontSize(int value) {
		if (fontSize != value) {
			fontSize = value;
			font = null;
		}
	}


	/**
	 *  Gets the LineHeight attribute of the LinePrinter object 
	 *
	 *@param  g  Description of Parameter 
	 *@return    The LineHeight value 
	 */
	public int getLineHeight(Graphics g) {
		init(g);

		FontMetrics fm = g.getFontMetrics();
		return fm.getHeight();
	}


	/**
	 *  Initializes the graphics object to begin printing 
	 *
	 *@param  g  the graphics object 
	 */
	public void init(Graphics g) {
		if (font == null) {
			font = new Font("Monospaced", Font.PLAIN, fontSize);
		}
		g.setColor(Color.black);
		g.setFont(font);
	}


	/**
	 *  Prints the line 
	 *
	 *@param  g      The graphics device 
	 *@param  line   The string to print 
	 *@param  x      The x location on the graphics device 
	 *@param  y      The y location on the graphics device 
	 *@param  set    The set of lines 
	 *@param  index  The line we are printing 
	 */
	public void print(Graphics g, String line, int x, int y, 
			LineSet set, int index) {
		if (line.length() > 0) {
			g.drawString(line, x, y);
		}
	}
}

