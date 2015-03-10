/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.print;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import org.acm.seguin.print.PagePrinter;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Handles printing the page
 *
 *@author     Chris Seguin
 *@created    August 6, 1999
 */
public class UMLPagePrinter extends PagePrinter {
	private UMLPackage currentPackage;


	/**
	 *  Constructor for the UMLPagePrinter object
	 *
	 *@param  panel  the current package
	 */
	public UMLPagePrinter(UMLPackage panel) {
		currentPackage = panel;
	}


	/**
	 *  Guess the number of pages
	 *
	 *@param  pf  Description of Parameter
	 *@return     Description of the Returned Value
	 */
	public int calculatePageCount(PageFormat pf) {
		Dimension size = currentPackage.getPreferredSize();
		int pageHeight = (int) pf.getImageableHeight() - headerHeight;
		int pageWidth = (int) pf.getImageableWidth();

		int pagesWide = (int) (1 + getScale() * size.width / pageWidth);
		int pagesHigh = (int) (1 + getScale() * size.height / pageHeight);

		return pagesWide * pagesHigh;
	}


	/**
	 *  Print the page
	 *
	 *@param  g           the graphics object
	 *@param  pf          the page format
	 *@param  pageNumber  the page number
	 *@return             Description of the Returned Value
	 */
	public int print(Graphics g, PageFormat pf, int pageNumber) {
		Dimension size = currentPackage.getPreferredSize();
		int pageHeight = (int) pf.getImageableHeight() - headerHeight;
		int pageWidth = (int) pf.getImageableWidth();

		int pagesWide = (int) (1 + getScale() * size.width / pageWidth);
		int pagesHigh = (int) (1 + getScale() * size.height / pageHeight);
		if (pageNumber > pagesWide * pagesHigh) {
			return Printable.NO_SUCH_PAGE;
		}

		int row = pageNumber / pagesWide;
		int col = pageNumber % pagesWide;

		/*
		 * if (panelBuffer == null) {
		 * panelBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		 * Graphics tempGraphics = panelBuffer.getGraphics();
		 * tempGraphics.setColor(Color.white);
		 * tempGraphics.fillRect(0, 0, size.width, size.height);
		 * currentPackage.print(tempGraphics, 0, 0);
		 * }
		 * g.drawImage(panelBuffer,
		 * ((int) pf.getImageableX()) - col * pageWidth,
		 * ((int) pf.getImageableY())- row * pageHeight, null);
		 */

		((Graphics2D) g).translate(pf.getImageableX() - col * pageWidth, pf.getImageableY() - row * pageHeight + headerHeight);
		((Graphics2D) g).scale(getScale(), getScale());
		currentPackage.print(g, 0, 0);

		((Graphics2D) g).scale(1 / getScale(), 1 / getScale());
		((Graphics2D) g).translate(-(pf.getImageableX() - col * pageWidth),
				-(pf.getImageableY() - row * pageHeight + headerHeight));

		String packageName = currentPackage.getSummary().getName();
		if ((packageName == null) || (packageName.length() == 0)) {
			packageName = "Top Level Package";
		}
		printHeader(g, packageName,
				"(" + (1 + col) + ", " + (1 + row) + ")",
				"(" + pagesWide + ", " + pagesHigh + ")");

		return Printable.PAGE_EXISTS;
	}


	/**
	 *  Returns the page
	 *
	 *@param  dialog  present a dialog screen if none
	 *@return         the current page format
	 */
	public static PageFormat getPageFormat(boolean dialog) {
		PageFormat pf = PagePrinter.getPageFormat(dialog);
		setScale(0.8);
		return pf;
	}


	/**
	 *  Return the width of the page
	 *
	 *@return    Description of the Returned Value
	 */
	public static int getPageHeight() {
		int result = PagePrinter.getPageHeight();
		if (result == -1) {
			return -1;
		}

		return (result - headerHeight);
	}
}
