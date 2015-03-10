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
import com.borland.jbuilder.node.JavaFileNode;

/**
 *  Factory for node viewers for Refactoring editor
 *
 *@author    Chris Seguin
 */
public class RefactoryNodeViewerFactory implements NodeViewerFactory {
	private static RefactoryNodeViewerFactory factory = null;


	/**
	 *  Constructor for the RefactoryNodeViewerFactory object
	 */
	private RefactoryNodeViewerFactory() {
	}


	/**
	 *  Determines if this factory can view this type of file
	 *
	 *@param  node  the type of file
	 *@return       true if it can be displayed
	 */
	public boolean canDisplayNode(Node node) {
		return node instanceof JavaFileNode;
	}


	/**
	 *  Creates the node viewer
	 *
	 *@param  context  the information about what is to be displayed
	 *@return          the viewer
	 */
	public NodeViewer createNodeViewer(Context context) {
		if (canDisplayNode(context.getNode())) {
			RefactoringBrowser viewer = new RefactoringBrowser(context);
			return viewer;
		}

		return null;
	}


	/**
	 *  Gets the Factory attribute of the RefactoryNodeViewerFactory class
	 *
	 *@return    The Factory value
	 */
	public static RefactoryNodeViewerFactory getFactory() {
		if (factory == null) {
			factory = new RefactoryNodeViewerFactory();
		}

		return factory;
	}
}
