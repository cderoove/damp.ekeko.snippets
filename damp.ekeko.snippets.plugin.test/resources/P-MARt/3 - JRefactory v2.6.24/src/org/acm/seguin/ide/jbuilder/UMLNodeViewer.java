/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.primetime.ide.Context;
import com.borland.primetime.node.Node;
import com.borland.primetime.viewer.AbstractNodeViewer;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.ide.common.ClassDiagramReloader;
import org.acm.seguin.ide.common.DividedSummaryPanel;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Stores a view of a UML class diagram
 *
 *@author    Chris Seguin
 */
public class UMLNodeViewer extends AbstractNodeViewer {
	private UMLPackage diagram;
	private ClassDiagramReloader reloader;


	/**
	 *  Constructor for the UMLNodeViewer object
	 *
	 *@param  summary  Description of Parameter
	 *@param  init     Description of Parameter
	 */
	public UMLNodeViewer(PackageSummary summary, ClassDiagramReloader init) {
		super(null);
		diagram = new UMLPackage(summary);
		reloader = init;
		reloader.add(diagram);
	}


	/**
	 *  Constructor for the UMLNodeViewer object
	 *
	 *@param  context  Description of Parameter
	 *@param  init     Description of Parameter
	 */
	public UMLNodeViewer(Context context, ClassDiagramReloader init) {
		super(context);

		Node node = context.getNode();
		if (node instanceof UMLNode) {
			UMLNode umlNode = (UMLNode) node;
			diagram = umlNode.getDiagram();
			if (diagram == null) {
				try {
					diagram = new UMLPackage(umlNode.getInputStream());
				}
				catch (IOException ioe) {
					ExceptionPrinter.print(ioe);
					diagram = null;
				}
				umlNode.setDiagram(diagram);
			}
		}
		else {
			diagram = null;
		}

		reloader = init;
		reloader.add(diagram);
	}


	/**
	 *  Gets the ViewerTitle attribute of the UMLNodeViewer object
	 *
	 *@return    The ViewerTitle value
	 */
	public String getViewerTitle() {
		return "Class Diagram";
	}


	/**
	 *  Gets the Diagram attribute of the UMLNodeViewer object
	 *
	 *@return    The Diagram value
	 */
	public UMLPackage getDiagram() {
		return diagram;
	}


	/**
	 *  Creates the main viewer
	 *
	 *@return    the viewer
	 */
	public JComponent createViewerComponent() {
		if (diagram == null) {
			return null;
		}
		JScrollPane pane = new JScrollPane(diagram,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		JScrollBar horiz = pane.getHorizontalScrollBar();
		horiz.setUnitIncrement(400);
		JScrollBar vert = pane.getVerticalScrollBar();
		vert.setUnitIncrement(400);

		diagram.setScrollPane(pane);

		return pane;
	}


	/**
	 *  Creates a summary component, which is blank
	 *
	 *@return    the component
	 */
	public JComponent createStructureComponent() {
		DividedSummaryPanel dsp =
				new DividedSummaryPanel(diagram.getSummary(), diagram);
		return dsp.getPane();
	}


	/**
	 *  Releases the viewer
	 */
	public void releaseViewer() {
		try {
			diagram.save();
		}
		catch (IOException ioe) {
		}
		reloader.remove(diagram);
	}
}
