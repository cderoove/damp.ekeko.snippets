/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.undo;

import java.io.File;
import java.io.Serializable;

import org.acm.seguin.summary.FileSummary;

/**
 *  A set of files that are bound together by the undo operation. This object
 *  stores three files. The first file is the original file name. The second
 *  file is the renamed instance of the original file. The third file is the
 *  new file. <P>
 *
 *  To undo a refactoring on this particular file you need to delete the old
 *  file, then rename the renamed file back to the original file.
 *
 *@author    Chris Seguin
 */
public class FileSet implements Serializable {
	/**
	 *  The original file
	 *
	 *@serial true
	 */
	private File original;

	/**
	 *  The renamed file
	 *
	 *@serial true
	 */
	private File renamed;

	/**
	 *  The new file
	 *
	 *@serial true
	 */
	private File newName;


	/**
	 *  Creates a set of files that can be undone.
	 *
	 *@param  one    the original file
	 *@param  two    the renamed orginal
	 *@param  three  the new file
	 */
	public FileSet(File one, File two, File three) {
		original = one;
		renamed = two;
		newName = three;
	}


	/**
	 *  Performs the undo operation on this particular file
	 */
	public void undo() {
		if (newName != null) {
			FileSummary.removeFileSummary(newName);
			newName.delete();
		}

		if (original != null) {
			renamed.renameTo(original);
			FileSummary.getFileSummary(original);
		}
	}
}

