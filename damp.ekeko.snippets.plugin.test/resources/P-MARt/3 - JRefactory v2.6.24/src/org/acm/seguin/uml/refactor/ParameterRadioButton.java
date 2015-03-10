package org.acm.seguin.uml.refactor;

import javax.swing.JRadioButton;

import org.acm.seguin.summary.ParameterSummary;

/**
 *  This radio button holds a parameter and sets the name
 *  of the radio button automatically.  You can ask for
 *  the parameter summary.
 *
 *@author    Chris Seguin
 */
class ParameterRadioButton extends JRadioButton {
	private ParameterSummary summary;


	/**
	 *  Constructor for the ParameterRadioButton object
	 *
	 *@param  init  the parameter summary
	 */
	public ParameterRadioButton(ParameterSummary init) {
		summary = init;
		setText(summary.getName() + " (" + summary.getType() + ")");
	}


	/**
	 *  Gets the ParameterSummary
	 *
	 *@return    The ParameterSummary
	 */
	public ParameterSummary getParameterSummary() {
		return summary;
	}
}
