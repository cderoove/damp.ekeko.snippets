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
import java.awt.event.ActionListener;
import java.io.IOException;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.io.Saveable;

/**
 *  Saves this panel
 *
 *@author     Chris Seguin
 *@created    August 13, 1999
 */
public class SaveMenuSelection implements ActionListener {
	Saveable panel;


	/**
	 *  Constructor for the SaveMenuSelection object
	 *
	 *@param  init  Description of Parameter
	 */
	public SaveMenuSelection(Saveable init) {
		panel = init;
	}


	/**
	 *  Saves this panel
	 *
	 *@param  evt  The triggering event
	 */
	public void actionPerformed(ActionEvent evt) {
		try {
			panel.save();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}
}
