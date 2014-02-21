package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;

public class OperandBindingLabelProviderDescription extends ColumnLabelProvider {

	public static IFn FN_LABELPROVIDER_DESCRIPTION_TEXT;
	
	@Override
	public String getText(Object element) {
	  return (String) FN_LABELPROVIDER_DESCRIPTION_TEXT.invoke(element);  
	}

}
