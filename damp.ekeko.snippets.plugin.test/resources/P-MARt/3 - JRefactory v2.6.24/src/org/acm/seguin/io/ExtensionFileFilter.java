/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.io;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

/**
 *  Accepts all files
 *
 *@author     Chris Seguin
 *@created    May 30, 1999
 */
public class ExtensionFileFilter extends FileFilter {
	//  Local Variables
	private String description;
	private Vector extensions;


	/**
	 *  Constructor for the ExtensionFileFilter
	 */
	public ExtensionFileFilter() {
		description = "Unknown set of files";
		extensions = new Vector();
	}


	/**
	 *  Sets the description of the files accepted
	 *
	 *@param  descr  the new description
	 */
	public void setDescription(String descr) {
		if (descr != null) {
			description = descr;
		}
	}


	/**
	 *  Return the description of the files accepted
	 *
	 *@return    the description to be displayed in the file box
	 */
	public String getDescription() {
		return description;
	}


	/**
	 *  Add an extension
	 *
	 *@param  ext  the extension to add
	 */
	public void addExtension(String ext) {
		if (ext != null) {
			extensions.addElement(ext);
		}
	}


	/**
	 *  Should this file be accepted
	 *
	 *@param  file  the file under consideration
	 *@return       true - all files are accepted
	 */
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}

		Enumeration enum_ = extensions.elements();
		while (enum_.hasMoreElements()) {
			String ext = ((String) enum_.nextElement()).toLowerCase();

			String filename = file.getName().toLowerCase();
			if (filename.endsWith(ext)) {
				return true;
			}
		}

		return false;
	}
}
