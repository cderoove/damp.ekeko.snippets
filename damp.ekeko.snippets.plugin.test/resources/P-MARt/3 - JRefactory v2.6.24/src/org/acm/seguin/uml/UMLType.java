/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.line.AssociationRelationship;
import org.acm.seguin.uml.line.DragPanelAdapter;
import org.acm.seguin.uml.line.EndPointPanel;
import org.acm.seguin.uml.line.LabelSizeComputation;
import org.acm.seguin.uml.line.SizableLabel;

/**
 *  Displays the summary of a type object
 *
 *@author     Chris Seguin
 *@created    June 7, 1999
 */
public class UMLType extends EndPointPanel implements ISourceful {
	/**
	 *  Description of the Field
	 */
	protected int borderWidth = 2;
	/**
	 *  Description of the Field
	 */
	protected int lineSize = 0;
	//  Instance Variables
	private UMLPackage parent;
	private SizableLabel nameLabel;
	private RoleHolder roles;
	private TypeSummary type;
	private int wide;
	private int high;
	private int titleHeight;
	private int state;

	//  Colors
	private static Color defaultBG = null;
	private static Color selectedBG;
	private static Color foreignBG;
	private static Color selectedForeignBG;

	//  States
	private final static int DEFAULT = 0;
	private final static int SELECTED = 1;
	private final static int FOREIGN = 2;

	private final static int TITLE_BORDER = 4;


	/**
	 *  Create a new instance of a UMLType
	 *
	 *@param  initParent  the parent
	 *@param  initType    the initial type data
	 *@param  foreign     Description of Parameter
	 */
	public UMLType(UMLPackage initParent, TypeSummary initType, boolean foreign) {
		super(null, true);

		//  Remember local variables
		parent = initParent;
		type = initType;
		wide = 0;
		high = 0;

		if (foreign) {
			state = FOREIGN;
		}
		else {
			state = DEFAULT;
		}

		//  Create a mouse listener
		UMLMouseAdapter listener = new UMLMouseAdapter(parent, this, null);
		addMouseListener(listener);

		//  Create another adapter for draging this
		DragPanelAdapter adapter = new DragPanelAdapter(this, parent);
		addMouseListener(adapter);
		addMouseMotionListener(adapter);

		//  Add the name label
		nameLabel = new SizableLabel(type.getName());
		nameLabel.setLocation(borderWidth, borderWidth);
		nameLabel.setSLHorizontalAlignment(JLabel.CENTER);
		nameLabel.setSLFont(UMLLine.getProtectionFont(true, type.getModifiers()));
		Dimension titleSize = nameLabel.getPreferredSize();
		titleHeight = titleSize.height;
		wide = titleSize.width + 2 * TITLE_BORDER;
		add(nameLabel);
		nameLabel.addMouseListener(listener);
		nameLabel.addMouseListener(adapter);
		nameLabel.addMouseMotionListener(adapter);

		//  Check to see if we need a role
		roles = new RoleHolder(listener, adapter);
		if (type.isInterface()) {
			roles.add(((char) 171) + "Interface" + ((char) 187));
		}
		if (foreign) {
			roles.add("Package:  " + getPackageName());
		}

		if (roles.hasAny()) {
			roles.setLocation(borderWidth, borderWidth + titleSize.height);
			add(roles);
			Dimension roleSize = roles.getPreferredSize();
			roles.setSize(roleSize);
			wide = Math.max(wide, roleSize.width);
			titleHeight += roleSize.height;
		}

		//  Determine the size of a line
		lineSize = computeLineSize();

		//  Add attribute labels
		int nY = titleHeight + borderWidth * 2;
		Iterator iter = type.getFields();
		if (iter != null) {
			while (iter.hasNext()) {
				UMLField field = new UMLField(parent, this, (FieldSummary) iter.next(), adapter);
				field.setLocation(borderWidth, nY);
				add(field);
				lineSize = field.getPreferredSize().height;
				nY += field.getPreferredSize().height;
				wide = Math.max(wide, field.getPreferredSize().width);
			}
		}
		else {
			nY += lineSize;
		}

		//  Add operation label
		nY += borderWidth;
		iter = type.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary nextMethod = (MethodSummary) iter.next();
				if (!nextMethod.isInitializer()) {
					UMLMethod method = new UMLMethod(parent, this, nextMethod, adapter);
					method.setLocation(borderWidth, nY);
					add(method);
					nY += method.getPreferredSize().height;
					wide = Math.max(wide, method.getPreferredSize().width);
				}
			}
		}
		else {
			nY += lineSize;
		}

		//  Add nested types label
		int nestedTypes = type.getTypeCount();
		if (nestedTypes > 0) {
			nY += borderWidth;
			iter = type.getTypes();
			if (iter != null) {
				while (iter.hasNext()) {
					UMLNestedType nestedType = new UMLNestedType(parent, this, (TypeSummary) iter.next(), adapter);
					nestedType.setLocation(borderWidth, nY);
					add(nestedType);
					nY += nestedType.getPreferredSize().height;
					wide = Math.max(wide, nestedType.getPreferredSize().width);
				}
			}
		}

		//  Add the final extra space at the bottom
		high = nY + borderWidth;

		//  Set the size
		nameLabel.setSize(wide, titleSize.height);
		if (roles.hasAny()) {
			roles.resetWidth(wide);
		}

		//  Revise the width
		wide += (2 * borderWidth);

		//  Set the size for the whole thing
		setSize(getPreferredSize());
	}


	/**
	 *  Sets the Selected attribute of the UMLType object
	 *
	 *@param  way  The new Selected value
	 */
	public void setSelected(boolean way) {
		if (way) {
			select();
		}
		else {
			deselect();
		}
	}


	/**
	 *  Get the summary
	 *
	 *@return    the summary
	 */
	public TypeSummary getSummary() {
		return type;
	}


	/**
	 *  Returns the minimum size
	 *
	 *@return    The size
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}


	/**
	 *  Returns the preferred size
	 *
	 *@return    The size
	 */
	public Dimension getPreferredSize() {
		return new Dimension(wide, high);
	}


	/**
	 *  Get the UML package that is holding this
	 *
	 *@return    the package
	 */
	public UMLPackage getPackage() {
		return parent;
	}


	/**
	 *  Determine if this is selected
	 *
	 *@return    true if this is selected
	 */
	public boolean isSelected() {
		return ((state & SELECTED) > 0);
	}


	/**
	 *  Determine if this is foreign
	 *
	 *@return    true if this is foreign
	 */
	public boolean isForeign() {
		return ((state & FOREIGN) > 0);
	}


	/**
	 *  Return the background color
	 *
	 *@return    the background color
	 */
	public Color getBackgroundColor() {
		if (defaultBG == null) {
			UMLType.initColors();
		}

		if (state == SELECTED) {
			return selectedBG;
		}
		if (state == FOREIGN) {
			return foreignBG;
		}
		if (isSelected() && isForeign()) {
			return selectedForeignBG;
		}
		return defaultBG;
	}


	/**
	 *  Returns an identifier for a type
	 *
	 *@return    an identifier for this panel
	 */
	public String getID() {
		return type.getPackageSummary().getName() + ":" + type.getName();
	}


	/**
	 *  Count the number of attributes
	 *
	 *@param  name  Description of Parameter
	 *@return       the number of attributes
	 */
	public UMLField getField(String name) {
		if (name == null) {
			return null;
		}

		Component[] children = getComponents();
		int last = children.length;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLField) {
				UMLField field = (UMLField) children[ndx];
				if (name.equals(field.getSummary().getName())) {
					return field;
				}
			}
		}

		return null;
	}


	/**
	 *  Paint this object
	 *
	 *@param  g  the graphics object
	 */
	public void paint(Graphics g) {
		//  Set the background color
		Color bg = getBackgroundColor();
		setBackground(bg);
		roles.setBackground(bg);

		//  Paint the components
		super.paint(g);

		drawFrame(g, 0, 0);
	}


	/**
	 *  Print this object
	 *
	 *@param  g  the graphics object
	 *@param  x  the x coordinate
	 *@param  y  the y coordinate
	 */
	public void print(Graphics g, int x, int y) {
		//  Set the background color
		Rectangle bounds = getBounds();
		g.setColor(getBackgroundColor());
		g.fillRect(x, y, bounds.width, bounds.height);

		//  Draw the title
		Point pt = nameLabel.getLocation();
		nameLabel.print(g, x + pt.x, y + pt.y);

		//  Draw the role
		if (roles.hasAny()) {
			pt = roles.getLocation();
			roles.print(g, x + pt.x, y + pt.y);
		}

		//  Paint the components
		Component[] children = getComponents();
		int last = children.length;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLLine) {
				pt = children[ndx].getLocation();
				((UMLLine) children[ndx]).print(g, x + pt.x, y + pt.y);
			}
		}

		drawFrame(g, x, y);
	}


	/**
	 *  Resizes and repositions the compontents
	 */
	public void resize() {
		//  Local Variables
		Component[] children = getComponents();
		int last = children.length;

		//  Set the default sizes
		wide = 0;
		high = 0;

		//  Get the size of the title
		Dimension titleSize = nameLabel.getPreferredSize();
		titleHeight = titleSize.height;
		wide = titleSize.width + 2 * borderWidth;
		if (roles.hasAny()) {
			Dimension roleSize = roles.getPreferredSize();
			titleHeight += roleSize.height;
			wide = Math.max(roleSize.width, wide);
		}

		//  Add attribute labels
		int nY = titleHeight + 2 * borderWidth;
		boolean foundField = false;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLField) {
				UMLField field = (UMLField) children[ndx];
				field.setLocation(borderWidth, nY);
				nY += lineSize;
				wide = Math.max(wide, field.getPreferredSize().width);
				foundField = true;
			}
		}
		if (!foundField) {
			nY += lineSize;
		}

		//  Add operation label
		nY += borderWidth;
		boolean foundMethod = false;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLMethod) {
				UMLMethod method = (UMLMethod) children[ndx];
				method.setLocation(borderWidth, nY);
				nY += lineSize;
				wide = Math.max(wide, method.getPreferredSize().width);
				foundMethod = true;
			}
		}

		if (!foundMethod) {
			nY += lineSize;
		}

		//  Add nested types label
		int nestedTypes = type.getTypeCount();
		if (nestedTypes > 0) {
			nY += borderWidth;
			for (int ndx = 0; ndx < last; ndx++) {
				if (children[ndx] instanceof UMLNestedType) {
					UMLNestedType nestedType = (UMLNestedType) children[ndx];
					nestedType.setLocation(borderWidth, nY);
					nY += lineSize;
					wide = Math.max(wide, nestedType.getPreferredSize().width);
				}
			}
		}

		//  Add the final extra space at the bottom
		high = nY + borderWidth;

		//  Set the size
		nameLabel.setSize(wide, titleSize.height);
		if (roles.hasAny()) {
			roles.resetWidth(wide);
		}

		//  Revise the width
		wide += (2 * borderWidth);

		//  Set the size for the whole thing
		setSize(getPreferredSize());
		parent.repaint();
	}


	/**
	 *  Select this item
	 */
	public void select() {
		state = state | SELECTED;
		repaint();
	}


	/**
	 *  Select this item
	 */
	public void deselect() {
		state = state & ~SELECTED;
		repaint();
	}


	/**
	 *  Toggle the selected state
	 */
	public void toggleSelect() {
		state = state ^ SELECTED;
		repaint();
	}


	/**
	 *  Save the files
	 *
	 *@param  output  the output stream
	 */
	public void save(PrintWriter output) {
		Point pt = getUnscaledLocation();
		output.println("P[" + getID() + "]{" + pt.x + "," + pt.y + "}");
	}


	/**
	 *  Load the type
	 *
	 *@param  buffer  the buffer
	 */
	public void load(String buffer) {
		StringTokenizer tok = new StringTokenizer(buffer, ",");
		String strX = tok.nextToken();
		String strY = tok.nextToken();

		try {
			setLocation(Integer.parseInt(strX), Integer.parseInt(strY));
		}
		catch (NumberFormatException nfe) {
		}
	}


	/**
	 *  Convert an attribute to an association
	 *
	 *@param  packagePanel  the package panel
	 *@param  fieldPanel    the field panel
	 *@return               the new segmented line
	 */
	public AssociationRelationship convertToAssociation(UMLPackage packagePanel, UMLField fieldPanel) {
		remove(fieldPanel);
		resize();
		packagePanel.add(fieldPanel);

		TypeSummary typeSummary = fieldPanel.getType();
		JPanel endPanel = packagePanel.findType(typeSummary);
		if (endPanel == null) {
			endPanel = new UMLType(packagePanel, typeSummary, true);
			packagePanel.add(endPanel);
			endPanel.setLocation(0, 0);
		}

		AssociationRelationship result = new AssociationRelationship(this, (EndPointPanel) endPanel, fieldPanel);
		packagePanel.add(result);

		return result;
	}


	/**
	 *  Convert from an association to an attribute
	 *
	 *@param  packagePanel  the package panel
	 *@param  fieldPanel    the field panel
	 */
	public void convertToAttribute(UMLPackage packagePanel, UMLField fieldPanel) {
		packagePanel.remove(fieldPanel);
		packagePanel.removeAssociation(fieldPanel);
		add(fieldPanel);
		resize();
	}


	/**
	 *  Sets the scaling factor
	 *
	 *@param  value  scaling factor
	 */
	public void scale(double value) {
		super.scale(value);
		nameLabel.scale(value);
		roles.scale(value);

		//  Rescale the children
		Component[] children = getComponents();
		int last = children.length;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLLine) {
				((UMLLine) children[ndx]).scale(value);
			}
		}
	}


	/**
	 *  Returns the type summary for this class
	 *
	 *@return    the type summary
	 */
	public Summary getSourceSummary() {
		return type;
	}


	/**
	 *  Get the name of the package
	 *
	 *@return    the package name
	 */
	private String getPackageName() {
		Summary current = type;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}

		return ((PackageSummary) current).getName();
	}


	/**
	 *  Count the number of attributes
	 *
	 *@return    the number of attributes
	 */
	private int getAttributeCount() {
		int result = 0;

		Component[] children = getComponents();
		int last = children.length;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLField) {
				result++;
			}
		}

		return result;
	}


	/**
	 *  Draws the frame
	 *
	 *@param  g  the graphics object
	 *@param  x  the x coordinate
	 *@param  y  the y coordinate
	 */
	private void drawFrame(Graphics g, int x, int y) {
		g.setColor(UMLType.getFrameColor());
		Dimension size = getSize();
		double scalingFactor = getScale();

		//  Draw outer edge
		g.drawRect(x, y, size.width - 1, size.height - 1);
		g.drawRect(x + 1, y + 1, size.width - 3, size.height - 3);

		//  Separate name from field
		g.drawLine(x, (int) (y + scalingFactor * (titleHeight + 4)),
				x + size.width - 1, (int) (y + scalingFactor * (titleHeight + 4)));
		g.drawLine(x, (int) (y + scalingFactor * (titleHeight + 5)),
				x + size.width - 1, (int) (y + scalingFactor * (titleHeight + 5)));

		//  Separate field from methods
		int high = (int) (scalingFactor * (titleHeight + 4 + lineSize * Math.max(1, getAttributeCount())));
		g.drawLine(x, y + high, x + size.width - 1, y + high);
		g.drawLine(x, y + high + 1, x + size.width - 1, y + high + 1);

		//  Check if there are any nested types - if so draw their frame
		int typeCount = type.getTypeCount();
		if (typeCount > 0) {
			int previousLabels = Math.max(1, getAttributeCount()) + Math.max(1, type.getMethodCount());
			high = (int) (scalingFactor * (titleHeight + 4 + lineSize * previousLabels));
			g.drawLine(x, y + high, x + size.width - 1, y + high);
			g.drawLine(x, y + high + 1, x + size.width - 1, y + high + 1);
		}
	}


	/**
	 *  Compute the line size
	 *
	 *@return    Description of the Returned Value
	 */
	private int computeLineSize() {
		LabelSizeComputation lsc = LabelSizeComputation.get();
		int height = lsc.computeHeight("Test", UMLLine.defaultFont);
		return height + 2 * UMLLine.labelMargin;
	}


	/**
	 *  Return the frame color
	 *
	 *@return    the frame color
	 */
	private static Color getFrameColor() {
		return Color.black;
	}


	/**
	 *  Initializes the background colors for the various classes
	 */
	private static synchronized void initColors() {
		if (defaultBG == null) {
			defaultBG = Color.white;
			selectedBG = new Color(250, 255, 220);
			foreignBG = new Color(200, 200, 255);
			selectedForeignBG = new Color(220, 255, 220);
		}
	}
}
