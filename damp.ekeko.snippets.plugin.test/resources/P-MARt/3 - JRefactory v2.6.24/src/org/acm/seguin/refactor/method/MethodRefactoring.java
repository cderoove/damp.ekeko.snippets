/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  This is a base class that is shared by a number of different
 *  method refactorings.
 *
 *@author    Chris Seguin
 */
abstract class MethodRefactoring extends Refactoring {

	/**
	 *  Description of the Method
	 *
	 *@param  source     Description of Parameter
	 *@param  transform  Description of Parameter
	 *@param  rft        Description of Parameter
	 */
	protected void removeMethod(TypeSummary source, ComplexTransform transform, RemoveMethodTransform rft) {
		transform.add(rft);
		FileSummary fileSummary = (FileSummary) source.getParent();
		transform.apply(fileSummary.getFile(), fileSummary.getFile());
	}
}
