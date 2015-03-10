/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.Graphics;
import java.awt.Point;

/**
 *  InheretenceRelationship
 *
 *@author     Chris Seguin
 *@created    August 1, 1999
 */
public class InheretenceRelationship extends SegmentedLine {
	/**
	 *  Constructor for the InheretenceRelationship object
	 *
	 *@param  start  Description of Parameter
	 *@param  end    Description of Parameter
	 */
	public InheretenceRelationship(EndPointPanel start, EndPointPanel end) {
		super(start, end);
	}


	/**
	 *  Draws the segmented line
	 *
	 *@param  g  Description of Parameter
	 */
	public void paint(Graphics g) {
		super.paint(g);
	}


	/**
	 *  Draws the arrow and the last segment
	 *
	 *@param  g  the graphics object
	 */
	protected void drawArrow(Graphics g) {
		Point shortPt = getShortPoint();

		int last = vertices.length;
		double X0 = vertices[last - 2].getPoint().getX();
		double Y0 = vertices[last - 2].getPoint().getY();
		g.drawLine((int) X0, (int) Y0, (int) shortPt.getX(), (int) shortPt.getY());

		Point end = vertices[last - 1].getPoint();
		Point above = getArrowPointAbove();
		Point below = getArrowPointBelow();

		Xs[0] = (int) end.getX();
		Xs[1] = (int) above.getX();
		Xs[2] = (int) below.getX();
		Xs[3] = (int) end.getX();

		Ys[0] = (int) end.getY();
		Ys[1] = (int) above.getY();
		Ys[2] = (int) below.getY();
		Ys[3] = (int) end.getY();

		g.drawPolyline(Xs, Ys, 4);
	}
}
