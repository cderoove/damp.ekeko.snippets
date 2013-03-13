package damp.ekeko.snippets;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentVector;
import clojure.lang.RT;

public class SnippetOperator {

	public SnippetOperator() {
	}

	public static void setInput(Tree tree) {
		TreeItem root = new TreeItem(tree, 0);
		root.setText("Operator");
		LazySeq operators = (LazySeq) RT.var("damp.ekeko.snippets.precondition", "operator-names").invoke();

		for (int i = 0; i < operators.size(); i++) {
			TreeItem child = new TreeItem(root, 0);
			child.setText(operators.get(i).toString().replace(":", ""));
		}			
	}

	public static String[] getOperatorArguments(String operator) {
		PersistentVector args = (PersistentVector) RT.var("damp.ekeko.snippets.precondition", "operator-arguments").invoke(Keyword.intern(operator));		
		if (args != null) {
			String[] result = new String[args.size()];
			for (int i = 0; i < args.size(); i++) {
				result[i] = args.get(i).toString();
			}			
			return result;
		}
		return null;
	}
}
