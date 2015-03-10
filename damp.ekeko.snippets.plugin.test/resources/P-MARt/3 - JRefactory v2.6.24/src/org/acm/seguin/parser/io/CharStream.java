package org.acm.seguin.parser.io;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Basic character stream. The javacc tool creates one of four different
 *  types of character streams. To be able to switch between different types,
 *  I've created this parent class. The parent class invokes the appropriate
 *  child class that was created by javacc. <P>
 *
 *  <B>ASCII_CharStream</B> processes western files where characters are
 *  stored as a single byte. <P>
 *
 *  <B>UCode_CharStream</B> processes far eastern files where unicode (2 byte)
 *  characters are used.
 *
 *@author    Chris Seguin
 */
public class CharStream extends Object {
	/**
	 *  Is this a static parser
	 */
	public final static boolean staticFlag = true;
	/**
	 *  The buffer location
	 */
	public static int bufpos = -1;
	/**
	 *  The buffer size
	 */
	protected static int bufsize;
	/**
	 *  Are there more characters available
	 */
	protected static int available;
	/**
	 *  Index of the current token's starting point
	 */
	protected static int tokenBegin;
	/**
	 *  The buffer line
	 */
	protected static int bufline[];
	/**
	 *  The buffer column
	 */
	protected static int bufcolumn[];

	/**
	 *  The column index
	 */
	protected static int column = 0;
	/**
	 *  The line index
	 */
	protected static int line = 1;

	/**
	 *  Was the previous character a CR?
	 */
	protected static boolean prevCharIsCR = false;
	/**
	 *  Was the previous character a LF?
	 */
	protected static boolean prevCharIsLF = false;

	/**
	 *  The input
	 */
	protected static java.io.Reader inputStream;

	/**
	 *  The buffer
	 */
	protected static char[] buffer;
	/**
	 *  The maximum next character index
	 */
	protected static int maxNextCharInd = 0;
	/**
	 *  Index into the buffer
	 */
	protected static int inBuf = 0;

	/**
	 *  This field determines which type of character stream to use
	 */
	private static int charStreamType = -1;
	/**
	 *  Use the ascii character stream
	 */
	private final static int ASCII = 1;
	/**
	 *  Use the unicode character stream
	 */
	private final static int UNICODE = 2;
	/**
	 *  Use the unicode character stream
	 */
	private final static int FULL_CHAR = 3;


	/**
	 *  Gets the Column attribute of the CharStream class
	 *
	 *@return    The Column value
	 */
	public static int getColumn() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getColumn();
		else
			return UCode_CharStream.getColumn();
	}


	/**
	 *  Gets the Line attribute of the CharStream class
	 *
	 *@return    The Line value
	 */
	public static int getLine() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getLine();
		else
			return UCode_CharStream.getLine();
	}


	/**
	 *  Gets the EndColumn attribute of the CharStream class
	 *
	 *@return    The EndColumn value
	 */
	public static int getEndColumn() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getEndColumn();
		else
			return UCode_CharStream.getEndColumn();
	}


	/**
	 *  Gets the EndLine attribute of the CharStream class
	 *
	 *@return    The EndLine value
	 */
	public static int getEndLine() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getEndLine();
		else
			return UCode_CharStream.getEndLine();
	}


	/**
	 *  Gets the BeginColumn attribute of the CharStream class
	 *
	 *@return    The BeginColumn value
	 */
	public static int getBeginColumn() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getBeginColumn();
		else
			return UCode_CharStream.getBeginColumn();
	}


	/**
	 *  Gets the BeginLine attribute of the CharStream class
	 *
	 *@return    The BeginLine value
	 */
	public static int getBeginLine() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.getBeginLine();
		else
			return UCode_CharStream.getBeginLine();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  len  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	public static char[] GetSuffix(int len) {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.GetSuffix(len);
		else
			return UCode_CharStream.GetSuffix(len);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public static String GetImage() {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.GetImage();
		else
			return UCode_CharStream.GetImage();
	}


	/**
	 *  Description of the Method
	 *
	 *@return                          Description of the Returned Value
	 *@exception  java.io.IOException  Description of Exception
	 */
	public static char BeginToken() throws java.io.IOException {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.BeginToken();
		else
			return UCode_CharStream.BeginToken();
	}


	/**
	 *  Description of the Method
	 *
	 *@return                          Description of the Returned Value
	 *@exception  java.io.IOException  Description of Exception
	 */
	public static char readChar() throws java.io.IOException {
		if (getCharStreamType() != UNICODE)
			return ASCII_CharStream.readChar();
		else
			return UCode_CharStream.readChar();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  amount  Description of Parameter
	 */
	public static void backup(int amount) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.backup(amount);
		else
			UCode_CharStream.backup(amount);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@param  buffersize   Description of Parameter
	 */
	public static void ReInit(java.io.Reader dstream, int startline, int startcolumn, int buffersize) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.ReInit(dstream, startline, startcolumn, buffersize);
		else
			UCode_CharStream.ReInit(dstream, startline, startcolumn, buffersize);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 */
	public static void ReInit(java.io.Reader dstream, int startline, int startcolumn) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.ReInit(dstream, startline, startcolumn);
		else
			UCode_CharStream.ReInit(dstream, startline, startcolumn);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@param  buffersize   Description of Parameter
	 */
	public static void ReInit(java.io.InputStream dstream, int startline, int startcolumn, int buffersize) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.ReInit(dstream, startline, startcolumn, buffersize);
		else
			UCode_CharStream.ReInit(dstream, startline, startcolumn, buffersize);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 */
	public static void ReInit(java.io.InputStream dstream, int startline, int startcolumn) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.ReInit(dstream, startline, startcolumn);
		else
			UCode_CharStream.ReInit(dstream, startline, startcolumn);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  newLine  Description of Parameter
	 *@param  newCol   Description of Parameter
	 */
	public static void adjustBeginLineColumn(int newLine, int newCol) {
		if (getCharStreamType() != UNICODE)
			ASCII_CharStream.adjustBeginLineColumn(newLine, newCol);
		else
			UCode_CharStream.adjustBeginLineColumn(newLine, newCol);
	}


	/**
	 *  Constructor for the ASCII_CharStream object
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@param  buffersize   Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public static CharStream make(java.io.Reader dstream, int startline,
			int startcolumn, int buffersize) {
		if (getCharStreamType() != UNICODE)
			return new ASCII_CharStream(dstream, startline, startcolumn, buffersize, getCharStreamType() == FULL_CHAR);
		else
			return new UCode_CharStream(dstream, startline, startcolumn, buffersize);
	}


	/**
	 *  Constructor for the ASCII_CharStream object
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public static CharStream make(java.io.Reader dstream, int startline,
			int startcolumn) {
		if (getCharStreamType() != UNICODE)
			return new ASCII_CharStream(dstream, startline, startcolumn, getCharStreamType() == FULL_CHAR);
		else
			return new UCode_CharStream(dstream, startline, startcolumn);
	}


	/**
	 *  Constructor for the ASCII_CharStream object
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@param  buffersize   Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public static CharStream make(java.io.InputStream dstream, int startline,
			int startcolumn, int buffersize) {
		if (getCharStreamType() != UNICODE)
			return new ASCII_CharStream(dstream, startline, startcolumn, buffersize, getCharStreamType() == FULL_CHAR);
		else
			return new UCode_CharStream(dstream, startline, startcolumn, buffersize);
	}


	/**
	 *  Constructor for the ASCII_CharStream object
	 *
	 *@param  dstream      Description of Parameter
	 *@param  startline    Description of Parameter
	 *@param  startcolumn  Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public static CharStream make(java.io.InputStream dstream, int startline,
			int startcolumn) {
		if (getCharStreamType() != UNICODE)
			return new ASCII_CharStream(dstream, startline, startcolumn, getCharStreamType() == FULL_CHAR);
		else
			return new UCode_CharStream(dstream, startline, startcolumn);
	}


	/**
	 *  Gets the type of character stream
	 *
	 *@return    The CharStreamType value
	 */
	protected static int getCharStreamType() {
		if (charStreamType == -1) {
			initCharStreamType();
		}
		return charStreamType;
	}


	/**
	 *  Initializes the character stream type from the pretty.settings file
	 */
	private static synchronized void initCharStreamType() {
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
			charStreamType = bundle.getInteger("char.stream.type");
		}
		catch (MissingSettingsException mse) {
			charStreamType = ASCII;
		}
	}
}
