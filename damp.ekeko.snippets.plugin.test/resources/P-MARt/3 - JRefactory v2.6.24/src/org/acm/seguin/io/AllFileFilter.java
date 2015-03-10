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

import javax.swing.filechooser.FileFilter;

/**
 *  Accepts all files 
 *
 *@author     Chris Seguin 
 *@created    May 30, 1999 
 */
public class AllFileFilter extends FileFilter {

	/**
	 *  Return the description of the files accepted 
	 *
	 *@return    the description to be displayed in the file box 
	 */
	public String getDescription() {
		return "All Files (*.*)";
	}


	/**
	 *  Should this file be accepted 
	 *
	 *@param  file  the file under consideration 
	 *@return       true - all files are accepted 
	 */
	public boolean accept(File file) {
		return true;
	}
}
