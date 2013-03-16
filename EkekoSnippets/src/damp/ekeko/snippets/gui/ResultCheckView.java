package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import clojure.lang.RT;
import org.eclipse.swt.widgets.Label;


public class ResultCheckView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.ResultCheckView"; //$NON-NLS-1$
	private Tree treeResult;
	private Tree treeConfirmResult;
	private Tree treeOperator;
	private Object[] result;

	public ResultCheckView() {
	}

	public void setResult(Object[] result) {
		this.result = result;
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
		group_1.setLayout(new GridLayout(1, false));
		GridData gd_group_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_1.widthHint = 355;
		group_1.setLayoutData(gd_group_1);
		
		Label lblNewLabel = new Label(group_1, SWT.NONE);
		lblNewLabel.setText("RESULT");
				
		treeResult = new Tree(group_1, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_treeResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_treeResult.heightHint = 44;
		treeResult.setLayoutData(gd_treeResult);
		treeResult.setLinesVisible(true);
		treeResult.setHeaderVisible(true);
		
		TreeColumn tblclmnResult = new TreeColumn(treeResult, SWT.NONE);
		tblclmnResult.setWidth(0);

		TreeColumn tblclmnResult2 = new TreeColumn(treeResult, SWT.NONE);
		tblclmnResult2.setWidth(25);
		
		ToolBar toolBar = new ToolBar(group_1, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmAddAsPositive = new ToolItem(toolBar, SWT.NONE);
		tltmAddAsPositive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPositiveResult();
			}
		});
		tltmAddAsPositive.setImage(ResourceManager.getPluginImage("EkekoSnippets", "icons/positive.gif"));
		tltmAddAsPositive.setText("Add as Positive Example");
		
		ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmAddAsNegative = new ToolItem(toolBar, SWT.NONE);
		tltmAddAsNegative.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNegativeResult();
			}
		});
		tltmAddAsNegative.setImage(ResourceManager.getPluginImage("EkekoSnippets", "icons/negative.gif"));
		tltmAddAsNegative.setText("Add as Negative Example");

		Label label = new Label(group_1, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		Label lblNewLabel_2 = new Label(group_1, SWT.NONE);

		Label lblNewLabel_1 = new Label(group_1, SWT.NONE);
		lblNewLabel_1.setText("EXPECTED RESULT");

		treeConfirmResult = new Tree(group_1, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_treeConfirmResult = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_treeConfirmResult.heightHint = 167;
		treeConfirmResult.setLayoutData(gd_treeConfirmResult);
		treeConfirmResult.setLinesVisible(true);
		treeConfirmResult.setHeaderVisible(true);
		
		TreeColumn tblclmnCResult = new TreeColumn(treeConfirmResult, SWT.NONE);
		tblclmnCResult.setWidth(0);
		
		TreeColumn tblclmnCResult2 = new TreeColumn(treeConfirmResult, SWT.NONE);
		tblclmnCResult2.setWidth(25);

		ToolBar toolBar_1 = new ToolBar(group_1, SWT.FLAT | SWT.RIGHT);
		toolBar_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		ToolItem tltmAddAsPositive_1 = new ToolItem(toolBar_1, SWT.NONE);
		tltmAddAsPositive_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPositiveConfirmResult();
			}
		});
		tltmAddAsPositive_1.setImage(ResourceManager.getPluginImage("EkekoSnippets", "icons/positive.gif"));
		tltmAddAsPositive_1.setText("Add as Positive Example");
		
		ToolItem toolItem_1 = new ToolItem(toolBar_1, SWT.SEPARATOR);
		
		ToolItem tltmAddAsNegative_1 = new ToolItem(toolBar_1, SWT.NONE);
		tltmAddAsNegative_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNegativeConfirmResult();
			}
		});
		tltmAddAsNegative_1.setImage(ResourceManager.getPluginImage("EkekoSnippets", "icons/negative.gif"));
		tltmAddAsNegative_1.setText("Add as Negative Example");
		
		ToolItem toolItem_2 = new ToolItem(toolBar_1, SWT.SEPARATOR);
		
		ToolItem tltmRemove = new ToolItem(toolBar_1, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeConfirmResult();
			}
		});
		tltmRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		tltmRemove.setText("Remove");
		
		Group group_2 = new Group(group, SWT.NONE);
		GridData gd_group_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_2.widthHint = 38;
		group_2.setLayoutData(gd_group_2);
		group_2.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel_3 = new Label(group_2, SWT.NONE);
		lblNewLabel_3.setText("Operator Suggestion");

		treeOperator = new Tree(group_2, SWT.BORDER | SWT.FULL_SELECTION);
		treeOperator.setHeaderVisible(true);
		GridData gd_treeOperator = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_treeOperator.heightHint = 213;
		treeOperator.setLayoutData(gd_treeOperator);
		
		TreeColumn tblclmnOperator = new TreeColumn(treeOperator, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		tblclmnOperator.setWidth(250);
		tblclmnOperator.setText("Operator");
		
		Group group_3 = new Group(group_2, SWT.NONE);
		group_3.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		GridData gd_group_3 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_group_3.heightHint = 0;
		gd_group_3.widthHint = 0;
		group_3.setLayoutData(gd_group_3);
		
		Button btnApply = new Button(group_3, SWT.NONE);
		btnApply.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnApply.setText("Apply");

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
	
	public void createColumn(Object[] arrCol) {
		for (int i = 0; i < arrCol.length; i++) {
			TreeColumn tblclmn = new TreeColumn(treeResult, SWT.NONE);
			tblclmn.setWidth(250);
			tblclmn.setText(arrCol[i].toString());

			TreeColumn tblclmnr = new TreeColumn(treeConfirmResult, SWT.NONE);
			tblclmnr.setWidth(250);
			tblclmn.setText(arrCol[i].toString());
		}
	}

	public TreeItem[] getSelectedResults() {
		return treeResult.getSelection();
	}
	
	public TreeItem[] getSelectedConfirmResults() {
		return treeConfirmResult.getSelection();
	}

	// Logic Code
	//------------------------------
	
	Image positiveIcon = ResourceManager.getPluginImage("EkekoSnippets", "icons/positive.gif");
	Image negativeIcon = ResourceManager.getPluginImage("EkekoSnippets", "icons/negative.gif");
	
	public static Object[] getArray(Object clojureList) {
		return (Object[]) RT.var("clojure.core", "to-array").invoke(clojureList);
	}

	public static String[] getString(Object clojureList) {
		Object[] aResult = getArray(clojureList);
		String[] arrStr = new String[aResult.length + 2];
		arrStr[0] = ""; arrStr[1] = "";
		for (int i = 0; i < aResult.length; i++) {
			arrStr[i+2] = aResult[i].toString();
		}
		return arrStr;
	}

	public void putData() {
		Object[] arrResult = result;
		if (arrResult.length > 0)
			createColumn(getArray(arrResult[0]));
		
		for (int i = 1; i < arrResult.length; i++) {
			TreeItem item = new TreeItem(treeResult, 0);
			item.setText(getString(arrResult[i]));
			item.setData(arrResult[i]);
	    }	

	}
	
	public void addPositiveResult() {
		TreeItem[] selected = getSelectedResults();
		for (int i=0; i < selected.length; i++) {
			TreeItem item = new TreeItem(treeConfirmResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			item.setBackground(new Color(Display.getCurrent(), 0, 255, 0));
			item.setImage(1, positiveIcon);
			selected[i].dispose();
		}
	}

	public void addNegativeResult() {
		TreeItem[] selected = getSelectedResults();
		for (int i=0; i < selected.length; i++) {
			TreeItem item = new TreeItem(treeConfirmResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			item.setBackground(new Color(Display.getCurrent(), 255, 0, 0));
			item.setImage(1, negativeIcon);
			selected[i].dispose();
		}
	}

	public void addPositiveConfirmResult() {
		
	}

	public void addNegativeConfirmResult() {
		
	}

	public void removeConfirmResult() {
		TreeItem[] selected = getSelectedConfirmResults();
		for (int i=0; i < selected.length; i++) {
			TreeItem item = new TreeItem(treeResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			selected[i].dispose();
		}
	}
}
