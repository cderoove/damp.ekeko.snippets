package damp.ekeko.snippets;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetTreeLabelProviders {

	static {	
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
	}		
	
	public abstract static class SnippetColumnLabelProvider extends ColumnLabelProvider {
		protected SnippetViewer snippetViewer;
		
		public SnippetColumnLabelProvider(SnippetViewer s) {
			snippetViewer = s;
		}
		
		protected Object getSnippet() {
			return snippetViewer.getContentProvider().getSnippet();
		}
		
	}
	
	public static class NodeColumnLabelProvider extends SnippetColumnLabelProvider {

		public NodeColumnLabelProvider(SnippetViewer s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-node").invoke(getSnippet(), element);
		}
	}


	public static class KindColumnLabelProvider extends SnippetColumnLabelProvider {

		public KindColumnLabelProvider(SnippetViewer s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-kind").invoke(getSnippet(), element);
		}
	}

	public static class PropertyColumnLabelProvider extends SnippetColumnLabelProvider {

		public PropertyColumnLabelProvider(SnippetViewer s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-property").invoke(getSnippet(), element);
		}
	}

	
	
	public static class VariableColumnLabelProvider extends SnippetColumnLabelProvider {

		public VariableColumnLabelProvider(SnippetViewer s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-variable").invoke(getSnippet(), element);
		}
	}

	public static class GrounderColumnLabelProvider extends SnippetColumnLabelProvider {

		public GrounderColumnLabelProvider(SnippetViewer s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-grounder").invoke(getSnippet(), element);
		}
	}
	
	public static class ConstrainerColumnLabelProvider extends SnippetColumnLabelProvider {

		public ConstrainerColumnLabelProvider(SnippetViewer s) {
			super(s);
		}
		
		public String getText(Object element) {
			return (String) RT.var("damp.ekeko.snippets.gui", "snippetviewercolumn-constrainer").invoke(getSnippet(), element);
		}
	}

}
