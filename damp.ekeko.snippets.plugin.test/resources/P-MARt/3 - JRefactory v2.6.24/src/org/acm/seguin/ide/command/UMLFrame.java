package org.acm.seguin.ide.command;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.acm.seguin.ide.common.DividedSummaryPanel;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.uml.UMLPackage;

/**
 *  The UML frame
 *
 *@author    Chris Seguin
 */
public class UMLFrame extends JFrame {
	private PackageSummary packageSummary;
	private UMLPackage view;
	private JSplitPane splitPane;


	/**
	 *  Constructor for the UMLFrame object
	 *
	 *@param  init  Description of Parameter
	 */
	public UMLFrame(PackageSummary init) {
		super(init.getName());

		packageSummary = init;
		setup();
	}


	/**
	 *  Gets the UmlPackage attribute of the UMLFrame object
	 *
	 *@return    The UmlPackage value
	 */
	public UMLPackage getUmlPackage() {
		return view;
	}


	/**
	 *  Description of the Method
	 */
	private void setup() {
		view = new UMLPackage(packageSummary);

		JScrollPane pane = new JScrollPane(view);
		view.setScrollPane(pane);

		JScrollBar horiz = pane.getHorizontalScrollBar();
		horiz.setUnitIncrement(400);
		JScrollBar vert = pane.getVerticalScrollBar();
		vert.setUnitIncrement(400);

		DividedSummaryPanel dsp = new DividedSummaryPanel(packageSummary, view);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dsp.getPane(), pane);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		dsp.getPane().setMinimumSize(new Dimension(50, 150));
		pane.setMinimumSize(new Dimension(150, 150));

		getContentPane().add(splitPane);

		setSize(500, 350);

		CommandLineMenu clm = new CommandLineMenu();
		setJMenuBar(clm.getMenuBar(view));
		setVisible(true);
	}
}
