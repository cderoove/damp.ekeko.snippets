/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.acm.seguin.awt.Question;
import org.acm.seguin.io.InplaceOutputStream;
import org.acm.seguin.parser.ast.ASTCompilationUnit;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;
import org.acm.seguin.util.FileSettings;

/**
 *  Holds a refactoring. Default version just pretty prints the file.
 *
 *@author     Chris Seguin
 *@created    July 1, 1999
 *@date       May 12, 1999
 */
public class PrettyPrintFile {
	//  Instance Variables
	private ParserFactory factory;
	private boolean ask;



	/**
	 *  Refactors java code.
	 */
	public PrettyPrintFile() {
		ask = true;
	}


	/**
	 *  Sets whether we should ask the user
	 *
	 *@param  way  the way to set the variable
	 */
	public void setAsk(boolean way) {
		ask = way;
	}


	/**
	 *  Set the parser factory
	 *
	 *@param  factory  Description of Parameter
	 */
	public void setParserFactory(ParserFactory factory) {
		this.factory = factory;
	}


	/**
	 *  Returns true if this refactoring is applicable
	 *
	 *@param  inputFile  the input file
	 *@return            true if this refactoring is applicable
	 */
	public boolean isApplicable(File inputFile) {
		if (!inputFile.canWrite()) {
			return false;
		}

		boolean result = true;
		if (ask) {
			result = Question.isYes("Pretty Printer",
					"Do you want to pretty print the file\n" + inputFile.getPath() + "?");
		}

		//  Create a factory if necessary
		if (result) {
			setParserFactory(new FileParserFactory(inputFile));
		}

		return result;
	}


	/**
	 *  Return the factory that gets the abstract syntax trees
	 *
	 *@return    the parser factory
	 */
	public ParserFactory getParserFactory() {
		return factory;
	}


	/**
	 *  Apply the refactoring
	 *
	 *@param  inputFile  the input file
	 */
	public void apply(File inputFile) {
		SimpleNode root = factory.getAbstractSyntaxTree(true);
		apply(inputFile, root);
	}


	/**
	 *  Apply the refactoring
	 *
	 *@param  inputFile  the input file
	 *@param  root       Description of Parameter
	 */
	public void apply(File inputFile, SimpleNode root) {
		if (root != null) {
			FileSettings pretty = FileSettings.getSettings("Refactory", "pretty");
			pretty.setReloadNow(true);

			//  Create the visitor
			PrettyPrintVisitor printer = new PrettyPrintVisitor();

			//  Create the appropriate print data
			PrintData data = getPrintData(inputFile);

			if (root instanceof ASTCompilationUnit) {
				printer.visit((ASTCompilationUnit) root, data);
			}
			else {
				printer.visit(root, data);
			}

			//  Flush the output stream
			data.close();
		}
	}


	/**
	 *  Create the output stream
	 *
	 *@param  file  the name of the file
	 *@return       the output stream
	 */
	protected OutputStream getOutputStream(File file) {
		//  Local Variables
		OutputStream out = null;

		try {
			out = new InplaceOutputStream(file);
		}
		catch (IOException ioe) {
			out = System.out;
		}

		//  Return the output stream
		return out;
	}


	/**
	 *  Return the appropriate print data
	 *
	 *@param  input  Description of Parameter
	 *@return        the print data
	 */
	protected PrintData getPrintData(File input) {
		//  Create the new stream
		return new PrintData(getOutputStream(input));
	}
}
