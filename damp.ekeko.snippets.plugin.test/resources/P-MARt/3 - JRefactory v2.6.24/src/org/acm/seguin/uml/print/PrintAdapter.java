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

import org.acm.seguin.uml.UMLPackage;

/**
 *  Handles the print menu option 
 *
 *@author     Chris Seguin 
 *@created    August 6, 1999 
 */
public class PrintAdapter implements ActionListener {
	private UMLPackage currentPackage;


	/**
	 *  Constructor for the PrintAdapter object 
	 *
	 *@param  panel  the current package 
	 */
	public PrintAdapter(UMLPackage panel) {
		currentPackage = panel;
	}


	/**
	 *  The action performed 
	 *
	 *@param  ev  the action event 
	 */
	public void actionPerformed(ActionEvent ev) {
		Thread pt = new PrintingThread(currentPackage);
		pt.start();
	}
}
