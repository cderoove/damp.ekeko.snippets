package org.acm.seguin.ide.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import org.acm.seguin.awt.ExceptionPrinter;

/**
 *  Exits after this menu option is selected
 *
 *@author     Chris Seguin
 *@created    August 2, 1999
 */
public class ExitMenuSelection extends WindowAdapter implements ActionListener {
	/**
	 *  Exits when this menu items is selected
	 *
	 *@param  evt  The triggering event
	 */
	public void actionPerformed(ActionEvent evt) {
		finish();
	}


	/**
	 *  The window is closing
	 *
	 *@param  evt  Description of Parameter
	 */
	public void windowClosing(WindowEvent evt) {
		finish();
	}


	/**
	 *  Close everything down
	 */
	private void finish() {
		PackageSelectorPanel psp = PackageSelectorPanel.getMainPanel(null);
		if (psp != null) {
			try {
				psp.save();
			}
			catch (IOException ioe) {
				ExceptionPrinter.print(ioe);
			}
		}

		System.exit(0);
	}
}
