package org.acm.seguin.ide.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.acm.seguin.uml.ClassIcon;
import org.acm.seguin.uml.InterfaceIcon;
import org.acm.seguin.uml.ProtectionIcon;
import org.acm.seguin.uml.UMLLine;

/**
 *  Stores the key for the UML diagram
 *
 *@author    Chris Seguin
 */
public class KeyPanel extends JPanel {
	/**
	 *  Constructor for the KeyPanel object
	 */
	public KeyPanel() {
		init();
	}


	/**
	 *  Initializes the panel
	 */
	private void init() {
		setLayout(new GridBagLayout());
		setBackground(Color.white);

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel title = new JLabel("Key");
		title.setFont(new Font("Dialog", Font.BOLD, 14));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 10, 0, 10);
		add(title, gbc);

		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;

		ProtectionIcon pi = new ProtectionIcon(8, 8);
		pi.setProtection(UMLLine.PRIVATE);
		IconPanel privatePanel = new IconPanel(pi);
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(privatePanel, gbc);

		JLabel privateLabel = new JLabel("Private Scope", JLabel.LEFT);
		gbc.gridx = 1;
		add(privateLabel, gbc);

		pi = new ProtectionIcon(8, 8);
		pi.setProtection(UMLLine.DEFAULT);
		IconPanel packagePanel = new IconPanel(pi);
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(packagePanel, gbc);

		JLabel packageLabel = new JLabel("Package or Default Scope", JLabel.LEFT);
		gbc.gridx = 1;
		add(packageLabel, gbc);

		pi = new ProtectionIcon(8, 8);
		pi.setProtection(UMLLine.PROTECTED);
		IconPanel protectedPanel = new IconPanel(pi);
		gbc.gridx = 0;
		gbc.gridy = 3;
		add(protectedPanel, gbc);

		JLabel protectedLabel = new JLabel("Protected Scope", JLabel.LEFT);
		gbc.gridx = 1;
		add(protectedLabel, gbc);

		pi = new ProtectionIcon(8, 8);
		pi.setProtection(UMLLine.PUBLIC);
		IconPanel publicPanel = new IconPanel(pi);
		gbc.gridx = 0;
		gbc.gridy = 4;
		add(publicPanel, gbc);

		JLabel publicLabel = new JLabel("Public Scope", JLabel.LEFT);
		gbc.gridx = 1;
		add(publicLabel, gbc);

		ClassIcon ci = new ClassIcon(8, 8);
		IconPanel classPanel = new IconPanel(ci);
		gbc.gridx = 0;
		gbc.gridy = 5;
		add(classPanel, gbc);

		JLabel classLabel = new JLabel("Class", JLabel.LEFT);
		gbc.gridx = 1;
		add(classLabel, gbc);

		InterfaceIcon ii = new InterfaceIcon(8, 8);
		IconPanel interfacePanel = new IconPanel(ii);
		gbc.gridx = 0;
		gbc.gridy = 6;
		add(interfacePanel, gbc);

		JLabel interfaceLabel = new JLabel("Interface", JLabel.LEFT);
		gbc.gridx = 1;
		add(interfaceLabel, gbc);

		JLabel instanceItem = new JLabel("plain");
		instanceItem.setFont(new Font("Dialog", Font.PLAIN, 12));
		gbc.gridx = 0;
		gbc.gridy = 7;
		add(instanceItem, gbc);

		JLabel instanceLabel = new JLabel("Instance Variable or Method", JLabel.LEFT);
		gbc.gridx = 1;
		add(instanceLabel, gbc);

		JLabel staticItem = new JLabel("bold");
		staticItem.setFont(new Font("Dialog", Font.BOLD, 12));
		gbc.gridx = 0;
		gbc.gridy = 8;
		add(staticItem, gbc);

		JLabel staticLabel = new JLabel("Static Variable or Method", JLabel.LEFT);
		gbc.gridx = 1;
		add(staticLabel, gbc);

		JLabel abstractItem = new JLabel("italic");
		abstractItem.setFont(new Font("Dialog", Font.ITALIC, 12));
		gbc.gridx = 0;
		gbc.gridy = 9;
		add(abstractItem, gbc);

		JLabel abstractLabel = new JLabel("Abstract Class or Method", JLabel.LEFT);
		gbc.gridx = 1;
		add(abstractLabel, gbc);
	}


	/**
	 *  The main program for the KeyPanel class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		javax.swing.JFrame frame = new javax.swing.JFrame("Key Panel");
		frame.getContentPane().add(new KeyPanel());
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new ExitOnCloseAdapter());
	}
}
