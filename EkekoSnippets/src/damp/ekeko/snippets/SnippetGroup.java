package damp.ekeko.snippets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroup {
	/*static {	
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.parsing"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.representation"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.operators"));
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.precondition"));
	}*/		

	private static String es = "damp.ekeko.snippets";
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
	
	public void applyOperator(String operator, Object node, String[] args) {
		Object snippet = RT.var(es+".representation", "snippetgroup-snippet-for-node").invoke(getGroup(), node);
		Object newsnippet = snippet;
		System.out.println("len "+args.length+args.toString());
		
		if (args.length == 2)
			newsnippet = RT.var(es+".operators", operator).invoke(snippet, node, args[0], args[1]);
		else if (args.length == 1 && !args[0].isEmpty())
			newsnippet = RT.var(es+".operators", operator).invoke(snippet, node, Symbol.intern(args[0]));
		else
			newsnippet = RT.var(es+".operators", operator).invoke(snippet, node);
			
		group = RT.var(es+".representation", "snippetgroup-replace-snippet").invoke(group, snippet, newsnippet);		
	}
	
	public Object getObjectValue(Object node) {
		if (node instanceof ASTNode) {
			return node;
		} else {
			Object snippet = RT.var(es+".representation", "snippetgroup-snippet-for-node").invoke(getGroup(), node);		
			return RT.var(es+".representation", "snippet-value-for-node").invoke(snippet, node);		
		}
	}

	public static String getTypeValue(Object node) {
		if (node instanceof ASTNode) {
			return node.getClass().toString().replace("class org.eclipse.jdt.core.dom.", "");
		} else {
			PersistentArrayMap nodeList = (PersistentArrayMap) node;
			StructuralPropertyDescriptor property = (StructuralPropertyDescriptor) nodeList.get(Keyword.intern("property"));
			return property.toString().replace("ChildListProperty[org.eclipse.jdt.core.dom.", "[");
		}
	}
	
	public void runQuery() {
		RT.var(es, "query-by-snippetgroup*").invoke(getGroup());		
	}
}
