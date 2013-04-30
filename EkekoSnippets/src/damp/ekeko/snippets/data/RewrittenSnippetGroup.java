package damp.ekeko.snippets.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class RewrittenSnippetGroup extends SnippetGroup{
	
	public RewrittenSnippetGroup(String name) {
		super(name);
	}
	
	public RewrittenSnippetGroup(Object group) {
		//given clojure SnippetGroup
		super(group);
	}

	/**
	 * 
	 * REWRITE SNIPPET PART
	 */
	
	public Object getOriginalSnippet(SnippetGroup sGroup, Object rwSnippet) {
		return RT.var("damp.ekeko.snippets.rewrite","get-original-snippet").invoke(sGroup.getGroup(), rwSnippet);
	}

	public void applyOperator(Object operator, SnippetGroup sGroup, Object sNode, Object rwNode, String[] args) {
		Object snippet = sGroup.getSnippet(sNode);
		Object oldRWSnippet = getSnippet(rwNode);
		Object rwSnippet = null;
		
		//special case
		if (operator == Keyword.intern("introduce-logic-variables-for-snippet")) {
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, new Object[] {snippet});		
		} else if (operator == Keyword.intern("change-name")) {
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, new Object[] {snippet, sNode, args[0]});		
		} else if (SnippetOperator.isTransformOperator(operator)) {
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, new Object[] {snippet, sNode});		
		} else 
			rwSnippet = RT.var("damp.ekeko.snippets.operatorsrep", "apply-operator").invoke(oldRWSnippet, operator, rwNode, args);		
		
		setGroupHistory(RT.var("damp.ekeko.snippets.operators", "update-snippet-in-snippetgrouphistory").invoke(getGroupHistory(), oldRWSnippet, rwSnippet));
	}

	public String getTransformationQuery(SnippetGroup sGroup) {
		Object query = RT.var("damp.ekeko.snippets.querying","snippetgroup-rewrite-query").invoke(sGroup.getGroup(), getGroup(), Symbol.intern("damp.ekeko/ekeko")); 		
		if (query != null)
			return query.toString().replace(") ", ") \n").replace("] ", "] \n");
		return "";
	}

	public void doTransformation(SnippetGroup sGroup) {
		RT.var("damp.ekeko.snippets","query-rewrite-by-snippetgroup").invoke(sGroup.getGroup(), getGroup()); 		
	}

	public void setTableRW(Table table, SnippetGroup sGroup) {
		table.removeAll();
		Object[] mapping = getArray(RT.var("damp.ekeko.snippets.rewrite","snippetgroup-rewrite-mapping").invoke(sGroup.getGroup(), getGroup()));

		for (int i = 0; i < mapping.length; i++) {
			Object[] oneMapping = getArray(mapping[i]);
			TableItem item = new TableItem(table, 0);
			item.setText(new String[] { oneMapping[0].toString() , oneMapping[1].toString(), oneMapping[2].toString() });
		}
		
	}
	
}
