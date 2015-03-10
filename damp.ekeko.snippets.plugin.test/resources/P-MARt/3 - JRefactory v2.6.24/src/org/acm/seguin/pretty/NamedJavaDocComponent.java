/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.pretty;
/*
 * Copyright 1999
 *
 * Chris Seguin
 */


/**
 *  Store a portion of a javadoc item
 *
 *@author     Chris Seguin
 *@created    October 14, 1999
 *@date       April 15, 1999
 */
public class NamedJavaDocComponent extends JavaDocComponent {
	//  Instance Variable
	private String id;


	/**
	 *  Create an instance of this java doc object
	 */
	public NamedJavaDocComponent() {
		super();
		id = "";
	}


	/**
	 *  Set the id
	 *
	 *@param  newID  the new id
	 */
	public void setID(String newID) {
		if (newID != null) {
			id = newID;
			setLongestLength(getType().length() + id.length() + 2);
		}
	}


	/**
	 *  Return the id
	 *
	 *@return    the id
	 */
	public String getID() {
		return id;
	}


	/**
	 *  Print this tag
	 *
	 *@param  printData  printData
	 */
	public void print(PrintData printData) {
		//  We are now printing it
		setPrinted(true);

		//  Start the line
		printData.indent();
		printData.appendComment(" *", PrintData.JAVADOC_COMMENT);

		if (printData.isSpaceBeforeAt()) {
			printData.appendComment(" ", PrintData.JAVADOC_COMMENT);
		}

		//  Print the type
		printData.appendComment(getType(), PrintData.JAVADOC_COMMENT);
		for (int i = 0; i < printData.getJavadocIndent(); ++i) {
			printData.appendComment(" ", PrintData.JAVADOC_COMMENT);
		}

		//  Print the ID
		printData.appendComment(getID(), PrintData.JAVADOC_COMMENT);

		if (!printData.isJavadocLinedUp() && getID().length() > 0) {
			// Add a space after "param foo", "throws exc", etc.
			printData.appendComment(" ", PrintData.JAVADOC_COMMENT);
		}

		if (printData.isJavadocLinedUp()) {
			//  Pad extra spaces after the ID
			int extra = getLongestLength() - (getType().length() + getID().length());
			for (int ndx = 0; ndx < extra; ndx++) {
				printData.appendComment(" ", PrintData.JAVADOC_COMMENT);
			}
		}

		//  Print the description
		printDescription(printData);
		printData.newline();
	}
}
