package org.acm.seguin.awt;

import javax.swing.AbstractListModel;
import javax.swing.JList;

/**
 *  Contains a list of items for a list box that can be reordered
 *
 *@author    Chris Seguin
 */
class OrderableListModel extends AbstractListModel {
	private Object[] data;
	private JList list;


	/**
	 *  Constructor for the OrderableListModel object
	 */
	public OrderableListModel() {
	}


	/**
	 *  Sets the Data attribute of the OrderableListModel object
	 *
	 *@param  value  The new Data value
	 */
	public void setData(Object[] value) {
		data = value;
	}


	/**
	 *  Sets the List attribute of the OrderableListModel object
	 *
	 *@param  value  The new List value
	 */
	public void setList(JList value) {
		list = value;
	}


	/**
	 *  Gets the Data attribute of the OrderableListModel object
	 *
	 *@return    The Data value
	 */
	public Object[] getData() {
		return data;
	}


	/**
	 *  Gets the Element At from the data array
	 *
	 *@param  index  the index into the array
	 *@return        The ElementAt value
	 */
	public Object getElementAt(int index) {
		return data[index];
	}


	/**
	 *  Gets the Size attribute of the OrderableListModel object
	 *
	 *@return    The Size value
	 */
	public int getSize() {
		return data.length;
	}


	/**
	 *  Swaps two items in the list box
	 *
	 *@param  first   the first one
	 *@param  second  the second one
	 */
	public void swap(int first, int second) {
		Object temp = data[first];
		data[first] = data[second];
		data[second] = temp;
		fireContentsChanged(this,
				Math.min(first, second),
				Math.max(first, second));
	}
}
