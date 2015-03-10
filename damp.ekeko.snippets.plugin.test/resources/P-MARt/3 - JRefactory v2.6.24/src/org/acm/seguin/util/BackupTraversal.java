/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.util;

import java.io.File;

import org.acm.seguin.io.DirectoryTreeTraversal;
import org.acm.seguin.io.FileCopy;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Traverses a directory structure and backups all java files found
 *
 *@author     Chris Seguin
 *@created    October 31, 1999
 *@date       May 12, 1999
 */
public class BackupTraversal extends DirectoryTreeTraversal {
	//  Instance Variables
	private String dest;


	/**
	 *  Traverses a directory tree structure
	 *
	 *@param  init  the initial directory
	 *@param  out   the output directory
	 */
	public BackupTraversal(String init, String out) {
		super(init);
		if (out.charAt(out.length() - 1) != File.separatorChar) {
			dest = out + File.separator;
		}
		else {
			dest = out;
		}
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile) {
		String filename = currentFile.getName();
		String lowercase = filename.toLowerCase();
		if (!lowercase.endsWith(".java")) {
			return false;
		}

		String classname = lowercase.substring(0, lowercase.length() - 5) + ".class";
		File classFile = new File(currentFile.getParentFile(), classname);
		return classFile.exists();
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile) {
		String destString = getDestination(currentFile);
		File destFile = new File(destString);
		(new FileCopy(currentFile, destFile)).run();
	}


	/**
	 *  Program called when we arrive at a directory
	 *
	 *@param  current  the current directory
	 */
	protected void arriveAtDir(File current) {
		String currentPath = current.getPath();
		String base = "";
		if (currentPath.startsWith("./") || currentPath.startsWith(".\\")) {
			base = currentPath.substring(2);
		}
		else {
			base = currentPath;
		}
		createDir(dest + "src/" + base);
		createDir(dest + "test/src/" + base);
	}


	/**
	 *  Returns the destination file from the current file
	 *
	 *@param  current  the current file
	 *@return          the destination file
	 */
	private String getDestination(File current) {
		String prefix = "src/";
		if (current.getName().startsWith("Test")) {
			prefix = "test/src/";
		}

		String currentPath = current.getPath();
		if (currentPath.startsWith("./") || currentPath.startsWith(".\\")) {
			return dest + prefix + currentPath.substring(2);
		}
		else {
			return dest + prefix + currentPath;
		}
	}


	/**
	 *  The main program
	 *
	 *@param  args  Description of Parameter
	 */
	public static void main(String[] args) {
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		if (args.length != 2) {
			System.out.println("Syntax:  java BackupTraversal source dest");
			return;
		}
		(new BackupTraversal(args[0], args[1])).go();
	}


	/**
	 *  Creates a named directory if it does not exist
	 */
	private void createDir(String destDir) {
		File destDirFile = new File(destDir);
		if (destDirFile.exists()) {
			//  Nothing to do
		}
		else {
			destDirFile.mkdirs();
		}
	}
	/**
	 *  Creates a named directory if it does not exist
	 */
	private void deleteDir(String destDir) {
		File destDirFile = new File(destDir);
		String[] children = destDirFile.list();
		if (children.length == 0) {
			destDirFile.delete();
		}
	}

	/**
	 *  Program called when we arrive at a directory
	 *
	 *@param  currentFile  the current file
	 */
	protected void leaveDir(File current) {
		String currentPath = current.getPath();
		String base = "";
		if (currentPath.startsWith("./") || currentPath.startsWith(".\\")) {
			base = currentPath.substring(2);
		}
		else {
			base = currentPath;
		}
		deleteDir(dest + "src/" + base);
		deleteDir(dest + "test/src/" + base);
	}
}
