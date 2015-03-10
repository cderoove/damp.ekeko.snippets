package org.acm.seguin.parser.build;

import org.acm.seguin.parser.ast.ASTAdditiveExpression;
import org.acm.seguin.parser.ast.ASTAndExpression;
import org.acm.seguin.parser.ast.ASTConditionalAndExpression;
import org.acm.seguin.parser.ast.ASTConditionalExpression;
import org.acm.seguin.parser.ast.ASTConditionalOrExpression;
import org.acm.seguin.parser.ast.ASTEqualityExpression;
import org.acm.seguin.parser.ast.ASTExclusiveOrExpression;
import org.acm.seguin.parser.ast.ASTExpression;
import org.acm.seguin.parser.ast.ASTInclusiveOrExpression;
import org.acm.seguin.parser.ast.ASTInstanceOfExpression;
import org.acm.seguin.parser.ast.ASTMultiplicativeExpression;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPostfixExpression;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTRelationalExpression;
import org.acm.seguin.parser.ast.ASTShiftExpression;
import org.acm.seguin.parser.ast.ASTUnaryExpression;
import org.acm.seguin.parser.ast.ASTUnaryExpressionNotPlusMinus;

/**
 *  This object builds an expression.  The first method builds
 *  an expression based on the name of the argument.
 *
 *@author    Chris Seguin
 */
public class BuildExpression {
	/**
	 *  Builds an expression based on a name
	 *
	 *@param  name  the name of the field or parameter
	 *@return       the expression
	 */
	public ASTExpression buildName(String name) {
		ASTExpression expression = new ASTExpression(0);

		ASTConditionalExpression condExpression = new ASTConditionalExpression(0);
		expression.jjtAddChild(condExpression, 0);

		ASTConditionalOrExpression condOrExpression = new ASTConditionalOrExpression(0);
		condExpression.jjtAddChild(condOrExpression, 0);

		ASTConditionalAndExpression condAndExpression = new ASTConditionalAndExpression(0);
		condOrExpression.jjtAddChild(condAndExpression, 0);

		ASTInclusiveOrExpression inclOrExpression = new ASTInclusiveOrExpression(0);
		condAndExpression.jjtAddChild(inclOrExpression, 0);

		ASTExclusiveOrExpression exclOrExpression = new ASTExclusiveOrExpression(0);
		inclOrExpression.jjtAddChild(exclOrExpression, 0);

		ASTAndExpression andExpression = new ASTAndExpression(0);
		exclOrExpression.jjtAddChild(andExpression, 0);

		ASTEqualityExpression equalExpression = new ASTEqualityExpression(0);
		andExpression.jjtAddChild(equalExpression, 0);

		ASTInstanceOfExpression instanceOfExpression = new ASTInstanceOfExpression(0);
		equalExpression.jjtAddChild(instanceOfExpression, 0);

		ASTRelationalExpression relationalExpression = new ASTRelationalExpression(0);
		instanceOfExpression.jjtAddChild(relationalExpression, 0);

		ASTShiftExpression shiftExpression = new ASTShiftExpression(0);
		relationalExpression.jjtAddChild(shiftExpression, 0);

		ASTAdditiveExpression addExpression = new ASTAdditiveExpression(0);
		shiftExpression.jjtAddChild(addExpression, 0);

		ASTMultiplicativeExpression multExpression = new ASTMultiplicativeExpression(0);
		addExpression.jjtAddChild(multExpression, 0);

		ASTUnaryExpression unaryExpression = new ASTUnaryExpression(0);
		multExpression.jjtAddChild(unaryExpression, 0);

		ASTUnaryExpressionNotPlusMinus uenpm = new ASTUnaryExpressionNotPlusMinus(0);
		unaryExpression.jjtAddChild(uenpm, 0);

		ASTPostfixExpression postfixExpression = new ASTPostfixExpression(0);
		uenpm.jjtAddChild(postfixExpression, 0);

		ASTPrimaryExpression primaryExpression = new ASTPrimaryExpression(0);
		postfixExpression.jjtAddChild(primaryExpression, 0);

		ASTPrimaryPrefix primaryPrefix = new ASTPrimaryPrefix(0);
		primaryExpression.jjtAddChild(primaryPrefix, 0);

		ASTName nameNode = new ASTName(0);
		nameNode.addNamePart(name);
		primaryExpression.jjtAddChild(nameNode, 0);

		return expression;
	}
}
