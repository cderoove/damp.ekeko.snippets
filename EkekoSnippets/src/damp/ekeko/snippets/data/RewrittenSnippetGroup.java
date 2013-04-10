package damp.ekeko.snippets.data;

import clojure.lang.Keyword;
import clojure.lang.RT;

public class RewrittenSnippetGroup extends SnippetGroup{
	private Object rewriteMap;
	
	public RewrittenSnippetGroup(String name) {
		super(name);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite", "make-rewritemap").invoke();
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
		Object rewriteSnippet = addSnippetCode(code);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","add-rewrite-snippet").invoke(getRewriteMap(), snippet, rewriteSnippet); 		
	}
	
	public void updateRewriteSnippet(SnippetGroup sGroup, Object nodeInSnippet, Object nodeInRewriteSnippet) {
		Object snippet = sGroup.getSnippet(nodeInSnippet);
		Object rewriteSnippet = getSnippet(nodeInRewriteSnippet);
		rewriteMap = RT.var("damp.ekeko.snippets.rewrite","update-rewrite-snippet").invoke(getRewriteMap(), snippet, rewriteSnippet); 		
	}
	
	public void applyOperator(Object operator, SnippetGroup sGroup, Object sNode, Object rwNode, String[] args) {
		Object rwRoot = getRoot(rwNode);
		//special case
		if (operator == Keyword.intern("introduce-logic-variables-for-snippet")) {
			Object snippet = sGroup.getSnippet(sNode);
			setGroupHistory(RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator-to-snippetgrouphistory").invoke(getGroupHistory(), operator, rwNode, new Object[] {snippet}));		
		} else
			applyOperator(operator, rwNode, args, null);
		updateRewriteSnippet(sGroup, sNode, rwRoot);
	}

	public String getTransformationQuery(SnippetGroup snippetGroup) {
		Object query = RT.var("damp.ekeko.snippets.rewrite","snippetgrouphistory-rewrite-query").invoke(snippetGroup.getGroupHistory(), getRewriteMap()); 		
		return query.toString().replace(") ", ") \n").replace("] ", "] \n");
	}

	public void doTransformation(SnippetGroup snippetGroup) {
		RT.var("damp.ekeko.snippets.rewrite","rewrite-query-by-snippetgrouphistory").invoke(snippetGroup.getGroupHistory(), getRewriteMap()); 		
		RT.var("damp.ekeko.snippets.rewrite","apply-and-reset-rewrites").invoke(); 		
	}

	
}
