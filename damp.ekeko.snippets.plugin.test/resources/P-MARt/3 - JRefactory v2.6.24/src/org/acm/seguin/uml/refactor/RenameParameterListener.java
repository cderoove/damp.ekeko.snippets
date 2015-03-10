/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Menu listener that invokes the refactoring dialog for renaming a parameter
 *
 *@author    Chris Seguin
 */
public class RenameParameterListener extends DialogViewListener {
	private ParameterSummary param;
	private MethodSummary method;
	private UMLPackage packageDiagram;


	/**
	 *  Constructor for the RenameParameterListener object
	 *
	 *@param  initMenu     Description of Parameter
	 *@param  initItem     Description of Parameter
	 *@param  initPackage  Description of Parameter
	 *@param  init         Description of Parameter
	 */
	public RenameParameterListener(JPopupMenu initMenu, JMenuItem initItem,
			UMLPackage initPackage, ParameterSummary init)
	{
		super(initMenu, initItem);
		param = init;
		packageDiagram = initPackage;
		method = null;
	}


	/**
	 *  Constructor for the RenameParameterListener object
	 *
	 *@param  init      Description of Parameter
	 */
	public RenameParameterListener(MethodSummary init)
	{
		super(null, null);
		param = null;
		packageDiagram = null;
		method = init;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	protected JDialog createDialog()
	{
		if (param == null) {
			return new RenameParameterDialog(packageDiagram, method);
		}
		else {
			return new RenameParameterDialog(packageDiagram, param);
		}
	}
}
