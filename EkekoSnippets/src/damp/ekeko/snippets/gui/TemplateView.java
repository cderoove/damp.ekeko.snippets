package damp.ekeko.snippets.gui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wb.swt.ResourceManager;

import damp.ekeko.snippets.data.Groups;
import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.data.TemplateGroup;
import damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter;

public class TemplateView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.TemplateView"; //$NON-NLS-1$
	private String viewID;

	private List<Action> actions; 
	private StyledText textSnippet;
	private StyledText textCondition;
	private TreeViewer snippetTreeViewer;
	private Tree operatorTree;
	private TreeViewer operatorTreeViewer;
	private StyledText textOpInfo;
	private Table operandsTable;
	private Table tableNode;
	
	private Groups groups;

	private TemplateGroup templateGroup;
	
	private TemplateTreeContentProvider contentProvider;
	private TableViewer operandsTableViewer;

	public TemplateView() {
		templateGroup = new TemplateGroup("Template Group");
	}
	
	public void setGroup(Groups groups, TemplateGroup group) {
		this.groups = groups;
		templateGroup = group;
		renderSnippet();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group snippetTextGroup = new Group(container, SWT.NONE);
		snippetTextGroup.setLayout(new GridLayout(2, false));
		
		/*
		Label lblSnippet = new Label(snippetTextGroup, SWT.NONE);
		GridData gd_lblSnippet = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSnippet.heightHint = 23;
		lblSnippet.setLayoutData(gd_lblSnippet);
		lblSnippet.setText("");
		*/
		
		ToolBar snippetTextToolBar = new ToolBar(snippetTextGroup, SWT.FLAT | SWT.RIGHT);
		snippetTextToolBar.setOrientation(SWT.RIGHT_TO_LEFT);
		snippetTextToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		
		ToolItem tltmCondition = new ToolItem(snippetTextToolBar, SWT.NONE);
		tltmCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addLogicCondition();
			}
		});
		tltmCondition.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/editor_area.gif"));
		tltmCondition.setToolTipText("Add condition");
		
		
		
		TextViewer textViewerSnippet = new TextViewer(snippetTextGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerSnippet.setEditable(false);
		textSnippet = textViewerSnippet.getTextWidget();
		textSnippet.setEditable(false);
		GridData gd_textSnippet = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_textSnippet.heightHint = 95;
		textSnippet.setLayoutData(gd_textSnippet);
		//textSnippet.setSelectionBackground(new Color(Display.getCurrent(), 127, 255, 127));
		
		
		/*
		 * ToolBar toolBar_2 = new ToolBar(snippetTextGroup, SWT.FLAT | SWT.RIGHT);
		 *
		toolBar_2.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		*/
		
		
		TextViewer textViewerCondition = new TextViewer(snippetTextGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerCondition.setEditable(false);
		textCondition = textViewerCondition.getTextWidget();
		textCondition.setEditable(false);
		GridData gd_textCondition = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);		gd_textCondition.heightHint = 100;		textCondition.setLayoutData(gd_textCondition);
		
		Group snippetTreeGroup = new Group(container, SWT.NONE);
		snippetTreeGroup.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(snippetTreeGroup, SWT.FLAT | SWT.RIGHT);
		toolBar.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmRemove = new ToolItem(toolBar, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSnippet();
			}
		});
		tltmRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		tltmRemove.setToolTipText("Remove Snippet");
		
		ToolItem tltmAdd = new ToolItem(toolBar, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSnippet();
			}
		});
		tltmAdd.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
		tltmAdd.setToolTipText("Add Snippet");
				
		snippetTreeViewer = new TreeViewer(snippetTreeGroup, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
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
		
		/*
		TreeViewerColumn snippetDirectivesCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn snippetDirectivesColCol = snippetDirectivesCol.getColumn();
		snippetDirectivesColCol.setWidth(75);
		snippetDirectivesColCol.setText("Directives");
		*/
		
		TreeViewerColumn snippetPropCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(250);
		trclmnProperty.setText("Value of property in parent");
		
		
		contentProvider = new TemplateTreeContentProvider();
		snippetTreeViewer.setContentProvider(getContentProvider());
		snippetNodeCol.setLabelProvider(new TemplateTreeLabelProviders.NodeColumnLabelProvider(getSnippetGroup()));		
		snippetPropCol.setLabelProvider(new TemplateTreeLabelProviders.PropertyColumnLabelProvider(getSnippetGroup()));
		snippetKindCol.setLabelProvider(new TemplateTreeLabelProviders.KindColumnLabelProvider(getSnippetGroup()));
		//snippetDirectivesCol.setLabelProvider(new TemplateTreeLabelProviders.DirectivesColumnLabelProvider(this));

		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onNodeSelection();
			}
		});		
		
		
		Group snippetOperatorGroup = new Group(container, SWT.NONE);
		snippetOperatorGroup.setLayout(new GridLayout(1, false));
		
		ToolBar snippetOperatorGroupToolbar = new ToolBar(snippetOperatorGroup, SWT.FLAT | SWT.RIGHT);
		snippetOperatorGroupToolbar.setOrientation(SWT.RIGHT_TO_LEFT);
		snippetOperatorGroupToolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		
		
		/*
		Label lblOperator = new Label(snippetOperatorGroup, SWT.NONE);
		GridData gd_lblOperator = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblOperator.heightHint = 22;
		lblOperator.setLayoutData(gd_lblOperator);
		lblOperator.setText("");
		
		*/
		
		
		ToolItem tltmApplyOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		tltmApplyOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onApplyOperator();
			}
		});
		tltmApplyOperator.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/etool16/validate.gif"));
		tltmApplyOperator.setToolTipText("Apply Operator");
		
		/*
		ToolItem undoOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		undoOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				undo();
			}
		});
		undoOperator.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/undo_edit.gif"));
		undoOperator.setToolTipText("Undo operator application");
		
		
		ToolItem redoOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		redoOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				redo();
			}
		});
		redoOperator.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/redo_edit.gif"));
		redoOperator.setToolTipText("Redo operator application");
		*/

		
		
		operatorTreeViewer = new TreeViewer(snippetOperatorGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		operatorTreeViewer.setAutoExpandLevel(3);
		operatorTree = operatorTreeViewer.getTree();
		operatorTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn operatorNameColumn = new TreeViewerColumn(operatorTreeViewer, SWT.NONE);
		TreeColumn trclmnOperator = operatorNameColumn.getColumn();
		trclmnOperator.setWidth(150);
		trclmnOperator.setText("Operator");
		
		operatorNameColumn.setLabelProvider(new OperatorTreeLabelProvider());
		operatorTreeViewer.setContentProvider(new OperatorTreeContentProvider());
		
			
		operatorTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onOperatorSelection();
			}
		});		

		TextViewer textViewer = new TextViewer(snippetOperatorGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textOpInfo = textViewer.getTextWidget();
		textOpInfo.setEditable(false);
		GridData gd_textOpInfo = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_textOpInfo.heightHint = 100;
		textOpInfo.setLayoutData(gd_textOpInfo);

		
		
		
		Group snippetOperatorOperandsGroup = new Group(container, SWT.NONE);
		snippetOperatorOperandsGroup.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar_3 = new ToolBar(snippetOperatorOperandsGroup, SWT.FLAT | SWT.RIGHT);
		toolBar_3.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		
		operandsTableViewer = new TableViewer(snippetOperatorOperandsGroup, SWT.BORDER | SWT.FULL_SELECTION);
		operandsTable = operandsTableViewer.getTable();
		operandsTable.setLinesVisible(true);
		operandsTable.setHeaderVisible(true);
		GridData gd_tableOpArgs = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableOpArgs.heightHint = 31;
		operandsTable.setLayoutData(gd_tableOpArgs);
		
		operandsTableViewer.setContentProvider(new ArrayContentProvider());
		
		//operandsTableDecorator = new OperandsTableDecorator(operandsTable);
		
		TableViewerColumn operandDescriptionCol = new TableViewerColumn(operandsTableViewer, SWT.NONE);
		TableColumn operandDescriptionColCol = operandDescriptionCol.getColumn();
		operandDescriptionColCol.setWidth(150);
		operandDescriptionColCol.setText("Operand");
		operandDescriptionCol.setLabelProvider(new OperandBindingLabelProviderDescription());
		
		
		
		TableViewerColumn operandValueCol = new TableViewerColumn(operandsTableViewer, SWT.NONE);
		TableColumn operandValueColCol = operandValueCol.getColumn();
		operandValueColCol.setWidth(150);
		operandValueColCol.setText("Value");
		operandValueCol.setLabelProvider(new OperandBindingLabelProviderValue());

		operandValueCol.setEditingSupport(new OperandBindingEditingSupport(operandsTableViewer));
		



		/*
		TableViewer tableViewer = new TableViewer(snippetOperatorOperandsGroup, SWT.BORDER | SWT.FULL_SELECTION);
		tableNode = tableViewer.getTable();
		tableNode.setHeaderVisible(true);
		tableNode.setLinesVisible(true);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table.heightHint = 31;
		tableNode.setLayoutData(gd_table);
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnArgument = tableViewerColumn_2.getColumn();
		tblclmnArgument.setWidth(150);
		tblclmnArgument.setText("Node");
		
		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnProp = tableViewerColumn_3.getColumn();
		tblclmnProp.setWidth(150);
		tblclmnProp.setText("Parent");
		*/

		
		
		
		
	    createActions();
		initializeToolBar();
		initializeMenu();
	}


	private void createActions() {
		// Create the actions
		
		actions = new LinkedList<Action>();
				
		Action runQuery = new Action("Match template") {
			public void run() {
				runQuery();
			}
		};
		runQuery.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.pde.ui", "/icons/obj16/profile_exc.gif"));
		runQuery.setToolTipText("Match template");
		actions.add(runQuery);
		
		Action inspectQuery = new Action("Inspect corresponding query") {
			public void run() {
				viewQuery();
			}
		};
		inspectQuery.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/eview16/new_persp.gif"));
		inspectQuery.setToolTipText("Inspect corresponding query");
		actions.add(inspectQuery);
		
		Action inspectMatches = new Action("Inspect matches") {
			public void run() {
				checkResult();
			}
		};
		inspectMatches.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.pde.ui", "/icons/obj16/tsk_alert_obj.gif"));
		inspectMatches.setToolTipText("Inspect matches");
		actions.add(inspectMatches);

		/*
		//TODO: move to top-level
		Action actTrans = new Action("Program Transformation") {			public void run() {
				transformation();
			}
		};
		actTrans.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.egit.ui", "/icons/elcl16/filterresource.gif"));
		actTrans.setToolTipText("Program Transformation");
		*/
	
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		for(Action action : actions) {
			toolbarManager.add(action);
		}
	}

	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		for(Action action : actions) {
			menuManager.add(action);
		}
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	String getSelectedTextFromActiveEditor() {
		ITextEditor editor =  (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();	
		return selection.getText();
	}
		 
	public Object getSelectedOperator() {
         IStructuredSelection selection = (IStructuredSelection) operatorTreeViewer.getSelection();
         return selection.getFirstElement();
	}
	

	//returns a Snippet instance
	public Object getSelectedSnippet() {
		Object selectedSnippetNode = getSelectedSnippetNode();
		return templateGroup.getSnippet(selectedSnippetNode);
	}
	
	//returns an AST node or wrapper within a Snippet
	public Object getSelectedSnippetNode() {
		IStructuredSelection selection = (IStructuredSelection) snippetTreeViewer.getSelection();
		return selection.getFirstElement();
	}
	
	
	public Object[] getSelectedSnippets() {
		TreeItem[] selectedItems = snippetTreeViewer.getTree().getSelection();
		Object[] nodes = new Object[selectedItems.length];
		for (int i=0; i < selectedItems.length; i++) {
			nodes[i] = selectedItems[i].getData(); 
		}
		return nodes;
	}
		
	public Object getSelectedNode() {
		if (tableNode.getSelectionCount() > 0)
			return tableNode.getSelection()[0].getData();
        return null;
	}
	
	public TemplateTreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}
	

	public Object getSnippetGroup() {
		return templateGroup.getGroup();
	}
	
	private void updateGroupTree() {
		snippetTreeViewer.setInput(getSnippetGroup());
	}
	
	public void renderSnippet() {
		updateGroupTree();
		//TODO: dit moet blijkbaar wel gecalled worden
		/*
		if (treeViewerSnippet.getTree().getSelectionCount() == 0) {
			TreeItem root = treeViewerSnippet.getTree().getItem(0);
			treeViewerSnippet.getTree().setSelection(root);
		}
		*/
		updateTextFields();
}
	
	public void addSnippet() {
		String code = getSelectedTextFromActiveEditor();
		if (code != null && !code.isEmpty()) {
			//throws NPE when selected text cannot be parsed as the starting point for a template
			templateGroup.addSnippetCode(code);
			renderSnippet();
		}
	}
	
	public void viewSnippet() {
		//removed plain snippet viewer
		//snippetGroup.viewSnippet(getSelectedSnippet());
	}

	public void removeSnippet() {
		if(!MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Delete templates", "Are you sure you want to delete the selected templates?")) 
			return;
		for(Object selected : getSelectedSnippets()) 
			templateGroup.removeSnippet(selected);
		renderSnippet();
	}

	public void viewQuery() {
		String query = templateGroup.getQuery(getSelectedSnippet());
		OperatorApplicationDialog dlg = new OperatorApplicationDialog(Display.getCurrent().getActiveShell(),
				"Query", query, "\nExecute the Query?", null, null);
		dlg.create();
		if (dlg.open() == Window.OK) 
			runQuery();
	}

	public void runQuery() {
		templateGroup.runQuery(getSelectedSnippet());
	}
	
	class QueryResultThread extends Thread {
		Object selectedSnippet;
		
		public QueryResultThread (Object selectedSnippet) {
			this.selectedSnippet = selectedSnippet;
		}
		
        public void run() {
        	//result check view only for group
			final Object[] result = templateGroup.getQueryResult("Group");
			
    		Display.getDefault().syncExec(new Runnable() {    			
    		    public void run() {
    				try {
    					ResultCheckView view = (ResultCheckView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.ResultCheckView");
    					view.setResult(result);
    					view.setGroup(templateGroup);
    					//view.setSnippet(snippetGroup.getSnippet(selectedSnippet));
    					view.setSnippet(null);
    					view.putData();
    				} catch (PartInitException e) {
    					e.printStackTrace();
    				}
    		    }
    		});
        }
    }	
		
	public void checkResult() {
		QueryResultThread qsThread = new QueryResultThread(getSelectedSnippet());
		qsThread.start();
	}

	public void addLogicCondition() {
		Object[] inputs = {textCondition.getText()};
		//applyOperator(templateGroup.getRoot(getSelectedSnippet()), Keyword.intern("update-logic-conditions"), inputs);
		updateTextFields();
	}

	private void updateTextFields() {
		Object selectedSnippet = getSelectedSnippet();
		if(selectedSnippet == null) {
			textCondition.setText("");
			textSnippet.setText("");
			return;
		}			
		//shows text for and condition associated with currently selected snippet
		textCondition.setText(templateGroup.getLogicConditions(selectedSnippet));
		Object selectedSnippetNode = getSelectedSnippetNode();
		SnippetPrettyPrinter prettyprinter = new SnippetPrettyPrinter();
		prettyprinter.setHighlightNode(selectedSnippetNode);
		textSnippet.setText(prettyprinter.prettyPrint(selectedSnippet));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textSnippet.setStyleRange(range);
	}
	
	
	private void onNodeSelection() {
		updateTextFields();
		updateOperatorTreeView();
		updateOperandsTable();
		
		
	} 
	
	private void updateOperandsTable() {
		Object snippetGroup = getSnippetGroup();
		Object selectedSnippet = getSelectedSnippet();
		Object selectedSnippetNode = getSelectedSnippetNode();
		Object selectedOperator = getSelectedOperator();
		if(selectedOperator == null || !SnippetOperator.isOperator(selectedOperator)) {
			textOpInfo.setText(""); 
			operandsTableViewer.setInput(null);
			return;	
		}
		
		textOpInfo.setText(SnippetOperator.getDescription(selectedOperator));
		operandsTableViewer.setInput(SnippetOperator.getOperands(snippetGroup, selectedSnippet, selectedSnippetNode, selectedOperator));

	}

	private void updateOperatorTreeView() {
		
		Object selectedSnippetNode = getSelectedSnippetNode();
		operatorTreeViewer.setInput(selectedSnippetNode);
		operatorTreeViewer.setSelection(StructuredSelection.EMPTY);
		
		
		
		
		/*
		
		//:generalization 
		Object[] types = getArray(FN_OPERATOR_CATEGORIES.invoke());
		for (int i = 0; i < types.length; i++) {
			TreeItem itemType = new TreeItem(root, 0);
			//"Generalization"
			itemType.setText((String) FN_OPERATORCATEGORY_DESCRIPTION.invoke(types[i]));
			itemType.setData(types[i]);
			
			Object[] operators = getArray(FN_APPLICABLE_OPERATORS_IN_CATEGORY.invoke(types[i], selectedNode));
			for (int j = 0; j < operators.length; j++) {
				TreeItem itemOp = new TreeItem(itemType, 0);
				itemOp.setText((String) FN_OPERATOR_NAME.invoke(operators[j]));
				itemOp.setData(operators[j]);
			}			
			itemType.setExpanded(true);
		}
		root.setExpanded(true);
		*/

		
		//tableOpArgs.removeAll();
		//tableOpArgsDecorator.removeAllEditors();
		//tableNode.removeAll();

		
		
	}

	private void onOperatorSelection() {
		
		
		updateOperandsTable();

		/*
		tableOpArgsDecorator.removeAllEditors();
		SnippetOperator.setInputArguments(tableOpArgs, tableNode, snippetGroupHistory.getGroup(), getSelectedOperator());
		tableOpArgsDecorator.setTextEditor(1);
		tableOpArgsDecorator.setButtonEditor(1);
		
		*/
	} 
	
	private void onApplyOperator() {
		Object operands = operandsTableViewer.getInput();
		if(operands == null)
			return;
		applyOperator(getSelectedOperator(), operandsTableViewer.getInput());
		
	}
	
	private void applyOperator(Object selectedOperator, Object operands) {
		templateGroup.applyOperator(selectedOperator, operands);
		refreshWidgets();
	}
	

	private void refreshWidgets() {
		updateTextFields();
		updateGroupTree();
		updateOperatorTreeView();
		updateOperandsTable();
	}

	/*
	public void undo() {
		snippetGroupHistory.undoOperator();
		renderSnippet();
	}

	public void redo() {
		snippetGroupHistory.redoOperator();
		renderSnippet();
	}
	*/
	
	public void transformation() {
		try {
			TransformsView view = (TransformsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.TransformsView");
			view.setRewrittenGroup(groups, templateGroup);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
