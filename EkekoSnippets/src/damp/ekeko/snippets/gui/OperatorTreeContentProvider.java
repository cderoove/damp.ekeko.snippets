package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import clojure.lang.IFn;

public class OperatorTreeContentProvider implements ITreeContentProvider {

	private Object selectedSnippetNode;
	
	public static IFn FN_ELEMENTS;
	public static IFn FN_CHILDREN;
	public static IFn FN_PARENT;
	
	public OperatorTreeContentProvider() {
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		selectedSnippetNode = newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null)
			return null;
		return (Object[]) FN_ELEMENTS.invoke(selectedSnippetNode, inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == null)
			return null;
		return (Object[]) FN_CHILDREN.invoke(selectedSnippetNode, parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element == null)
			return null;
		return FN_PARENT.invoke(selectedSnippetNode, element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == null)
			return false;
		return getChildren(element).length > 0;

	}

	@Override
	public void dispose() {
		
	}


}
