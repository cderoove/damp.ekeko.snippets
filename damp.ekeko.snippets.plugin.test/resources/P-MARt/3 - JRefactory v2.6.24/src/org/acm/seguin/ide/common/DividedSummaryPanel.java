package org.acm.seguin.ide.common;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Creates an object that holds the divided summary panel
 *
 *@author    Chris Seguin
 */
public class DividedSummaryPanel {
	JScrollPane keyPane;
	JScrollPane summaryPane;
	JSplitPane splitPane;


	/**
	 *  Constructor for the DividedSummaryPanel object
	 *
	 *@param  summary     Description of Parameter
	 *@param  umlPackage  Description of Parameter
	 */
	public DividedSummaryPanel(PackageSummary summary, UMLPackage umlPackage) {
		init(summary, umlPackage);
	}


	/**
	 *  Gets the Pane attribute of the DividedSummaryPanel object
	 *
	 *@return    The Pane value
	 */
	public JComponent getPane() {
		return splitPane;
	}


	/**
	 *  Initializes the splitpane
	 *
	 *@param  summary     Description of Parameter
	 *@param  umlPackage  Description of Parameter
	 */
	private void init(PackageSummary summary, UMLPackage umlPackage) {
		keyPane = new JScrollPane(new KeyPanel());
		summaryPane = new JScrollPane(new ClassListPanel(summary, umlPackage));
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, keyPane, summaryPane);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);
		keyPane.setMinimumSize(new Dimension(50, 50));
		summaryPane.setMinimumSize(new Dimension(50, 50));
	}


	/**
	 *  The main program for the DividedSummaryPanel class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		(new SummaryTraversal("c:\\temp\\download")).go();
		javax.swing.JFrame frame = new javax.swing.JFrame("Divided Summary");
		DividedSummaryPanel dsp = new DividedSummaryPanel(PackageSummary.getPackageSummary("java.lang"), null);
		frame.getContentPane().add(dsp.getPane());
		frame.pack();
		frame.setSize(200, 400);
		frame.setVisible(true);
		frame.addWindowListener(new ExitOnCloseAdapter());
	}
}
