package damp.ekeko.snippets.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import clojure.lang.RT;

public class Groups {
	private HashMap<String, SnippetGroupHistory> groups;
	private HashMap<String, RewrittenSnippetGroup> rewrittenGroups;
	
	public Groups() {
		groups = new HashMap<String, SnippetGroupHistory>(); 
		rewrittenGroups = new HashMap<String, RewrittenSnippetGroup>(); 
	}
	
	public void addGroup(String name) {
		SnippetGroupHistory group = new SnippetGroupHistory(name); 
		groups.put(name, group);
		RewrittenSnippetGroup rwGroup = new RewrittenSnippetGroup(name); 
		rewrittenGroups.put(name, rwGroup);
	}
	
	public void removeGroup(String name) {
		groups.remove(name);
		rewrittenGroups.remove(name);
	}

	public SnippetGroupHistory getGroup(String name) {
		return groups.get(name);
	}

	public RewrittenSnippetGroup getRewrittenGroup(SnippetGroupHistory group) {
		return rewrittenGroups.get(group.getName());
	}
	
	public Object[] getGroups() {
		return groups.keySet().toArray();
	}
	
	public void transform(Object[] names) {
		RT.var("damp.ekeko.snippets.rewrite","reset-rewrites!").invoke();
		
		for (int i=0; i<names.length; i++) {
			SnippetGroupHistory sGroup = groups.get(names[i].toString());
			RewrittenSnippetGroup rwGroup = rewrittenGroups.get(names[i].toString());
			rwGroup.doTransformation(sGroup);
		}
		RT.var("damp.ekeko.snippets.rewrite","apply-and-reset-rewrites").invoke(); 		
	}

	private Object[] getGroupsValues() {
		//returns array of clojure SnippetGroup
		Collection<SnippetGroupHistory> values = groups.values();
		Object[] data = new Object[values.size()];
		
		Iterator<SnippetGroupHistory> i = values.iterator();
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

	private HashMap<String, SnippetGroupHistory> makeGroupsMap(Object[] data) {
		//given array of clojure SnippetGroup
		HashMap<String, SnippetGroupHistory> groupMap = new HashMap<String, SnippetGroupHistory>();
				
		for (int i=0; i<data.length; i++) {
			SnippetGroupHistory sGroup = new SnippetGroupHistory(data[i]);
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
