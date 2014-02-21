package damp.ekeko.snippets.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import clojure.lang.RT;

public class Groups {
	private HashMap<String, TemplateGroup> groups;
	private HashMap<String, RewrittenSnippetGroup> rewrittenGroups;
	
	public Groups() {
		groups = new HashMap<String, TemplateGroup>(); 
		rewrittenGroups = new HashMap<String, RewrittenSnippetGroup>(); 
	}
	
	public void addGroup(String name) {
		TemplateGroup group = new TemplateGroup(name); 
		groups.put(name, group);
		RewrittenSnippetGroup rwGroup = new RewrittenSnippetGroup(name); 
		rewrittenGroups.put(name, rwGroup);
	}
	
	public void removeGroup(String name) {
		groups.remove(name);
		rewrittenGroups.remove(name);
	}

	public TemplateGroup getGroup(String name) {
		return groups.get(name);
	}

	public RewrittenSnippetGroup getRewrittenGroup(TemplateGroup group) {
		return rewrittenGroups.get(group.getName());
	}
	
	public Object[] getGroups() {
		return groups.keySet().toArray();
	}
	
	public void transform(Object[] names) {
		RT.var("damp.ekeko.snippets.rewrite","reset-rewrites!").invoke();
		
		for (int i=0; i<names.length; i++) {
			TemplateGroup sGroup = groups.get(names[i].toString());
			RewrittenSnippetGroup rwGroup = rewrittenGroups.get(names[i].toString());
			rwGroup.doTransformation(sGroup);
		}
		RT.var("damp.ekeko.snippets.rewrite","apply-and-reset-rewrites").invoke(); 		
	}

	private Object[] getGroupsValues() {
		//returns array of clojure SnippetGroup
		Collection<TemplateGroup> values = groups.values();
		Object[] data = new Object[values.size()];
		
		Iterator<TemplateGroup> i = values.iterator();
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

	private HashMap<String, TemplateGroup> makeGroupsMap(Object[] data) {
		
		//TODO: refactor
		/*
		//given array of clojure SnippetGroup
		HashMap<String, TemplateGroup> groupMap = new HashMap<String, TemplateGroup>();
				
		for (int i=0; i<data.length; i++) {
			TemplateGroup sGroup = new TemplateGroup(data[i]);
			groupMap.put(sGroup.getName(), sGroup);
		}
		
		return groupMap;
		
		*/
		
		return null;
	}

	private HashMap<String, RewrittenSnippetGroup> makeRewrittenGroupsMap(Object[] data) {
		/*
		//given array of clojure SnippetGroup
		HashMap<String, RewrittenSnippetGroup> groupMap = new HashMap<String, RewrittenSnippetGroup>();
				
		for (int i=0; i<data.length; i++) {
			RewrittenSnippetGroup sGroup = new RewrittenSnippetGroup(data[i]);
			groupMap.put(sGroup.getName(), sGroup);
		}
		
		return groupMap;
		*/
		return null;
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
