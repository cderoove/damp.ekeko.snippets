package org.acm.seguin.tools.international;

import java.io.File;

import org.acm.seguin.io.DirectoryTreeTraversal;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;

/**
 *  Lists the strings in a set of files
 *
 *@author    Chris Seguin
 */
class StringListTraversal extends DirectoryTreeTraversal {
	/**
	 *  Constructor for the StringListTraversal object
	 *
	 *@param  init  Description of Parameter
	 */
	public StringListTraversal(String init) {
		super(init);
	}


	/**
	 *  Gets the Target attribute of the StringListTraversal object
	 *
	 *@param  currentFile  Description of Parameter
	 *@return              The Target value
	 */
	protected boolean isTarget(File currentFile) {
		String name = currentFile.getName();
		String lower = name.toLowerCase();
		return lower.endsWith(".java");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  currentFile  Description of Parameter
	 */
	protected void visit(File currentFile) {
		System.out.println("File:  " + currentFile.getPath());
		try {
			FileParserFactory fpf = new FileParserFactory(currentFile);
			SimpleNode root = fpf.getAbstractSyntaxTree(false);
			if (root != null) {
				root.jjtAccept(new StringListVisitor(), null);
			}
		}
		catch (Throwable thrown) {
			thrown.printStackTrace();
		}
		System.out.println(" ");
	}


	/**
	 *  The main program for the StringListTraversal class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			(new StringListTraversal(System.getProperty("user.dir"))).go();
		}
		else {
			(new StringListTraversal(args[0])).go();
		}
	}
}
