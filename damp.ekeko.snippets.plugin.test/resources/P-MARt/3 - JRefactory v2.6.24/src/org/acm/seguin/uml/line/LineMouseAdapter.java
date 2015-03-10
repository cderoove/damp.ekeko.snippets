/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.line;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *  MouseAdapter 
 *
 *@author     Chris Seguin 
 *@created    July 28, 1999 
 */
public class LineMouseAdapter implements MouseListener, MouseMotionListener {
	private LinedPanel panel;


	/**
	 *  Constructor for the LineMouseAdapter object 
	 *
	 *@param  init  the panel that contains segmented lines 
	 */
	public LineMouseAdapter(LinedPanel init) {
		panel = init;
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  the mouse event 
	 */
	public void mouseClicked(MouseEvent mevt) {
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  the mouse event 
	 */
	public void mouseEntered(MouseEvent mevt) {
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  the mouse event 
	 */
	public void mouseExited(MouseEvent mevt) {
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  the mouse event 
	 */
	public void mousePressed(MouseEvent mevt) {
		Point result = mevt.getPoint();
		panel.hit(result);
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  the mouse event 
	 */
	public void mouseReleased(MouseEvent mevt) {
		panel.drop();
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  Description of Parameter 
	 */
	public void mouseDragged(MouseEvent mevt) {
		panel.drag(mevt.getPoint());
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  mevt  Description of Parameter 
	 */
	public void mouseMoved(MouseEvent mevt) {
	}
}
