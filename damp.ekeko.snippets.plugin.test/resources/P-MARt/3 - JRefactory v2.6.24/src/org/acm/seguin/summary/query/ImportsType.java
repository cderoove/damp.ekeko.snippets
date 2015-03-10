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
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines if a particular type is imported
 *
 *@author    Chris Seguin
 */
public class ImportsType {
	/**
	 *  Checks to see if the type is imported
	 *
	 *@param  summary  Description of Parameter
	 *@param  type     Description of Parameter
	 *@return          true if it is imported
	 */
	public static boolean query(Summary summary, TypeSummary type)
	{
		//  Check the special cases first
		PackageSummary packageSummary = getPackageSummary(type);
		if (packageSummary.getName().equals("java.lang")) {
			return true;
		}

		PackageSummary destPackage = getPackageSummary(summary);
		if (packageSummary == destPackage) {
			return true;
		}

		//  Now we need to search the list of imports
		FileSummary fileSummary = getFileSummary(summary);
		Iterator iter = fileSummary.getImports();

		if (iter != null) {
			while (iter.hasNext()) {
				ImportSummary next = (ImportSummary) iter.next();
				if (packageSummary == next.getPackage()) {
					if (next.getType() == null) {
						return true;
					}
					else if (next.getType().equals(type.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 *  Gets the FileSummary attribute of the ImportsType class
	 *
	 *@param  summary  Description of Parameter
	 *@return          The FileSummary value
	 */
	private static FileSummary getFileSummary(Summary summary)
	{
		Summary current = summary;
		while ((current != null) && !(current instanceof FileSummary)) {
			current = current.getParent();
		}

		return (FileSummary) current;
	}


	/**
	 *  Gets the PackageSummary attribute of the ImportsType class
	 *
	 *@param  summary  Description of Parameter
	 *@return          The PackageSummary value
	 */
	private static PackageSummary getPackageSummary(Summary summary)
	{
		Summary current = summary;
		while ((current != null) && !(current instanceof PackageSummary)) {
			current = current.getParent();
		}

		return (PackageSummary) current;
	}
}
