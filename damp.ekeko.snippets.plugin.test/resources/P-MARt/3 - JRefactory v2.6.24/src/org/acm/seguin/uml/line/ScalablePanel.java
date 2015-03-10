package org.acm.seguin.uml.line;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 *  Panel that can be scaled and shifted
 *
 *@author    Chris Seguin
 */
abstract class ScalablePanel extends JPanel {
	private double scale;
	private boolean inShapeChange;
	private int absoluteWidth = -1;
	private int absoluteHeight = -1;
	private boolean inScaling = false;
	private int absoluteX = -1;
	private int absoluteY = -1;


	/**
	 *  Constructor for the ScalablePanel object
	 */
	public ScalablePanel() {
		init();
	}


	/**
	 *  Constructor for the ScalablePanel object
	 *
	 *@param  doubleBuffered  Description of Parameter
	 */
	public ScalablePanel(boolean doubleBuffered) {
		super(doubleBuffered);
		init();
	}


	/**
	 *  Constructor for the ScalablePanel object
	 *
	 *@param  layout  Description of Parameter
	 */
	public ScalablePanel(LayoutManager layout) {
		super(layout);
		init();
	}


	/**
	 *  Constructor for the ScalablePanel object
	 *
	 *@param  layout          Description of Parameter
	 *@param  doubleBuffered  Description of Parameter
	 */
	public ScalablePanel(LayoutManager layout, boolean doubleBuffered) {
		super(layout, doubleBuffered);
		init();
	}


	/**
	 *  Sets the Location attribute of the ScalablePanel object
	 *
	 *@param  x  The new Location value
	 *@param  y  The new Location value
	 */
	public void setLocation(int x, int y) {
		if (inShapeChange) {
			super.setLocation(x, y);
		}
		else {
			inShapeChange = true;
			super.setLocation(scaleInteger(x), scaleInteger(y));
			inShapeChange = false;
		}
	}


	/**
	 *  Sets the Location attribute of the ScalablePanel object
	 *
	 *@param  pt  The new Location value
	 */
	public void setLocation(Point pt) {
		if (inShapeChange) {
			super.setLocation(pt);
		}
		else {
			inShapeChange = true;
			super.setLocation(scaleInteger(pt.x), scaleInteger(pt.y));
			inShapeChange = false;
		}
	}


	/**
	 *  Sets the Size attribute of the ScalablePanel object
	 *
	 *@param  w  The new Size value
	 *@param  h  The new Size value
	 */
	public void setSize(int w, int h) {
		if (inShapeChange) {
			super.setSize(w, h);
		}
		else {
			inShapeChange = true;
			super.setSize(scaleInteger(w), scaleInteger(h));
			inShapeChange = false;
		}
	}


	/**
	 *  Sets the Size attribute of the ScalablePanel object
	 *
	 *@param  dim  The new Size value
	 */
	public void setSize(Dimension dim) {
		if (inShapeChange) {
			super.setSize(dim);
		}
		else {
			inShapeChange = true;
			super.setSize(scaleInteger(dim.width), scaleInteger(dim.height));
			inShapeChange = false;
		}
	}


	/**
	 *  Sets the Bounds attribute of the ScalablePanel object
	 *
	 *@param  x  The new Bounds value
	 *@param  y  The new Bounds value
	 *@param  w  The new Bounds value
	 *@param  h  The new Bounds value
	 */
	public void setBounds(int x, int y, int w, int h) {
		if (inShapeChange) {
			super.setBounds(x, y, w, h);
		}
		else {
			inShapeChange = true;
			super.setBounds(scaleInteger(x), scaleInteger(y),
					scaleInteger(h), scaleInteger(h));
			inShapeChange = false;
		}
	}


	/**
	 *  Sets the Bounds attribute of the ScalablePanel object
	 *
	 *@param  rect  The new Bounds value
	 */
	public void setBounds(Rectangle rect) {
		if (inShapeChange) {
			super.setBounds(rect);
		}
		else {
			inShapeChange = true;
			super.setBounds(scaleInteger(rect.x), scaleInteger(rect.y),
					scaleInteger(rect.width), scaleInteger(rect.height));
			inShapeChange = false;
		}
	}


	/**
	 *  This method moves the class diagram around on the screen
	 *
	 *@param  x  the x coordinate
	 *@param  y  the y coordinate
	 */
	public void shift(int x, int y) {
		Point pt = getLocation();
		inShapeChange = true;
		setLocation(x + pt.x, y + pt.y);
		inShapeChange = false;
	}


	/**
	 *  Scales the image
	 *
	 *@param  value  the amount to scale
	 */
	public void scale(double value) {
		if (Math.abs(scale - value) > 0.001) {
			Rectangle rect = getUnscaledBounds();

			scale = value;

			inScaling = true;
			setBounds(rect);
			inScaling = false;
		}
	}


	/**
	 *  Invokes old version of setLocation
	 *
	 *@param  x  Description of Parameter
	 *@param  y  Description of Parameter
	 */
	public void move(int x, int y) {
		if (inShapeChange) {
			super.move(x, y);
		}
		else {
			inShapeChange = true;
			super.move(scaleInteger(x), scaleInteger(y));
			inShapeChange = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  w  Description of Parameter
	 *@param  h  Description of Parameter
	 */
	public void resize(int w, int h) {
		if (inShapeChange) {
			super.resize(w, h);
		}
		else {
			inShapeChange = true;
			super.resize(scaleInteger(w), scaleInteger(h));
			inShapeChange = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dim  Description of Parameter
	 */
	public void resize(Dimension dim) {
		if (inShapeChange) {
			super.resize(dim);
		}
		else {
			inShapeChange = true;
			super.resize(scaleInteger(dim.width), scaleInteger(dim.height));
			inShapeChange = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of Parameter
	 *@param  y  Description of Parameter
	 *@param  w  Description of Parameter
	 *@param  h  Description of Parameter
	 */
	public void reshape(int x, int y, int w, int h) {
		if (inShapeChange) {
			super.reshape(x, y, w, h);
		}
		else {
			inShapeChange = true;
			super.reshape(scaleInteger(x), scaleInteger(y),
					scaleInteger(h), scaleInteger(h));
			inShapeChange = false;
		}
	}


	/**
	 *  Return the scaling factor
	 *
	 *@return    the scaling factor
	 */
	protected double getScale() {
		return scale;
	}


	/**
	 *  Scale the integer
	 *
	 *@param  value  the value to be converted
	 *@return        the scaled version
	 */
	protected int scaleInteger(int value) {
		return (int) Math.round(scale * value);
	}


	/**
	 *  Get the bounds without scaling factors
	 *
	 *@return    the rectangle containing the boundaries
	 */
	private Rectangle getUnscaledBounds() {
		Rectangle rect = getBounds();
		if (Math.abs(scale - 1.0) < 0.01) {
			absoluteX = rect.x;
			absoluteY = rect.y;
			absoluteWidth = rect.width;
			absoluteHeight = rect.height;
		}
		else {
			rect.x = getCoordinate(rect.x, absoluteX);
			rect.y = getCoordinate(rect.y, absoluteY);

			if (absoluteWidth == -1) {
				rect.width = unscaleInteger(rect.width);
				rect.height = unscaleInteger(rect.height);
			}
			else {
				rect.width = absoluteWidth;
				rect.height = absoluteHeight;
			}
		}
		return rect;
	}


	/**
	 *  Convert a coordinate or use an absolute coordinate
	 *
	 *@param  value     the input coordinate
	 *@param  absolute  the absolute coordinate
	 *@return           the correct coordinate
	 */
	private int getCoordinate(int value, int absolute) {
		int scaled = unscaleInteger(value);
		if ((absolute >= 0) && (Math.abs(scaled - absolute) < 6)) {
			scaled = absolute;
		}
		return scaled;
	}


	/**
	 *  Inverse of the scaleInteger operation
	 *
	 *@param  value  the input value
	 *@return        the result of the unscape operation
	 */
	private int unscaleInteger(int value) {
		return (int) (value / scale);
	}


	/**
	 *  Initialize the valuse
	 */
	private void init() {
		inShapeChange = false;
		scale = 1.0;
	}
}
