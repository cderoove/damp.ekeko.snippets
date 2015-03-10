/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.uml.PopupMenuListener;

/**
 *  Creates a listener that will view a dialog box and do whatever the dialog 
 *  box says. 
 *
 *@author    Chris Seguin 
 */
public abstract class DialogViewListener extends PopupMenuListener {
	/**
	 *  Constructor for the DialogViewListener object 
	 *
	 *@param  initMenu  The popup menu 
	 *@param  initItem  The current item 
	 */
	public DialogViewListener(JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
	}


	/**
	 *  A menu item has been selected, display the dialog box 
	 *
	 *@param  evt  the action event 
	 */
	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt);
		createDialog().show();
	}


	/**
	 *  Creates an appropriate dialog to prompt the user for additional input 
	 *
	 *@return    the dialog box 
	 */
	protected abstract JDialog createDialog();
}
