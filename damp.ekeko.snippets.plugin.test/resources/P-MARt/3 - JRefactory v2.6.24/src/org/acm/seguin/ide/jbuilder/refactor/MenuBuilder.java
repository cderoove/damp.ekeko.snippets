/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import org.acm.seguin.ide.common.action.ExtractMethodAction;
import org.acm.seguin.ide.common.action.PushDownFieldAction;
import org.acm.seguin.ide.common.action.PushDownMethodAction;
import org.acm.seguin.ide.common.action.PushUpAbstractMethodAction;
import org.acm.seguin.ide.common.action.PushUpFieldAction;
import org.acm.seguin.ide.common.action.PushUpMethodAction;
import org.acm.seguin.ide.common.action.RenameFieldAction;
import org.acm.seguin.ide.common.action.RenameParameterAction;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class MenuBuilder {
	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public static ActionGroup build()
	{
		ActionGroup group = new ActionGroup("Refactorings");
		group.setPopup(true);

		group.add(buildType());
		group.add(buildMethod());
		group.add(buildField());

		return group;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public static ActionGroup buildMethod()
	{
		ActionGroup group = new ActionGroup("Method Refactorings");
		group.setPopup(true);

		group.add(new PushUpMethodAction());
		group.add(new PushUpAbstractMethodAction());
		group.add(new PushDownMethodAction());
		group.add(new RenameParameterAction());
		group.add(new ExtractMethodAction());

		return group;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public static ActionGroup buildField()
	{
		ActionGroup group = new ActionGroup("Field Refactorings");
		group.setPopup(true);

		group.add(new RenameFieldAction());
		group.add(new PushUpFieldAction());
		group.add(new PushDownFieldAction());

		return group;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	private static ActionGroup buildType()
	{
		ActionGroup group = new ActionGroup("Type Refactorings");
		group.setPopup(true);

		group.add(new JBuilderRenameClassAction(null));
		group.add(new JBuilderMoveClassAction(null));
		group.add(new JBuilderAddParentClassAction(null));
		group.add(new JBuilderAddChildClassAction(null));
		group.add(new JBuilderRemoveClassAction(null));
		group.add(new JBuilderExtractInterfaceAction(null));

		return group;
	}
}
