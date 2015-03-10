/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.BrowserListener;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.util.VetoException;

/**
 *  Default implementation of the BrowserListener
 *
 *@author    Chris Seguin
 */
public class BrowserAdapter implements BrowserListener {
	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 */
	public void browserOpened(Browser browser) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 */
	public void browserActivated(Browser browser) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 */
	public void browserDeactivated(Browser browser) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser            Description of Parameter
	 *@exception  VetoException  Description of Exception
	 */
	public void browserClosing(Browser browser) throws VetoException {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 */
	public void browserClosed(Browser browser) {
	}


	/**
	 *  A particular project was activated
	 *
	 *@param  browser  The browser that it was activated in
	 *@param  project  The project
	 */
	public void browserProjectActivated(Browser browser, Project project) {
	}


	/**
	 *  A project was closed in a particular browser
	 *
	 *@param  browser  the browser
	 *@param  project  the project
	 */
	public void browserProjectClosed(Browser browser, Project project) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 *@param  node     Description of Parameter
	 */
	public void browserNodeActivated(Browser browser, Node node) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 *@param  node     Description of Parameter
	 */
	public void browserNodeClosed(Browser browser, Node node) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 *@param  node     Description of Parameter
	 *@param  viewer   Description of Parameter
	 */
	public void browserViewerActivated(Browser browser, Node node, NodeViewer viewer) {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  browser            Description of Parameter
	 *@param  node               Description of Parameter
	 *@param  viewer             Description of Parameter
	 *@exception  VetoException  Description of Exception
	 */
	public void browserViewerDeactivating(Browser browser, Node node, NodeViewer viewer) throws VetoException {
	}
}
