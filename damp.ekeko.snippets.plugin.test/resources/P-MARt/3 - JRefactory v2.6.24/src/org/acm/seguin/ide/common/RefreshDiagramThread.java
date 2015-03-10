/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.util.Enumeration;

import org.acm.seguin.uml.UMLPackage;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
class RefreshDiagramThread extends Thread {
	private Enumeration enum;


	/**
	 *  Constructor for the RefreshDiagramThread object
	 *
	 *@param  init  Description of Parameter
	 */
	RefreshDiagramThread(Enumeration init) {
		enum = init;
	}


	/**
	 *  Main processing method for the RefreshDiagramThread object
	 */
	public void run() {
		SummaryLoaderThread.waitForLoading();

		while (enum.hasMoreElements()) {
			UMLPackage next = (UMLPackage) enum.nextElement();
			next.reload();
		}
	}
}
