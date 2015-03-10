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
import org.acm.seguin.refactor.type.ExtractInterfaceRefactoring;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetPackageSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Extracts an interface from a class
 *
 *@author     Grant Watson
 *@created    November 30, 2000
 */
public class ExtractInterfaceDialog extends ClassNameDialog {
	private TypeSummary[] typeArray;
	private JComboBox packageNameBox;


	/**
	 *  Constructor for ExtractInterfaceDialog
	 *
	 *@param  init       The package where this operation is occuring
	 *@param  initTypes  Description of Parameter
	 */
	public ExtractInterfaceDialog(UMLPackage init, TypeSummary[] initTypes)
	{
		super(init, 2);

		typeArray = initTypes;

		PackageList pl = new PackageList();
		packageNameBox = pl.add(this);

		String name;
		if (init == null) {
			name = GetPackageSummary.query(initTypes[0]).getName();
		}
		else {
			name = init.getSummary().getName();
		}
		packageNameBox.setSelectedItem(name);

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
		return "Extract Interface";
	}


	/**
	 *  Gets the label for the text
	 *
	 *@return    the text for the label
	 */
	public String getLabelText()
	{
		return "Interface:";
	}


	/**
	 *  Extracts an interface from all specified classes.
	 *
	 *@return    the refactoring
	 */
	protected Refactoring createRefactoring()
	{
		//  Create system
		ExtractInterfaceRefactoring eir = RefactoringFactory.get().extractInterface();

		String interfaceName = getClassName();
		if (interfaceName.indexOf(".") > 0) {
			eir.setInterfaceName(interfaceName);
		}
		else {
			String packageName = (String) packageNameBox.getSelectedItem();
			if (packageName.indexOf("<") == -1) {
				eir.setInterfaceName(packageName + "." + interfaceName);
			}
			else {
				eir.setInterfaceName(interfaceName);
			}
		}

		//  Add the types
		for (int ndx = 0; ndx < typeArray.length; ndx++) {
			eir.addImplementingClass(typeArray[ndx]);
		}

		return eir;
	}
}
