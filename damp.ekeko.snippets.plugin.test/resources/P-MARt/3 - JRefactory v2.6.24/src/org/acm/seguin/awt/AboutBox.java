package org.acm.seguin.awt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JWindow;

import org.acm.seguin.JRefactoryVersion;

/**
 *  About box for the JRefactory software component
 *
 *@author    Chris Seguin
 */
public class AboutBox extends JWindow implements ActionListener {
	/**
	 *  Constructor for the AboutBox object
	 */
	public AboutBox() {
		super();

		getContentPane().setLayout(null);

		Toolkit kit = getToolkit();
		setBackground(Color.black);
		getContentPane().setBackground(Color.black);

		ImagePanel imagePanel = new ImagePanel("JRefactory.jpg");

		int width = 0;
		int maxWidth = 0;
		int currentHeight = 0;

		Dimension dim = imagePanel.getPreferredSize();
		imagePanel.setLocation(25,5);
		getContentPane().add(imagePanel);

		width = 10 + dim.width;
		maxWidth = Math.max(maxWidth, dim.width);
		currentHeight = 10 + dim.height;

		JLabel label1 = new JLabel("Version:  " + (new JRefactoryVersion()).toString());
		label1.setHorizontalAlignment(label1.CENTER);
		label1.setForeground(Color.red);
		dim = label1.getPreferredSize();
		maxWidth = Math.max(maxWidth, dim.width);
		label1.setSize(dim);
		label1.setLocation((width - dim.width) / 2, currentHeight);
		currentHeight += (5 + dim.height);
		getContentPane().add(label1);

		JLabel label2 = new JLabel("Author:  Chris Seguin");
		label2.setHorizontalAlignment(label2.CENTER);
		label2.setForeground(Color.red);
		dim = label2.getPreferredSize();
		maxWidth = Math.max(maxWidth, dim.width);
		label2.setSize(dim);
		label2.setLocation((width - dim.width) / 2, currentHeight);
		currentHeight += (5 + dim.height);
		getContentPane().add(label2);

		JLabel label3 = new JLabel("Email:  seguin@acm.org");
		label3.setHorizontalAlignment(label3.CENTER);
		label3.setForeground(Color.red);
		dim = label3.getPreferredSize();
		maxWidth = Math.max(maxWidth, dim.width);
		label3.setSize(dim);
		label3.setLocation((width - dim.width) / 2, currentHeight);
		currentHeight += (5 + dim.height);
		getContentPane().add(label3);

		JLabel label4 = new JLabel("Home:  http://jrefactory.sourceforge.net");
		label4.setHorizontalAlignment(label4.CENTER);
		dim = label4.getPreferredSize();
		maxWidth = Math.max(maxWidth, dim.width);
		label4.setSize(dim);
		label4.setLocation((width - dim.width) / 2, currentHeight);
		label4.setForeground(Color.red);
		currentHeight += (5 + dim.height);
		getContentPane().add(label4);

		JButton okButton = new JButton("OK");
		okButton.setForeground(Color.red);
		okButton.setBackground(Color.black);
		okButton.addActionListener(this);
		dim = okButton.getPreferredSize();
		maxWidth = Math.max(maxWidth, dim.width);
		okButton.setSize(dim);
		okButton.setLocation((width - dim.width) / 2, currentHeight);
		currentHeight += (5 + dim.height);
		getContentPane().add(okButton);

		currentHeight += 5;
		maxWidth += 10;
		setSize(maxWidth, currentHeight);

		Point pt = imagePanel.getLocation();
		dim = imagePanel.getSize();
		imagePanel.setLocation((maxWidth - dim.width) / 2, 5);

		pt = label1.getLocation();
		dim = label1.getSize();
		label1.setLocation((maxWidth - dim.width) / 2, pt.y);

		pt = label2.getLocation();
		dim = label2.getSize();
		label2.setLocation((maxWidth - dim.width) / 2, pt.y);

		pt = label3.getLocation();
		dim = label3.getSize();
		label3.setLocation((maxWidth - dim.width) / 2, pt.y);

		pt = label4.getLocation();
		dim = label4.getSize();
		label4.setLocation((maxWidth - dim.width) / 2, pt.y);

		pt = okButton.getLocation();
		dim = okButton.getSize();
		okButton.setLocation((maxWidth - dim.width) / 2, pt.y);

		dim = kit.getScreenSize();
		setLocation((dim.width - width) / 2, (dim.height - currentHeight) / 2);
	}


	/**
	 *  The main program for the AboutBox class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		run();
	}

	public void actionPerformed(ActionEvent evt) {
		dispose();
	}

	public static void run() {
		(new AboutBox()).show();
	}
}
