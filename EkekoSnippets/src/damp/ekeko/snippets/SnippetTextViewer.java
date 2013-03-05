package damp.ekeko.snippets;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SnippetTextViewer extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.SnippetTextViewer"; //$NON-NLS-1$

	public SnippetTextViewer() {
		
	}

	private String viewID;
	private StyledText viewer;
	private SnippetPrettyPrinter printer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new StyledText(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.setText("Snippet");
		
		printer = new SnippetPrettyPrinter();
		
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

	public StyledText getViewer() {
		return viewer;
	}
	
	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}

	public void setInput(Object snippet, ASTNode node) {
		printer.setSnippet(snippet);
		node.accept(printer);
		viewer.setText(printer.getResult());
	}
}
