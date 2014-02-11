package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;

public class TemplateViewTreeLabelProviders {
	
	public static IFn FN_LABELPROVIDER_NODE;
	public static IFn FN_LABELPROVIDER_KIND;
	public static IFn FN_LABELPROVIDER_PROPERTY;
	
	
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
			return (String) FN_LABELPROVIDER_NODE.invoke(getGroup(), element);
		}
	}
	
	public static class KindColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public KindColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) FN_LABELPROVIDER_KIND.invoke(getGroup(), element);
		}
	}


	public static class PropertyColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public PropertyColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) FN_LABELPROVIDER_PROPERTY.invoke(getGroup(), element);
		}
	}


}
