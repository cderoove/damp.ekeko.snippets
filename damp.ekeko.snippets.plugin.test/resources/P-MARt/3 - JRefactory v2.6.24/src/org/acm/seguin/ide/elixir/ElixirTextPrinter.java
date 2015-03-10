/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

import java.io.File;

import org.acm.seguin.ide.common.TextPrinter;
import org.acm.seguin.tools.install.RefactoryInstaller;
import org.acm.seguin.uml.print.PrintingThread;

/**
 *  Text printer for elixir 
 *
 *@author     Chris Seguin 
 *@created    May 31, 1999 
 */
public class ElixirTextPrinter extends TextPrinter {

	/**
	 *  Prints the current document 
	 */
	public void print() {
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		//  Get the data from the window
		FrameManager fm = FrameManager.current();
		ViewManager currentView = fm.getViewSite().getCurrentViewManager();
		if (currentView instanceof UMLViewManager) {
			UMLViewManager node = (UMLViewManager) currentView;
			(new PrintingThread(node.getDiagram())).start();
		}
		else {
			BasicViewManager bvm = (BasicViewManager) currentView;
			String windowText = bvm.getContentsString();
			String fullFilename = bvm.getTitle();
			File file = new File(fullFilename);

			print(file.getName(), windowText);
		}
	}


	/**
	 *  Prints the current document 
	 */
	public static void printCurrent() {
		(new ElixirTextPrinter()).print();
	}
}
