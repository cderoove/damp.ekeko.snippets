package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class TemplateGroup {
	public static IFn FN_MAKE_SNIPPETGROUP;

	public static IFn FN_SNIPPETGROUP_NAME;
	public static IFn FN_SNIPPETGROUP_SNIPPET_FOR_NODE;
	public static IFn FN_SNIPPETGROUP_USERQUERY;
	public static IFn FN_SNIPPETGROUP_NEWSTATE;

	public static IFn FN_SNIPPET_ROOT;
	public static IFn FN_SNIPPET_USERQUERIES;
	public static IFn FN_SNIPPET_FROMDOCUMENT;
	
	public static IFn FN_QUERY_BY_SNIPPET;
	public static IFn FN_QUERY_BY_SNIPPETGROUP;
	public static IFn FN_QUERY_BY_SNIPPETGROUP_HEADER;
	public static IFn FN_QUERY_BY_SNIPPET_HEADER;

	public static IFn FN_SNIPPETGROUP_QUERY;
	public static IFn FN_SNIPPET_QUERY;
	
	public static IFn FN_APPLY_TO_SNIPPETGROUP;
	
	public static IFn FN_ADD_SNIPPET_TO_SNIPPETGROUP;
	public static IFn FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP;
	
	public static IFn FN_SEARCH;
	
	public static IFn FN_PARSE_TO_DOC;
	public static IFn FN_PARSE_TO_NODES;

	protected Object group;
	
	public TemplateGroup(String name) {
		group = FN_MAKE_SNIPPETGROUP.invoke(name);
	}
		
	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}
	
	
	public Object getGroup() {
		return group;
	}
	
	public String getName() {
		return (String) FN_SNIPPETGROUP_NAME.invoke(group);
	}
	
	public Object getSnippet(Object node) {
		return FN_SNIPPETGROUP_SNIPPET_FOR_NODE.invoke(group, node);		
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
	
	//TODO: can be eliminated once transformsview has been reviewed
	public String toString(Object node) {
		return node.toString();
	}
	
	public String nodeToString(Object node) {
		return node.toString();
	}
	
	//TODO: single-parameter variant -> group, other -> snippet
	public String getLogicConditions(Object node) {
		Object snippet = getSnippet(node);
		Object conds;
		
		if (snippet == null)		
			conds = FN_SNIPPETGROUP_USERQUERY.invoke(group);
		else
			conds = FN_SNIPPET_USERQUERIES.invoke(snippet);
		
		String strConds = conds.toString().replace("\n", "");
		if (strConds.contains("EmptyList"))
			return "";
		return strConds.substring(1, strConds.length() - 1).replace(") ", ") \n").replace("] ", "] \n");
	}
	
	public void applyOperator(Object operator, Object operands) {
		group = FN_APPLY_TO_SNIPPETGROUP.invoke(group, operator, operands);		
	}
	
	
	public Object addSnippetCode(String code) {
		Object document = FN_PARSE_TO_DOC.invoke(code);
		Object snippet = FN_SNIPPET_FROMDOCUMENT.invoke(document);
		group = FN_ADD_SNIPPET_TO_SNIPPETGROUP.invoke(group, snippet);
		return snippet;
	}


	public void removeSnippet(Object node) {
		Object snippet = getSnippet(node);
		if (snippet != null)	{
			group = FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP.invoke(group, snippet);
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

	public TemplateGroup newState() {
		TemplateGroup newState = new TemplateGroup("Group");
		newState.group = FN_SNIPPETGROUP_NEWSTATE.invoke(group); 		
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
