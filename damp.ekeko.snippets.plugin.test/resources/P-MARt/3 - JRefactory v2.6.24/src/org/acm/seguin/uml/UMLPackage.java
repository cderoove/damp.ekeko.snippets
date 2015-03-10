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
import java.awt.print.PageFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.ide.common.ClassListPanel;
import org.acm.seguin.io.Saveable;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.uml.line.AssociationRelationship;
import org.acm.seguin.uml.line.LineMouseAdapter;
import org.acm.seguin.uml.line.LinedPanel;
import org.acm.seguin.uml.line.SegmentedLine;
import org.acm.seguin.uml.line.Vertex;
import org.acm.seguin.uml.print.UMLPagePrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Draws a UML diagram for all the classes in a package
 *
 *@author    Chris Seguin
 */
public class UMLPackage extends LinedPanel implements Saveable {
	//  Instance Variables
	private PackageSummary summary;
	private SegmentedLine currentLine = null;
	private boolean hasChanged;
	private String packageName;
	private JScrollPane scrollPane;
	private ClassListPanel classListPanel = null;
	private boolean first = false;
	private boolean loading = false;


	/**
	 *  Constructor for UMLPackage
	 *
	 *@param  packageSummary  the summary of the package
	 */
	public UMLPackage(PackageSummary packageSummary) {
		//  Initialize the instance variables
		defaultValues();
		setSummary(packageSummary);

		//  Don't use a layout manager
		setLayout(null);

		//  Load the summaries
		(new PackageLoader(this)).start(summary);

		//  Reset the size
		setSize(getPreferredSize());

		addMouseAdapter();
	}


	/**
	 *  Constructor for UMLPackage
	 *
	 *@param  filename  the name of the file
	 */
	public UMLPackage(String filename) {
		//  Initialize the instance variables
		defaultValues();

		//  Don't use a layout manager
		setLayout(null);

		//  Load the summaries
		(new PackageLoader(this)).start(filename);

		//  Reset the size
		setSize(getPreferredSize());

		addMouseAdapter();
	}


	/**
	 *  Constructor for UMLPackage
	 *
	 *@param  input  the input stream
	 */
	public UMLPackage(InputStream input) {
		//  Initialize the instance variables
		defaultValues();

		//  Don't use a layout manager
		setLayout(null);

		//  Load the summaries
		(new PackageLoader(this)).start(input);

		//  Reset the size
		setSize(getPreferredSize());

		addMouseAdapter();
	}


	/**
	 *  Sets the Dirty attribute of the UMLPackage object
	 */
	public void setDirty() {
		hasChanged = true;
	}


	/**
	 *  Sets the ScrollPane attribute of the UMLPackage object
	 *
	 *@param  value  The new ScrollPane value
	 */
	public void setScrollPane(JScrollPane value) {
		scrollPane = value;
	}


	/**
	 *  Sets the class list panel
	 *
	 *@param  value  the new list
	 */
	public void setClassListPanel(ClassListPanel value) {
		classListPanel = value;
		first = true;
	}


	/**
	 *  Sets the loading value
	 *
	 *@param  value  The new Loading value
	 */
	public void setLoading(boolean value) {
		loading = value;
	}


	/**
	 *  Gets the PackageName attribute of the UMLPackage object
	 *
	 *@return    The PackageName value
	 */
	public String getPackageName() {
		return packageName;
	}


	/**
	 *  Get the components that are UMLTypes
	 *
	 *@return    Description of the Returned Value
	 */
	public UMLType[] getTypes() {
		//  Instance Variables
		Component[] children = getComponents();
		int last = children.length;
		int count = 0;

		//  Count the UMLTypes
		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				count++;
			}
		}

		//  Count the UMLTypes
		UMLType[] results = new UMLType[count];
		int item = 0;
		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				results[item] = (UMLType) children[ndx];
				item++;
			}
		}

		//  Return the result
		return results;
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
		//  Initialize local variables
		int wide = 10;
		int high = 10;
		Component[] children = getComponents();
		int last = children.length;

		//  Deselect the children
		for (int ndx = 0; ndx < last; ndx++) {
			Rectangle bounds = children[ndx].getBounds();
			wide = Math.max(wide, 20 + bounds.x + bounds.width);
			high = Math.max(high, 20 + bounds.y + bounds.height);
		}

		return new Dimension(wide, high);
	}


	/**
	 *  Get the summary
	 *
	 *@return    The package summary
	 */
	public PackageSummary getSummary() {
		return summary;
	}


	/**
	 *  Gets the File attribute of the UMLPackage object
	 *
	 *@return    The File value
	 */
	public File getFile() {
		return (new PackageLoader(this)).getFile();
	}


	/**
	 *  Gets the Dirty attribute of the UMLPackage object
	 *
	 *@return    The Dirty value
	 */
	public boolean isDirty() {
		return hasChanged;
	}


	/**
	 *  Determines the title
	 *
	 *@return    the title
	 */
	public String getTitle() {
		return ("UML Diagram for " + packageName);
	}


	/**
	 *  Remove the association
	 *
	 *@param  field  Description of Parameter
	 */
	public void removeAssociation(UMLField field) {
		Iterator iter = getLines();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof AssociationRelationship) {
				AssociationRelationship assoc = (AssociationRelationship) next;
				if (assoc.getField().equals(field)) {
					assoc.delete();
					iter.remove();
					return;
				}
			}
		}
	}


	/**
	 *  Paint this object
	 *
	 *@param  g  the graphics object
	 */
	public void paint(Graphics g) {
		setBackground(Color.lightGray);
		g.setColor(Color.lightGray);
		Dimension size = getSize();
		g.fillRect(0, 0, size.width, size.height);

		//  Draw the grid
		PageFormat pf = UMLPagePrinter.getPageFormat(false);
		if (pf != null) {
			int pageHeight = (int) UMLPagePrinter.getPageHeight();
			int pageWidth = (int) UMLPagePrinter.getPageWidth();

			g.setColor(Color.gray);
			for (int x = pageWidth; x < size.width; x += pageWidth) {
				g.drawLine(x, 0, x, size.height);
			}

			for (int y = pageHeight; y < size.width; y += pageHeight) {
				g.drawLine(0, y, size.width, y);
			}
		}

		//  Abort once we are loading
		if (loading) {
			return;
		}

		//  Draw the segmented lines
		Iterator iter = getLines();
		while (iter.hasNext()) {
			((SegmentedLine) iter.next()).paint(g);
		}

		//  Draw the components
		paintChildren(g);
	}


	/**
	 *  Print this object
	 *
	 *@param  g  the graphics object
	 *@param  x  the x coordinate
	 *@param  y  the y coordinate
	 */
	public void print(Graphics g, int x, int y) {
		Component[] children = getComponents();
		int last = children.length;

		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				Point pt = children[ndx].getLocation();
				((UMLType) children[ndx]).print(g, x + pt.x, y + pt.y);
			}
			else if (children[ndx] instanceof UMLLine) {
				Point pt = children[ndx].getLocation();
				((UMLLine) children[ndx]).print(g, x + pt.x, y + pt.y);
			}
		}

		Iterator iter = getLines();
		while (iter.hasNext()) {
			((SegmentedLine) iter.next()).paint(g);
		}
	}


	/**
	 *  Reloads the UML class diagrams
	 */
	public void reload() {
		//  Save the image
		try {
			save();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}

		//  Reload it
		(new PackageLoader(this)).start(summary);

		//  Reset the size
		setSize(getPreferredSize());

		reset();

		//  Nothing has changed
		hasChanged = false;
	}


	/**
	 *  Description of the Method
	 */
	public void clear() {
		removeAll();
		super.clear();
	}


	/**
	 *  Determine what you hit
	 *
	 *@param  actual  The hit location
	 */
	public void hit(Point actual) {
		currentLine = null;
		Iterator iter = getLines();
		while ((currentLine == null) && iter.hasNext()) {
			SegmentedLine next = (SegmentedLine) iter.next();
			if (next.hit(actual)) {
				currentLine = next;
			}
		}

		while (iter.hasNext()) {
			SegmentedLine next = (SegmentedLine) iter.next();
			next.select(false);
		}

		repaint();
	}


	/**
	 *  Dragging a segmented line point
	 *
	 *@param  actual  The mouse's current location
	 */
	public void drag(Point actual) {
		if (currentLine != null) {
			currentLine.drag(actual);
			repaint();
		}
	}


	/**
	 *  User dropped an item
	 */
	public void drop() {
		if (currentLine != null) {
			currentLine.drop();

			hasChanged = true;

			currentLine = null;
		}

		reset();
	}


	/**
	 *  Save the files
	 *
	 *@exception  IOException  Description of Exception
	 */
	public void save() throws IOException {
		//  Make sure we have something that has changed
		if (!hasChanged) {
			return;
		}

		//  Local Variables
		Component[] children = getComponents();
		int last = children.length;

		File dir = summary.getDirectory();
		File outputFile;
		if (dir == null) {
			dir = new File(System.getProperty("user.home") +
					File.separator + ".Refactory" +
					File.separator + "UML");
			dir.mkdirs();
			outputFile = new File(dir +
					File.separator + summary.getName() + ".uml");
		}
		else {
			outputFile = new File(summary.getDirectory(), "package.uml");
		}
		PrintWriter output = new PrintWriter(
				new FileWriter(outputFile));

		output.println("V[1.1:" + summary.getName() + "]");

		//  Save the line segments
		Iterator iter = getLines();
		while (iter.hasNext()) {
			((SegmentedLine) iter.next()).save(output);
		}

		//  Save the types
		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				((UMLType) children[ndx]).save(output);
			}
		}

		output.close();

		//  Nothing has changed
		hasChanged = false;
	}


	/**
	 *  Tells the scrollbar to jump to this location
	 *
	 *@param  type  Description of Parameter
	 */
	public void jumpTo(TypeSummary type) {
		UMLType umlType = findType(type);
		if (umlType == null) {
			return;
		}

		Point pt = umlType.getLocation();

		JScrollBar horiz = scrollPane.getHorizontalScrollBar();
		horiz.setValue(pt.x - 10);
		JScrollBar vert = scrollPane.getVerticalScrollBar();
		vert.setValue(pt.y - 10);
	}


	/**
	 *  Find the type based on a summary
	 *
	 *@param  searching  the variable we are searching for
	 *@return            the UML type object
	 */
	protected UMLType findType(TypeSummary searching) {
		//  Instance Variables
		Component[] children = getComponents();
		int last = children.length;
		int count = 0;
		TypeSummary current;

		if (searching == null) {
			return null;
		}

		//  Count the UMLTypes
		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				current = ((UMLType) children[ndx]).getSummary();
				if (searching.equals(current)) {
					return (UMLType) children[ndx];
				}
			}
		}

		//  Not found
		return null;
	}


	/**
	 *  Find the type based on a id code
	 *
	 *@param  id  the code we are searching for
	 *@return     the UML type object
	 */
	protected UMLType find(String id) {
		//  Instance Variables
		Component[] children = getComponents();
		int last = children.length;
		int count = 0;
		String current;

		if (id == null) {
			return null;
		}

		//  Find the id that matches
		for (int ndx = 0; ndx < last; ndx++) {
			if (children[ndx] instanceof UMLType) {
				current = ((UMLType) children[ndx]).getID();
				if (id.equals(current)) {
					return (UMLType) children[ndx];
				}
			}
		}

		//  Not found
		return null;
	}


	/**
	 *  Find the type based on a id code
	 *
	 *@param  panel1  Description of Parameter
	 *@param  panel2  Description of Parameter
	 *@return         the UML type object
	 */
	protected SegmentedLine find(String panel1, String panel2) {
		UMLType first = find(panel1);
		UMLType second = find(panel2);

		if ((first == null) || (second == null)) {
			return null;
		}

		Iterator iter = getLines();
		while (iter.hasNext()) {
			SegmentedLine line = (SegmentedLine) iter.next();
			if (line.match(first, second)) {
				return line;
			}
		}

		return null;
	}


	/**
	 *  Sets the summary
	 *
	 *@param  value  The package summary
	 */
	void setSummary(PackageSummary value) {
		summary = value;
		if (summary != null)
			packageName = summary.getName();
	}


	/**
	 *  Tells the class list panel to laod itself
	 */
	void updateClassListPanel() {
		if (classListPanel == null) {
			return;
		}

		if (first) {
			first = false;
			return;
		}

		classListPanel.load(summary);
	}


	/**
	 *  Set up the default values
	 */
	private void defaultValues() {
		packageName = "Unknown Package";
		hasChanged = false;

		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			umlBundle.setContinuallyReload(true);
			Vertex.setVertexSize(umlBundle.getInteger("sticky.point.size"));
			Vertex.setNear(umlBundle.getDouble("halo.size"));
		}
		catch (MissingSettingsException mse) {
			Vertex.setNear(3.0);
			Vertex.setVertexSize(5);
		}
	}


	/**
	 *  Adds a feature to the MouseAdapter attribute of the UMLPackage object
	 */
	private void addMouseAdapter() {
		LineMouseAdapter adapter = new LineMouseAdapter(this);
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
	}


	/**
	 *  Resets the scroll panes
	 */
	private void reset() {
		if (scrollPane == null) {
			repaint();
		}
		else {
			Dimension panelSize = getPreferredSize();
			JViewport view = scrollPane.getViewport();
			Dimension viewSize = view.getSize();
			setSize(Math.max(panelSize.width, viewSize.width),
					Math.max(panelSize.height, viewSize.height));
			view.setViewSize(getSize());
			scrollPane.repaint();
		}
	}
}
