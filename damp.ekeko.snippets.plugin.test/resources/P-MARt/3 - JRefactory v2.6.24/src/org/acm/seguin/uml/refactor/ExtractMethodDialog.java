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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.acm.seguin.awt.CenterDialog;
import org.acm.seguin.awt.OrderableList;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.method.ExtractMethodRefactoring;
import org.acm.seguin.summary.VariableSummary;

/**
 *  User interface to enter the name of the method that was just extracted
 *
 *@author    Chris Seguin
 */
public abstract class ExtractMethodDialog extends RefactoringDialog {
	//  Instance Variables
	private JTextField newName;
	private ExtractMethodRefactoring emr;
	private OrderableList list;
	private JRadioButton privateButton;
	private JRadioButton packageButton;
	private JRadioButton protectedButton;
	private JRadioButton publicButton;
	private JList returnList;
	private JTextField returnTextField;
	private JLabel signatureLabel;
	private SignatureUpdateAdapter sua;
	private JLabel sizer;
	private Dimension originalSize;


	/**
	 *  Constructor for the ExtractMethodDialog object
	 *
	 *@param  parent                    the parent frame
	 *@exception  RefactoringException  problem in setting up the refactoring
	 */
	public ExtractMethodDialog(JFrame parent) throws RefactoringException
	{
		super(null, parent);

		sua = new SignatureUpdateAdapter(this);

		init();

		//  Set the window size and layout
		setTitle(getWindowTitle());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().setLayout(gridbag);
		setSize(310, 120);

		Insets zeroInsets = new Insets(0, 0, 0, 0);

		//  Add components
		int currentRow = 1;
		JLabel newNameLabel = new JLabel(getLabelText());
		gbc.gridx = 1;
		gbc.gridy = currentRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 0, 0, 10);
		gridbag.setConstraints(newNameLabel, gbc);
		getContentPane().add(newNameLabel);

		newName = new JTextField();
		newName.setColumns(40);
		newName.getDocument().addDocumentListener(sua);
		newName.addFocusListener(sua);
		gbc.gridx = 2;
		gbc.gridy = currentRow;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = zeroInsets;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(newName, gbc);
		getContentPane().add(newName);

		currentRow++;

		JLabel parameterLabel = new JLabel("Parameters:  ");
		gbc.gridx = 1;
		gbc.gridy = currentRow;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 0, 0);
		getContentPane().add(parameterLabel, gbc);

		currentRow++;

		VariableSummary[] vs = emr.getParameters();
		if (vs.length > 1) {
			list = new OrderableList(vs, new VariableListCellRenderer());
			list.addListDataListener(sua);
			gbc.gridx = 1;
			gbc.gridy = currentRow;
			gbc.gridwidth = 3;
			gbc.gridheight = 1;
			gbc.insets = zeroInsets;
			gbc.fill = GridBagConstraints.CENTER;
			gridbag.setConstraints(list, gbc);
			getContentPane().add(list);
		}
		else {
			JLabel label;
			if (vs.length == 0) {
				label = new JLabel("There are no parameters required for this method");
			}
			else {
				label = new JLabel("There is only one parameter required for this method:  " + vs[0].getName());
			}

			list = null;
			gbc.gridx = 1;
			gbc.gridy = currentRow;
			gbc.gridwidth = 3;
			gbc.gridheight = 1;
			gbc.insets = zeroInsets;
			gbc.fill = GridBagConstraints.CENTER;
			gridbag.setConstraints(label, gbc);
			getContentPane().add(label);
		}

		currentRow++;

		JPanel panel = initRadioButtons();
		gbc.gridx = 1;
		gbc.gridy = currentRow;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.insets = zeroInsets;
		gbc.fill = GridBagConstraints.CENTER;
		gridbag.setConstraints(panel, gbc);
		getContentPane().add(panel);

		currentRow++;

		JLabel returnNameLabel = new JLabel("Return:");
		gbc.gridx = 1;
		gbc.gridy = currentRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 10);
		gridbag.setConstraints(returnNameLabel, gbc);
		getContentPane().add(returnNameLabel);

		gbc.gridx = 2;
		gbc.gridy = currentRow;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = zeroInsets;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		if (emr.isStatement()) {
			returnList = new JList(emr.getReturnTypes());
			returnList.setSelectedIndex(0);
			gridbag.setConstraints(returnList, gbc);
			getContentPane().add(returnList);
			returnList.addListSelectionListener(sua);
			returnTextField = null;
		}
		else {
			returnTextField = new JTextField(emr.getReturnType().toString());
			gridbag.setConstraints(returnTextField, gbc);
			getContentPane().add(returnTextField);
			returnTextField.getDocument().addDocumentListener(sua);
			returnTextField.addFocusListener(sua);
			returnList = null;
		}

		currentRow++;

		JLabel signNameLabel = new JLabel("Signature:");
		gbc.gridx = 1;
		gbc.gridy = currentRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 10);
		gridbag.setConstraints(signNameLabel, gbc);
		getContentPane().add(signNameLabel);

		signatureLabel = new JLabel("");
		gbc.gridx = 2;
		gbc.gridy = currentRow;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.insets = zeroInsets;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(signatureLabel, gbc);
		getContentPane().add(signatureLabel);

		currentRow++;
		JButton okButton = new JButton("OK");
		gbc.gridx = 2;
		gbc.gridy = currentRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = zeroInsets;
		gbc.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(okButton, gbc);
		okButton.addActionListener(this);
		getContentPane().add(okButton);

		JButton cancelButton = new JButton("Cancel");
		gbc.gridx = 3;
		gbc.gridy = currentRow;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gridbag.setConstraints(cancelButton, gbc);
		cancelButton.addActionListener(this);
		getContentPane().add(cancelButton);

		update();

		pack();

		sizer = new JLabel();
		originalSize = signatureLabel.getSize();

		CenterDialog.center(this, parent);
	}


	/**
	 *  Gets the WindowTitle attribute of the ExtractMethodDialog object
	 *
	 *@return    The WindowTitle value
	 */
	public String getWindowTitle()
	{
		return "Extract Method Dialog";
	}


	/**
	 *  Gets the LabelText attribute of the ExtractMethodDialog object
	 *
	 *@return    The LabelText value
	 */
	public String getLabelText()
	{
		return "Method name:";
	}


	/**
	 *  Performs the update to the signature line
	 */
	public void update()
	{
		createRefactoring();

		String signature = emr.getSignature();

		if (sizer != null) {
			sizer.setText(signature);
			Dimension current = sizer.getPreferredSize();
			int length = signature.length();
			while (current.width > originalSize.width) {
				length--;
				signature = signature.substring(0, length) + "...";
				sizer.setText(signature);
				current = sizer.getPreferredSize();
			}
		}

		signatureLabel.setText(signature);
	}


	/**
	 *  Sets the StringInIDE attribute of the ExtractMethodDialog object
	 *
	 *@param  value  The new StringInIDE value
	 */
	protected abstract void setStringInIDE(String value);


	/**
	 *  Gets the StringFromIDE attribute of the ExtractMethodDialog object
	 *
	 *@return    The StringFromIDE value
	 */
	protected abstract String getStringFromIDE();


	/**
	 *  Gets the SelectionFromIDE attribute of the ExtractMethodDialog object
	 *
	 *@return    The SelectionFromIDE value
	 */
	protected abstract String getSelectionFromIDE();


	/**
	 *  Follows up the refactoring by updating the text in the current window
	 *
	 *@param  refactoring  Description of Parameter
	 */
	protected void followup(Refactoring refactoring)
	{
		ExtractMethodRefactoring emr = (ExtractMethodRefactoring) refactoring;
		setStringInIDE(emr.getFullFile());
	}


	/**
	 *  Creates the refactoring and fills in the data
	 *
	 *@return    the extract method refactoring
	 */
	protected Refactoring createRefactoring()
	{
		emr.setMethodName(newName.getText());
		if (list == null) {
			//  Don't need to set the parameter order!
		}
		else {
			emr.setParameterOrder(list.getData());
		}

		int prot = ExtractMethodRefactoring.PRIVATE;
		if (packageButton.isSelected()) {
			prot = ExtractMethodRefactoring.PACKAGE;
		}
		if (protectedButton.isSelected()) {
			prot = ExtractMethodRefactoring.PROTECTED;
		}
		if (publicButton.isSelected()) {
			prot = ExtractMethodRefactoring.PUBLIC;
		}
		emr.setProtection(prot);

		if (returnTextField == null) {
			Object result = returnList.getSelectedValue();
			emr.setReturnType(result);
		}
		else {
			emr.setReturnType(returnTextField.getText());
		}

		return emr;
	}


	/**
	 *  Initialize the extract method refactoring
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	private void init() throws RefactoringException
	{
		emr = RefactoringFactory.get().extractMethod();
		String full = getStringFromIDE();
		if (full == null) {
			dispose();
			throw new RefactoringException("Invalid file for extracting the source code");
		}
		else {
			emr.setFullFile(full);
		}

		String selection = getSelectionFromIDE();
		if (full == null) {
			JOptionPane.showMessageDialog(null,
					"You must select a series of statements or an expression to extract.",
					"Extract Method Error",
					JOptionPane.ERROR_MESSAGE);
			dispose();
			throw new RefactoringException("Empty selection.");
		}
		else {
			emr.setSelection(selection);
		}

		emr.setMethodName("extractedMethod");
	}


	/**
	 *  Creates a panel of radio buttons with protection levels
	 *
	 *@return    the panel
	 */
	private JPanel initRadioButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JLabel label = new JLabel("Protection:");
		panel.add(label);

		ButtonGroup group = new ButtonGroup();

		privateButton = new JRadioButton("private");
		privateButton.setSelected(true);
		panel.add(privateButton);
		group.add(privateButton);
		privateButton.addActionListener(sua);

		packageButton = new JRadioButton("package");
		panel.add(packageButton);
		group.add(packageButton);
		packageButton.addActionListener(sua);

		protectedButton = new JRadioButton("protected");
		panel.add(protectedButton);
		group.add(protectedButton);
		protectedButton.addActionListener(sua);

		publicButton = new JRadioButton("public");
		panel.add(publicButton);
		group.add(publicButton);
		publicButton.addActionListener(sua);

		panel.setBorder(BorderFactory.createEtchedBorder());

		return panel;
	}
}
