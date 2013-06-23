package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetOperator {

	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.operatorsrep"));
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
		
		Object[] types = getArray(RT.var("damp.ekeko.snippets.operatorsrep", "operator-types").invoke());
		for (int i = 0; i < types.length; i++) {
			TreeItem itemType = new TreeItem(root, 0);
			itemType.setText((String) RT.var("damp.ekeko.snippets.operatorsrep", "operatortype-name").invoke(types[i]));
			itemType.setData(types[i]);
			
			Object[] operators = getArray(RT.var("damp.ekeko.snippets.precondition", "applicable-operators-with-type").invoke(types[i], selectedNode));
			for (int j = 0; j < operators.length; j++) {
				TreeItem itemOp = new TreeItem(itemType, 0);
				itemOp.setText((String) RT.var("damp.ekeko.snippets.operatorsrep", "operator-name").invoke(operators[j]));
				itemOp.setData(operators[j]);
			}			
			itemType.setExpanded(true);
		}
		root.setExpanded(true);
	}

	public static void setInputForTransformation(Tree tree, Object selectedNode) {
		tree.removeAll();

		TreeItem root = new TreeItem(tree, 0);
		root.setText("Action");
		root.setData("");
		
		Object[] operators = getArray(RT.var("damp.ekeko.snippets.precondition", "applicable-operators-for-transformation").invoke(selectedNode));
		for (int j = 0; j < operators.length; j++) {
			TreeItem itemOp = new TreeItem(root, 0);
			itemOp.setText((String) RT.var("damp.ekeko.snippets.operatorsrep", "operator-name").invoke(operators[j]));
			itemOp.setData(operators[j]);
		}			
		root.setExpanded(true);
	}

	public static void setInputArguments(Table table, Table tableNode, Object snippetgroup, Object operator) {
		table.removeAll();
		tableNode.removeAll();

		String[] args = getArguments(operator);
		for (int i = 0; i < args.length; i++) {
			TableItem item = new TableItem(table, 0);
			item.setText(new String[] { args[i] , "" });
		}
		
		String arg = getArgumentWithPrecondition(operator);
		if (arg != null) {
			tableNode.getColumn(0).setText(arg);
			Object[] nodes = possibleNodesForArgument(snippetgroup, operator);
			if (nodes.length <= 0) {
				TableItem item = new TableItem(tableNode, 0);
				item.setText("No Match Node");
			} else {
				for (int i = 0; i < nodes.length; i++) {
					TableItem item = new TableItem(tableNode, 0);
					ASTNode node = (ASTNode) nodes[i];
					item.setText(new String[] {node.toString(), node.getParent().toString()});
					item.setData(nodes[i]);
				}
			}
		} 
	}

	public static String[] getArguments(Object operator) {
		Object[] args = getArray(RT.var("damp.ekeko.snippets.operatorsrep", "operator-arguments").invoke(operator));		
		String[] result = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			result[i] = args[i].toString();
		}			
		return result;
	}

	public static String getArgumentWithPrecondition(Object operator) {
		return (String) RT.var("damp.ekeko.snippets.operatorsrep", "operator-argument-with-precondition").invoke(operator);		
	}

	public static Object[] possibleNodesForArgument(Object snippetgroup, Object operator) {
		return getArray(RT.var("damp.ekeko.snippets.precondition", "possible-nodes-for-operator-argument-in-group").invoke(snippetgroup, operator));		
	}

	public static String getDescription(Object operator) {
		String result = (String) RT.var("damp.ekeko.snippets.operatorsrep", "operator-description").invoke(operator);
		if (result == null)
			return "";
		return result;		
	}

	public static boolean isTransformOperator(Object operator) {
		return (boolean) RT.var("damp.ekeko.snippets.operatorsrep","is-transform-operator?").invoke(operator);
	}

}
