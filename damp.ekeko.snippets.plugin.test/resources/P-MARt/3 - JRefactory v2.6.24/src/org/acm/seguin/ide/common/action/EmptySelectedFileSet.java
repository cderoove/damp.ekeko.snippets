/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import java.io.File;

import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.summary.TypeSummary;

/**
 *  The concrete implementation of this class for JBuilder
 *
 *@author    Chris Seguin
 */
public class EmptySelectedFileSet extends SelectedFileSet {
	/**
	 *  Constructor for the EmptySelectedFileSet object
	 *
	 *@param  init  Description of Parameter
	 */
	public EmptySelectedFileSet()
	{
	}


	/**
	 *  Gets the AllJava attribute of the SelectedFileSet object
	 *
	 *@return    The AllJava value
	 */
	public boolean isAllJava()
	{
		return isSingleJavaFile();
	}



	/**
	 *  Gets the SingleJavaFile attribute of the SelectedFileSet object
	 *
	 *@return    The SingleJavaFile value
	 */
	public boolean isSingleJavaFile()
	{
		File file = EditorOperations.get().getFile();
		if (file == null)
			return false;

		String name = file.getName();
		return name.endsWith(".java");
	}


	/**
	 *  Gets the TypeSummaryArray attribute of the SelectedFileSet object
	 *
	 *@return    The TypeSummaryArray value
	 */
	public TypeSummary[] getTypeSummaryArray()
	{
		return null;
	}
}
