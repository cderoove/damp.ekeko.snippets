/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.version;

/**
 *  Interact with version control 
 *
 *@author     Chris Seguin 
 *@created    June 29, 1999 
 */
public interface VersionControl {
	/**
	 *  Determines if a file is under version control 
	 *
	 *@param  fullFilename  The full path of the file 
	 *@return               Returns true if the files is under version control 
	 */
	public boolean contains(String fullFilename);


	/**
	 *  Adds a file to version control 
	 *
	 *@param  fullFilename  the file to add 
	 */
	public void add(String fullFilename);


	/**
	 *  Checks in a file 
	 *
	 *@param  fullFilename  the file to check in 
	 */
	public void checkIn(String fullFilename);


	/**
	 *  Check out a file 
	 *
	 *@param  fullFilename  the file to check out 
	 */
	public void checkOut(String fullFilename);
}
