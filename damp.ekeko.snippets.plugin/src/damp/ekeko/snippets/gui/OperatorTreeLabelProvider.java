package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;

public class OperatorTreeLabelProvider extends ColumnLabelProvider {
	
	public static IFn FN_LABELPROVIDER_OPERATOR;	
		
	public String getText(Object element) {
			return (String) FN_LABELPROVIDER_OPERATOR.invoke(element);
	}
	
	
}