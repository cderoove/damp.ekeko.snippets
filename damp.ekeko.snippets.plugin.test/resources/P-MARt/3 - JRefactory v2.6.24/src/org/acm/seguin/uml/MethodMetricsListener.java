/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.acm.seguin.metrics.MethodMetricsFrame;
import org.acm.seguin.summary.MethodSummary;

/**
 *  Create a mouse listener for a method or a field or a title 
 *
 *@author     Chris Seguin 
 *@created    July 7, 1999 
 */
public class MethodMetricsListener extends PopupMenuListener {
	//  Instance Variables
	private MethodSummary methodSummary;


	/**
	 *  Constructor for the MethodMetricsListener object 
	 *
	 *@param  initSummary  Description of Parameter 
	 *@param  initMenu     Description of Parameter 
	 *@param  initItem     Description of Parameter 
	 */
	public MethodMetricsListener(MethodSummary initSummary, JPopupMenu initMenu, JMenuItem initItem) {
		super(initMenu, initItem);
		methodSummary = initSummary;
	}


	/**
	 *  A menu item has been selected 
	 *
	 *@param  evt   Description of Parameter 
	 */
	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt);
		new MethodMetricsFrame(methodSummary);
	}
}
