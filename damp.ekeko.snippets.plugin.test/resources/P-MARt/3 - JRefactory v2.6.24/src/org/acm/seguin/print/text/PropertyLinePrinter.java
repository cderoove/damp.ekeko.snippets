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

import org.acm.seguin.util.TextFormatter;

/**
 *  Prints a single line 
 *
 *@author    Chris Seguin 
 */
public class PropertyLinePrinter extends LinePrinter {
	private Font lineNo;
	private Font normal;
	private Font keyword;
	private Font comment;


	/**
	 *  Constructor for the PropertyLinePrinter object 
	 */
	public PropertyLinePrinter() {
		normal = null;
		keyword = null;
		comment = null;
		lineNo = null;
	}


	/**
	 *  Sets the FontSize attribute of the LinePrinter object 
	 *
	 *@param  value  The new FontSize value 
	 */
	public void setFontSize(int value) {
		if (fontSize != value) {
			super.setFontSize(value);

			normal = null;
			keyword = null;
			comment = null;
			lineNo = null;
		}
	}


	/**
	 *  Initializes the graphics object to begin printing 
	 *
	 *@param  g  the graphics object 
	 */
	public void init(Graphics g) {
		if (normal == null) {
			normal = new Font("SansSerif", Font.PLAIN, fontSize);
			keyword = new Font("SansSerif", Font.BOLD, fontSize);
			comment = new Font("SansSerif", Font.ITALIC, fontSize);
			lineNo = new Font("Monospaced", Font.PLAIN, fontSize);
		}
		g.setColor(Color.black);
		g.setFont(normal);
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
		String output = TextFormatter.rightJustifyNumber(index + 1, 5) + ":  ";
		g.setFont(lineNo);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(output, x, y);

		x = x + fm.stringWidth(output);

		//  Decide next part
		if (line.length() < 1) {
			return;
		}

		if (line.charAt(0) == '#') {
			g.setFont(comment);
			g.drawString(line, x, y);
			return;
		}

		int equalsIndex = line.indexOf('=');
		if (equalsIndex > 0) {
			String keywordString = line.substring(0, equalsIndex);
			String valueString = line.substring(equalsIndex + 1);

			g.setFont(keyword);
			g.drawString(keywordString, x, y);
			fm = g.getFontMetrics();
			x = x + fm.stringWidth(keywordString);

			g.setColor(Color.gray);
			g.drawString("=", x, y);
			x = x + fm.stringWidth("=");

			g.setFont(normal);
			g.setColor(Color.black);
			g.drawString(valueString, x, y);
			return;
		}

		g.drawString(line, x, y);
	}
}

