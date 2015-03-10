/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.MoveClass;
import org.acm.seguin.refactor.type.RemoveEmptyClassRefactoring;
import org.acm.seguin.refactor.type.RenameClassRefactoring;

/**
 *  Creates refactorings specific to JBuilder. The refactorings make sure to
 *  close out existing files before the refactoring is performed.
 *
 *@author    Chris Seguin
 */
public class JBuilderRefactoringFactory extends RefactoringFactory {
	/**
	 *  Constructor for the JBuilderRefactoringFactory object
	 */
	protected JBuilderRefactoringFactory()
	{
		super();
	}


	/**
	 *  Creates a move class refactoring object
	 *
	 *@return    the move class refactoring object
	 */
	public MoveClass moveClass()
	{
		prepare();
		return new JBuilderMoveClassRefactoring();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RenameClassRefactoring renameClass()
	{
		prepare();
		return new JBuilderRenameClassRefactoring();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RemoveEmptyClassRefactoring removeEmptyClass()
	{
		prepare();
		return new JBuilderRemoveEmptyClassRefactoring();
	}


	/**
	 *  We use this as an opportunity to save all the files in the system before
	 *  we perform the refactorings.
	 */
	protected void prepare()
	{
		Browser.getActiveBrowser().doSaveAll(true);
	}


	/**
	 *  Register this as the factory
	 */
	public static void register()
	{
		RefactoringFactory.setSingleton(new JBuilderRefactoringFactory());
	}
}
