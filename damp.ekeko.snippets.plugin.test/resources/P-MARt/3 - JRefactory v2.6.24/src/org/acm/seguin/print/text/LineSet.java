/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.text;

import java.util.ArrayList;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class LineSet {
	private ArrayList set;
	private final static int TAB_SIZE = 4;


	/**
	 *  Constructor for the LineSet object
	 *
	 *@param  data  Description of Parameter
	 */
	public LineSet(String data) {
		set = new ArrayList();

		breakLine(data);
	}


	/**
	 *  Gets the Line attribute of the LineSet object
	 *
	 *@param  index  Description of Parameter
	 *@return        The Line value
	 */
	public String getLine(int index) {
		if ((index < 0) || (index >= set.size())) {
			return null;
		}

		return expandTabs((String) set.get(index));
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public int size() {
		return set.size();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  input  Description of Parameter
	 */
	private void breakLine(String input) {
		int last = -1;
		int current = 0;
		int length = input.length();

		while (last < length) {
			while ((current < length) && (input.charAt(current) != '\n')) {
				current++;
			}

			String next = input.substring(last + 1, current);
			set.add(next);
			last = current;
			current++;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  line  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private String expandTabs(String line) {
		StringBuffer buffer = new StringBuffer();
		int last = line.length();
		for (int ndx = 0; ndx < last; ndx++) {
			char ch = line.charAt(ndx);
			if (ch == '\t') {
				int bufferLength = buffer.length();
				int spaces = bufferLength % TAB_SIZE;
				if (spaces == 0) {
					spaces = TAB_SIZE;
				}

				for (int ndx2 = 0; ndx2 < spaces; ndx2++) {
					buffer.append(' ');
				}
			}
			else if ((ch == '\r') || (ch == '\n')) {
				//  Skip this character
			}
			else {
				buffer.append(ch);
			}
		}

		return buffer.toString();
	}
}
