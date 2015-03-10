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
import com.borland.primetime.ide.NodeViewerFactory;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.Node;
import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.summary.PackageSummary;

/**
 *  Factory for node viewers
 *
 *@author    Chris Seguin
 */
public class UMLNodeViewerFactory implements NodeViewerFactory {
	private MultipleDirClassDiagramReloader reloader;

	private static UMLNodeViewerFactory factory = null;


	/**
	 *  Constructor for the UMLNodeViewerFactory object
	 */
	private UMLNodeViewerFactory() {
		reloader = new JBuilderClassDiagramLoader();
	}


	/**
	 *  Gets the class diagram reloader
	 *
	 *@return    the reloader
	 */
	public MultipleDirClassDiagramReloader getReloader() {
		return reloader;
	}


	/**
	 *  Determines if this factory can view this type of file
	 *
	 *@param  node  the type of file
	 *@return       true if it can be displayed
	 */
	public boolean canDisplayNode(Node node) {
		return node instanceof UMLNode;
	}


	/**
	 *  Creates the node viewer
	 *
	 *@param  context  the information about what is to be displayed
	 *@return          the viewer
	 */
	public NodeViewer createNodeViewer(Context context) {
		if (canDisplayNode(context.getNode())) {
			if (!reloader.isNecessary()) {
				reloader.setNecessary(true);
				reloader.reload();
			}

			UMLNodeViewer viewer = new UMLNodeViewer(context, reloader);
			return viewer;
		}

		return null;
	}


	/**
	 *  Creates the node viewer
	 *
	 *@param  summary  Description of Parameter
	 *@return          the viewer
	 */
	public NodeViewer createNodeViewer(PackageSummary summary) {
		if (!reloader.isNecessary()) {
			reloader.setNecessary(true);
			reloader.reload();
		}

		UMLNodeViewer viewer = new UMLNodeViewer(summary, reloader);
		return viewer;
	}


	/**
	 *  Gets the Factory attribute of the UMLNodeViewerFactory class
	 *
	 *@return    The Factory value
	 */
	public static UMLNodeViewerFactory getFactory() {
		if (factory == null) {
			factory = new UMLNodeViewerFactory();
		}

		return factory;
	}
}
