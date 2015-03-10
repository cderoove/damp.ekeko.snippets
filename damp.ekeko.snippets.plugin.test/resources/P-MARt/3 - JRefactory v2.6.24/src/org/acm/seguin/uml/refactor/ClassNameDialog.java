/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.acm.seguin.uml.UMLPackage;

/**
 *  Prompts the user for a class name. The class name can then be used to
 *  rename a class, add an abstract parent, or add a child.
 *
 *@author    Chris Seguin
 */
public abstract class ClassNameDialog extends RefactoringDialog {
	//  Instance Variables
	private JTextField newName;


	/**
	 *  Constructor for ClassNameDialog
	 *
	 *@param  init      The package where this operation is occuring
	 *@param  startRow  Description of Parameter
	 */
	public ClassNameDialog(UMLPackage init, int startRow)
	{
		super(init);

		//  Set the window size and layout
		setTitle(getWindowTitle());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().setLayout(gridbag);
		setSize(310, 120);

		//  Add components
		JLabel newNameLabel = new JLabel(getLabelText());
		gbc.gridx = 1;
		gbc.gridy = startRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gridbag.setConstraints(newNameLabel, gbc);
		getContentPane().add(newNameLabel);

		newName = new JTextField();
		newName.setColumns(25);
		gbc.gridx = 2;
		gbc.gridy = startRow;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(newName, gbc);
		getContentPane().add(newName);

		JButton okButton = new JButton("OK");
		gbc.gridx = 2;
		gbc.gridy = startRow + 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(okButton, gbc);
		okButton.addActionListener(this);
		getContentPane().add(okButton);

		JButton cancelButton = new JButton("Cancel");
		gbc.gridx = 3;
		gbc.gridy = startRow + 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gridbag.setConstraints(cancelButton, gbc);
		cancelButton.addActionListener(this);
		getContentPane().add(cancelButton);

		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}


	/**
	 *  Returns the window title
	 *
	 *@return    the title
	 */
	public abstract String getWindowTitle();


	/**
	 *  Gets the label for the text
	 *
	 *@return    the text for the label
	 */
	public abstract String getLabelText();


	/**
	 *  Gets the ClassName attribute of the ClassNameDialog object Gets the
	 *  ClassName attribute of the ClassNameDialog object
	 *
	 *@return    The ClassName value
	 */
	protected String getClassName()
	{
		return newName.getText();
	}
}
