/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.vfs.Url;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.ide.common.PackageSelectorDialog;
import org.acm.seguin.summary.PackageSummary;

/**
 *  Package selector dialog box
 *
 *@author    Chris Seguin
 */
public class NewClassDiagramAction extends JBuilderAction {
	/**
	 *  Constructor for the PrettyPrinterAction object
	 */
	public NewClassDiagramAction()
	{
		putValue(NAME, "New UML Class Diagram");
		putValue(SHORT_DESCRIPTION, "New UML Class Diagram");
		putValue(LONG_DESCRIPTION, "Creates a new UML class diagram");
	}


	/**
	 *  Gets the Enabled attribute of the PrettyPrinterAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled()
	{
		MultipleDirClassDiagramReloader reloader =
				UMLNodeViewerFactory.getFactory().getReloader();

		return reloader.isNecessary();
	}


	/**
	 *  The pretty printer action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt)
	{
		Browser browser = Browser.getActiveBrowser();
		PackageSelectorDialog psd =
				new PackageSelectorDialog(browser);
		psd.setVisible(true);
		PackageSummary summary = psd.getSummary();
		if (summary == null) {
			return;
		}

		Project proj = browser.getActiveProject();
		Node parent = proj;
		File diagramFile = getFile(summary);
		createFile(diagramFile, summary);
		Url url = new Url(diagramFile);
		UMLNode node = (UMLNode) proj.findNode(url);

		if (node == null) {
			try {
				node = new UMLNode(proj, parent, url);
			}
			catch (com.borland.primetime.node.DuplicateNodeException dne) {
				dne.printStackTrace(System.out);
			}
		}

		try {
			browser.setActiveNode(node, true);
//			NodeViewer[] viewers = browser.getViewers(node);
//			for (int ndx = 0; ndx < viewers.length; ndx++) {
//				if (viewers[ndx] instanceof UMLNodeViewer) {
//					browser.setActiveViewer(node, viewers[ndx], true);
//				}
//			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 *  Gets the file associated with the package summary
	 *
	 *@param  summary  the package summary
	 *@return          the file to get
	 */
	private File getFile(PackageSummary summary)
	{
		File dir = summary.getDirectory();
		File inputFile;
		if (dir == null) {
			dir = new File(System.getProperty("user.home") +
					File.separator + ".Refactory" +
					File.separator + "UML");
			dir.mkdirs();
			inputFile = new File(dir, summary.getName() + ".uml");
		}
		else {
			inputFile = new File(summary.getDirectory(), "package.uml");
		}

		return inputFile;
	}


	/**
	 *  Creates a file if one does not yet exist
	 *
	 *@param  diagramFile  the file to create
	 *@param  summary      the associated package
	 */
	private void createFile(File diagramFile, PackageSummary summary)
	{
		if (!diagramFile.exists()) {
			try {
				FileWriter output = new FileWriter(diagramFile);
				output.write("V[1.1:" + summary.getName() + "]\n");
				output.close();
			}
			catch (IOException ioe) {
			}
		}
	}
}
