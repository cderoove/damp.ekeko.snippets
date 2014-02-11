package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroupHistory {
	private Object groupHistory;
	private int[] activeNodePos;
	
	
	private static String ns_snippetgrouphistory = "damp.ekeko.snippets.snippetgrouphistory";
	private static String ns_snippetgroup = "damp.ekeko.snippets.snippetgroup";
	private static String ns_snippet = "damp.ekeko.snippets.snippet";
	private static String ns_snippets = "damp.ekeko.snippets";
	private static String ns_gui = "damp.ekeko.snippets.gui";
	private static String ns_querying = "damp.ekeko.snippets.querying";
	private static String ns_operatorsrep = "damp.ekeko.snippets.operatorsrep";
	private static String ns_operators = "damp.ekeko.snippets.operators";
	private static String ns_search = "damp.ekeko.snippets.searchspace";
	private static String ns_parsing = 	"damp.ekeko.snippets.parsing";

	

	public SnippetGroupHistory(String name) {
		groupHistory = RT.var(ns_snippetgrouphistory, "make-snippetgrouphistory").invoke(name);
		activeNodePos = new int[2];
	}
	
	public SnippetGroupHistory(Object group) {
		groupHistory = RT.var(ns_snippetgrouphistory, "make-snippetgrouphistory-from-snippetgroup").invoke(group);
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
		return RT.var(ns_snippetgrouphistory, "snippetgrouphistory-current").invoke(getGroupHistory());
	}
	
	public String getName() {
		return (String) RT.var(ns_snippetgroup, "snippetgroup-name").invoke(getGroup());
	}
	
	public int[] getActiveNodePos() {
		return activeNodePos;
	}
	
	public Object getSnippet(Object node) {
		return RT.var(ns_snippetgroup, "snippetgroup-snippet-for-node").invoke(getGroup(), node);		
	}
	
	public Object getRoot(Object node) {
		return RT.var(ns_snippet, "snippet-root").invoke(getSnippet(node));
	}

	public static ASTNode getRootOfSnippet(Object snippet) {
		return (ASTNode) RT.var(ns_snippet, "snippet-root").invoke(snippet);
	}

	public Object[] getRootOfSnippets(Object[] snippets) {
		Object[] roots = new Object[snippets.length];
		for (int i=0; i<snippets.length; i++)
			roots[i] = getRootOfSnippet(snippets[i]);
		return roots;
	}

	public String toString() {
		activeNodePos[0] = 0;
		activeNodePos[1] = 0;
		String result = (String) RT.var(ns_gui, "print-snippetgroup").invoke(getGroup());
		//System.out.println(result);
		return result;
	}
	
	
	//TODO: can be eliminated
	public String toString(Object node) {
		if (node == null)
			return toString();
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		Object[] code = getArray(RT.var(ns_gui, "print-snippet-with-highlight").invoke(snippet, node));
		activeNodePos = (int[]) code[1];
		//System.out.println(code[0]);
		return code[0].toString();
	}
	
	public String nodeToString(Object node) {
		if (node == null)
			return toString();
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		String result = (String) RT.var(ns_gui, "print-plain-node").invoke(snippet, node);
		//System.out.println(result);
		return result;
	}
	
	public String getLogicConditions(Object node) {
		Object snippet = getSnippet(node);
		Object conds;
		
		if (snippet == null)		
			conds = RT.var(ns_snippetgroup, "snippetgroup-userquery").invoke(getGroup());
		else
			conds = RT.var(ns_snippet, "snippet-userqueries").invoke(snippet);
		
		String strConds = conds.toString().replace("\n", "");
		if (strConds.contains("EmptyList"))
			return "";
		return strConds.substring(1, strConds.length() - 1).replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public Object addSnippetCode(String code) {
		Object document = RT.var(ns_parsing, "parse-string-to-document").invoke(code);
		Object snippet = RT.var(ns_snippet, "document-as-snippet").invoke(document);
		groupHistory = RT.var(ns_operators, "add-snippet-to-snippetgrouphistory").invoke(getGroupHistory(), snippet);
		return snippet;
	}

	public void applyOperator(Object operator, Object node, String[] args, Object selectedArgNode) {
		if (selectedArgNode != null) {
			Object[] targs = new Object[args.length+1];
			for (int i = 0; i < args.length; i++) {
				targs[i] = args[i];
			}		
			targs[args.length] = selectedArgNode;
			groupHistory = RT.var(ns_operatorsrep, "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), operator, node, targs);		
		} else
			groupHistory = RT.var(ns_operatorsrep, "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), operator, node, args);		
	}
	
	public void applyOperatorToNodes(Object operator, Object[] nodes, String[] args, Object selectedArgNode) {
		//check special case for remove-node, should remove all nodes first before apply rewrite
		if (operator.equals(Keyword.intern("remove-node"))) {
			groupHistory = RT.var(ns_operatorsrep, "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), Keyword.intern("remove-nodes"), nodes, args);		
		} else {
			for (int i=0; i < nodes.length; i++) {
				applyOperator(operator, nodes[i], args, selectedArgNode);
			}
		}
	}

	public void undoOperator() {
		groupHistory = RT.var(ns_operatorsrep, "undo-operator").invoke(getGroupHistory());		
	}
	
	public void redoOperator() {
		groupHistory = RT.var(ns_operatorsrep, "redo-operator").invoke(getGroupHistory());		
	}
	
	public Object getObjectValue(Object node) {
		if (node instanceof ASTNode) {
			return node;
		} else {
			Object snippet = getSnippet(node);		
			return RT.var(ns_snippet, "snippet-value-for-node").invoke(snippet, node);		
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
		Object snippet = getSnippet(node);
		if (snippet != null)	{
			groupHistory = RT.var(ns_operators, "remove-snippet-from-snippetgrouphistory").invoke(getGroupHistory(), snippet);
		}
	}

	public String getQuery(Object node) {
		Object snippet = getSnippet(node);
		Object query = "";
		if (snippet == null)		
			query = RT.var(ns_querying,"snippetgroup-query").invoke(getGroup(), Symbol.intern("damp.ekeko/ekeko*"));
		else 
			query = RT.var(ns_querying,"snippet-query").invoke(snippet, Symbol.intern("damp.ekeko/ekeko*"));
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public void runQuery(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			RT.var(ns_snippets,"query-by-snippetgroup*").invoke(getGroup()); 		
		else 
			RT.var(ns_snippets,"query-by-snippet*").invoke(snippet);		
			//RT.var(ns_snippets,"query-by-snippet-in-group*").invoke(snippet, getGroup());		
	}

	public Object[] getQueryResult(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			return getArray(RT.var(ns_snippets,"query-by-snippetgroup-with-header").invoke(getGroup())); 		
		else 
			return getArray(RT.var(ns_snippets,"query-by-snippet-with-header").invoke(snippet));
			//return getArray(RT.var(ns_snippets,"query-by-snippet-in-group-with-header").invoke(snippet, getGroup()));
	}

	public SnippetGroupHistory newState() {
		SnippetGroupHistory newState = new SnippetGroupHistory("Group");
		newState.setGroupHistory(RT.var(ns_snippetgroup,"snippetgrouphistory-new-state").invoke(getGroupHistory())); 		
		return newState;
	}
	
	public Object[] searchSpace(Object[] positiveExamples, Object[] negativeExamples) {
		//return getArray(RT.var(ns_search,"dfs-snippet").invoke(getGroup(), positiveExamples, negativeExamples)); 		
		return getArray(RT.var(ns_search,"iterate-snippet").invoke(getGroup(), positiveExamples, negativeExamples)); 		
	}

	public static Object parseStringsToNodes(String[] arrStr) {
		return RT.var(ns_parsing,"parse-strings-to-nodes").invoke(arrStr); 		
	}
	
}