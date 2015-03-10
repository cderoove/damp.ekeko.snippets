/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.io.File;

import javax.swing.JOptionPane;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Pretty print the source code based on the IDE
 *
 *@author    Chris Seguin
 */
public abstract class PrettyPrintFromIDE extends PrettyPrintString {
	private PrintData data;


	/**
	 *  Reformat the source code in the current window
	 */
	public void prettyPrintCurrentWindow()
	{
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		try {
			setInputString(getStringFromIDE());
			apply(null);

			//  Store the string back
			String contents = getOutputBuffer();
			if (contents.length() > 0) {
				setStringInIDE(contents);
				setLineNumber(data.getFinalLine());
			}
			else {
				displayErrorMessage();
			}

			// Done with the output buffer so reset it
			resetOutputBuffer();
		}
		catch (Exception exc) {
			displayErrorMessage();
			ExceptionPrinter.print(exc);
		}
	}


	/**
	 *  Sets the string in the IDE
	 *
	 *@param  value  The new file contained in a string
	 */
	protected abstract void setStringInIDE(String value);


	/**
	 *  Sets the line number
	 *
	 *@param  value  The new LineNumber value
	 */
	protected abstract void setLineNumber(int value);


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected abstract String getStringFromIDE();


	/**
	 *  Return the appropriate print data
	 *
	 *@param  input  Description of Parameter
	 *@return        the print data
	 */
	protected PrintData getPrintData(File input)
	{
		data = super.getPrintData(input);
		data.setOriginalLine(getLineNumber());
		return data;
	}


	/**
	 *  Returns the initial line number
	 *
	 *@return    The LineNumber value
	 */
	protected abstract int getLineNumber();


	/**
	 *  Displays the error message
	 */
	private void displayErrorMessage()
	{
		JOptionPane.showMessageDialog(null,
				"Unable to parse the file",
				"Parse Error",
				JOptionPane.ERROR_MESSAGE);
	}
}
