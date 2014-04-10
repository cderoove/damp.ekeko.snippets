package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import damp.ekeko.snippets.DirectiveOperandBinding;
import damp.ekeko.snippets.data.TemplateGroup;

public class DirectiveOperandBindingLabelProviderValue extends StyledCellLabelProvider {

	private TemplateGroup javaGroup;
	private Object cljSnippet, cljSelectedSnippetNode;
	
	public DirectiveOperandBindingLabelProviderValue(Object cljGroup,
			Object cljSnippet, Object cljSelectedSnippetNode) {
		javaGroup = TemplateGroup.newFromClojureGroup(cljGroup);
		this.cljSnippet = cljSnippet;
		this.cljSelectedSnippetNode = cljSelectedSnippetNode;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		DirectiveOperandBinding dob = (DirectiveOperandBinding) element;
		Object value = dob.getValue();
		//pretty print the implicit match operand
		if(cljSnippet != null && 
				cljSelectedSnippetNode != null 
				&& cljSelectedSnippetNode.equals(value)) {
			TemplatePrettyPrinter tpp = new TemplatePrettyPrinter(javaGroup);
			String text = tpp.prettyPrintElement(cljSnippet, value);
			cell.setText(text);
			cell.setStyleRanges(tpp.getStyleRanges());
		} else {
			cell.setText("" + value);
		}
	}


}
