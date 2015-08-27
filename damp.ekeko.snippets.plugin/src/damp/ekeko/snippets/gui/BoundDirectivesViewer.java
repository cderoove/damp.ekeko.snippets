package damp.ekeko.snippets.gui;

import java.util.Collection;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import clojure.lang.IFn;
import damp.ekeko.snippets.BoundDirective;
import damp.ekeko.snippets.DirectiveOperandBinding;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class BoundDirectivesViewer extends Composite {


	private TableViewer directivesTableViewer;
	//private Label directiveLabel;
	private TableViewer operandsTableViewer;

	private TemplateGroup templateGroup;
	private Object cljSelectedSnippet;
	private Object cljSelectedSnippetNode;

	public static IFn FN_BOUNDDIRECTIVES_FOR_NODE;
	public static IFn FN_GROUP_REMOVE_BOUNDDIRECTIVE_FROM_NODE;
	public static IFn FN_GROUP_ADD_DIRECTIVE_TO_NODE;
	

	private ToolItem tltmRemove;
	private ToolItem tltmAdd;
	private TextViewer textViewerNode;
	private TableViewerColumn operandValueCol;
		
	public static Collection getBoundDirectivesForElementOfTemplate(Object node, Object template) {
		return (Collection) FN_BOUNDDIRECTIVES_FOR_NODE.invoke(template, node);
	}
	
	public BoundDirectivesViewer(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);
	
		
		//toolbar
		
		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT | SWT.NO_FOCUS);
		toolBar.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		tltmAdd = new ToolItem(toolBar, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addBoundDirective();
			}
		});
		tltmAdd.setImage(EkekoSnippetsPlugin.IMG_ADD);
		tltmAdd.setToolTipText("Add directive");
				
		
		tltmRemove = new ToolItem(toolBar, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeBoundDirective();
			}

		});
		tltmRemove.setImage(EkekoSnippetsPlugin.IMG_DELETE);
		tltmRemove.setToolTipText("Remove selected directive");
		tltmRemove.setEnabled(false);

		
		//text with node
		textViewerNode = new TextViewer(this, SWT.NONE | SWT.WRAP | SWT.READ_ONLY | SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL);
		StyledText textViewerNodeText = textViewerNode.getTextWidget();
		GridData gd_styledTextNode = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_styledTextNode.heightHint = 100;
		gd_styledTextNode.widthHint = 500;
		textViewerNodeText.setLayoutData(gd_styledTextNode);
		textViewerNodeText.setFont(EkekoSnippetsPlugin.getEditorFont());
		textViewerNodeText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		textViewerNodeText.setCaret(null);

		
		
	
		//table of directives
		
		directivesTableViewer = new TableViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table directivesTable = directivesTableViewer.getTable();
		//directivesTable.setLinesVisible(true);
		directivesTable.setHeaderVisible(true);

		GridData gd_directivesTable = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_directivesTable.heightHint = 100;
		directivesTable.setLayoutData(gd_directivesTable);		
		directivesTableViewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn directiveCol = new TableViewerColumn(directivesTableViewer, SWT.NONE);
		directiveCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof BoundDirective) {
					BoundDirective boundDirective = (BoundDirective) element;
					return boundDirective.toString();
				}
				return "";
			}
		});
		TableColumn directiveColCol = directiveCol.getColumn();
		directiveColCol.setWidth(200);
		directiveColCol.setText("Directive");
		
		TableViewerColumn directiveDescriptionCol = new TableViewerColumn(directivesTableViewer, SWT.NONE);
		directiveDescriptionCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if(element instanceof BoundDirective) {
					BoundDirective boundDirective = (BoundDirective) element;
					return boundDirective.getDescription();
				}
				return "";
			}
		});
		TableColumn directiveDescriptionColCol = directiveDescriptionCol.getColumn();
		directiveDescriptionColCol.setWidth(200);
		directiveDescriptionColCol.setText("Description");		
		
		directivesTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				updateWidgets();
			}
		});		
		
				
		//explanation of current directive
		
		//directiveLabel = new Label(this, SWT.NONE | SWT.WRAP);
		//gd_tableOpArgs.heightHint = 31;
		//directiveLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		//table with arguments for current directive

		operandsTableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		Table operandsTable = operandsTableViewer.getTable();
		operandsTable.setLinesVisible(true);
		operandsTable.setHeaderVisible(true);
		GridData gd_tableOpArgs = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableOpArgs.heightHint = 100;
		operandsTable.setLayoutData(gd_tableOpArgs);
		operandsTableViewer.setContentProvider(new ArrayContentProvider());
		
		TableViewerColumn operandDescriptionCol = new TableViewerColumn(operandsTableViewer, SWT.NONE);
		TableColumn operandDescriptionColCol = operandDescriptionCol.getColumn();
		operandDescriptionColCol.setWidth(200);
		operandDescriptionColCol.setText("Operand");
		operandDescriptionCol.setLabelProvider(new OperandBindingLabelProviderDescription());
		
		
		operandValueCol = new TableViewerColumn(operandsTableViewer, SWT.NONE);
		TableColumn operandValueColCol = operandValueCol.getColumn();
		operandValueColCol.setWidth(200);
		operandValueColCol.setText("Value");
		
				

	}

		
	protected void addBoundDirective() {
		DirectiveSelectionDialog dialog = new DirectiveSelectionDialog(getShell());
		int open = dialog.open();
		if(open == DirectiveSelectionDialog.CANCEL)
			return;
		Object selectedDirective = dialog.getSelectedDirective();
		if(selectedDirective != null) {
			cljSelectedSnippet = templateGroup.addDirective(cljSelectedSnippet, cljSelectedSnippetNode, selectedDirective);
		}
		updateWidgets();
	}



	protected void removeBoundDirective() {
		BoundDirective boundDirective = getSelectedBoundDirective();
		if(boundDirective == null)
			return;
		cljSelectedSnippet = templateGroup.removeBoundDirective(cljSelectedSnippet, cljSelectedSnippetNode, boundDirective);
		updateWidgets();
	}






	public void setInput(TemplateGroup group, Object selectedSnippet, Object selectedSnippetNode) {
		this.templateGroup = group;
		this.cljSelectedSnippet = selectedSnippet;
		this.cljSelectedSnippetNode = selectedSnippetNode;
		
		operandValueCol.setLabelProvider(new DirectiveOperandBindingLabelProviderValue(templateGroup, cljSelectedSnippet, cljSelectedSnippetNode));
		operandValueCol.setEditingSupport(new DirectiveOperandBindingEditingSupport(this, operandsTableViewer, templateGroup, cljSelectedSnippet, cljSelectedSnippetNode));
		
		updateWidgets();
		
	}
	
	public BoundDirective getSelectedBoundDirective() {
        IStructuredSelection selection = (IStructuredSelection) directivesTableViewer.getSelection();
        return (BoundDirective) selection.getFirstElement();
	}


	public void updateWidgets() {	
		if(cljSelectedSnippetNode == null || cljSelectedSnippet == null) {
			directivesTableViewer.setInput(null);
			operandsTableViewer.setInput(null);
			//directiveLabel.setText("");
			tltmRemove.setEnabled(false);			
			tltmAdd.setEnabled(false);
			return;	
		}
		
		tltmAdd.setEnabled(true);

		TemplatePrettyPrinter prettyprinter = new TemplatePrettyPrinter(templateGroup);
		textViewerNode.getTextWidget().setText(prettyprinter.prettyPrintElement(cljSelectedSnippet, cljSelectedSnippetNode));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textViewerNode.getTextWidget().setStyleRange(range);
		
		Collection boundDirectives = getBoundDirectivesForElementOfTemplate(cljSelectedSnippetNode, cljSelectedSnippet);
		BoundDirective oldSelection = getSelectedBoundDirective();
		directivesTableViewer.setInput(boundDirectives);
		if(oldSelection != null)
			directivesTableViewer.setSelection(new StructuredSelection(oldSelection));
		BoundDirective newSelection = getSelectedBoundDirective();
		if(newSelection == null) {
			tltmRemove.setEnabled(false);
			operandsTableViewer.setInput(null);
			//directiveLabel.setText("");
			return;	
		}
		tltmRemove.setEnabled(true);

		//directiveLabel.setText(newSelection.getDescription());
		Collection<DirectiveOperandBinding> operandBindings = newSelection.getOperandBindings();
		operandValueCol.setEditingSupport(new DirectiveOperandBindingEditingSupport(this, operandsTableViewer, templateGroup.getGroup(), cljSelectedSnippet, cljSelectedSnippetNode));
		operandValueCol.setLabelProvider(new DirectiveOperandBindingLabelProviderValue(templateGroup.getGroup(), cljSelectedSnippet, cljSelectedSnippetNode));
		operandsTableViewer.setInput(operandBindings.toArray());


	}
	

}
