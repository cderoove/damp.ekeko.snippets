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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wb.swt.ResourceManager;

import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.TemplateView"; //$NON-NLS-1$
	private String viewID;

	private List<Action> actions; 
	private Tree operatorTree;
	private TreeViewer operatorTreeViewer;
	private StyledText textOpInfo;
	private Table operandsTable;
	private Table tableNode;
	
	private TemplateGroup templateGroup;
	private TemplateGroupViewer templateGroupViewer;
	
	private TemplateTreeContentProvider contentProvider;
	private TableViewer operandsTableViewer;

	public TemplateView() {
		templateGroup = TemplateGroup.newFromGroupName("Template Group");
	}
	
	public void setGroup(TemplateGroup group) {
		templateGroup = group;
		refreshWidgets();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
				
			
		Group snippetTreeGroup = new Group(container, SWT.NONE);
		snippetTreeGroup.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(snippetTreeGroup, SWT.FLAT | SWT.RIGHT);
		toolBar.setOrientation(SWT.RIGHT_TO_LEFT);		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		ToolItem tltmAdd = new ToolItem(toolBar, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSnippet();
			}
		});
		tltmAdd.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
		tltmAdd.setToolTipText("Add Snippet");
				
		
		ToolItem tltmRemove = new ToolItem(toolBar, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSnippet();
			}
		});
		tltmRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		tltmRemove.setToolTipText("Remove Snippet");
		
		
		templateGroupViewer = new TemplateGroupViewer(snippetTreeGroup, SWT.NONE);		GridData gd_templateGroupViewer = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);		gd_templateGroupViewer.heightHint = 0;		templateGroupViewer.setLayoutData(gd_templateGroupViewer);
		templateGroupViewer.addNodeSelectionListener(new TemplateGroupViewerNodeSelectionListener() {
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				//event.getSelectedTemplateGroup();
				//event.getSelectedTemplate();
				//event.getSelectedTemplateNode();
				updateOperatorTreeView();
				updateOperandsTable();
			}
		});

		
		
		templateGroupViewer.setInput(templateGroup.getGroup(), null, null);
		
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
				
	public void addSnippet() {
		String code = getSelectedTextFromActiveEditor();
		if (code != null && !code.isEmpty()) {
			//throws NPE when selected text cannot be parsed as the starting point for a template
			templateGroup.addSnippetCode(code);
			refreshWidgets();
		}
	}
	
	public void viewSnippet() {
		//removed plain snippet viewer
		//snippetGroup.viewSnippet(getSelectedSnippet());
	}

	public void removeSnippet() {
		Object selected = templateGroupViewer.getSelectedSnippet();
		if(selected == null)
			return;
		if(!MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Delete template", "Are you sure you want to delete the selected template?")) 
			return;
		templateGroup.removeSnippet(selected);
		refreshWidgets();
	}

	public void viewQuery() {
		String query = templateGroup.getQuery( templateGroupViewer.getSelectedSnippet());
		OperatorApplicationDialog dlg = new OperatorApplicationDialog(Display.getCurrent().getActiveShell(),
				"Query", query, "\nExecute the Query?", null, null);
		dlg.create();
		if (dlg.open() == Window.OK) 
			runQuery();
	}

	public void runQuery() {
		templateGroup.runQuery(templateGroupViewer.getSelectedSnippet());
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
		QueryResultThread qsThread = new QueryResultThread(templateGroupViewer.getSelectedSnippet());
		qsThread.start();
	}

	
	
	private void onNodeSelection() {
		updateOperatorTreeView();
		updateOperandsTable();
		
		
	} 
	
	private void updateOperandsTable() {
		Object snippetGroup =  templateGroup.getGroup();
		Object selectedSnippet = templateGroupViewer.getSelectedSnippet();
		Object selectedSnippetNode = templateGroupViewer.getSelectedSnippetNode();
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
		
		Object selectedSnippetNode = templateGroupViewer.getSelectedSnippetNode();
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
		templateGroupViewer.setInput(templateGroup.getGroup(), templateGroupViewer.getSelectedSnippet(), templateGroupViewer.getSelectedSnippetNode());
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
		/*
		try {
			TransformsView view = (TransformsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.TransformsView");
			view.setRewrittenGroup(groups, templateGroup);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		*/
	}
}
