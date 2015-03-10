/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.acm.seguin.uml.line.DragPanelAdapter;
import org.acm.seguin.uml.line.SizableLabel;

/**
 *  Holds the roles associated with a type
 *
 *@author     Chris Seguin
 *@created    September 30, 1999
 */
public class RoleHolder extends JPanel {
	private LinkedList labels;
	private int wide;
	private int high;
	private UMLMouseAdapter popupMenuListener;
	private DragPanelAdapter panelDragAdapter;


	/**
	 *  Constructor for the RoleHolder object
	 *
	 *@param  popupMenuListener  listener that launches the popup menu
	 *@param  panelDragAdapter   listener that drags the type
	 */
	public RoleHolder(UMLMouseAdapter popupMenuListener, DragPanelAdapter panelDragAdapter) {
		setLayout(null);
		labels = new LinkedList();
		wide = 0;
		high = 0;
		this.popupMenuListener = popupMenuListener;
		this.panelDragAdapter = panelDragAdapter;
	}


	/**
	 *  Gets the preferred size
	 *
	 *@return    the preferred size for this object
	 */
	public Dimension getPreferredSize() {
		return new Dimension(wide, high);
	}


	/**
	 *  Gets the minimum size
	 *
	 *@return    The minimum size for this object
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}


	/**
	 *  Adds a role
	 *
	 *@param  msg  the role name
	 */
	public void add(String msg) {
		SizableLabel roleLabel = new SizableLabel(msg);
		roleLabel.setSLFont(UMLLine.defaultFont);
		roleLabel.setSLHorizontalAlignment(JLabel.CENTER);
		roleLabel.setLocation(0, high);
		add(roleLabel);

		Dimension dim = roleLabel.getPreferredSize();
		roleLabel.setSize(dim);
		wide = Math.max(wide, dim.width);
		high = high + dim.height;

		roleLabel.addMouseListener(popupMenuListener);
		roleLabel.addMouseListener(panelDragAdapter);
		roleLabel.addMouseMotionListener(panelDragAdapter);

		labels.add(roleLabel);
	}


	/**
	 *  Determines if there are any roles
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean hasAny() {
		return (high > 0);
	}


	/**
	 *  Reset width
	 *
	 *@param  newWidth  the new width
	 */
	public void resetWidth(int newWidth) {
		Dimension temp = getPreferredSize();
		temp.width = newWidth;
		setSize(temp);

		Iterator iter = labels.iterator();
		while (iter.hasNext()) {
			SizableLabel next = (SizableLabel) iter.next();
			temp = next.getPreferredSize();
			temp.width = newWidth;
			next.setSize(temp);
		}
	}


	/**
	 *  Print the roles
	 *
	 *@param  g  Description of Parameter
	 *@param  x  Description of Parameter
	 *@param  y  Description of Parameter
	 */
	public void print(Graphics g, int x, int y) {
		Rectangle bounds = getBounds();
		g.setFont(UMLLine.defaultFont);
		FontMetrics fm = g.getFontMetrics();

		Iterator iter = labels.iterator();
		while (iter.hasNext()) {
			SizableLabel roleLabel = (SizableLabel) iter.next();
			Point pt = roleLabel.getLocation();
			roleLabel.print(g, x + pt.x, y + pt.y);
		}
	}


	/**
	 *  Sets the scaling factor
	 *
	 *@param  value  scaling factor
	 */
	public void scale(double value) {
		Iterator iter = labels.iterator();
		while (iter.hasNext()) {
			SizableLabel roleLabel = (SizableLabel) iter.next();
			roleLabel.scale(value);
		}
	}

}
