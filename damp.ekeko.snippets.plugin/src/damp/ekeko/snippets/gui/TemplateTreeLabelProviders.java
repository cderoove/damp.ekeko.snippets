package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import clojure.lang.IFn;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateTreeLabelProviders {


	public static IFn FN_LABELPROVIDER_NODE;
	public static IFn FN_LABELPROVIDER_KIND;
	public static IFn FN_LABELPROVIDER_PROPERTY;
	public static IFn FN_LABELPROVIDER_DIRECTIVES;




	//These were ColumnLabelProviders before, 
	//but since the provider for one column was changed to StyledCellLabelProvider
	//had to change them all to ensure consistency of rendering (StyledCellLabelProvider=platform-specific, ColumnLabelProvider=generic)
	public abstract static class SnippetGroupColumnLabelProvider extends StyledCellLabelProvider {
		protected Object  cljSnippetGroup;
		protected TemplateGroup jTemplateGroup;


		public SnippetGroupColumnLabelProvider(Object cljSnippetGroup) {
			this.cljSnippetGroup = cljSnippetGroup;
			this.jTemplateGroup = TemplateGroup.newFromClojureGroup(cljSnippetGroup);
		}

		protected Object getCljGroup() {
			return  cljSnippetGroup;
		}

		protected TemplateGroup getJGroup() {
			return  jTemplateGroup;
		}
		
		public String getText(Object element) {
			return element.toString();
		}
		
		public void internalUpdate(ViewerCell cell) {
			Object element = cell.getElement();
			cell.setText(getText(element));
		}
		
		@Override
		public void update(ViewerCell cell) {
			internalUpdate(cell);
			super.update(cell);
		}
	}

	public static class NodeColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public NodeColumnLabelProvider(Object s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) FN_LABELPROVIDER_NODE.invoke(getCljGroup(), element);
		}
	}

	public static class KindColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public KindColumnLabelProvider(Object s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) FN_LABELPROVIDER_KIND.invoke(getCljGroup(), element);
		}
	}

	public static class PropertyColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public PropertyColumnLabelProvider(Object s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) FN_LABELPROVIDER_PROPERTY.invoke(getCljGroup(), element);
		}
	}

	public static class DirectivesColumnLabelProvider extends SnippetGroupColumnLabelProvider {

		public DirectivesColumnLabelProvider(Object s) {
			super(s);
		}

		public String getText(Object element) {
			return (String) TemplatePrettyPrinter.boundDirectivesString(getJGroup().getSnippet(element), element);
		}
	}

	public static class ElementColumnLabelProvider extends SnippetGroupColumnLabelProvider {


		public ElementColumnLabelProvider(Object cljGroup) {
			super(cljGroup);
		}
		
		@Override
		public void internalUpdate(ViewerCell cell) {
			Object element = cell.getElement();
			TemplatePrettyPrinter tpp = new TemplatePrettyPrinter(getJGroup());
			String text = tpp.prettyPrintElement(getJGroup().getSnippet(element), element);
			cell.setText(text);
			cell.setStyleRanges(tpp.getStyleRanges());
		}
	}


}
