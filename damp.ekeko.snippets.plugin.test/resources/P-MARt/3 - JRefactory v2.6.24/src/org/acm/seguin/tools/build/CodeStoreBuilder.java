/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
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
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class CodeStoreBuilder extends DirectoryTreeTraversal {
	private LinkedList directoryList;
	private PrintWriter output;
	private int rootLength;
	private boolean first;
	private String jarFile;
	private int directoryCount;

	private final static int MAX_DIRS = 3;


	/**
	 *  Constructor for the CodeStoreBuilder object
	 *
	 *@param  jarFile  Description of Parameter
	 *@param  rootDir  Description of Parameter
	 *@param  out      Description of Parameter
	 */
	public CodeStoreBuilder(String jarFile, String rootDir, PrintWriter out) {
		super(rootDir);
		directoryList = new LinkedList();
		output = out;
		first = true;
		rootLength = rootDir.length();
		this.jarFile = jarFile;
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

			if (directoryCount % MAX_DIRS == 0) {
				if (first) {
					output.print("jar cf " + jarFile);
					first = false;
				}
				else {
					output.print("jar uf " + jarFile);
				}
			}
			directoryCount++;

			if (parentString.length() == rootLength) {
				output.print(" *.java *.html");
			}
			else {
				output.print(" " + parentString.substring(rootLength + 1) +
						File.separator + "*.java" +
						" " + parentString.substring(rootLength + 1) +
						File.separator + "*.html");
			}

			if (directoryCount % MAX_DIRS == 0) {
				output.println("");
			}
		}
	}


	/**
	 *  The main program for the CodeStoreBuilder class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		String jarFile = "code.jar";
		String directory = System.getProperty("user.dir");
		PrintWriter out = null;

		if (args.length > 0) {
			jarFile = args[0];
		}

		try {
			if (args.length > 1) {
				out = new PrintWriter(new FileWriter(args[1]));
			}
			else {
				out = new PrintWriter(new OutputStreamWriter(System.out));
			}
		}
		catch (IOException ioe) {
			System.out.println("Unable to create the output file:  " + args[0]);
			return;
		}

		if (args.length > 2) {
			directory = args[2];
		}

		(new CodeStoreBuilder(jarFile, directory, out)).go();

		out.println("");

		out.flush();
		out.close();
	}
}
