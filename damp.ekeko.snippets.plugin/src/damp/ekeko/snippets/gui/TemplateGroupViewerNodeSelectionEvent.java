package damp.ekeko.snippets.gui;

import java.util.EventObject;

import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateGroupViewerNodeSelectionEvent extends EventObject {

	private Object cljTemplate, cljNode;

	private TemplateGroup jGroup;
	
	public TemplateGroupViewerNodeSelectionEvent(Object source, TemplateGroup jGroup, Object cljTemplate, Object cljNode) {
		super(source);
		this.jGroup = jGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}
	
	public Object getSelectedTemplateNode() {
		return cljNode;
	}
	
	public Object getSelectedTemplate() {
		return cljTemplate;
	}
	
	public TemplateGroup getSelectedTemplateGroup() {
		return jGroup;
	}
	
}
