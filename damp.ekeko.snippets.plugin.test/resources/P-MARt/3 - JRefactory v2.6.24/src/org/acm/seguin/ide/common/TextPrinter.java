/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import org.acm.seguin.print.text.LinePrinter;
import org.acm.seguin.print.text.NumberedLinePrinter;
import org.acm.seguin.print.text.PrintingThread;
import org.acm.seguin.print.text.PropertyLinePrinter;
import org.acm.seguin.print.xml.XMLLinePrinter;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class TextPrinter {
	/**
	 *  Description of the Method
	 *
	 *@param  filename  Description of Parameter
	 *@param  contents  Description of Parameter
	 */
	protected void print(String filename, String contents) {
		LinePrinter lp = null;
		if (isPropertyFile(filename)) {
			lp = new PropertyLinePrinter();
		}
		else if (isMarkupLanguage(filename)) {
			lp = new XMLLinePrinter();
		}
		else {
			lp = new NumberedLinePrinter();
		}
		(new PrintingThread(filename, contents, lp)).start();
	}


	/**
	 *  Gets the PropertyFile attribute of the ElixirTextPrinter object
	 *
	 *@param  fullFilename  Description of Parameter
	 *@return               The PropertyFile value
	 */
	private boolean isPropertyFile(String fullFilename) {
		String ext = getExtension(fullFilename);
		return ext.equals("properties") || ext.equals("settings");
	}


	/**
	 *  Gets the PropertyFile attribute of the ElixirTextPrinter object
	 *
	 *@param  fullFilename  Description of Parameter
	 *@return               The PropertyFile value
	 */
	private boolean isMarkupLanguage(String fullFilename) {
		String ext = getExtension(fullFilename);
		return ext.endsWith("ml");
	}


	/**
	 *  Gets the Extension attribute of the ElixirTextPrinter object
	 *
	 *@param  filename  Description of Parameter
	 *@return           The Extension value
	 */
	private String getExtension(String filename) {
		int ndx = filename.lastIndexOf(".");
		if (ndx == -1) {
			return "";
		}
		return filename.substring(ndx + 1);
	}
}
