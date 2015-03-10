/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import org.acm.seguin.summary.PackageSummary;

/**
 *  The package list filter that determines if the package should be included
 *  in the PackageSelectorArea.
 *
 *@author    Chris Seguin
 */
public abstract class PackageListFilter {
	private static PackageListFilter singleton;


	/**
	 *  Returns true if we should include it
	 *
	 *@param  summary  the summary in question
	 *@return          true if it should be included
	 */
	public abstract boolean isIncluded(PackageSummary summary);


	/**
	 *  Sets the Singleton attribute of the PackageListFilter class
	 *
	 *@param  value  The new Singleton value
	 */
	public static void setSingleton(PackageListFilter value)
	{
		singleton = value;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public static PackageListFilter get()
	{
		if (singleton == null) {
			singleton = new DefaultPackageListFilter();
		}
		return singleton;
	}
}
