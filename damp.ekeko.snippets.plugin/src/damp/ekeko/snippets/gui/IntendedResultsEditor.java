package damp.ekeko.snippets.gui;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.part.EditorPart;

import com.google.common.collect.Iterators;

import damp.ekeko.gui.EkekoLabelProvider;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class IntendedResultsEditor extends EditorPart {
	public IntendedResultsEditor() {
	}

	public static final String ID = "damp.ekeko.snippets.gui.IntendedResultsEditor"; //$NON-NLS-1$

	private IEditorPart linkedTransformationOrTemplateEditor;
	private TemplateEditor linkedTemplateEditor;
	private ToolBar toolBar;

	private TableViewer matchesViewer;
	private Table matchesViewerTable;
	private int activeColumn = -1;

	private TableViewer verifiedViewer;
	private Table verifiedViewerTable;

	private Link linkStatus;

	private Button linkButton;

	private ToolItem toolitemCompareResults;
	
	private EkekoLabelProvider ekekoLabelProvider;


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
		parent.setLayout(new GridLayout(2,true));
		toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));

		ToolItem toolitemInitialize = new ToolItem(toolBar, SWT.NONE);
		toolitemInitialize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//initialize from file, clone instances, recorded changes, diff ...
				//probably want drop down menu
			}

		});
		toolitemInitialize.setImage(EkekoSnippetsPlugin.IMG_RESULTS_IMPORT);
		toolitemInitialize.setToolTipText("Initialize intended results");


		ToolItem toolitemAddColumn = new ToolItem(toolBar, SWT.NONE);
		toolitemAddColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addColumn(matchesViewerTable.getColumnCount());
			}
		});
		toolitemAddColumn.setImage(EkekoSnippetsPlugin.IMG_COLUMN_ADD);
		toolitemAddColumn.setToolTipText("Add Column");

		
		toolitemCompareResults = new ToolItem(toolBar, SWT.NONE);
		toolitemCompareResults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onCompareResults();
			}
		});
		toolitemCompareResults.setImage(EkekoSnippetsPlugin.IMG_RESULTS_REFRESH);
		toolitemCompareResults.setToolTipText("Compare results");
		
		
		final ToolItem tltmSearchModifications = new ToolItem(toolBar, SWT.NONE);
		tltmSearchModifications.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSearchModifications();
			}
		});
		tltmSearchModifications.setImage(EkekoSnippetsPlugin.IMG_SEARCH);
		tltmSearchModifications.setToolTipText("Suggest suitable modifications to template");
		tltmSearchModifications.setEnabled(false);



		linkStatus = new Link(parent, SWT.NONE);
		linkStatus.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		linkToEditor(null);

		linkStatus.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.text.equals("linked")) {
					onEditLink();
				} else {
					onRevealLinkedEditor();
				}
			}});

		linkButton = new Button(parent, SWT.NONE);
		linkButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		linkButton.setText("Link to editor...");
		linkButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEditLink();
			}
		});

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));


		matchesViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		matchesViewerTable = matchesViewer.getTable();
		matchesViewerTable.setLinesVisible(true);
		matchesViewerTable.setHeaderVisible(true);


		matchesViewer.setContentProvider(new ArrayContentProvider());		
		//matchesViewer.setLabelProvider(new EkekoLabelProvider());
		//addColumn(matchesViewer, 0, "?match");
		//matchesViewer.setInput(new String[]{"a", "b"});

		
		Composite bottomComposite = new Composite(sash, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		bottomComposite.setLayout(gridLayout);
		
		
		ToolBar bottomToolBar = new ToolBar(bottomComposite, SWT.FLAT | SWT.RIGHT);
		bottomToolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		ToolItem toolitemAddPositive = new ToolItem(bottomToolBar, SWT.NONE);
		toolitemAddPositive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddPositiveExample();
			}
		});
		toolitemAddPositive.setImage(EkekoSnippetsPlugin.IMG_POSITIVE_EXAMPLE);
		toolitemAddPositive.setToolTipText("Mark as true positive");

		ToolItem toolitemAddNegative = new ToolItem(bottomToolBar, SWT.NONE);
		toolitemAddNegative.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddNegativeExample();
			}
		});
		toolitemAddNegative.setImage(EkekoSnippetsPlugin.IMG_NEGATIVE_EXAMPLE);
		toolitemAddNegative.setToolTipText("Mark as false positive");


		verifiedViewer = new TableViewer(bottomComposite, SWT.BORDER | SWT.FULL_SELECTION);
		verifiedViewerTable = verifiedViewer.getTable();
		verifiedViewerTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		verifiedViewerTable.setLinesVisible(true);
		verifiedViewerTable.setHeaderVisible(true);

		verifiedViewer.setContentProvider(new ArrayContentProvider());		
		//verifiedViewer.setLabelProvider(new EkekoLabelProvider());
		//addColumn(verifiedViewer, 0, "?match");
		//verifiedViewer.setInput(new String[]{"a", "b"});

		addActiveColumnListener(matchesViewerTable);
		addActiveColumnListener(verifiedViewerTable);

		addMenu(matchesViewerTable);
		addMenu(verifiedViewerTable);
		
		
		ekekoLabelProvider = new EkekoLabelProvider();
		
		updateWidgets();

	}

	protected void onSearchModifications() {
		// TODO Auto-generated method stub
		
	}

	protected void onAddPositiveExample() {
		// TODO Auto-generated method stub
		
	}

	protected void onAddNegativeExample() {
		// TODO Auto-generated method stub
		
	}

	protected void onCompareResults() {
		for (TableColumn tableColumn : matchesViewerTable.getColumns()) {
			tableColumn.dispose();
		}
		for (Object object : getResultVariables()) {
			String varname = (String) object;
			TableViewerColumn column = addColumn(matchesViewer, matchesViewerTable.getColumnCount(), varname);
			column.setEditingSupport(new EditingSupport(matchesViewer) {
				@Override
				protected void setValue(Object element, Object value) {
						
				}
				
				@Override
				protected Object getValue(Object element) {
					return element;
				}
				
				@Override
				protected CellEditor getCellEditor(final Object element) {
					return new DialogCellEditor(matchesViewerTable) {
						
						@Override
						protected Object openDialogBox(Control cellEditorWindow) {
							System.out.println(element);
							return null;
						}
					};
				}
				
				@Override
				protected boolean canEdit(Object element) {
					return true;
				}
			});
		}

		matchesViewerTable.layout(true);
		matchesViewer.setInput(getResults());
		
	}

	protected void onRevealLinkedEditor() {
		if(linkedTemplateEditor != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(linkedTransformationOrTemplateEditor);
		}

	}

	private List<IEditorPart> getTemplateEditors() {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		LinkedList<IEditorPart> editors = new LinkedList<>();
		for(IEditorReference ref : editorReferences) {
			IEditorPart editor = ref.getEditor(true);
			if(editor instanceof TemplateEditor) {
				editors.add(editor);
			}
			if(editor instanceof TransformationEditor) {
				editors.add(editor);
			}
		}
		return editors;
	}

	protected void onEditLink() {
		ListDialog listSelectionDialog = new ListDialog(getSite().getShell());
		listSelectionDialog.setContentProvider(new ArrayContentProvider());
		listSelectionDialog.setLabelProvider(new WorkbenchPartLabelProvider());
		listSelectionDialog.setInput(getTemplateEditors().toArray());
		listSelectionDialog.setTitle("Select Ekeko/X editor");
		listSelectionDialog.setMessage("Select the Ekeko/X template or transformation editor to link to.");
		int open = listSelectionDialog.open();
		if(open == listSelectionDialog.OK) {
			Object[] result = listSelectionDialog.getResult();
			if(result.length == 1) {
				linkToEditor((IEditorPart) result[0]);
			}
		}

	}
	
	private void updateLinkWidget() {
		if(linkedTransformationOrTemplateEditor instanceof TransformationEditor) {
			linkStatus.setText("Linked to LHS of transformation editor on <a>" + linkedTransformationOrTemplateEditor.getEditorInput().getName() + "</a>");
		}
		if(linkedTransformationOrTemplateEditor instanceof TemplateEditor) {
			linkStatus.setText("Linked to template editor on <a>" + linkedTransformationOrTemplateEditor.getEditorInput().getName() + "</a>");
		}
		if(linkedTransformationOrTemplateEditor == null) {
			linkStatus.setText("Not <a>linked</a> to template editor.");
		}
		linkStatus.pack();

	}
	
	private void updateWidgets() {
		updateLinkWidget();
		toolitemCompareResults.setEnabled(linkedTransformationOrTemplateEditor != null);
		
	}
	

	@SuppressWarnings("rawtypes")
	private Collection getResults() {
		return linkedTemplateEditor.getGroup().getResults();
	}
	
	@SuppressWarnings("rawtypes")
	private Collection getResultVariables() {
		return linkedTemplateEditor.getGroup().getNormalizedMatchVariables();
	}


	private void linkToEditor(IEditorPart editor) {
		if(editor instanceof TransformationEditor) {
			TransformationEditor transformationEditor = (TransformationEditor) editor;
			linkedTemplateEditor = transformationEditor.getSubjectsEditor();
			linkedTransformationOrTemplateEditor = editor;
		}
		if(editor instanceof TemplateEditor) {
			linkedTemplateEditor = (TemplateEditor) editor;
			linkedTransformationOrTemplateEditor = editor;
		}
		if(editor == null) {
			linkedTemplateEditor = null;
			linkedTransformationOrTemplateEditor = null;
		}
		updateWidgets();
	}

	private void addMenu(final Table table) {
		final MenuManager mgr = new MenuManager();
		final Action insertColumnAfter = new Action("Insert New Column After") {
			public void run() {
				addColumn(activeColumn + 1);
			}
		};
		insertColumnAfter.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_COLUMN_ADD));


		final Action removeColumn = new Action("Remove Column") {
			public void run() {
				removeColumn(activeColumn);
			}
		};
		removeColumn.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_COLUMN_DELETE));



		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (table.getColumnCount() == 1) {
					manager.add(insertColumnAfter);
				} else {
					manager.add(insertColumnAfter);
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
		matchesViewerTable.getColumn(columnIndex).dispose();
		matchesViewerTable.layout(true);

		verifiedViewerTable.getColumn(columnIndex).dispose();
		verifiedViewerTable.layout(true);
	}

	protected void addRow() {
	}

	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(200);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {
							
			@Override
			public String getText(Object element) {
				@SuppressWarnings("rawtypes")
				Collection row = (Collection) element;
				
				Iterator iterator = row.iterator();
				for(int i=0; i<columnIndex; i++){
					iterator.next();
				}
				return ekekoLabelProvider.getText(iterator.next());
			}
		});
		return column;
	}


	protected TableViewerColumn addColumn(int columnIndex) {
		String attributeName = getAttributeName();
		if(attributeName == null)
			return null;
		//addColumn(matchesViewer, columnIndex, attributeName);
		return addColumn(verifiedViewer, columnIndex, attributeName);
	}




	@Override
	public void setFocus() {
		//Object cljTransformation = transformationEditor.getTransformation();
		//textViewerSnippet.getControl().setFocus();

	}

	public void setTemplateEditor(TemplateEditor templateEditor) {
		this.linkedTemplateEditor = templateEditor;
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



	public void setPreviouslyActiveEditor(IEditorPart activeEditor) {
	}




}
