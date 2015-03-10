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

import javax.swing.JPanel;

import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.ChildClassSearcher;

/**
 *  Panel of checkboxes with all of the children classes listed
 *
 *@author    Chris Seguin
 */
class ChildClassCheckboxPanel extends JPanel {
	private TypeSummary parentType;
	private LinkedList childrenCheckboxes;


	/**
	 *  Constructor for the ChildClassCheckboxPanel object
	 *
	 *@param  initType  The type
	 */
	public ChildClassCheckboxPanel(TypeSummary initType) {
		parentType = initType;

		childrenCheckboxes = new LinkedList();

		Iterator iter = ChildClassSearcher.query(parentType);
		int count = 0;
		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();

			TypeCheckbox tcb = new TypeCheckbox(next);
			childrenCheckboxes.add(tcb);
			count++;
		}

		int columns = count / 10 + 1;
		setLayout(new GridLayout(count / columns + 1, columns));

		iter = childrenCheckboxes.iterator();
		while (iter.hasNext()) {
			add((TypeCheckbox) iter.next());
		}
	}


	/**
	 *  Gets the Checkboxes associated with this child class
	 *
	 *@return    The list of type checkboxes
	 */
	public Iterator getCheckboxes() {
		return childrenCheckboxes.iterator();
	}
}
