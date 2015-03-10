/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser;

import org.acm.seguin.parser.ast.ASTAdditiveExpression;
import org.acm.seguin.parser.ast.ASTAllocationExpression;
import org.acm.seguin.parser.ast.ASTAndExpression;
import org.acm.seguin.parser.ast.ASTArgumentList;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTArrayDimsAndInits;
import org.acm.seguin.parser.ast.ASTArrayInitializer;
import org.acm.seguin.parser.ast.ASTAssignmentOperator;
import org.acm.seguin.parser.ast.ASTBlock;
import org.acm.seguin.parser.ast.ASTBlockStatement;
import org.acm.seguin.parser.ast.ASTBooleanLiteral;
import org.acm.seguin.parser.ast.ASTBreakStatement;
import org.acm.seguin.parser.ast.ASTCastExpression;
import org.acm.seguin.parser.ast.ASTCastLookahead;
import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTClassBodyDeclaration;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTCompilationUnit;
import org.acm.seguin.parser.ast.ASTConditionalAndExpression;
import org.acm.seguin.parser.ast.ASTConditionalExpression;
import org.acm.seguin.parser.ast.ASTConditionalOrExpression;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTContinueStatement;
import org.acm.seguin.parser.ast.ASTDoStatement;
import org.acm.seguin.parser.ast.ASTEmptyStatement;
import org.acm.seguin.parser.ast.ASTEqualityExpression;
import org.acm.seguin.parser.ast.ASTExclusiveOrExpression;
import org.acm.seguin.parser.ast.ASTExplicitConstructorInvocation;
import org.acm.seguin.parser.ast.ASTExpression;
import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTForInit;
import org.acm.seguin.parser.ast.ASTForStatement;
import org.acm.seguin.parser.ast.ASTForUpdate;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTIfStatement;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTInclusiveOrExpression;
import org.acm.seguin.parser.ast.ASTInitializer;
import org.acm.seguin.parser.ast.ASTInstanceOfExpression;
import org.acm.seguin.parser.ast.ASTInterfaceBody;
import org.acm.seguin.parser.ast.ASTInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTInterfaceMemberDeclaration;
import org.acm.seguin.parser.ast.ASTLabeledStatement;
import org.acm.seguin.parser.ast.ASTLiteral;
import org.acm.seguin.parser.ast.ASTLocalVariableDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarationLookahead;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTMultiplicativeExpression;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTNameList;
import org.acm.seguin.parser.ast.ASTNestedClassDeclaration;
import org.acm.seguin.parser.ast.ASTNestedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTNullLiteral;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTPostfixExpression;
import org.acm.seguin.parser.ast.ASTPreDecrementExpression;
import org.acm.seguin.parser.ast.ASTPreIncrementExpression;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTRelationalExpression;
import org.acm.seguin.parser.ast.ASTResultType;
import org.acm.seguin.parser.ast.ASTReturnStatement;
import org.acm.seguin.parser.ast.ASTShiftExpression;
import org.acm.seguin.parser.ast.ASTStatement;
import org.acm.seguin.parser.ast.ASTStatementExpression;
import org.acm.seguin.parser.ast.ASTStatementExpressionList;
import org.acm.seguin.parser.ast.ASTSwitchLabel;
import org.acm.seguin.parser.ast.ASTSwitchStatement;
import org.acm.seguin.parser.ast.ASTSynchronizedStatement;
import org.acm.seguin.parser.ast.ASTThrowStatement;
import org.acm.seguin.parser.ast.ASTTryStatement;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTUnaryExpression;
import org.acm.seguin.parser.ast.ASTUnaryExpressionNotPlusMinus;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTVariableDeclarator;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.parser.ast.ASTVariableInitializer;
import org.acm.seguin.parser.ast.ASTWhileStatement;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  Scan through the abstract syntax tree and does nothing 
 *
 *@author     Chris Seguin 
 *@created    December 10, 1999 
 */
public class ChildrenVisitor implements JavaParserVisitor {
	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTCompilationUnit node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPackageDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTImportDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTTypeDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTClassDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTClassBody node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTNestedClassDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTClassBodyDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTMethodDeclarationLookahead node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInterfaceDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTNestedInterfaceDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInterfaceBody node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInterfaceMemberDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTFieldDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTVariableDeclarator node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTVariableDeclaratorId node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTVariableInitializer node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTArrayInitializer node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTMethodDeclarator node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTFormalParameters node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTFormalParameter node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTConstructorDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTExplicitConstructorInvocation node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInitializer node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTType node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPrimitiveType node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTResultType node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTName node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTNameList node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTAssignmentOperator node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTConditionalExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTConditionalOrExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTConditionalAndExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInclusiveOrExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTExclusiveOrExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTAndExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTEqualityExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTInstanceOfExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTRelationalExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTShiftExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTAdditiveExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTMultiplicativeExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnaryExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPreIncrementExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPreDecrementExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTCastLookahead node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPostfixExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTCastExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPrimaryExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPrimaryPrefix node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTPrimarySuffix node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTLiteral node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTBooleanLiteral node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTNullLiteral node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTArguments node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTArgumentList node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTAllocationExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTArrayDimsAndInits node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTLabeledStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTBlock node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTBlockStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTLocalVariableDeclaration node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTEmptyStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTStatementExpression node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTSwitchStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTSwitchLabel node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTIfStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTWhileStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTDoStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTForStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTForInit node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTStatementExpressionList node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTForUpdate node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTBreakStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTContinueStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTReturnStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTThrowStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTSynchronizedStatement node, Object data) {
		return node.childrenAccept(this, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTTryStatement node, Object data) {
		return node.childrenAccept(this, data);
	}
}
