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
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Stores the undo operation. The undo operation consists of a description of
 *  the refactoring that was performed to create this UndoAction and a list of
 *  files that have changed. <P>
 *
 *  The files that have changed are indexed files, in that they have an index
 *  appended to the name of the file.
 *
 *@author    Chris Seguin
 */
public class UndoAction implements Serializable {
	/**
	 *  A description of the refactoring
	 *
	 *@serial    true
	 */
	private String description;

	/**
	 *  A linked list
	 *
	 *@serial    true
	 */
	private LinkedList list;


	/**
	 *  Constructor for the UndoAction object
	 *
	 *@param  init  the description of the action
	 */
	public UndoAction(String init)
	{
		description = init;
		list = new LinkedList();
	}


	/**
	 *  Gets the Description attribute of the UndoAction object
	 *
	 *@return    The Description value
	 */
	public String getDescription()
	{
		return description;
	}


	/**
	 *  Adds a file to this action
	 *
	 *@param  oldFile  the original file
	 *@param  newFile  the new file
	 */
	public void add(File oldFile, File newFile)
	{
		File dest = null;
		if (oldFile == null) {
			dest = null;
		}
		else {
			//  Get the parent directory and the name
			File parent = oldFile.getParentFile();
			String name = oldFile.getName();

			//  Find the file's next index location
			dest = findNextStorageSlot(parent, name);

			//  Renames the file
			oldFile.renameTo(dest);
		}

		list.add(new FileSet(oldFile, dest, newFile));
	}


	/**
	 *  Undo the current action
	 */
	public void undo()
	{
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			((FileSet) iter.next()).undo();
		}
	}


	/**
	 *  Gets the NextName attribute of the UndoAction object
	 *
	 *@param  name     Description of Parameter
	 *@param  index    Description of Parameter
	 *@param  pattern  Description of Parameter
	 *@return          The NextName value
	 */
	private String getNextName(String name, int index, String pattern)
	{
		StringBuffer buffer = new StringBuffer(name);
		char ch;

		for (int ndx = 0; ndx < pattern.length(); ndx++) {
			ch = pattern.charAt(ndx);
			if (ch == '#') {
				buffer.append(index);
			}
			else {
				buffer.append(ch);
			}
		}

		return buffer.toString();
	}


	/**
	 *  Finds the next slot to store the original file
	 *
	 *@param  dir   the directory that the original file is contained in
	 *@param  name  the name of the original file
	 *@return       Description of the Returned Value
	 */
	private File findNextStorageSlot(File dir, String name)
	{
		String pattern = ".#";

		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			umlBundle.setContinuallyReload(true);
			pattern = umlBundle.getString("backup.ext");
		}
		catch (MissingSettingsException mse) {
			//  The default is fine
		}

		File dest = null;
		int index = 0;
		do {
			dest = new File(dir, getNextName(name, index, pattern));
			index++;
		} while (dest.exists());
		return dest;
	}
}
