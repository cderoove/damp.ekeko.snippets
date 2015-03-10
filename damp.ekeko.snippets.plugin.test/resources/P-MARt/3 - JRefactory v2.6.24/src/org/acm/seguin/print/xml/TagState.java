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

/**
 *  State pattern that is used to print the XML file
 *
 *@author    Chris Seguin
 */
public class TagState extends State {
	private static State state = null;


	/**
	 *  Gets the Font attribute of the State object
	 *
	 *@return    The Font value
	 */
	public Font getFont() {
		if (font == null) {
			font = new Font("SansSerif", Font.BOLD, getFontSize());
		}
		return font;
	}


	/**
	 *  The actual worker method that processes the line. This is what is defined
	 *  by the various states
	 *
	 *@param  line   the line
	 *@param  index  the index of the character
	 *@param  buf    the buffer
	 *@return        the state at the end of the line
	 */
	protected State processLine(String line, int index,
			StringBuffer buf) {
		State nextState = null;
		int length = line.length();

		while (nextState == null) {
			if (index == length) {
				print(buf);
				return this;
			}

			if (line.charAt(index) == ' ') {
				nextState = AttributeState.getState();
			}
			else if ((length == index + 1) &&
					(line.charAt(index) == '>')) {
				nextState = TextState.getState();
			}
			else if ((length != index + 1) &&
					(line.charAt(index) == '>') &&
					(line.charAt(index + 1) != '<')) {
				nextState = TextState.getState();
			}
			buf.append(line.charAt(index));
			index++;
		}

		print(buf);
		initState(nextState);
		buf.setLength(0);
		return nextState.processLine(line, index, buf);
	}


	/**
	 *  Gets the State attribute of the TagState class
	 *
	 *@return    The State value
	 */
	public static State getState() {
		if (state == null) {
			state = new TagState();
		}
		return state;
	}
}
