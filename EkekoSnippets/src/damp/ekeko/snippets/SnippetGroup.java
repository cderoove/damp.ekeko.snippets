package damp.ekeko.snippets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;

public class SnippetGroup {
	private final String es = "damp.ekeko.snippets";
	private Object group;
	
	public SnippetGroup() {
		
	}
	
	public Object getGroup() {
		return group;
	}
	
	public String toString() {
		String result = (String) RT.var(es+".representation", "print-snippetgroup").invoke(getGroup());
		return result;
	}
	
	public void addSnippetCode(String code) {
		ASTNode rootNode = (ASTNode) RT.var(es+".parsing", "parse-string-ast").invoke(code);
		Object snippet = RT.var(es+".representation", "jdt-node-as-snippet").invoke(rootNode);
		group = RT.var(es+".operators", "add-snippet").invoke(group, snippet);
	}

	public LazySeq getPossibleNodes(String operator) {
		return (LazySeq) RT.var(es+".precondition", "possible-nodes-for-operator-in-group").invoke(getGroup(), Keyword.intern(operator));
	}
	
	public void applyOperator(String operator, Object node) {
		Object snippet = RT.var(es+".representation", "snippetgroup-snippet-for-node").invoke(getGroup(), node);		
		Object newsnippet = RT.var(es+".operators", operator).invoke(snippet, node);
		group = RT.var(es+".representation", "snippetgroup-replace-snippet").invoke(group, snippet, newsnippet);		
	}
	
	public static Object getObject(Object node) {
		if (node instanceof ASTNode) {
			return node;
		} else {
			PersistentArrayMap nodeList = (PersistentArrayMap) node;
			return nodeList.get(Keyword.intern("value"));
		}
	}

	public static String getType(Object node) {
		if (node instanceof ASTNode) {
			return node.getClass().toString().replace("class org.eclipse.jdt.core.dom.", "");
		} else {
			PersistentArrayMap nodeList = (PersistentArrayMap) node;
			StructuralPropertyDescriptor property = (StructuralPropertyDescriptor) nodeList.get(Keyword.intern("property"));
			return property.toString().replace("ChildListProperty[org.eclipse.jdt.core.dom.", "[");
		}
	}
}
