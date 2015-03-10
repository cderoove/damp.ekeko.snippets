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
import java.util.LinkedList;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines if a package contains a certain type 
 *
 *@author     Chris Seguin 
 *@created    November 22, 1999 
 */
public abstract class TypeList {
	/**
	 *  Places the query 
	 *
	 *@param  packageName  the name of the package 
	 *@return              the list of types 
	 */
	public LinkedList query(String packageName) {
		return query(PackageSummary.getPackageSummary(packageName));
	}


	/**
	 *  Places the query 
	 *
	 *@param  summary  the package summary 
	 *@return          the list of types 
	 */
	public LinkedList query(PackageSummary summary) {
		LinkedList list = new LinkedList();

		Iterator iter = summary.getFileSummaries();
		if (iter != null) {
			while (iter.hasNext()) {
				FileSummary fileSummary = (FileSummary) iter.next();

				if (isIncluded(fileSummary)) {
					add(fileSummary, list);
				}
			}
		}

		return list;
	}


	/**
	 *  Determines if the types in the file should be included or not 
	 *
	 *@param  summary  the summary to check 
	 *@return          true if it should be included 
	 */
	protected abstract boolean isIncluded(FileSummary summary);


	/**
	 *  Determines if the types in the file should be included or not 
	 *
	 *@param  summary  the summary to check 
	 *@return          true if it should be included 
	 */
	protected abstract boolean isIncluded(TypeSummary summary);


	/**
	 *  Adds the type in a certain file 
	 *
	 *@param  summary  the file summary 
	 *@param  list     the linked list to add to 
	 */
	private void add(FileSummary summary, LinkedList list) {
		Iterator iter = summary.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary typeSummary = (TypeSummary) iter.next();
				if (isIncluded(typeSummary)) {
					list.add(typeSummary.getName());
				}
			}
		}
	}
}
