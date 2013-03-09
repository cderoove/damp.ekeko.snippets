package damp.ekeko.snippets;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.TreeViewer;


import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentVector;
import clojure.lang.RT;

public class SnippetOperator {

	public SnippetOperator() {
	}

	public static void setInput(TreeViewer treeViewer) {
		TreeItem root = new TreeItem(treeViewer.getTree(), 0);
		root.setText("Operator");
		LazySeq operators = (LazySeq) RT.var("damp.ekeko.snippets.precondition", "operator-names").invoke();

		for (int i = 0; i < operators.size(); i++) {
			TreeItem child = new TreeItem(root, 0);
			child.setText(operators.get(i).toString().replace(":", ""));
		}			
	}

	public static String getOperatorArgumentsInformation(String operator) {
		String str = "";
		PersistentVector args = (PersistentVector) RT.var("damp.ekeko.snippets.precondition", "operator-arguments").invoke(Keyword.intern(operator));		
		
		if (args != null) {
			for (int i = 0; i < args.size(); i++) {
				str += "-" + args.get(i) + "\n";
			}	
			
			if (args.size() > 0) 
				str = "\nPlease input parameter below:\n" + str;
	
			if (args.size() > 1) 
				str += "\n(Separate each argument by \":\")\n";
		}
		
		return str;
	}
}
