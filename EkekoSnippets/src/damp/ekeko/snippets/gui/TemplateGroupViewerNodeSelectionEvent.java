package damp.ekeko.snippets.gui;

import java.util.EventObject;

public class TemplateGroupViewerNodeSelectionEvent extends EventObject {

	private Object cljGroup, cljTemplate, cljNode;

	public TemplateGroupViewerNodeSelectionEvent(Object source, Object cljGroup, Object cljTemplate, Object cljNode) {
		super(source);
		this.cljGroup = cljGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}
	
	public Object getSelectedTemplateNode() {
		return cljNode;
	}
	
	public Object getSelectedTemplate() {
		return cljTemplate;
	}
	
	public Object getSelectedTemplateGroup() {
		return cljGroup;
	}
	
}
