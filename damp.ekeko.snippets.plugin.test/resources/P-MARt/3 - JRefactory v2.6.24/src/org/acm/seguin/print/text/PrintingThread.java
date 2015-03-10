/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print.text;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Places the print operations in a separate thread
 *
 *@author     Chris Seguin
 *@created    August 6, 1999
 */
public class PrintingThread extends Thread {
	private String data;
	private LinePrinter printer;
	private String filename;


	/**
	 *  Constructor for the PrintingThread object
	 *
	 *@param  filename  Description of Parameter
	 *@param  init      Description of Parameter
	 *@param  printer   Description of Parameter
	 */
	public PrintingThread(String filename, String init, LinePrinter printer) {
		data = init;
		this.printer = printer;
		this.filename = filename;
	}


	/**
	 *  This is where the work actually gets done
	 */
	public void run() {
		PrinterJob job = PrinterJob.getPrinterJob();
		Book book = new Book();
		//  Cover Page goes here
		//  Package picture
		TextPagePrinter textpp = new TextPagePrinter(filename, data, printer);
		loadDefaults(textpp);
		PageFormat pf = TextPagePrinter.getPageFormat(false);
		if (pf == null) {
			pf = TextPagePrinter.getPageFormat(true);
		}

		int count = textpp.calculatePageCount(pf);
		book.append(textpp, pf, count);
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


	/**
	 *  Description of the Method
	 *
	 *@param  textpp  Description of Parameter
	 */
	private void loadDefaults(TextPagePrinter textpp) {
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "printing");
			textpp.setTextFontSize(Integer.parseInt(bundle.getString("text.font.size")));
			textpp.setBetweenLineSpacing(Integer.parseInt(bundle.getString("text.space")));
			textpp.setFilenameFontSize(Integer.parseInt(bundle.getString("filename.font.size")));
			textpp.setDatePageCountFontSize(Integer.parseInt(bundle.getString("date.font.size")));
		}
		catch (MissingSettingsException mre) {
			ExceptionPrinter.print(mre);
		}
		catch (NumberFormatException inf) {
			ExceptionPrinter.print(inf);
		}
	}


	/**
	 *  The main program for the PrintingThread class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		try {
			FileReader in = new FileReader(args[0]);
			BufferedReader input = new BufferedReader(in);
			StringBuffer all = new StringBuffer();

			String line = input.readLine();
			while (line != null) {
				all.append(line);
				all.append("\n");
				line = input.readLine();
			}

			input.close();
			(new PrintingThread(args[0], all.toString(), new LinePrinter())).run();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}
}
