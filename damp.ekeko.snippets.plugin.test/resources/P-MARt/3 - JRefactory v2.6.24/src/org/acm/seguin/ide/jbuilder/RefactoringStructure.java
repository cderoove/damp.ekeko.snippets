/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

/**
 *  Structure view that provides access to the refactorings
 *
 *@author    Chris Seguin
 */
public class RefactoringStructure extends TextStructureDelegate
{
	private Object currentNode = null;

	public RefactoringStructure(TextStructure init) {
		super(init);
	}

	/**
	 *  Gets the Popup attribute of the RefactoringStructureView object
	 *
	 *@return    The Popup value
	 */
	public JPopupMenu getPopup()
	{
		JPopupMenu menu = super.getPopup();
		menu.addSeparator();
		menu.add(new JMenu("Refactor"));
		return menu;
	}


	/**
	 *  The mouse was pressed
	 *
	 *@param  evt  the mouse event
	 */
	public void mousePressed(MouseEvent evt)
	{
		TreePath path = getTree().getClosestPathForLocation(evt.getX(), evt.getY());
		currentNode = path.getLastPathComponent();

		System.out.println("Hit:  " + currentNode.toString() +
				"  is a " + currentNode.getClass().getName());
		super.mousePressed(evt);
	}
}
