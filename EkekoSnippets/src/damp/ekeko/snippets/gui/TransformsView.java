package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;

import damp.ekeko.snippets.data.Groups;
import damp.ekeko.snippets.data.RewrittenSnippetGroup;
import damp.ekeko.snippets.data.SnippetGroup;
import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeContentProvider;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeLabelProviders;

import org.eclipse.swt.graphics.Color;

public class TransformsView extends SnippetView {

	public static final String ID = "damp.ekeko.snippets.gui.TransformsView"; //$NON-NLS-1$

	private String viewID;

	private Action actQuery;
	private Action actTrans;
	private StyledText textSnippet;
	private StyledText textRWSnippet;
	private TreeViewer treeViewerSnippet;
	private TreeViewer treeViewerRWSnippet;
	private Tree treeOperator;
	private Table tableRW;

	private Groups groups;
	private SnippetGroup snippetGroup;
	private RewrittenSnippetGroup rwSnippetGroup;
	private SnippetGroupTreeContentProvider contentProvider;

	public TransformsView() {
	}

	public void setRewrittenGroup(Groups groups, SnippetGroup group) {
		this.groups = groups;
		snippetGroup = group;
		rwSnippetGroup = groups.getRewrittenGroup(snippetGroup);
		init();
	}
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		
		Group group = new Group(container, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group group_1 = new Group(group, SWT.NONE);
		group_1.setLayout(new GridLayout(1, false));
		
		Label lblSnippet = new Label(group_1, SWT.NONE);
		GridData gd_lblSnippet = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSnippet.heightHint = 23;
		lblSnippet.setLayoutData(gd_lblSnippet);
		lblSnippet.setText("Before");
		
		TextViewer textViewerSnippet = new TextViewer(group_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerSnippet.setEditable(false);
		textSnippet = textViewerSnippet.getTextWidget();
		textSnippet.setEditable(false);
		GridData gd_textSnippet = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textSnippet.heightHint = 95;
		textSnippet.setLayoutData(gd_textSnippet);
		textSnippet.setSelectionBackground(new Color(Display.getCurrent(), 127, 255, 127));
		
		Group group_2 = new Group(group, SWT.NONE);
		group_2.setLayout(new GridLayout(1, false));
		
		Label lblSnippet2 = new Label(group_2, SWT.NONE);
		GridData gd_lblSnippet2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSnippet2.heightHint = 23;
		lblSnippet2.setLayoutData(gd_lblSnippet2);
		lblSnippet2.setText("");

		treeViewerSnippet = new TreeViewer(group_2, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeViewerSnippet.setAutoExpandLevel(1);
		Tree treeSnippet = treeViewerSnippet.getTree();
		treeSnippet.setHeaderVisible(true);
		treeSnippet.setLinesVisible(true);
		treeSnippet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn snippetNodeCol2 = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnNode2 = snippetNodeCol2.getColumn();
		trclmnNode2.setWidth(150);
		trclmnNode2.setText("Snippet");
		
		TreeViewerColumn snippetPropCol2 = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnProperty2 = snippetPropCol2.getColumn();
		trclmnProperty2.setWidth(150);
		trclmnProperty2.setText("Property");

		contentProvider = new SnippetGroupTreeContentProvider();
		treeViewerSnippet.setContentProvider(getContentProvider());
		snippetNodeCol2.setLabelProvider(new SnippetGroupTreeLabelProviders.NodeColumnLabelProvider(this));		
		snippetPropCol2.setLabelProvider(new SnippetGroupTreeLabelProviders.PropertyColumnLabelProvider(this));

		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onSnippetSelection();
			}
		});		
		
		Group group_11 = new Group(group, SWT.NONE);
		group_11.setLayout(new GridLayout(1, false));

		Label lblNewLabel = new Label(group_11, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.heightHint = 23;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("After");

		TextViewer textViewerSnippet1 = new TextViewer(group_11, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerSnippet1.setEditable(false);
		textRWSnippet = textViewerSnippet1.getTextWidget();
		textRWSnippet.setEditable(false);
		GridData gd_textSnippet1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textSnippet1.heightHint = 400;
		textRWSnippet.setLayoutData(gd_textSnippet1);
		textRWSnippet.setSelectionBackground(new Color(Display.getCurrent(), 127, 255, 127));
		
		Group group_21 = new Group(group, SWT.NONE);
		group_21.setLayout(new GridLayout(1, false));

		ToolBar toolBar = new ToolBar(group_21, SWT.FLAT | SWT.RIGHT);
		toolBar.setOrientation(SWT.RIGHT_TO_LEFT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmRemove = new ToolItem(toolBar, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeRewriteSnippet();
			}
		});
		tltmRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		tltmRemove.setToolTipText("Remove Rewritten Code");

		ToolItem tltmAdd = new ToolItem(toolBar, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addRewriteSnippet();
			}
		});
		tltmAdd.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
		tltmAdd.setToolTipText("Add Rewritten Code");
		
		treeViewerRWSnippet = new TreeViewer(group_21, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		treeViewerRWSnippet.setAutoExpandLevel(1);
		Tree treeTemplate = treeViewerRWSnippet.getTree();
		treeTemplate.setHeaderVisible(true);
		treeTemplate.setLinesVisible(true);
		treeTemplate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TreeViewerColumn snippetNodeCol = new TreeViewerColumn(treeViewerRWSnippet, SWT.NONE);
		TreeColumn trclmnNode = snippetNodeCol.getColumn();
		trclmnNode.setWidth(150);
		trclmnNode.setText("Snippet");
		
		TreeViewerColumn snippetPropCol = new TreeViewerColumn(treeViewerRWSnippet, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(150);
		trclmnProperty.setText("Property");

		contentProvider = new SnippetGroupTreeContentProvider();
		treeViewerRWSnippet.setContentProvider(getContentProvider());
		snippetNodeCol.setLabelProvider(new SnippetGroupTreeLabelProviders.NodeColumnLabelProvider(this));		
		snippetPropCol.setLabelProvider(new SnippetGroupTreeLabelProviders.PropertyColumnLabelProvider(this));

		treeTemplate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onRWSnippetSelection();
			}
		});		

		Group group_3 = new Group(group, SWT.NONE);
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
		GridData gd_group_4 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_4.heightHint = 300;
		group_4.setLayoutData(gd_group_4);
		
		ToolBar toolBar_1 = new ToolBar(group_4, SWT.FLAT | SWT.RIGHT);
		toolBar_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem toolItem = new ToolItem(toolBar_1, SWT.NONE);
		toolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeRewriteRule();
			}
		});
		toolItem.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		toolItem.setToolTipText("Remove Rule");
		
		Composite composite = new Composite(group_4, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.widthHint = 700;
		composite.setLayoutData(gd_composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableRW = tableViewer.getTable();
		tableRW.setHeaderVisible(true);
		tableRW.setLinesVisible(true);
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnOperation = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnOperation, new ColumnPixelData(130, true, true));
		tblclmnOperation.setText("Operation");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnOriginalCode = tableViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnOriginalCode, new ColumnPixelData(330, true, true));
		tblclmnOriginalCode.setText("Original Code");
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnRewrittenCode = tableViewerColumn_2.getColumn();
		tcl_composite.setColumnData(tblclmnRewrittenCode, new ColumnPixelData(330, true, true));
		tblclmnRewrittenCode.setText("Rewritten Code");

		contentProvider = new SnippetGroupTreeContentProvider();

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
			actQuery = new Action("Show Query") {
				public void run() {
					showQuery();
				}
			};
			actQuery.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.jdt.ui", "/icons/full/obj16/occ_write.gif"));
			actQuery.setToolTipText("Show Query");
		}
		{
			actTrans = new Action("Transform") {
				public void run() {
					transform();
				}
			};
			actTrans.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.pde.ui", "/icons/obj16/profile_exc.gif"));
			actTrans.setToolTipText("Transform");
		}
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(actQuery);
		toolbarManager.add(actTrans);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(actQuery);
		menuManager.add(actTrans);
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public Object getSelectedOperator() {
        return treeOperator.getSelection()[0].getData();
	}
	
	public Object getSelectedRWSnippet() {
		TreeItem[] selectedItems = treeViewerRWSnippet.getTree().getSelection();
		if (selectedItems.length > 0)
			return selectedItems[0].getData();
		return null;
	}
	
	public Object[] getSelectedRWSnippets() {
		TreeItem[] selectedItems = treeViewerRWSnippet.getTree().getSelection();
		Object[] nodes = new Object[selectedItems.length];
		for (int i=0; i < selectedItems.length; i++) {
			nodes[i] = selectedItems[i].getData(); 
		}
		return nodes;
	}

	public Object getSelectedSnippet() {
		TreeItem[] selectedItems = treeViewerSnippet.getTree().getSelection();
		if (selectedItems.length > 0)
			return selectedItems[0].getData();
		return null;
	}
	
	public Object getSelectedRule() {
		TableItem[] selectedItems = tableRW.getSelection();
		if (selectedItems.length > 0)
			return selectedItems[0].getData();
		return null;
	}
	
	public boolean arrayContain(Object[] array, Object data) {
		for (int i=0; i < array.length; i++) {
			if (array[i].equals(data)) 
				return true;
		}
		return false;
	}
	
	public TreeItem[] getTreeItem(Tree tree, Object[] data) {
		TreeItem root = tree.getItem(0);
		TreeItem[] selection = new TreeItem[data.length];
		int j = 0;
		for (int i=0; i < root.getItemCount(); i++) {
			if (arrayContain(data, root.getItem(i).getData())) {
				selection[j] = root.getItem(i);
				j++;
			}
		}
		return selection;
	}

	public SnippetGroupTreeContentProvider getContentProvider() {
		return contentProvider;
	}

	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}
	
	public void setCFStyle(StyledText sText, int start, int end) {
		StyleRange styleRange = new StyleRange();
	    styleRange.start = start;
	    styleRange.length = end - start + 1;
	    styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	    styleRange.fontStyle = SWT.BOLD;
	    sText.setStyleRange(styleRange);
	}

	public void clearCFStyle(StyledText sText) {
		StyleRange styleRange = new StyleRange();
	    styleRange.start = 0;
	    styleRange.length = textSnippet.getText().length();
	    styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	    styleRange.fontStyle = SWT.NORMAL;
	    sText.setStyleRange(styleRange);
	}

	//-----------------------------------------------
	//LOGIC PART
	//all logic part for this View are written below
	
	public void init() {
		textSnippet.setSelectionRange(0, 0);
		textSnippet.setText(snippetGroup.toString());
		treeViewerSnippet.setInput(snippetGroup.getGroup());
		markSnippet(textSnippet);
		renderSnippet();
	}
	
	public void markSnippet(StyledText sText) {
		clearCFStyle(sText);
		String text = sText.getText();
		int idx = 0, startIdx = 0, endIdx = 0;	
		
		while (idx < text.length() && startIdx > -1) {
			startIdx = text.indexOf('@', idx);
			if (startIdx > 0) {
				endIdx = text.indexOf(") ", startIdx);
				setCFStyle(sText, startIdx, endIdx);
				idx = endIdx;
			}
		}
	}
	
	public void renderSnippet() {
		treeViewerRWSnippet.setInput(rwSnippetGroup.getGroup());
		if (treeViewerRWSnippet.getTree().getSelectionCount() == 0) {
			TreeItem root = treeViewerRWSnippet.getTree().getItem(0);
			treeViewerRWSnippet.getTree().setSelection(root);
		}
		
		rwSnippetGroup.setTableRW(tableRW, snippetGroup);
		onRWSnippetSelection();
	}

	public void removeRewriteRule() {
		Object node = getSelectedRule();
		rwSnippetGroup.removeRule(node);
		renderSnippet();
	}

	public void removeRewriteSnippet() {
		Object[] nodes = getSelectedRWSnippets();
		for (int i=0; i < nodes.length; i++) {
			rwSnippetGroup.removeSnippet(nodes[i]);
		}
		renderSnippet();
	}

	public void addRewriteSnippet() {
		String code = getSelectedTextFromActiveEditor();
		if (code != null && !code.isEmpty()) {
			rwSnippetGroup.addSnippetCode(code);
			renderSnippet();
		}
	}

	public void onSnippetSelection() {
		textSnippet.setSelectionRange(0, 0);
		textSnippet.setText(snippetGroup.toString(getSelectedSnippet()));
		markSnippet(textSnippet);
		int x = snippetGroup.getActiveNodePos()[0];
		int y = snippetGroup.getActiveNodePos()[1];
		if (x < 0) {x = 0; y = 0;}
		textSnippet.setSelectionRange(x, y-x);
	} 
	
	public void onRWSnippetSelection() {
		SnippetOperator.setInputForTransformation(treeOperator, getSelectedRWSnippet());
		textRWSnippet.setSelectionRange(0, 0);
		
		Object data[] = getSelectedRWSnippets();
		String code = "";
		for (int i=0; i<data.length; i++)
			code += rwSnippetGroup.toString(data[i]) + "\n";
		
		textRWSnippet.setText(code);
		markSnippet(textRWSnippet);
		int x = rwSnippetGroup.getActiveNodePos()[0];
		int y = rwSnippetGroup.getActiveNodePos()[1];
		if (x < 0) {x = 0; y = 0;}
		textRWSnippet.setSelectionRange(x, y-x);
	} 
	
	public void onOperatorSelection() {
		applyOperator(getSelectedSnippet(), getSelectedRWSnippet(), getSelectedOperator());
	} 
	
	public void applyOperator(Object selectedNode, Object selectedRWNode, Object selectedOperator) {
		String[] args = SnippetOperator.getArguments(selectedOperator);

		if (selectedRWNode != null) {
			String	nodeInfo = "Rewritten Node\n " + SnippetGroup.getTypeValue(selectedRWNode) + 
					"\n" + selectedRWNode.toString().replace(", :",  "\n:") ;

			if (SnippetOperator.isTransformOperator(selectedOperator)) {
				nodeInfo = "Original Node \n" + SnippetGroup.getTypeValue(selectedNode) + 
						"\n" + selectedNode.toString().replace(", :",  "\n:") + "\n \n" + nodeInfo;
			}
			
			SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
					"Apply Operator", "Apply Operator to " + nodeInfo, 
					"\nApply the Operator?", args, null);
			dlg.create();
			
			if (dlg.open() == Window.OK) {
				rwSnippetGroup.applyOperator(selectedOperator, snippetGroup, selectedNode, selectedRWNode, dlg.getInputs());
				renderSnippet();
			}
		}
	
	}
	
	public void showQuery() {
		SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
				"Apply Transformation", rwSnippetGroup.getTransformationQuery(snippetGroup), 
				"", null, null);
		dlg.create();

		if (dlg.open() == Window.OK) {
			rwSnippetGroup.doTransformation(snippetGroup);
			 boolean b = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
					 "Info", "Transformation of this group is saved. To process the actual transformation, please activate button Transform in main menu.");
		}
	}

	public void transform() {
		boolean b = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
				 "Transformation", "Apply Transformation?");
		
		if (b) {
			rwSnippetGroup.doTransformation(snippetGroup);
			 boolean m = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
					 "Info", "Transformation of this group is saved. To process the actual transformation, please activate button Transform in main menu.");
		}
	}
}