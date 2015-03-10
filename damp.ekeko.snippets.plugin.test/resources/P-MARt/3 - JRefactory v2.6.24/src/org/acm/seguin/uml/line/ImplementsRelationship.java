/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

/**
 *  ImplementsRelationship
 *
 *@author     Chris Seguin
 *@created    August 1, 1999
 */
public class ImplementsRelationship extends SegmentedLine {
	private Stroke dashedStroke;


	/**
	 *  Constructor for the ImplementsRelationship object
	 *
	 *@param  start  Description of Parameter
	 *@param  end    Description of Parameter
	 */
	public ImplementsRelationship(EndPointPanel start, EndPointPanel end) {
		super(start, end);

		float[] pattern = new float[2];
		pattern[0] = 20;
		pattern[1] = 20;
		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 20, pattern, 40);
	}


	/**
	 *  Draw central portion of the segmented line
	 *
	 *@param  g  the graphics object
	 */
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		//  Setup & paint vertices
		for (int ndx = 0; ndx < vertices.length; ndx++) {
			Point p = vertices[ndx].getPoint();
			Xs[ndx] = (int) p.getX();
			Ys[ndx] = (int) p.getY();
			vertices[ndx].paint(g);
		}

		Point shortPoint = getShortPoint();
		Xs[vertices.length - 1] = shortPoint.x;
		Ys[vertices.length - 1] = shortPoint.y;

		//  Use a dashed stroke
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(dashedStroke);

		//  Draw lines
		g.setColor(Color.black);
		g.drawPolyline(Xs, Ys, vertices.length);

		//  Reset the stroke
		g2d.setStroke(stroke);

		//  Draw the arrow head
		drawArrowHead(g);
	}


	/**
	 *  Draws anything special at the start
	 *
	 *@param  g  the graphics object
	 */
	protected void drawStartSegment(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		Stroke stroke = g2d.getStroke();
		g2d.setStroke(dashedStroke);

		super.drawStartSegment(g);

		g2d.setStroke(stroke);
	}


	/**
	 *  Draws the arrow and the last segment
	 *
	 *@param  g  the graphics object
	 */
	private void drawArrowHead(Graphics g) {
		//  Local Variables
		int last = vertices.length;

		//  Get the 3 points of the arrow head
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

		//  Draw the arrow head
		g.drawPolyline(Xs, Ys, 4);
	}

	/**  Scales this type of line */
	public void scale(double factor) {
		float[] pattern = new float[2];
		pattern[0] = (float) (20 * factor);
		pattern[1] = (float) (20 * factor);
		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL,
				(float) (20 * factor),
				pattern,
				(float) (40 * factor));
		super.scale(factor);
	}
}
