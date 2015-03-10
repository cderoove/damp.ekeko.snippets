package org.acm.seguin.tools.international;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTLiteral;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;

/**
 *  Creates a list of strings in the directory that aren't used for internal
 *  information.
 *
 *@author    Chris Seguin
 */
public class StringListVisitor extends ChildrenVisitor {
	/**
	 *  Prints out the literal if it is a string literal
	 *
	 *@param  node  The node we are visiting
	 *@param  data  The rename type data
	 *@return       The rename type data
	 */
	public Object visit(ASTLiteral node, Object data) {
		String name = node.getName();
		if ((name != null) && (name.length() > 0) && (name.charAt(0) == '\"') && !name.equals("\"\"")) {
			System.out.println("\t" + name);
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
		if (prefix.jjtGetChild(0) instanceof ASTName) {
			ASTName name = (ASTName) prefix.jjtGetChild(0);
			int count = name.getNameSize();
			if (name.getNamePart(0).equals("Debug")) {
				return data;
			}
			else {
				String part = name.getNamePart(count - 1);
				if (part.equals("getBundle")) {
					return data;
				}
				else if (part.equals("getCachedBundle")) {
					return data;
				}
				else if (part.equals("getString")) {
					return data;
				}
			}
		}
		return node.childrenAccept(this, data);
	}
}
