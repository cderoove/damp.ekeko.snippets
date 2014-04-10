package damp.ekeko.snippets.gui;

import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateCodeGenerator extends TemplatePrettyPrinter {

	private Map<Object, Object> varsToValues;

	public TemplateCodeGenerator(TemplateGroup group, Map<Object, Object> varsToValues) {
		super(group);
		this.varsToValues = varsToValues;
	}
	
	@Override
	public void printOpeningNode(Object node) {
	}
	
	@Override
	public void printClosingNode(Object node) {
	}
	
	public Object getVariableBinding(Object var) {
		return varsToValues.get(var);
	}
	
	public Object getUnwrappedValue(Object templateElement) {
		//TODO: value can also stem form LHS template!
		
		
		if(isNodeValueInTemplate(snippet, templateElement))
			return templateElement;
		if(isListValueInTemplate(snippet, templateElement))
			return getActualListValueInTemplate(snippet, templateElement);
		if(isPrimitiveValueInTemplate(snippet, templateElement))
			return getActualPrimitiveValueInTemplate(snippet, templateElement);
		if(isNullValueInTemplate(snippet, templateElement))
			return null;
		//throw new RuntimeException("Unexpected template value to be unwrapped: " + templateElement.toString());]
		return null;
	}
	
	public String getStringForTemplateElement(Object templateElement) {
		Object unwrappedValue = getUnwrappedValue(templateElement);
		if(unwrappedValue == null)
			return "" + templateElement;
		return unwrappedValue.toString();
	}
	
	@Override
	protected void printVariableReplacement(Object replacementVar) {
		Object variableBinding = getVariableBinding("" + replacementVar);
		String variableBindingString = getStringForTemplateElement(variableBinding);
		this.buffer.append(variableBindingString);
	}
	
	@Override
	public String prettyPrintSnippet(Object snippet) {
		setSnippet(snippet);
		ASTNode root = TemplateGroup.getRootOfSnippet(snippet); 
		root.accept(this);
		System.out.println(getResult());
		return getResult();
	}


}
