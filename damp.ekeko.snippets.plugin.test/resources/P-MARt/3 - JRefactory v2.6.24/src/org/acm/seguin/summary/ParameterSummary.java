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
 *  Stores information about the formal parameter 
 *
 *@author     Chris Seguin 
 *@created    June 10, 1999 
 */
public class ParameterSummary extends VariableSummary {
	/**
	 *  Creates a parameter summary 
	 *
	 *@param  parentSummary  the parent summary 
	 *@param  typeNode       the type of parameter 
	 *@param  id             the id of the parameter 
	 */
	public ParameterSummary(Summary parentSummary, ASTType typeNode, ASTVariableDeclaratorId id) {
		super(parentSummary, typeNode, id);
	}


	/**
	 *  Provide method to visit a node 
	 *
	 *@param  visitor  the visitor 
	 *@param  data     the data for the visit 
	 *@return          some new data 
	 */
	public Object accept(SummaryVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
