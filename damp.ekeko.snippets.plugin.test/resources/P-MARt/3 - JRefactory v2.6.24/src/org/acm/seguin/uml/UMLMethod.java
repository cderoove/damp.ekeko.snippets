/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.uml.line.DragPanelAdapter;

/**
 *  Displays a single UML method in a line
 *
 *@author     Chris Seguin
 *@created    July 6, 1999
 */
public class UMLMethod extends UMLLine implements ISourceful {
	//  Instance Variables
	private MethodSummary summary;
	private UMLPackage current;


	/**
	 *  Create a new instance of a UMLLine
	 *
	 *@param  initCurrent  Description of Parameter
	 *@param  parent       Description of Parameter
	 *@param  method       Description of Parameter
	 *@param  adapter      Description of Parameter
	 */
	public UMLMethod(UMLPackage initCurrent, UMLType parent, MethodSummary method, DragPanelAdapter adapter) {
		super(parent, adapter);

		//  Set the instance variables
		summary = method;
		current = initCurrent;

		//  Reset the parent data
		ModifierHolder modifiers = summary.getModifiers();
		setProtection(UMLLine.getProtectionCode(modifiers));
		setLabelText(summary.toString());
		setLabelFont(UMLLine.getProtectionFont(false, modifiers));

		//  Reset the size
		setSize(getPreferredSize());

		//  Add a mouse listener
		addMouseListener(new UMLMouseAdapter(current, parent, this));
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public MethodSummary getSummary() {
		return summary;
	}
  public org.acm.seguin.summary.Summary getSourceSummary() {
    return summary;
  }
}
