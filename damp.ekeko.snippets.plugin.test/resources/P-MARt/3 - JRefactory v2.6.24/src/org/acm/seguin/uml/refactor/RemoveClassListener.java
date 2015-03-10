/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.RemoveEmptyClassRefactoring;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Removes a class that has no body to it
 *
 *@author    Chris Seguin
 */
public class RemoveClassListener extends NoInputRefactoringListener
{
	private TypeSummary typeSummary;


	/**
	 *  Constructor for the RemoveClassListener object
	 *
	 *@param  initPackage  the UML package that is being operated on
	 *@param  initType     the type that is being removed
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 */
	public RemoveClassListener(UMLPackage initPackage, TypeSummary initType,
			JPopupMenu initMenu, JMenuItem initItem)
	{
		super(initPackage, initMenu, initItem);
		typeSummary = initType;
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		RemoveEmptyClassRefactoring recr = RefactoringFactory.get().removeEmptyClass();
		recr.setClass(typeSummary);
		return recr;
	}
}
