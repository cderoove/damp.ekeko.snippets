package org.acm.seguin.parser.factory;

import java.io.InputStream;

/**
 *  Generates new parsers for a java file 
 *
 *@author     Chris Seguin 
 *@created    June 6, 1999 
 */
public class InputStreamParserFactory extends ParserFactory {
	//  Instance Variables
	private InputStream input;
	private String key;


	/**
	 *  Constructor for a file ParserFactory 
	 *
	 *@param  inputStream  Description of Parameter 
	 *@param  initKey      Description of Parameter 
	 */
	public InputStreamParserFactory(InputStream inputStream, String initKey) {
		input = inputStream;
		key = initKey;
	}


	/**
	 *  Return the input stream 
	 *
	 *@return    the input stream 
	 */
	protected InputStream getInputStream() {
		return input;
	}


	/**
	 *  A method to return some key identifying the file that is being parsed 
	 *
	 *@return    the identifier 
	 */
	protected String getKey() {
		return key;
	}
}

