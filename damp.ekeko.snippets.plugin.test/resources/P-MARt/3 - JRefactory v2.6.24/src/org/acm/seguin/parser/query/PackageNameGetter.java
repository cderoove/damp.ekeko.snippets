package org.acm.seguin.parser.query;

import java.io.File;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;

/**
 *  Gets the package name
 *
 *@author     Chris Seguin
 *@created    November 23, 1999
 */
public class PackageNameGetter {
	/**
	 *  Return the package name
	 *
	 *@param  initialDir  Description of Parameter
	 *@param  filename    Description of Parameter
	 *@return             the package name
	 */
	public static ASTName query(File initialDir, String filename) {
		//  Create a factory to get a root
		File inputFile = new File(initialDir, filename);
		ParserFactory factory = new FileParserFactory(inputFile);
		SimpleNode root = factory.getAbstractSyntaxTree(false);

		return query(root);
	}


	/**
	 *  Gets the package name
	 *
	 *@param  root  the syntax tree
	 *@return       the name of the package or null if there is none
	 */
	public static ASTName query(SimpleNode root) {
		if (root == null) {
			System.out.println("Unable to find the file!");
			return null;
		}

		SimpleNode first = (SimpleNode) root.jjtGetChild(0);
		if (first instanceof ASTPackageDeclaration) {
			return (ASTName) first.jjtGetChild(0);
		}

		return null;
	}
}
