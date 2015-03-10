/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;
import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.refactor.undo.UndoAction;
import org.acm.seguin.version.VersionControl;
import org.acm.seguin.version.VersionControlFactory;

/**
 *  Base class for a program that reads in an abstract syntax tree, transforms
 *  the code, and rewrites the file to disk.
 *
 *@author    Chris Seguin
 */
public class ComplexTransform {
	//  Instance Variables
	private ArrayList transforms;
	private UndoAction undo;


	/**
	 *  Constructor for the ComplexTransform object
	 *
	 *@param  init  the undo action
	 */
	public ComplexTransform(UndoAction init) {
		transforms = new ArrayList();
		undo = init;
	}


	/**
	 *  Adds a syntax tree transformation
	 *
	 *@param  value  Description of Parameter
	 */
	public void add(TransformAST value) {
		if (value != null) {
			transforms.add(value);
		}
	}


	/**
	 *  Clears all the transforms
	 */
	public void clear() {
		transforms.clear();
	}


	/**
	 *  Is it worth applying the transforms
	 *
	 *@return    true if there is any
	 */
	public boolean hasAnyChanges() {
		return (transforms.size() > 0);
	}


	/**
	 *  Given a file, applies a set of transformations to it
	 *
	 *@param  inputFile   Description of Parameter
	 *@param  outputFile  Description of Parameter
	 */
	public void apply(File inputFile, File outputFile) {
		//  Get the abstract syntax tree
		ParserFactory factory = new FileParserFactory(inputFile);
		SimpleNode root = factory.getAbstractSyntaxTree(false);

		//  Apply each individual transformation
		int last = transforms.size();
		for (int ndx = 0; ndx < last; ndx++) {
			TransformAST next = (TransformAST) transforms.get(ndx);
			next.update(root);
		}

		//  Check it out if it is read only
		if (!inputFile.canWrite()) {
			checkOut(inputFile);
		}

		//  Print it
		undo.add(inputFile, outputFile);
		createParent(outputFile);
		try {
			(new PrettyPrintFile()).apply(outputFile, root);
		}
		catch (Throwable thrown) {
			ExceptionPrinter.print(thrown);
		}
	}


	/**
	 *  Creates a new file
	 *
	 *@param  file  Description of Parameter
	 */
	public void createFile(File file) {
		undo.add(null, file);
	}


	/**
	 *  Removes an old file
	 *
	 *@param  file  Description of Parameter
	 */
	public void removeFile(File file) {
		undo.add(file, null);
	}


	/**
	 *  Creates the parent directory if it does not exist
	 *
	 *@param  file  the file that is about to be created
	 */
	private void createParent(File file) {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
	}


	/**
	 *  Checks out the specific file from the repository
	 *
	 *@param  file  the file to check out
	 */
	private void checkOut(File file) {
		VersionControl controller = VersionControlFactory.get();
		String filename;

		try {
			filename = file.getCanonicalPath();
		}
		catch (IOException ioe) {
			filename = file.getPath();
		}

		controller.checkOut(filename);
	}
}
