/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Icon that draws the protection symbol
 *
 *@author    Chris Seguin
 */
public class ProtectionIcon extends UMLIcon {
	private int protection;
	private int type;

	private final static int CIRCLE = 0;
	private final static int LETTER = 1;


	/**
	 *  Constructor for the ProtectionIcon object
	 *
	 *@param  wide  the size of the icon
	 *@param  high  the size of the icon
	 */
	public ProtectionIcon(int wide, int high)
	{
		super(wide, high);

		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			umlBundle.setContinuallyReload(false);
			String pattern = umlBundle.getString("icon.type");
			if (pattern.equalsIgnoreCase("letter")) {
				type = LETTER;
			}
			else {
				type = CIRCLE;
			}
		}
		catch (MissingSettingsException mse) {
			type = CIRCLE;
		}

	}


	/**
	 *  Sets the Protection attribute of the ProtectionIcon object
	 *
	 *@param  value  The new Protection value
	 */
	public void setProtection(int value)
	{
		protection = value;
	}


	/**
	 *  Draws the icon
	 *
	 *@param  c  The component on which we are drawing
	 *@param  g  The graphics object
	 *@param  x  the x location
	 *@param  y  the y location
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		if (type == LETTER) {
			drawLetterIcon(g, x, y);
		}
		else {
			drawCircleIcon(g, x, y);
		}
	}


	/**
	 *  Draws the protection icon like a circle
	 *
	 *@param  g  Description of Parameter
	 *@param  x  Description of Parameter
	 *@param  y  Description of Parameter
	 */
	private void drawCircleIcon(Graphics g, int x, int y)
	{
		g.setColor(UMLLine.getProtectionColor(protection));

		int wide = Math.max(1, (int) (iconWidth * scale));
		int high = Math.max(1, (int) (iconHeight * scale));
		int margin = (int) (scale);

		g.fillOval(x + margin, y, wide, high);

		if ((wide > 1) && (high > 1)) {
			g.setColor(Color.black);
			g.drawOval(x + margin, y, wide, high);
		}
	}
	/**
	 *  Draws the protection icon like a letter
	 *
	 *@param  g  Description of Parameter
	 *@param  x  Description of Parameter
	 *@param  y  Description of Parameter
	 */
	private void drawLetterIcon(Graphics g, int x, int y)
	{
		g.setColor(Color.black);

		int wide = Math.max(1, (int) (iconWidth * scale));
		int high = Math.max(1, (int) (iconHeight * scale));
		int margin = (int) (scale);

		int halfHigh = high / 2;
		int halfWide = wide / 2;

		if (protection == UMLLine.PUBLIC) {
			g.drawLine(x + margin, y + halfHigh, x + margin + wide, y + halfHigh);
			g.drawLine(x + margin + halfWide, y, x + margin + halfWide, y + high);
		}
		else if (protection == UMLLine.PROTECTED) {
			g.drawLine(x + margin, y + halfHigh + 1, x + margin + wide, y + halfHigh + 1);
			g.drawLine(x + margin, y + halfHigh - 1, x + margin + wide, y + halfHigh - 1);
			g.drawLine(x + margin + halfWide + 1, y, x + margin + halfWide + 1, y + high);
			g.drawLine(x + margin + halfWide - 1, y, x + margin + halfWide - 1, y + high);
		}
		else if (protection == UMLLine.DEFAULT) {
			g.drawLine(x + margin, y + halfHigh + 1, x + margin + wide, y + halfHigh + 1);
			g.drawLine(x + margin, y + halfHigh - 1, x + margin + wide, y + halfHigh - 1);
		}
		else if (protection == UMLLine.PRIVATE) {
			g.drawLine(x + margin, y + halfHigh, x + margin + wide, y + halfHigh);
		}
	}
}
