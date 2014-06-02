package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

import clojure.lang.IFn;
import damp.ekeko.snippets.DirectiveOperandBinding;

public class OperatorOperandBindingEditingSupport extends EditingSupport {

	public static IFn FN_OPERANDBINDING_EDITOR;
	
	OperatorOperandsViewer operatorOperandsViewer;
	
	public OperatorOperandBindingEditingSupport(OperatorOperandsViewer operatorOperandsViewer) {
		super(operatorOperandsViewer.getTableViewer());
		this.operatorOperandsViewer = operatorOperandsViewer;
	}

	//NOTE: if the Eclipse GUI hangs, there is a Clojure error in the multi-method that is dispatched to
	//such errors seem no to propagate outside of the event loop
	@Override
	protected CellEditor getCellEditor(Object element) {
		Table table = operatorOperandsViewer.getTableViewer().getTable();
		Object returned = FN_OPERANDBINDING_EDITOR.invoke(table, operatorOperandsViewer, element);
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
		operatorOperandsViewer.getTableViewer().update(element, null);
	}

}
