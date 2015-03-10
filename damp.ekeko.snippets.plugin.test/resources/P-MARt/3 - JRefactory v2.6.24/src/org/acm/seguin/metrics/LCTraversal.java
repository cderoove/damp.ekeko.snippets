/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.metrics;

import java.io.File;

import org.acm.seguin.io.DirectoryTreeTraversal;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Counts the number of lines in a file
 *
 *@author     Chris Seguin
 *@created    June 30, 1999
 */
public class LCTraversal extends DirectoryTreeTraversal {
	//  Instance Variables
	private long total;
	private int fileCount;


	/**
	 *  Traverses a directory tree structure
	 *
	 *@param  init  the initial directory
	 */
	public LCTraversal(String init)
	{
		super(init);

		total = 0;
		fileCount = 0;
	}


	/**
	 *  Starts the tree traversal
	 */
	public void go()
	{
		super.go();

		long count = total;
		if (count < 10) {
			System.out.print("      " + count);
		}
		else if (count < 100) {
			System.out.print("     " + count);
		}
		else if (count < 1000) {
			System.out.print("    " + count);
		}
		else if (count < 10000) {
			System.out.print("   " + count);
		}
		else if (count < 100000) {
			System.out.print("  " + count);
		}
		else {
			System.out.print(" " + count);
		}
		System.out.println(" total lines in " + fileCount + " files");

		double top = count;
		double bottom = fileCount;

		System.out.println("Average:  " + (top / bottom));
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile)
	{
		String filename = currentFile.getName().toLowerCase();
		return (filename.indexOf(".java") >= 0) ||
				(filename.indexOf(".h") >= 0) ||
				(filename.indexOf(".cpp") >= 0);
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile)
	{
		int count = (new LineCounter(currentFile)).printMessage();
		total += count;
		fileCount++;
	}


	/**
	 *  Main program
	 *
	 *@param  args  Command line arguments
	 */
	public static void main(String[] args)
	{
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		if (args.length == 0) {
			System.out.println("Syntax:  java org.acm.seguin.metrics.LCTraversal <directory>");
			return;
		}

		(new LCTraversal(args[0])).go();
	}
}
