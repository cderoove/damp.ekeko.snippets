package damp.ekeko.snippets.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import clojure.lang.IFn;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateTreeLabelProviders {
	
	public static IFn FN_LABELPROVIDER_NODE;
	public static IFn FN_LABELPROVIDER_KIND;
	public static IFn FN_LABELPROVIDER_PROPERTY;
	public static IFn FN_LABELPROVIDER_DIRECTIVES;
	
	
	

	public abstract static class SnippetGroupColumnLabelProvider extends ColumnLabelProvider {
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

}
