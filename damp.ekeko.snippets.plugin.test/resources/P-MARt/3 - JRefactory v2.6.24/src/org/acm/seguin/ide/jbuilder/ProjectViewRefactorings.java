/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.ContextActionProvider;
import com.borland.primetime.node.Node;
import javax.swing.Action;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderAddChildClassAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderAddParentClassAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderExtractInterfaceAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderMoveClassAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderRemoveClassAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderRenameClassAction;

/**
 *  Adds the refactorings onto the project view
 *
 *@author    Chris Seguin
 */
public class ProjectViewRefactorings implements ContextActionProvider {
	/**
	 *  Gets the ContextAction attribute of the ProjectViewRefactorings object
	 *
	 *@param  browser  Description of Parameter
	 *@param  nodes    Description of Parameter
	 *@return          The ContextAction value
	 */
	public Action getContextAction(Browser browser, Node[] nodes)
	{
		ActionGroup group = new ActionGroup("JRefactory");
		group.setPopup(true);

		group.add(new JBuilderRenameClassAction(nodes));
		group.add(new JBuilderMoveClassAction(nodes));
		group.add(new JBuilderAddParentClassAction(nodes));
		group.add(new JBuilderAddChildClassAction(nodes));
		group.add(new JBuilderRemoveClassAction(nodes));
		group.add(new JBuilderExtractInterfaceAction(nodes));

		return group;
	}
}
