package org.acm.seguin.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;

/**
 *  Adapter to move items around in the listbox
 *
 *@author    Chris Seguin
 */
class MoveItemAdapter implements ActionListener {
	private OrderableListModel olm;
	private JList list;
	private int direction;


	/**
	 *  Constructor for the MoveItemAdapter object
	 *
	 *@param  model      the list model
	 *@param  list       the list
	 *@param  direction  the direction of movement
	 */
	public MoveItemAdapter(OrderableListModel model, JList list, int direction) {
		olm = model;
		this.list = list;
		this.direction = direction;
	}


	/**
	 *  Swap the item's on the user's command
	 *
	 *@param  evt  the command to swap
	 */
	public void actionPerformed(ActionEvent evt) {
		int item = list.getSelectedIndex();
		if (item == -1) {
			return;
		}

		int newPos = item + direction;
		if ((newPos < 0) || (newPos >= olm.getSize())) {
			return;
		}

		olm.swap(item, newPos);

		list.setSelectedIndex(newPos);
	}
}
