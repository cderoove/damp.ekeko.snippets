/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

/**
 *  Factory for field refactorings
 *
 *@author    Chris Seguin
 */
public class FieldRefactoringFactory {
	/**
	 *  Moves the field into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushDownFieldRefactoring pushDownField()
	{
		return new PushDownFieldRefactoring();
	}


	/**
	 *  Moves the field into the child class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpFieldRefactoring pushUpField()
	{
		return new PushUpFieldRefactoring();
	}


	/**
	 *  Renames a field
	 *
	 *@return    The refactoring
	 */
	public RenameFieldRefactoring renameField()
	{
		return new RenameFieldRefactoring();
	}
}
