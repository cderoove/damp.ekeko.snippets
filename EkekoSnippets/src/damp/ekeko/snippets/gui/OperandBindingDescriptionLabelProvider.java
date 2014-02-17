package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;

public class OperandBindingDescriptionLabelProvider extends ColumnLabelProvider {
	
	public static IFn FN_BINDING_OPERAND_DESCRIPTION;
	
	@Override
	public String getText(Object element) {
	  return (String) FN_BINDING_OPERAND_DESCRIPTION.invoke(element);  
	}

}
