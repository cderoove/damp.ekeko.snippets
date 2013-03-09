package damp.ekeko.snippets;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.RT;
import clojure.lang.Symbol;
import damp.ekeko.snippets.gui.SnippetView;

public class SnippetGroupTreeLabelProviders {

	static {	
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
	}		
	
	public abstract static class SnippetGroupColumnLabelProvider extends ColumnLabelProvider {
		protected SnippetView snippetViewer;
		
		public SnippetGroupColumnLabelProvider(SnippetView s) {
			snippetViewer = s;
		}
		
		protected Object getGroup() {
			return snippetViewer.getContentProvider().getGroup();
		}
		
	}
	
	public static class NodeColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public NodeColumnLabelProvider(SnippetView s) {
			super(s);
		}
		
		public String getText(Object element) {
			System.out.println("nodecol"+element);
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-node").invoke(getGroup(), element);
		}
	}


	public static class VariableColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public VariableColumnLabelProvider(SnippetView s) {
			super(s);
		}
		
		public String getText(Object element) {
			System.out.println("varcol"+element);
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewercolumn-variable").invoke(getGroup(), element);
		}
	}


}
