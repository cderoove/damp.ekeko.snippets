/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines if a specified package contains a class with that name 
 *
 *@author     Chris Seguin 
 *@created    November 28, 1999 
 */
public class PackageContainsClass {
	/**
	 *  Checks to see if the package contains a class with that name 
	 *
	 *@param  packageName  the name of the package 
	 *@param  className    the name of the class 
	 *@return              true if it is included 
	 */
	public static boolean query(String packageName, String className) {
		return query(PackageSummary.getPackageSummary(packageName), className);
	}


	/**
	 *  Checks to see if the package contains a class with that name 
	 *
	 *@param  summary    the summary of the package 
	 *@param  className  the name of the class 
	 *@return            true if it is included 
	 */
	public static boolean query(PackageSummary summary, String className) {
		Iterator iter = summary.getFileSummaries();
		if (iter != null) {
			while (iter.hasNext()) {
				FileSummary next = (FileSummary) iter.next();
				if (checkFile(next, className)) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 *  Checks a single file for the class 
	 *
	 *@param  summary    the file summary 
	 *@param  className  the name of the class 
	 *@return            true if this particular file contains that class 
	 */
	private static boolean checkFile(FileSummary summary, String className) {
		Iterator iter = summary.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				if (next.getName().equals(className)) {
					return true;
				}
			}
		}

		return false;
	}
}
