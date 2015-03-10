/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.text;

import java.awt.Graphics;

import org.acm.seguin.util.TextFormatter;

/**
 *  Prints a single line 
 *
 *@author    Chris Seguin 
 */
public class NumberedLinePrinter extends LinePrinter {
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
		String output = TextFormatter.rightJustifyNumber(index + 1, 5) + ":  " + line;
		g.drawString(output, x, y);
	}
}

