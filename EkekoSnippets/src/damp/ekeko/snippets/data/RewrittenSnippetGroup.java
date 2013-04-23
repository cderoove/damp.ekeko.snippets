package damp.ekeko.snippets.data;

import clojure.lang.Keyword;
import clojure.lang.RT;

public class RewrittenSnippetGroup extends SnippetGroup{
	private Object rewriteMap;
	private Object rewriteImportMap;
	
	public RewrittenSnippetGroup(String name) {
		super(name);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite", "make-rewritemap").invoke();
		rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite", "make-rewritemap").invoke();
	}
	

	/**
	 * 
	 * REWRITE SNIPPET PART
	 */
	
	public Object getRewriteMap() {
		return rewriteMap;
	}
	
	public Object getRewriteImportMap() {
		return rewriteImportMap;
	}

	public void addRewriteSnippet(SnippetGroup sGroup, Object nodeInSnippet, String code) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object rewriteSnippet = addSnippetCode(code);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","add-rewrite-snippet").invoke(getRewriteMap(), rewriteSnippet, snippet); 		
	}
	
	public void addRewriteImportSnippet(SnippetGroup sGroup, Object nodeInSnippet, String code) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object rewriteSnippet = addSnippetCode(code);
		rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite","add-rewrite-snippet").invoke(getRewriteImportMap(), rewriteSnippet, snippet); 		
	}
	
	public Object[] getRewriteSnippets(SnippetGroup sGroup, Object nodeInSnippet) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object[] rwSnippets = getArray(RT.var("damp.ekeko.snippets.rewrite","get-rewrite-snippets").invoke(getRewriteMap(), snippet)); 		
		Object[] rwImportSnippets = getArray(RT.var("damp.ekeko.snippets.rewrite","get-rewrite-snippets").invoke(getRewriteImportMap(), snippet));
		return getArray(RT.var("clojure.core","concat").invoke(rwSnippets, rwImportSnippets)); 		
	}

	private void updateRewriteSnippet(Object oldRewriteSnippet, Object rewriteSnippet) {
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","update-rewrite-snippet").invoke(getRewriteMap(), oldRewriteSnippet, rewriteSnippet); 		
		rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite","update-rewrite-snippet").invoke(getRewriteImportMap(), oldRewriteSnippet, rewriteSnippet); 		
	}
	
	public void removeRewriteSnippet(Object nodeInSnippet) {
		Object rwSnippet = getSnippet(nodeInSnippet);
		removeSnippet(nodeInSnippet);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","remove-rewrite-snippet").invoke(getRewriteMap(), rwSnippet); 		
		rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite","remove-rewrite-snippet").invoke(getRewriteImportMap(), rwSnippet); 		
	}
	
	public Object getOriginalSnippet(Object node) {
		Object rwSnippet = getSnippet(node);
		Object snippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteMap(), rwSnippet);
		if (snippet == null )
			snippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteImportMap(), rwSnippet);
		return snippet;
	}

	public void applyOperator(Object operator, Object rwNode, String[] args) {
		Object oldRWSnippet = getSnippet(rwNode);
		Object rwSnippet = null;
		Object snippet = getOriginalSnippet(rwNode);
		
		//special case
		if (operator == Keyword.intern("introduce-logic-variables-for-snippet")) {
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, new Object[] {snippet});		
		} else
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, args);		
		
		setGroupHistory(RT.var("damp.ekeko.snippets.operators", "update-snippet-in-snippetgrouphistory").invoke(getGroupHistory(), oldRWSnippet, rwSnippet));
		updateRewriteSnippet(oldRWSnippet, rwSnippet);
	}

	public String getTransformationQuery(SnippetGroup snippetGroup) {
		Object query = RT.var("damp.ekeko.snippets.rewrite","snippetgrouphistory-rewrite-query").invoke(snippetGroup.getGroupHistory(), getRewriteMap()); 		
		Object queryImport = RT.var("damp.ekeko.snippets.rewrite","snippetgrouphistory-rewrite-import-declaration-query").invoke(snippetGroup.getGroupHistory(), getRewriteImportMap());
		String result = "";
		if (query != null)
			result += query.toString().replace(") ", ") \n").replace("] ", "] \n");
		if (queryImport != null)
			result += queryImport.toString().replace(") ", ") \n").replace("] ", "] \n");
		return result;
	}

	public void doTransformation(SnippetGroup snippetGroup) {
		RT.var("damp.ekeko.snippets.rewrite","rewrite-query-by-snippetgrouphistory").invoke(snippetGroup.getGroupHistory(), getRewriteMap(), getRewriteImportMap()); 		
		RT.var("damp.ekeko.snippets.rewrite","apply-and-reset-rewrites").invoke(); 		
	}

	
}
