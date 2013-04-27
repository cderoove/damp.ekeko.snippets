package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;

import damp.ekeko.snippets.data.Groups;
import damp.ekeko.snippets.data.SnippetGroup;
import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeContentProvider;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeLabelProviders;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;

import clojure.lang.Keyword;

public class SnippetView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.SnippetView"; //$NON-NLS-1$
	private String viewID;

	private Action actAddSnippet;
	private Action actUndo;
	private StyledText textSnippet;
	private StyledText textCondition;
	private TreeViewer treeViewerSnippet;
	private Tree treeOperator;
	private StyledText textOpInfo;
	private Table tableOpArgs;
	private TableDecorator tableOpArgsDecorator;
	private Table tableNode;
	
	private Groups groups;
	private SnippetGroup snippetGroup;
	private SnippetGroupTreeContentProvider contentProvider;
	private Action actRedo;
	private Table table_1;
	private Action actTrans;

	public SnippetView() {
	}
	
	public void setGroup(Groups groups, SnippetGroup group) {
		this.groups = groups;
		snippetGroup = group;
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
		
		Group group_1 = new Group(container, SWT.NONE);
		group_1.setLayout(new GridLayout(2, false));
		
		Label lblSnippet = new Label(group_1, SWT.NONE);
		GridData gd_lblSnippet = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSnippet.heightHint = 23;
		lblSnippet.setLayoutData(gd_lblSnippet);
		lblSnippet.setText("Snippet");

		ToolBar toolBar_1 = new ToolBar(group_1, SWT.FLAT | SWT.RIGHT);
		toolBar_1.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmRunQuery = new ToolItem(toolBar_1, SWT.NONE);
		tltmRunQuery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runQuery();
			}
		});
		tltmRunQuery.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/obj16/profile_exc.gif"));
		tltmRunQuery.setToolTipText("Run Query");
		
		ToolItem tltmViewquery = new ToolItem(toolBar_1, SWT.NONE);
		tltmViewquery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewQuery();
			}
		});
		tltmViewquery.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/eview16/new_persp.gif"));
		tltmViewquery.setToolTipText("View Query");
		
		ToolItem tltmCheckresult = new ToolItem(toolBar_1, SWT.NONE);
		tltmCheckresult.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkResult();
			}
		});
		tltmCheckresult.setToolTipText("Check Query Result");
		tltmCheckresult.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/obj16/tsk_alert_obj.gif"));
		
		TextViewer textViewerSnippet = new TextViewer(group_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerSnippet.setEditable(false);
		textSnippet = textViewerSnippet.getTextWidget();
		textSnippet.setEditable(false);
		GridData gd_textSnippet = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_textSnippet.heightHint = 95;
		textSnippet.setLayoutData(gd_textSnippet);
		textSnippet.setSelectionBackground(new Color(Display.getCurrent(), 127, 255, 127));
		
		ToolBar toolBar_2 = new ToolBar(group_1, SWT.FLAT | SWT.RIGHT);
		toolBar_2.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		ToolItem tltmCondition = new ToolItem(toolBar_2, SWT.NONE);
		tltmCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addLogicCondition();
			}
		});
		tltmCondition.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/editor_area.gif"));
		tltmCondition.setToolTipText("Add Logic Condition");
		
		TextViewer textViewerCondition = new TextViewer(group_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerCondition.setEditable(false);
		textCondition = textViewerCondition.getTextWidget();
		textCondition.setEditable(false);
		textCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		Group group_2 = new Group(container, SWT.NONE);
		group_2.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(group_2, SWT.FLAT | SWT.RIGHT);
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
		
		ToolItem tltmView = new ToolItem(toolBar, SWT.NONE);
		tltmView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewSnippet();
			}
		});
		tltmView.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/keygroups_obj.gif"));
		tltmView.setToolTipText("View Snippet");
		/*		
		ToolItem tltmFlag = new ToolItem(toolBar, SWT.NONE);
		tltmFlag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				flagSnippet();
			}
		});
		tltmFlag.setImage(ResourceManager.getPluginImage("EkekoSnippets", "icons/mandatory.gif"));
		tltmFlag.setToolTipText("Update Snippet Status");
		*/
		treeViewerSnippet = new TreeViewer(group_2, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeViewerSnippet.setAutoExpandLevel(1);
		Tree treeSnippet = treeViewerSnippet.getTree();
		treeSnippet.setHeaderVisible(true);
		treeSnippet.setLinesVisible(true);
		treeSnippet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn snippetNodeCol = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnNode = snippetNodeCol.getColumn();
		trclmnNode.setWidth(150);
		trclmnNode.setText("Snippet");
		/*
		TreeViewerColumn snippetNodeFlag = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnFlag = snippetNodeFlag.getColumn();
		trclmnFlag.setWidth(20);
		trclmnFlag.setText("");
		*/
		TreeViewerColumn snippetPropCol = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(150);
		trclmnProperty.setText("Property");

		TreeViewerColumn snippetVarCol = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnLogicVariable = snippetVarCol.getColumn();
		trclmnLogicVariable.setWidth(150);
		trclmnLogicVariable.setText("Logic Variable");
		
		contentProvider = new SnippetGroupTreeContentProvider();
		treeViewerSnippet.setContentProvider(getContentProvider());
		snippetNodeCol.setLabelProvider(new SnippetGroupTreeLabelProviders.NodeColumnLabelProvider(this));		
		//snippetNodeFlag.setLabelProvider(new SnippetGroupTreeLabelProviders.FlagColumnLabelProvider(this));		
		snippetPropCol.setLabelProvider(new SnippetGroupTreeLabelProviders.PropertyColumnLabelProvider(this));
		snippetVarCol.setLabelProvider(new SnippetGroupTreeLabelProviders.VariableColumnLabelProvider(this));

		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onSnippetSelection();
			}
		});		
		
		treeSnippet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				viewSnippet();
			}
		});

		Group group_3 = new Group(container, SWT.NONE);
		group_3.setLayout(new GridLayout(1, false));
		
		Label lblOperator = new Label(group_3, SWT.NONE);
		GridData gd_lblOperator = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblOperator.heightHint = 22;
		lblOperator.setLayoutData(gd_lblOperator);
		lblOperator.setText("Operator");
		
		treeOperator = new Tree(group_3, SWT.BORDER  | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeOperator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeColumn trclmnOperator = new TreeColumn(treeOperator, SWT.NONE);
		trclmnOperator.setWidth(300);
		trclmnOperator.setText("Operator");
		
		treeOperator.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onOperatorSelection();
			}
		});		

		Group group_4 = new Group(container, SWT.NONE);
		group_4.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar_3 = new ToolBar(group_4, SWT.FLAT | SWT.RIGHT);
		toolBar_3.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		ToolItem tltmApplyOperator = new ToolItem(toolBar_3, SWT.NONE);
		tltmApplyOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onApplyOperator();
			}
		});
		tltmApplyOperator.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/etool16/validate.gif"));
		tltmApplyOperator.setToolTipText("Apply Operator");
		
		TableViewer tableViewerOpArgs = new TableViewer(group_4, SWT.BORDER | SWT.FULL_SELECTION);
		tableOpArgs = tableViewerOpArgs.getTable();
		tableOpArgs.setLinesVisible(true);
		tableOpArgs.setHeaderVisible(true);
		GridData gd_tableOpArgs = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableOpArgs.heightHint = 31;
		tableOpArgs.setLayoutData(gd_tableOpArgs);
		tableOpArgsDecorator = new TableDecorator(tableOpArgs);
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewerOpArgs, SWT.NONE);
		TableColumn tblclmnType = tableViewerColumn.getColumn();
		tblclmnType.setWidth(150);
		tblclmnType.setText("Argument");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewerOpArgs, SWT.NONE);
		TableColumn tblclmnNode = tableViewerColumn_1.getColumn();
		tblclmnNode.setWidth(150);
		tblclmnNode.setText("Input");

		TableViewer tableViewer = new TableViewer(group_4, SWT.BORDER | SWT.FULL_SELECTION);
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

		TextViewer textViewer = new TextViewer(group_4, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textOpInfo = textViewer.getTextWidget();
		textOpInfo.setEditable(false);
		GridData gd_textOpInfo = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textOpInfo.heightHint = 41;
		textOpInfo.setLayoutData(gd_textOpInfo);
		
	    createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
		{
			actAddSnippet = new Action("Add Snippet") {				public void run() {
					addSnippet();
				}
			};
			actAddSnippet.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
			actAddSnippet.setToolTipText("Add Snippet");
		}
		{
			actUndo = new Action("Undo Operator") {				public void run() {
					undo();
				}
			};
			actUndo.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/etool16/undo_edit.gif"));
			actUndo.setToolTipText("Undo");
		}
		{
			actRedo = new Action("Redo Operator") {				public void run() {
					redo();
				}
			};
			actRedo.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/etool16/redo_edit.gif"));
			actRedo.setToolTipText("Redo");
		}
		{
			actTrans = new Action("Program Transformation") {				public void run() {
					transformation();
				}
			};
			actTrans.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.egit.ui", "/icons/elcl16/filterresource.gif"));
			actTrans.setToolTipText("Program Transformation");
		}
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(actAddSnippet);
		toolbarManager.add(actUndo);
		toolbarManager.add(actRedo);
		toolbarManager.add(actTrans);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(actAddSnippet);
		menuManager.add(actUndo);
		menuManager.add(actRedo);
		menuManager.add(actTrans);
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
        return treeOperator.getSelection()[0].getData();
	}
	
	public Object getSelectedSnippet() {
		return treeViewerSnippet.getTree().getSelection()[0].getData();
	}
	
	public Object[] getSelectedSnippets() {
		TreeItem[] selectedItems = treeViewerSnippet.getTree().getSelection();
		Object[] nodes = new Object[selectedItems.length];
		for (int i=0; i < selectedItems.length; i++) {
			nodes[i] = selectedItems[i].getData(); 
		}
		return nodes;
	}
	
	public String[] getInputs(Table table, int column) {
		TableItem[] items = table.getItems();
		String[] result = new String[items.length];

		for (int i = 0; i < items.length; i++) {
			result[i] = items[i].getText(column);
		}		
		return result;
	}
	
	public Object getSelectedNode() {
		if (tableNode.getSelectionCount() > 0)
			return tableNode.getSelection()[0].getData();
        return null;
	}
	
	public SnippetGroupTreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}
	
	public void setCFStyle(int start, int end) {
		StyleRange styleRange = new StyleRange();
	    styleRange.start = start;
	    styleRange.length = end - start + 1;
	    styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	    styleRange.fontStyle = SWT.BOLD;
	    textSnippet.setStyleRange(styleRange);
	}

	public void clearCFStyle() {
		StyleRange styleRange = new StyleRange();
	    styleRange.start = 0;
	    styleRange.length = textSnippet.getText().length();
	    styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	    styleRange.fontStyle = SWT.NORMAL;
	    textSnippet.setStyleRange(styleRange);
	}

	//-----------------------------------------------
	//LOGIC PART
	//all logic part for this View are written below
	
	public void markSnippet() {
		clearCFStyle();
		String text = textSnippet.getText();
		int idx = 0, startIdx = 0, endIdx = 0;	
		
		while (idx < text.length() && startIdx > -1) {
			startIdx = text.indexOf('@', idx);
			if (startIdx > 0) {
				endIdx = text.indexOf(' ', startIdx);
				setCFStyle(startIdx, endIdx);
				idx = endIdx;
			}
		}
	}
	
	public void renderSnippet() {
		treeViewerSnippet.setInput(snippetGroup.getGroup());
		if (treeViewerSnippet.getTree().getSelectionCount() == 0) {
			TreeItem root = treeViewerSnippet.getTree().getItem(0);
			treeViewerSnippet.getTree().setSelection(root);
		}
		onSnippetSelection();
	}
	
	public void addSnippet() {
		String code = getSelectedTextFromActiveEditor();
		if (code != null && !code.isEmpty()) {
			snippetGroup.addSnippetCode(code);
			renderSnippet();
		}
	}
	
	public void viewSnippet() {
		snippetGroup.viewSnippet(getSelectedSnippet());
	}

	public void removeSnippet() {
		Object[] nodes = getSelectedSnippets();
		for (int i=0; i < nodes.length; i++) {
			snippetGroup.removeSnippet(nodes[i]);
		}
		renderSnippet();
	}

	public void flagSnippet() {
		snippetGroup.updateSnippetFlag(getSelectedSnippet());
		renderSnippet();
	}

	public void viewQuery() {
		String query = snippetGroup.getQuery(getSelectedSnippet());
		SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
				"Query", query, "\nExecute the Query?", null, null);
		dlg.create();
		if (dlg.open() == Window.OK) 
			runQuery();
	}

	public void runQuery() {
		snippetGroup.runQuery(getSelectedSnippet());
	}
	
	public void checkResult() {
		try {
			ResultCheckView view = (ResultCheckView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.ResultCheckView");
			view.setResult(snippetGroup.getQueryResult(getSelectedSnippet()));
			view.setGroup(snippetGroup);
			view.setSnippet(snippetGroup.getSnippet(getSelectedSnippet()));
			view.putData();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public void addLogicCondition() {
		String[] inputs = {textCondition.getText()};
		inputs = applyOperator(snippetGroup.getRoot(getSelectedSnippet()), Keyword.intern("update-logic-conditions"), inputs);
		textCondition.setText(inputs[0]);
	}

	public void onSnippetSelection() {
		textSnippet.setSelectionRange(0, 0);
		textSnippet.setText(snippetGroup.toString(getSelectedSnippet()));
		textCondition.setText(snippetGroup.getLogicConditions(getSelectedSnippet()));
		markSnippet();

		int x = snippetGroup.getActiveNodePos()[0];
		int y = snippetGroup.getActiveNodePos()[1];
		if (x < 0) {x = 0; y = 0;}
		textSnippet.setSelectionRange(x, y-x);
		SnippetOperator.setInput(treeOperator, getSelectedSnippet());

		tableOpArgs.removeAll();
		tableOpArgsDecorator.removeAllEditors();
		tableNode.removeAll();
	} 
	
	public void onOperatorSelection() {
		tableOpArgsDecorator.removeAllEditors();
		SnippetOperator.setInputArguments(tableOpArgs, tableNode, snippetGroup.getGroup(), getSelectedOperator());
		tableOpArgsDecorator.setTextEditor(1);
		tableOpArgsDecorator.setButtonEditor(1);
		textOpInfo.setText(SnippetOperator.getDescription(getSelectedOperator()));
	} 
	
	public void onApplyOperator() {
		Object[] nodes = getSelectedSnippets();
		if (nodes.length > 1) 
			applyOperatorToNodes(nodes, getSelectedOperator(), getInputs(tableOpArgs, 1));
		else
			applyOperator(getSelectedSnippet(), getSelectedOperator(), getInputs(tableOpArgs, 1));
	}
	
	public String[] applyOperator(Object selectedNode, Object selectedOperator, String[] inputs) {
		String[] args = SnippetOperator.getArguments(selectedOperator);
		String nodeInfo = "Group";
		if (selectedNode != null)
			nodeInfo = "Node " + SnippetGroup.getTypeValue(selectedNode) + 
				"\n" + selectedNode.toString().replace(", :",  "\n:") ;

		SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
				"Apply Operator", "Apply Operator to " + nodeInfo, 
				"\nApply the Operator?", args, inputs);
		dlg.create();
		
		if (dlg.open() == Window.OK) {
			snippetGroup.applyOperator(selectedOperator, selectedNode, dlg.getInputs(), getSelectedNode());
			renderSnippet();
		}

		return dlg.getInputs();
	}
	
	public String[] applyOperatorToNodes(Object[] selectedNodes, Object selectedOperator, String[] inputs) {
		String[] args = SnippetOperator.getArguments(selectedOperator);

		SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
				"Apply Operator", "Apply Operator to selected nodes", 
				"\nApply the Operator?", args, inputs);
		dlg.create();
		
		if (dlg.open() == Window.OK) {
			snippetGroup.applyOperatorToNodes(selectedOperator, selectedNodes, dlg.getInputs(), getSelectedNode());
			renderSnippet();
		}

		return dlg.getInputs();
	}

	public void undo() {
		snippetGroup.undoOperator();
		renderSnippet();
	}

	public void redo() {
		snippetGroup.redoOperator();
		renderSnippet();
	}

	public void transformation() {
		try {
			ProgramTransView view = (ProgramTransView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.ProgramTransView");
			view.setRewrittenGroup(groups, snippetGroup);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
