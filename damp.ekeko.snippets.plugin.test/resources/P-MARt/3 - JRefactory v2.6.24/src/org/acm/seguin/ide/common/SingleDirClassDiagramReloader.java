/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

/**
 *  Loads the class diagrams based on a single directory
 *
 *@author    Chris Seguin
 */
public class SingleDirClassDiagramReloader extends ClassDiagramReloader {
	private String base;
	private boolean necessary;


	/**
	 *  Constructor for the SingleDirClassDiagramReloader object
	 */
	public SingleDirClassDiagramReloader() {
		super();
		base = null;
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
	 *  Sets the directory to load the data from
	 *
	 *@param  value  the directory
	 */
	public void setRootDirectory(String value) {
		base = value;
	}


	/**
	 *  Reload the summary information and update the diagrams
	 */
	public void reload() {
		if (!necessary) {
			return;
		}

		(new SummaryLoaderThread(base)).start();

		reloadDiagrams();
	}
}
