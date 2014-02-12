package damp.ekeko.snippets.db;

//Not used, only for testing

/**
 * Convert Snippet as JSONObject 
 * and JSONObject as Snippet
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;

public class JSONSnippet {
	 
	 
	public static JSONObject mapToJson(PersistentArrayMap clojureSnippetMap) {
		HashMap snippetMap = new HashMap();
		snippetMap.putAll(clojureSnippetMap);

		JSONObject obj = new JSONObject();
		obj.putAll(snippetMap);
		
		return obj;
	}
	
	public static HashMap jsonToHashMap(JSONObject jsonObject) {
		Set data = jsonObject.entrySet();
		HashMap map = new HashMap();
		
		Iterator i = data.iterator();
	    while (i.hasNext()){
	    	Map.Entry el = (Map.Entry)i.next();
			if (el.getValue() instanceof java.lang.String) {
				map.put(el.getKey(), el.getValue());
			} else {
				map.put(el.getKey(), jsonToHashMap((JSONObject)el.getValue()));
			}
	    }
				
	    return map;
	}
 
	public static Object addSnippetCode(String code) {
		Object document = RT.var("damp.ekeko.snippets.parsing", "parse-string-to-document").invoke(code);
		Object snippet = RT.var("damp.ekeko.snippets.snippet", "document-as-snippet").invoke(document);
		return snippet;
	}

}
