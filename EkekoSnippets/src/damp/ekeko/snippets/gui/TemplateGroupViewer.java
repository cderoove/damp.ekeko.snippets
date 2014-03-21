package damp.ekeko.snippets.gui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class  TemplateGroupViewer extends Composite {

	private TextViewer textViewerSnippet;
	private TreeViewer snippetTreeViewer;
	private TreeViewerColumn snippetKindCol;
	private TreeViewerColumn snippetPropCol;
	private TreeViewerColumn snippetNodeCol;
	private TreeViewerColumn snippetDirectivesCol;

	private List<TemplateGroupViewerNodeSelectionListener> nodeSelectionListeners; 

	private Object cljGroup, cljTemplate, cljNode;
	private Table directivesTable;
	private TableViewer directivesTableViewer;
	private TreeViewerColumn snippetElementCol;
	//private TextViewer textViewerNode;

	public TemplateGroupViewer(Composite parent, int style) {
		super(parent, SWT.NONE);
		
		nodeSelectionListeners = new LinkedList<TemplateGroupViewerNodeSelectionListener>();
		
		//Composite composite = new Composite(parent, SWT.NONE);
		Composite composite = this;
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);
		textViewerSnippet = new TextViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		StyledText styledText = textViewerSnippet.getTextWidget();
		GridData gd_styledText = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_styledText.heightHint = 100;
		styledText.setLayoutData(gd_styledText);
		textViewerSnippet.setEditable(false);		
		styledText.setFont(EkekoSnippetsPlugin.getEditorFont());
		styledText.setCaret(null);

		
		//Label label = new Label(getShell(), SWT.SINGLE);
		//RGB background = label.getBackground().getRGB();
		//label.dispose();

		
		/*
		textViewerNode = new TextViewer(composite, SWT.NONE | SWT.WRAP | SWT.READ_ONLY | SWT.NO_FOCUS);
		StyledText textViewerNodeText = textViewerNode.getTextWidget();
		GridData gd_styledTextNode = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		//gd_styledTextNode.heightHint = 100;
		textViewerNodeText.setLayoutData(gd_styledTextNode);
		//textViewerNodeText.setFont(EkekoSnippetsPlugin.getEditorFont());
		textViewerNodeText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		textViewerNodeText.setCaret(null);
		*/

		snippetTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		snippetTreeViewer.setAutoExpandLevel(3);
		Tree treeSnippet = snippetTreeViewer.getTree();
		treeSnippet.setHeaderVisible(true);
		treeSnippet.setLinesVisible(true);
		treeSnippet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		snippetNodeCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn trclmnNode = snippetNodeCol.getColumn();
		trclmnNode.setWidth(150);
		trclmnNode.setText("Element");

		snippetElementCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn templateElementColCol = snippetElementCol.getColumn();
		templateElementColCol.setWidth(150);
		templateElementColCol.setText("Textual Representation");
		
		snippetKindCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn snippetKindColCol = snippetKindCol.getColumn();
		snippetKindColCol.setWidth(150);
		snippetKindColCol.setText("Element Kind");

		snippetPropCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(150);
		trclmnProperty.setText("Description");
		
		snippetDirectivesCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn snippetDirectivesColCol = snippetDirectivesCol.getColumn();
		snippetDirectivesColCol.setWidth(150);
		snippetDirectivesColCol.setText("Directives");

		
		

		snippetTreeViewer.setContentProvider(new TemplateTreeContentProvider());
		
		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				onNodeSelectionInternal();
			}
		});	
		
		
		
		/*
		
		TO BE MOVED
		
		directivesTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		directivesTable = directivesTableViewer.getTable();
		directivesTable.setLinesVisible(true);
		directivesTable.setHeaderVisible(true);
		GridData gd_tableDirectives = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableDirectives.heightHint = 31;
		directivesTable.setLayoutData(gd_tableDirectives);
		
		directivesTableViewer.setContentProvider(new ArrayContentProvider());
		
		//operandsTableDecorator = new OperandsTableDecorator(operandsTable);
		
		TableViewerColumn directiveNameCol = new TableViewerColumn(directivesTableViewer, SWT.NONE);
		TableColumn directiveNameColCol = directiveNameCol.getColumn();
		directiveNameColCol.setWidth(150);
		directiveNameColCol.setText("Directive");
		directiveNameCol.setLabelProvider(new DirectiveNameLabelProviderDescription());
		directiveNameCol.setEditingSupport(new DirectiveNameEditingSupport(directivesTableViewer));

		
		TableViewerColumn operandValueCol = new TableViewerColumn(operandsTableViewer, SWT.NONE);
		TableColumn operandValueColCol = operandValueCol.getColumn();
		operandValueColCol.setWidth(150);
		operandValueColCol.setText("Value");
		operandValueCol.setLabelProvider(new OperandBindingLabelProviderValue());

		operandValueCol.setEditingSupport(new OperandBindingEditingSupport(operandsTableViewer));
		
		*/

		
	}
	
	private void onNodeSelectionInternal() {
		updateTextFields();
		TemplateGroupViewerNodeSelectionEvent event = new TemplateGroupViewerNodeSelectionEvent(this, cljGroup, cljTemplate, cljNode);
		for(TemplateGroupViewerNodeSelectionListener listener : nodeSelectionListeners) {
			listener.nodeSelected(event);
		}
	}
	
	public boolean addNodeSelectionListener(TemplateGroupViewerNodeSelectionListener listener) {
		return nodeSelectionListeners.add(listener);
	}
	
	public boolean removeNodeSelectionListener(TemplateGroupViewerNodeSelectionListener listener) {
		return nodeSelectionListeners.remove(listener);
	}
			
	public Object getSelectedSnippetNode() {
		IStructuredSelection selection = (IStructuredSelection) snippetTreeViewer.getSelection();
		cljNode = selection.getFirstElement();
		return cljNode;
	}

	public Object getSelectedSnippet() {
		cljTemplate =  TemplateGroup.FN_SNIPPETGROUP_SNIPPET_FOR_NODE.invoke(cljGroup, getSelectedSnippetNode());
		return cljTemplate;
	}

	private void updateTextFields() {
		Object selectedSnippet = getSelectedSnippet();
		if(selectedSnippet == null) {
			textViewerSnippet.getTextWidget().setText("");
			//textViewerNode.getTextWidget().setText("");
			return;
		}			
	
		Object selectedSnippetNode = getSelectedSnippetNode();
		TemplateGroup templateGroup = TemplateGroup.newFromClojureGroup(cljGroup);
		TemplatePrettyPrinter prettyprinter = new TemplatePrettyPrinter(templateGroup);
		prettyprinter.setHighlightNode(selectedSnippetNode);
		textViewerSnippet.getTextWidget().setText(prettyprinter.prettyPrintSnippet(selectedSnippet));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textViewerSnippet.getTextWidget().setStyleRange(range);
	
		/*
		prettyprinter =  new TemplatePrettyPrinter(templateGroup);
		textViewerNode.getTextWidget().setText(prettyprinter.prettyPrintElement(selectedSnippet, selectedSnippetNode));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textViewerNode.getTextWidget().setStyleRange(range);
		*/
		
	
	}
	
	public void clearSelection() {
		snippetTreeViewer.setSelection(null);
	}
	
	public void setInput(Object cljGroup, Object cljTemplate, Object cljNode) {
		this.cljGroup = cljGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
		snippetElementCol.setLabelProvider(new TemplateTreeLabelProviders.ElementColumnLabelProvider(cljGroup));		
		snippetNodeCol.setLabelProvider(new TemplateTreeLabelProviders.NodeColumnLabelProvider(cljGroup));		
		snippetPropCol.setLabelProvider(new TemplateTreeLabelProviders.PropertyColumnLabelProvider(cljGroup));
		snippetKindCol.setLabelProvider(new TemplateTreeLabelProviders.KindColumnLabelProvider(cljGroup));
		snippetDirectivesCol.setLabelProvider(new TemplateTreeLabelProviders.DirectivesColumnLabelProvider(cljGroup));
		snippetTreeViewer.setInput(cljGroup);
		
		if(cljNode != null) {
			//set selection to node
			snippetTreeViewer.setSelection(new StructuredSelection(cljNode), true);
		}
		else if (cljTemplate != null) {
			//set selection to template
			snippetTreeViewer.setSelection(new StructuredSelection(cljTemplate), true);
		}
		else  {
			//set selection to first template in template group
			Tree tree = snippetTreeViewer.getTree();
			TreeItem[] items = tree.getItems();
			if(items.length > 0)
				tree.setSelection(items[0]);
		}
		
		onNodeSelectionInternal();

	}
	
}

		
	