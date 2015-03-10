package org.acm.seguin.summary.query;

import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Finds the package summary associated with this type
 *
 *@author    Chris Seguin
 */
public class GetPackageSummary {
	/**
	 *  Description of the Method
	 *
	 *@param  type  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public static PackageSummary query(TypeSummary type) {
		Summary current = type;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		return (PackageSummary) current;
	}
}
