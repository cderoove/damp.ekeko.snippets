/*
 * Author:  Chris Seguin
 * 
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.PopupMenuListener;
import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.uml.loader.ReloaderSingleton;

/**
 *  Performs a refactoring that requires no further user input 
 *
 *@author    Chris Seguin 
 */
public abstract class NoInputRefactoringListener extends PopupMenuListener {
	private UMLPackage currentPackage;


	/**
	 *  Constructor for the NoInputRefactoringListener object 
	 *
	 *@param  initPackage  the UML package that is being operated on 
	 *@param  initMenu     The popup menu 
	 *@param  initItem     The current item 
	 */
	public NoInputRefactoringListener(UMLPackage initPackage, 
			JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		currentPackage = initPackage;
	}


	/**
	 *  A menu item has been selected, display the dialog box 
	 *
	 *@param  evt  the action event 
	 */
	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt);
		runRefactoring();
	}


	/**
	 *  Creates a refactoring to be performed 
	 *
	 *@return    the refactoring 
	 */
	protected abstract Refactoring createRefactoring();


	/**
	 *  Do any necessary updates to the summaries after the refactoring is 
	 *  complete 
	 */
	protected void updateSummaries() {
	}


	/**
	 *  Adds an abstract parent class to all specified classes. 
	 */
	private void runRefactoring() {
		Refactoring refactoring = createRefactoring();

		//  Update the code
		try {
			refactoring.run();
		}
		catch (RefactoringException re) {
			JOptionPane.showMessageDialog(null, re.getMessage(), "Refactoring Exception", 
					JOptionPane.ERROR_MESSAGE);
		}

		updateSummaries();

		//  Update the GUIs
		ReloaderSingleton.reload();
	}
}
