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

import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Basic dialog box taht lists all the children classes
 *
 *@author    Chris Seguin
 */
abstract class ChildrenCheckboxDialog extends RefactoringDialog {
	/**
	 *  List of type checkboxes
	 */
	protected ChildClassCheckboxPanel children;
	/**
	 *  The parent type summary
	 */
	protected TypeSummary parentType;


	/**
	 *  Constructor for the ChildrenCheckboxDialog object
	 *
	 *@param  init      the package diagram
	 *@param  initType  the parent type
	 */
	public ChildrenCheckboxDialog(UMLPackage init, TypeSummary initType)
	{
		super(init);

		parentType = initType;

		getContentPane().setLayout(new BorderLayout());

		children = new ChildClassCheckboxPanel(parentType);
		getContentPane().add(children, BorderLayout.NORTH);

		JButton okButton = new JButton("OK");
		getContentPane().add(okButton, BorderLayout.WEST);
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		getContentPane().add(cancelButton, BorderLayout.EAST);
		cancelButton.addActionListener(this);

		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}
}
