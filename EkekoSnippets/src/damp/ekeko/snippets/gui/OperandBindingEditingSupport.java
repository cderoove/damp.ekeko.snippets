package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import clojure.lang.IFn;

public class OperandBindingEditingSupport extends EditingSupport {

	public static IFn FN_OPERANDBINDING_VALUE;
	public static IFn FN_UPDATE_OPERANDBINDING_VALUE;
	public static IFn FN_OPERANDBINDING_TEMPLATE;

	
	TableViewer operandsTableViewer;
	
	public OperandBindingEditingSupport(TableViewer viewer) {
		super(viewer);
		operandsTableViewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(operandsTableViewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return FN_OPERANDBINDING_VALUE.invoke(element);
	}

	@Override
	protected void setValue(Object element, Object value) {
		FN_UPDATE_OPERANDBINDING_VALUE.invoke(element, value);
		operandsTableViewer.update(element, null);
	}

}
