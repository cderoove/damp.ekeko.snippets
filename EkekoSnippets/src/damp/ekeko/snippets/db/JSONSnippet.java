package damp.ekeko.snippets.db;

/**
 * Convert Snippet as JSONObject 
 * and JSONObject as Snippet
 */

import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONSnippet {

	public JSONSnippet() {
		// TODO Auto-generated constructor stub
	}
	 
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JSONObject obj = new JSONObject();
		obj.put("name", "vub");
		obj.put("age", new Integer(100));
		 
		JSONArray list = new JSONArray();
		list.add("msg 1");
		list.add("msg 2");
		list.add("msg 3");
		 
		obj.put("messages", list);
		
		JSONFile.write("c:\\test.json", obj);
		 
		JSONObject jsonObject = JSONFile.read("c:\\test.json");
		String name = (String) jsonObject.get("name");
		System.out.println(name);
 
		long age = (Long) jsonObject.get("age");
		System.out.println(age);
 
		// loop array
		JSONArray msg = (JSONArray) jsonObject.get("messages");
		Iterator<String> iterator = msg.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
		 
	}

	 
}
