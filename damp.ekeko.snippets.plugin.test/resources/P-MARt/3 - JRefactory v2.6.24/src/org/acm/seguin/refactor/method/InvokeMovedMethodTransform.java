/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;

import org.acm.seguin.parser.ast.ASTArgumentList;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTBlock;
import org.acm.seguin.parser.ast.ASTBlockStatement;
import org.acm.seguin.parser.ast.ASTExpression;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTStatement;
import org.acm.seguin.parser.ast.ASTStatementExpression;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.build.BuildExpression;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;

/**
 *  Adds an abstract method to the class
 *
 *@author    Chris Seguin
 */
public class InvokeMovedMethodTransform extends AddNewMethod {
	private Summary dest;


	/**
	 *  Constructor for the InvokeMovedMethodTransform object
	 *
	 *@param  init         The signature of the method that we are adding
	 *@param  destination  Description of Parameter
	 */
	public InvokeMovedMethodTransform(MethodSummary init, Summary destination) {
		super(init);
		dest = destination;
	}


	/**
	 *  Determines if the method is abstract
	 *
	 *@return    true if the method is abstract
	 */
	protected boolean isAbstract() {
		return false;
	}


	/**
	 *  Adds the body of the method
	 *
	 *@param  methodDecl  The feature to be added to the Body attribute
	 *@param  index       The feature to be added to the Body attribute
	 */
	protected void addBody(SimpleNode methodDecl, int index) {
		ASTBlock block = new ASTBlock(0);
		methodDecl.jjtAddChild(block, index);

		if (dest instanceof ParameterSummary) {
			ParameterSummary paramSummary = (ParameterSummary) dest;

			ASTBlockStatement blockStatement = new ASTBlockStatement(0);
			block.jjtAddChild(blockStatement, 0);

			ASTStatement statement = new ASTStatement(0);
			blockStatement.jjtAddChild(statement, 0);

			ASTStatementExpression stateExpress = new ASTStatementExpression(0);
			statement.jjtAddChild(stateExpress, 0);

			ASTPrimaryExpression primaryExpression = new ASTPrimaryExpression(0);
			stateExpress.jjtAddChild(primaryExpression, 0);

			ASTPrimaryPrefix prefix = new ASTPrimaryPrefix(0);
			primaryExpression.jjtAddChild(prefix, 0);

			ASTName name = new ASTName(0);
			name.addNamePart(paramSummary.getName());
			name.addNamePart(methodSummary.getName());
			primaryExpression.jjtAddChild(name, 0);

			ASTPrimarySuffix suffix = new ASTPrimarySuffix(0);
			primaryExpression.jjtAddChild(suffix, 1);

			ASTArguments args = new ASTArguments(0);
			suffix.jjtAddChild(args, 0);

			ASTArgumentList argList = new ASTArgumentList(0);
			args.jjtAddChild(argList, 0);

			int count = 0;
			Iterator iter = methodSummary.getParameters();
			if (iter != null) {
				while (iter.hasNext()) {
					ParameterSummary next = (ParameterSummary) iter.next();
					if (!next.equals(paramSummary)) {
						ASTExpression expr = buildExpression(next.getName());
						argList.jjtAddChild(expr, count);
						count++;
					}
				}
			}

			if (isObjectReferenced()) {
				argList.jjtAddChild(buildExpression("this"), count);
			}
		}
	}


	/**
	 *  Determines if this object is referenced
	 *
	 *@return    The ObjectReferenced value
	 */
	private boolean isObjectReferenced() {
		return ObjectReference.isReferenced(methodSummary);
	}


	/**
	 *  Builds an expression
	 *
	 *@param  name  the name at the base of the expression
	 *@return       the expression
	 */
	private ASTExpression buildExpression(String name) {
		BuildExpression be = new BuildExpression();
		return be.buildName(name);
	}
}
