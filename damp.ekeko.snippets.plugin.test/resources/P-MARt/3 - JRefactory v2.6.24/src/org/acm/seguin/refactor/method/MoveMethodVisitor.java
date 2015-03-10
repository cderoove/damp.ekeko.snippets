package org.acm.seguin.refactor.method;

import java.util.Iterator;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTArgumentList;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTExpression;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.FieldQuery;

/**
 *  Visitor that prepares a method for being incorporated into another class.
 *
 *@author    Chris Seguin
 */
public class MoveMethodVisitor extends ChildrenVisitor {
	private MethodSummary methodSummary;
	private TypeSummary typeSummary;
	private Summary destination;


	/**
	 *  Constructor for the MoveMethodVisitor object
	 *
	 *@param  initType    Description of Parameter
	 *@param  initMethod  Description of Parameter
	 *@param  initDest    Description of Parameter
	 */
	public MoveMethodVisitor(TypeSummary initType, MethodSummary initMethod, Summary initDest) {
		typeSummary = initType;
		methodSummary = initMethod;
		destination = initDest;
	}


	/**
	 *  Don't go into any class definitions
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
		return data;
	}


	/**
	 *  Don't go into any interface definitions
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
		return data;
	}


	/**
	 *  To visit a node
	 *
	 *@param  node  The node we are visiting
	 *@param  data  The rename type data
	 *@return       The rename type data
	 */
	public Object visit(ASTFormalParameters node, Object data) {
		if (destination instanceof ParameterSummary) {
			String name = destination.getName();

			int last = node.jjtGetNumChildren();
			for (int ndx = 0; ndx < last; ndx++) {
				ASTFormalParameter param = (ASTFormalParameter) node.jjtGetChild(ndx);
				ASTVariableDeclaratorId decl = (ASTVariableDeclaratorId) param.jjtGetChild(1);
				if (decl.getName().equals(name)) {
					node.jjtDeleteChild(ndx);
					ndx--;
					last--;
				}
			}

			if (ObjectReference.isReferenced(methodSummary)) {
				ASTFormalParameter newParam = new ASTFormalParameter(0);
				ASTType type = new ASTType(0);
				newParam.jjtAddChild(type, 0);
				ASTName nameNode = new ASTName(0);
				String typeName = typeSummary.getName();
				nameNode.addNamePart(typeName);
				type.jjtAddChild(nameNode, 0);
				ASTVariableDeclaratorId id = new ASTVariableDeclaratorId(0);
				id.setName(typeName.substring(0, 1).toLowerCase() + typeName.substring(1));
				newParam.jjtAddChild(id, 1);

				last = node.jjtGetNumChildren();
				node.jjtAddChild(newParam, last);
			}
		}

		return data;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTExpression node, Object data) {
		if (node.jjtGetNumChildren() > 1) {
			Object result = super.visit(node, Boolean.TRUE);
			if (result != null) {
				ASTArguments args = (ASTArguments) result;
				ASTArgumentList list = new ASTArgumentList(0);
				args.jjtAddChild(list, 0);
				list.jjtAddChild(node.jjtGetChild(2), 0);
				node.jjtDeleteChild(2);
				node.jjtDeleteChild(1);
			}

			return null;
		}
		else {
			return super.visit(node, Boolean.FALSE);
		}
	}


	/**
	 *  Visit method for primary expressions
	 *
	 *@param  node  the node we are visiting
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTPrimaryExpression node, Object data) {
		Object result = null;
		if (destination instanceof ParameterSummary) {
			String parameterName = destination.getName();

			ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node.jjtGetChild(0);
			if ("this".equals(prefix.getName())) {
				ASTName nameNode = new ASTName(0);
				nameNode.addNamePart(getReplacementVariableName());
				prefix.jjtAddChild(nameNode, 0);
			}
			else if (isMethod(node, prefix)) {
				updatePrimaryPrefix(prefix, parameterName);
			}
			else if (isVariable(node, prefix)) {
				ASTName nameNode = (ASTName) prefix.jjtGetChild(0);
				if (!isLocalVariable(nameNode.getNamePart(0))) {
					Boolean value = (Boolean) data;
					result = updatePrivateField(node, prefix, nameNode, parameterName, value.booleanValue());
				}
			}
		}

		super.visit(node, data);

		return result;
	}


	/**
	 *  Determine if we have a method
	 *
	 *@param  node    Description of Parameter
	 *@param  prefix  Description of Parameter
	 *@return         The Method value
	 */
	private boolean isMethod(ASTPrimaryExpression node, ASTPrimaryPrefix prefix) {
		return (node.jjtGetNumChildren() > 1) &&
				(node.jjtGetChild(1).jjtGetChild(0) instanceof ASTArguments) &&
				(prefix.jjtGetChild(0) instanceof ASTName);
	}


	/**
	 *  Determine if we have a field access
	 *
	 *@param  node    Description of Parameter
	 *@param  prefix  Description of Parameter
	 *@return         The Variable value
	 */
	private boolean isVariable(ASTPrimaryExpression node, ASTPrimaryPrefix prefix) {
		return (node.jjtGetNumChildren() == 1) &&
				(prefix.jjtGetChild(0) instanceof ASTName);
	}


	/**
	 *  Checks if the value is a local variable
	 *
	 *@param  name  the name of the variable
	 *@return       true if the name is a local variable
	 */
	private boolean isLocalVariable(String name) {
		Iterator iter = methodSummary.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				if (next instanceof LocalVariableSummary) {
					LocalVariableSummary lvs = (LocalVariableSummary) next;
					if (lvs.getName().equals(name)) {
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 *  Gets the name of the getter for the field
	 *
	 *@param  summary  the field summary
	 *@return          the getter
	 */
	private String getFieldGetter(FieldSummary summary) {
		String typeName = summary.getType();
		String prefix = "get";
		if (typeName.equalsIgnoreCase("boolean")) {
			prefix = "is";
		}

		String name = summary.getName();

		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}


	/**
	 *  Gets the name of the setter for the field
	 *
	 *@param  summary  the field summary
	 *@return          the setter
	 */
	private String getFieldSetter(FieldSummary summary) {
		String prefix = "set";
		String name = summary.getName();
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}


	/**
	 *  Update the primary prefix
	 *
	 *@param  prefix         Description of Parameter
	 *@param  parameterName  Description of Parameter
	 */
	private void updatePrimaryPrefix(ASTPrimaryPrefix prefix, String parameterName) {
		ASTName nameNode = (ASTName) prefix.jjtGetChild(0);

		if (nameNode.getNameSize() == 1) {
			updateLocalReferences(prefix, nameNode);
		}
		else if ((nameNode.getNameSize() == 2) && (nameNode.getNamePart(0).equals(parameterName))) {
			updateParameterReferences(prefix, nameNode);
		}
	}


	/**
	 *  Updates the local references
	 *
	 *@param  prefix    Description of Parameter
	 *@param  nameNode  Description of Parameter
	 */
	private void updateLocalReferences(ASTPrimaryPrefix prefix,
			ASTName nameNode) {
		ASTName newName = new ASTName(0);
		newName.addNamePart(getReplacementVariableName());
		newName.addNamePart(nameNode.getNamePart(0));
		prefix.jjtAddChild(newName, 0);
	}

	/**
 	 *  Gets the variable name that is replacing "this" in the method
	 *
	 *@return the name of the variable
	 */
	private String getReplacementVariableName() {
		String typeName = typeSummary.getName();
		return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
	}


	/**
	 *  Updates references to the parameter
	 *
	 *@param  prefix    Description of Parameter
	 *@param  nameNode  Description of Parameter
	 */
	private void updateParameterReferences(ASTPrimaryPrefix prefix, ASTName nameNode) {
		ASTName newName = new ASTName(0);
		newName.addNamePart(nameNode.getNamePart(1));
		prefix.jjtAddChild(newName, 0);
	}


	/**
	 *  Updates a private field
	 *
	 *@param  primary        Description of Parameter
	 *@param  prefix         Description of Parameter
	 *@param  nameNode       Description of Parameter
	 *@param  parameterName  Description of Parameter
	 *@param  isSetter       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	private Object updatePrivateField(ASTPrimaryExpression primary,
			ASTPrimaryPrefix prefix, ASTName nameNode,
			String parameterName, boolean isSetter) {
		if (nameNode.getNameSize() == 1) {
			String name = nameNode.getNamePart(0);
			FieldSummary field = FieldQuery.find(typeSummary, name);
			if (field.getModifiers().isPrivate()) {
				ASTName newName = new ASTName(0);
				newName.addNamePart(getReplacementVariableName());
				newName.addNamePart(isSetter ? getFieldSetter(field) : getFieldGetter(field));
				prefix.jjtAddChild(newName, 0);

				ASTPrimarySuffix suffix = new ASTPrimarySuffix(0);
				ASTArguments args = new ASTArguments(0);
				suffix.jjtAddChild(args, 0);
				primary.jjtInsertChild(suffix, 1);

				return args;
			}
		}

		updatePrimaryPrefix(prefix, parameterName);

		return null;
	}
}
