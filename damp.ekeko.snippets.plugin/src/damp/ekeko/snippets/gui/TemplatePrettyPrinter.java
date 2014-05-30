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

	public static IFn FN_SNIPPET_ELEMENT_ISLIST;
	public static IFn FN_SNIPPET_ELEMENT_ISVALUE;
	public static IFn FN_SNIPPET_ELEMENT_ISNODE;
	public static IFn FN_SNIPPET_ELEMENT_ISNULL;


	public static IFn FN_SNIPPET_ELEMENT_VALUE;
	public static IFn FN_SNIPPET_ELEMENT_LIST;
	public static IFn FN_SNIPPET_ELEMENT_NODE;




	protected Object snippet;
	protected Object highlightNode;
	protected TemplateGroup templateGroup;



	protected LinkedList<StyleRange> styleRanges;
	protected Stack<StyleRange> currentHighlight;
	@SuppressWarnings("rawtypes")
	private Stack listWrapperForWhichToIgnoreListDecorations;

	@SuppressWarnings("rawtypes")
	public TemplatePrettyPrinter(TemplateGroup group) {
		styleRanges = new LinkedList<StyleRange>();
		currentHighlight = new Stack<StyleRange>();
		listWrapperForWhichToIgnoreListDecorations = new Stack();
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




	protected void printVariableReplacement(Object replacementVar) {
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
			//can be null when node is root of template
			if(nodeListWrapper != null) {
				Object listReplacementVar = getUserVar(nodeListWrapper);
				if(listReplacementVar != null) {
					if(listWrapperForWhichToIgnoreListDecorations.isEmpty() ||
							!nodeListWrapper.equals(listWrapperForWhichToIgnoreListDecorations.peek())) {
						printVariableReplacement(listReplacementVar);
						return false; //do not print node itself because list has been replaced
					}
				}
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
			if(listWrapperForWhichToIgnoreListDecorations.isEmpty() ||
					!nodeListWrapper.equals(listWrapperForWhichToIgnoreListDecorations.peek())) {
				preVisitNodeListWrapper(nodeListWrapper);
			}
		}
		printOpeningNode(node);
		printOpeningHighlight(node);
	}

	public void postVisit(ASTNode node) {
		printClosingHighlight(node);
		printClosingNode(node);
		if(isLastElementOfList(node)) {
			Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
			if(nodeListWrapper == null)
				return;
			if(	listWrapperForWhichToIgnoreListDecorations.isEmpty() ||
					!nodeListWrapper.equals(listWrapperForWhichToIgnoreListDecorations.peek())) {
				postVisitNodeListWrapper(nodeListWrapper);
			}
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

	public static boolean isNodeValueInTemplate(Object template, Object element) {
		return (Boolean) FN_SNIPPET_ELEMENT_ISNODE.invoke(template, element);
	}

	public static ASTNode getActualNodeValueInTemplate(Object template, Object element) {
		return (ASTNode) FN_SNIPPET_ELEMENT_NODE.invoke(template, element);
	}

	public static Collection getActualListValueInTemplate(Object template, Object element) {
		return (Collection) FN_SNIPPET_ELEMENT_LIST.invoke(template, element);
	}

	public static Boolean isListValueInTemplate(Object template, Object element) {
		return (Boolean) FN_SNIPPET_ELEMENT_ISLIST.invoke(template, element);
	}

	public static Boolean isPrimitiveValueInTemplate(Object template, Object element) {
		return (Boolean) FN_SNIPPET_ELEMENT_ISVALUE.invoke(template, element);
	}

	public static Object getActualPrimitiveValueInTemplate(Object template, Object element) {
		return FN_SNIPPET_ELEMENT_VALUE.invoke(template, element);
	}

	public static Boolean isNullValueInTemplate(Object template, Object element) {
		return (Boolean) FN_SNIPPET_ELEMENT_ISNULL.invoke(template, element);
	}

	//called by labelproviders to pretty print an individual template value
	public String prettyPrintElement(Object snippet, Object element) {
		setSnippet(snippet);

		if(isNodeValueInTemplate(snippet, element)) {
			ASTNode node = (ASTNode) FN_SNIPPET_ELEMENT_NODE.invoke(snippet, element);
			if(isElementOfList(node)) {
				Object nodeListWrapper = FN_SNIPPET_LIST_CONTAINING.invoke(snippet, node); 
				if(nodeListWrapper != null) {
					listWrapperForWhichToIgnoreListDecorations.push(nodeListWrapper);
					node.accept(this);
					listWrapperForWhichToIgnoreListDecorations.pop();
					return getResult();
				}
			}
			node.accept(this);
			return getResult();
		} 

		if(isListValueInTemplate(snippet, element)) {

			Object listReplacementVar = getUserVar(element);
			if(listReplacementVar != null) {
				printVariableReplacement(listReplacementVar);
				return getResult();
			}

			printOpeningNode(element);
			listWrapperForWhichToIgnoreListDecorations.push(element);

			@SuppressWarnings("rawtypes")
			Collection lst = getActualListValueInTemplate(snippet, element);

			for(Object member : lst) {
				prettyPrintElement(snippet, member);
				this.buffer.append(" ");
			}

			if(!lst.isEmpty()) {
				this.buffer.deleteCharAt(getCurrentCharacterIndex()-1);
			}

			listWrapperForWhichToIgnoreListDecorations.pop();
			printClosingNode(element);

			return getResult().trim();
		}

		if(isPrimitiveValueInTemplate(snippet, element)) {
			Object value = getActualPrimitiveValueInTemplate(snippet, element);
			if(hasNonDefaultDirectives(snippet, element)) {
				printOpeningNode(element);
				this.buffer.append(value.toString());
				printClosingNode(element);
			}
			return getResult();
		} 

		if(isNullValueInTemplate(snippet, element)) {
			if(hasNonDefaultDirectives(snippet, element)) {
				printOpeningNode(element);
				this.buffer.append("null");
				printClosingNode(element);
			}
			return getResult();
		} else
			throw new RuntimeException("Unexpected value to be pretty-printed: " + element.toString());


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
