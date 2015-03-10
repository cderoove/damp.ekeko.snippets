/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.ast;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.acm.seguin.parser.JavaParser;
import org.acm.seguin.parser.JavaParserVisitor;

/**
 *  Stores a name. The name can consist of a number of parts separated by
 *  periods.
 *
 *@author     Chris Seguin
 *@created    October 13, 1999
 */
public class ASTName extends SimpleNode implements Cloneable {
	//  Instance Variables
	private Vector name;


	/**
	 *  Constructor for the ASTName object
	 *
	 *@param  id  Description of Parameter
	 */
	public ASTName(int id)
	{
		super(id);
		name = new Vector();
	}


	/**
	 *  Constructor for the ASTName object
	 *
	 *@param  p   Description of Parameter
	 *@param  id  Description of Parameter
	 */
	public ASTName(JavaParser p, int id)
	{
		super(p, id);
		name = new Vector();
	}


	/**
	 *  Add a component of the name
	 *
	 *@param  ndx    the index of the part requested
	 *@param  value  The new NamePart value
	 */
	public void setNamePart(int ndx, String value)
	{
		name.setElementAt(value, ndx);
	}


	/**
	 *  Add a component of the name
	 *
	 *@param  ndx  the index of the part requested
	 *@return      the portion of the name requested
	 */
	public String getNamePart(int ndx)
	{
		if ((ndx >= 0) && (ndx < name.size())) {
			return (String) name.elementAt(ndx);
		}

		return null;
	}


	/**
	 *  Get the object's name
	 *
	 *@return    the name
	 */
	public String getName()
	{
		//  Local Variables
		StringBuffer buf = new StringBuffer();
		Enumeration enum_ = name.elements();
		boolean first = true;

		//  Iterate through the parts
		while (enum_.hasMoreElements()) {
			if (!first) {
				buf.append(".");
			}
			buf.append((String) enum_.nextElement());
			first = false;
		}

		//  Return the buffer
		return buf.toString();
	}


	/**
	 *  Get the length of the name
	 *
	 *@return    the number of parts in the name
	 */
	public int getNameSize()
	{
		return name.size();
	}


	/**
	 *  Add a component of the name
	 *
	 *@param  ndx    the index of the part requested
	 *@param  value  Description of Parameter
	 */
	public void insertNamePart(int ndx, String value)
	{
		name.insertElementAt(value, ndx);
	}


	/**
	 *  Set the object's name
	 *
	 *@param  newName  the new name
	 */
	public void addNamePart(String newName)
	{
		name.addElement(newName.intern());
	}


	/**
	 *  Convert this object to a string
	 *
	 *@return    a string representing this object
	 */
	public String toString()
	{
		return super.toString() + " [" + getName() + "]";
	}


	/**
	 *  Convert this object from a string
	 *
	 *@param  input  Description of Parameter
	 */
	public void fromString(String input)
	{
		//  Clean the old one
		name.removeAllElements();

		//  Load it
		StringTokenizer tok = new StringTokenizer(input, ".");
		while (tok.hasMoreTokens()) {
			String next = tok.nextToken();
			name.addElement(next);
		}
	}


	/**
	 *  Checks to see if the two names are equal
	 *
	 *@param  other  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	public boolean equals(Object other)
	{
		if (other == this) {
			return true;
		}
		if (other instanceof ASTName) {
			ASTName otherName = (ASTName) other;

			if (otherName.getNameSize() == getNameSize()) {
				return startsWith(otherName);
			}
		}
		return false;
	}


	/**
	 *  Determines if two names start with the same series of items
	 *
	 *@param  otherName  Description of Parameter
	 *@return            Description of the Returned Value
	 */
	public boolean startsWith(ASTName otherName)
	{
		//  To start with the other name, the other name must be less than or equal in parts
		if (otherName.getNameSize() > getNameSize()) {
			return false;
		}

		//  Look for the point where they are different
		int last = Math.min(otherName.getNameSize(), getNameSize());
		for (int ndx = 0; ndx < last; ndx++) {
			if (!getNamePart(ndx).equals(otherName.getNamePart(ndx))) {
				return false;
			}
		}

		//  They must be the same
		return true;
	}


	/**
	 *  Change starting part. Presumes that otherName is less than the length of
	 *  the current name.
	 *
	 *@param  oldBase  Description of Parameter
	 *@param  newBase  Description of Parameter
	 *@return          Description of the Returned Value
	 */
	public ASTName changeStartingPart(ASTName oldBase, ASTName newBase)
	{
		ASTName result = new ASTName(0);

		int last = newBase.getNameSize();
		for (int ndx = 0; ndx < last; ndx++) {
			result.addNamePart(newBase.getNamePart(ndx));
		}

		int end = getNameSize();
		int start = oldBase.getNameSize();
		for (int ndx = start; ndx < end; ndx++) {
			result.addNamePart(getNamePart(ndx));
		}

		return result;
	}


	/**
	 *  Create a copy
	 *
	 *@return    Description of the Returned Value
	 */
	public Object clone()
	{
		ASTName result = new ASTName(0);

		int last = getNameSize();
		for (int ndx = 0; ndx < last; ndx++) {
			result.addNamePart(getNamePart(ndx));
		}

		return result;
	}



	/**
	 *  Accept the visitor.
	 *
	 *@param  visitor  Description of Parameter
	 *@param  data     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	public Object jjtAccept(JavaParserVisitor visitor, Object data)
	{
		return visitor.visit(this, data);
	}
}
