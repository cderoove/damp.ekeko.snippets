package org.acm.seguin.ide.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.ClassIcon;
import org.acm.seguin.uml.InterfaceIcon;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Holds the list of classes
 *
 *@author    Chris Seguin
 */
public class ClassListPanel extends JPanel {
	private PackageSummary summary;
	private UMLPackage umlPackage;


	/**
	 *  Constructor for the ClassListPanel object
	 *
	 *@param  init         Description of Parameter
	 *@param  initPackage  Description of Parameter
	 */
	public ClassListPanel(PackageSummary init, UMLPackage initPackage) {
		summary = init;
		umlPackage = initPackage;
		umlPackage.setClassListPanel(this);

		init();
	}


	/**
	 *  Used to reload the class list
	 *
	 *@param  init  Description of Parameter
	 */
	public void load(PackageSummary init) {
		summary = init;
		removeAll();
		init();
	}


	/**
	 *  Initializes the panel
	 */
	private void init() {
		setLayout(new GridBagLayout());
		setBackground(Color.white);

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel title;
		if (summary == null) {
			title = new JLabel("Unknown");
		}
		else {
			title = new JLabel(summary.getName());
		}
		title.setFont(new Font("Dialog", Font.BOLD, 14));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 10, 0, 10);
		add(title, gbc);

		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;

		int count = 1;
		Iterator iter = listTypes();
		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();
			addTypeToPanel(next, gbc, count);
			count++;
		}

		repaint();
	}


	/**
	 *  Adds a feature to the TypeToPanel attribute of the ClassListPanel object
	 *
	 *@param  nextType  The feature to be added to the TypeToPanel attribute
	 *@param  gbc       The feature to be added to the TypeToPanel attribute
	 *@param  count     The feature to be added to the TypeToPanel attribute
	 */
	private void addTypeToPanel(TypeSummary nextType, GridBagConstraints gbc, int count) {
		JumpToTypeAdapter jumpToType = new JumpToTypeAdapter(umlPackage, nextType);

		Icon icon;
		if (nextType.isInterface()) {
			icon = new InterfaceIcon(8, 8);
		}
		else {
			icon = new ClassIcon(8, 8);
		}
		IconPanel classPanel = new IconPanel(icon);
		gbc.gridx = 0;
		gbc.gridy = count;
		add(classPanel, gbc);
		classPanel.addMouseListener(jumpToType);

		JLabel classLabel = new JLabel(nextType.getName(), JLabel.LEFT);
		gbc.gridx = 1;
		add(classLabel, gbc);
		classLabel.addMouseListener(jumpToType);
	}


	/**
	 *  Creates a list of type summaries
	 *
	 *@return    Description of the Returned Value
	 */
	private Iterator listTypes() {
		TreeMap map = new TreeMap();

		Iterator iter = null;
		if (summary != null) iter = summary.getFileSummaries();
		while ((iter != null) && iter.hasNext()) {
			FileSummary nextFileSummary = (FileSummary) iter.next();
			Iterator iter2 = nextFileSummary.getTypes();
			while ((iter2 != null) && iter2.hasNext()) {
				TypeSummary nextType = (TypeSummary) iter2.next();
				map.put(nextType.getName(), nextType);
			}
		}

		return map.values().iterator();
	}


	/**
	 *  The main program for the ClassListPanel class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		(new SummaryTraversal("c:\\temp\\download")).go();
		javax.swing.JFrame frame = new javax.swing.JFrame("Class List");
		frame.getContentPane().add(new ClassListPanel(PackageSummary.getPackageSummary("java.lang"), null));
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new ExitOnCloseAdapter());
	}
}
