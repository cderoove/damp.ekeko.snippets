package damp.ekeko;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SnippetViewer extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.SnippetViewer"; //$NON-NLS-1$

	public SnippetViewer() {
		
	}

	private TreeViewer viewer;
	
	private String viewID;
	
	private SnippetTreeContentProvider contentProvider;
	

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		contentProvider = new SnippetTreeContentProvider();
		viewer.setContentProvider(getContentProvider());
/*
		TreeViewerColumn nodeCol = new TreeViewerColumn(viewer, SWT.NONE);
		nodeCol.getColumn().setText("Node");
		nodeCol.getColumn().setWidth(150);
		nodeCol.setLabelProvider(new SnippetTreeLabelProviders.NodeColumnLabelProvider(this));

		TreeViewerColumn kindCol = new TreeViewerColumn(viewer, SWT.NONE);
		kindCol.getColumn().setText("Kind");
		kindCol.getColumn().setWidth(150);
		kindCol.setLabelProvider(new SnippetTreeLabelProviders.KindColumnLabelProvider(this));

		TreeViewerColumn variableCol = new TreeViewerColumn(viewer, SWT.NONE);
		variableCol.getColumn().setText("Variable");
		variableCol.getColumn().setWidth(150);
		variableCol.setLabelProvider(new SnippetTreeLabelProviders.VariableColumnLabelProvider(this));

		TreeViewerColumn grounderCol = new TreeViewerColumn(viewer, SWT.RIGHT);
		grounderCol.getColumn().setText("Grounder");
		grounderCol.getColumn().setWidth(150);
		grounderCol.setLabelProvider(new SnippetTreeLabelProviders.GrounderColumnLabelProvider(this));

		TreeViewerColumn constrainerCol = new TreeViewerColumn(viewer, SWT.RIGHT);
		constrainerCol.getColumn().setText("Constrainer");
		constrainerCol.getColumn().setWidth(150);
		constrainerCol.setLabelProvider(new SnippetTreeLabelProviders.ConstrainerColumnLabelProvider(this));

		*/
		
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public TreeViewer getViewer() {
		return viewer;
	}
	
	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}

	public SnippetTreeContentProvider getContentProvider() {
		return contentProvider;
	}


}
