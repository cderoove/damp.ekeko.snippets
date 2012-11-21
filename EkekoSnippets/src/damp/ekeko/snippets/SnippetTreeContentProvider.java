package damp.ekeko.snippets;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetTreeContentProvider implements ITreeContentProvider {

	private Object snippet;
	private TreeViewer viewer;
	
	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
	}

	public SnippetTreeContentProvider() {
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		snippet = newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null)
			return null;
		return (Object[]) RT.var("damp.ekeko.snippets.gui", "snippetviewer-elements").invoke(getSnippet(), inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == null)
			return null;
		return (Object[]) RT.var("damp.ekeko.snippets.gui", "snippetviewer-children").invoke(getSnippet(), parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element == null)
			return null;

		return RT.var("damp.ekeko.snippets.gui", "snippetviewer-parent").invoke(getSnippet(), element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == null)
			return false;
		return getChildren(element).length > 0;

	}

	public Object getSnippet() {
		return snippet;
	}


}
