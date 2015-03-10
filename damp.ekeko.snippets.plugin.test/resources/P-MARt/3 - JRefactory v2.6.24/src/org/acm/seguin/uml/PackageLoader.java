/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.uml;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.ide.common.SummaryLoaderThread;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.uml.line.AssociationRelationship;
import org.acm.seguin.uml.line.ImplementsRelationship;
import org.acm.seguin.uml.line.InheretenceRelationship;
import org.acm.seguin.uml.line.SegmentedLine;

/**
 *  Loads a UMLPackage panel from a package summary
 *
 *@author    Chris Seguin
 */
class PackageLoader extends Thread {
	private UMLPackage packagePanel;
	private int defaultX;
	private int defaultY;
	private boolean loaded;
	private String filename;

	private PackageSummary loadSummary;
	private File loadFile;
	private InputStream loadStream;


	/**
	 *  Constructor for the PackageLoader object
	 *
	 *@param  panel  the panel that we are loading
	 */
	public PackageLoader(UMLPackage panel) {
		packagePanel = panel;
		defaultX = 20;
		defaultY = 20;
		loaded = false;
	}


	/**
	 *  Main processing method for the PackageLoader object
	 */
	public void run() {
		/*
		 * Don't run this until we have completed loading the
		 * summaries from disk
		 */
		SummaryLoaderThread.waitForLoading();

		synchronized (PackageLoader.class) {
			packagePanel.setLoading(true);
			packagePanel.clear();

			if (loadSummary != null) {
				load(loadSummary);
			}
			if (loadFile != null) {
				load(loadFile);
			}
			if (loadStream != null) {
				load(loadStream);
			}

			packagePanel.updateClassListPanel();
			packagePanel.setLoading(false);
			packagePanel.repaint();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  summary  Description of Parameter
	 */
	public void start(PackageSummary summary) {
		loadSummary = summary;
		loadStream = null;
		loadFile = null;

		super.start();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  filename  Description of Parameter
	 */
	public void start(String filename) {
		loadSummary = null;
		loadStream = null;
		loadFile = new File(filename);

		super.start();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  input  Description of Parameter
	 */
	public void start(InputStream input) {
		loadSummary = null;
		loadStream = input;
		loadFile = null;

		super.start();
	}


	/**
	 *  Gets the File attribute of the PackageLoader object
	 *
	 *@return    The File value
	 */
	File getFile() {
		PackageSummary summary = packagePanel.getSummary();

		File dir = summary.getDirectory();
		File inputFile;
		if (dir == null) {
			dir = new File(System.getProperty("user.home") +
					File.separator + ".Refactory" +
					File.separator + "UML");
			dir.mkdirs();
			inputFile = new File(dir, summary.getName() + ".uml");
		}
		else {
			inputFile = new File(summary.getDirectory(), "package.uml");
		}

		return inputFile;
	}


	/**
	 *  Returns the UMLType panel associated with a summary or null if none is
	 *  available
	 *
	 *@param  summary  the type declaration
	 *@return          the panel associated with the type
	 */
	private UMLType getUMLType(TypeDeclSummary summary) {
		if (summary != null) {
			TypeSummary typeSummary = GetTypeSummary.query(summary);
			if (typeSummary != null) {
				UMLType typePanel = packagePanel.findType(typeSummary);
				if (typePanel == null) {
					typePanel = addType(typeSummary, true);
				}
				return typePanel;
			}
		}

		return null;
	}


	/**
	 *  Returns the location of this class
	 *
	 *@param  umlType  the type panel
	 *@param  summary  the type summary
	 *@return          the point where this type panel should be located
	 */
	private Point getLocation(UMLType umlType, TypeSummary summary) {
		Dimension dim = umlType.getPreferredSize();
		Point result = new Point(defaultX, defaultY);
		defaultX += (20 + dim.width);
		return result;
	}


	/**
	 *  Reload the summaries
	 *
	 *@param  summary  the package summary
	 */
	private void load(PackageSummary summary) {
		if (summary != null) {
			defaultPositions(summary);
		}

		loadPositions(getFile());
	}


	/**
	 *  Reload the summaries
	 *
	 *@param  file  Description of Parameter
	 */
	private void load(File file) {
		loadPositions(file);
	}


	/**
	 *  Reload the summaries
	 *
	 *@param  input  Description of Parameter
	 */
	private void load(InputStream input) {
		loadPositions(input);
	}


	/**
	 *  Loads all the classes into their default positions
	 *
	 *@param  summary  the package that is being loaded
	 */
	private void defaultPositions(PackageSummary summary) {
		//  Add all the types
		Iterator iter = summary.getFileSummaries();
		if (iter != null) {
			while (iter.hasNext()) {
				addFile((FileSummary) iter.next());
			}
		}

		loadInheretence();
		loadImplements();

		loaded = true;
	}


	/**
	 *  Adds all the types in a file
	 *
	 *@param  fileSummary  the file summary
	 */
	private void addFile(FileSummary fileSummary) {
		Iterator iter = fileSummary.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				addType((TypeSummary) iter.next(), false);
			}
		}
	}


	/**
	 *  Adds a UML type
	 *
	 *@param  typeSummary  the type summary being added
	 *@param  foreign      whether this type is in this package (true means it is
	 *      from a different package
	 *@return              The panel
	 */
	private UMLType addType(TypeSummary typeSummary, boolean foreign) {
		UMLType umlType = new UMLType(packagePanel, typeSummary, foreign);
		packagePanel.add(umlType);
		umlType.setLocation(getLocation(umlType, typeSummary));

		return umlType;
	}


	/**
	 *  Loads the inheritence relationships
	 */
	private void loadInheretence() {
		UMLType[] typeList = packagePanel.getTypes();

		for (int ndx = 0; ndx < typeList.length; ndx++) {
			TypeSummary current = typeList[ndx].getSummary();
			TypeDeclSummary parent = current.getParentClass();
			UMLType typePanel = getUMLType(parent);
			if (typePanel != null) {
				packagePanel.add(new InheretenceRelationship(typeList[ndx], typePanel));
			}
		}
	}


	/**
	 *  Loads the inheritence relationships
	 */
	private void loadImplements() {
		UMLType[] typeList = packagePanel.getTypes();

		for (int ndx = 0; ndx < typeList.length; ndx++) {
			if (typeList[ndx].isForeign()) {
				continue;
			}

			TypeSummary current = typeList[ndx].getSummary();
			Iterator iter = current.getImplementedInterfaces();
			if (iter != null) {
				while (iter.hasNext()) {
					TypeDeclSummary next = (TypeDeclSummary) iter.next();
					UMLType typePanel = getUMLType(next);
					if (typePanel != null) {
						SegmentedLine nextLine;
						if (current.isInterface()) {
							nextLine = new InheretenceRelationship(typeList[ndx], typePanel);
						}
						else {
							nextLine = new ImplementsRelationship(typeList[ndx], typePanel);
						}

						packagePanel.add(nextLine);
					}
				}
			}
		}
	}


	/**
	 *  Loads the package from disk
	 *
	 *@param  inputFile  Description of Parameter
	 */
	private void loadPositions(File inputFile) {
		try {
			FileReader fr = new FileReader(inputFile);
			BufferedReader input = new BufferedReader(fr);
			loadPositions(input);
		}
		catch (FileNotFoundException fnfe) {
			//  This is a normal and expected condition
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Loads the package from disk
	 *
	 *@param  inputStream  Description of Parameter
	 */
	private void loadPositions(InputStream inputStream) {
		try {
			InputStreamReader fr = new InputStreamReader(inputStream);
			BufferedReader input = new BufferedReader(fr);
			loadPositions(input);
			input.close();
			fr.close();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Loads the package from disk
	 *
	 *@param  input            Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	private void loadPositions(BufferedReader input) throws IOException {
		String line = input.readLine();
		while (line != null) {
			//  Decide what to do
			char ch = line.charAt(0);
			if (ch == 'P') {
				positionPanel(line);
			}
			else if (ch == 'S') {
				positionLine(line);
			}
			else if (ch == 'A') {
				positionAttribute(line);
			}
			else if (ch == 'V') {
				loadVersion(line);
			}

			//  Read the next line
			line = input.readLine();
		}
	}


	/**
	 *  position the type in the UMLPackage
	 *
	 *@param  buffer  the line that describes where to position the type
	 */
	private void positionPanel(String buffer) {
		StringTokenizer tok = new StringTokenizer(buffer, "[]{},\n");
		String code = tok.nextToken();
		String id = tok.nextToken();
		String x = tok.nextToken();
		String y = tok.nextToken();

		UMLType type = packagePanel.find(id);
		if (type == null) {
			return;
		}
		Point pt = type.getLocation();
		int nX = pt.x;
		int nY = pt.y;

		try {
			nX = Integer.parseInt(x);
			nY = Integer.parseInt(y);
		}
		catch (NumberFormatException nfe) {
		}

		type.setLocation(nX, nY);
	}


	/**
	 *  Position the line
	 *
	 *@param  buffer  the line read from the file
	 */
	private void positionLine(String buffer) {
		StringTokenizer tok = new StringTokenizer(buffer, "[]{}\n");
		String code = tok.nextToken();
		String pair = tok.nextToken();
		String position = tok.nextToken();

		tok = new StringTokenizer(pair, ",");
		String first = tok.nextToken();
		String second = tok.nextToken();

		SegmentedLine line = packagePanel.find(first, second);
		if (line != null) {
			line.load(position);
		}
	}


	/**
	 *  The attribute
	 *
	 *@param  buffer  the input string
	 */
	private void positionAttribute(String buffer) {
		StringTokenizer tok = new StringTokenizer(buffer, "[]{}\n");
		String code = tok.nextToken();
		String pair = tok.nextToken();
		String position = tok.nextToken();
		String fieldPosition = tok.nextToken();

		tok = new StringTokenizer(pair, ",");
		String first = tok.nextToken();
		String second = tok.nextToken();

		UMLType type = packagePanel.find(first);
		if (type == null) {
			return;
		}

		UMLField field = type.getField(second);
		if (field == null) {
			return;
		}

		field.setAssociation(true);
		AssociationRelationship ar = type.convertToAssociation(packagePanel, field);
		ar.load(position);

		//  Set the field label position
		tok = new StringTokenizer(fieldPosition, ",");
		String x = tok.nextToken();
		String y = tok.nextToken();
		try {
			field.setLocation(Integer.parseInt(x), Integer.parseInt(y));
		}
		catch (NumberFormatException nfe) {
		}
	}


	/**
	 *  Loads a version line
	 *
	 *@param  buffer  the input line that contains the version number and package
	 *      name
	 */
	private void loadVersion(String buffer) {
		StringTokenizer tok = new StringTokenizer(buffer, "[]:\n");
		String key = tok.nextToken();

		String versionID = tok.nextToken();
		String packageName = "";
		if (tok.hasMoreTokens()) {
			packageName = tok.nextToken();
		}

		System.out.println("Loading:  " + packageName + " from a file with version " + versionID);
		if (!loaded) {
			PackageSummary summary = PackageSummary.getPackageSummary(packageName);
			packagePanel.setSummary(summary);
			defaultPositions(summary);
		}
	}
}
