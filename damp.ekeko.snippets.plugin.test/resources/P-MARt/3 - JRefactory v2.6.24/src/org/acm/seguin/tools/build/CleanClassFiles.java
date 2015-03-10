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
public class CleanClassFiles extends DirectoryTreeTraversal {
	private LinkedList directoryList;
	private PrintWriter output;


	/**
	 *  Constructor for the CleanClassFiles object 
	 *
	 *@param  rootDir  Description of Parameter 
	 *@param  out      Description of Parameter 
	 */
	public CleanClassFiles(String rootDir, PrintWriter out) {
		super(rootDir);
		directoryList = new LinkedList();
		output = out;
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
			output.println("del " + parentString + 
					File.separator + "*.class");
		}
	}


	/**
	 *  The main program for the CleanClassFiles class 
	 *
	 *@param  args  The command line arguments 
	 */
	public static void main(String[] args) {
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

		(new CleanClassFiles(directory, out)).go();

		out.flush();
		out.close();
	}
}

