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

import org.acm.seguin.parser.ast.ASTBlock;
import org.acm.seguin.parser.ast.ASTClassBodyDeclaration;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTNameList;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTResultType;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.TypeDeclSummary;

/**
 *  A series of transformations taht adds a new method to a class.
 *
 *@author    Chris Seguin
 */
abstract class AddNewMethod extends TransformAST {
	/**
	 *  Description of the Field
	 */
	protected MethodSummary methodSummary;


	/**
	 *  Constructor for the AddNewMethod object
	 *
	 *@param  init  the method summary to add
	 */
	public AddNewMethod(MethodSummary init) {
		methodSummary = init;
	}


	/**
	 *  Update the syntax tree
	 *
	 *@param  root  the root of the syntax tree
	 */
	public void update(SimpleNode root) {
		//  Find the type declaration
		int last = root.jjtGetNumChildren();
		for (int ndx = 0; ndx < last; ndx++) {
			if (root.jjtGetChild(ndx) instanceof ASTTypeDeclaration) {
				drillIntoType((SimpleNode) root.jjtGetChild(ndx));
				return;
			}
		}
	}


	/**
	 *  Sets up the modifiers
	 *
	 *@param  source  the source holder
	 *@param  dest    the destination holder
	 */
	protected void setupModifiers(ModifierHolder source, ModifierHolder dest) {
		dest.copy(source);
	}


	/**
	 *  Determines if the method is abstract
	 *
	 *@return    true if the method is abstract
	 */
	protected boolean isAbstract() {
		return methodSummary.getModifiers().isAbstract();
	}


	/**
	 *  Adds the return to the method declaration
	 *
	 *@param  methodDecl  The feature to be added to the Return attribute
	 *@param  index       The feature to be added to the Return attribute
	 */
	protected void addReturn(SimpleNode methodDecl, int index) {
		ASTResultType result = new ASTResultType(0);
		TypeDeclSummary returnType = methodSummary.getReturnType();
		if ((returnType != null) && !returnType.getType().equals("void")) {
			ASTType type = buildType(returnType);
			result.jjtAddChild(type, 0);
		}
		methodDecl.jjtAddChild(result, index);
	}


	/**
	 *  Creates the parameters
	 *
	 *@return    Description of the Returned Value
	 */
	protected ASTFormalParameters createParameters() {
		ASTFormalParameters params = new ASTFormalParameters(0);
		Iterator iter = methodSummary.getParameters();
		if (iter != null) {
			int paramCount = 0;
			while (iter.hasNext()) {
				ParameterSummary paramSummary = (ParameterSummary) iter.next();
				ASTFormalParameter nextParam = new ASTFormalParameter(0);
				ASTType type = buildType(paramSummary.getTypeDecl());
				nextParam.jjtAddChild(type, 0);
				ASTVariableDeclaratorId paramDeclID = new ASTVariableDeclaratorId(0);
				paramDeclID.setName(paramSummary.getName());
				nextParam.jjtAddChild(paramDeclID, 1);
				params.jjtAddChild(nextParam, paramCount);
				paramCount++;
			}
		}
		return params;
	}


	/**
	 *  Creates the exceptions
	 *
	 *@param  iter  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	protected ASTNameList createExceptions(Iterator iter) {
		ASTNameList list = new ASTNameList(0);
		int ndx = 0;
		while (iter.hasNext()) {
			TypeDeclSummary next = (TypeDeclSummary) iter.next();
			list.jjtAddChild(buildName(next), ndx);
			ndx++;
		}
		return list;
	}


	/**
	 *  Adds the exceptions to the node
	 *
	 *@param  methodDecl  The feature to be added to the Exceptions attribute
	 *@param  index       The feature to be added to the Exceptions attribute
	 *@return             Description of the Returned Value
	 */
	protected int addExceptions(SimpleNode methodDecl, int index) {
		Iterator iter = methodSummary.getExceptions();
		if (iter != null) {
			ASTNameList list = createExceptions(iter);
			methodDecl.jjtAddChild(list, index);
			index++;
		}
		return index;
	}


	/**
	 *  Adds the body of the method
	 *
	 *@param  methodDecl  The feature to be added to the Body attribute
	 *@param  index       The feature to be added to the Body attribute
	 */
	protected void addBody(SimpleNode methodDecl, int index) {
		if (!isAbstract()) {
			ASTBlock block = new ASTBlock(0);
			methodDecl.jjtAddChild(block, index);
		}
	}


	/**
	 *  Drills down into the class definition to add the method
	 *
	 *@param  node  the type syntax tree node that is being modified
	 */
	private void drillIntoType(SimpleNode node) {
		if (node.jjtGetChild(0) instanceof ASTClassDeclaration) {
			ASTClassDeclaration classDecl = (ASTClassDeclaration) node.jjtGetChild(0);
			if (isAbstract()) {
				classDecl.addModifier("abstract");
			}
			SimpleNode unmodified = (SimpleNode) classDecl.jjtGetChild(0);
			SimpleNode classBody = (SimpleNode) unmodified.jjtGetChild(unmodified.jjtGetNumChildren() - 1);
			ASTClassBodyDeclaration decl = new ASTClassBodyDeclaration(0);
			decl.jjtAddChild(build(true), 0);
			classBody.jjtAddChild(decl, classBody.jjtGetNumChildren());
		}
		else if (node.jjtGetChild(0) instanceof ASTInterfaceDeclaration) {
			ASTInterfaceDeclaration classDecl = (ASTInterfaceDeclaration) node.jjtGetChild(0);
			SimpleNode unmodified = (SimpleNode) classDecl.jjtGetChild(0);
			SimpleNode classBody = (SimpleNode) unmodified.jjtGetChild(unmodified.jjtGetNumChildren() - 1);
			classBody.jjtAddChild(build(false), classBody.jjtGetNumChildren());
		}
	}


	/**
	 *  Builds the method to be adding
	 *
	 *@param  addBody  Description of Parameter
	 *@return          a syntax tree branch containing the new method
	 */
	private ASTMethodDeclaration build(boolean addBody) {
		ASTMethodDeclaration methodDecl = new ASTMethodDeclaration(0);

		//  Set the modifiers
		setupModifiers(methodSummary.getModifiers(),
				methodDecl.getModifiers());

		//  Set return type
		addReturn(methodDecl, 0);

		//  Set the declaration
		ASTMethodDeclarator declarator = new ASTMethodDeclarator(0);
		declarator.setName(methodSummary.getName());
		ASTFormalParameters params = createParameters();
		declarator.jjtAddChild(params, 0);
		methodDecl.jjtAddChild(declarator, 1);

		int index = 2;

		index = addExceptions(methodDecl, index);

		if (addBody) {
			addBody(methodDecl, index);
		}

		return methodDecl;
	}


	/**
	 *  Builds the type from the type declaration summary
	 *
	 *@param  summary  the summary
	 *@return          the AST type node
	 */
	private ASTType buildType(TypeDeclSummary summary) {
		ASTType type = new ASTType(0);
		if (summary.isPrimitive()) {
			ASTPrimitiveType addition = new ASTPrimitiveType(0);
			addition.setName(summary.getLongName());
			type.jjtAddChild(addition, 0);
		}
		else {
			ASTName name = buildName(summary);
			type.jjtAddChild(name, 0);
		}

		type.setArrayCount(summary.getArrayCount());
		return type;
	}


	/**
	 *  Builds the name of the type from the type decl summary
	 *
	 *@param  summary  the summary
	 *@return          the name node
	 */
	private ASTName buildName(TypeDeclSummary summary) {
		ASTName name = new ASTName(0);
		name.fromString(summary.getLongName());
		return name;
	}
}
