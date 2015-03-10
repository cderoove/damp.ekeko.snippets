/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.type;

/**
 *  Factory for the type refactorings
 *
 *@author    Chris Seguin
 */
public class TypeRefactoringFactory
{
	/**
	 *  Adds a feature to the Child attribute of the TypeRefactoringFactory
	 *  object
	 *
	 *@return    Description of the Returned Value
	 */
	public AddChildRefactoring addChild()
	{
		return new AddChildRefactoring();
	}


	/**
	 *  Adds a feature to the Parent attribute of the TypeRefactoringFactory
	 *  object
	 *
	 *@return    Description of the Returned Value
	 */
	public AddAbstractParent addParent()
	{
		return new AddAbstractParent();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public MoveClass moveClass()
	{
		return new MoveClass();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RenameClassRefactoring renameClass()
	{
		return new RenameClassRefactoring();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RemoveEmptyClassRefactoring removeEmptyClass()
	{
		return new RemoveEmptyClassRefactoring();
	}


	/**
	 *  Extracts the interface of a class into a new interface object
	 *
	 *@return    Description of the Returned Value
	 */
	public ExtractInterfaceRefactoring extractInterface()
	{
		return new ExtractInterfaceRefactoring();
	}
}
