package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTAllocationExpression;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTNameList;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.ContainsStatic;

/**
 *  Scan through the abstract syntax tree and replace types with a new value. 
 *
 *@author     Chris Seguin 
 *@created    September 10, 1999 
 */
public class RenameTypeVisitor extends ChildrenVisitor {
	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
		RenameTypeData rtd = (RenameTypeData) data;
		if (rtd.getOld().getNameSize() == 1) {
			String oldName = rtd.getOld().getName();
			if (oldName.equals(node.getName())) {
				String name = rtd.getNew().getName();
				node.setName(name);
			}
		}

		int index = 0;
		if (node.jjtGetChild(index) instanceof ASTName) {
			if (node.jjtGetChild(index).equals(rtd.getOld())) {
				node.jjtAddChild(rtd.getNew(), index);
			}
			index++;
		}

		if (node.jjtGetChild(index) instanceof ASTNameList) {
			ASTNameList namelist = (ASTNameList) node.jjtGetChild(index);
			for (int ndx = 0; ndx < namelist.jjtGetNumChildren(); ndx++) {
				if (namelist.jjtGetChild(ndx).equals(rtd.getOld())) {
					namelist.jjtAddChild(rtd.getNew(), ndx);
				}
			}
		}
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
		RenameTypeData rtd = (RenameTypeData) data;
		if (rtd.getOld().getNameSize() == 1) {
			String oldName = rtd.getOld().getName();
			if (oldName.equals(node.getName())) {
				String name = rtd.getNew().getName();
				node.setName(name);
			}
		}

		int index = 0;
		if (node.jjtGetChild(index) instanceof ASTNameList) {
			ASTNameList namelist = (ASTNameList) node.jjtGetChild(index);
			for (int ndx = 0; ndx < namelist.jjtGetNumChildren(); ndx++) {
				if (namelist.jjtGetChild(ndx).equals(rtd.getOld())) {
					namelist.jjtAddChild(rtd.getNew(), ndx);
				}
			}
		}

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
		RenameTypeData rtd = (RenameTypeData) data;
		if (rtd.getOld().getNameSize() == 1) {
			String oldName = rtd.getOld().getName();
			if (oldName.equals(node.getName())) {
				String name = rtd.getNew().getName();
				node.setName(name);
			}
		}

		if ((node.jjtGetNumChildren() >= 2) && 
				(node.jjtGetChild(1) instanceof ASTNameList)) {
			ASTNameList nameList = (ASTNameList) node.jjtGetChild(1);
			processExceptions(nameList, rtd);
		}

		return node.childrenAccept(this, data);
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  node  Description of Parameter 
	 *@param  data  Description of Parameter 
	 *@return       Description of the Returned Value 
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		if ((node.jjtGetNumChildren() >= 3) && 
				(node.jjtGetChild(2) instanceof ASTNameList)) {
			ASTNameList nameList = (ASTNameList) node.jjtGetChild(2);
			RenameTypeData rtd = (RenameTypeData) data;
			processExceptions(nameList, rtd);
		}

		return super.visit(node, data);
	}


	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTType node, Object data) {
		if (node.jjtGetNumChildren() == 0) {
			return data;
		}

		if (node.jjtGetChild(0) instanceof ASTName) {
			ASTName child = (ASTName) node.jjtGetChild(0);

			RenameTypeData rtd = (RenameTypeData) data;
			if (child.equals(rtd.getOld())) {
				node.jjtAddChild(rtd.getNew(), 0);
			}
			else if (child.startsWith(rtd.getOld())) {
				node.jjtAddChild(child.changeStartingPart(rtd.getOld(), rtd.getNew()), 0);
			}
		}

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
		ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node.jjtGetChild(0);

		if (prefix.jjtGetNumChildren() == 0) {
			return data;
		}

		if (prefix.jjtGetChild(0) instanceof ASTName) {
			ASTName child = (ASTName) prefix.jjtGetChild(0);

			RenameTypeData rtd = (RenameTypeData) data;
			if (child.equals(rtd.getOld())) {
				prefix.jjtAddChild(rtd.getNew(), 0);
			}
			else if (child.startsWith(rtd.getOld())) {
				if (isStatic(rtd, child, isMethod(node))) {
					ASTName name = child.changeStartingPart(rtd.getOld(), rtd.getNew());
					prefix.jjtAddChild(name, 0);
				}
			}
		}

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
		if (node.jjtGetNumChildren() == 0) {
			return data;
		}

		if (node.jjtGetChild(0) instanceof ASTName) {
			ASTName child = (ASTName) node.jjtGetChild(0);

			RenameTypeData rtd = (RenameTypeData) data;
			if (child.equals(rtd.getOld())) {
				node.jjtAddChild(rtd.getNew(), 0);
			}
			else if (child.startsWith(rtd.getOld())) {
				node.jjtAddChild(child.changeStartingPart(rtd.getOld(), rtd.getNew()), 0);
			}
		}

		return node.childrenAccept(this, data);
	}


	/**
	 *  Determines if the node is a method invocation 
	 *
	 *@param  node  the node in question 
	 *@return       true when we are looking at a method 
	 */
	private boolean isMethod(ASTPrimaryExpression node) {
		if (node.jjtGetNumChildren() <= 1) {
			return false;
		}

		ASTPrimarySuffix suffix = (ASTPrimarySuffix) node.jjtGetChild(1);
		return (suffix.jjtGetChild(0) instanceof ASTArguments);
	}


	/**
	 *  Gets the Static attribute of the RenameTypeVisitor object 
	 *
	 *@param  data      Description of Parameter 
	 *@param  name      Description of Parameter 
	 *@param  isMethod  Description of Parameter 
	 *@return           The Static value 
	 */
	private boolean isStatic(RenameTypeData data, ASTName name, boolean isMethod) {
		int last = name.getNameSize();
		String value = name.getNamePart(last - 1);
		TypeSummary typeSummary = data.getTypeSummary();
		return ContainsStatic.query(typeSummary, value, isMethod);
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  nameList  Description of Parameter 
	 *@param  rtd       Description of Parameter 
	 */
	private void processExceptions(ASTNameList nameList, RenameTypeData rtd) {
		int last = nameList.jjtGetNumChildren();

		for (int ndx = 0; ndx < last; ndx++) {
			ASTName next = (ASTName) nameList.jjtGetChild(ndx);

			if (next.equals(rtd.getOld())) {
				nameList.jjtAddChild(rtd.getNew(), 0);
			}
			else if (next.startsWith(rtd.getOld())) {
				nameList.jjtAddChild(next.changeStartingPart(rtd.getOld(), rtd.getNew()), 0);
			}
		}
	}
}
