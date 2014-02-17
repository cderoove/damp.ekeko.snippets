package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class OperandBindingValueLabelProvider extends ColumnLabelProvider {
		
	@Override
	public String getText(Object element) {
	  return (String) OperandBindingEditingSupport.FN_OPERANDBINDING_VALUE.invoke(element);  
	}

}
