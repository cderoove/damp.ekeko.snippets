/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import java.awt.event.ActionEvent;

import org.acm.seguin.summary.TypeSummary;

/**
 *  Shares the commonality between actions that perform refactorings from
 *  JBuilder's IDE
 *
 *@author    Chris Seguin
 */
abstract class RefactoringAction extends GenericAction {
	private SelectedFileSet selectedFileSet;


	/**
	 *  Constructor for the JBuilderRefactoringAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public RefactoringAction(SelectedFileSet init)
	{
		super();

		selectedFileSet = init;
	}


	/**
	 *  The action to be performed
	 *
	 *@param  evt  the triggering event
	 */
	public void actionPerformed(ActionEvent evt)
	{
		updateMetaData();
		TypeSummary[] typeSummaryArray = getTypeSummaryArray();
		activateListener(typeSummaryArray, evt);

		CurrentSummary.get().reset();
	}


	/**
	 *  Gets the SingleJavaFile attribute of the AddChildClassAction object
	 *
	 *@return    The SingleJavaFile value
	 */
	protected boolean isSingleJavaFile()
	{
		return selectedFileSet.isSingleJavaFile();
	}


	/**
	 *  Gets the AllJava attribute of the AddParentClassAction object
	 *
	 *@return    The AllJava value
	 */
	protected boolean isAllJava()
	{
		return selectedFileSet.isAllJava();
	}


	/**
	 *  Reloads all the metadata before attempting to perform a refactoring.
	 */
	protected void updateMetaData()
	{
		CurrentSummary.get().updateMetaData();
	}


	/**
	 *  The listener to activate with the specified types
	 *
	 *@param  typeSummaryArray  Description of Parameter
	 *@param  evt               Description of Parameter
	 */
	protected abstract void activateListener(TypeSummary[] typeSummaryArray, ActionEvent evt);


	/**
	 *  Gets the TypeSummaryArray attribute of the JBuilderRefactoringAction
	 *  object
	 *
	 *@return    The TypeSummaryArray value
	 */
	private TypeSummary[] getTypeSummaryArray()
	{
		return selectedFileSet.getTypeSummaryArray();
	}
}
