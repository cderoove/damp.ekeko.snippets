package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroupHistory {
	private Object groupHistory;
	private int[] activeNodePos;
	
	public static IFn FN_MAKE_SNIPPETGROUPHISTORY;
	public static IFn FN_MAKE_SNIPPETGROUPHISTORY_FROM_SNIPPETGROUP;
	public static IFn FN_SNIPPETGROUPHISTORY_CURRENT;
	
	public static IFn FN_SNIPPETGROUP_NAME;
	public static IFn FN_SNIPPETGROUP_SNIPPET_FOR_NODE;
	public static IFn FN_SNIPPETGROUP_USERQUERY;
	public static IFn FN_SNIPPETGROUPHISTORY_NEWSTATE;

	public static IFn FN_SNIPPET_ROOT;
	public static IFn FN_SNIPPET_USERQUERIES;
	public static IFn FN_SNIPPET_FROMDOCUMENT;
	
	public static IFn FN_QUERY_BY_SNIPPET;
	public static IFn FN_QUERY_BY_SNIPPETGROUP;
	public static IFn FN_QUERY_BY_SNIPPETGROUP_HEADER;
	public static IFn FN_QUERY_BY_SNIPPET_HEADER;

	public static IFn FN_SNIPPETGROUP_QUERY;
	public static IFn FN_SNIPPET_QUERY;
	
	public static IFn FN_APPLY_TO_SNIPPETGROUPHISTORY;
	public static IFn FN_UNDO;
	public static IFn FN_REDO;
	
	public static IFn FN_ADD_SNIPPET_TO_SNIPPETGROUPHISTORY;
	public static IFn FN_REMOVE_SNIPPET_FROM_SNIPPETGROUPHISTORY;
	
	public static IFn FN_SEARCH;
	
	public static IFn FN_PARSE_TO_DOC;
	public static IFn FN_PARSE_TO_NODES;
	

	
	//TODO: eliminate
	public static IFn FN_PRINT_SNIPPETGROUP;
	public static IFn FN_PRINT_PLAINNODE;
	public static IFn FN_PRINT_SNIPPETHIGHLIGHT;

	

	
	public SnippetGroupHistory(String name) {
		//groupHistory = RT.var(ns_snippetgrouphistory, "make-snippetgrouphistory").invoke(name);
		groupHistory = FN_MAKE_SNIPPETGROUPHISTORY.invoke(name);
		activeNodePos = new int[2];
	}
	
	public SnippetGroupHistory(Object group) {
		//groupHistory = RT.var(ns_snippetgrouphistory, "make-snippetgrouphistory-from-snippetgroup").invoke(group);
		groupHistory = FN_MAKE_SNIPPETGROUPHISTORY_FROM_SNIPPETGROUP.invoke(group);
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
		return FN_SNIPPETGROUPHISTORY_CURRENT.invoke(getGroupHistory());
	}
	
	public String getName() {
		return (String) FN_SNIPPETGROUP_NAME.invoke(getGroup());
	}
	
	public int[] getActiveNodePos() {
		return activeNodePos;
	}
	
	public Object getSnippet(Object node) {
		return FN_SNIPPETGROUP_SNIPPET_FOR_NODE.invoke(getGroup(), node);		
	}
	
	public ASTNode getRoot(Object node) {
		return  (ASTNode)  FN_SNIPPET_ROOT.invoke(getSnippet(node));
	}

	public static ASTNode getRootOfSnippet(Object snippet) {
		return (ASTNode) FN_SNIPPET_ROOT.invoke(snippet);
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
		String result = (String) FN_PRINT_SNIPPETGROUP.invoke(getGroup());
		//System.out.println(result);
		return result;
	}
	
	
	//TODO: can be eliminated once transformsview has been reviewed
	public String toString(Object node) {
		if (node == null)
			return toString();
		Object snippet = getSnippet(node);
		if (snippet == null)
			return toString();
		Object[] code = getArray(FN_PRINT_SNIPPETHIGHLIGHT.invoke(snippet, node));
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
		String result = (String) FN_PRINT_PLAINNODE.invoke(snippet, node);
		//System.out.println(result);
		return result;
	}
	
	public String getLogicConditions(Object node) {
		Object snippet = getSnippet(node);
		Object conds;
		
		if (snippet == null)		
			conds = FN_SNIPPETGROUP_USERQUERY.invoke(getGroup());
		else
			conds = FN_SNIPPET_USERQUERIES.invoke(snippet);
		
		String strConds = conds.toString().replace("\n", "");
		if (strConds.contains("EmptyList"))
			return "";
		return strConds.substring(1, strConds.length() - 1).replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public Object addSnippetCode(String code) {
		Object document = FN_PARSE_TO_DOC.invoke(code);
		Object snippet = FN_SNIPPET_FROMDOCUMENT.invoke(document);
		groupHistory = FN_ADD_SNIPPET_TO_SNIPPETGROUPHISTORY.invoke(getGroupHistory(), snippet);
		return snippet;
	}

	public void applyOperator(Object operator, Object node, Object[] args) {
		groupHistory = FN_APPLY_TO_SNIPPETGROUPHISTORY.invoke(getGroupHistory(), operator, node, args);		
	}
	
	public void applyOperatorToNodes(Object operator, Object[] nodes, Object[] args) {
		//check special case for remove-node, should remove all nodes first before apply rewrite
		if (operator.equals(Keyword.intern("remove-node"))) {
			groupHistory = FN_APPLY_TO_SNIPPETGROUPHISTORY.invoke(getGroupHistory(), Keyword.intern("remove-nodes"), nodes, args);		
		} else {
			for (int i=0; i < nodes.length; i++) {
				applyOperator(operator, nodes[i], args);
			}
		}
	}

	public void undoOperator() {
		groupHistory = FN_UNDO.invoke(getGroupHistory());		
	}
	
	public void redoOperator() {
		groupHistory = FN_REDO.invoke(getGroupHistory());		
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
			groupHistory = FN_REMOVE_SNIPPET_FROM_SNIPPETGROUPHISTORY.invoke(getGroupHistory(), snippet);
		}
	}

	public String getQuery(Object node) {
		Object snippet = getSnippet(node);
		Object query = "";
		if (snippet == null)		
			query = FN_SNIPPETGROUP_QUERY.invoke(getGroup(), Symbol.intern("damp.ekeko/ekeko*"));
		else 
			query = FN_SNIPPET_QUERY.invoke(snippet, Symbol.intern("damp.ekeko/ekeko*"));
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public void runQuery(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			FN_QUERY_BY_SNIPPETGROUP.invoke(getGroup()); 		
		else 
			FN_QUERY_BY_SNIPPET.invoke(snippet);		
			//RT.var(ns_snippets,"query-by-snippet-in-group*").invoke(snippet, getGroup());		
	}

	public Object[] getQueryResult(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			return getArray(FN_QUERY_BY_SNIPPETGROUP_HEADER.invoke(getGroup())); 		
		else 
			return getArray(FN_QUERY_BY_SNIPPET_HEADER.invoke(snippet));
			//return getArray(RT.var(ns_snippets,"query-by-snippet-in-group-with-header").invoke(snippet, getGroup()));
	}

	public SnippetGroupHistory newState() {
		SnippetGroupHistory newState = new SnippetGroupHistory("Group");
		newState.setGroupHistory(FN_SNIPPETGROUPHISTORY_NEWSTATE.invoke(getGroupHistory())); 		
		return newState;
	}
	
	public Object[] searchSpace(Object[] positiveExamples, Object[] negativeExamples) {
		//return getArray(RT.var(ns_search,"dfs-snippet").invoke(getGroup(), positiveExamples, negativeExamples)); 		
		return getArray(FN_SEARCH.invoke(getGroup(), positiveExamples, negativeExamples)); 		
	}

	public static Object parseStringsToNodes(String[] arrStr) {
		return FN_PARSE_TO_NODES.invoke(arrStr); 		
	}
	
}
