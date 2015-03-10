/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.refactor.ExtractMethodDialog;

/**
 *  JBuilder's method to extract a file
 *
 *@author    Chris Seguin
 */
class GenericExtractMethod extends ExtractMethodDialog {
	/**
	 *  Constructor for the JBuilderExtractMethod object
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	public GenericExtractMethod() throws RefactoringException
	{
		super(EditorOperations.get().getEditorFrame());
	}


	/**
	 *  Sets the StringInIDE attribute of the JBuilderExtractMethod object
	 *
	 *@param  value  The new StringInIDE value
	 */
	protected void setStringInIDE(String value)
	{
		EditorOperations.get().setStringInIDE(value);
	}


	/**
	 *  Gets the StringFromIDE attribute of the JBuilderExtractMethod object
	 *
	 *@return    The StringFromIDE value
	 */
	protected String getStringFromIDE()
	{
		return EditorOperations.get().getStringFromIDE();
	}


	/**
	 *  Gets the SelectionFromIDE attribute of the JBuilderExtractMethod object
	 *
	 *@return    The SelectionFromIDE value
	 */
	protected String getSelectionFromIDE()
	{
		return EditorOperations.get().getSelectionFromIDE();
	}
}
