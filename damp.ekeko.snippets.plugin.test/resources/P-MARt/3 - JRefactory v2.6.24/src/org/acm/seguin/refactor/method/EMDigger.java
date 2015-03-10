package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTBlock;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;

class EMDigger {
	Node dig(ASTMethodDeclaration start) {
		ASTBlock block = (ASTBlock) start.jjtGetChild(start.jjtGetNumChildren() - 1);
		Node current = block.jjtGetChild(0);
		while (current.jjtGetNumChildren() == 1) {
			current = current.jjtGetChild(0);
		}
		return current;
	}

	Node last(ASTMethodDeclaration start) {
		ASTBlock block = (ASTBlock) start.jjtGetChild(start.jjtGetNumChildren() - 1);
		return block;
	}
}
