/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;

/**
 *  Stores information about a method
 *
 *@author     Chris Seguin
 *@created    June 8, 1999
 */
public class VariableSummary extends Summary {
	//  Instance Variables
	private String name;
	private TypeDeclSummary type;
	private StringBuffer declaration = null;


	/**
	 *  Construct a method from a method declaration node
	 *
	 *@param  parentSummary  the parent summary
	 *@param  id             The id of the variable
	 *@param  typeNode       Description of Parameter
	 */
	public VariableSummary(Summary parentSummary, ASTType typeNode, ASTVariableDeclaratorId id)
	{
		//  Initialize the parent class
		super(parentSummary);

		//  Remember the name
		name = id.getName().intern();

		//  Remember the type
		type = TypeDeclSummary.getTypeDeclSummary(this, typeNode);

		//  Rememeber the array count
		type.setArrayCount(type.getArrayCount() + id.getArrayCount());
	}


	/**
	 *  Create a variable for debugging purposes
	 *
	 *@param  parentSummary  Description of Parameter
	 *@param  type           Description of Parameter
	 *@param  name           Description of Parameter
	 */
	public VariableSummary(Summary parentSummary, TypeDeclSummary type, String name)
	{
		//  Initialize the parent class
		super(parentSummary);

		//  Remember the name
		this.name = name;

		//  Remember the type
		this.type = type;
	}


	/**
	 *  Sets the name
	 *
	 *@param  value  the name of the field
	 */
	public void setName(String value)
	{
		name = value;
	}


	/**
	 *  Return the name
	 *
	 *@return    the name of the method
	 */
	public String getName()
	{
		return name;
	}


	/**
	 *  Return the type as a string
	 *
	 *@return    the name of the method
	 */
	public String getType()
	{
		return type.toString();
	}


	/**
	 *  Return the type as a type summary
	 *
	 *@return    the type summary
	 */
	public TypeDeclSummary getTypeDecl()
	{
		return type;
	}


	/**
	 *  Convert this to a string
	 *
	 *@return    a string representation of this object
	 */
	public String getDeclaration()
	{
		if (declaration == null) {
			declaration = new StringBuffer();
		}
		declaration.setLength(0);
		declaration.append(type.getName());
		declaration.append(" ");
		declaration.append(getName());
		return declaration.toString();
	}


	/**
	 *  Convert this to a string
	 *
	 *@return    a string representation of this object
	 */
	public String toString()
	{
		return getName() + ":" + getType();
	}


	/**
	 *  Provide method to visit a node
	 *
	 *@param  visitor  the visitor
	 *@param  data     the data for the visit
	 *@return          some new data
	 */
	public Object accept(SummaryVisitor visitor, Object data)
	{
		return visitor.visit(this, data);
	}
}
