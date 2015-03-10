/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import java.io.File;
import java.util.Iterator;

import org.acm.seguin.refactor.type.MoveClass;

/**
 *  Make sure to close the files before performing this refactoring
 *
 *@author    Chris Seguin
 */
class JBuilderMoveClassRefactoring extends MoveClass {
	/**
	 *  Constructor for the JBuilderMoveClass object
	 */
	protected JBuilderMoveClassRefactoring()
	{
		super();
	}


	/**
	 *  Performs the refactoring by traversing through the files and updating
	 *  them.
	 */
	protected void transform()
	{
		File dir = new File(initDir);
		Iterator iter = fileList.iterator();
		while (iter.hasNext()) {
			File next = new File(dir, (String) iter.next());
			FileCloser.close(next);
		}

		super.transform();
	}
}
