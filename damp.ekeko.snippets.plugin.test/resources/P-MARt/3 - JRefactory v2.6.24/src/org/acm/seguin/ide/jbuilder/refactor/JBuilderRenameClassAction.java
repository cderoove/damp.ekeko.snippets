/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import com.borland.jbuilder.node.JavaFileNode;
import com.borland.primetime.node.Node;
import java.awt.event.ActionEvent;
import org.acm.seguin.ide.common.action.RenameClassAction;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.refactor.AddRenameClassListener;

/**
 *  Renames the class
 *
 *@author    Chris Seguin
 */
public class JBuilderRenameClassAction extends RenameClassAction {
	/**
	 *  Constructor for the RenameClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public JBuilderRenameClassAction(Node[] init)
	{
		super(new JBuilderSelectedFileSet(init));
	}
}
