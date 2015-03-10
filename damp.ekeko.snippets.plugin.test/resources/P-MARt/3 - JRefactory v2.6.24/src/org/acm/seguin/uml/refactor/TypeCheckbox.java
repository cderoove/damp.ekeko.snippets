/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import javax.swing.JCheckBox;

import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Holds a type summary in addition to being a checkbox
 *
 *@author    Chris Seguin
 */
class TypeCheckbox extends JCheckBox {
	private TypeSummary type;


	/**
	 *  Constructor for the TypeCheckbox object
	 *
	 *@param  init  Description of Parameter
	 */
	public TypeCheckbox(TypeSummary init) {
		super(" ");

		type = init;

		setText(getFullName());
		setSize(getPreferredSize());
		setSelected(true);
	}


	/**
	 *  Gets the TypeSummary attribute of the TypeCheckbox object
	 *
	 *@return    The TypeSummary value
	 */
	public TypeSummary getTypeSummary() {
		return type;
	}


	/**
	 *  Gets the FullName attribute of the TypeCheckbox object
	 *
	 *@return    The FullName value
	 */
	String getFullName() {
		StringBuffer buf = new StringBuffer(type.getName());
		Summary current = type.getParent();

		while (current != null) {
			if (current instanceof TypeSummary) {
				buf.insert(0, ".");
				buf.insert(0, ((TypeSummary) current).getName());
			}
			else if (current instanceof PackageSummary) {
				String temp = ((PackageSummary) current).getName();

				if ((temp != null) && (temp.length() > 0)) {
					buf.insert(0, ".");
					buf.insert(0, temp);
				}
			}
			current = current.getParent();
		}

		return buf.toString();
	}
}
