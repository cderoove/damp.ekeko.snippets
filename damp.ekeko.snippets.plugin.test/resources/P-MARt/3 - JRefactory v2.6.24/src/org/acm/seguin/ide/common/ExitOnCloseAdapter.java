package org.acm.seguin.ide.common;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *  Simple adapter that exits the application when the frame is closed
 *
 *@author    Chris Seguin
 */
public class ExitOnCloseAdapter extends WindowAdapter {
	/**
	 *  The window is closing
	 *
	 *@param  evt  Description of Parameter
	 */
	public void windowClosing(WindowEvent evt) {
		System.exit(0);
	}
}
