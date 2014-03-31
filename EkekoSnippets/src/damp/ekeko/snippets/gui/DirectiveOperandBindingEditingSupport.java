package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import damp.ekeko.snippets.DirectiveOperandBinding;

public class DirectiveOperandBindingEditingSupport extends EditingSupport {

	private Object cljTemplateGroup, cljTemplate, cljNode;
	private TableViewer tableViewer;
	private BoundDirectivesViewer boundDirectivesViewer;
	
	public DirectiveOperandBindingEditingSupport(BoundDirectivesViewer boundDirectivesViewer, TableViewer viewer, Object cljTemplateGroup, Object cljTemplate, Object cljNode) {
		super(viewer);
		this.boundDirectivesViewer = boundDirectivesViewer; 
		this.tableViewer = viewer;
		this.cljTemplateGroup = cljTemplateGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(tableViewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		if(element instanceof DirectiveOperandBinding) {
			//only allow editing operands that do not correspond to the implicit match 
			DirectiveOperandBinding dob = (DirectiveOperandBinding) element;
			if(cljNode == null)
				return false;
			return !(cljNode.equals(dob.getValue()));
		}
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof DirectiveOperandBinding) {
			DirectiveOperandBinding dob = (DirectiveOperandBinding) element;
			return "" + dob.getValue();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof DirectiveOperandBinding) {
			DirectiveOperandBinding dob = (DirectiveOperandBinding) element;
			dob.setValue(value);
			tableViewer.update(element, null);
			boundDirectivesViewer.updateWidgets();
		}
	}
}
