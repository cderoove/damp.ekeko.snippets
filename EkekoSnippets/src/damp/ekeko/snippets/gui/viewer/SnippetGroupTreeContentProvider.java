package damp.ekeko.snippets.gui.viewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class SnippetGroupTreeContentProvider implements ITreeContentProvider {

	private Object group;
	private TreeViewer viewer;
	
	static {
		RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.gui"));
	}

	public SnippetGroupTreeContentProvider() {
	}
	
	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
		group = newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null)
			return null;
		return (Object[]) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewer-elements").invoke(getGroup(), inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == null)
			return null;
		return (Object[]) RT.var("damp.ekeko.snippets.gui", "snippetgroupviewer-children").invoke(getGroup(), parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element == null)
			return null;
		return RT.var("damp.ekeko.snippets.gui", "snippetgroupviewer-parent").invoke(getGroup(), element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == null)
			return false;
		return getChildren(element).length > 0;

	}

	public Object getGroup() {
		return group;
	}


}
