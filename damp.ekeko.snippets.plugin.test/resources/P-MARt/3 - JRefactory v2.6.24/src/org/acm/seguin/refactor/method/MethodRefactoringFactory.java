/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class MethodRefactoringFactory
{
	/**
	 *  Moves the method into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpMethodRefactoring pushUpMethod()
	{
		return new PushUpMethodRefactoring();
	}


	/**
	 *  Moves the method signature into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpAbstractMethodRefactoring pushUpAbstractMethod()
	{
		return new PushUpAbstractMethodRefactoring();
	}


	/**
	 *  Moves the method into a child class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushDownMethodRefactoring pushDownMethod()
	{
		return new PushDownMethodRefactoring();
	}


	/**
	 *  Moves the method into another class
	 *
	 *@return    Description of the Returned Value
	 */
	public MoveMethodRefactoring moveMethod()
	{
		return new MoveMethodRefactoring();
	}


	/**
	 *  Extracts code from one method to create a new method
	 *
	 *@return    Description of the Returned Value
	 */
	public ExtractMethodRefactoring extractMethod()
	{
		return new ExtractMethodRefactoring();
	}


	/**
	 *  Renames a parameter
	 *
	 *@return    Description of the Returned Value
	 */
	public RenameParameterRefactoring renameParameter()
	{
		return new RenameParameterRefactoring();
	}
}
