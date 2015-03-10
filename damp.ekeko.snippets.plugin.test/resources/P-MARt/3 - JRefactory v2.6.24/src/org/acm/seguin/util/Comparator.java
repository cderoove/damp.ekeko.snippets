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
 *  Description of the Interface 
 *
 *@author    Chris Seguin 
 */
public interface Comparator {
	/**
	 *  Compares two items and returns -1, 0, or 1 
	 *
	 *@param  obj1  the first item 
	 *@param  obj2  the second item 
	 *@return       the comparision between them 
	 */
	public int compare(Object obj1, Object obj2);
}
