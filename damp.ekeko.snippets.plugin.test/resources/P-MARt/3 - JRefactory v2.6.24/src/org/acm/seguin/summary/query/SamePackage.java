/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines if the package name matches the summary
 *
 *@author     Chris Seguin
 *@created    November 28, 1999
 */
public class SamePackage {
	/**
	 *  Check to see if they are the same package
	 *
	 *@param  packageName  the name of the package
	 *@param  summary      the summary
	 *@return              true if they come from the same package
	 */
	public static boolean query(String packageName, FileSummary summary) {
		PackageSummary parent = (PackageSummary) summary.getParent();
		return packageName.equals(parent.getName());
	}


	/**
	 *  Check to see if they are the same package
	 *
	 *@param  packageName  the name of the package
	 *@param  summary      the summary
	 *@return              true if they come from the same package
	 */
	public static boolean query(String packageName, TypeSummary summary) {
		PackageSummary parent = GetPackageSummary.query(summary);
		return packageName.equals(parent.getName());
	}


	/**
	 *  Check to see if they are the same package
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 *@return      true if they come from the same package
	 */
	public static boolean query(TypeSummary one, TypeSummary two) {
		if ((one == null) || (two == null)) {
			return false;
		}

		PackageSummary firstPackage = getPackageSummary(one);
		PackageSummary secondPackage = getPackageSummary(two);
		return firstPackage.equals(secondPackage);
	}


	/**
	 *  Gets the package summary
	 *
	 *@param  base  Description of Parameter
	 *@return       the package summary
	 */
	private static PackageSummary getPackageSummary(Summary base) {
		Summary current = base;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		return (PackageSummary) current;
	}
}
