/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Color;

import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.uml.line.DragPanelAdapter;

/**
 *  Displays a single UML field in a line
 *
 *@author     Chris Seguin
 *@created    July 6, 1999
 */
public class UMLField extends UMLLine implements ISourceful {
	//  Instance Variables
	private FieldSummary summary;
	private UMLPackage current;
	private boolean association;
	private DragPanelAdapter parentDragAdapter;
	private DragPanelAdapter fieldDragAdapter;


	/**
	 *  Create a new instance of a UMLLine
	 *
	 *@param  initCurrent  Description of Parameter
	 *@param  parent       Description of Parameter
	 *@param  field        Description of Parameter
	 *@param  adapter      Description of Parameter
	 */
	public UMLField(UMLPackage initCurrent, UMLType parent, FieldSummary field, DragPanelAdapter adapter) {
		super(parent, adapter);

		//  Set the instance variables
		summary = field;
		current = initCurrent;
		association = false;

		//  Reset the parent data
		ModifierHolder modifiers = summary.getModifiers();
		setProtection(UMLLine.getProtectionCode(modifiers));
		setLabelText(summary.toString());
		setLabelFont(UMLLine.getProtectionFont(false, modifiers));

		//  Reset the size
		setSize(getPreferredSize());

		//  Create another adapter for draging this
		parentDragAdapter = adapter;
		fieldDragAdapter = new DragPanelAdapter(this, initCurrent);

		//  Add a mouse listener
		addMouseListener(new UMLMouseAdapter(current, parent, this));
	}


	/**
	 *  Transform into an association
	 *
	 *@param  way  Description of Parameter
	 */
	public void setAssociation(boolean way) {
		association = way;
		if (association) {
			setLabelText(summary.getName());
			addMouseListener(fieldDragAdapter);
			addMouseMotionListener(fieldDragAdapter);
			removeMouseListener(parentDragAdapter);
			removeMouseMotionListener(parentDragAdapter);
			label.addMouseListener(fieldDragAdapter);
			label.addMouseMotionListener(fieldDragAdapter);
			label.removeMouseListener(parentDragAdapter);
			label.removeMouseMotionListener(parentDragAdapter);
		}
		else {
			setLabelText(summary.toString());
			addMouseListener(parentDragAdapter);
			addMouseMotionListener(parentDragAdapter);
			removeMouseListener(fieldDragAdapter);
			removeMouseMotionListener(fieldDragAdapter);
			label.addMouseListener(parentDragAdapter);
			label.addMouseMotionListener(parentDragAdapter);
			label.removeMouseListener(fieldDragAdapter);
			label.removeMouseMotionListener(fieldDragAdapter);
		}

		setSize(getPreferredSize());
	}


	/**
	 *  Return the summary
	 *
	 *@return    Description of the Returned Value
	 */
	public FieldSummary getSummary() {
		return summary;
	}


	/**
	 *  Is this object represented as an association
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean isAssociation() {
		return association;
	}


	/**
	 *  Is this object represented as an association
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean isConvertable() {
		TypeDeclSummary typeDecl = summary.getTypeDecl();
		if (typeDecl.isPrimitive()) {
			return false;
		}

		TypeSummary typeSummary = GetTypeSummary.query(typeDecl);
		return (typeSummary != null);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public TypeSummary getType() {
		TypeDeclSummary typeDecl = summary.getTypeDecl();
		return GetTypeSummary.query(typeDecl);
	}


	/**
	 *  Return the default background color
	 *
	 *@return    the color
	 */
	protected Color getDefaultBackground() {
		if (association) {
			return Color.lightGray;
		}
		else {
			return super.getDefaultBackground();
		}
	}

  public org.acm.seguin.summary.Summary getSourceSummary() {
    return summary;
  }
}
