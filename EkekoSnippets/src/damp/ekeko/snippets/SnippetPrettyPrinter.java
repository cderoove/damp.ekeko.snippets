package damp.ekeko.snippets;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetPrettyPrinter extends NaiveASTFlattener {
	private final String rep = "damp.ekeko.snippets.representation";
	private Object snippet;
	private Object highlightNode;
	private int[] highlightPos;
	
	public SnippetPrettyPrinter () {
		highlightPos = new int[2];
	}
	
	public void setSnippet(Object snippet) {
		this.snippet = snippet;
	}
	
	public Object getSnippet() {
		return snippet;
	}

	public void setHighlightNode(Object node) {
		this.highlightNode = node;
	}
	
	public Object getHighlightNode() {
		return highlightNode;
	}
	
	public int[] getHighlightPos() {
		return highlightPos;
	}

	public Symbol getVar(Object node) {
		return (Symbol) RT.var(rep, "snippet-var-for-node").invoke(getSnippet(), node);
	}
	
	public Symbol getUserVar(Object node) {
		return (Symbol) RT.var(rep, "snippet-uservar-for-node").invoke(getSnippet(), node);
	}

	public Keyword getGroundF(Object node) {
		return (Keyword) RT.var(rep, "snippet-grounder-for-node").invoke(getSnippet(), node);
	}

	public Keyword getConstrainF(Object node) {
		return (Keyword) RT.var(rep, "snippet-constrainer-for-node").invoke(getSnippet(), node);
	}

	public boolean hasDefaultGroundf(Object node) {
		Keyword groundf = getGroundF(node);
		if (groundf == Keyword.intern("minimalistic") ||
				groundf == Keyword.intern("epsilon"))
			return true;
		return false;
	}

	public boolean hasDefaultConstrainf(Object node) {
		Keyword constrainf = getConstrainF(node);
		if (constrainf == Keyword.intern("exact") ||
				constrainf == Keyword.intern("variable") || 
				constrainf == Keyword.intern("epsilon")) 	
			return true;
		return false;
	}

	public String getGroundFString(Object node) {
		PersistentList functionArgs = (PersistentList) RT.var(rep, "snippet-grounder-with-args-for-node").invoke(getSnippet(), node); 
		return getFunctionString(functionArgs);
	}

	public String getConstrainFString(Object node) {
		PersistentList functionArgs = (PersistentList) RT.var(rep, "snippet-constrainer-with-args-for-node").invoke(getSnippet(), node); 
		return getFunctionString(functionArgs);
	}

	public String getFunctionString(PersistentList functionList) {
		if (functionList == null || functionList.isEmpty())
			return "";
		else {
			String function = functionList.first().toString();
		 	String functionArgs = functionList.toString().replace(function, "").replace(" ", ","); 
		 	return function.replace(":", "@") + functionArgs;
		}
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
			PersistentArrayMap nodeListWrapper = (PersistentArrayMap) RT.var(rep, "snippet-node-with-member").invoke(getSnippet(), node); 
			List nodeList = (List) RT.var(rep, "snippet-value-for-node").invoke(getSnippet(), nodeListWrapper);
			if (nodeList.size() > 0 && nodeList.get(0).equals(node))
				preVisitNodeList(nodeListWrapper);
		}

		//print bracket
		if (!hasDefaultGroundf(node) || 
				!hasDefaultConstrainf(node)) 
			this.buffer.append("&open");
		
		//color highlightNode
		if (highlightNode != null && highlightNode.equals(node))
			this.buffer.append("&coloropen");
	}

	public void postVisit(ASTNode node) {
		String fString = "";
		
		//print bracket, followed by groundf and constrainf
		if (!hasDefaultGroundf(node))
			fString = getGroundFString(node);
		if (!hasDefaultConstrainf(node))
			fString += getConstrainFString(node);
		if (!fString.isEmpty()) 
			addBufferBeforeEOL("&close" + fString);

		//if node is last member of NodeList, then postVisitNodeList
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property.isChildListProperty()) {
			PersistentArrayMap nodeListWrapper = (PersistentArrayMap) RT.var(rep, "snippet-node-with-member").invoke(getSnippet(), node); 
			List nodeList = (List) RT.var(rep, "snippet-value-for-node").invoke(getSnippet(), nodeListWrapper);
			if (nodeList.size() > 0 && nodeList.get(nodeList.size()-1).equals(node))
				postVisitNodeList(nodeListWrapper);
		}
		
		//color highlightNode
		if (highlightNode != null && highlightNode.equals(node))
			this.buffer.append("&colorclose");
	}
	
	public void preVisitNodeList(PersistentArrayMap nodeListWrapper) {
		//print bracket
		if (!hasDefaultGroundf(nodeListWrapper) || 
				!hasDefaultConstrainf(nodeListWrapper)) 
			this.buffer.append("&open");

		//color highlightNode
		if (highlightNode != null && highlightNode.equals(nodeListWrapper))
			this.buffer.append("&coloropen");
	}
	
	public void postVisitNodeList(PersistentArrayMap nodeListWrapper) {
		String fString = "";

		//print bracket, followed by groundf and constrainf
		if (!hasDefaultGroundf(nodeListWrapper))
			fString = getGroundFString(nodeListWrapper);
		if (!hasDefaultConstrainf(nodeListWrapper))
			fString += getConstrainFString(nodeListWrapper);
		if (!fString.isEmpty()) 
			addBufferBeforeEOL("&close" + fString);

		//color highlightNode
		if (highlightNode != null && highlightNode.equals(nodeListWrapper))
			this.buffer.append("&colorclose");
	}

	public String getResult(){
		String result = super.getResult();
		
		//delete first "&open"
		result = result.replaceFirst("&open", "");
		
		//replace "&open  " with "  &open"
		while (result.indexOf("&open ") > -1) {
			result = result.replaceAll("&open ", " &open");
		}
		
		result = result.replaceAll("&open", "[");
		result = result.replaceAll("&close", "]");
		
		//get highlightNode position
		highlightPos[0] = result.indexOf("&coloropen");
		result = result.replaceAll("&coloropen", "");
		highlightPos[1] = result.indexOf("&colorclose");
		result = result.replaceAll("&colorclose", "");
		
		return result;
	}

	public void addBufferBeforeEOL(String str) {
		//add buffer before end of line
		int len = this.buffer.length();
		if (this.buffer.substring(len-1).equals("\n")) {
			this.buffer.delete(len-1, len);
			this.buffer.append(str + "\n");
		} else 
			this.buffer.append(str);
	}
}
