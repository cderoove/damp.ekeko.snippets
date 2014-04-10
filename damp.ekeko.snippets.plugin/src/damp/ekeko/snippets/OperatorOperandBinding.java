package damp.ekeko.snippets;

public class OperatorOperandBinding extends DirectiveOperandBinding {
			
	public OperatorOperandBinding(Object operand, Object group, Object template, Object value) {
		super(operand, value);
		this.setGroup(group);
		this.setTemplate(template);
	}

	public Object getTemplate() {
		return template;
	}
	public void setTemplate(Object template) {
		this.template = template;
	}

	public Object getGroup() {
		return group;
	}

	public void setGroup(Object group) {
		this.group = group;
	}

	public Object template;
	public Object group;

	
}
