/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.line.DragPanelAdapter;

/**
 *  Displays a single UML nested type in a line
 *
 *@author     Chris Seguin
 *@created    July 6, 1999
 */
public class UMLNestedType extends UMLLine implements ISourceful {
	//  Instance Variables
	private TypeSummary summary;
	private UMLPackage current;


	/**
	 *  Create a new instance of a UMLLine
	 *
	 *@param  initCurrent  Description of Parameter
	 *@param  parent       Description of Parameter
	 *@param  nestedType   Description of Parameter
	 *@param  adapter      Description of Parameter
	 */
	public UMLNestedType(UMLPackage initCurrent, UMLType parent, TypeSummary nestedType, DragPanelAdapter adapter) {
		super(parent, adapter);

		//  Set the instance variables
		summary = nestedType;
		current = initCurrent;

		//  Reset the parent data
		ModifierHolder modifiers = summary.getModifiers();
		setLabelText(summary.toString());
		setLabelFont(UMLLine.getProtectionFont(false, modifiers));

		//  Reset the size
		setSize(getPreferredSize());

		//  Add a mouse listener
		addMouseListener(new UMLMouseAdapter(current, parent, this));

		if (summary.isInterface()) {
			icon = new InterfaceIcon(8, 8);
		}
		else {
			icon = new ClassIcon(8, 8);
		}
	}


	/**
	 *  Return the summary
	 *
	 *@return    Description of the Returned Value
	 */
	public TypeSummary getSummary() {
		return summary;
	}


	/**
	 *  Gets the SourceSummary attribute of the UMLNestedType object
	 *
	 *@return    The SourceSummary value
	 */
	public org.acm.seguin.summary.Summary getSourceSummary() {
		return summary;
	}
}

