/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JComboBox;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.AddChildRefactoring;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetPackageSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Creates a dialog box to prompt for the new parent name
 *
 *@author    Chris Seguin
 */
public class AddChildClassDialog extends ClassNameDialog {
	private TypeSummary typeSummary;
	private JComboBox packageNameBox;


	/**
	 *  Constructor for AddAbstractParentDialog
	 *
	 *@param  init      The package where this operation is occuring
	 *@param  initType  the type summary
	 */
	public AddChildClassDialog(UMLPackage init, TypeSummary initType)
	{
		super(init, 2);

		PackageList pl = new PackageList();
		packageNameBox = pl.add(this);

		String name;
		if (init == null) {
			name = GetPackageSummary.query(initType).getName();
		}
		else {
			name = init.getSummary().getName();
		}
		packageNameBox.setSelectedItem(name);

		typeSummary = initType;
		pack();

		org.acm.seguin.awt.CenterDialog.center(this, init);
	}


	/**
	 *  Returns the window title
	 *
	 *@return    the title
	 */
	public String getWindowTitle()
	{
		return "Add a child class";
	}


	/**
	 *  Gets the label for the text
	 *
	 *@return    the text for the label
	 */
	public String getLabelText()
	{
		return "Child class:";
	}


	/**
	 *  Adds an abstract parent class to all specified classes.
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		//  Create system
		AddChildRefactoring refactoring = RefactoringFactory.get().addChild();
		refactoring.setChildName(getClassName());

		//  Add the type
		refactoring.setParentClass(typeSummary);

		refactoring.setPackageName((String) packageNameBox.getSelectedItem());

		return refactoring;
	}
}
