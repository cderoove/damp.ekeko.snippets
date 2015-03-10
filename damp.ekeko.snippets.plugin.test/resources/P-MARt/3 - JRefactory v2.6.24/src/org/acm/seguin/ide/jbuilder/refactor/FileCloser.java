/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import java.io.File;
import com.borland.primetime.vfs.Url;

/**
 *  Makes sure that the node is no longer open before a refactoring is
 *  performed which will remove or move the node.
 *
 *@author    Chris Seguin
 */
public class FileCloser {
	/**
	 *  Closes a file in JBuilder
	 *
	 *@param  file  Description of Parameter
	 */
	public static void close(File file)
	{
		// yikes! the nodes are about to be moved, so close them if they are open
		Browser browser = Browser.getActiveBrowser();
		Node[] possible = browser.getOpenNodes();
		Node nodeToClose = null;
		Url url = new Url(file);

		for (int ndx = 0; ndx < possible.length; ndx++) {
			if (possible[ndx] instanceof FileNode) {
				FileNode fileNode = (FileNode) possible[ndx];
				if (fileNode.getUrl().equals(url)) {
					nodeToClose = fileNode;
					break;
				}
			}
		}

		if (nodeToClose == null) {
			return;
		}

		try {
			browser.closeNode(nodeToClose);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
