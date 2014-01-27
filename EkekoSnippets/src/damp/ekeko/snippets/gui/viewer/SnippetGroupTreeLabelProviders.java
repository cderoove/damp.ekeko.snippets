package damp.ekeko.snippets.gui.viewer;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.RT;
import clojure.lang.Symbol;
import damp.ekeko.snippets.gui.TemplateView;

public class SnippetGroupTreeLabelProviders {

	static {	
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
	}		
	
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
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-node").invoke(getGroup(), element);
		}
	}

	public static class PropertyColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public PropertyColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-property").invoke(getGroup(), element);
		}
	}


	public static class VariableColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public VariableColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-variable").invoke(getGroup(), element);
		}
	}

	public static class FlagColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public FlagColumnLabelProvider(TemplateView s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-flag").invoke(getGroup(), element);
		}
	}

}
