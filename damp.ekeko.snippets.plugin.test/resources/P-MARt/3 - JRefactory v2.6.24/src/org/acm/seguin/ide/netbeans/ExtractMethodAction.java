/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.netbeans;

import javax.swing.*;
import org.acm.seguin.refactor.RefactoringException;
import org.openide.cookies.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class ExtractMethodAction extends CookieAction {

	/**
	 *  Gets the Name attribute of the ExtractMethodAction object
	 *
	 *@return    The Name value
	 */
	public String getName()
	{
		return NbBundle.getMessage(ExtractMethodAction.class, "LBL_ExtractMethodAction");
	}


	/**
	 *  Gets the HelpCtx attribute of the ExtractMethodAction object
	 *
	 *@return    The HelpCtx value
	 */
	public HelpCtx getHelpCtx()
	{
		return HelpCtx.DEFAULT_HELP;
		// return new HelpCtx (ExtractMethodAction.class);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	protected Class[] cookieClasses()
	{
		return new Class[]{EditorCookie.class};
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	protected int mode()
	{
		return MODE_EXACTLY_ONE;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  nodes  Description of Parameter
	 */
	protected void performAction(Node[] nodes)
	{
		EditorCookie cookie =
				(EditorCookie) nodes[0].getCookie(EditorCookie.class);
		try {
			// (new NetBeansExtractMethodDialog(cookie)).show();
			(new NetBeansExtractMethodDialog()).show();
		}
		catch (RefactoringException re) {
			//(PENDING) NetBeans specific exception
			JOptionPane.showMessageDialog(null, re.getMessage(),
					"Refactoring Exception", JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	protected String iconResource()
	{
		return null;
	}


	/**
	 *  Perform special enablement check in addition to the normal one.
	 *
	 *@param  nodes  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	protected boolean enable(Node[] nodes)
	{
		if (!super.enable(nodes)) {
			return false;
		}
		// Any additional checks ...
		return true;
	}


	/**
	 *  Perform extra initialization of this action's singleton. PLEASE do not
	 *  use constructors for this purpose!
	 */
	protected void initialize()
	{
		super.initialize();
		putProperty(PrettyPrinterAction.SHORT_DESCRIPTION,
				NbBundle.getMessage(ExtractMethodAction.class,
				"HINT_ExtractMethodAction"));
	}

}
