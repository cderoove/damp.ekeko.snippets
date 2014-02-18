package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class OperandBindingValueLabelProvider extends ColumnLabelProvider {
		
	@Override
	public String getText(Object element) {
	  return OperandBindingEditingSupport.FN_OPERANDBINDING_VALUE.invoke(element).toString();  
	}

}
