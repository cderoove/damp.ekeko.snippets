/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.uml.loader.ReloaderSingleton;

/**
 *  Dialog box that runs a refactoring
 *
 *@author    Chris Seguin
 */
abstract class RefactoringDialog extends JDialog implements ActionListener {
	private UMLPackage currentPackage;


	/**
	 *  Constructor for the RefactoringDialog object
	 *
	 *@param  init  the current package
	 */
	public RefactoringDialog(UMLPackage init) {
		currentPackage = init;
	}


	/**
	 *  Constructor for the RefactoringDialog object
	 *
	 *@param  init  the current package
	 */
	public RefactoringDialog(UMLPackage init, JFrame frame) {
		super(frame);
		currentPackage = init;
	}


	/**
	 *  Respond to a button press
	 *
	 *@param  evt  The action event
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("OK")) {
			dispose();
			runRefactoring();
		}
		else if (evt.getActionCommand().equals("Cancel")) {
			dispose();
		}

		if (currentPackage != null) {
			currentPackage.repaint();
		}
	}


	/**
	 *  Returns the current UML package
	 *
	 *@return    the package
	 */
	protected UMLPackage getUMLPackage() {
		return currentPackage;
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
		catch (Throwable thrown) {
			ExceptionPrinter.print(thrown);
			ExceptionPrinter.dialog(thrown);
		}

		followup(refactoring);
	}

	/**
	 *  Follows up the refactoring by updating the
	 *  class diagrams
	 */
	protected void followup(Refactoring refactoring) {
		updateSummaries();

		//  Update the GUIs
		ReloaderSingleton.reload();
	}
}
