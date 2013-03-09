package damp.ekeko.snippets;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SnippetGroupViewer extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.SnippetGroupViewer"; //$NON-NLS-1$

	public SnippetGroupViewer() {
		
	}

	private TreeViewer viewer;
	
	private String viewID;
	
	private SnippetGroupTreeContentProvider contentProvider;
	

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		contentProvider = new SnippetGroupTreeContentProvider();
		viewer.setContentProvider(getContentProvider());

		
		TreeViewerColumn nodeCol = new TreeViewerColumn(viewer, SWT.NONE);
		nodeCol.getColumn().setText("Value");
		nodeCol.getColumn().setWidth(150);
		//nodeCol.setLabelProvider(new SnippetGroupTreeLabelProviders.NodeColumnLabelProvider(this));
		
		TreeViewerColumn variableCol = new TreeViewerColumn(viewer, SWT.NONE);
		variableCol.getColumn().setText("Variable");
		variableCol.getColumn().setWidth(150);
		//variableCol.setLabelProvider(new SnippetGroupTreeLabelProviders.VariableColumnLabelProvider(this));

		
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

	public SnippetGroupTreeContentProvider getContentProvider() {
		return contentProvider;
	}


}
