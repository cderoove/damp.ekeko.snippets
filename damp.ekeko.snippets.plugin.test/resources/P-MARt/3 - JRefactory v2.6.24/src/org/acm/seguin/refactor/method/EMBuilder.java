/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTArgumentList;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTAssignmentOperator;
import org.acm.seguin.parser.ast.ASTBlockStatement;
import org.acm.seguin.parser.ast.ASTExpression;
import org.acm.seguin.parser.ast.ASTLocalVariableDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTStatement;
import org.acm.seguin.parser.ast.ASTStatementExpression;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTVariableDeclarator;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.parser.ast.ASTVariableInitializer;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.build.BuildExpression;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Builds the invocation of the method that is extracted for insertion into
 *  the place where the method was extracted
 *
 *@author    Chris Seguin
 */
class EMBuilder
{
	private String methodName;
	private boolean isStatement;
	private LinkedList parameters;
	private VariableSummary returnSummary;
	private boolean localVariableNeeded = false;


	/**
	 *  Constructor for the EMBuilder object
	 */
	public EMBuilder()
	{
		returnSummary = null;
	}


	/**
	 *  Sets the MethodName attribute of the EMBuilder object
	 *
	 *@param  value  The new MethodName value
	 */
	public void setMethodName(String value)
	{
		methodName = value;
	}


	/**
	 *  Sets the Statement attribute of the EMBuilder object
	 *
	 *@param  value  The new Statement value
	 */
	public void setStatement(boolean value)
	{
		isStatement = value;
	}


	/**
	 *  Sets the Parameters attribute of the EMBuilder object
	 *
	 *@param  list  The new Parameters value
	 */
	public void setParameters(LinkedList list)
	{
		parameters = list;
	}


	/**
	 *  Sets the ReturnName attribute of the EMBuilder object
	 *
	 *@param  name  The new ReturnName value
	 */
	public void setReturnSummary(VariableSummary value)
	{
		returnSummary = value;
	}


	/**
	 *  Sets the LocalVariableNeeded attribute of the EMBuilder object
	 *
	 *@param  value  The new LocalVariableNeeded value
	 */
	public void setLocalVariableNeeded(boolean value)
	{
		localVariableNeeded = value;
	}


	/**
	 *  Builds the statement or assignment or local variable declaration
	 *
	 *@return    The resulting value
	 */
	public Node build()
	{
		ASTBlockStatement blockStatement = new ASTBlockStatement(0);

		if (localVariableNeeded) {
			buildWithLocal(blockStatement);
			return blockStatement;
		}

		ASTStatement statement = new ASTStatement(0);
		blockStatement.jjtAddChild(statement, 0);

		ASTStatementExpression stateExpress = new ASTStatementExpression(0);
		statement.jjtAddChild(stateExpress, 0);

		ASTPrimaryExpression primaryExpression = null;
		if (isStatement && (returnSummary != null))
		{
			buildAssignment(stateExpress);
		}
		else
		{
			primaryExpression = buildMethodInvocation(stateExpress, 0);
		}

		if (isStatement)
		{
			return blockStatement;
		}
		else
		{
			return primaryExpression;
		}
	}


	/**
	 *  Builds the assignment
	 *
	 *@param  stateExpress  Description of Parameter
	 */
	private void buildAssignment(ASTStatementExpression stateExpress)
	{
		//  First add what we are returning
		ASTPrimaryExpression primaryExpression = new ASTPrimaryExpression(0);
		stateExpress.jjtAddChild(primaryExpression, 0);

		ASTPrimaryPrefix prefix = new ASTPrimaryPrefix(0);
		primaryExpression.jjtAddChild(prefix, 0);

		ASTName name = new ASTName(0);
		name.addNamePart(returnSummary.getName());
		primaryExpression.jjtAddChild(name, 0);

		//  Now add the assignment operator
		ASTAssignmentOperator assign = new ASTAssignmentOperator(0);
		assign.setName("=");
		stateExpress.jjtAddChild(assign, 1);

		//  Finally add the rest
		buildMethodInvocation(stateExpress, 2);
	}


	/**
	 *  Builds the method invocation
	 *
	 *@param  stateExpress  Description of Parameter
	 *@param  index         Description of Parameter
	 *@return               Description of the Returned Value
	 */
	private ASTPrimaryExpression buildMethodInvocation(SimpleNode stateExpress, int index)
	{
		ASTPrimaryExpression primaryExpression = new ASTPrimaryExpression(0);
		stateExpress.jjtAddChild(primaryExpression, index);

		ASTPrimaryPrefix prefix = new ASTPrimaryPrefix(0);
		primaryExpression.jjtAddChild(prefix, 0);

		ASTName name = new ASTName(0);
		name.addNamePart(methodName);
		primaryExpression.jjtAddChild(name, 0);

		ASTPrimarySuffix suffix = new ASTPrimarySuffix(0);
		primaryExpression.jjtAddChild(suffix, 1);

		ASTArguments args = new ASTArguments(0);
		suffix.jjtAddChild(args, 0);

		ASTArgumentList argList = new ASTArgumentList(0);
		args.jjtAddChild(argList, 0);

		int count = 0;
		BuildExpression be = new BuildExpression();
		Iterator iter = parameters.iterator();
		if (iter != null)
		{
			while (iter.hasNext())
			{
				VariableSummary next = (VariableSummary) iter.next();
				ASTExpression expr = be.buildName(next.getName());
				argList.jjtAddChild(expr, count);
				count++;
			}
		}

		return primaryExpression;
	}


	/**
	 *  Builds a local variable declaration
	 *
	 *@param  blockStatement  the block statement we are inserting into
	 */
	private void buildWithLocal(ASTBlockStatement blockStatement)
	{
		ASTLocalVariableDeclaration statement = new ASTLocalVariableDeclaration(0);
		blockStatement.jjtAddChild(statement, 0);

		ASTType type = new ASTType(0);
		statement.jjtAddChild(type, 0);

		TypeDeclSummary typeDecl = returnSummary.getTypeDecl();
		type.setArrayCount(typeDecl.getArrayCount());
		if (typeDecl.isPrimitive())
		{
			ASTPrimitiveType primitiveType = new ASTPrimitiveType(0);
			primitiveType.setName(typeDecl.getType());
			type.jjtAddChild(primitiveType, 0);
		}
		else
		{
			ASTName name = new ASTName(0);
			name.fromString(typeDecl.getLongName());
			type.jjtAddChild(name, 0);
		}

		ASTVariableDeclarator varDecl = new ASTVariableDeclarator(0);
		statement.jjtAddChild(varDecl, 1);

		ASTVariableDeclaratorId varDeclID = new ASTVariableDeclaratorId(0);
		varDeclID.setName(returnSummary.getName());
		varDecl.jjtAddChild(varDeclID, 0);

		ASTVariableInitializer initializer = new ASTVariableInitializer(0);
		varDecl.jjtAddChild(initializer, 1);

		buildMethodInvocation(initializer, 0);
	}
}
