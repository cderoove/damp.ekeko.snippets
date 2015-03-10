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
 *  This is the default filter that is currently being used by the system. It
 *  allows all packages that have at least one type summary to be included in
 *  the list.
 *
 *@author    Chris Seguin
 */
public class DefaultPackageListFilter extends PackageListFilter {
	/**
	 *  Returns true if we should include it
	 *
	 *@param  summary  the summary in question
	 *@return          true if it should be included
	 */
	public boolean isIncluded(PackageSummary summary)
	{
		return summary.getFileSummaries() != null;
	}
}
