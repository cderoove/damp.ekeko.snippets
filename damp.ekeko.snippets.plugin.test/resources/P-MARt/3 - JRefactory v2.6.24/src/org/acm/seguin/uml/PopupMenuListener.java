/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *  Create a mouse listener for a method or a field or a title
 *
 *@author     Chris Seguin
 *@created    July 7, 1999
 */
public class PopupMenuListener extends MouseAdapter implements ActionListener {
	//  Instance Variables
	private JPopupMenu menu;
	private JMenuItem menuItem;


	/**
	 *  Constructor for the PopupMenuListener object
	 *
	 *@param  initMenu  Description of Parameter
	 *@param  initItem  Description of Parameter
	 */
	public PopupMenuListener(JPopupMenu initMenu, JMenuItem initItem) {
		menu = initMenu;
		menuItem = initItem;
	}


	/**
	 *  A menu item has been selected
	 *
	 *@param  evt   Description of Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
		if (menuItem == null) {
			return;
		}

		if (menuItem instanceof JMenu) {
			//  Do nothing
		}
		else {
			menu.setVisible(false);
		}
	}


	/**
	 *  A menu item has been selected
	 *
	 *@param  mevt  mouse event
	 */
	public void mouseEntered(MouseEvent mevt) {
		menuItem.setSelected(true);
	}


	/**
	 *  A menu item has been selected
	 *
	 *@param  mevt  mouse event
	 */
	public void mouseExited(MouseEvent mevt) {
		menuItem.setSelected(false);
	}
}
