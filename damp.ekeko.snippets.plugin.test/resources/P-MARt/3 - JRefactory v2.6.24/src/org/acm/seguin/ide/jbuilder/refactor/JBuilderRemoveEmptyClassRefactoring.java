/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import org.acm.seguin.refactor.type.RemoveEmptyClassRefactoring;
import org.acm.seguin.summary.FileSummary;

/**
 *  Be sure to close the file before performing this refactoring
 *
 *@author    Chris Seguin
 */
class JBuilderRemoveEmptyClassRefactoring extends RemoveEmptyClassRefactoring {
	/**
	 *  Constructor for the JBuilderRemoveEmptyClassRefactoring object
	 */
	protected JBuilderRemoveEmptyClassRefactoring()
	{
		super();
	}


	/**
	 *  Performs the refactoring by traversing through the files and updating
	 *  them.
	 */
	protected void transform()
	{
		FileSummary summary = getFileSummary();
		FileCloser.close(summary.getFile());

		super.transform();
	}
}
