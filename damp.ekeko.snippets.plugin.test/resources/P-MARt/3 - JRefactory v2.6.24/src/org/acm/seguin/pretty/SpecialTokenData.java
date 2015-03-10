/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import org.acm.seguin.parser.Token;

/**
 *  Store the data that understands how to output comments and newlines
 *
 *@author     Chris Seguin
 *@created    April 30, 1999
 */
public class SpecialTokenData {
	//  Instance Variables
	private PrintData data;
	private Token special;
	private boolean lastReturnExpected;
	private boolean acceptNewlines;
	private boolean setAcceptingNewlines;
	private boolean reformatComments;


	/**
	 *  Creates a special token data object
	 *
	 *@param  token      the special token
	 *@param  printData  the print data
	 */
	public SpecialTokenData(Token token, PrintData printData) {
		this(token, printData, true);
	}


	/**
	 *  Creates a special token data object
	 *
	 *@param  token      the special token
	 *@param  printData  the print data
	 *@param  accept     whether newlines should be accepted
	 */
	public SpecialTokenData(Token token, PrintData printData, boolean accept) {
		data = printData;
		special = beginning(token);
		lastReturnExpected = true;
		acceptNewlines = accept;
		setAcceptingNewlines = false;

		//  Check if comments should be reformatted
		reformatComments = printData.isReformatComments();
	}


	/**
	 *  Set that the last return was (or not) expected
	 *
	 *@param  way  the way it was expected
	 */
	public void setReturnExpected(boolean way) {
		if (!setAcceptingNewlines) {
			lastReturnExpected = way;
			setAcceptingNewlines = true;
		}
	}


	/**
	 *  Returns true when it is the last
	 *
	 *@return    true if we are at the last
	 */
	public boolean isLast() {
		return ((special == null) || (special.next == null));
	}


	/**
	 *  Returns true when it is the first
	 *
	 *@return    true if we are at the first
	 */
	public boolean isFirst() {
		return ((special == null) || (special.specialToken == null));
	}


	/**
	 *  Return the print data
	 *
	 *@return    the print data object
	 */
	public PrintData getPrintData() {
		return data;
	}


	/**
	 *  Return the special token
	 *
	 *@return    the special token
	 */
	public Token getSpecialToken() {
		return special;
	}


	/**
	 *  Get the token type
	 *
	 *@return    the token type
	 */
	public int getTokenType() {
		if (special == null) {
			return -1;
		}
		return special.kind;
	}


	/**
	 *  Get the token image
	 *
	 *@return    the token image
	 */
	public String getTokenImage() {
		if (special == null) {
			return "";
		}

		return special.image;
	}


	/**
	 *  Return true if the last return was expected
	 *
	 *@return    true if last was expected
	 */
	public boolean isReturnExpected() {
		return lastReturnExpected;
	}


	/**
	 *  Return true if new lines should be accepted
	 *
	 *@return    true if newlines should be accepted
	 */
	public boolean isAcceptingReturns() {
		return acceptNewlines;
	}


	/**
	 *  Returns true if comments are being reformatted
	 *
	 *@return    true if comments should be reformatted
	 */
	public boolean isReformattingComments() {
		return reformatComments;
	}


	/**
	 *  Got to the beginning
	 *
	 *@param  tok  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public Token beginning(Token tok) {
		if (tok == null) {
			return null;
		}

		//  Find the first token
		Token current = tok;
		Token previous = tok.specialToken;
		while (previous != null) {
			current = previous;
			previous = current.specialToken;
		}

		//  Return the first
		return current;
	}


	/**
	 *  Get the next token
	 */
	public void next() {
		if (special != null) {
			special = special.next;
		}
	}
}
