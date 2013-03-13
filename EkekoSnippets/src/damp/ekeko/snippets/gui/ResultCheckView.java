package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.widgets.Button;


public class ResultCheckView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.ResultCheckView"; //$NON-NLS-1$
	private Tree treeResult;
	private Tree treeOperator;

	public ResultCheckView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group group = new Group(container, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		
		Group group_1 = new Group(group, SWT.NONE);
		group_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_group_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_1.widthHint = 355;
		group_1.setLayoutData(gd_group_1);
		
		treeResult = new Tree(group_1, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		treeResult.setLinesVisible(true);
		treeResult.setHeaderVisible(true);
		
		TreeColumn tblclmnResult = new TreeColumn(treeResult, SWT.NONE);
		tblclmnResult.setWidth(600);
		tblclmnResult.setText("Result");

	    treeResult.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event e) {
	    		onResultSelection();
	    	}
	    });

		Group group_2 = new Group(group, SWT.NONE);
		GridData gd_group_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_2.widthHint = 38;
		group_2.setLayoutData(gd_group_2);
		group_2.setLayout(new GridLayout(1, false));
		
		treeOperator = new Tree(group_2, SWT.BORDER | SWT.FULL_SELECTION);
		treeOperator.setHeaderVisible(true);
		GridData gd_treeOperator = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_treeOperator.heightHint = 213;
		treeOperator.setLayoutData(gd_treeOperator);
		
		TreeColumn tblclmnOperator = new TreeColumn(treeOperator, SWT.NONE);
		tblclmnOperator.setWidth(250);
		tblclmnOperator.setText("Operator Suggestion");
		
		Group group_3 = new Group(group_2, SWT.NONE);
		group_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		group_3.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		
		Button btnApply = new Button(group_3, SWT.NONE);
		btnApply.setText("Apply");

		createActions();
		initializeToolBar();
		initializeMenu();
		putData();
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
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	public Object getSelectedResult() {
		return treeResult.getSelection()[0].getData();
	}
	
	// Logic Code
	//------------------------------
	
	public void putData() {
	    for (int i = 0; i < 10; i++) {
			TreeItem item = new TreeItem(treeResult, 0);
			item.setText("Node"+i);
			item.setData("Node"+i);

			TreeItem child = new TreeItem(item, 0);
			child.setText("Detail Information");

			TreeItem itemx = new TreeItem(treeOperator, 0);
			itemx.setText("Operator"+i);
			itemx.setData("Operator"+i);
	    }	
	}

	public void onResultSelection() {
	}
	
}
