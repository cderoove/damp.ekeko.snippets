package org.acm.seguin.awt;

import java.awt.Dimension;
import java.awt.Graphics;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *  Little panel that holds an image
 *
 *@author    Chris Seguin
 */
public class ImagePanel extends JPanel {
	private ImageIcon imgIcon;
	private int wide;
	private int high;


	/**
	 *  Constructor for the ImagePanel object
	 *
	 *@param  init  Description of Parameter
	 */
	public ImagePanel(String init) {
		ClassLoader cl = getClass().getClassLoader();
		URL url = cl.getResource(init);
		imgIcon = new ImageIcon(url);

		wide = imgIcon.getIconWidth();
		high = imgIcon.getIconHeight();
		Dimension dim = new Dimension(wide, high);
		setPreferredSize(dim);
		setSize(dim);
	}


	/**
	 *  Draw the image on the panel
	 *
	 *@param  g  the graphics context
	 */
	public void paint(Graphics g) {
		imgIcon.paintIcon(this, g, 0, 0);
	}
}

