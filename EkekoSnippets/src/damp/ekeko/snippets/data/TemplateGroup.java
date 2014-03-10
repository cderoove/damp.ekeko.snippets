package damp.ekeko.snippets.data;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.dom.ASTNode;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class TemplateGroup {
	//Java wrapper for SnippetGroup record on the Clojure side
	
	public static IFn FN_MAKE_SNIPPETGROUP;

	public static IFn FN_SNIPPETGROUP_NAME;
	public static IFn FN_SNIPPETGROUP_SNIPPET_FOR_NODE;

	public static IFn FN_SNIPPET_ROOT;
	public static IFn FN_SNIPPET_USERQUERY;
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

	protected Object cljGroup;
	
	public static TemplateGroup newFromClojureGroup(Object cljGroup) {
		TemplateGroup group = new TemplateGroup();
		group.cljGroup = cljGroup;
		return group;
	}
	
	public static TemplateGroup newFromGroupName(String name) {
		TemplateGroup group = new TemplateGroup();
		group.cljGroup = FN_MAKE_SNIPPETGROUP.invoke(name);
		return group;
	}
	
	private TemplateGroup() {
	}
	
	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}
	
	
	public Object getGroup() {
		return cljGroup;
	}
	
	public String getName() {
		return (String) FN_SNIPPETGROUP_NAME.invoke(cljGroup);
	}
	
	public Object getSnippet(Object node) {
		return FN_SNIPPETGROUP_SNIPPET_FOR_NODE.invoke(cljGroup, node);		
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
	
	@SuppressWarnings("rawtypes")
	public Collection getLogicConditions(Object snippet) {
		return (Collection) FN_SNIPPET_USERQUERY.invoke(snippet);
	}
	
	public void applyOperator(Object operator, Object operands) {
		cljGroup = FN_APPLY_TO_SNIPPETGROUP.invoke(cljGroup, operator, operands);		
	}
	
	
	public Object addSnippetCode(String code) {
		Object document = FN_PARSE_TO_DOC.invoke(code);
		Object snippet = FN_SNIPPET_FROMDOCUMENT.invoke(document);
		cljGroup = FN_ADD_SNIPPET_TO_SNIPPETGROUP.invoke(cljGroup, snippet);
		return snippet;
	}


	public void removeSnippet(Object snippet) {
		cljGroup = FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP.invoke(cljGroup, snippet);
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
	
	public void runQuery(final Object node) {
		Job job = new Job("Matching template") {
			protected IStatus run(final IProgressMonitor m) {
				m.beginTask("Evaluating corresponding query", 1);
				Object snippet = getSnippet(node);
				if (snippet == null)		
					FN_QUERY_BY_SNIPPETGROUP.invoke(getGroup()); 		
				else 
					FN_QUERY_BY_SNIPPET.invoke(snippet);		
				//RT.var(ns_snippets,"query-by-snippet-in-group*").invoke(snippet, getGroup());		
				m.worked(1);
				m.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public Object[] getQueryResult(Object node) {
		Object snippet = getSnippet(node);
		if (snippet == null)		
			return getArray(FN_QUERY_BY_SNIPPETGROUP_HEADER.invoke(getGroup())); 		
		else 
			return getArray(FN_QUERY_BY_SNIPPET_HEADER.invoke(snippet));
			//return getArray(RT.var(ns_snippets,"query-by-snippet-in-group-with-header").invoke(snippet, getGroup()));
	}
	
	public Object[] searchSpace(Object[] positiveExamples, Object[] negativeExamples) {
		//return getArray(RT.var(ns_search,"dfs-snippet").invoke(getGroup(), positiveExamples, negativeExamples)); 		
		return getArray(FN_SEARCH.invoke(getGroup(), positiveExamples, negativeExamples)); 		
	}

	public static Object parseStringsToNodes(String[] arrStr) {
		return FN_PARSE_TO_NODES.invoke(arrStr); 		
	}
	
}
