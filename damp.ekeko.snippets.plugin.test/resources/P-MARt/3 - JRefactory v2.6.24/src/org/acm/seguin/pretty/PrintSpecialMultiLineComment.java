/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.util.StringTokenizer;

import org.acm.seguin.parser.JavaParserConstants;
import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.Token;

/**
 *  Consume a multi line comment
 *
 *@author     Chris Seguin
 *@created    October 14, 1999
 *@date       April 10, 1999
 */
public class PrintSpecialMultiLineComment extends PrintSpecial
{
	/**
	 *  Determines if this print special can handle the current object
	 *
	 *@param  spec  Description of Parameter
	 *@return       true if this one should process the input
	 */
	public boolean isAcceptable(SpecialTokenData spec)
	{
		return (spec.getTokenType() == JavaParserConstants.MULTI_LINE_COMMENT);
	}


	/**
	 *  Processes the special token
	 *
	 *@param  node  the type of node this special is being processed for
	 *@param  spec  the special token data
	 *@return       Description of the Returned Value
	 */
	public boolean process(Node node, SpecialTokenData spec)
	{
		//  Get the print data
		PrintData printData = spec.getPrintData();
		String image = spec.getTokenImage();
		int formatCode = printData.getCStyleFormatCode();

		if (formatCode == PrintData.CSC_LEAVE_UNTOUCHED)
		{
			transcribe(printData, image);
		}
		else
		{
			cleanFormat(printData, image, formatCode, spec.isLast());
		}

		//  Changed something
		return true;
	}


	/**
	 *  Cleanly format the code
	 *
	 *@param  printData   the print data
	 *@param  image       the comment from the original file
	 *@param  formatCode  the formatting style
	 *@param  last        Description of Parameter
	 */
	private void cleanFormat(PrintData printData, String image,
			int formatCode, boolean last)
	{
		//  Make sure we are indented
		if ((formatCode != PrintData.CSC_MAINTAIN_STAR)
				 && !printData.isLineIndented())
		{
			printData.indent();
		}

		if (formatCode == PrintData.CSC_MAINTAIN_STAR)
		{
			if (!printData.isBufferEmpty())
			{
				printData.space();
			}
			else if (!printData.isLineIndented())
			{
				printData.indent();
			}
		}

		//  Start the comment
		printData.appendComment("/*", PrintData.C_STYLE_COMMENT);
		if (formatCode == PrintData.CSC_MAINTAIN_STAR)
		{
			//  Do nothing
		}
		else
		{
			startNewline(printData, true, formatCode);
		}

		//  Print the comment
		JavadocTokenizer tok = new JavadocTokenizer(image);
		tok.next();
		boolean lastWasNewline = false;
		boolean first = true;

		while (tok.hasNext())
		{
			Token token = tok.next();

			if (first && ((formatCode == PrintData.CSC_ALIGN_STAR) ||
					(formatCode == PrintData.CSC_ALIGN_BLANK)))
			{
				while (token.kind != JavadocTokenizer.WORD)
				{
					if (tok.hasNext()) {
						token = tok.next();
					}
					else {
						break;
					}
				}

				first = false;
			}

			//  On a newline skip the space so that we realign things
			if (lastWasNewline && (token.kind == JavadocTokenizer.SPACE)
					 && (formatCode != PrintData.CSC_MAINTAIN_STAR))
			{
				token = tok.next();
			}

			if (token.kind == JavadocTokenizer.NEWLINE)
			{
				startNewline(printData, tok.hasNext(), formatCode);
				lastWasNewline = true;
			}
			else
			{
				printData.appendComment(token.image, PrintData.C_STYLE_COMMENT);
				lastWasNewline = false;
			}
		}

		//  Finish the comment
		if (lastWasNewline)
		{
			String rest = "/";
			if (formatCode == PrintData.CSC_ALIGN_BLANK)
			{
				rest = "*/";
			}
			printData.appendComment(rest, PrintData.C_STYLE_COMMENT);
		}
		else
		{
			if ((formatCode != PrintData.CSC_MAINTAIN_STAR)
					 && !printData.isLineIndented())
			{
				printData.indent();
			}
			printData.appendComment(" */", PrintData.C_STYLE_COMMENT);
		}

		//  Newline
		if (((formatCode == PrintData.CSC_ALIGN_STAR) ||
				(formatCode == PrintData.CSC_ALIGN_BLANK)) && last)
		{
			printData.newline();
			SpecialTokenVisitor.surpriseIndent(printData);
		}
	}


	/**
	 *  Starts a newline
	 *
	 *@param  printData   The print interface
	 *@param  more        Are there more tokens
	 *@param  formatCode  the formatting style
	 */
	private void startNewline(PrintData printData, boolean more, int formatCode)
	{
		printData.indent();
		if (formatCode == PrintData.CSC_ALIGN_BLANK)
		{
			printData.appendComment("  ", PrintData.C_STYLE_COMMENT);
		}
		else
		{
			printData.appendComment(" *", PrintData.C_STYLE_COMMENT);
		}

		if ((formatCode == PrintData.CSC_MAINTAIN_STAR) || !more)
		{
			//  Do nothing
		}
		else
		{
			for (int ndx = 0; ndx < printData.getCStyleIndent(); ndx++)
			{
				printData.appendComment(" ", PrintData.C_STYLE_COMMENT);
			}
		}
	}


	/**
	 *  Simply copy the C style comment into the output file
	 *
	 *@param  printData  the print data
	 *@param  image      the comment
	 */
	private void transcribe(PrintData printData, String image)
	{
		StringTokenizer tok = new StringTokenizer(image, "\n\r");
		if (!printData.isBufferEmpty())
		{
			printData.space();
		}
		else if (!printData.isLineIndented())
		{
			printData.indent();
		}

		while (tok.hasMoreTokens())
		{
			printData.appendComment(tok.nextToken(), PrintData.C_STYLE_COMMENT);
			if (tok.hasMoreTokens())
			{
				printData.newline();
			}
		}
	}
}
