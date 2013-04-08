package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Tree;
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

import damp.ekeko.snippets.data.SnippetGroup;
import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeContentProvider;
import damp.ekeko.snippets.gui.viewer.SnippetGroupTreeLabelProviders;

import org.eclipse.swt.graphics.Color;

public class ProgramTransView extends SnippetView {

	public static final String ID = "damp.ekeko.snippets.gui.ProgramTransView"; //$NON-NLS-1$

	private String viewID;

	private Action actUndo;
	private Action actRedo;
	private StyledText textSnippet;
	private StyledText textAfter;
	private TreeViewer treeViewerSnippet;
	private Tree treeOperator;
	
	private SnippetGroup snippetGroup;
	private SnippetGroupTreeContentProvider contentProvider;
	private Action actTrans;

	public ProgramTransView() {
	}

	public void setSnippet(SnippetGroup grp) {
		snippetGroup = grp;
		init();
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
		
		Group group_11 = new Group(container, SWT.NONE);
		group_11.setLayout(new GridLayout(1, false));

		Label lblNewLabel = new Label(group_11, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.heightHint = 23;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("After");

		TextViewer textViewerSnippet1 = new TextViewer(group_11, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewerSnippet1.setEditable(false);
		textAfter = textViewerSnippet1.getTextWidget();
		textAfter.setEditable(false);
		GridData gd_textSnippet1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textSnippet1.heightHint = 421;
		textAfter.setLayoutData(gd_textSnippet1);
		textAfter.setSelectionBackground(new Color(Display.getCurrent(), 127, 255, 127));
		
		Group group_2 = new Group(container, SWT.NONE);
		group_2.setLayout(new GridLayout(1, false));
		
		Label lblSnippet2 = new Label(group_2, SWT.NONE);
		GridData gd_lblSnippet2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSnippet2.heightHint = 23;
		lblSnippet2.setLayoutData(gd_lblSnippet2);
		lblSnippet2.setText("Snippet");

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
		
		TreeViewerColumn snippetPropCol = new TreeViewerColumn(treeViewerSnippet, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(150);
		trclmnProperty.setText("Property");

		contentProvider = new SnippetGroupTreeContentProvider();
		treeViewerSnippet.setContentProvider(getContentProvider());
		snippetNodeCol.setLabelProvider(new SnippetGroupTreeLabelProviders.NodeColumnLabelProvider(this));		
		snippetPropCol.setLabelProvider(new SnippetGroupTreeLabelProviders.PropertyColumnLabelProvider(this));

		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        onSnippetSelection();
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
			actUndo = new Action("Undo Operator") {
				public void run() {
					undo();
				}
			};
			actUndo.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/etool16/undo_edit.gif"));
			actUndo.setToolTipText("Undo");
		}
		{
			actRedo = new Action("Redo Operator") {
				public void run() {
					redo();
				}
			};
			actRedo.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/etool16/redo_edit.gif"));
			actRedo.setToolTipText("Redo");
		}
		{
			actTrans = new Action("Transform") {
				public void run() {
					transform();
				}
			};
			actTrans.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.jdt.ui", "/icons/full/obj16/occ_write.gif"));
			actTrans.setToolTipText("Transform");
		}
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
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
		menuManager.add(actUndo);
		menuManager.add(actRedo);
		menuManager.add(actTrans);
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public Object getSelectedOperator() {
        return treeOperator.getSelection()[0].getData();
	}
	
	public Object getSelectedSnippet() {
		return treeViewerSnippet.getTree().getSelection()[0].getData();
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
		textSnippet.setText(snippetGroup.toString());
		textAfter.setSelectionRange(0, 0);
		textAfter.setText(snippetGroup.toString());
		treeViewerSnippet.setInput(snippetGroup.getGroup());
		markSnippet(textSnippet);
		markSnippet(textAfter);
	}
	
	public void markSnippet(StyledText sText) {
		clearCFStyle(sText);
		String text = sText.getText();
		int idx = 0, startIdx = 0, endIdx = 0;	
		
		while (idx < text.length() && startIdx > -1) {
			startIdx = text.indexOf('@', idx);
			if (startIdx > 0) {
				endIdx = text.indexOf(')', startIdx);
				setCFStyle(sText, startIdx, endIdx);
				idx = endIdx;
			}
		}
	}
	
	public void onSnippetSelection() {
		System.out.println(getSelectedSnippet());
		SnippetOperator.setInputForTransformation(treeOperator, getSelectedSnippet());
		textAfter.setSelectionRange(0, 0);
		textAfter.setText(snippetGroup.toString(getSelectedSnippet()));
		markSnippet(textAfter);
		int x = snippetGroup.getActiveNodePos()[0];
		int y = snippetGroup.getActiveNodePos()[1];
		if (x < 0) {x = 0; y = 0;}
		textAfter.setSelectionRange(x, y-x);
	} 
	
	public void onOperatorSelection() {
		applyOperator(getSelectedSnippet(), getSelectedOperator(), null);
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
			snippetGroup.applyOperator(selectedOperator, selectedNode, dlg.getInputs(), null);
			textAfter.setText(snippetGroup.toString());
			treeViewerSnippet.setInput(snippetGroup.getGroup());
			markSnippet(textAfter);
		}

		return dlg.getInputs();
	}
	
	public void undo() {
		snippetGroup.undoOperator();
		textAfter.setText(snippetGroup.toString());
		treeViewerSnippet.setInput(snippetGroup.getGroup());
		markSnippet(textAfter);
	}

	public void redo() {
		snippetGroup.redoOperator();
		textAfter.setText(snippetGroup.toString());
		treeViewerSnippet.setInput(snippetGroup.getGroup());
		markSnippet(textAfter);
	}
	
	public void transform() {
		SInputDialog dlg = new SInputDialog(Display.getCurrent().getActiveShell(),
				"Apply Transformation", snippetGroup.getTransformationQuery(), 
				"", null, null);
		dlg.create();

		if (dlg.open() == Window.OK) {
			snippetGroup.doTransformation();
			 boolean b = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
					 "Info", "Process of program transformation is done.");
		}
	}
}
