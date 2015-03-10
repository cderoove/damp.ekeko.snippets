/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.acm.seguin.ide.common.PackageNameLoader;
import org.acm.seguin.ide.common.PackageSelectorDialog;
import org.acm.seguin.ide.common.SummaryLoaderThread;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  View manager for a particular UML file
 *
 *@author    Chris Seguin
 */
public class UMLViewManager implements ViewManager {
	private UMLDocManager docManager;
	private UMLPackage packagePanel;
	private PackageSummary summary;
	private JScrollPane pane;
	private String filename;
	private String packageName;


	/**
	 *  Constructor for the UMLViewManager object
	 *
	 *@param  parent  the parent document manager
	 *@param  name    the name of the file to view
	 *@param  base    Description of Parameter
	 */
	public UMLViewManager(UMLDocManager parent, String name, String base) {
		/*
		 * Creating this instance requires that the summaries
		 * have been loaded at least once, but shouldn't
		 * block further opertions.
		 */
		SummaryLoaderThread.waitForLoading();

		docManager = parent;

		if (name != null) {
			filename = name;
			packagePanel = new UMLPackage(filename);
		}
		else {
			PackageSelectorDialog dialog =
					new PackageSelectorDialog(FrameManager.current().getFrame());
			dialog.setVisible(true);
			summary = dialog.getSummary();

			filename = null;
			packagePanel = new UMLPackage(summary);
		}

		parent.getReloader().add(packagePanel);

		pane = new JScrollPane(packagePanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JScrollBar horiz = pane.getHorizontalScrollBar();
		horiz.setUnitIncrement(400);
		JScrollBar vert = pane.getVerticalScrollBar();
		vert.setUnitIncrement(400);

		loadPackageName();
	}


	/**
	 *  Get the actions currently supported (may vary with state)
	 *
	 *@return    The Actions value
	 */
	public ActionEx[] getActions() {
		return new ActionEx[0];
	}


	/**
	 *  Get the document manager responsible for this view
	 *
	 *@return    The DocManager value
	 */
	public DocManager getDocManager() {
		return docManager;
	}


	/**
	 *  Get the title of the document being viewed
	 *
	 *@return    The Title value
	 */
	public String getTitle() {
		if (packageName.length() > 0) {
			return packageName;
		}
		else {
			return "<Top Level Package>";
		}
	}


	/**
	 *  Get the view component which renders/edits the document
	 *
	 *@return    The View value
	 */
	public JComponent getView() {
		return pane;
	}


	/**
	 *  Gets the Diagram attribute of the UMLViewManager object
	 *
	 *@return    The Diagram value
	 */
	public UMLPackage getDiagram() {
		return packagePanel;
	}


	/**
	 *  Notify the view manager that it has been closed
	 */
	public void closed() {
		save();
	}


	/**
	 *  Notify the view manager that it is about to close
	 */
	public void closing() {
	}


	/**
	 *  Determine whether it is ok to close the view.
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean okToClose() {
		return true;
	}


	/**
	 *  Reload the document from its storage (if it has one).
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean reload() {
		packagePanel.reload();
		return true;
	}


	/**
	 *  Save the current document.
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean save() {
		try {
			packagePanel.save();
		}
		catch (IOException ioe) {
			return false;
		}
		return true;
	}


	/**
	 *  Loads the package name from the file
	 */
	private void loadPackageName() {
		if (filename == null) {
			packageName = summary.getName();
			return;
		}

		PackageNameLoader pnl = new PackageNameLoader();
		packageName = pnl.load(filename);
	}
}
