/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

/**
 *  Stores the size of a field
 *
 *@author    Chris Seguin
 */
public class FieldSize
{
	private int modifierLength = 0;
	private int typeLength = 0;
	private int nameLength = 0;
	private int equalsLength = 0;


	/**
	 *  Constructor for the FieldSize object
	 */
	public FieldSize()
	{
	}


	/**
	 *  Sets the ModifierLength attribute of the FieldSizeFieldSize object
	 *
	 *@param  value  The new ModifierLength value
	 */
	public void setModifierLength(int value)
	{
		if (value > modifierLength)
		{
			modifierLength = value;
		}
	}


	/**
	 *  Sets the TypeLength attribute of the FieldSizeFieldSize object
	 *
	 *@param  value  The new TypeLength value
	 */
	public void setTypeLength(int value)
	{
		if (value > typeLength)
		{
			typeLength = value;
		}
	}


	/**
	 *  Sets the NameLength attribute of the FieldSizeFieldSize object
	 *
	 *@param  value  The new NameLength value
	 */
	public void setNameLength(int value)
	{
		if (value > nameLength)
		{
			nameLength = value;
		}
	}


	/**
	 *  Sets the MinimumEquals attribute of the FieldSize object
	 *
	 *@param  value  The new MinimumEquals value
	 */
	public void setMinimumEquals(int value)
	{
		if (value > equalsLength)
		{
			equalsLength = value;
		}
	}


	/**
	 *  Gets the ModifierLength attribute of the FieldSizeFieldSize object
	 *
	 *@return    The ModifierLength value
	 */
	public int getModifierLength()
	{
		return modifierLength;
	}


	/**
	 *  Gets the TypeLength attribute of the FieldSizeFieldSize object
	 *
	 *@return    The TypeLength value
	 */
	public int getTypeLength()
	{
		return typeLength;
	}


	/**
	 *  Gets the NameLength attribute of the FieldSizeFieldSize object
	 *
	 *@return    The NameLength value
	 */
	public int getNameLength()
	{
		return nameLength;
	}


	/**
	 *  Gets the EqualsLength attribute of the FieldSize object
	 *
	 *@return    The EqualsLength value
	 */
	public int getEqualsLength()
	{
		return equalsLength;
	}


	/**
	 *  Adds the amount of space that is necessary for the pretty printer to add
	 *  extra spaces for each of the values in a field or local variable
	 *  declaration.
	 *
	 *@param  value  The amount of space to add
	 */
	public void update(int value)
	{
		if (modifierLength != 0)
		{
			modifierLength += value;
		}

		typeLength += value;
		nameLength += value;
		equalsLength += value;
	}


	/**
	 *  Converts this object into a string
	 *
	 *@return    a string
	 */
	public String toString()
	{
		return "Modifier:  " + modifierLength +
				"    Type:  " + typeLength +
				"    Name:  " + nameLength;
	}
}
