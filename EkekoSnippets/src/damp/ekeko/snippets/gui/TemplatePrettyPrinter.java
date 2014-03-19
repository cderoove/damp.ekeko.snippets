package damp.ekeko.snippets.gui;

import java.util.Collection;
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
import clojure.lang.RT;

import com.google.common.base.Joiner;

import damp.ekeko.snippets.data.TemplateGroup;

public class TemplatePrettyPrinter extends NaiveASTFlattener {

	public static IFn FN_SNIPPET_USERVAR_FOR_NODE;
	public static IFn FN_SNIPPET_BOUNDDIRECTIVES_STRING;

	public static IFn FN_SNIPPET_NONDEFAULT_BOUNDDIRECTIVES;
	public static IFn FN_SNIPPET_HAS_NONDEFAULT_BOUNDDIRECTIVES;

	
	public static IFn FN_SNIPPET_LIST_CONTAINING;


	
	protected Object snippet;
	protected Object highlightNode;
	protected TemplateGroup templateGroup;
	
	
	
	protected LinkedList<StyleRange> styleRanges;
	protected Stack<StyleRange> currentHighlight;

	public TemplatePrettyPrinter (TemplateGroup group) {
		styleRanges = new LinkedList<StyleRange>();
		currentHighlight = new Stack<StyleRange>();
		this.templateGroup = group;
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

	public Object getUserVar(Object node) {
		return FN_SNIPPET_USERVAR_FOR_NODE.invoke(snippet, node);
	}

	//TODO: figure out why these are hard-coded	}

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




	private void printVariableReplacement(Object replacementVar) {
		int start = getCurrentCharacterIndex();
		this.buffer.append(replacementVar);
		styleRanges.add(styleRangeForVariable(start, getCurrentCharacterIndex() - start));	
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		//TODO: this does not work for empty lists ... 
		//should override every method in NativeASTFlattener to check whether list children should be visited (but too much work)
		
		preVisit(node);
		if(isElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			Object listReplacementVar = getUserVar(nodeListWrapper);
			if(listReplacementVar != null) {
				printVariableReplacement(listReplacementVar);
				return false; //do not print node itself because list has been replaced
			}
		}
		Object replacementVar = getUserVar(node);
		if (replacementVar != null) {
			printVariableReplacement(replacementVar);
			return false;//do not print node itself because node has been replace
		} 
		return true;
	}
	
	
	static boolean isElementOfList(ASTNode node) {
		ASTNode parent = node.getParent();
		if(parent == null)
			return false;
		StructuralPropertyDescriptor property = node.getLocationInParent();
		return property != null && property.isChildListProperty();
	}
	
	static boolean isFirstElementOfList(ASTNode node) {
		ASTNode parent = node.getParent();
		if(parent == null)
			return false;
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property != null && property.isChildListProperty()) {
			List nodeList = (List) parent.getStructuralProperty(property);
			if (nodeList.get(0).equals(node))	
				return true;	
		}
		return false;
	}
	
	static boolean isLastElementOfList(ASTNode node) {
		ASTNode parent = node.getParent();
		if(parent == null)
			return false;
		StructuralPropertyDescriptor property = node.getLocationInParent();
		if (property != null && property.isChildListProperty()) {
			List nodeList = (List) parent.getStructuralProperty(property);
			if(nodeList.get(nodeList.size()-1).equals(node))
				return true;
		}
		return false;
	}

	
	@Override
	public void preVisit(ASTNode node) {
		if(node==null)
			return;
		if(isFirstElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			preVisitNodeListWrapper(nodeListWrapper);
		}
		printOpeningNode(node);
		printOpeningHighlight(node);
	}

	public void postVisit(ASTNode node) {
		printClosingHighlight(node);
		printClosingNode(node);
		if(isLastElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			postVisitNodeListWrapper(nodeListWrapper);
		}
	}	

	public void preVisitNodeListWrapper(Object nodeListWrapper) {
		printOpeningNode(nodeListWrapper);
		printOpeningHighlight(nodeListWrapper);
	}

	public void postVisitNodeListWrapper(Object nodeListWrapper) {
		printClosingHighlight(nodeListWrapper);
		printClosingNode(nodeListWrapper);
	}

	@SuppressWarnings("rawtypes")
	public static Collection getNonDefaultDirectives(Object cljTemplate, Object cljNode) {
		return (Collection) FN_SNIPPET_NONDEFAULT_BOUNDDIRECTIVES.invoke(cljTemplate, cljNode);
	}
	
	public static Boolean hasNonDefaultDirectives(Object cljTemplate, Object cljNode) {
		return (Boolean) FN_SNIPPET_HAS_NONDEFAULT_BOUNDDIRECTIVES.invoke(cljTemplate, cljNode);
	}
	
	public static String boundDirectivesString(Object cljTemplate, Object cljNode) {
		return (String) FN_SNIPPET_BOUNDDIRECTIVES_STRING.invoke(cljTemplate, cljNode);
	}

	
	public void printOpeningNode(Object node) {
		if(hasNonDefaultDirectives(snippet, node)) {
			int start = getCurrentCharacterIndex();
			this.buffer.append("[");
			styleRanges.add(styleRangeForMeta(start, 1));
		}
	}

	private int getCurrentCharacterIndex() {
		return this.buffer.length();
	}
	
	public void printClosingNode(Object node) {
		if (hasNonDefaultDirectives(snippet, node)) { 
			int start = getCurrentCharacterIndex();
			this.buffer.append("]");
			styleRanges.add(styleRangeForMeta(start, 1));	
			start = getCurrentCharacterIndex();
			this.buffer.append("@[");
			styleRanges.add(styleRangeForMeta(start, 2));
			start = getCurrentCharacterIndex();
			
			this.buffer.append(boundDirectivesString(snippet, node));
			
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

	public String prettyPrintNode(Object snippet, Object node) {
		setSnippet(snippet);
		((ASTNode) node).accept(this);
		return getResult();
	}

	
	public String prettyPrintSnippet(Object snippet) {
		setSnippet(snippet);
		ASTNode root = TemplateGroup.getRootOfSnippet(snippet); 
		root.accept(this);
		
		Collection conditions = templateGroup.getLogicConditions(snippet);
		this.buffer.append('\n');
		this.buffer.append(Joiner.on('\n').join(conditions));
		
		return getResult();
	}

}
