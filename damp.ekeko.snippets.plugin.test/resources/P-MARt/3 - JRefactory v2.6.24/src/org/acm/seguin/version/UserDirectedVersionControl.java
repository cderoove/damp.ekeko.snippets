/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.version;

import javax.swing.JOptionPane;

/**
 *  User directed version control 
 *
 *@author    Chris Seguin 
 */
public class UserDirectedVersionControl implements VersionControl {
	/**
	 *  Determines if a file is under version control 
	 *
	 *@param  fullFilename  The full path of the file 
	 *@return               Returns true if the files is under version control 
	 */
	public boolean contains(String fullFilename) {
		return (JOptionPane.YES_OPTION == 
				JOptionPane.showConfirmDialog(null, 
				"Does your source control system contain\n" + fullFilename + 
				"?", 
				"Contains", 
				JOptionPane.YES_NO_OPTION));
	}


	/**
	 *  Adds a file to version control 
	 *
	 *@param  fullFilename  the file to add 
	 */
	public void add(String fullFilename) {
		JOptionPane.showMessageDialog(null, 
				"Please add\n" + fullFilename + 
				"\nfrom your version control system", 
				"Add", 
				JOptionPane.QUESTION_MESSAGE);
	}


	/**
	 *  Checks in a file 
	 *
	 *@param  fullFilename  the file to check in 
	 */
	public void checkIn(String fullFilename) {
		JOptionPane.showMessageDialog(null, 
				"Please check in\n" + fullFilename + 
				"\nto your version control system", 
				"Check in", 
				JOptionPane.QUESTION_MESSAGE);
	}


	/**
	 *  Check out a file 
	 *
	 *@param  fullFilename  the file to check out 
	 */
	public void checkOut(String fullFilename) {
		JOptionPane.showMessageDialog(null, 
				"Please check out\n" + fullFilename + 
				"\nfrom your version control system", 
				"Check out", 
				JOptionPane.QUESTION_MESSAGE);
	}
}
