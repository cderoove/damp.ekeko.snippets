package damp.ekeko.snippets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.jface.viewers.TableViewer;

import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroup {
	/*static {	
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.parsing"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.representation"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.operators"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.precondition"));
	}*/		

	private Object group;
	private int[] activeNodePos;
	
	public SnippetGroup(String name) {
		group = RT.var("damp.ekeko.snippets.representation", "make-snippetgroup").invoke(name);
		activeNodePos = new int[2];
	}
	
	public Object getGroup() {
		return group;
	}
	
	public int[] getActiveNodePos() {
		return activeNodePos;
	}
	
	public Object getSnippet(Object node) {
		return RT.var("damp.ekeko.snippets.representation", "snippetgroup-snippet-for-node").invoke(getGroup(), node);		
	}
	
	public Object getRoot(Object node) {
		return RT.var("damp.ekeko.snippets.representation", "snippet-root").invoke(getSnippet(node));
	}

	public String toString() {
		activeNodePos[0] = 0;
		activeNodePos[1] = 0;
		String result = (String) RT.var("damp.ekeko.snippets.gui", "print-snippetgroup").invoke(getGroup());
		return result;
	}
	
	public String getLogicConditions(Object node) {
		Object snippet = getSnippet(node);
		Object conds;
		
		if (snippet == null)		
			conds = RT.var("damp.ekeko.snippets.representation", "snippetgroup-userqueries").invoke(getGroup());
		else
			conds = RT.var("damp.ekeko.snippets.representation", "snippet-userqueries").invoke(snippet);
		
		String strConds = conds.toString().replace("\n", "");
		if (strConds.contains("EmptyList"))
			return "";
		return strConds.substring(1, strConds.length() - 1).replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public String toString(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		PersistentList code = (PersistentList) RT.var("damp.ekeko.snippets.gui", "print-snippet-with-highlight").invoke(snippet, node);
		activeNodePos = (int[]) code.get(1);
		System.out.println("pos: "+activeNodePos[0]+";"+activeNodePos[1]);
		return (String) code.get(0);
	}
	
	public void addSnippetCode(String code) {
		System.out.println("inside");
		ASTNode rootNode = (ASTNode) RT.var("damp.ekeko.snippets.parsing", "parse-string-ast").invoke(code);
		Object snippet = RT.var("damp.ekeko.snippets.representation", "jdt-node-as-snippet").invoke(rootNode);
		group = RT.var("damp.ekeko.snippets.operators", "add-snippet").invoke(getGroup(), snippet);
	}

	public void applyOperator(String operator, Object node, String[] args) {
		Object snippet = getSnippet(node);
		Object newsnippet = snippet;
		
		if (args != null && args.length == 2)
			if (operator.equals("add-node")) {
				//add-node
				Object newnode = RT.var("damp.ekeko.snippets.parsing", "parse-string-ast").invoke(args[0]);
				newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node, newnode, Integer.parseInt(args[1]));
			} else
				newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node, args[0], args[1]);
		else if (args != null && args.length == 1)
			if (operator.equals("update-logic-conditions")) {
				//update-logic-conditions
				if (snippet == null)
					group = RT.var("damp.ekeko.snippets.operators", "update-logic-conditions-to-snippetgroup").invoke(getGroup(), Symbol.intern(args[0].toString().replace("\n", "")));
				else
					newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, Symbol.intern(args[0].toString().replace("\n", "")));
			} else
				//introduce-logic-variable variant
				newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node, Symbol.intern(args[0].toString()));
		else
			newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node);
			
		if (snippet != null)
			group = RT.var("damp.ekeko.snippets.representation", "snippetgroup-replace-snippet").invoke(getGroup(), snippet, newsnippet);		
	}
	
	public Object getObjectValue(Object node) {
		if (node instanceof ASTNode) {
			return node;
		} else {
			Object snippet = getSnippet(node);		
			return RT.var("damp.ekeko.snippets.representation", "snippet-value-for-node").invoke(snippet, node);		
		}
	}

	public static String getTypeValue(Object node) {
		if (node == null) {
			return "Snippet Group";
		} else if (node instanceof ASTNode) {
			return node.getClass().toString().replace("class org.eclipse.jdt.core.dom.", "");
		} else if (node instanceof PersistentArrayMap) {
			PersistentArrayMap nodeList = (PersistentArrayMap) node;
			StructuralPropertyDescriptor property = (StructuralPropertyDescriptor) nodeList.get(Keyword.intern("property"));
			return property.toString().replace("ChildListProperty[org.eclipse.jdt.core.dom.", "[");
		} else {
			return node.toString();
		}
	}
	
	public LazySeq getPossibleNodes(Object rootNode, String operator) {
		Object snippet = getSnippet(rootNode);
		if (snippet == null)		
			return (LazySeq) RT.var("damp.ekeko.snippets.precondition", "possible-nodes-for-operator-in-group").invoke(getGroup(), Keyword.intern(operator));
		else
			return (LazySeq) RT.var("damp.ekeko.snippets.precondition", "possible-nodes-for-operator").invoke(rootNode, Keyword.intern(operator));
	}
	
	public void setInputPossibleNodes(Object rootNode, TableViewer tableViewer, String operator) {
		tableViewer.getTable().removeAll();
		for (int y = 2 ; y < tableViewer.getTable().getColumnCount(); y++) {
			//delete all columns except first 2 columns
			tableViewer.getTable().getColumn(y).dispose();
		}
		
		LazySeq rows = getPossibleNodes(rootNode, operator);

		if (rows.size() > 0) {
			PersistentVector columns = (PersistentVector) rows.get(0);
			int cols = columns.size() * 2;
		    for (int i = 2; i < cols; i++) {
		        TableColumn column = new TableColumn(tableViewer.getTable(), SWT.NONE);
		        column.setWidth(150);
		        if (i % 2 == 0)
		        	column.setText("Type");
		        else
		        	column.setText("Node");
		    }
		}
		
	    for (int i = 0; i < rows.size(); i++) {
			TableItem item = new TableItem(tableViewer.getTable(), SWT.NONE);
			PersistentVector columns = (PersistentVector) rows.get(i);
			String[] str = new String[columns.size() * 2];

			int k = 0;
			for (int j = 0; j < columns.size(); j++) {
				str[k++] = getTypeValue(columns.get(j));
				str[k++] = getObjectValue(columns.get(j)).toString();
			}
			item.setText(str);
			item.setData(columns);
		}	
	}

	public void viewSnippet(Object node) {
		Object snippet = getSnippet(node);
		if (snippet != null)		
			RT.var("damp.ekeko.snippets.gui","view-snippet").invoke(snippet);		
	}

	public String getQuery(Object node) {
		Object snippet = getSnippet(node);
		Object query = "";
		if (snippet == null)		
			query = RT.var("damp.ekeko.snippets.querying","snippetgroup-query").invoke(getGroup(), Symbol.intern("damp.ekeko/ekeko*"));
		else 
			query = RT.var("damp.ekeko.snippets.querying","snippet-query").invoke(snippet, Symbol.intern("damp.ekeko/ekeko*"));
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public void runQuery(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			RT.var("damp.ekeko.snippets","query-by-snippetgroup*").invoke(getGroup()); //--> both error java class not found ...PrimitiveType		
		else 
			RT.var("damp.ekeko.snippets","query-by-snippet*").invoke(snippet);		
	}

	public static void runStringQuery(String query) {
		RT.var("(clojure.core","eval").invoke(query);		
	}
	
}
