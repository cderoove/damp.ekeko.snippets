/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import java.io.Serializable;

/**
 *  Holds a description of the modifiers for a field or a class
 *
 *@author    Chris Seguin
 */
public class ModifierHolder implements Serializable {

	//  Instance Variables
	private int modifiers = 0;
	//  Class Variables
	/**
	 *  Description of the Field
	 */
	protected static int ABSTRACT = 0x0001;
	/**
	 *  Description of the Field
	 */
	protected static int EXPLICIT = 0x0002;
	/**
	 *  Description of the Field
	 */
	protected static int FINAL = 0x0004;
	/**
	 *  Description of the Field
	 */
	protected static int INTERFACE = 0x0008;
	/**
	 *  Description of the Field
	 */
	protected static int NATIVE = 0x0010;
	/**
	 *  Description of the Field
	 */
	protected static int PRIVATE = 0x0020;
	/**
	 *  Description of the Field
	 */
	protected static int PROTECTED = 0x0040;
	/**
	 *  Description of the Field
	 */
	protected static int PUBLIC = 0x0080;
	/**
	 *  Description of the Field
	 */
	protected static int STATIC = 0x0100;
	/**
	 *  Description of the Field
	 */
	protected static int STRICT = 0x0200;
	/**
	 *  Description of the Field
	 */
	protected static int SYNCHRONIZED = 0x0400;
	/**
	 *  Description of the Field
	 */
	protected static int TRANSIENT = 0x0800;
	/**
	 *  Description of the Field
	 */
	protected static int VOLATILE = 0x1000;
	/**
	 *  Description of the Field
	 */
	protected static int STRICTFP = 0x2000;

	/**
	 *  Description of the Field
	 */
	protected static String[] names =
			{
			"abstract",
			"explicit",
			"final",
			"interface",
			"native",
			"private",
			"protected",
			"public",
			"static",
			"strict",
			"strictfp",
			"synchronized",
			"transient",
			"volatile"
			};


	/**
	 *  Sets the private bit in the modifiers
	 *
	 *@param  value  true if we are setting the private modifier
	 */
	public void setPrivate(boolean value)
	{
		setCode(value, PRIVATE);
	}


	/**
	 *  Sets the protected bit in the modifiers
	 *
	 *@param  value  true if we are setting the protected modifier
	 */
	public void setProtected(boolean value)
	{
		setCode(value, PROTECTED);
	}


	/**
	 *  Sets the public bit in the modifiers
	 *
	 *@param  value  true if we are setting the public modifier
	 */
	public void setPublic(boolean value)
	{
		setCode(value, PUBLIC);
	}


	/**
	 *  Sets the abstract bit in the modifiers
	 *
	 *@param  value  true if we are setting the modifier
	 */
	public void setAbstract(boolean value)
	{
		setCode(value, ABSTRACT);
	}


	/**
	 *  Sets the Synchronized attribute of the ModifierHolder object
	 *
	 *@param  value  The new Synchronized value
	 */
	public void setSynchronized(boolean value)
	{
		setCode(value, SYNCHRONIZED);
	}


	/**
	 *  Sets the Static attribute of the ModifierHolder object
	 *
	 *@param  value  The new Static value
	 */
	public void setStatic(boolean value)
	{
		setCode(value, STATIC);
	}


	/**
	 *  Determine if the object is abstract
	 *
	 *@return    true if this stores an ABSTRACT flag
	 */
	public boolean isAbstract()
	{
		return ((modifiers & ABSTRACT) != 0);
	}


	/**
	 *  Determine if the object is explicit
	 *
	 *@return    true if this stores an EXPLICIT flag
	 */
	public boolean isExplicit()
	{
		return ((modifiers & EXPLICIT) != 0);
	}


	/**
	 *  Determine if the object is final
	 *
	 *@return    true if this stores an FINAL flag
	 */
	public boolean isFinal()
	{
		return ((modifiers & FINAL) != 0);
	}


	/**
	 *  Determine if the object is interface
	 *
	 *@return    true if this stores an INTERFACE flag
	 */
	public boolean isInterface()
	{
		return ((modifiers & INTERFACE) != 0);
	}


	/**
	 *  Determine if the object is native
	 *
	 *@return    true if this stores an NATIVE flag
	 */
	public boolean isNative()
	{
		return ((modifiers & NATIVE) != 0);
	}


	/**
	 *  Determine if the object is private
	 *
	 *@return    true if this stores an PRIVATE flag
	 */
	public boolean isPrivate()
	{
		return ((modifiers & PRIVATE) != 0);
	}


	/**
	 *  Determine if the object is protected
	 *
	 *@return    true if this stores an PROTECTED flag
	 */
	public boolean isProtected()
	{
		return ((modifiers & PROTECTED) != 0);
	}


	/**
	 *  Determine if the object is public
	 *
	 *@return    true if this stores an PUBLIC flag
	 */
	public boolean isPublic()
	{
		return ((modifiers & PUBLIC) != 0);
	}


	/**
	 *  Determine if the object is static
	 *
	 *@return    true if this stores an static flag
	 */
	public boolean isStatic()
	{
		return ((modifiers & STATIC) != 0);
	}


	/**
	 *  Determine if the object is strict
	 *
	 *@return    true if this stores an STRICT flag
	 */
	public boolean isStrict()
	{
		return ((modifiers & STRICT) != 0);
	}


	/**
	 *  Determine if the object is strictFP
	 *
	 *@return    true if this stores an STRICTFP flag
	 */
	public boolean isStrictFP()
	{
		return ((modifiers & STRICTFP) != 0);
	}


	/**
	 *  Determine if the object is synchronized
	 *
	 *@return    true if this stores an SYNCHRONIZED flag
	 */
	public boolean isSynchronized()
	{
		return ((modifiers & SYNCHRONIZED) != 0);
	}


	/**
	 *  Determine if the object is transient
	 *
	 *@return    true if this stores an TRANSIENT flag
	 */
	public boolean isTransient()
	{
		return ((modifiers & TRANSIENT) != 0);
	}


	/**
	 *  Determine if the object is volatile
	 *
	 *@return    true if this stores an VOLATILE flag
	 */
	public boolean isVolatile()
	{
		return ((modifiers & VOLATILE) != 0);
	}


	/**
	 *  Determines if this has package scope
	 *
	 *@return    true if this has package scope
	 */
	public boolean isPackage()
	{
		return !isPublic() && !isProtected() && !isPrivate();
	}


	/**
	 *  Add a modifier
	 *
	 *@param  mod  the new modifier
	 */
	public void add(String mod)
	{
		if ((mod == null) || (mod.length() == 0)) {
			//  Nothing to add
			return;
		}
		else if (mod.equalsIgnoreCase(names[0])) {
			modifiers = modifiers | ABSTRACT;
		}
		else if (mod.equalsIgnoreCase(names[1])) {
			modifiers = modifiers | EXPLICIT;
		}
		else if (mod.equalsIgnoreCase(names[2])) {
			modifiers = modifiers | FINAL;
		}
		else if (mod.equalsIgnoreCase(names[3])) {
			modifiers = modifiers | INTERFACE;
		}
		else if (mod.equalsIgnoreCase(names[4])) {
			modifiers = modifiers | NATIVE;
		}
		else if (mod.equalsIgnoreCase(names[5])) {
			modifiers = modifiers | PRIVATE;
		}
		else if (mod.equalsIgnoreCase(names[6])) {
			modifiers = modifiers | PROTECTED;
		}
		else if (mod.equalsIgnoreCase(names[7])) {
			modifiers = modifiers | PUBLIC;
		}
		else if (mod.equalsIgnoreCase(names[8])) {
			modifiers = modifiers | STATIC;
		}
		else if (mod.equalsIgnoreCase(names[9])) {
			modifiers = modifiers | STRICT;
		}
		else if (mod.equalsIgnoreCase(names[10])) {
			modifiers = modifiers | STRICTFP;
		}
		else if (mod.equalsIgnoreCase(names[11])) {
			modifiers = modifiers | SYNCHRONIZED;
		}
		else if (mod.equalsIgnoreCase(names[12])) {
			modifiers = modifiers | TRANSIENT;
		}
		else if (mod.equalsIgnoreCase(names[13])) {
			modifiers = modifiers | VOLATILE;
		}
	}


	/**
	 *  Convert the object to a string
	 *
	 *@return    a string describing the modifiers
	 */
	public String toString()
	{
		//  Local Variables
		StringBuffer buf = new StringBuffer();

		//  Protection first
		if (isPrivate()) {
			buf.append(names[5]);
			buf.append(" ");
		}
		if (isProtected()) {
			buf.append(names[6]);
			buf.append(" ");
		}
		if (isPublic()) {
			buf.append(names[7]);
			buf.append(" ");
		}

		//  Others next
		if (isAbstract()) {
			buf.append(names[0]);
			buf.append(" ");
		}
		if (isExplicit()) {
			buf.append(names[1]);
			buf.append(" ");
		}
		if (isFinal()) {
			buf.append(names[2]);
			buf.append(" ");
		}
		if (isInterface()) {
			buf.append(names[3]);
			buf.append(" ");
		}
		if (isNative()) {
			buf.append(names[4]);
			buf.append(" ");
		}
		if (isStatic()) {
			buf.append(names[8]);
			buf.append(" ");
		}
		if (isStrict()) {
			buf.append(names[9]);
			buf.append(" ");
		}
		if (isStrictFP()) {
			buf.append(names[10]);
			buf.append(" ");
		}
		if (isSynchronized()) {
			buf.append(names[11]);
			buf.append(" ");
		}
		if (isTransient()) {
			buf.append(names[12]);
			buf.append(" ");
		}
		if (isVolatile()) {
			buf.append(names[13]);
			buf.append(" ");
		}

		return buf.toString();
	}


	/**
	 *  Copies the modifiers from another source
	 *
	 *@param  source  the source
	 */
	public void copy(ModifierHolder source)
	{
		modifiers = source.modifiers;
	}


	/**
	 *  Compare two of these objects and get it right
	 *
	 *@param  obj  the object
	 *@return      true if they are the same
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof ModifierHolder) {
			ModifierHolder other = (ModifierHolder) obj;
			return other.modifiers == modifiers;
		}
		return false;
	}


	/**
	 *  You need to overload the hashcode when you overload the equals operator
	 *
	 *@return    a hashcode for this object
	 */
	public int hashCode()
	{
		return modifiers;
	}


	/**
	 *  Sets or resets a single bit in the modifiers
	 *
	 *@param  value  true if we are setting the bit
	 *@param  code   The new Code value
	 */
	protected void setCode(boolean value, int code)
	{
		if (value) {
			modifiers = modifiers | code;
		}
		else {
			modifiers = modifiers & (~code);
		}
	}
}
