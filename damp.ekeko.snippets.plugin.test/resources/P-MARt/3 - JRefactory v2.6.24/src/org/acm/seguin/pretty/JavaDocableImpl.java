/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.util.StringTokenizer;
import java.util.Vector;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Stores the java doc components
 *
 *@author    Chris Seguin
 *@date      April 15, 1999
 */
public class JavaDocableImpl implements JavaDocable {
	//  Instance Variables
	private Vector docs;
	private boolean printed;


	/**
	 *  Constructor
	 */
	public JavaDocableImpl()
	{
		docs = new Vector();
		printed = false;
	}


	/**
	 *  Checks to see if it was printed
	 *
	 *@return    true if it still needs to be printed
	 */
	public boolean isRequired()
	{
		return !printed;
	}


	/**
	 *  Determines if the javadoc comments were printed
	 *
	 *@return    The Printed value
	 */
	public boolean isPrinted()
	{
		return printed && (docs.size() > 0);
	}


	/**
	 *  Allows you to add a java doc component
	 *
	 *@param  component  the component that can be added
	 */
	public void addJavaDocComponent(JavaDocComponent component)
	{
		if (component != null) {
			docs.addElement(component);
		}
	}


	/**
	 *  Prints all the java doc components
	 *
	 *@param  printData  the print data
	 */
	public void printJavaDocComponents(PrintData printData)
	{
		printJavaDocComponents(printData, "");
	}


	/**
	 *  Prints all the java doc components
	 *
	 *@param  printData  the print data
	 *@param  order      the order for the tags
	 */
	public void printJavaDocComponents(PrintData printData, String order)
	{
		//  Saying that we are done
		printed = true;

		//  Abort if there is nothing
		if (docs.size() == 0) {
			return;
		}

		//  Adjust the spacing for the IDs
		setLongest(getLongest());

		//  Make sure we are indented
		if (!printData.isLineIndented()) {
			printData.indent();
		}

		//  Start the comment
		int loop = printData.getJavadocStarCount() - 2;
		printData.appendComment("/**", PrintData.JAVADOC_COMMENT);
		for (int ndx = 0; ndx < loop; ndx++) {
			printData.appendComment("*", PrintData.JAVADOC_COMMENT);
		}
		boolean onSingleLine = isOnSingleLine(printData);
		if (!onSingleLine) {
			printData.newline();
		}

		//  Print all the components
		print(printData, order, onSingleLine);

		//  Finish the comment
		if (!onSingleLine) {
			printData.indent();
		}
		printData.appendComment(" ", PrintData.JAVADOC_COMMENT);
		for (int ndx = 0; ndx < loop; ndx++) {
			printData.appendComment("*", PrintData.JAVADOC_COMMENT);
		}
		printData.appendComment("*/", PrintData.JAVADOC_COMMENT);

		//  Newline
		printData.newline();
	}


	/**
	 *  Makes sure all the java doc components are present
	 */
	public void finish() { }


	/**
	 *  Contains a particular item
	 *
	 *@param  type  the type we are looking for
	 *@return       Description of the Returned Value
	 */
	public boolean contains(String type)
	{
		//  Local Variables
		int last = docs.size();
		boolean found = false;

		//  Iterate through the components
		for (int ndx = 0; ndx < last; ndx++) {
			JavaDocComponent jdc = ((JavaDocComponent) docs.elementAt(ndx));
			String typeName = jdc.getType();
			if (typeName.equalsIgnoreCase(type)) {
				jdc.setRequired(true);
				found = true;

				if (typeName.equals("param") || typeName.equals("return") ||
						typeName.equals("exception") || typeName.equals("throws") ||
						typeName.equals("")) {
					return true;
				}
			}
		}

		//  Not found
		return found;
	}


	/**
	 *  Contains a particular item
	 *
	 *@param  type  the type we are looking for
	 *@param  id    the id
	 *@return       Description of the Returned Value
	 */
	public boolean contains(String type, String id)
	{
		//  Local Variables
		int last = docs.size();

		//  Iterate through the components
		for (int ndx = 0; ndx < last; ndx++) {
			Object next = docs.elementAt(ndx);
			if (next instanceof NamedJavaDocComponent) {
				NamedJavaDocComponent jdc = (NamedJavaDocComponent) next;
				if ((jdc.getType().equalsIgnoreCase(type)) &&
						(jdc.getID().equals(id))) {
					jdc.setRequired(true);
					return true;
				}
			}
		}

		//  Not found
		return false;
	}


	/**
	 *  Make a required field
	 *
	 *@param  tag    the tag
	 *@param  descr  the default description
	 */
	public void require(String tag, String descr)
	{
		if (!contains(tag)) {
			JavaDocComponent jdc = new JavaDocComponent();
			jdc.setType(tag);
			jdc.setDescription(descr);
			addJavaDocComponent(jdc);
			jdc.setRequired(true);
		}
	}


	/**
	 *  Make a required field
	 *
	 *@param  tag    the tag
	 *@param  id     the id
	 *@param  descr  the default description
	 */
	public void require(String tag, String id, String descr)
	{
		if (!contains(tag, id)) {
			NamedJavaDocComponent jdc = new NamedJavaDocComponent();
			jdc.setType(tag);
			jdc.setID(id);
			jdc.setDescription(descr);
			addJavaDocComponent(jdc);
			jdc.setRequired(true);
		}
	}


	/**
	 *  Set the longest id
	 *
	 *@param  length  the longest length
	 */
	private void setLongest(int length)
	{
		int last = docs.size();
		for (int ndx = 0; ndx < last; ndx++) {
			((JavaDocComponent) docs.elementAt(ndx)).setLongestLength(length);
		}
	}


	/**
	 *  Determine the maximum length
	 *
	 *@return    the maximum length
	 */
	private int getLongest()
	{
		int longest = 0;
		int last = docs.size();
		for (int ndx = 0; ndx < last; ndx++) {
			int next = ((JavaDocComponent) docs.elementAt(ndx)).getLongestLength();
			longest = Math.max(next, longest);
		}
		return longest;
	}


	/**
	 *  Determines if a particular component is a description
	 *
	 *@param  current  the current tag
	 *@return          true if it is a description
	 */
	private boolean isDescription(JavaDocComponent current)
	{
		return current.getType().length() == 0;
	}


	/**
	 *  Gets the TagRequired attribute of the JavaDocableImpl object
	 *
	 *@param  tag  Description of Parameter
	 *@return      The TagRequired value
	 */
	private boolean isTagRequired(String tag)
	{
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
			bundle.getString(tag + ".descr");
			return true;
		}
		catch (MissingSettingsException mse) {
			return false;
		}
	}


	/**
	 *  Determines if the javadoc will fit on a single line
	 *
	 *@param  printData  the print data
	 *@return            true if it can fit (and is allowed to fit) on a single
	 *      line
	 */
	private boolean isOnSingleLine(PrintData printData)
	{
		if (!printData.isAllowSingleLineJavadoc()) {
			return false;
		}

		if (docs.size() > 1) {
			return false;
		}

		JavaDocComponent single = (JavaDocComponent) docs.elementAt(0);
		if (!single.isDescription()) {
			return false;
		}

		String text = single.getDescription();
		if (text.length() > printData.getJavadocWordWrapMaximum()) {
			return false;
		}

		text = text.toUpperCase();
		if ((text.indexOf("<P>") >= 0) ||
				(text.indexOf("<BR") >= 0) ||
				(text.indexOf("<OL") >= 0) ||
				(text.indexOf("<UL") >= 0)) {
			return false;
		}

		return true;
	}


	/**
	 *  Actually prints the components
	 *
	 *@param  printData  the print data
	 *@param  order      the order that stuff should be printed in
	 */
	private void print(PrintData printData, String order, boolean singleLine)
	{
		StringTokenizer tok = new StringTokenizer(order, ", \t\r\n");

		//  First print the description
		printDescription(printData, singleLine);

		//  Now order the tags we know about
		while (tok.hasMoreTokens()) {
			String next = tok.nextToken();

			boolean isRequired = isTagRequired(next);
			tagPass("@" + next, printData, isRequired);
		}

		//  Now print everything else
		finalPass(printData);
	}


	/**
	 *  Prints all the components that have a particular tag
	 *
	 *@param  next       the component
	 *@param  printData  the print data
	 *@param  req        Description of Parameter
	 */
	private void tagPass(String next, PrintData printData, boolean req)
	{
		JavaDocComponent current;
		int last = docs.size();

		for (int ndx = 0; ndx < last; ndx++) {
			//  Get the next element
			current = ((JavaDocComponent) docs.elementAt(ndx));

			//  Does it go here
			boolean now = next.equals(current.getType());
			boolean required = !req || current.isRequired();

			//  If it is time, print it
			if (now) {
				if (required) {
					printCurrentTag(current, printData, false, false);
				}
				else {
					current.setPrinted(true);
				}
			}
		}
	}


	/**
	 *  Prints the description
	 *
	 *@param  printData  the print data
	 */
	private void printDescription(PrintData printData, boolean singleLine)
	{
		JavaDocComponent current;
		int last = docs.size();

		for (int ndx = 0; ndx < last; ndx++) {
			//  Get the next element
			current = ((JavaDocComponent) docs.elementAt(ndx));

			if (current.getType().equals("")) {
				String descr = current.getDescription();
				if (JavadocTokenizer.hasContent(descr)) {
					printCurrentTag(current, printData, last == 1, singleLine);
				}
				current.setPrinted(true);
			}
		}
	}


	/**
	 *  Prints all unknown tags
	 *
	 *@param  printData  the print data
	 */
	private void finalPass(PrintData printData)
	{
		JavaDocComponent current;
		int last = docs.size();

		for (int ndx = 0; ndx < last; ndx++) {
			//  Get the next element
			current = ((JavaDocComponent) docs.elementAt(ndx));

			//  If it is time, print it
			if (!current.isPrinted()) {
				printCurrentTag(current, printData, false, false);
			}
		}
	}


	/**
	 *  Prints the current tag
	 *
	 *@param  current          the tag
	 *@param  printData        the print data
	 *@param  onlyDescription  if it is the only tag
	 */
	private void printCurrentTag(JavaDocComponent current, PrintData printData, boolean onlyDescription, boolean singleLine)
	{
		//  Print the element
		printData.setCurrentIsSingle(singleLine);
		current.print(printData);
		printData.setCurrentIsSingle(false);
		if (!onlyDescription && isDescription(current)) {
			printSpaceAfterDescription(printData);
		}
	}


	/**
	 *  Prints a blank line after a description
	 *
	 *@param  printData  the print data
	 */
	private void printSpaceAfterDescription(PrintData printData)
	{
		printData.indent();
		printData.appendComment(" *", PrintData.JAVADOC_COMMENT);
		printData.newline();
	}
}
