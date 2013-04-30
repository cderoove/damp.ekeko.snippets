package damp.ekeko.snippets.db;

//Not used, only for testing

/**
 * Convert Snippet as JSONObject 
 * and JSONObject as Snippet
 */

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class JSONSnippet {

	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.representation"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.parsing"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.datastore"));
	}

	public JSONSnippet() {
		// TODO Auto-generated constructor stub
	}
	 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		Object snippet = addSnippetCode("int i = 0;");
		JSONObject snippetjson = (JSONObject)RT.var("damp.ekeko.snippets.datastore", "snippet-to-snippetjson").invoke(snippet);
		JSONFile.write("c:\\test.json", snippetjson);		 
		System.out.println("new : " + snippetjson.toJSONString());

		JSONObject jsonObject = JSONFile.read("c:\\test.json");
		Object newSnippet = RT.var("damp.ekeko.snippets.datastore", "snippetjson-to-snippet").invoke(jsonObject);
		System.out.println("new : " + newSnippet.toString());
	}
	 
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
		Object snippet = RT.var("damp.ekeko.snippets.representation", "document-as-snippet").invoke(document);
		return snippet;
	}

}
