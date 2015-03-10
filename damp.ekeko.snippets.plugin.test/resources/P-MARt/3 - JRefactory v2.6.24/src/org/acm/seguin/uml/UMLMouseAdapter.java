/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 *  Create a mouse listener for a method or a field or a title
 *
 *@author     Chris Seguin
 *@created    July 7, 1999
 */
public class UMLMouseAdapter extends MouseAdapter {
	//  Instance Variables
	private UMLPackage current;
	private UMLType type;
	private JPanel child;


	/**
	 *  Constructor for the UMLMouseAdapter object
	 *
	 *@param  initType        Type object
	 *@param  initChild       Single line
	 *@param  currentPackage  Description of Parameter
	 */
	public UMLMouseAdapter(UMLPackage currentPackage, UMLType initType, JPanel initChild) {
		current = currentPackage;
		type = initType;
		child = initChild;
	}


	/**
	 *  User has pressed a mouse button
	 *
	 *@param  mevt  the mouse event
	 */
	public void mousePressed(MouseEvent mevt) {
		if ((mevt.getModifiers() & MouseEvent.BUTTON1_MASK) == 0) {
			Point pt;
			UMLPopupMenu upm;
			if (child == null) {
				upm = new UMLPopupMenu(current, type);
				pt = type.getLocationOnScreen();
			}
			else {
				upm = new UMLPopupMenu(current, child);
				pt = child.getLocationOnScreen();
			}
			JPopupMenu menu = upm.getMenu();
			menu.setLocation(mevt.getX() + pt.x, mevt.getY() + pt.y);
			menu.setVisible(true);
		}
	}
}
