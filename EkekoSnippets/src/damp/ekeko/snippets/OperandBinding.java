package damp.ekeko.snippets;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;


public class OperandBinding {
	
	public static IFn FN_LABELPROVIDER_OPERANDBINDING_VALUE;
	public static IFn FN_LABELPROVIDER_OPERANDBINDING_DESCRIPTION;

	public ColumnLabelProvider getValueLabelProvider() {
		return (ColumnLabelProvider) FN_LABELPROVIDER_OPERANDBINDING_VALUE.invoke(this);
	}
	
	public ColumnLabelProvider getDescriptionLabelProvider() {
		return (ColumnLabelProvider) FN_LABELPROVIDER_OPERANDBINDING_DESCRIPTION.invoke(this);
	}
		
	public OperandBinding(Object operand, Object group, Object template, Object value) {
		this.setOperand(operand);
		this.setGroup(group);
		this.setTemplate(template);
		this.setValue(value);
	}

	public Object getTemplate() {
		return template;
	}
	public void setTemplate(Object template) {
		this.template = template;
	}

	public Object getOperand() {
		return operand;
	}

	public void setOperand(Object operand) {
		this.operand = operand;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getGroup() {
		return group;
	}

	public void setGroup(Object group) {
		this.group = group;
	}

	public Object template;
	public Object operand;
	public Object value;
	public Object group;

	
}
