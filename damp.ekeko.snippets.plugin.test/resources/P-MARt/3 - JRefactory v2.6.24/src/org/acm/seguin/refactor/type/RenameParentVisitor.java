package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;

/**
 *  Description of the Class 
 *
 *@author    Chris Seguin 
 */
public class RenameParentVisitor extends ChildrenVisitor {
	/**
	 *  To visit a node 
	 *
	 *@param  node  The node we are visiting 
	 *@param  data  The rename type data 
	 *@return       The rename type data 
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
		RenameTypeData rtd = (RenameTypeData) data;
		String oldName = rtd.getOld().getName();

		if (oldName.equals(node.getName())) {
			if (node.jjtGetChild(0) instanceof ASTName) {
				node.jjtAddChild(rtd.getNew(), 0);
			}
			else {
				node.jjtInsertChild(rtd.getNew(), 0);
			}
		}

		return data;
	}
}
