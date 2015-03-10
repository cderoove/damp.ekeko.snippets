/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty.sort;

import org.acm.seguin.util.Comparator;

/**
 *  The ordering is the basic tool to determine if the parse
 *  tree node is in the proper order.  This object contains the
 *  base class for the order.
 *
 *@author    Chris Seguin
 */
public abstract class Ordering implements Comparator {
	/**
	 *  Compare two items
	 *
	 *@param  one  the first item
	 *@param  two  the second item
	 *@return      1 if the first item is greater than the second, -1 if the
	 *      first item is less than the second, and 0 otherwise.
	 */
	public int compare(Object one, Object two) {
		int oneIndex = getIndex(one);
		int twoIndex = getIndex(two);

		if (oneIndex > twoIndex) {
			return 1;
		}
		else if (oneIndex < twoIndex) {
			return -1;
		}
		else {
			return 0;
		}
	}


	/**
	 *  Return the index of the item in the order array
	 *
	 *@param  object  the object we are checking
	 *@return         the objects index if it is found or 7 if it is not
	 */
	protected abstract int getIndex(Object object);
}
