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
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.acm.seguin.summary.PackageSummary;

/**
 *  Creates a label and a jcombo box and adds it into the JDialog. The combo
 *  box contains a list of all the packages that have been created so far.
 *  This assumes that the dialog box has used a GridBagLayout, and makes the
 *  label fill one column and the combo box fill 2 columns. <P>
 *
 *  The usage: <BR>
 *  <TT><BR>
 *  PackageList pl = new PackageList(); <BR>
 *  JComboBox save = pl.add(this); <BR>
 *  </TT>
 *
 *@author    Chris Seguin
 */
class PackageList {
	/**
	 *  Adds a label and the combo box to the designated dialog
	 *
	 *@param  dialog  the dialog window
	 *@return         the combo box that was added
	 */
	public JComboBox add(JDialog dialog)
	{
		GridBagConstraints gbc = new GridBagConstraints();

		JLabel packageLabel = new JLabel("Package:");
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		dialog.getContentPane().add(packageLabel, gbc);

		JComboBox packageName = new JComboBox();
		packageName.setEditable(true);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		dialog.getContentPane().add(packageName, gbc);

		addPackages(packageName);

		JPanel blank = new JPanel();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		Dimension blankDim = new Dimension(50, packageName.getPreferredSize().height * 5);
		blank.setMinimumSize(blankDim);
		blank.setPreferredSize(blankDim);
		dialog.getContentPane().add(blank, gbc);

		return packageName;
	}


	/**
	 *  Fills in the combo box with the names of the packages
	 *
	 *@param  comboBox  the combo box to fill in
	 */
	private void addPackages(JComboBox comboBox)
	{
		//  Add the package names
		Iterator iter = PackageSummary.getAllPackages();
		TreeSet set = new TreeSet();
		while (iter.hasNext()) {
			PackageSummary next = (PackageSummary) iter.next();
			set.add(next.toString());
		}

		iter = set.iterator();
		while (iter.hasNext()) {
			comboBox.addItem(iter.next().toString());
		}
	}
}
