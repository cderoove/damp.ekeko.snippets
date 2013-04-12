package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroup {
	private Object groupHistory;
	private int[] activeNodePos;
	
	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.representation"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.parsing"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.operators"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.operatorsrep"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.querying"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.rewrite"));
	}

	public SnippetGroup(String name) {
		groupHistory = RT.var("damp.ekeko.snippets.representation", "make-snippetgrouphistory").invoke(name);
		activeNodePos = new int[2];
	}
	
	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}
	
	/**
	 * 
	 * TEMPLATE SNIPPET PART
	 */
	
	public void setGroupHistory(Object grp) {
		groupHistory = grp;
	}

	public Object getGroupHistory() {
		return groupHistory;
	}
	
	public Object getGroup() {
		return RT.var("damp.ekeko.snippets.representation", "snippetgrouphistory-current").invoke(getGroupHistory());
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
	
	public String toString(Object node) {
		if (node == null)
			return toString();
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		Object[] code = getArray(RT.var("damp.ekeko.snippets.gui", "print-snippet-with-highlight").invoke(snippet, node));
		activeNodePos = (int[]) code[1];
		return code[0].toString();
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
	
	public Object addSnippetCode(String code) {
		Object document = RT.var("damp.ekeko.snippets.parsing", "parse-string-to-document").invoke(code);
		Object snippet = RT.var("damp.ekeko.snippets.representation", "document-as-snippet").invoke(document);
		groupHistory = RT.var("damp.ekeko.snippets.operators", "add-snippet-to-snippetgrouphistory").invoke(getGroupHistory(), snippet);
		return snippet;
	}

	public void applyOperator(Object operator, Object node, String[] args, Object selectedArgNode) {
		if (selectedArgNode != null) {
			Object[] targs = new Object[args.length+1];
			for (int i = 0; i < args.length; i++) {
				targs[i] = args[i];
			}		
			targs[args.length] = selectedArgNode;
			groupHistory = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), operator, node, targs);		
		} else
			groupHistory = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), operator, node, args);		
	}
	
	public void undoOperator() {
		groupHistory = RT.var("damp.ekeko.snippets.operatorsrep", "undo-operator").invoke(getGroupHistory());		
	}
	
	public void redoOperator() {
		groupHistory = RT.var("damp.ekeko.snippets.operatorsrep", "redo-operator").invoke(getGroupHistory());		
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
	
	public void removeSnippet(Object node) {
		System.out.println("delete");
		Object snippet = getSnippet(node);
		if (snippet != null)	{
			System.out.println("inside");
			groupHistory = RT.var("damp.ekeko.snippets.operators", "remove-snippet-from-snippetgrouphistory").invoke(getGroupHistory(), snippet);
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
			query = RT.var("damp.ekeko.snippets.querying","snippet-in-group-query").invoke(snippet, getGroup(), Symbol.intern("damp.ekeko/ekeko*"));
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public void runQuery(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			RT.var("damp.ekeko.snippets","query-by-snippetgroup*").invoke(getGroup()); 		
		else 
			RT.var("damp.ekeko.snippets","query-by-snippet-in-group*").invoke(snippet, getGroup());		
	}

	public Object[] getQueryResult(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			return getArray(RT.var("damp.ekeko.snippets","query-by-snippetgroup-with-header").invoke(getGroup())); 		
		else 
			return getArray(RT.var("damp.ekeko.snippets","query-by-snippet-in-group-with-header").invoke(snippet, getGroup()));
	}

	public SnippetGroup newState() {
		SnippetGroup newState = new SnippetGroup("Group");
		newState.setGroupHistory(RT.var("damp.ekeko.snippets.representation","snippetgrouphistory-new-state").invoke(getGroupHistory())); 		
		return newState;
	}
	

	
}
