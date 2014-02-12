package damp.ekeko.snippets.gui.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;

import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;

import com.google.common.base.Joiner;

import damp.ekeko.snippets.data.SnippetGroupHistory;

public class SnippetPrettyPrinter extends NaiveASTFlattener {

	public static IFn FN_SNIPPET_VAR_FOR_NODE;
	public static IFn FN_SNIPPET_USERVAR_FOR_NODE;
	public static IFn FN_SNIPPET_GROUNDER_FOR_NODE;
	public static IFn FN_SNIPPET_CONSTRAINER_FOR_NODE;
	public static IFn FN_SNIPPET_USERFS_FOR_NODE;
	public static IFn FN_SNIPPET_GROUNDERWITHARGS_FOR_NODE;
	public static IFn FN_SNIPPET_CONSTRAINERWITHARGS_FOR_NODE;
	public static IFn FN_SNIPPET_LIST_CONTAINING;


	
	
	protected Object snippet;
	protected Object highlightNode;
	protected LinkedList<StyleRange> styleRanges;
	protected Stack<StyleRange> currentHighlight;

	public SnippetPrettyPrinter () {
		styleRanges = new LinkedList<StyleRange>();
		currentHighlight = new Stack<StyleRange>();
	}

	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}

	public void setSnippet(Object snippet) {
		this.snippet = snippet;
	}

	public void setHighlightNode(Object node) {
		this.highlightNode = node;
	}

	public StyleRange[] getStyleRanges() {
		return styleRanges.toArray(new StyleRange[0]);
	}

	public Object getVar(Object node) {
		return FN_SNIPPET_VAR_FOR_NODE.invoke(snippet, node);
	}

	public Object getUserVar(Object node) {
		return FN_SNIPPET_USERVAR_FOR_NODE.invoke(snippet, node);
	}

	public Object getGroundF(Object node) {
		return FN_SNIPPET_GROUNDER_FOR_NODE.invoke(snippet, node);
	}

	public Object getConstrainF(Object node) {
		return FN_SNIPPET_CONSTRAINER_FOR_NODE.invoke(snippet, node);
	}

	public Object[] getUserFS(Object node) {
		return getArray(FN_SNIPPET_USERFS_FOR_NODE.invoke(snippet, node));
	}

	//TODO: figure out why these are hard-coded
	public boolean hasDefaultGroundf(Object node) {
		Object groundf = getGroundF(node);
		if(groundf == null) return true;
		if (groundf == Keyword.intern("minimalistic") ||
				groundf == Keyword.intern("exact") ||
				groundf == Keyword.intern("child+") ||
				groundf == Keyword.intern("epsilon"))
			return true;
		return false;
	}

	//TODO: figure out why these are hard-coded
	public boolean hasDefaultConstrainf(Object node) {
		Object constrainf = getConstrainF(node);
		if(constrainf == null) return true;
		if (constrainf == Keyword.intern("exact") ||
				constrainf == Keyword.intern("variable") || 
				constrainf == Keyword.intern("variable-info") || 
				constrainf == Keyword.intern("epsilon")) 	
			return true;
		return false;
	}

	public boolean hasUserf(Object node) {
		Object[] userf = getUserFS(node);
		if (userf.length > 0)
			return true;
		return false;
	}

	public String getGroundFString(Object node) {
		Object[] functionArgs = getArray(FN_SNIPPET_GROUNDERWITHARGS_FOR_NODE.invoke(snippet, node)); 
		return getFunctionString(functionArgs);
	}

	public String getConstrainFString(Object node) {
		Object[] functionArgs = getArray(FN_SNIPPET_CONSTRAINERWITHARGS_FOR_NODE.invoke(snippet, node)); 
		Object constrainf = getConstrainF(node);

		if (constrainf == Keyword.intern("change-name")) 
			return getFunctionStringForChangeName(functionArgs);

		if (getConstrainF(node) == Keyword.intern("exact-variable")) 
			return getFunctionStringForExactVariable(getUserVar(node));

		return getFunctionString(functionArgs);
	}

	public String getUserFSString(Object node) {
		Object[] userFS = getUserFS(node);
		String result = "";
		for (int i=0; i<userFS.length; i++) {
			result += getFunctionString(getArray(userFS[i])) + ",";
		}
		return result.substring(0, result.length()-1);
	}

	public String getUserVarString(Object node) {
		Object[] functionArgs = getArray(FN_SNIPPET_CONSTRAINERWITHARGS_FOR_NODE.invoke(snippet, node)); 
		Object constrainf = getConstrainF(node);
		Object uservar = getUserVar(node);
		//TODO: figure out why these are hard-coded
		if ((uservar != null) && 
				(constrainf != Keyword.intern("exact-variable")) &&
				(constrainf != Keyword.intern("variable")) &&
				(constrainf != Keyword.intern("variable-info")))  	
			return getFunctionStringForExactVariable(uservar);

		return "";
	}

	public String getFunctionString(Object[] functionList) {
		//functionList = (:function arg1 arg2 ... argn)
		if (functionList == null || functionList.length == 0)
			return "";
		else {
			String function = functionList[0].toString();
			String functionArgs = "";
			for (int i=1; i<functionList.length; i++) {
				functionArgs += " " + functionList[i].toString();
			}
			return "(" + function.replace(":", "") + functionArgs + ")";
		}
	}

	public String getFunctionStringForChangeName(Object[] functionList) {
		String function = functionList[0].toString();
		String rule = functionList[1].toString(); 
		String nodeStr = functionList[2].toString(); 
		String functionArg = (String) RT.var("damp.ekeko.snippets.util", "convert-rule-to-string").invoke(rule, nodeStr);
		return "(" + function.replace(":", "") + " " + functionArg + ")";
	}

	public String getFunctionStringForExactVariable(Object uservar) {
		return "(= " + uservar + ")";
	}

	public static StyleRange styleRangeForVariable(int start, int length) {
		return new StyleRange(start, length, Display.getCurrent().getSystemColor(SWT.COLOR_BLUE), null);
	}

	public static StyleRange styleRangeForMeta(int start, int length) {
		return new StyleRange(start, length, Display.getCurrent().getSystemColor(SWT.COLOR_RED), null);
	}
	public static StyleRange styleRangeForHighlight(int start) {
		return new StyleRange(start, 0, null, Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
	}

	public static StyleRange styleRangeForDirectives(int start, int length) {
		return new StyleRange(start, length, Display.getCurrent().getSystemColor(SWT.COLOR_GRAY), null);
	}



	@Override
	public boolean preVisit2(ASTNode node) {
		preVisit(node);

		Object uservar = getUserVar(node);
		//TODO: figure out why these are hard-coded
		if (uservar != null) {
			Object constrainf = getConstrainF(node);
			if (constrainf == Keyword.intern("variable") ||
					constrainf == Keyword.intern("variable-info") || 	
					constrainf == Keyword.intern("change-name")) { 	
				int start = getCurrentCharacterIndex();
				buffer.append(uservar);
				styleRanges.add(styleRangeForVariable(start, getCurrentCharacterIndex() - start));
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	
	static boolean isFirstElementOfList(ASTNode node) {
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property != null && property.isChildListProperty()) {
			ASTNode parent = node.getParent();
			List nodeList = (List) parent.getStructuralProperty(property);
			if (nodeList.get(0).equals(node))	
				return true;	
		}
		return false;
	}
	
	static boolean isLastElementOfList(ASTNode node) {
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property != null && property.isChildListProperty()) {
			ASTNode parent = node.getParent();
			List nodeList = (List) parent.getStructuralProperty(property);
			if(nodeList.get(nodeList.size()-1).equals(node))
				return true;
		}
		return false;
	}

	
	@Override
	public void preVisit(ASTNode node) {	
		if(isFirstElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			preVisitNodeListWrapper(nodeListWrapper);
		} 
		printOpeningNode(node);
		printOpeningHighlight(node);
	}

	public void postVisit(ASTNode node) {
		if(isLastElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			postVisitNodeListWrapper(nodeListWrapper);
		}
		printClosingHighlight(node);
		printClosingNode(node);
	}	

	public void preVisitNodeListWrapper(Object nodeListWrapper) {
		//TODO: check whether logic variable has been associated with list itself
		printOpeningNode(nodeListWrapper);
		printOpeningHighlight(nodeListWrapper);
	}

	public void postVisitNodeListWrapper(Object nodeListWrapper) {
		printClosingHighlight(nodeListWrapper);
		printClosingNode(nodeListWrapper);
	}

	public void printOpeningNode(Object node) {
		//print bracket
		if (!hasDefaultGroundf(node) || 
				!hasDefaultConstrainf(node) || 
				hasUserf(node)) {
			int start = getCurrentCharacterIndex();
			this.buffer.append("[");
			styleRanges.add(styleRangeForMeta(start, 1));
		}

	}

	private int getCurrentCharacterIndex() {
		return this.buffer.length();
	}

	public void printClosingNode(Object node) {
		String fString = "";
		//print bracket, followed by groundf, constrainf, and userfs
		List<String> directives = new LinkedList<String>();
		if (!hasDefaultGroundf(node))
			directives.add(getGroundFString(node));
		if (!hasDefaultConstrainf(node))
			directives.add(getConstrainFString(node));
		if (hasUserf(node))
			directives.add(getUserFSString(node));
		String userVarString = getUserVarString(node);
		if (!userVarString.isEmpty())
			directives.add(userVarString);

		if (!directives.isEmpty()) { 
			int start = getCurrentCharacterIndex();
			this.buffer.append("]");
			styleRanges.add(styleRangeForMeta(start, 1));	
			start = getCurrentCharacterIndex();
			this.buffer.append("@[");
			styleRanges.add(styleRangeForMeta(start, 2));
			start = getCurrentCharacterIndex();
			this.buffer.append(Joiner.on(',').join(directives));
			styleRanges.add(styleRangeForDirectives(start, getCurrentCharacterIndex() - start));
			start = getCurrentCharacterIndex();
			this.buffer.append("]");
			styleRanges.add(styleRangeForMeta(start, 1));	
		}

	}

	public void printOpeningHighlight(Object node) {
		if(node == null)
			return;
		if (node.equals(highlightNode))	
			currentHighlight.push(styleRangeForHighlight(getCurrentCharacterIndex()));
	}

	public void printClosingHighlight(Object node) {
		if(node == null)
			return;
		if (node.equals(highlightNode)) {
			StyleRange style = currentHighlight.pop();
			style.length = getCurrentCharacterIndex() -style.start;
			styleRanges.add(style);
		}
	}

	public String getPlainResult(){
		return getResult();
	}


	public String prettyPrint(Object snippet) {
		setSnippet(snippet);
		ASTNode root = SnippetGroupHistory.getRootOfSnippet(snippet); 
		root.accept(this);
		return getResult();
	}

}
