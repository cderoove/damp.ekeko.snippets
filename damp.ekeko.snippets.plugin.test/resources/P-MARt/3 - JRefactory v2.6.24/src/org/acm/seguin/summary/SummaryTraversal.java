/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.acm.seguin.io.DirectoryTreeTraversal;
import org.acm.seguin.summary.load.LoadStatus;
import org.acm.seguin.summary.load.SwingLoadStatus;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Summarizes a directory structure
 *
 *@author     Chris Seguin
 *@created    June 6, 1999
 */
public class SummaryTraversal extends DirectoryTreeTraversal {
	private String root;
	private String blockDirectories;
	private LoadStatus status;
	private static FrameworkLoader framework = null;


	/**
	 *  Traverses a directory tree structure and generates a summary of the
	 *  classes.
	 *
	 *@param  init  the initial directory
	 */
	public SummaryTraversal(String init)
	{
		this(init, new SwingLoadStatus());
	}


	/**
	 *  Traverses a directory tree structure and generates a summary of the
	 *  classes.
	 *
	 *@param  init        the initial directory
	 *@param  initStatus  Description of Parameter
	 */
	public SummaryTraversal(String init, LoadStatus initStatus)
	{
		super(init);
		root = init;
		status = initStatus;
		if (framework == null) {
			framework = new FrameworkFileSummaryLoader(status);
		}

		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			umlBundle.setContinuallyReload(true);
			blockDirectories = umlBundle.getString("skip.dir");
			if (blockDirectories == null) {
				blockDirectories = "";
			}
			else {
				blockDirectories = blockDirectories.trim();
				if (blockDirectories == null) {
					blockDirectories = "";
				}
			}
		}
		catch (MissingSettingsException mse) {
			blockDirectories = "";
		}
	}


	/**
	 *  Method that starts the traversal to generate the summaries.
	 */
	public void go()
	{
		framework.run();

		File temp = new File(root);
		String dir = null;
		try {
			dir = temp.getCanonicalPath();
		}
		catch (IOException ioe) {
			dir = temp.getPath();
		}

		status.setRoot(dir);
		FileSummary.removeDeletedSummaries();
		super.go();
		status.done();
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile)
	{
		String name = currentFile.getName();
		int dot = name.indexOf(".");
		int java = name.indexOf(".java");

		return (dot == java) && name.endsWith(".java");
	}


	/**
	 *  Are we allowed to traverse this directory?
	 *
	 *@param  currentDirectory  the directory that we are about to enter
	 *@return                   true if we are allowed to enter it
	 */
	protected boolean isAllowed(File currentDirectory)
	{
		if ((blockDirectories == null) || (blockDirectories.length() == 0)) {
			return true;
		}

		StringTokenizer tok = new StringTokenizer(blockDirectories, File.pathSeparator);
		while (tok.hasMoreTokens()) {
			String next = tok.nextToken();
			if (currentDirectory.getName().indexOf(next) >= 0) {
				return false;
			}
		}

		return true;
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile)
	{
		try {
			status.setCurrentFile(currentFile.getPath());
			FileSummary.getFileSummary(currentFile);

			Thread.currentThread().yield();
		}
		catch (Throwable oops) {
			System.out.println("\nError loading:  " + currentFile.getName());
			oops.printStackTrace(System.out);
		}
	}


	/**
	 *  Sets the framework loader
	 *
	 *@param  value  The new FrameworkLoader value
	 */
	public static void setFrameworkLoader(FrameworkLoader value)
	{
		framework = value;
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args)
	{
		if (args.length == 0) {
			(new SummaryTraversal(System.getProperty("user.dir"))).go();
		}
		else {
			(new SummaryTraversal(args[0])).go();
		}

		debug();
		System.exit(0);
	}


	/**
	 *  Print everything for debugging purposes
	 */
	public static void debug()
	{
		//  Now print everything
		PrintVisitor printer = new PrintVisitor();
		Iterator iter = PackageSummary.getAllPackages();
		while (iter.hasNext()) {
			PackageSummary next = (PackageSummary) iter.next();
			next.accept(printer, "");
		}
	}
}
