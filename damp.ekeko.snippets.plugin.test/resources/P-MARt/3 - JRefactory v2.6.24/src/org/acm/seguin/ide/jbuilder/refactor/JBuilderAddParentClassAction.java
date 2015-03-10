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
import java.io.IOException;
import org.acm.seguin.ide.common.action.AddParentClassAction;
import org.acm.seguin.ide.jbuilder.JBuilderAction;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.refactor.AddParentClassListener;

/**
 *  Adds an abstract parent class
 *
 *@author    Chris Seguin
 */
public class JBuilderAddParentClassAction extends AddParentClassAction {
	/**
	 *  Constructor for the AddParentClassAction object
	 *
	 *@param  init  Description of Parameter
	 */
	public JBuilderAddParentClassAction(Node[] init)
	{
		super(new JBuilderSelectedFileSet(init));
	}
}
