/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *  Generates new parsers for a java file
 *
 *@author     Chris Seguin
 *@created    June 6, 1999
 */
public class BufferParserFactory extends ParserFactory {
	//  Instance Variables
	private String inputBuffer;


	/**
	 *  Constructor for the buffer parser factory
	 *
	 *@param  buffer  the initial buffer
	 */
	public BufferParserFactory(String buffer) {
		inputBuffer = buffer;
	}


	/**
	 *  Return the input stream
	 *
	 *@return    the input stream
	 */
	protected InputStream getInputStream() {
		return new ByteArrayInputStream(inputBuffer.getBytes());
	}


	/**
	 *  A method to return some key identifying the file that is being parsed
	 *
	 *@return    the identifier
	 */
	protected String getKey() {
		return "the current file";
	}
}

