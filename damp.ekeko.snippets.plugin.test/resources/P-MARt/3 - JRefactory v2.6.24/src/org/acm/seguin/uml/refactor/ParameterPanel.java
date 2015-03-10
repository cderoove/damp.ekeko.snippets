/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.GridLayout;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;

/**
 *  Panel of radio buttons containing the parameters
 *
 *@author    Chris Seguin
 */
class ParameterPanel extends JPanel {
	private MethodSummary methodSummary;
	private LinkedList children;


	/**
	 *  Constructor for the ParameterPanel object
	 *
	 *@param  initType  The type
	 */
	public ParameterPanel(MethodSummary init) {
		methodSummary = init;

		children = new LinkedList();
		ButtonGroup buttonGroup = new ButtonGroup();

		Iterator iter = methodSummary.getParameters();
		int count = 0;
		while (iter.hasNext()) {
			ParameterSummary next = (ParameterSummary) iter.next();

			ParameterRadioButton tcb = new ParameterRadioButton(next);
			children.add(tcb);
			buttonGroup.add(tcb);
			tcb.setSelected(count == 0);
			count++;
		}

		int columns = count / 10 + 1;
		setLayout(new GridLayout(count / columns + 1, columns));

		iter = children.iterator();
		while (iter.hasNext()) {
			add((JComponent) iter.next());
		}
	}


	/**
	 *  Gets the selected parameter
	 *
	 *@return    The selected parameter
	 */
	public ParameterSummary get() {
		Iterator iter = children.iterator();
		while (iter.hasNext()) {
			ParameterRadioButton prb = (ParameterRadioButton) iter.next();
			if (prb.isSelected()) {
				return prb.getParameterSummary();
			}
		}

		return null;
	}
}
