/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

import org.acm.seguin.pretty.PrettyPrintFromIDE;

/**
 *  Pretty printer for the elixir editor.
 *
 *@author    Chris Seguin
 *@date      May 31, 1999
 */
public class ElixirPrettyPrinter extends PrettyPrintFromIDE {
	private BasicViewManager bvm;


	/**
	 *  Create an ElixirPrettyPrinter object
	 */
	public ElixirPrettyPrinter() {
		super();
	}


	/**
	 *  Remove \r from buffer
	 *
	 *@param  input  Description of Parameter
	 *@return        a string containing the results
	 */
	public String removeCR(String input) {
		StringBuffer buffer = new StringBuffer();
		int last = input.length();

		for (int ndx = 0; ndx < last; ndx++) {
			char ch = input.charAt(ndx);
			if (ch == '\r') {
				//  Do nothing
			}
			else {
				buffer.append(ch);
			}
		}

		return buffer.toString();
	}


	/**
	 *  Sets the string in the IDE
	 *
	 *@param  value  The new file contained in a string
	 */
	protected void setStringInIDE(String value) {
		bvm.setContentsString(value);
	}


	/**
	 *  Sets the line number
	 *
	 *@param  value  The new LineNumber value
	 */
	protected void setLineNumber(int value) {
		bvm.setLineNo(value);
	}


	/**
	 *  Get the output buffer
	 *
	 *@return    a string containing the results
	 */
	protected String getOutputBuffer() {
		return removeCR(super.getOutputBuffer());
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected String getStringFromIDE() {
		FrameManager fm = FrameManager.current();
		bvm = (BasicViewManager) fm.getViewSite().getCurrentViewManager();
		return bvm.getContentsString();
	}


	/**
	 *  Returns the initial line number
	 *
	 *@return    The LineNumber value
	 */
	protected int getLineNumber() {
		return bvm.getLineNo();
	}


	/**
	 *  Reformats the current source code
	 */
	public static void prettyPrint() {
		ElixirPrettyPrinter epp = new ElixirPrettyPrinter();
		epp.prettyPrintCurrentWindow();
	}
}
