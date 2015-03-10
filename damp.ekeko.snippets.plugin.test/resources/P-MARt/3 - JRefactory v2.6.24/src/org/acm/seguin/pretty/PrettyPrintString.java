/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.acm.seguin.parser.factory.BufferParserFactory;

/**
 *  Pretty prints the string
 *
 *@author    Chris Seguin
 */
abstract class PrettyPrintString extends PrettyPrintFile {
	//  Instance Variables
	private ByteArrayOutputStream outputStream;


	/**
	 *  Create an PrettyPrintString object
	 */
	public PrettyPrintString()
	{
		outputStream = new ByteArrayOutputStream();
	}


	/**
	 *  Sets the input string to be pretty printed
	 *
	 *@param  input  the input buffer
	 */
	protected void setInputString(String input)
	{
		if (input == null) {
			return;
		}

		setParserFactory(new BufferParserFactory(input));
	}


	/**
	 *  Get the output buffer
	 *
	 *@return    a string containing the results
	 */
	protected String getOutputBuffer()
	{
		return new String(outputStream.toByteArray());
	}


	/**
	 *  Create the output stream
	 *
	 *@param  file  the name of the file
	 *@return       the output stream
	 */
	protected OutputStream getOutputStream(File file)
	{
		return outputStream;
	}


	/**
	 *  Reset the output buffer
	 */
	protected void resetOutputBuffer()
	{
		outputStream.reset();
	}
}
