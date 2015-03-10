/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.jbuilder.node.JavaFileNode;
import com.borland.primetime.ide.BrowserListener;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.util.VetoException;
import javax.swing.JComponent;
import java.awt.Component;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class RefactoringAdapter extends BrowserAdapter
{
	/**
	 *  Description of the Method
	 *
	 *@param  browser  Description of Parameter
	 *@param  node     Description of Parameter
	 *@param  viewer   Description of Parameter
	 */
	public void browserViewerActivated(Browser browser, Node node, NodeViewer viewer)
	{
		System.out.println("We are activating a viewer!");
		if (node instanceof JavaFileNode) {
			System.out.println("  Viewer:  " + viewer.getClass().getName());
			System.out.println("  TextStructure:  " + ((JavaFileNode) node).getTextStructureClass().getName());
			JComponent structure = viewer.getStructureComponent();
			if (structure == null) {
				System.out.println("No structure viewer");
			}
			else {
				System.out.println("  Structure:  " + structure.getClass().getName());
				Component[] comps = structure.getComponents();
				for (int ndx = 0; ndx < comps.length; ndx++) {
					System.out.println("    Contains:  " + comps[ndx].getClass().getName());
				}
			}
		}
	}
}
