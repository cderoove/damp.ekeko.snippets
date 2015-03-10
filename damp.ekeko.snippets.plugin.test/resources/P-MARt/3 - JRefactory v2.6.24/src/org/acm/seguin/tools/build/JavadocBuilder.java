/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.tools.build;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.acm.seguin.io.DirectoryTreeTraversal;

/**
 *  Tool that creates a list of packages to be processed by javadoc
 *
 *@author    Chris Seguin
 */
public class JavadocBuilder extends DirectoryTreeTraversal {
	private LinkedList directoryList;
	private PrintWriter output;
	private int rootLength;
	private boolean first;
	private int directoryCount;

	private final static int MAX_DIRS = 5;


	/**
	 *  Constructor for the JarDirectoryBuilder object
	 *
	 *@param  rootDir  the directory
	 *@param  out      the output stream
	 */
	public JavadocBuilder(String rootDir, PrintWriter out) {
		super(rootDir);
		directoryList = new LinkedList();
		output = out;
		first = true;
		rootLength = rootDir.length();
		directoryCount = 0;
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile) {
		return currentFile.getName().endsWith(".java");
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile) {
		String parentString = currentFile.getParent();
		//System.out.println("Parent:  " + parentString);
		if (!directoryList.contains(parentString)) {
			directoryList.add(parentString);

			if (parentString.length() == rootLength) {
				//output.println("*.java");
			}
			else {
				String dir = parentString.substring(rootLength + 1);
				String packageName = convert(dir);
				output.println(packageName);
			}
		}
	}


	/**
	 *  Converts a directory path into a package name
	 *
	 *@param  value  The directory path
	 *@return        the package name
	 */
	private String convert(String value) {
		StringBuffer buffer = new StringBuffer();
		for (int ndx = 0; ndx < value.length(); ndx++) {
			if ((value.charAt(ndx) == '\\') || (value.charAt(ndx) == '/')) {
				buffer.append(".");
			}
			else {
				buffer.append(value.charAt(ndx));
			}
		}

		return buffer.toString();
	}


	/**
	 *  The main program for the JarDirectoryBuilder class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		String jarFile = "sample.jar";
		String directory = System.getProperty("user.dir");
		PrintWriter out = null;

		try {
			if (args.length > 0) {
				out = new PrintWriter(new FileWriter(args[0]));
			}
			else {
				out = new PrintWriter(new OutputStreamWriter(System.out));
			}
		}
		catch (IOException ioe) {
			System.out.println("Unable to create the output file:  " + args[0]);
			return;
		}

		if (args.length > 1) {
			directory = args[1];
		}

		(new JavadocBuilder(directory, out)).go();

		out.println("");

		out.flush();
		out.close();
	}
}

