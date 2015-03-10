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
 *  Prints a description from a java doc comment with HTML tags formatted.
 *
 *@author     Chris Seguin
 *@created    July 23, 1999
 */
public class JavadocDescriptionPrinter {
	/*<Instance Variables>*/
	private PrintData printData;
	private StringBuffer buffer;
	private int indent;
	private int mode;
	private boolean newline;
	private int owedLines;
	/*</Instance Variables>*/

	/*<Class Variables>*/
	private static int NORMAL = 0;
	private static int PARA = 1;
	private static int LIST = 2;
	private static int END_LIST = 3;
	private static int TABLE = 4;
	private static int END_TAG = 5;
	private static int LINE_BREAK = 6;
	private static int PREFORMATTED = 7;
	/*</Class Variables>*/


	/*<Constructors>*/
	/**
	 *  Constructor for the JavadocDescriptionPrinter object
	 *
	 *@param  data         Description of Parameter
	 *@param  description  Description of Parameter
	 */
	public JavadocDescriptionPrinter(PrintData data, String description)
	{
		printData = data;
		buffer = new StringBuffer(description);
		indent = printData.getJavadocIndent();
		newline = false;
		mode = NORMAL;
	}


	/**
	 *  Constructor for the JavadocDescriptionPrinter object
	 *
	 *@param  data         Description of Parameter
	 *@param  description  Description of Parameter
	 *@param  initIndent   Description of Parameter
	 */
	public JavadocDescriptionPrinter(PrintData data, String description, int initIndent)
	{
		printData = data;
		buffer = new StringBuffer(description);
		indent = initIndent;
	}
	/*</Constructors>*/


	/*<Methods>*/
	/**
	 *  This is the main program.
	 */
	public void run()
	{
		if (printData.isReformatComments()) {
			int MIN = printData.getJavadocWordWrapMinimum();
			int MAX = printData.getJavadocWordWrapMaximum();

			JavadocTokenizer tok = new JavadocTokenizer(buffer.toString());
			mode = NORMAL;
			boolean first = true;
			while (tok.hasNext()) {
				Token nextToken = tok.next();
				first = printToken(nextToken, MIN, MAX, first);
			}
		}
		else {
			maintainCurrentFormat();
		}
	}


	/**
	 *  Indents the line and inserts the required "*"
	 */
	protected void indent()
	{
		if (printData.isCurrentSingle())
			return;

		newline = true;
		printData.indent();
		printData.appendComment(" *", PrintData.JAVADOC_COMMENT);

		if (printData.isReformatComments() && (mode != PREFORMATTED)) {
			for (int ndx = 0; ndx < indent; ndx++) {
				printData.space();
			}
		}
	}


	/**
	 *  Certain tags require that we insert a new line after them.
	 *
	 *@param  token  the tag that we are considering
	 *@return        true if we just printed a space or a newline
	 */
	protected boolean startMode(String token)
	{
		if (startsWith(token, "<PRE") || startsWith(token, "<CODE")) {
			mode = PREFORMATTED;
		}
		else if (startsWith(token, "</PRE") || startsWith(token, "</CODE")) {
			mode = NORMAL;
		}
		else if (startsWith(token, "<P")) {
			mode = PARA;
		}
		else if (startsWith(token, "<BR")) {
			mode = LINE_BREAK;
		}
		else if (startsWith(token, "<UL")) {
			mode = LIST;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "<OL")) {
			mode = LIST;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "</UL")) {
			mode = END_LIST;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "</OL")) {
			mode = END_LIST;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "<LI")) {
			indent();
			mode = END_TAG;
			return true;
		}
		else if (startsWith(token, "<TABLE")) {
			mode = TABLE;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "<TR")) {
			mode = TABLE;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "<TD")) {
			mode = TABLE;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "<TH")) {
			mode = TABLE;
			indent();
			indent += 2;
			return true;
		}
		else if (startsWith(token, "</TABLE")) {
			mode = TABLE;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "</TR")) {
			mode = TABLE;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "</TD")) {
			mode = TABLE;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "</TH")) {
			mode = TABLE;
			indent -= 2;
			indent();
			return true;
		}
		else if (startsWith(token, "</") && !newline) {
			mode = END_TAG;
		}

		return false;
	}


	/**
	 *  Detects the end of the tag marker
	 *
	 *@param  token  the token
	 *@return        Description of the Returned Value
	 */
	protected boolean endMode(String token)
	{
		if (mode == END_TAG) {
			mode = NORMAL;
			printData.space();
			return true;
		}
		if (mode == PARA) {
			mode = NORMAL;
			indent();
			indent();
			return true;
		}
		if (mode == LINE_BREAK) {
			mode = NORMAL;
			indent();
			return true;
		}
		if (mode == LIST) {
			mode = NORMAL;
		}
		if (mode == END_LIST) {
			mode = NORMAL;
			indent();
			return true;
		}
		if (mode == TABLE) {
			mode = NORMAL;
			indent();
			return true;
		}

		return false;
	}


	/**
	 *  Checks to see if this tag is the same as what we want and ignores case
	 *  troubles
	 *
	 *@param  have  the token that we have
	 *@param  want  the token that we are interested in
	 *@return       true if what we have is the same as what we want
	 */
	protected boolean startsWith(String have, String want)
	{
		return have.toUpperCase().startsWith(want);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  nextToken  Description of Parameter
	 *@param  MIN        Description of Parameter
	 *@param  MAX        Description of Parameter
	 *@param  isFirst    Description of Parameter
	 *@return            Description of the Returned Value
	 */
	private boolean printToken(Token nextToken, int MIN, int MAX, boolean isFirst)
	{
		if (nextToken.kind == JavadocTokenizer.WORD) {
			newline = false;
			int length = nextToken.image.length();
			if ((printData.getLineLength() > MIN) &&
					(printData.getLineLength() + length > MAX) &&
					(mode != PREFORMATTED)) {
				indent();
				newline = true;
			}
			if (nextToken.image.charAt(0) == '<') {
				newline = startMode(nextToken.image.toUpperCase());
			}
			else {
				newline = false;
			}
			printData.appendComment(nextToken.image, PrintData.JAVADOC_COMMENT);
			if (nextToken.image.charAt(nextToken.image.length() - 1) == '>') {
				newline = endMode(nextToken.image) || newline;
			}

			return newline;
		}
		else {
			if (mode != PREFORMATTED) {
				if (!isFirst) {
					printData.space();
					return true;
				}
			}
			else if (nextToken.kind == JavadocTokenizer.SPACE) {
				printData.appendComment(nextToken.image, PrintData.JAVADOC_COMMENT);
			}
			else {
				indent();
			}

			return isFirst;
		}
	}


	/**
	 *  Maintains the current format
	 */
	private void maintainCurrentFormat()
	{
		JavadocTokenizer tok = new JavadocTokenizer(buffer.toString());
		owedLines = 0;

		Token last = null;
		Token current = tok.next();

		while (current.kind != JavadocTokenizer.WORD) {
			last = current;
			if (!tok.hasNext()) {
				return;
			}
			current = tok.next();
		}

		if ((last != null) && (last.kind != JavadocTokenizer.NEWLINE)) {
			mcfOutputToken(last, printData);
		}
		mcfOutputToken(current, printData);

		while (tok.hasNext()) {
			Token nextToken = tok.next();
			mcfOutputToken(nextToken, printData);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  nextToken  Description of Parameter
	 *@param  printData  Description of Parameter
	 */
	private void mcfOutputToken(Token nextToken, PrintData printData)
	{
		if (nextToken.kind == JavadocTokenizer.NEWLINE) {
			owedLines++;
		}
		else {
			while (owedLines > 0) {
				indent();
				owedLines--;
			}

			printData.appendComment(nextToken.image, PrintData.JAVADOC_COMMENT);
		}
	}
	/*</Methods>*/
}
