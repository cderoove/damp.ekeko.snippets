package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

import clojure.lang.IFn;
import damp.ekeko.snippets.DirectiveOperandBinding;

public class OperatorOperandBindingEditingSupport extends EditingSupport {

	public static IFn FN_OPERANDBINDING_EDITOR;
	
	TableViewer operandsTableViewer;
	
	public OperatorOperandBindingEditingSupport(TableViewer viewer) {
		super(viewer);
		operandsTableViewer = viewer;
	}

	//NOTE: if the Eclipse GUI hangs, there is a Clojure error in the multi-method that is dispatched to
	//such errors seem no to propagate outside of the event loop
	@Override
	protected CellEditor getCellEditor(Object element) {
		Table table = operandsTableViewer.getTable();
		Object returned = FN_OPERANDBINDING_EDITOR.invoke(table, element);
		return (CellEditor) returned; 
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((DirectiveOperandBinding) element).getValue();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((DirectiveOperandBinding) element).setValue(value);
		operandsTableViewer.update(element, null);
	}

}
