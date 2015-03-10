/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.undo;

import java.io.File;

import org.acm.seguin.io.DirectoryTreeTraversal;

/**
 *  Clase responsible for cleaning up all the undo files that are left on the
 *  hard disk.
 *
 *@author    Chris Seguin
 */
public class UndoCleaner extends DirectoryTreeTraversal {
	/**
	 *  Constructor for the UndoCleaner object
	 *
	 *@param  dir  Description of Parameter
	 */
	public UndoCleaner(String dir) {
		super(dir);

		UndoStack.get().delete();
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile) {
		String filename = currentFile.getName();
		int index = filename.indexOf(".java.");
		if (index < 0) {
			return false;
		}

		try {
			int value = Integer.parseInt(filename.substring(index + 6));
			return true;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile) {
		currentFile.delete();
	}


	/**
	 *  The main program for the UndoCleaner class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			(new UndoCleaner(args[0])).go();
		}
		else {
			String dir = System.getProperty("user.dir");
			(new UndoCleaner(dir)).go();
		}
	}
}
