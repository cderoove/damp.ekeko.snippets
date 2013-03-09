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
	
	public SnippetGroup(String name) {
		group = RT.var("damp.ekeko.snippets.representation", "make-snippetgroup").invoke(name);
	}
	
	public Object getGroup() {
		return group;
	}
	
	public Object getSnippet(Object node) {
		return RT.var("damp.ekeko.snippets.representation", "snippetgroup-snippet-for-node").invoke(getGroup(), node);		
	}
	
	public String toString() {
		String result = (String) RT.var("damp.ekeko.snippets.representation", "print-snippetgroup").invoke(getGroup());
		return result;
	}
	
	public String toString(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		return (String) RT.var("damp.ekeko.snippets.representation", "print-snippet").invoke(snippet);
	}
	
	public void addSnippetCode(String code) {
		System.out.println("inside");
		ASTNode rootNode = (ASTNode) RT.var("damp.ekeko.snippets.parsing", "parse-string-ast").invoke(code);
		Object snippet = RT.var("damp.ekeko.snippets.representation", "jdt-node-as-snippet").invoke(rootNode);
		group = RT.var("damp.ekeko.snippets.operators", "add-snippet").invoke(getGroup(), snippet);
	}

	public void applyOperator(String operator, Object node, Object[] args) {
		Object snippet = getSnippet(node);
		Object newsnippet = snippet;
		System.out.println(args.length);
		
		if (args.length == 2)
			//add-node
			newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node, args[0], args[1]);
		else if (args.length == 1 && !args[0].toString().isEmpty())
			//introduce-logic-variable variant
			newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node, Symbol.intern(args[0].toString()));
		else
			newsnippet = RT.var("damp.ekeko.snippets.operators", operator).invoke(snippet, node);
			
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
		if (node instanceof ASTNode) {
			return node.getClass().toString().replace("class org.eclipse.jdt.core.dom.", "");
		} else {
			PersistentArrayMap nodeList = (PersistentArrayMap) node;
			StructuralPropertyDescriptor property = (StructuralPropertyDescriptor) nodeList.get(Keyword.intern("property"));
			return property.toString().replace("ChildListProperty[org.eclipse.jdt.core.dom.", "[");
		}
	}
	
	public void runQuery() {
		RT.var("damp.ekeko.snippets","query-by-snippetgroup*").invoke(getGroup());		
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

}
