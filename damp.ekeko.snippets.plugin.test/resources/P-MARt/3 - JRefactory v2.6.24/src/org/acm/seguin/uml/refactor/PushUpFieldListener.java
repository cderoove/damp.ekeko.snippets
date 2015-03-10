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
import org.acm.seguin.refactor.field.PushUpFieldRefactoring;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Pushes a field into the parent class
 *
 *@author    Chris Seguin
 */
public class PushUpFieldListener extends NoInputRefactoringListener
{
	private TypeSummary typeSummary;
	private String name;


	/**
	 *  Constructor for the PushUpFieldListener object
	 *
	 *@param  initPackage  the UML package that is being operated on
	 *@param  initType     the type that is being removed
	 *@param  initMenu     The popup menu
	 *@param  initItem     The current item
	 *@param  initField    Description of Parameter
	 */
	public PushUpFieldListener(UMLPackage initPackage,
			TypeSummary initType, FieldSummary fieldSummary,
			JPopupMenu initMenu, JMenuItem initItem)
	{
		super(initPackage, initMenu, initItem);
		typeSummary = initType;
		name = fieldSummary.getName();
		if (typeSummary == null) {
			typeSummary = (TypeSummary) fieldSummary.getParent();
		}
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		PushUpFieldRefactoring puff = RefactoringFactory.get().pushUpField();
		puff.setClass(typeSummary);
		puff.setField(name);
		return puff;
	}
}
