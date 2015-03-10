/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.print;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Stores the pretty printer settings. Allows the user to reload or write
 *  them to a file.
 *
 *@author    Chris Seguin
 */
public class PrintingSettings {
	private int textFontSize;
	private int textSpace;
	private int headerBlock;
	private int filenameFont;
	private int dateFont;
	private int linesPerPage;


	/**
	 *  Constructor for the PrintingSettings object
	 */
	public PrintingSettings() {
		init();
	}


	/**
	 *  Sets the TextFontSize attribute of the PrintingSettings object
	 *
	 *@param  value  The new TextFontSize value
	 */
	public void setTextFontSize(int value) {
		if (value != textFontSize) {
			textFontSize = value;
			save();
		}
	}


	/**
	 *  Sets the TextSpace attribute of the PrintingSettings object
	 *
	 *@param  value  The new TextSpace value
	 */
	public void setTextSpace(int value) {
		if (value != textSpace) {
			textSpace = value;
			save();
		}
	}


	/**
	 *  Sets the HeaderBlockHeight attribute of the PrintingSettings object
	 *
	 *@param  value  The new HeaderBlockHeight value
	 */
	public void setHeaderBlockHeight(int value) {
		if (value != headerBlock) {
			headerBlock = value;
			save();
		}
	}


	/**
	 *  Sets the FilenameFontSize attribute of the PrintingSettings object
	 *
	 *@param  value  The new FilenameFontSize value
	 */
	public void setFilenameFontSize(int value) {
		if (value != filenameFont) {
			filenameFont = value;
			save();
		}
	}


	/**
	 *  Sets the DateFontSize attribute of the PrintingSettings object
	 *
	 *@param  value  The new DateFontSize value
	 */
	public void setDateFontSize(int value) {
		if (value != dateFont) {
			dateFont = value;
			save();
		}
	}


	/**
	 *  Sets the LinesPerPage attribute of the PrintingSettings object
	 *
	 *@param  value  The new LinesPerPage value
	 */
	public void setLinesPerPage(int value) {
		if (linesPerPage != value) {
			linesPerPage = value;
			save();
		}
	}


	/**
	 *  Gets the TextFontSize attribute of the PrintingSettings object
	 *
	 *@return    The TextFontSize value
	 */
	public int getTextFontSize() {
		return textFontSize;
	}


	/**
	 *  Gets the TextSpace attribute of the PrintingSettings object
	 *
	 *@return    The TextSpace value
	 */
	public int getTextSpace() {
		return textSpace;
	}


	/**
	 *  Gets the HeaderBlockHeight attribute of the PrintingSettings object
	 *
	 *@return    The HeaderBlockHeight value
	 */
	public int getHeaderBlockHeight() {
		return headerBlock;
	}


	/**
	 *  Gets the FilenameFontSize attribute of the PrintingSettings object
	 *
	 *@return    The FilenameFontSize value
	 */
	public int getFilenameFontSize() {
		return filenameFont;
	}


	/**
	 *  Gets the DateFontSize attribute of the PrintingSettings object
	 *
	 *@return    The DateFontSize value
	 */
	public int getDateFontSize() {
		return dateFont;
	}


	/**
	 *  Gets the LinesPerPage attribute of the PrintingSettings object
	 *
	 *@return    The LinesPerPage value
	 */
	public int getLinesPerPage() {
		return linesPerPage;
	}


	/**
	 *  Description of the Method
	 */
	public void save() {
		try {
			String dir = FileSettings.getSettingsRoot() + File.separator + ".Refactory";
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			String filename = dir + File.separator + "printing.settings";
			FileWriter output = new FileWriter(filename);
			PrintWriter printer = new PrintWriter(output);
			write(printer);
			printer.close();
			output.close();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Description of the Method
	 */
	private void defaults() {
		textFontSize = 10;
		textSpace = 0;
		headerBlock = 30;
		filenameFont = 14;
		dateFont = 8;
		linesPerPage = 36;
	}


	/**
	 *  Sets the default values for these
	 */
	private void init() {
		defaults();

		try {
			FileSettings setting = FileSettings.getSettings("Refactory", "printing");

			textFontSize = setting.getInteger("text.font.size");
			textSpace = setting.getInteger("text.space");
			headerBlock = setting.getInteger("header.space");
			filenameFont = setting.getInteger("filename.font.size");
			dateFont = setting.getInteger("date.font.size");
			linesPerPage = setting.getInteger("lines.per.page");
		}
		catch (MissingSettingsException mse) {
			//  Expected
		}
	}


	/**
	 *  Writes the values back to the disk
	 *
	 *@param  printer  the output writer
	 */
	private void write(PrintWriter printer) {
		printer.println("#  This is the font size for the text of the file");
		printer.println("text.font.size=" + textFontSize);
		printer.println(" ");
		printer.println("#  This is the number of pixels to skip between");
		printer.println("#  lines in the text of the file");
		printer.println("text.space=" + textSpace);
		printer.println(" ");
		printer.println("#  The header block is 30 pixels high");
		printer.println("header.space=" + headerBlock);
		printer.println(" ");
		printer.println("#  The name of the file is specified with this parameter");
		printer.println("filename.font.size=" + filenameFont);
		printer.println(" ");
		printer.println("#  The date that the file was printed and the number");
		printer.println("#  of pages is in this font size");
		printer.println("date.font.size=" + dateFont);
		printer.println(" ");
		printer.println("#  The number of lines on a page.  This is an estimate");
		printer.println("#  that is updated by the software each time a new set of");
		printer.println("#  values is changed");
		printer.println("lines.per.page=" + linesPerPage);
	}
}
