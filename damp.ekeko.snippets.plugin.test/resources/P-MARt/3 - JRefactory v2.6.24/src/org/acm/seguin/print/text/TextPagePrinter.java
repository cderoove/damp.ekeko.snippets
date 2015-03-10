/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.text;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import org.acm.seguin.print.PagePrinter;
import org.acm.seguin.print.PrintingSettings;

/**
 *  Handles printing the page 
 *
 *@author     Chris Seguin 
 *@created    August 6, 1999 
 */
public class TextPagePrinter extends PagePrinter {
	private String filename;
	private LineSet lineSet;
	private LinePrinter linePrinter;
	private int textFontSize = 10;
	private int textSkip = 2;

	private static int linesPerPage = -1;


	/**
	 *  Constructor for the UMLPagePrinter object 
	 *
	 *@param  initFilename  Description of Parameter 
	 *@param  init          Description of Parameter 
	 *@param  printer       Description of Parameter 
	 */
	public TextPagePrinter(String initFilename, String init, LinePrinter printer) {
		lineSet = new LineSet(init);
		linePrinter = printer;
		filename = initFilename;
	}


	/**
	 *  Sets the TextFontSize attribute of the TextPagePrinter object 
	 *
	 *@param  value  The new TextFontSize value 
	 */
	public void setTextFontSize(int value) {
		textFontSize = value;
	}


	/**
	 *  Sets the BetweenLineSpacing attribute of the TextPagePrinter object 
	 *
	 *@param  value  The new BetweenLineSpacing value 
	 */
	public void setBetweenLineSpacing(int value) {
		textSkip = value;
	}


	/**
	 *  Guess the number of pages 
	 *
	 *@param  pf  Description of Parameter 
	 *@return     Description of the Returned Value 
	 */
	public int calculatePageCount(PageFormat pf) {
		int pageHeight = (int) pf.getImageableHeight();
		int pageWidth = (int) pf.getImageableWidth();

		int pagesHigh;
		int lpp = linesPerPage;
		int lineCount = lineSet.size();
		if (linesPerPage == -1) {
			PrintingSettings ps = new PrintingSettings();
			lpp = ps.getLinesPerPage();
		}

		pagesHigh = lineCount / lpp;
		if (lineCount % lpp != 0) {
			pagesHigh++;
		}

		return pagesHigh;
	}


	/**
	 *  Print the page 
	 *
	 *@param  g           the graphics object 
	 *@param  pf          the page format 
	 *@param  pageNumber  the page number 
	 *@return             Whether there is more pages or not 
	 */
	public int print(Graphics g, PageFormat pf, int pageNumber) {
		int pageCount = calculatePageCount(pf);
		if (pageNumber > pageCount) {
			return Printable.NO_SUCH_PAGE;
		}

		linePrinter.setFontSize(textFontSize);
		int high = linePrinter.getLineHeight(g) + textSkip;
		if (linesPerPage == -1) {
			int pageHeight = (int) pf.getImageableHeight() - headerHeight;
			linesPerPage = pageHeight / high;
			PrintingSettings ps = new PrintingSettings();
			ps.setLinesPerPage(linesPerPage);
		}

		int startIndex = pageNumber * linesPerPage;

		int xOffset = (int) pf.getImageableX();
		int yOffset = (int) pf.getImageableY() + headerHeight;

		printHeader(g, filename, "" + (1 + pageNumber), "" + pageCount);

		linePrinter.init(g);
		for (int ndx = 0; ndx < linesPerPage; ndx++) {
			int index = ndx + pageNumber * linesPerPage;
			String line = lineSet.getLine(index);
			if (line == null) {
				break;
			}

			linePrinter.print(g, line, xOffset, yOffset + (1 + ndx) * high, 
					lineSet, index);
		}

		return Printable.PAGE_EXISTS;
	}
}
