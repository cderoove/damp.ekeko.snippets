/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines if a package contains a certain type 
 *
 *@author     Chris Seguin 
 *@created    November 22, 1999 
 */
public class StayingTypeList extends TypeList {
	/**
	 *  Determines if the types in the file should be included or not 
	 *
	 *@param  summary  the summary to check 
	 *@return          true if it should be included 
	 */
	protected boolean isIncluded(FileSummary summary) {
		return !summary.isMoving();
	}


	/**
	 *  Determines if the types in the file should be included or not 
	 *
	 *@param  summary  the summary to check 
	 *@return          true if it should be included 
	 */
	protected boolean isIncluded(TypeSummary summary) {
		return true;
	}
}
