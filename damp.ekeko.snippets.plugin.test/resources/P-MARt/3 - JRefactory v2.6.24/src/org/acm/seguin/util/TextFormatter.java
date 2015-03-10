/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.util;

/**
 *  Creates an object that can handle text formatting 
 *
 *@author     Chris Seguin 
 *@created    July 3, 1999 
 */
public class TextFormatter {
	/**
	 *  Create a string that contains a number that is right justified 
	 *
	 *@param  count  the value to print 
	 *@param  size   the size of the buffer 
	 *@return        Description of the Returned Value 
	 */
	public static String rightJustifyNumber(long count, int size) {
		StringBuffer buffer = new StringBuffer(size);
		buffer.append("" + count);

		while (buffer.length() < size) {
			buffer.insert(0, " ");
		}

		return buffer.toString();
	}
}
