/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.util.Iterator;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.uml.PackageSummaryListModel;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Just the package selector area
 *
 *@author    Chris Seguin
 */
public class PackageSelectorArea extends JPanel {
	/**
	 *  The list box of packages
	 */
	protected JList listbox;
	private JScrollPane pane;


	/**
	 *  Constructor for the PackageSelectorArea object
	 */
	public PackageSelectorArea()
	{
		//  Setup the UI
		setLayout(null);
		super.setSize(220, 300);

		//  Create the list
		listbox = new JList();

		pane = new JScrollPane(listbox);
		pane.setBounds(10, 10, 200, 280);
		add(pane);
	}


	/**
	 *  Gets the ScrollPane attribute of the PackageSelectorArea object
	 *
	 *@return    The ScrollPane value
	 */
	public JScrollPane getScrollPane()
	{
		return pane;
	}


	/**
	 *  Gets the Selection attribute of the PackageSelectorArea object
	 *
	 *@return    The Selection value
	 */
	public PackageSummary getSelection()
	{
		Object[] selection = listbox.getSelectedValues();
		return (PackageSummary) selection[0];
	}


	/**
	 *  Load the summaries
	 */
	public void loadSummaries() { }


	/**
	 *  Loads the package into the listbox
	 */
	public void loadPackages()
	{
		PackageSummaryListModel model = new PackageSummaryListModel();

		//  Get the list of packages
		Iterator iter = PackageSummary.getAllPackages();
		if (iter == null) {
			return;
		}

		//  Add in the packages
		UMLPackage view = null;
		PackageSummary packageSummary = null;
		PackageListFilter filter = PackageListFilter.get();
		while (iter.hasNext()) {
			packageSummary = (PackageSummary) iter.next();
			if (filter.isIncluded(packageSummary)) {
				model.add(packageSummary);
			}
		}

		//  Set the model
		listbox.setModel(model);
	}
}
