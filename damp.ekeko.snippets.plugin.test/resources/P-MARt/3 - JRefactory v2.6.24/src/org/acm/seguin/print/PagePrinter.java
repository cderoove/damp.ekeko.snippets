/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.text.DateFormat;
import java.util.Date;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Handles printing the page
 *
 *@author     Chris Seguin
 *@created    August 8, 1999
 */
public abstract class PagePrinter implements Printable {
	private int filenameFontSize = 14;
	private int datePageFontSize = 8;

	/**
	 *  Description of the Field
	 */
	protected static int headerHeight = 30;
	/**
	 *  Description of the Field
	 */
	protected static PageFormat pf;
	private static double scale = 1.0;


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 */
	public void setFilenameFontSize(int value) {
		filenameFontSize = value;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 */
	public void setDatePageCountFontSize(int value) {
		datePageFontSize = value;
	}


	/**
	 *  Prints the header at the top of the page
	 *
	 *@param  g           The graphics object
	 *@param  title       the title
	 *@param  pageNumber  the number of pages
	 *@param  pageCount   the page count
	 */
	protected void printHeader(Graphics g, String title, String pageNumber, String pageCount) {
		//  Draw the frame
		int x = (int) pf.getImageableX();
		int y = (int) pf.getImageableY();
		int wide = (int) pf.getImageableWidth();
		int high = headerHeight;
		g.setColor(Color.white);
		g.fillRect(x, y, wide - 1, high - 1);
		g.setColor(Color.black);
		g.drawRect(x, y, wide - 1, high - 1);
		int quarterWide = wide / 4;
		g.drawLine(x + 2 * quarterWide, y, x + 2 * quarterWide, y + headerHeight - 1);
		g.drawLine(x + 3 * quarterWide, y, x + 3 * quarterWide, y + headerHeight - 1);

		int centerY = y + headerHeight / 2;

		//  Draw the filename
		g.setFont(new Font("Serif", Font.BOLD, filenameFontSize));
		FontMetrics fm = g.getFontMetrics();
		int tempY = y + (headerHeight + fm.getAscent() + fm.getDescent()) / 2 - fm.getDescent();
		if ((title != null) && (title.length() > 0)) {
			g.drawString(title, x + 10, tempY);
		}

		//  Draw the date
		g.setFont(new Font("Serif", Font.BOLD, datePageFontSize));
		fm = g.getFontMetrics();

		String now = DateFormat.getDateTimeInstance().format(new Date());
		tempY = y + (headerHeight + fm.getAscent() + fm.getDescent()) / 2 - fm.getDescent();
		g.drawString(now, x + 5 * quarterWide / 2 - fm.stringWidth(now) / 2, tempY);

		//  Draw the page count
		String pages = pageNumber + " of " + pageCount;
		g.drawString(pages, x + 7 * quarterWide / 2 - fm.stringWidth(pages) / 2, tempY);
	}


	/**
	 *  Sets the size of the header box
	 *
	 *@param  value  The size of the header box
	 */
	public static void setHeaderHeight(int value) {
		headerHeight = value;
	}


	/**
	 *  Returns the page
	 *
	 *@param  dialog  present a dialog screen if none
	 *@return         the current page format
	 */
	public static PageFormat getPageFormat(boolean dialog) {
		if (dialog) {
			PrinterJob job = PrinterJob.getPrinterJob();
			pf = job.pageDialog(job.defaultPage());
		}

		//  Get the header height
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "printing");
			setHeaderHeight(Integer.parseInt(bundle.getString("header.space")));
		}
		catch (MissingSettingsException mre) {
			ExceptionPrinter.print(mre);
		}
		catch (NumberFormatException inf) {
			ExceptionPrinter.print(inf);
		}

		return pf;
	}


	/**
	 *  Return the width of the page
	 *
	 *@return    Description of the Returned Value
	 */
	public static int getPageWidth() {
		if (pf == null) {
			return -1;
		}

		return (int) (pf.getImageableWidth() / scale);
	}


	/**
	 *  Return the width of the page
	 *
	 *@return    Description of the Returned Value
	 */
	public static int getPageHeight() {
		if (pf == null) {
			return -1;
		}

		return (int) (pf.getImageableHeight() / scale);
	}


	/**
	 *  Sets the scaling
	 *
	 *@param  value  the scaled value
	 */
	protected static void setScale(double value) {
		scale = value;
	}


	/**
	 *  Returns the scaling
	 *
	 *@return    The scale size
	 */
	protected static double getScale() {
		return scale;
	}
}
