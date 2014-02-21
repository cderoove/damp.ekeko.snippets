package damp.ekeko.snippets.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import damp.ekeko.snippets.data.TemplateGroup;
import damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter;


public class GroupNodeSelectionDialog extends Dialog
{

	private Object group;
	
	private Object template;
	
	private Object node;

	private TextViewer textViewerSnippet;

	private TreeViewer snippetTreeViewer;
	
	public GroupNodeSelectionDialog(Shell parentShell, Object group, Object template, Object node) {
		super(parentShell);
		this.group = group;
		this.template = template;
		this.node = node;
	}
	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select a node from the template.");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		 Composite composite = (Composite) super.createDialogArea(parent);
		 composite.setLayout(new GridLayout(1, false));
		 
		 textViewerSnippet = new TextViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		 textViewerSnippet.setEditable(false);
		 GridData gd_textSnippet = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		 gd_textSnippet.heightHint = 95;
		 textViewerSnippet.getControl().setLayoutData(gd_textSnippet);
		 	 
		 snippetTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		 snippetTreeViewer.setAutoExpandLevel(2);
		 Tree treeSnippet = snippetTreeViewer.getTree();
		 treeSnippet.setHeaderVisible(true);
		 treeSnippet.setLinesVisible(true);
		 treeSnippet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
		 TreeViewerColumn snippetNodeCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		 TreeColumn trclmnNode = snippetNodeCol.getColumn();
		 trclmnNode.setWidth(150);
		 trclmnNode.setText("Node");
			
		 TreeViewerColumn snippetKindCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		 TreeColumn snippetKindColCol = snippetKindCol.getColumn();
		 snippetKindColCol.setWidth(150);
		 snippetKindColCol.setText("Node kind");

		 TreeViewerColumn snippetPropCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		 TreeColumn trclmnProperty = snippetPropCol.getColumn();
		 trclmnProperty.setWidth(250);
		 trclmnProperty.setText("Value of property in parent");

		 snippetTreeViewer.setContentProvider(new TemplateTreeContentProvider());
		 snippetNodeCol.setLabelProvider(new TemplateTreeLabelProviders.NodeColumnLabelProvider(group));		
		 snippetPropCol.setLabelProvider(new TemplateTreeLabelProviders.PropertyColumnLabelProvider(group));
		 snippetKindCol.setLabelProvider(new TemplateTreeLabelProviders.KindColumnLabelProvider(group));
		 
		 treeSnippet.addListener(SWT.Selection, new Listener() {
			 public void handleEvent(Event e) {
				 onNodeSelection();
			 }
		 });		
		 
		 snippetTreeViewer.setInput(group);
		 snippetTreeViewer.setSelection(new StructuredSelection(node));
		 updateTextFields();
		 return composite;
	}


	protected void onNodeSelection() {
		node = getSelectedSnippetNode();
		updateTextFields();
	}
	
	public Object getSelectedSnippetNode() {
		IStructuredSelection selection = (IStructuredSelection) snippetTreeViewer.getSelection();
		return selection.getFirstElement();
	}

	public Object getSelectedSnippet() {
		node = getSelectedSnippetNode();
		//TODO: create a Group class where these functions reside
		return TemplateGroup.FN_SNIPPETGROUP_SNIPPET_FOR_NODE.invoke(group, node);		
	}

	private void updateTextFields() {
		Object selectedSnippet = getSelectedSnippet();
		if(selectedSnippet == null) {
			textViewerSnippet.getTextWidget().setText("");
			return;
		}			
		node = getSelectedSnippetNode();
		SnippetPrettyPrinter prettyprinter = new SnippetPrettyPrinter();
		prettyprinter.setHighlightNode(node);
		textViewerSnippet.getTextWidget().setText(prettyprinter.prettyPrint(selectedSnippet));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textViewerSnippet.getTextWidget().setStyleRange(range);
	}
	
	
	

}
