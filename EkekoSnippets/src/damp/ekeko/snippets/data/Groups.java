package damp.ekeko.snippets.data;

import java.util.HashMap;

import clojure.lang.RT;
import clojure.lang.Symbol;

public class Groups {
	private HashMap<String, SnippetGroup> groups;
	private HashMap<String, RewrittenSnippetGroup> rewrittenGroups;
	
	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.rewrite"));
	}

	public Groups() {
		groups = new HashMap<String, SnippetGroup>(); 
		rewrittenGroups = new HashMap<String, RewrittenSnippetGroup>(); 
	}
	
	public void addGroup(String name) {
		SnippetGroup group = new SnippetGroup(name); 
		groups.put(name, group);
		RewrittenSnippetGroup rwGroup = new RewrittenSnippetGroup(name); 
		rewrittenGroups.put(name, rwGroup);
	}
	
	public void removeGroup(String name) {
		groups.remove(name);
		rewrittenGroups.remove(name);
	}

	public SnippetGroup getGroup(String name) {
		return groups.get(name);
	}

	public RewrittenSnippetGroup getRewrittenGroup(SnippetGroup group) {
		return rewrittenGroups.get(group.getName());
	}

	public Object[] getGroups() {
		return groups.keySet().toArray();
	}
	
	public void transform() {
		/*
		RewrittenSnippetGroup[] arrRWGroups = (RewrittenSnippetGroup[]) groups.values().toArray();
		for (int i=0; i<arrRWGroups.length; i++) {
			arrRWGroups[i].doTransformation(getGroup(arrRWGroups[i].getName()));
		}
		*/
		RT.var("damp.ekeko.snippets.rewrite","apply-and-reset-rewrites").invoke(); 		
	}
}
