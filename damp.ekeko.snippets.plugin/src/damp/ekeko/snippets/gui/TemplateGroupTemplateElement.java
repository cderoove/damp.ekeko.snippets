package damp.ekeko.snippets.gui;


public class TemplateGroupTemplateElement  {

	private Object group, template, value;

	public TemplateGroupTemplateElement(Object cljGroup, Object cljTemplate, Object cljNode) {
		this.group = cljGroup;
		this.template = cljTemplate;
		this.value = cljNode;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
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
		
}
