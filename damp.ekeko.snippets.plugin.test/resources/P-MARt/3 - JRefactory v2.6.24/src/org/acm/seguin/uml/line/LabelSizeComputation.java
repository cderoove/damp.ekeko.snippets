/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *  Class responsible for computing the size of a label
 *
 *@author    Chris Seguin
 */
public class LabelSizeComputation {
	private Graphics g;
	private static LabelSizeComputation singleton;


	/**
	 *  Constructor for the LabelSizeComputation object
	 */
	private LabelSizeComputation() {
		BufferedImage doubleBuffer = new BufferedImage(300, 25, BufferedImage.TYPE_INT_RGB);
		g = doubleBuffer.getGraphics();
	}


	/**
	 *  Computes the size of a piece of text given a font
	 *
	 *@param  text  the text
	 *@param  font  the font
	 *@return       information about the size of the text
	 */
	public TextInfo compute(String text, Font font) {
		TextInfo result = new TextInfo();

		//  Determine the appropriate size
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		result.height = Math.max(1, fm.getHeight());
		if (text != null) {
			result.width = Math.max(1, fm.stringWidth(text));
		}
		else {
			result.width = 1;
		}

		result.ascent = fm.getAscent();

		return result;
	}


	/**
	 *  Computes the size of a piece of text given a font
	 *
	 *@param  text  the text
	 *@param  font  the font
	 *@return       information about the size of the text
	 */
	public int computeHeight(String text, Font font) {
		TextInfo ti = compute(text, font);
		return ti.height;
	}


	/**
	 *  Factory method for this object
	 *
	 *@return    Creates a single instance of this object
	 */
	public static LabelSizeComputation get() {
		if (singleton == null) {
			init();
		}

		return singleton;
	}


	/**
	 *  Initializer for the singleton
	 */
	private static synchronized void init() {
		if (singleton == null) {
			singleton = new LabelSizeComputation();
		}
	}
}
