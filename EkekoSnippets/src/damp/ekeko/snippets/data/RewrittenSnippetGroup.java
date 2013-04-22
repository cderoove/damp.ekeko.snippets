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
	
	public Object getRewriteSnippet(SnippetGroup sGroup, Object nodeInSnippet) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		return RT.var("damp.ekeko.snippets.rewrite","get-rewrite-snippet").invoke(getRewriteMap(), snippet); 		
	}

	public void addRewriteSnippet(SnippetGroup sGroup, Object nodeInSnippet, String code) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object document = RT.var("damp.ekeko.snippets.parsing", "parse-string-to-document").invoke(code);
		Object rewriteSnippet = RT.var("damp.ekeko.snippets.representation", "document-as-snippet").invoke(document);
		
		Object oldRewriteSnippet = getRewriteSnippet(sGroup, nodeInSnippet);
		if (oldRewriteSnippet == null)
			groupHistory = RT.var("damp.ekeko.snippets.operators", "add-snippet-to-snippetgrouphistory").invoke(getGroupHistory(), rewriteSnippet);
		else 
			groupHistory = RT.var("damp.ekeko.snippets.operators", "update-snippet-in-snippetgrouphistory").invoke(getGroupHistory(), oldRewriteSnippet, rewriteSnippet);

		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","add-rewrite-snippet").invoke(getRewriteMap(), snippet, rewriteSnippet); 		
	}
	
	private void updateRewriteSnippet(Object snippet, Object oldRewriteSnippet, Object rewriteSnippet) {
		Object oriSnippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteMap(), oldRewriteSnippet);
		if (oriSnippet != null )
			rewriteMap = RT.var("damp.ekeko.snippets.rewrite","update-rewrite-snippet").invoke(getRewriteMap(), snippet, rewriteSnippet); 		
	}
	
	public Object getRewriteImportMap() {
		return rewriteImportMap;
	}

	public Object getRewriteImportSnippet(SnippetGroup sGroup, Object nodeInSnippet) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		return RT.var("damp.ekeko.snippets.rewrite","get-rewrite-snippet").invoke(getRewriteImportMap(), snippet); 		
	}

	public void addRewriteImportSnippet(SnippetGroup sGroup, Object nodeInSnippet, String code) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object document = RT.var("damp.ekeko.snippets.parsing", "parse-string-to-document").invoke(code);
		Object rewriteSnippet = RT.var("damp.ekeko.snippets.representation", "document-as-snippet").invoke(document);
		
		Object oldRewriteSnippet = getRewriteImportSnippet(sGroup, nodeInSnippet);
		if (oldRewriteSnippet == null)
			groupHistory = RT.var("damp.ekeko.snippets.operators", "add-snippet-to-snippetgrouphistory").invoke(getGroupHistory(), rewriteSnippet);
		else 
			groupHistory = RT.var("damp.ekeko.snippets.operators", "update-snippet-in-snippetgrouphistory").invoke(getGroupHistory(), oldRewriteSnippet, rewriteSnippet);

		rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite","add-rewrite-snippet").invoke(getRewriteImportMap(), snippet, rewriteSnippet); 		
	}
	
	private void updateRewriteImportSnippet(Object snippet, Object oldRewriteSnippet, Object rewriteSnippet) {
		Object oriSnippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteImportMap(), oldRewriteSnippet);
		if (oriSnippet != null )
			rewriteImportMap = RT.var("damp.ekeko.snippets.rewrite","update-rewrite-snippet").invoke(getRewriteImportMap(), snippet, rewriteSnippet); 		
	}
	
	public Object getOriginalSnippet(Object node) {
		Object rwSnippet = getSnippet(node);
		Object snippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteMap(), rwSnippet);
		if (snippet == null )
			snippet = RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(getRewriteImportMap(), rwSnippet);
		return snippet;
	}

	public void applyOperator(Object operator, SnippetGroup sGroup, Object sNode, Object rwNode, String[] args) {
		Object snippet = sGroup.getSnippet(sNode);
		Object oldRWSnippet = getSnippet(rwNode);
		Object rwSnippet = getSnippet(rwNode);
		
		//special case
		if (operator == Keyword.intern("introduce-logic-variables-for-snippet")) {
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, new Object[] {snippet});		
		} else
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, args);		
		
		groupHistory = RT.var("damp.ekeko.snippets.operators", "update-snippet-in-snippetgrouphistory").invoke(getGroupHistory(), oldRWSnippet, rwSnippet);
		updateRewriteSnippet(snippet, oldRWSnippet, rwSnippet);
		updateRewriteImportSnippet(snippet, oldRWSnippet, rwSnippet);
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
