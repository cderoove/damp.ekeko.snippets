/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTLocalVariableDeclaration;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;

/**
 *  Stores information about the formal parameter
 *
 *@author     Chris Seguin
 *@created    June 13, 1999
 */
public class LocalVariableSummary extends VariableSummary
{
	/**
	 *  Creates a parameter summary
	 *
	 *@param  parentSummary  the parent summary
	 *@param  typeNode       the type of parameter
	 *@param  id             the id of the parameter
	 */
	public LocalVariableSummary(Summary parentSummary, ASTType typeNode, ASTVariableDeclaratorId id)
	{
		super(parentSummary, typeNode, id);
	}


	/**
	 *  Constructor for the LocalVariableSummary object
	 *
	 *@param  parentSummary  Description of Parameter
	 *@param  type           Description of Parameter
	 *@param  name           Description of Parameter
	 */
	public LocalVariableSummary(Summary parentSummary, TypeDeclSummary type, String name)
	{
		super(parentSummary, type, name);
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


	/**
	 *  Factory method
	 *
	 *@param  parentSummary  the parent summary
	 *@param  field          the field declarator
	 *@return                Description of the Returned Value
	 */
	public static LocalVariableSummary[] createNew(Summary parentSummary, ASTLocalVariableDeclaration field)
	{
		//  Local Variables
		int last = field.jjtGetNumChildren();
		LocalVariableSummary[] result = new LocalVariableSummary[last - 1];
		ASTType type = (ASTType) field.jjtGetChild(0);

		//  Create a summary for each field
		for (int ndx = 1; ndx < last; ndx++)
		{
			Node next = field.jjtGetChild(ndx);
			result[ndx - 1] = new LocalVariableSummary(parentSummary, type,
					(ASTVariableDeclaratorId) next.jjtGetChild(0));
		}

		//  Return the result
		return result;
	}
}
