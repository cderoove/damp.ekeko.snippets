package damp.ekeko.snippets.gui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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
	private Table tableResult;
	private Table tableConfirmResult;
	private TableDecorator tableConfirmResultDecorator;
	private Table tableOperator;
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
				
		tableResult = new Table(group_1, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_tableResult = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableResult.heightHint = 44;
		tableResult.setLayoutData(gd_tableResult);
		tableResult.setLinesVisible(true);
		tableResult.setHeaderVisible(true);
		
		TableColumn tblclmnResult = new TableColumn(tableResult, SWT.NONE);
		tblclmnResult.setWidth(0);

		TableColumn tblclmnResult2 = new TableColumn(tableResult, SWT.NONE);
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

		tableConfirmResult = new Table(group_1, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_tableConfirmResult = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_tableConfirmResult.heightHint = 167;
		tableConfirmResult.setLayoutData(gd_tableConfirmResult);
		tableConfirmResult.setLinesVisible(true);
		tableConfirmResult.setHeaderVisible(true);
	    tableConfirmResultDecorator = new TableDecorator(tableConfirmResult);
		
		TableColumn tblclmnCResult = new TableColumn(tableConfirmResult, SWT.NONE);
		tblclmnCResult.setWidth(0);
		
		TableColumn tblclmnCResult2 = new TableColumn(tableConfirmResult, SWT.NONE);
		tblclmnCResult2.setWidth(20);

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

		tableOperator = new Table(group_2, SWT.BORDER | SWT.FULL_SELECTION);
		tableOperator.setHeaderVisible(true);
		GridData gd_tableOperator = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tableOperator.heightHint = 213;
		tableOperator.setLayoutData(gd_tableOperator);
		
		TableColumn tblclmnOperator = new TableColumn(tableOperator, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
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
			TableColumn tblclmn = new TableColumn(tableResult, SWT.NONE);
			tblclmn.setWidth(250);
			tblclmn.setText(arrCol[i].toString());

			TableColumn tblclmnr = new TableColumn(tableConfirmResult, SWT.NONE);
			tblclmnr.setWidth(250);
			tblclmn.setText(arrCol[i].toString());
		}
	}

	public TableItem[] getSelectedResults() {
		return tableResult.getSelection();
	}
	
	public TableItem[] getSelectedConfirmResults() {
		return tableConfirmResult.getSelection();
	}

	// Logic Code
	//------------------------------
	
	Image positiveIcon = ResourceManager.getPluginImage("EkekoSnippets", "icons/positive.gif");
	Image negativeIcon = ResourceManager.getPluginImage("EkekoSnippets", "icons/negative.gif");
	Color green = new Color(Display.getCurrent(), 0, 255, 0);
	Color red = new Color(Display.getCurrent(), 255, 0, 0);
	
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
			TableItem item = new TableItem(tableResult, 0);
			item.setText(getString(arrResult[i]));
			item.setData(arrResult[i]);
	    }	

		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}
	
	public void addPositiveResult() {
		tableConfirmResultDecorator.removeAllEditors();
		tableConfirmResult.getItem(tableConfirmResult.getItemCount()-1).dispose();
		
		TableItem[] selected = getSelectedResults();
		for (int i=0; i < selected.length; i++) {
			TableItem item = new TableItem(tableConfirmResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			item.setBackground(green);
			item.setImage(1, positiveIcon);
			selected[i].dispose();
		}
		
		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}

	public void addNegativeResult() {
		tableConfirmResultDecorator.removeAllEditors();
		tableConfirmResult.getItem(tableConfirmResult.getItemCount()-1).dispose();

		TableItem[] selected = getSelectedResults();
		for (int i=0; i < selected.length; i++) {
			TableItem item = new TableItem(tableConfirmResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			item.setBackground(red);
			item.setImage(1, negativeIcon);
			selected[i].dispose();
		}

		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}

	public void addPositiveConfirmResult() {
		tableConfirmResultDecorator.removeAllEditors();
		TableItem lastItem = tableConfirmResult.getItem(tableConfirmResult.getItemCount()-1);
		//set data for the last item --> item.setData(selected[i].getData());
		lastItem.setBackground(red);
		lastItem.setImage(1, positiveIcon);
		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}

	public void addNegativeConfirmResult() {
		tableConfirmResultDecorator.removeAllEditors();
		TableItem lastItem = tableConfirmResult.getItem(tableConfirmResult.getItemCount()-1);
		//set data for the last item --> item.setData(selected[i].getData());
		lastItem.setBackground(green);
		lastItem.setImage(1, negativeIcon);
		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}

	public void removeConfirmResult() {
		tableConfirmResultDecorator.removeAllEditors();
		tableConfirmResult.getItem(tableConfirmResult.getItemCount()-1).dispose();

		TableItem[] selected = getSelectedConfirmResults();
		for (int i=0; i < selected.length; i++) {
			TableItem item = new TableItem(tableResult, 0);
			item.setData(selected[i].getData());
			item.setText(getString(selected[i].getData()));
			selected[i].dispose();
		}
		
		tableConfirmResultDecorator.setButtonEditorAtNewRow();
	}
}
