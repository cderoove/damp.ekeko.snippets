package damp.ekeko.snippets.data;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetOperator {

	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.precondition"));
	}

	public SnippetOperator() {
	}

	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}
	
	public static void setInput(Tree tree, Object selectedNode) {
		tree.removeAll();

		TreeItem root = new TreeItem(tree, 0);
		root.setText("Operator");
		root.setData("");
		
		Object[] types = getArray(RT.var("damp.ekeko.snippets.precondition", "operator-types").invoke());
		for (int i = 0; i < types.length; i++) {
			TreeItem itemType = new TreeItem(root, 0);
			itemType.setText((String) RT.var("damp.ekeko.snippets.precondition", "operatortype-name").invoke(types[i]));
			itemType.setData(types[i]);
			
			Object[] operators = getArray(RT.var("damp.ekeko.snippets.precondition", "applicable-operators-with-type").invoke(types[i], selectedNode));
			for (int j = 0; j < operators.length; j++) {
				TreeItem itemOp = new TreeItem(itemType, 0);
				itemOp.setText((String) RT.var("damp.ekeko.snippets.precondition", "operator-name").invoke(operators[j]));
				itemOp.setData(operators[j]);
			}			
			itemType.setExpanded(true);
		}
		root.setExpanded(true);
	}

	public static void setInputArguments(Table table, Object operator) {
		table.removeAll();

		String[] args = getArguments(operator);
		for (int i = 0; i < args.length; i++) {
			TableItem item = new TableItem(table, 0);
			item.setText(new String[] { args[i] , "" });
		}
	}

	public static String[] getArguments(Object operator) {
		Object[] args = getArray(RT.var("damp.ekeko.snippets.precondition", "operator-arguments").invoke(operator));		
		String[] result = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			result[i] = args[i].toString();
		}			
		return result;
	}

	public static String getDescription(Object operator) {
		String result = (String) RT.var("damp.ekeko.snippets.precondition", "operator-description").invoke(operator);
		if (result == null)
			return "";
		return result;		
	}
}
