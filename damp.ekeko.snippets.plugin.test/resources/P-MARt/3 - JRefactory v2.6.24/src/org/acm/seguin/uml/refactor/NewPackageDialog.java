/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.MoveClass;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.loader.ReloaderSingleton;

/**
 *  Creates a dialog box to prompt for the new package name
 *
 *@author    Chris Seguin
 */
public class NewPackageDialog extends JDialog implements ActionListener {
	//  Instance Variables
	private JComboBox packageName;
	private TypeSummary[] typeArray;


	/**
	 *  Constructor for NewPackageDialog
	 *
	 *@param  initTypes  Description of Parameter
	 */
	public NewPackageDialog(TypeSummary initTypes[])
	{
		super();

		typeArray = initTypes;

		//  Set the window size and layout
		setTitle("Move class to new package");
		GridBagLayout gridbag = new GridBagLayout();
		getContentPane().setLayout(gridbag);
		setSize(310, 150);

		//  Add components
		PackageList packageList = new PackageList();
		packageName = packageList.add(this);

		JButton okButton = new JButton("OK");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gridbag.setConstraints(okButton, gbc);
		okButton.addActionListener(this);
		getContentPane().add(okButton);

		JButton cancelButton = new JButton("Cancel");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gridbag.setConstraints(cancelButton, gbc);
		cancelButton.addActionListener(this);
		getContentPane().add(cancelButton);

		JPanel blank = new JPanel();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		Dimension blankDim = new Dimension(50, cancelButton.getPreferredSize().height * 4);
		blank.setMinimumSize(blankDim);
		blank.setPreferredSize(blankDim);
		getContentPane().add(blank, gbc);

		pack();

		org.acm.seguin.awt.CenterDialog.center(this);
	}


	/**
	 *  Respond to a button press
	 *
	 *@param  evt  the action event
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getActionCommand().equals("OK")) {
			dispose();
			String result = (String) packageName.getSelectedItem();
			if (result.startsWith("<")) {
				result = "";
			}
			repackage(result);
		}
		else if (evt.getActionCommand().equals("Cancel")) {
			dispose();
		}
	}


	/**
	 *  Repackage the files
	 *
	 *@param  destinationPackage  the new package name
	 */
	public void repackage(String destinationPackage)
	{
		//  Create the move class
		MoveClass moveClass = RefactoringFactory.get().moveClass();

		//  Set the destination package
		moveClass.setDestinationPackage(destinationPackage);

		//  Get the files
		String parentDir = null;
		for (int ndx = 0; ndx < typeArray.length; ndx++) {
			parentDir = addType(typeArray[ndx], moveClass);
		}

		//  Run it
		try {
			moveClass.run();
		}
		catch (RefactoringException re) {
			JOptionPane.showMessageDialog(null, re.getMessage(), "Refactoring Exception",
					JOptionPane.ERROR_MESSAGE);
		}

		ReloaderSingleton.reload();
	}


	/**
	 *  Adds a type to the refactoring
	 *
	 *@param  moveClass  the refactoring
	 *@param  type       The feature to be added to the Type attribute
	 *@return            Description of the Returned Value
	 */
	private String addType(TypeSummary type, MoveClass moveClass)
	{
		String parentDir = null;

		FileSummary parent = (FileSummary) type.getParent();
		File file = parent.getFile();
		if (file == null) {
			return null;
		}

		try {
			String canonicalPath = file.getCanonicalPath();
			parentDir = (new File(canonicalPath)).getParent();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}

		moveClass.setDirectory(parentDir);
		moveClass.add(file.getName());

		return parentDir;
	}
}
