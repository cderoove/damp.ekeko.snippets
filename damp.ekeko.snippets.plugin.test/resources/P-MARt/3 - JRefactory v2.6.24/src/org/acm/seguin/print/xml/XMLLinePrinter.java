/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.xml;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;

import org.acm.seguin.print.text.LinePrinter;
import org.acm.seguin.print.text.LineSet;
import org.acm.seguin.util.TextFormatter;

/**
 *  Prints a single line
 *
 *@author    Chris Seguin
 */
public class XMLLinePrinter extends LinePrinter {
	private ArrayList list;
	private int fontSize;
	private Font lineNo;


	/**
	 *  Constructor for the JavaLinePrinter object
	 */
	public XMLLinePrinter() {
		list = new ArrayList();
		fontSize = -1;
	}


	/**
	 *  Sets the FontSize attribute of the LinePrinter object
	 *
	 *@param  value  The new FontSize value
	 */
	public void setFontSize(int value) {
		if (value != fontSize) {
			fontSize = value;
			lineNo = new Font("Monospaced", Font.PLAIN, fontSize);
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

		g.setFont(lineNo);
		FontMetrics fm = g.getFontMetrics();
		return fm.getHeight();
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
		State state;

		if (index == 0) {
			list.add(0, TextState.getState());
		}

		state = (State) list.get(index);

		if (line.length() == 0) {
			list.add(index + 1, state);
			return;
		}

		if (state instanceof TextState) {
			if (line.charAt(0) == '<')
			state = TagState.getState();
		}

		String output = TextFormatter.rightJustifyNumber(index + 1, 5) + ":  ";
		g.setFont(lineNo);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(output, x, y);

		state.setGraphics(g);
		state.setX(x + fm.stringWidth(output));
		state.setY(y);
		state.setFontSize(fontSize);

		list.add(index + 1, state.processLine(line));
	}
}

