package damp.ekeko.snippets.gui.viewer;

import org.eclipse.jdt.core.dom.ASTNode;
import clojure.lang.Keyword;
import clojure.lang.Symbol;

public class SnippetPlainPrettyPrinter extends SnippetPrettyPrinter {

	public SnippetPlainPrettyPrinter () {
		super();
	}
	
	public boolean preVisit2(ASTNode node) {
		preVisit(node);

		Symbol uservar = getUserVar(node);
		if (uservar != null) {
			Keyword constrainf = getConstrainF(node);
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
