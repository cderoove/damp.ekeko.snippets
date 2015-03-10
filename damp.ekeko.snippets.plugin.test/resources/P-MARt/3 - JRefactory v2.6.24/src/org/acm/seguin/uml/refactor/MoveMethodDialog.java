/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.BorderLayout;

import javax.swing.JButton;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.method.MoveMethodRefactoring;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  This dialog box selects which parameter the method is being moved into.
 *
 *@author    Chris Seguin
 */
class MoveMethodDialog extends RefactoringDialog {
	/**
	 *  parameter panel
	 */
	private ParameterPanel params;
	/**
	 *  The parent type summary
	 */
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the MoveMethodDialog object
	 *
	 *@param  init        the package diagram
	 *@param  initMethod  Description of Parameter
	 */
	public MoveMethodDialog(UMLPackage init, MethodSummary initMethod)
	{
		super(init);

		methodSummary = initMethod;

		getContentPane().setLayout(new BorderLayout());

		params = new ParameterPanel(methodSummary);
		getContentPane().add(params, BorderLayout.NORTH);

		JButton okButton = new JButton("OK");
		getContentPane().add(okButton, BorderLayout.WEST);
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		getContentPane().add(cancelButton, BorderLayout.EAST);
		cancelButton.addActionListener(this);

		setTitle("Move method " + methodSummary.toString() + "  to:");

		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}


	/**
	 *  Creates a refactoring to be performed
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		MoveMethodRefactoring moveMethod = RefactoringFactory.get().moveMethod();
		moveMethod.setMethod(methodSummary);
		moveMethod.setDestination(params.get());

		return moveMethod;
	}
}
