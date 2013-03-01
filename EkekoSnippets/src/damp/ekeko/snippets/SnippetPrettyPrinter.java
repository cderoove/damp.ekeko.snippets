package damp.ekeko.snippets;

import java.util.AbstractList;
import java.util.LinkedHashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.Symbol;

public class SnippetPrettyPrinter extends NaiveASTFlattener {
	private LinkedHashMap snippet;
	
	public SnippetPrettyPrinter () {
	}
	
	public void setSnippet(LinkedHashMap snippet) {
		this.snippet = snippet;
	}
	
	public Symbol getVar(ASTNode node) {
		PersistentHashMap ast2var = (PersistentHashMap) this.snippet.get(Keyword.intern("ast2var"));
		return (Symbol) ast2var.get(node);
	}
	
	public Symbol getUserVar(ASTNode node) {
		PersistentArrayMap var2uservar = (PersistentArrayMap) this.snippet.get(Keyword.intern("var2uservar"));
		return (Symbol) var2uservar.get(getVar(node));
	}

	public PersistentList getGroundF(ASTNode node) {
		PersistentHashMap ast2groundf = (PersistentHashMap) this.snippet.get(Keyword.intern("ast2groundf"));
		return (PersistentList) ast2groundf.get(node);
	}

	public PersistentList getConstrainF(ASTNode node) {
		PersistentHashMap ast2constrainf = (PersistentHashMap) this.snippet.get(Keyword.intern("ast2constrainf"));
		return (PersistentList) ast2constrainf.get(node);
	}

	public boolean hasDefaultGroundf(ASTNode node) {
		PersistentList groundf = getGroundF(node);
		if (groundf.first() == Keyword.intern("minimalistic") ||
				groundf.first() == Keyword.intern("epsilon"))
			return true;
		return false;
	}

	public boolean hasDefaultConstrainf(ASTNode node) {
		PersistentList constrainf = getConstrainF(node);
		if (constrainf.first() == Keyword.intern("exact") ||
				constrainf.first() == Keyword.intern("variable") || 
				constrainf.first() == Keyword.intern("epsilon")) 	
			return true;
		return false;
	}

	public String getFunctionString(PersistentList functionList) {
		String function = functionList.first().toString();
		String functionArgs = functionList.toString().replace(function, "").replace(" ", ","); 
		return function.replace(":", "@") + functionArgs;
	}

	public boolean preVisit2(ASTNode node) {
		preVisit(node);

		Symbol uservar = getUserVar(node);
		if (uservar != null) {
			this.buffer.append(uservar);
			return false;
		}
		return true;
	}

	public void preVisit(ASTNode node) {
		//if node is first member of NodeList, then preVisitNodeList
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property.isChildListProperty()) {
			AbstractList nodeList = (AbstractList) node.getParent().getStructuralProperty(property);
			if (nodeList.get(0).equals(node))
				preVisitNodeList(nodeList);
		}

		if (!hasDefaultGroundf(node) || 
				!hasDefaultConstrainf(node)) 
			this.buffer.append("&open");
	}

	public void postVisit(ASTNode node) {
		String fString = "";
		if (!hasDefaultGroundf(node))
			fString = getFunctionString(getGroundF(node));
		if (!hasDefaultConstrainf(node))
			fString += getFunctionString(getConstrainF(node));
		if (!fString.isEmpty()) {
			int len = this.buffer.length();
			if (this.buffer.substring(len-1).equals("\n")) {
				this.buffer.delete(len-1, len);
				this.buffer.append("&close" + fString + "\n");
			} else 
				this.buffer.append("&close" + fString);
		}

		//if node is last member of NodeList, then postVisitNodeList
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property.isChildListProperty()) {
			AbstractList nodeList = (AbstractList) node.getParent().getStructuralProperty(property);
			if (nodeList.get(nodeList.size()-1).equals(node))
				postVisitNodeList(nodeList);
		}
	}
	
	public void preVisitNodeList(AbstractList nodeList) {
		//this.buffer.append("|");
	}
	
	public void postVisitNodeList(AbstractList nodeList) {
		//this.buffer.append("|");
	}

	public String getResult(){
		String result = super.getResult();
		while (result.indexOf("&open ") > -1) {
			result = result.replaceAll("&open ", " &open");
		}
		result = result.replaceAll("&open", "[");
		result = result.replaceAll("&close", "]");
		return result;
	}

}
