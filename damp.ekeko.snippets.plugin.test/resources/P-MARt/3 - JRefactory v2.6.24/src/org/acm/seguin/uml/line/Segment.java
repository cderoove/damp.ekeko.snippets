/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *  Segment
 *
 *@author     Chris Seguin
 *@created    July 28, 1999
 */
class Segment {
	/*<Instance Variables>*/
	private double A0;
	private double A1;
	private double B0;
	private double B1;
	private double m;


	/*</Instance Variables>*/

	/*<Constructors>*/
	/**
	 *  Constructor for the Segment object
	 */
	public Segment() {
		A0 = 0;
		A1 = 1;
		B0 = 0;
		B1 = 1;

		m = 1.414;
	}


	/*</Setters>*/

	/*<Getters>*/
	/**
	 *  Gets the Point attribute of the Segment object
	 *
	 *@param  t  Description of Parameter
	 *@return    The Point value
	 */
	public Point getPoint(double t) {
		return new Point((int) (A0 + t * A1), (int) (B0 + t * B1));
	}


	/**
	 *  Gets the ParamFromDistance attribute of the Segment object
	 *
	 *@param  dist  Description of Parameter
	 *@return       The ParamFromDistance value
	 */
	public double getParamFromDistance(double dist) {
		if (dist > m) {
			return (m - dist);
		}
		else {
			return dist / m;
		}
	}


	/*</Constructors>*/

	/*<Setters>*/
	/**
	 *  Description of the Method
	 *
	 *@param  left   Description of Parameter
	 *@param  right  Description of Parameter
	 */
	public void reset(Rectangle left, Rectangle right) {
		double X0 = left.getX() + left.getWidth() * 0.5;
		double Y0 = left.getY() + left.getHeight() * 0.5;

		double X1 = right.getX() + right.getWidth() * 0.5;
		double Y1 = right.getY() + right.getHeight() * 0.5;

		A0 = X0;
		A1 = X1 - X0;

		B0 = Y0;
		B1 = Y1 - Y0;

		m = Math.sqrt(A1 * A1 + B1 * B1);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  left   Description of Parameter
	 *@param  right  Description of Parameter
	 */
	public void reset(Point left, Point right) {
		double X0 = left.getX();
		double Y0 = left.getY();

		double X1 = right.getX();
		double Y1 = right.getY();

		A0 = X0;
		A1 = X1 - X0;

		B0 = Y0;
		B1 = Y1 - Y0;

		m = Math.sqrt(A1 * A1 + B1 * B1);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  left   Description of Parameter
	 *@param  right  Description of Parameter
	 */
	public void reset(Rectangle left, Point right) {
		double X0 = left.getX() + left.getWidth() * 0.5;
		double Y0 = left.getY() + left.getHeight() * 0.5;

		double X1 = right.getX();
		double Y1 = right.getY();

		A0 = X0;
		A1 = X1 - X0;

		B0 = Y0;
		B1 = Y1 - Y0;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  left   Description of Parameter
	 *@param  right  Description of Parameter
	 */
	public void reset(Point left, Rectangle right) {
		double X0 = left.getX();
		double Y0 = left.getY();

		double X1 = right.getX() + right.getWidth() * 0.5;
		double Y1 = right.getY() + right.getHeight() * 0.5;

		A0 = X0;
		A1 = X1 - X0;

		B0 = Y0;
		B1 = Y1 - Y0;

		m = Math.sqrt(A1 * A1 + B1 * B1);
	}


	/**
	 *  Intersects the rectangle with the segment
	 *
	 *@param  rect  The rectangle
	 *@return       The parameter of the location on the line, -1 if it does not
	 *      intersect
	 */
	public double intersect(Rectangle rect) {
		double left = rect.getX();
		double right = rect.getX() + rect.getWidth();
		double top = rect.getY();
		double bottom = rect.getY() + rect.getHeight();

		double leftSide = -1;
		double rightSide = -1;
		if (Math.abs(A1) > 0.0001) {
			leftSide = (left - A0) / A1;
			rightSide = (right - A0) / A1;
		}
		double topSide = -1;
		double bottomSide = -1;
		if (Math.abs(B1) > 0.001) {
			topSide = (top - B0) / B1;
			bottomSide = (bottom - B0) / B1;
		}

		if (inRectangle(leftSide, rect)) {
			return leftSide;
		}
		else if (inRectangle(rightSide, rect)) {
			return rightSide;
		}
		else if (inRectangle(topSide, rect)) {
			return topSide;
		}
		else if (inRectangle(bottomSide, rect)) {
			return bottomSide;
		}

		return -1;
	}


	/**
	 *  Determines the distance between a point and a segment
	 *
	 *@param  point  The point
	 *@return        Returns the distance between the segment and the point or -1
	 *      if the point is closer to an end
	 */
	public double distanceToPoint(Point point) {
		double vX1 = A1;
		double vY1 = B1;
		double vX2 = point.getX() - A0;
		double vY2 = point.getY() - B0;

		double magV2Square = vX2 * vX2 + vY2 * vY2;
		double magV1Square = vX1 * vX1 + vY1 * vY1;

		double dotProduct = vX1 * vX2 + vY1 * vY2;

		if ((dotProduct < 0) || (dotProduct > magV1Square)) {
			return -1.0;
		}

		double dist = Math.sqrt(magV2Square - dotProduct * dotProduct / magV1Square);

		return dist;
	}


	/**
	 *  Returns a parameter on the line that is a given distance from the end of
	 *  the segment
	 *
	 *@param  desiredDistance  the desired distance
	 *@return                  the parameter
	 */
	public double findFromEnd(double desiredDistance) {
		double p = Math.sqrt(A1 * A1 + B1 * B1);
		return 1 - desiredDistance / p;
	}


	/**
	 *  Returns a point above the line
	 *
	 *@param  t     Description of Parameter
	 *@param  dist  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Point aboveLine(double t, double dist) {
		if (Math.abs(A1) < 0.001) {
			Point temp = getPoint(t);
			return new Point((int) (temp.getX() - dist), (int) temp.getY());
		}

		Point p4 = getPoint(t);
		double x4 = p4.getX();
		double y4 = p4.getY();

		double a = B1 * B1 + A1 * A1;
		double b = -2 * y4 * a;
		double c = y4 * y4 * a - A1 * A1 * dist * dist;

		double y5 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		double x5 = -B1 * (y5 - y4) / A1 + x4;

		return new Point((int) x5, (int) y5);
	}


	/**
	 *  Returns a point below the line
	 *
	 *@param  t     Description of Parameter
	 *@param  dist  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Point belowLine(double t, double dist) {
		if (Math.abs(A1) < 0.001) {
			Point temp = getPoint(t);
			return new Point((int) (temp.getX() + dist), (int) temp.getY());
		}

		Point p4 = getPoint(t);
		double x4 = p4.getX();
		double y4 = p4.getY();

		double a = B1 * B1 + A1 * A1;
		double b = -2 * y4 * a;
		double c = y4 * y4 * a - A1 * A1 * dist * dist;

		double y5 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		double x5 = -B1 * (y5 - y4) / A1 + x4;

		return new Point((int) x5, (int) y5);
	}


	/**
	 *  Determines whether a particular point on the line is on the rectangle
	 *
	 *@param  t     the parameter that specifies the point
	 *@param  rect  the rectangle
	 *@return       true if the point is in the rectangle
	 */
	private boolean inRectangle(double t, Rectangle rect) {
		if ((t < 0) || (t > 1)) {
			return false;
		}

		double X0 = A0 + A1 * t;
		double Y0 = B0 + B1 * t;

		double left = rect.getX();
		double right = rect.getX() + rect.getWidth();
		double top = rect.getY();
		double bottom = rect.getY() + rect.getHeight();

		return (left <= X0) && (right >= X0) && (top <= Y0) && (bottom >= Y0);
	}
	/*</Getters>*/
}

