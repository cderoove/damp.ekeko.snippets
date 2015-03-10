/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.awt.event.ActionEvent;

/**
 *  Action definition that JBuilder uses
 *
 *@author    Chris Seguin
 */
public abstract class JBuilderAction extends UpdateAction {
	/**
	 *  Is this action enabled
	 */
	protected boolean enabled;


	/**
	 *  Constructor for the JBuilderAction object
	 */
	public JBuilderAction()
	{
		enabled = true;
	}


	/**
	 *  Sets the Enabled attribute of the PrettyPrinterAction object
	 *
	 *@param  value  The new Enabled value
	 */
	public void setEnabled(boolean value)
	{
		enabled = value;
	}


	/**
	 *  The action to be performed
	 *
	 *@param  evt  the triggering event
	 */
	public abstract void actionPerformed(ActionEvent evt);
}
