/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.awt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.uml.UMLPackage;

/**
 *  This object is responsible for centering the dialog box on the screen.
 *
 *@author    Chris Seguin
 */
public class CenterDialog {
	/**
	 *  Constructor for the CenterDialog object
	 */
	private CenterDialog() { }


	/**
	 *  Actually does the work
	 *
	 *@param  dialog  The dialog box
	 *@param  parent  the frame we are centering the dialog over or null if we
	 *      should center it on the screen
	 */
	public static void center(JDialog dialog, JFrame parent)
	{
		Dimension dim = dialog.getPreferredSize();
		Dimension frameSize;
		int x;
		int y;
		if (parent == null) {
			frameSize = Toolkit.getDefaultToolkit().getScreenSize();
			x = 0;
			y = 0;
		}
		else {
			frameSize = parent.getSize();
			Point loc = parent.getLocation();
			x = loc.x;
			y = loc.y;
		}

		x += (frameSize.width - dim.width) / 2;
		y += (frameSize.height - dim.height) / 2;

		dialog.setLocation(x, y);
	}


	/**
	 *  Actually does the work to center the dialog, but uses the
	 *  EditorOperation's frame to determine what to center the dialog over.
	 *
	 *@param  dialog  The dialog box
	 */
	public static void center(JDialog dialog)
	{
		EditorOperations eo = EditorOperations.get();
		if (eo == null) {
			center(dialog, (JFrame) null);
		}
		else {
			center(dialog, eo.getEditorFrame());
		}
	}


	/**
	 *  Center the dailog on a diagram's frame
	 *
	 *@param  dialog      the diagram
	 *@param  umlPackage  the frame
	 */
	public static void center(JDialog dialog, UMLPackage umlPackage)
	{
		if (umlPackage == null) {
			center(dialog);
			return;
		}

		Component current = umlPackage.getParent();
		while (!(current instanceof JFrame)) {
			current = current.getParent();
		}

		center(dialog, (JFrame) current);
	}
}
