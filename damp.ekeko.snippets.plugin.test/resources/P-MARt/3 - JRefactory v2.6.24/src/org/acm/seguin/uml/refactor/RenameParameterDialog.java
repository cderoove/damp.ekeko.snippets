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
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.method.RenameParameterRefactoring;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Dialog box that gets input for renaming the parameter
 *
 *@author    Chris Seguin
 */
class RenameParameterDialog extends ClassNameDialog {
	private ParameterSummary param;
	private MethodSummary method;
	private JComboBox parameterSelection;


	/**
	 *  Constructor for the RenameParameterDialog object
	 *
	 *@param  init       Description of Parameter
	 *@param  initParam  Description of Parameter
	 */
	public RenameParameterDialog(UMLPackage init, ParameterSummary initParam)
	{
		super(init, 0);
		param = initParam;
		method = (MethodSummary) param.getParent();
		if (method == null) {
			System.out.println("No method specified");
		}

		setTitle(getWindowTitle());

		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}


	/**
	 *  Constructor for the RenameParameterDialog object
	 *
	 *@param  init        Description of Parameter
	 *@param  initMethod  Description of Parameter
	 */
	public RenameParameterDialog(UMLPackage init, MethodSummary initMethod)
	{
		super(init, 1);

		param = null;
		method = initMethod;
		if (method == null) {
			System.out.println("No method specified");
		}

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel newNameLabel = new JLabel("Parameter:  ");
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		GridBagLayout gridbag = (GridBagLayout) getContentPane().getLayout();
		gridbag.setConstraints(newNameLabel, gbc);
		getContentPane().add(newNameLabel);

		parameterSelection = new JComboBox();
		Iterator iter = method.getParameters();
		while (iter.hasNext()) {
			parameterSelection.addItem(iter.next());
		}
		parameterSelection.setEditable(false);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(parameterSelection, gbc);
		getContentPane().add(parameterSelection);

		setTitle(getWindowTitle());

		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}


	/**
	 *  Gets the WindowTitle attribute of the RenameParameterDialog object
	 *
	 *@return    The WindowTitle value
	 */
	public String getWindowTitle()
	{
		if (param == null) {
			return "Renaming a parameter";
		}
		return "Renaming the parameter " + param.getName() + " in " + method.getName();
	}


	/**
	 *  Gets the LabelText attribute of the RenameParameterDialog object
	 *
	 *@return    The LabelText value
	 */
	public String getLabelText()
	{
		return "New parameter name:";
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	protected Refactoring createRefactoring()
	{
		RenameParameterRefactoring rpr = RefactoringFactory.get().renameParameter();
		rpr.setMethodSummary(method);
		if (param == null) {
			Object selection = parameterSelection.getSelectedItem();
			rpr.setParameterSummary((ParameterSummary) selection);
		}
		else {
			rpr.setParameterSummary(param);
		}
		rpr.setNewName(getClassName());
		return rpr;
	}
}
