/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *  Handles the print menu option 
 *
 *@author     Chris Seguin 
 *@created    August 6, 1999 
 */
public class PrintSetupAdapter implements ActionListener {
	/**
	 *  The action performed 
	 *
	 *@param  ev  the action event 
	 */
	public void actionPerformed(ActionEvent ev) {
		UMLPagePrinter.getPageFormat(true);
	}
}
