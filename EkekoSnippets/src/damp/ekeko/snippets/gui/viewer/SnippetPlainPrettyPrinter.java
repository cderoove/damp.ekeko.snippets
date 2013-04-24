package damp.ekeko.snippets.gui.viewer;

import org.eclipse.jdt.core.dom.ASTNode;
import clojure.lang.Keyword;

public class SnippetPlainPrettyPrinter extends SnippetPrettyPrinter {

	public SnippetPlainPrettyPrinter () {
		super();
	}
	
	public boolean preVisit2(ASTNode node) {
		preVisit(node);

		Object uservar = getUserVar(node);
		if (uservar != null) {
			Object constrainf = getConstrainF(node);
			if (constrainf == Keyword.intern("variable") ||
				constrainf == Keyword.intern("variable-info") ||
				constrainf == Keyword.intern("change-name")) 	{
				this.buffer.append(uservar);
				return false;
			}
		}
		return true;
	}

	public void preVisit(ASTNode node) {	
	}

	public void postVisit(ASTNode node) {
	}
	
}
