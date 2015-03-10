/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *  Loads the class diagrams based on a single directory
 *
 *@author    Chris Seguin
 */
public class MultipleDirClassDiagramReloader extends ClassDiagramReloader {
	private LinkedList list;
	private boolean necessary;


	/**
	 *  Constructor for the MultipleDirClassDiagramReloader object
	 */
	public MultipleDirClassDiagramReloader() {
		super();
		list = new LinkedList();
		necessary = false;
	}


	/**
	 *  Sets the Necessary attribute of the MultipleDirClassDiagramReloader
	 *  object
	 *
	 *@param  value  The new Necessary value
	 */
	public void setNecessary(boolean value) {
		necessary = value;
	}


	/**
	 *  Gets the Necessary attribute of the MultipleDirClassDiagramReloader
	 *  object
	 *
	 *@return    The Necessary value
	 */
	public boolean isNecessary() {
		return necessary;
	}


	/**
	 *  Clears all directories in the list
	 */
	public void clear() {
		list.clear();
	}


	/**
	 *  Sets the directory to load the data from
	 *
	 *@param  value  the directory
	 */
	public void addRootDirectory(String value) {
		if (!list.contains(value)) {
			list.add(value);
		}
	}


	/**
	 *  Reload the summary information and update the diagrams
	 */
	public void reload() {
		if (!necessary) {
			return;
		}

		//  Build a list of directories to load
		StringBuffer buffer = new StringBuffer();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			String base = (String) iter.next();
			buffer.append(base);
			if (iter.hasNext()) {
				buffer.append(File.pathSeparator);
			}
		}

		//  Load those directories
		String baseDirs = buffer.toString();
		if (baseDirs.length() > 0) {
			(new SummaryLoaderThread(baseDirs)).start();
		}

		//  Load the diagrams
		reloadDiagrams();
	}
}
