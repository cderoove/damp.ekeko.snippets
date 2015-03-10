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
 *  Determines if a package contains a certain type 
 *
 *@author     Chris Seguin 
 *@created    November 22, 1999 
 */
public class FileSummaryGetter {
	/**
	 *  Places the query 
	 *
	 *@param  packageName  the name of the package 
	 *@param  typeName     the name of the type 
	 *@return              true if the package contains a type with that name 
	 */
	public FileSummary query(String packageName, String typeName) {
		return query(PackageSummary.getPackageSummary(packageName), 
				typeName);
	}


	/**
	 *  Checks the package to determine if it contains that type 
	 *
	 *@param  summary   the package 
	 *@param  typeName  the type 
	 *@return           true if the type is in the package 
	 */
	public FileSummary query(PackageSummary summary, String typeName) {
		Iterator iter = summary.getFileSummaries();
		if (iter != null) {
			while (iter.hasNext()) {
				FileSummary fileSummary = (FileSummary) iter.next();
				if (query(fileSummary, typeName)) {
					return fileSummary;
				}
			}
		}

		return null;
	}


	/**
	 *  Checks if a specific file contains a type 
	 *
	 *@param  summary   the file 
	 *@param  typeName  the type name 
	 *@return           true if the file contains the type 
	 */
	private boolean query(FileSummary summary, String typeName) {
		Iterator iter = summary.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary typeSummary = (TypeSummary) iter.next();
				if (typeName.equals(typeSummary.getName())) {
					return true;
				}
			}
		}

		return false;
	}
}
