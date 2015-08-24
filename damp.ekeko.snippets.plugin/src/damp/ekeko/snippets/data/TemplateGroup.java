package damp.ekeko.snippets.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.dom.ASTNode;

import clojure.lang.IFn;
import clojure.lang.IPersistentCollection;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class TemplateGroup {
	//Java wrapper for SnippetGroup record on the Clojure side
	
	public static IFn FN_MAKE_SNIPPETGROUP;

	public static IFn FN_SNIPPETGROUP_NAME;
	public static IFn FN_SNIPPETGROUP_SNIPPET_FOR_NODE;

	public static IFn FN_SNIPPET_ROOT;
	public static IFn FN_SNIPPET_USERQUERY;

	public static IFn FN_SNIPPETGROUP_SNIPPETS;
	
	public static IFn FN_SNIPPET_FROM_STRING;
	public static IFn FN_SNIPPET_FROM_NODE;
	
	public static IFn FN_QUERY_BY_SNIPPET;
	public static IFn FN_QUERY_BY_SNIPPETGROUP;
	public static IFn FN_QUERY_BY_SNIPPETGROUP_NOGUI;

	/*
	public static IFn FN_SNIPPETGROUP_QUERY;
	public static IFn FN_SNIPPET_QUERY;
	
	*/
	
	public static IFn FN_APPLY_TO_SNIPPETGROUP;
	
	public static IFn FN_ADD_SNIPPET_TO_SNIPPETGROUP;

	public static IFn FN_ADD_COPY_OF_SNIPPET_TO_SNIPPETGROUP;
	public static IFn FN_ADD_COPY_OF_SNIPPETGROUP_TO_SNIPPETGROUP;


	public static IFn FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP;
	
	public static IFn FN_SEARCH;
	
	public static IFn FN_PARSE_TO_NODES;
	
	public static IFn FN_UPDATE_SNIPPET_IN_SNIPPETGROUP;
	
	public static IFn FN_TRANSFORM_BY_SNIPPETGROUPS;
	
	public static IFn FN_SNIPPETGROUP_NORMALIZED_MATCH_VARS;
	
	public static IFn FN_COPY_SNIPPETGROUP;
	
	protected Object cljGroup;


	

	public static void transformBySnippetGroups(Object cljLHSGroup, Object cljRHSGroup) {
		FN_TRANSFORM_BY_SNIPPETGROUPS.invoke(cljLHSGroup, cljRHSGroup);
	} 
	
	//returns new snippet group
	public static Object updateSnippetInSnippetGroup(Object cljGroup, Object cljSnippet, IFn updater) {
		return FN_UPDATE_SNIPPET_IN_SNIPPETGROUP.invoke(cljGroup, cljSnippet, updater);
	}
	
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
	
	public static Object copyClojureTemplateGroup(Object cljGroup) {
		return FN_COPY_SNIPPETGROUP.invoke(cljGroup);
	}
	public Object copyOfClojureTemplateGroup() {
		return copyClojureTemplateGroup(cljGroup);
	}
	
	public TemplateGroup copy() {
		return newFromClojureGroup(copyOfClojureTemplateGroup());
	}
	
	public Object getGroup() {
		return cljGroup;
	}
	
	@SuppressWarnings("rawtypes")
	public List getSnippets() {
		return (List) FN_SNIPPETGROUP_SNIPPETS.invoke(cljGroup);
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
		Object snippet = FN_SNIPPET_FROM_STRING.invoke(code);
		cljGroup = FN_ADD_SNIPPET_TO_SNIPPETGROUP.invoke(cljGroup, snippet);
		return snippet;
	}

	public Object addSnippetCode(ASTNode node) {
		Object snippet = FN_SNIPPET_FROM_NODE.invoke(node);
		cljGroup = FN_ADD_SNIPPET_TO_SNIPPETGROUP.invoke(cljGroup, snippet);
		return snippet;
	}
	
	
	public void addCopyOfSnippet(Object snippet) {
		cljGroup = FN_ADD_COPY_OF_SNIPPET_TO_SNIPPETGROUP.invoke(cljGroup, snippet);
	}

	public void addCopyOfSnippetGroup(TemplateGroup snippetGroup) {
		cljGroup = FN_ADD_COPY_OF_SNIPPETGROUP_TO_SNIPPETGROUP.invoke(cljGroup, snippetGroup.getGroup());
	}


	public void removeSnippet(Object snippet) {
		cljGroup = FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP.invoke(cljGroup, snippet);
	}

	public void setClojureGroup(Object cljGroup) {
		this.cljGroup = cljGroup;
	}
	
	/*
	public String getQuery(Object node) {
		Object snippet = getSnippet(node);
		Object query = "";
		if (snippet == null)		
			query = FN_SNIPPETGROUP_QUERY.invoke(getGroup(), Symbol.intern("damp.ekeko/ekeko*"), false);
		else 
			query = FN_SNIPPET_QUERY.invoke(snippet, Symbol.intern("damp.ekeko/ekeko*"));
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}
	*/
	
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
	
	public Collection getResults() {
		return (Collection) FN_QUERY_BY_SNIPPETGROUP_NOGUI.invoke(getGroup()); 		
	}
	
	
	public Object[] searchSpace(Object[] positiveExamples, Object[] negativeExamples) {
		//return getArray(RT.var(ns_search,"dfs-snippet").invoke(getGroup(), positiveExamples, negativeExamples)); 		
		return getArray(FN_SEARCH.invoke(getGroup(), positiveExamples, negativeExamples)); 		
	}

	public static Object parseStringsToNodes(String[] arrStr) {
		return FN_PARSE_TO_NODES.invoke(arrStr); 		
	}
	
	public Collection getNormalizedMatchVariables() {
		return (Collection) FN_SNIPPETGROUP_NORMALIZED_MATCH_VARS.invoke(getGroup()); 		

	}

}
