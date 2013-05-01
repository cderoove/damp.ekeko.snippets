package damp.ekeko.snippets.gui;

import java.util.LinkedList;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import damp.ekeko.snippets.data.Groups;
import damp.ekeko.snippets.gui.SnippetView.QueryResultThread;

public class GroupView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.GroupView"; //$NON-NLS-1$
	private Table tableGroup;
	private Text txtApplyTransformationTo;
	private Groups groups;
	private Text txtDoubleClickOn;
	private Text txtLoadGroupsFrom;
	private Text text;
	private Text txtSaveGroups;

	public GroupView() {
		groups = new Groups();
	}

	public Groups getGroups() {
		return groups;
	}
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		{
			Group grpGroup = new Group(container, SWT.NONE);
			grpGroup.setText("Groups");
			grpGroup.setLayout(new GridLayout(1, false));
			
			ToolBar toolBar = new ToolBar(grpGroup, SWT.FLAT | SWT.RIGHT);
			toolBar.setOrientation(SWT.RIGHT_TO_LEFT);
			GridData gd_toolBar = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
			gd_toolBar.widthHint = 282;
			toolBar.setLayoutData(gd_toolBar);
			
			ToolItem tltmRemove = new ToolItem(toolBar, SWT.NONE);
			tltmRemove.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					removeGroup();
				}
			});
			tltmRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
			tltmRemove.setToolTipText("Remove Group");
			
			ToolItem tltmAdd = new ToolItem(toolBar, SWT.NONE);
			tltmAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addGroup();
				}
			});
			tltmAdd.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
			tltmAdd.setToolTipText("Add Group");

			TableViewer tableViewer = new TableViewer(grpGroup, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
			tableGroup = tableViewer.getTable();
			tableGroup.setLinesVisible(true);
			tableGroup.setHeaderVisible(true);
			tableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tblclmnGroup = tableViewerColumn.getColumn();
			tblclmnGroup.setWidth(100);
			tblclmnGroup.setText("Name");
			
			TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
			TableColumn tblclmnDescription = tableViewerColumn_1.getColumn();
			tblclmnDescription.setWidth(300);
			tblclmnDescription.setText("Description");
			
			txtDoubleClickOn = new Text(grpGroup, SWT.BORDER);
			txtDoubleClickOn.setText("Double click on group to open Snippet View");
			txtDoubleClickOn.setEditable(false);
			txtDoubleClickOn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

			tableGroup.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					openSnippetView();
				}
			});
}
		{
			Group grpGroup_1 = new Group(container, SWT.NONE);
			grpGroup_1.setLayout(new GridLayout(2, false));
			
			text = new Text(grpGroup_1, SWT.BORDER);
			text.setEditable(false);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			text.setText("Ekeko Snippets and Transformation");
			
			Button btnTransform = new Button(grpGroup_1, SWT.NONE);
			btnTransform.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnTransform.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					transform();
				}
			});
			btnTransform.setText("Transform");
			
			txtApplyTransformationTo = new Text(grpGroup_1, SWT.BORDER);
			txtApplyTransformationTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			txtApplyTransformationTo.setEditable(false);
			txtApplyTransformationTo.setText("Check on groups, and apply transformation to selected groups.");
			
			Button btnLoad = new Button(grpGroup_1, SWT.NONE);
			btnLoad.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/obj16/fldr_obj.gif"));
			btnLoad.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnLoad.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					load();
				}
			});
			btnLoad.setText("Load");
			
			txtLoadGroupsFrom = new Text(grpGroup_1, SWT.BORDER);
			txtLoadGroupsFrom.setEditable(false);
			txtLoadGroupsFrom.setText("Load groups from file.");
			txtLoadGroupsFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Button btnSave = new Button(grpGroup_1, SWT.NONE);
			btnSave.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/saveas_edit.gif"));
			btnSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					save();
				}
			});
			btnSave.setText("Save");
			
			txtSaveGroups = new Text(grpGroup_1, SWT.BORDER);
			txtSaveGroups.setEditable(false);
			txtSaveGroups.setText("Save groups.");
			txtSaveGroups.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(grpGroup_1, SWT.NONE);
		}

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
	
	public Object[] getCheckedGroup() {
		TableItem[] items = tableGroup.getItems();
		LinkedList list = new LinkedList();

		for (int i=0; i<items.length; i++) {
			if (items[i].getChecked())
				list.add(items[i].getText());
		}
		
		return list.toArray();
	}

	public void renderGroups() {
		tableGroup.removeAll();
		Object[] groupNames = groups.getGroups();
		for (int i=0; i<groupNames.length; i++) {
			TableItem item = new TableItem(tableGroup, 0);
			item.setText(groupNames[i].toString());
		}

	}

	public void addGroup() {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
	            "Add Group", "Group Name", null, null);
        if (dlg.open() == Window.OK) {
    		groups.addGroup(dlg.getValue());
    		renderGroups();
        }
	}

	public void removeGroup() {
		if (tableGroup.getSelectionCount() > 0) {
			String selected = tableGroup.getSelection()[0].getText();
			groups.removeGroup(selected);
			renderGroups();
		}
	}

	public void openSnippetView() {
		if (tableGroup.getSelectionCount() > 0) {
			String selected = tableGroup.getSelection()[0].getText();
			try {
				SnippetView view = (SnippetView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.SnippetView");
				view.setGroup(groups, groups.getGroup(selected));
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
	
	class TransformThread extends Thread {
		Object[] checkedGroups;
		
		public TransformThread (Object[] checkedGroups) {
			this.checkedGroups = checkedGroups;
		}
		
        public void run() {
			groups.transform(checkedGroups);

			Display.getDefault().syncExec(new Runnable() {
    		    public void run() {
    				boolean m = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
    						"Info", "Transformation process is done.");
    		    }
    		});
        }
    }	
	
	TransformThread trThread; 
	
	public void transform() {
		boolean b = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
				 "Transformation", "Apply Transformation?");
		
		if (b) {
			trThread = new TransformThread(getCheckedGroup());
			trThread.start();
		}
	}
	
	public void save() {
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		String filename = dialog.open();
		if (filename != null && !filename.isEmpty()) {
			if (!filename.contains(".snp")) filename += ".snp";
			groups.save(filename);	
			boolean m = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
					"Info", "Snippet groups are saved.");
		}
	}

	public void load() {
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.snp", "*.*" });
		String filename = dialog.open();
		if (filename != null && !filename.isEmpty()) {
			groups.load(filename);
			renderGroups();
		}
	}
}
