package damp.ekeko.snippets.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import clojure.lang.RT;
import clojure.lang.Symbol;

public class Groups {
	private HashMap<String, SnippetGroup> groups;
	private HashMap<String, RewrittenSnippetGroup> rewrittenGroups;
	
	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.rewrite"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.datastore"));
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

	private Object[] getGroupsValues() {
		//returns array of clojure SnippetGroup
		Collection<SnippetGroup> values = groups.values();
		Object[] data = new Object[values.size()];
		
		Iterator<SnippetGroup> i = values.iterator();
		int j = 0;
		while (i.hasNext()) {
			data[j] = i.next().getGroup();
			j++;
		}
		
		return data;
	}

	private Object[] getRewrittenGroupsValues() {
		//returns array of clojure SnippetGroup
		Collection<RewrittenSnippetGroup> values = rewrittenGroups.values();
		Object[] data = new Object[values.size()];
		
		Iterator<RewrittenSnippetGroup> i = values.iterator();
		int j = 0;
		while (i.hasNext()) {
			data[j] = i.next().getGroup();
			j++;
		}
		
		return data;
	}

	private HashMap<String, SnippetGroup> makeGroupsMap(Object[] data) {
		//given array of clojure SnippetGroup
		HashMap<String, SnippetGroup> groupMap = new HashMap<String, SnippetGroup>();
				
		for (int i=0; i<data.length; i++) {
			SnippetGroup sGroup = new SnippetGroup(data[i]);
			groupMap.put(sGroup.getName(), sGroup);
		}
		
		return groupMap;
	}

	private HashMap<String, RewrittenSnippetGroup> makeRewrittenGroupsMap(Object[] data) {
		//given array of clojure SnippetGroup
		HashMap<String, RewrittenSnippetGroup> groupMap = new HashMap<String, RewrittenSnippetGroup>();
				
		for (int i=0; i<data.length; i++) {
			RewrittenSnippetGroup sGroup = new RewrittenSnippetGroup(data[i]);
			groupMap.put(sGroup.getName(), sGroup);
		}
		
		return groupMap;
	}

	public void save(String filename) {
		RT.var("damp.ekeko.snippets.datastore","save-groups").invoke(filename, getGroupsValues(), getRewrittenGroupsValues()); 		
	}

	public void load(String filename) {
		Object[] data = getArray(RT.var("damp.ekeko.snippets.datastore","load-groups").invoke(filename));
		groups = makeGroupsMap(getArray(data[0]));
		rewrittenGroups = makeRewrittenGroupsMap(getArray(data[1]));
	}

	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}
}
