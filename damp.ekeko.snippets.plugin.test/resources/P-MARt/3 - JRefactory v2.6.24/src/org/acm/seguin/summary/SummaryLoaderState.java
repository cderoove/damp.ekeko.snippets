/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.io.File;
import java.util.Stack;

/**
 *  State of the summary loader visitor
 *
 *@author     Chris Seguin
 *@created    August 30, 1999
 */
public class SummaryLoaderState {
	private Stack stack;
	private File file;
	private int code;

	/**
	 *  Description of the Field
	 */
	public final static int INITIALIZE = 0;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_FILE = 1;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_TYPE = 2;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_CLASSBODY = 3;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_INTERFACE_LIST = 4;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_EXCEPTIONS = 5;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_PARAMETERS = 6;
	/**
	 *  Description of the Field
	 */
	public final static int LOAD_METHODBODY = 7;
	/**  Tells the loader to ignore the structure - used only for counting  */
	public final static int IGNORE=100;


	/**
	 *  Create a summary loader state
	 */
	public SummaryLoaderState() {
		stack = new Stack();
		file = null;
		code = INITIALIZE;
	}


	/**
	 *  Set the current file
	 *
	 *@param  next  the new file
	 */
	public void setFile(File next) {
		file = next;
	}


	/**
	 *  Set the current code
	 *
	 *@param  next  The next code
	 */
	public void setCode(int next) {
		code = next;
	}


	/**
	 *  Get the current summary
	 *
	 *@return    the current summary
	 */
	public Summary getCurrentSummary() {
		if (code == INITIALIZE) {
			PackageSummary packageSummary = PackageSummary.getPackageSummary("");
			startSummary(new FileSummary(packageSummary, getFile()));
			code = LOAD_FILE;
		}

		return (Summary) stack.peek();
	}


	/**
	 *  Get the file
	 *
	 *@return    the file
	 */
	public File getFile() {
		return file;
	}


	/**
	 *  Get the current code
	 *
	 *@return    The code
	 */
	public int getCode() {
		return code;
	}


	/**
	 *  Set the current summary
	 *
	 *@param  next  the new current summary
	 */
	public void startSummary(Summary next) {
		stack.push(next);
	}


	/**
	 *  Complete the current summary
	 *
	 */
	public void finishSummary() {
		stack.pop();
	}
}
