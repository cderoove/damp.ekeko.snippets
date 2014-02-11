package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.RT;

public class TemplateViewTreeLabelProviders {
	
	public abstract static class SnippetGroupColumnLabelProvider extends ColumnLabelProvider {
		protected TemplateView snippetViewer;
		
		public SnippetGroupColumnLabelProvider(TemplateView s) {
			snippetViewer = s;
		}
		
		protected Object getGroup() {
			return snippetViewer.getContentProvider().getGroup();
		}
		
	}
	
	public static class NodeColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public NodeColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "templateviewtreelabelprovider-node").invoke(getGroup(), element);
		}
	}

	public static class PropertyColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public PropertyColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "templateviewtreelabelprovider-property").invoke(getGroup(), element);
		}
	}


}
