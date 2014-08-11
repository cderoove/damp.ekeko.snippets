package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.ConfigureColumns;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import polyglot.types.TableResolver;
import damp.ekeko.EkekoPlugin;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class IntendedResultsEditor extends EditorPart {

	public static final String ID = "damp.ekeko.snippets.gui.IntendedResultsEditor"; //$NON-NLS-1$

	private ToolBar toolBar;

	private TableViewer positiveViewer;
	private Table positiveViewerTable;
	private int activeColumn = -1;

	private TableViewer negativeViewer;

	private Table negativeViewerTable;


	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1,true));
		toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

		ToolItem toolitemInitialize = new ToolItem(toolBar, SWT.NONE);
		toolitemInitialize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
			}

		});
		//toolitemInitialize.setImage(EkekoSnippetsPlugin.IMG_INTENDED_RESULTS);
		toolitemInitialize.setToolTipText("Initialize");


		ToolItem toolitemAddColumn = new ToolItem(toolBar, SWT.NONE);
		toolitemAddColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addColumn(positiveViewerTable.getColumnCount());
			}
		});
		toolitemAddColumn.setImage(EkekoSnippetsPlugin.IMG_ADD);
		toolitemAddColumn.setToolTipText("Add Column");
		
		ToolItem toolitemAddPositive = new ToolItem(toolBar, SWT.NONE);
		toolitemAddPositive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		toolitemAddPositive.setImage(EkekoSnippetsPlugin.IMG_POSITIVE_EXAMPLE);
		toolitemAddPositive.setToolTipText("Add Positive Example");
		
		ToolItem toolitemAddNegative = new ToolItem(toolBar, SWT.NONE);
		toolitemAddNegative.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		toolitemAddNegative.setImage(EkekoSnippetsPlugin.IMG_NEGATIVE_EXAMPLE);
		toolitemAddNegative.setToolTipText("Add Negative Example");
		

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		GridData sashGD = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		sash.setLayoutData(sashGD);
			
		positiveViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		positiveViewerTable = positiveViewer.getTable();
		positiveViewerTable.setLinesVisible(true);
		positiveViewerTable.setHeaderVisible(true);
		
		
		positiveViewer.setContentProvider(new ArrayContentProvider());		
		positiveViewer.setLabelProvider(new LabelProvider());
		addColumn(positiveViewer, 0, "?match");
		positiveViewer.setInput(new String[]{"a", "b"});
		
		negativeViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		negativeViewerTable = negativeViewer.getTable();
		negativeViewerTable.setLinesVisible(true);
		negativeViewerTable.setHeaderVisible(true);
		
		
		
		
		negativeViewer.setContentProvider(new ArrayContentProvider());		
		negativeViewer.setLabelProvider(new LabelProvider());
		addColumn(negativeViewer, 0, "?match");
		negativeViewer.setInput(new String[]{"a", "b"});
		
		


		
		
		addActiveColumnListener(positiveViewerTable);
		addActiveColumnListener(negativeViewerTable);
		
		addMenu(positiveViewerTable);
		addMenu(negativeViewerTable);

	}

	private void addMenu(final Table table) {
		final MenuManager mgr = new MenuManager();

		final Action insertColumnBefore = new Action("Insert New Column Before") {
			public void run() {
				addColumn(activeColumn);
			}
		};
		
		//insertColumnBefore.setImageDescriptor(EkekoSnippetsPlugin.IMG_ADD_COLUMN);
		
		final Action insertColumnAfter = new Action("Insert New Column After") {
			public void run() {
				addColumn(activeColumn + 1);
			}
		};
		
		final Action removeColumn = new Action("Remove Column") {
			public void run() {
				removeColumn(activeColumn);
			}
		};
		//removeColumn.setImageDescriptor(EkekoSnippetsPlugin.getImageDescriptor("/icons/minus-white.png"));



		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (table.getColumnCount() == 1) {
					manager.add(insertColumnBefore);
					manager.add(insertColumnAfter);
				} else {
					manager.add(removeColumn);
				}
			}


		});

		table.setMenu(mgr.createContextMenu(table));
	}

	private void addActiveColumnListener(final Table table) {
		table.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				int x = 0;
				for (int i = 0; i < table.getColumnCount(); i++) {
					x +=table.getColumn(i).getWidth();
					if (e.x <= x) {
						activeColumn = i;
						break;
					}
				}
			}
		});
	}

	protected void removeColumn(int columnIndex) {
		positiveViewerTable.getColumn(columnIndex).dispose();
		positiveViewerTable.layout(true);
		
		negativeViewerTable.getColumn(columnIndex).dispose();
		negativeViewerTable.layout(true);
	}

	protected void addRow() {
	}

	protected TableViewerColumn addColumn(TableViewer viewer, int columnIndex, String attributeName) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(200);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider());
		return column;
	}
	

	protected void addColumn(int columnIndex) {
		String attributeName = getAttributeName();
		if(attributeName == null)
			return;
		addColumn(positiveViewer, columnIndex, attributeName);
		addColumn(negativeViewer, columnIndex, attributeName);
	}




	@Override
	public void setFocus() {
		//Object cljTransformation = transformationEditor.getTransformation();
		//textViewerSnippet.getControl().setFocus();

	}
	
	protected String getAttributeName() {
        InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),"Attribute Name", "Please enter a name for the new attribute", "?attribute", new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if(newText.length() < 1 || 
						newText.charAt(0) != '?') {
					return "Attribute name should start with '?'";
				}
				return null;
			}
		});
        if (dlg.open() == Window.OK) {
            return dlg.getValue();
        }
        else 
        	return null;
	}
	
	


}
