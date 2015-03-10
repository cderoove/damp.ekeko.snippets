/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.print;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Places the print operations in a separate thread
 *
 *@author     Chris Seguin
 *@created    August 6, 1999
 */
public class PrintingThread extends Thread {
	private UMLPackage currentPackage;


	/**
	 *  Constructor for the PrintingThread object
	 *
	 *@param  panel  the current package
	 */
	public PrintingThread(UMLPackage panel) {
		currentPackage = panel;
	}


	/**
	 *  This is where the work actually gets done
	 */
	public void run() {
		PrinterJob job = PrinterJob.getPrinterJob();
		Book book = new Book();
		//  Cover Page goes here
		//  Package picture
		UMLPagePrinter umlpp = new UMLPagePrinter(currentPackage);
		PageFormat pf = UMLPagePrinter.getPageFormat(false);
		if (pf == null) {
			pf = UMLPagePrinter.getPageFormat(true);
		}

		int count = umlpp.calculatePageCount(pf);
		book.append(umlpp, pf, count);
		job.setPageable(book);
		if (job.printDialog()) {
			try {
				job.print();
			}
			catch (Throwable ex) {
				ExceptionPrinter.print(ex);
			}
		}
	}
}
