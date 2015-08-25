package damp.ekeko.snippets.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import clojure.lang.IFn;
import clojure.lang.RT;
import damp.ekeko.snippets.NaiveASTFlattener;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplatePrettyPrinter extends NaiveASTFlattener {

	public static IFn FN_SNIPPET_USERVAR_FOR_NODE;
	public static IFn FN_SNIPPET_EXP_FOR_NODE;

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

	public static IFn FN_SNIPPET_ELEMENT_REPLACEDBY_WILDCARD;

	public static IFn FN_SNIPPET_VALUE_IDENTIFIER;

	public static IFn FN_SNIPPET_NODE_PROPERTY;

	protected Object snippet;
	protected Object highlightNode;
	protected TemplateGroup templateGroup;



	protected LinkedList<StyleRange> styleRanges;
	protected LinkedList<StyleRange> hyperlinks;

	protected Stack<StyleRange> currentHighlight;
	@SuppressWarnings("rawtypes")
	private Stack listWrapperForWhichToIgnoreListDecorations;
	private Stack currentHyperlink;

	@SuppressWarnings("rawtypes")
	public TemplatePrettyPrinter(TemplateGroup group) {
		styleRanges = new LinkedList<StyleRange>();
		hyperlinks = new LinkedList<StyleRange>();
		currentHighlight = new Stack<StyleRange>();
		listWrapperForWhichToIgnoreListDecorations = new Stack();
		currentHyperlink = new Stack();
		this.templateGroup = group;
	}

	public void setTemplateGroup(TemplateGroup group) {
		this.templateGroup = group;
	}

	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}

	public Object getIdentifier(Object value) {
		if(value == null)
			return null;
		return FN_SNIPPET_VALUE_IDENTIFIER.invoke(snippet, value);
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

	public Object getUserExp(Object node) {
		return FN_SNIPPET_EXP_FOR_NODE.invoke(snippet, node);
	}
	
	public Object getValueOfProperty(ASTNode node, StructuralPropertyDescriptor prop) {
		return FN_SNIPPET_NODE_PROPERTY.invoke(snippet, node, prop);
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

	public static Color colorOrNullOutsideOfDisplayThread(int id) {
		Display current = Display.getCurrent();
		if(current == null)
			return null;
		return current.getSystemColor(id);
	}

	public static StyleRange styleRangeForVariable(int start, int length) {
		return new StyleRange(start, length, colorOrNullOutsideOfDisplayThread(SWT.COLOR_BLUE), null);
	}

	public static StyleRange styleRangeForExp(int start, int length) {
		return new StyleRange(start, length, colorOrNullOutsideOfDisplayThread(SWT.COLOR_DARK_CYAN), null);
	}

	public static StyleRange styleRangeForMeta(int start, int length) {
		return new StyleRange(start, length, colorOrNullOutsideOfDisplayThread(SWT.COLOR_RED), null);
	}
	public static StyleRange styleRangeForHighlight(int start) {
		return new StyleRange(start, 0, null, colorOrNullOutsideOfDisplayThread(SWT.COLOR_YELLOW));
	}

	public static StyleRange styleRangeForDirectives(int start, int length) {
		return new StyleRange(start, length, colorOrNullOutsideOfDisplayThread(SWT.COLOR_GRAY), null);
	}

	public static StyleRange styleRangeForWildcard(int start, int length) {
		return new StyleRange(start, length, colorOrNullOutsideOfDisplayThread(SWT.COLOR_DARK_GREEN), null);
	}


	protected void printVariableReplacement(Object replacementVar) {
		int start = getCurrentCharacterIndex();
		this.buffer.append(replacementVar);
		styleRanges.add(styleRangeForVariable(start, getCurrentCharacterIndex() - start));	
	}

	protected void printExpReplacement(Object replacementExp) {
		int start = getCurrentCharacterIndex();
		this.buffer.append(replacementExp);
		styleRanges.add(styleRangeForExp(start, getCurrentCharacterIndex() - start));	
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

						if(isFirstElementOfList(node))
							printVariableReplacement(listReplacementVar);


						return false; //do not print node itself because list has been replaced
					}
				}



				if(hasBeenReplacedByWildcard(nodeListWrapper)) {
					if(listWrapperForWhichToIgnoreListDecorations.isEmpty() ||
							!nodeListWrapper.equals(listWrapperForWhichToIgnoreListDecorations.peek())) {

						if(isFirstElementOfList(node))
							printWildcardReplacement();

						return false;
					}	
				}


			}
		}
		Object replacementVar = getUserVar(node);
		if (replacementVar != null) {
			printVariableReplacement(replacementVar);
			return false;//do not print node itself because node has been replace
		} 

		Object replacementExp = getUserExp(node);
		if (replacementExp != null) {
			printExpReplacement(replacementExp);
			return false;//do not print node itself because node has been replaced
		} 

		if(hasBeenReplacedByWildcard(node)) {
			printWildcardReplacement();
			return false;
		}

		return true;
	}


	protected void printWildcardReplacement() {
		int start = getCurrentCharacterIndex();
		this.buffer.append("...");
		styleRanges.add(styleRangeForWildcard(start, getCurrentCharacterIndex() - start));	
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
			if(nodeListWrapper != null) {
				if(listWrapperForWhichToIgnoreListDecorations.isEmpty() ||
						!nodeListWrapper.equals(listWrapperForWhichToIgnoreListDecorations.peek())) {
					preVisitNodeListWrapper(nodeListWrapper);
				}
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



		StyleRange styleRange = new StyleRange();
		styleRange.start = getCurrentCharacterIndex();
		styleRange.data = getIdentifier(node);
		styleRange.underlineStyle = SWT.UNDERLINE_LINK;

		currentHyperlink.push(styleRange);
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

		StyleRange styleRange = (StyleRange) currentHyperlink.pop();
		styleRange.length = getCurrentCharacterIndex() - styleRange.start;
		hyperlinks.add(styleRange);
	}

	public LinkedList<StyleRange> getHyperlinks() {
		return hyperlinks;
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

	public Boolean hasBeenReplacedByWildcard(Object element) {
		return (Boolean) FN_SNIPPET_ELEMENT_REPLACEDBY_WILDCARD.invoke(snippet, element);
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
			return prettyPrintList(element);
		}

		if(isPrimitiveValueInTemplate(snippet, element)) {
			return prettyPrintPrimitive(element);
		} 

		if(isNullValueInTemplate(snippet, element)) {
			return prettyPrintNull(element);
		} 

		if(element == null) {
			this.buffer.append("null");	
			return getResult();
		} else
			throw new RuntimeException("Unexpected value to be pretty-printed: " + element.toString());


	}

	private String prettyPrintNull(Object element) {
		printOpeningNode(element);
		
		Object nullReplacementVar = getUserVar(element);
		if(nullReplacementVar != null) {
			printVariableReplacement(nullReplacementVar);
		} 
		else if(hasBeenReplacedByWildcard(element)) {
				printWildcardReplacement();
		} else {
			this.buffer.append("");	
			//this.buffer.append("null");	
		}
		printClosingNode(element);	
		return getResult();
	}

	private String prettyPrintPrimitive(Object element) {
		Object value = getActualPrimitiveValueInTemplate(snippet, element);
		printOpeningNode(element);
		Object primReplacementVar = getUserVar(element);
		if(primReplacementVar != null) {
			printVariableReplacement(primReplacementVar);
		} else if(hasBeenReplacedByWildcard(element)) {
			printWildcardReplacement();
		} else {
			Object replacementExp = getUserExp(element);
			if (replacementExp != null) {
				printExpReplacement(replacementExp);
			} 	
			else { 
				this.buffer.append(value.toString());
			}
		}
		printClosingNode(element);
		return getResult();
	}

	public boolean hasBeenReplacedByVariable(Object element) {
		Object listReplacementVar = getUserVar(element);
		return listReplacementVar != null;
	}
	
	private String prettyPrintList(Object element, String separator) {
		printOpeningNode(element);

		Object listReplacementVar = getUserVar(element);
		if(listReplacementVar != null) {
			printVariableReplacement(listReplacementVar);
			printClosingNode(element);
			return getResult();
		}

		if(hasBeenReplacedByWildcard(element)) {
			printWildcardReplacement();
			printClosingNode(element);
			return getResult();
		}

		listWrapperForWhichToIgnoreListDecorations.push(element);

		@SuppressWarnings("rawtypes")
		Collection lst = getActualListValueInTemplate(snippet, element);

		for(Object member : lst) {
			prettyPrintElement(snippet, member);
			this.buffer.append(separator);
		}

		if(!lst.isEmpty()) {
			this.buffer.delete(getCurrentCharacterIndex() - separator.length(), getCurrentCharacterIndex());
		}

		listWrapperForWhichToIgnoreListDecorations.pop();
		printClosingNode(element);

		return getResult().trim();
	}
	
	private String prettyPrintList(Object element) {
		return prettyPrintList(element, " ");
	}
		
	

	public String prettyPrint() {
		for(Object groupElement : templateGroup.getSnippets()) {
			prettyPrintSnippet(groupElement);

			if(this.buffer.codePointBefore(getCurrentCharacterIndex()) != '\n')			
				this.buffer.append("\n\n");
		}
		return getResult();
	}


	public void prettyPrintArrow() {
		int start = getCurrentCharacterIndex();
		this.buffer.append("\n=>\n\n");
		styleRanges.add(styleRangeForMeta(start, getCurrentCharacterIndex() - start));	
	}


	public String prettyPrintSnippet(Object snippet) {
		setSnippet(snippet);
		ASTNode root = TemplateGroup.getRootOfSnippet(snippet); 
		root.accept(this);

		/*
		Collection conditions = templateGroup.getLogicConditions(snippet);
		this.buffer.append('\n');
		this.buffer.append(Joiner.on('\n').join(conditions));
		 */

		return getResult();
	}
	
	
	
	
	
	/*
	 * 
	 * Special cases
	 * 
	 */
	@Override
	public boolean visit(SimpleName node) {
		prettyPrintPrimitive(getValueOfProperty(node, SimpleName.IDENTIFIER_PROPERTY));
		return false;
	}

	
	@Override
	public boolean visit(TypeDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		prettyPrintList(getValueOfProperty(node, TypeDeclaration.MODIFIERS2_PROPERTY), " ");
		this.buffer.append(" "); 

		
		this.buffer.append(node.isInterface() ? "interface " : "class ");
		node.getName().accept(this);
		
		
		Object typeParamList = getValueOfProperty(node, TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
		if(!isInvisibleList(typeParamList)) {
			this.buffer.append("<"); 
			prettyPrintList(typeParamList, ",");
			this.buffer.append(">"); 
		}
		
		this.buffer.append(" "); 
			if (node.getSuperclassType() != null) {
				this.buffer.append("extends "); 
				node.getSuperclassType().accept(this);
				this.buffer.append(" "); 
			}
			if (!node.superInterfaceTypes().isEmpty()) {
				this.buffer.append(node.isInterface() ? "extends " : "implements ");//$NON-NLS-2$ 
				
				prettyPrintList(getValueOfProperty(node, TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY), ", ");
				
				this.buffer.append(" "); 
			}
		this.buffer.append("{\n"); 
		this.indent++;

		prettyPrintList(getValueOfProperty(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY), "\n");	
		
		this.buffer.append("  \n"); 
		
		this.indent--;
		printIndent();
		this.buffer.append("}"); 
		return false;
	}
	
	
	@Override
	public boolean visit(FieldDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();

		prettyPrintList(getValueOfProperty(node, FieldDeclaration.MODIFIERS2_PROPERTY));	
		this.buffer.append(" "); 

		
		node.getType().accept(this);
		this.buffer.append(" ");
		
		prettyPrintList(getValueOfProperty(node, FieldDeclaration.FRAGMENTS_PROPERTY), ", ");
			
		this.buffer.append(";\n");//$NON-NLS-1$
		return false;
	}
	
	private boolean isInvisibleList(Object list) {
		if(hasBeenReplacedByVariable(list) || hasBeenReplacedByWildcard(list))
			return false;
		else 
			return getActualListValueInTemplate(snippet, list).isEmpty();
	}
	
	private boolean isInvisibleNullValue(Object nullvalue) {
		return !(hasBeenReplacedByVariable(nullvalue) || hasBeenReplacedByWildcard(nullvalue));
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.buffer.append(".");
		}
				
		Object typeArgList = getValueOfProperty(node, MethodInvocation.TYPE_ARGUMENTS_PROPERTY);
		if(!isInvisibleList(typeArgList)) {
				this.buffer.append("<");
				prettyPrintList(typeArgList, ",");
				this.buffer.append(">");
		}
			
		node.getName().accept(this);
		this.buffer.append("(");
		prettyPrintList(getValueOfProperty(node, MethodInvocation.ARGUMENTS_PROPERTY), ",");
		this.buffer.append(")");
		return false;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printIndent();
		prettyPrintList(getValueOfProperty(node, MethodDeclaration.MODIFIERS2_PROPERTY));
		this.buffer.append(" "); 

		Object typeParamList = getValueOfProperty(node, MethodDeclaration.TYPE_PARAMETERS_PROPERTY);
		if(!isInvisibleList(typeParamList)) {
				this.buffer.append("<");
				prettyPrintList(typeParamList, ",");
				this.buffer.append(">");
		}
			
		if (!node.isConstructor()) {
			if (node.getReturnType2() != null) {
				node.getReturnType2().accept(this);
			} else {
				// methods really ought to have a return type
				this.buffer.append("void");
			}
			this.buffer.append(" ");
		}
		
		node.getName().accept(this);
		this.buffer.append("(");//$NON-NLS-1$
		
		
	
		Type receiverType = node.getReceiverType();
		if (receiverType != null) {
				receiverType.accept(this);
				this.buffer.append(' ');
				SimpleName qualifier = node.getReceiverQualifier();
				if (qualifier != null) {
					qualifier.accept(this);
					this.buffer.append('.');
				}
				this.buffer.append("this"); //$NON-NLS-1$
				if (node.parameters().size() > 0) {
					this.buffer.append(',');
				}
			}
		
		prettyPrintList(getValueOfProperty(node, MethodDeclaration.PARAMETERS_PROPERTY),",");
		
		this.buffer.append(")");//$NON-NLS-1$
				
		prettyPrintList(getValueOfProperty(node, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY), "");
		
		Object thrownList = getValueOfProperty(node, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
		if(!isInvisibleList(thrownList)) {
			this.buffer.append(" throws ");
			prettyPrintList(thrownList, ", ");
			this.buffer.append(" ");//$NON-NLS-1$				
		}
				
		Object body = getValueOfProperty(node, MethodDeclaration.BODY_PROPERTY);
		if(isNullValueInTemplate(snippet, body)) {
			if(isInvisibleNullValue(body)) {
				this.buffer.append(";\n");
			} else {
				prettyPrintNull(body);
			}
		} else {
			this.buffer.append(" ");
			prettyPrintElement(snippet, body);
		}
		return false;
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		Object expression = getValueOfProperty(node, ClassInstanceCreation.EXPRESSION_PROPERTY);
		if(isNullValueInTemplate(snippet, expression)) {
			if(!isInvisibleNullValue(expression)) {
				prettyPrintNull(expression);
				this.buffer.append(".");
			}
		} else {
			prettyPrintElement(snippet, expression);
			this.buffer.append(".");
		}
		
		this.buffer.append("new ");
		
		
		Object typeArgList = getValueOfProperty(node, ClassInstanceCreation.TYPE_ARGUMENTS_PROPERTY);
		if(!isInvisibleList(typeArgList)) {
				this.buffer.append("<");
				prettyPrintList(typeArgList, ",");
				this.buffer.append(">");
		}
			
		node.getType().accept(this);
		
			
		this.buffer.append("(");//$NON-NLS-1$
		prettyPrintList(getValueOfProperty(node, ClassInstanceCreation.ARGUMENTS_PROPERTY), ", ");
		this.buffer.append(")");//$NON-NLS-1$
		
		
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}



	

}
