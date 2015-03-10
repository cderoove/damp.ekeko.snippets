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
 *  Performs an insertion sort on an array of objects. The objects are sorted 
 *  according to the comparator. 
 *
 *@author     Chris Seguin 
 *@created    July 3, 1999 
 */
public class InsertionSortArray {
	/**
	 *  Insert the array 
	 *
	 *@param  array       The array to sort 
	 *@param  comparator  The object describing their relationships 
	 */
	public void sort(Object[] array, Comparator comparator) {
		for (int ndx = 1; ndx < array.length; ndx++) {
			if (comparator.compare(array[ndx - 1], array[ndx]) > 0) {
				insert(array, ndx, comparator);
			}
		}
	}


	/**
	 *  Insert a particular element into the correct spot in the array 
	 *
	 *@param  array       The array 
	 *@param  index       The index of the element to be moved 
	 *@param  comparator  The order information 
	 */
	private void insert(Object[] array, int index, Comparator comparator) {
		int location = findSpot(array, index, comparator);
		insertAt(array, index, location);
	}


	/**
	 *  Finds the correct location for an item 
	 *
	 *@param  array       The array 
	 *@param  index       The index of the item that needs a spot 
	 *@param  comparator  The object that can compare the results 
	 *@return             The index where the item should be inserted 
	 */
	private int findSpot(Object[] array, int index, Comparator comparator) {
		for (int ndx = 0; ndx < index; ndx++) {
			if (comparator.compare(array[ndx], array[index]) > 0) {
				return ndx;
			}
		}

		return -1;
	}


	/**
	 *  Inserts an item in the array by moving everything over 
	 *
	 *@param  array     The array 
	 *@param  index     The index of the item to be moved 
	 *@param  location  The item's desired location 
	 */
	private void insertAt(Object[] array, int index, int location) {
		//  Remember the item
		Object swap = array[index];

		//  Iterate through the array and shift everything over
		for (int ndx = index; ndx > location; ndx--) {
			array[ndx] = array[ndx - 1];
		}

		//  Put the item in it's place
		array[location] = swap;
	}
}
