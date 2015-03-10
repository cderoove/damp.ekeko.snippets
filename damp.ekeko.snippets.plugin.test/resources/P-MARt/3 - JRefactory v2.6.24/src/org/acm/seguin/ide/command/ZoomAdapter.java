package org.acm.seguin.ide.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.acm.seguin.uml.line.LinedPanel;

/**
 *  Zooms in/out on a particular panel based on a scalar input 
 *
 *@author    Chris Seguin 
 */
public class ZoomAdapter implements ActionListener {
	private LinedPanel panel;
	private double scale;


	/**
	 *  Constructor for the ZoomAction object 
	 *
	 *@param  panel  The panel 
	 *@param  scale  the scaling factor 
	 */
	public ZoomAdapter(LinedPanel panel, double scale) {
		this.panel = panel;
		this.scale = scale;
	}


	/**
	 *  The button has been pressed, do it! 
	 *
	 *@param  evt  the action event 
	 */
	public void actionPerformed(ActionEvent evt) {
		panel.scale(scale);
		panel.repaint();
	}
}
