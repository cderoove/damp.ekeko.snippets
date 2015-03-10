/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.LayoutManager;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

/**
 *  A panel that contains segmented lineList
 *
 *@author     Chris Seguin
 *@created    August 6, 1999
 */
public abstract class LinedPanel extends JPanel {
	private LinkedList endPoints;
	private LinkedList lineList;


	/**
	 *  Constructor for the LinedPanel object
	 */
	public LinedPanel() {
		endPoints = new LinkedList();
		lineList = new LinkedList();
	}


	/**
	 *  Constructor for the LinedPanel object
	 *
	 *@param  doubleBuffered  Description of Parameter
	 */
	public LinedPanel(boolean doubleBuffered) {
		super(doubleBuffered);
		endPoints = new LinkedList();
		lineList = new LinkedList();
	}


	/**
	 *  Constructor for the LinedPanel object
	 *
	 *@param  layout  Description of Parameter
	 */
	public LinedPanel(LayoutManager layout) {
		super(layout);
		endPoints = new LinkedList();
		lineList = new LinkedList();
	}


	/**
	 *  Constructor for the LinedPanel object
	 *
	 *@param  layout          Description of Parameter
	 *@param  doubleBuffered  Description of Parameter
	 */
	public LinedPanel(LayoutManager layout, boolean doubleBuffered) {
		super(layout, doubleBuffered);
		endPoints = new LinkedList();
		lineList = new LinkedList();
	}


	/**
	 *  Sends a message to determine if any line has been hit
	 *
	 *@param  point  The point that the mouse is at
	 */
	public abstract void hit(Point point);


	/**
	 *  Dragging action
	 *
	 *@param  point  The mouse's current position
	 */
	public abstract void drag(Point point);


	/**
	 *  User dropped an item
	 */
	public abstract void drop();


	/**
	 *  Description of the Method
	 *
	 *@param  panel  Description of Parameter
	 */
	public void add(EndPointPanel panel) {
		endPoints.add(panel);
		super.add(panel);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 */
	public void add(SegmentedLine value) {
		lineList.add(value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 */
	public void scale(double value) {
		Iterator iter = endPoints.iterator();
		while (iter.hasNext()) {
			((EndPointPanel) iter.next()).scale(value);
		}

		iter = lineList.iterator();
		while (iter.hasNext()) {
			((SegmentedLine) iter.next()).scale(value);
		}
	}


	/**
	 *  Shifts the components of this lined panel
	 *
	 *@param  x  the amount to shift in the X axis
	 *@param  y  the amount to shift in the Y axis
	 */
	public void shift(int x, int y) {
		Iterator iter = endPoints.iterator();
		while (iter.hasNext()) {
			EndPointPanel next = (EndPointPanel) iter.next();
			if (next.isSelected()) {
				next.shift(x, y);
			}
		}

		iter = lineList.iterator();
		while (iter.hasNext()) {
			SegmentedLine next = (SegmentedLine) iter.next();
			if (next.isBothEndsSelected()) {
				next.shift(x, y);
			}
		}

		repaint();
	}


	/**
	 *  Removes all the lihnes and classes from this diagram
	 */
	public void clear() {
		endPoints.clear();
		lineList.clear();
	}


	/**
	 *  Deselects all the end points
	 */
	public void deselectAll() {
		Iterator iter = getEndPoints();
		while (iter.hasNext()) {
			((EndPointPanel) iter.next()).setSelected(false);
		}
	}


	/**
	 *  Returns a list of lines
	 *
	 *@return    the lines
	 */
	protected Iterator getLines() {
		return lineList.iterator();
	}


	/**
	 *  Return the list of panels
	 *
	 *@return    the panels
	 */
	protected Iterator getEndPoints() {
		return endPoints.iterator();
	}
}
