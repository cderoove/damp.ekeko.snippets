package damp.ekeko.snippets.gui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

import clojure.lang.IFn;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import baristaui.util.MarkerUtility;
import damp.ekeko.gui.EkekoLabelProvider;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class RecommendationEditor extends EditorPart {
	public RecommendationEditor() {
	}

	public static final String ID = "damp.ekeko.snippets.gui.RecommendationEditor"; //$NON-NLS-1$

	public static IFn FN_PROJECT_VALUE_IDENTIFIER;
	public static IFn FN_PROJECT_TUPLE_IDENTIFIER;
	public static IFn FN_IDENTIFIER_CORRESPONDING_PROJECT_VALUE;
	
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

	private ToolItem toolitemAddFromTemplate;

	private EkekoLabelProvider ekekoLabelProvider;

	private ToolItem toolitemAddPositive;


	private Set<Collection<Object>> results = new HashSet<>();
	
	private Set<Object> positiveIDs = new HashSet<>();
	private Set<Object> negativeIDs = new HashSet<>();

	private IStructuredContentProvider verifiedContentProvider;

	private ArrayContentProvider matchesContentProvider;

	private ToolItem toolitemDeleteVerifiedResult;


	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	public static Object projectValueIdentifier(Object value) {
		return FN_PROJECT_VALUE_IDENTIFIER.invoke(value);
	}
	
	public static Object projectTupleIdentifier(Object value) {
		return FN_PROJECT_TUPLE_IDENTIFIER.invoke(value);
	}
	
	public static Object projectvalueForIdentifier(Object identifier) {
		return FN_IDENTIFIER_CORRESPONDING_PROJECT_VALUE.invoke(identifier);
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

		toolitemAddFromTemplate = new ToolItem(toolBar, SWT.NONE);
		toolitemAddFromTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddMatchesFromTemplate();
			}
		});
		toolitemAddFromTemplate.setImage(EkekoSnippetsPlugin.IMG_ADD);
		toolitemAddFromTemplate.setToolTipText("Add the matches of a template as desired matches");


		toolitemAddPositive = new ToolItem(toolBar, SWT.NONE);
		toolitemAddPositive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddPositiveExample();
			}
		});
		toolitemAddPositive.setImage(EkekoSnippetsPlugin.IMG_POSITIVE_EXAMPLE);
		toolitemAddPositive.setToolTipText("Mark as positive example");


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

		// Top component of the SashForm
		matchesViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		matchesViewerTable = matchesViewer.getTable();
		matchesViewerTable.setLinesVisible(true);
		matchesViewerTable.setHeaderVisible(true);


		matchesViewer.setContentProvider(new ArrayContentProvider());		
		
		matchesContentProvider = new ArrayContentProvider();
		matchesViewer.setContentProvider(matchesContentProvider);		
		matchesViewer.setInput(results);

		matchesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				toolitemAddPositive.setEnabled(!selection.isEmpty());

			}
		});
		
		// Bottom component of the sash form
//		SwtLiveChartViewer view = new SwtLiveChartViewer(sash, SWT.NO_BACKGROUND);
//        Chart chart = view.createLiveChart();
		
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
		chart.getPlot().setBackground(ColorDefinitionImpl.ORANGE());
		chart.getLegend().setItemType(LegendItemType.CATEGORIES_LITERAL);
		chart.getLegend().setVisible(true);
		chart.getTitle().getLabel().getCaption().setValue("Hello world");
		
		Axis xAxis = ((ChartWithAxes) chart).getPrimaryBaseAxes()[0];
        xAxis.getTitle().setVisible(true);
        xAxis.getTitle().getCaption().setValue("Some x axis");
        
        Axis yAxis = ((ChartWithAxes) chart).getPrimaryOrthogonalAxis(xAxis);
        yAxis.getTitle().setVisible(true);
        yAxis.getTitle().getCaption().setValue("Some y axis");
        yAxis.getScale().setStep(1.0);

        TextDataSet xValues = TextDataSetImpl.create(new String[]{"hello", "world"});
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(xValues);
        SeriesDefinition sdX = SeriesDefinitionImpl.create();
        sdX.getSeriesPalette().update(1);
        xAxis.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);
        
        NumberDataSet yDataSet = NumberDataSetImpl.create(new double[]{1.4, 3.5});
        BarSeries bs1 = (BarSeries) BarSeriesImpl.create();
        bs1.setDataSet(yDataSet);
        SeriesDefinition sdY = SeriesDefinitionImpl.create();
        yAxis.getSeriesDefinitions().add(sdY);
        sdY.getSeries().add(bs1);

        ChartCanvas view = new ChartCanvas(sash, SWT.NO_BACKGROUND);
		view.setChart(chart);

		//**** Bottom ****
//		Composite bottomComposite = new Composite(sash, SWT.NONE);
//		GridLayout gridLayout = new GridLayout();
//		gridLayout.marginWidth = 0;
//		gridLayout.marginHeight = 0;
//		bottomComposite.setLayout(gridLayout);
//
//
//		ToolBar bottomToolBar = new ToolBar(bottomComposite, SWT.FLAT | SWT.RIGHT);
//		bottomToolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
//
//		ToolItem toolitemInitialize = new ToolItem(bottomToolBar, SWT.NONE);
//		toolitemInitialize.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				//initialize from file, clone instances, recorded changes, diff ...
//				//probably want drop down menu
//			}
//
//		});
//		toolitemInitialize.setImage(EkekoSnippetsPlugin.IMG_RESULTS_IMPORT);
//		toolitemInitialize.setToolTipText("Initialize intended results");
//
//		ToolItem toolitemAddColumn = new ToolItem(bottomToolBar, SWT.NONE);
//		toolitemAddColumn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				addColumnToVerifiedViewer(verifiedViewerTable.getColumnCount());
//				verifiedViewer.refresh();
//			}
//		});
//		toolitemAddColumn.setImage(EkekoSnippetsPlugin.IMG_COLUMN_ADD);
//		toolitemAddColumn.setToolTipText("Add Column");
//		
//		
//		toolitemDeleteVerifiedResult = new ToolItem(bottomToolBar, SWT.NONE);
//		toolitemDeleteVerifiedResult.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				onDeleteVerifiedResult();
//			}
//		});
//		toolitemDeleteVerifiedResult.setImage(EkekoSnippetsPlugin.IMG_DELETE);
//		toolitemDeleteVerifiedResult.setToolTipText("Delete example");
//
//		verifiedViewer = new TableViewer(bottomComposite, SWT.BORDER | SWT.FULL_SELECTION);
//		verifiedViewerTable = verifiedViewer.getTable();
//		verifiedViewerTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//
//		verifiedViewerTable.setLinesVisible(true);
//		verifiedViewerTable.setHeaderVisible(true);
		
//		TableViewerColumn imgColumn = new TableViewerColumn(verifiedViewer, SWT.NONE, 0);
//		imgColumn.getColumn().setWidth(24);
//		imgColumn.getColumn().setResizable(false);
//		imgColumn.getColumn().setText("");
//		imgColumn.getColumn().setMoveable(false);
//		imgColumn.setLabelProvider(new ColumnLabelProvider() {
//			@SuppressWarnings("rawtypes")
//			@Override
//			public String getText(Object element) {
//				return "";
//			}
//			
//			@Override
//			public org.eclipse.swt.graphics.Image getImage(Object tupleIdentifier) {
//				if(isPositiveIdentifier(tupleIdentifier)) 
//					return EkekoSnippetsPlugin.IMG_POSITIVE_EXAMPLE;
//				if(isNegativeIdentifier(tupleIdentifier))
//					return EkekoSnippetsPlugin.IMG_NEGATIVE_EXAMPLE;
//				return null;
//			}
//			
//		});
//
//
//		verifiedContentProvider = new ArrayContentProvider();
//		verifiedViewer.setContentProvider(verifiedContentProvider);		
//		verifiedViewer.setInput(Sets.union(positiveIDs,negativeIDs));
//		
//		verifiedViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				ISelection selection = event.getSelection();
//				toolitemDeleteVerifiedResult.setEnabled(!selection.isEmpty());
//
//			}
//		});

		
		

		addActiveColumnListener(matchesViewerTable);
//		addActiveColumnListener(verifiedViewerTable);

		addMenu(matchesViewer);
//		addMenu(verifiedViewer);


		ekekoLabelProvider = new EkekoLabelProvider();

		linkToEditor(null);
		updateWidgets();

	}

	protected void onDeleteVerifiedResult() {
		ISelection selection = verifiedViewer.getSelection();
		IStructuredSelection sel = (IStructuredSelection) selection;
		if(sel.isEmpty()) {
			return;
		}
		Object exampleID = sel.getFirstElement();
		positiveIDs.remove(exampleID);
		negativeIDs.remove(exampleID);
		verifiedViewer.refresh();
	}

	protected boolean isPositiveIdentifier(Object tupleIdentifier) {
		return positiveIDs.contains(tupleIdentifier);
	}

	protected void onSearchModifications() {

	}

	protected void addVerifiedExample(boolean isPositive) {
		ISelection selection = matchesViewer.getSelection();
		IStructuredSelection sel = (IStructuredSelection) selection;
		if(sel.isEmpty()) {
			return;
		}
		Collection selectedTuple = (Collection) sel.getFirstElement();
		Object tupleIdentifier = projectTupleIdentifier(selectedTuple);
		if(isPositive) {
			positiveIDs.add(tupleIdentifier);
			negativeIDs.remove(tupleIdentifier);
		} else {
			negativeIDs.add(tupleIdentifier);
			positiveIDs.remove(tupleIdentifier);

		}
		//arraycontentprovider's implementation does nothing
		//verifiedContentProvider.inputChanged(verifiedViewer, verifiedViewer.getInput(), Sets.union(positives, negatives));
		
		//would be better to update only for the added example...
		verifiedViewer.setInput(Sets.union(positiveIDs, negativeIDs));
		matchesViewer.refresh();
	}

	protected void onAddPositiveExample() {
		addVerifiedExample(true);
	}

	protected void onAddNegativeExample() {
		addVerifiedExample(false);
	}

	protected void onAddMatchesFromTemplate() {
		ListDialog listSelectionDialog = new ListDialog(getSite().getShell());
		listSelectionDialog.setContentProvider(new ArrayContentProvider());
		listSelectionDialog.setLabelProvider(new WorkbenchPartLabelProvider());
		listSelectionDialog.setInput(getTemplateEditors().toArray());
		listSelectionDialog.setTitle("Retrieve desired matches from template");
		listSelectionDialog.setMessage("Select a Ekeko/X template or transformation editor.");
		int open = listSelectionDialog.open();
		if(open == listSelectionDialog.OK) {
			Object[] result = listSelectionDialog.getResult();
			if(result.length == 1) {
				IEditorPart ed = ((IEditorPart) result[0]);
				
				if(ed instanceof TransformationEditor) {
					TransformationEditor transformationEditor = (TransformationEditor) ed;
					linkedTemplateEditor = transformationEditor.getSubjectsEditor();
					linkedTransformationOrTemplateEditor = ed;
				}
				if(ed instanceof TemplateEditor) {
					linkedTemplateEditor = (TemplateEditor) ed;
					linkedTransformationOrTemplateEditor = ed;
				}
				
				
				
			}
		}
		
		
		for (TableColumn tableColumn : matchesViewerTable.getColumns()) {
			tableColumn.dispose();
		}
		for (Object object : getResultVariables()) {
			String varname = (String) object;
			final int columnIndex = matchesViewerTable.getColumnCount();
			TableViewerColumn column = addColumn(matchesViewer, columnIndex, varname);

			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					return ekekoLabelProvider.getText(nth((Collection) element, columnIndex));
				}
			});
		}

		matchesViewerTable.layout(true);
		results = new HashSet(getResults());
		matchesViewer.setInput(results);
		
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

		if(linkedTemplateEditor == null) {

			toolitemAddPositive.setEnabled(false);
			toolitemAddFromTemplate.setEnabled(false);
		} else {	
			toolitemAddFromTemplate.setEnabled(true);
			ISelection selection = matchesViewer.getSelection();
			toolitemAddPositive.setEnabled(!selection.isEmpty());
		}
//		toolitemDeleteVerifiedResult.setEnabled(!verifiedViewer.getSelection().isEmpty());
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

	private void addMenu(final TableViewer tableViewer) {
		final Table table = tableViewer.getTable();
		final MenuManager mgr = new MenuManager();
//		final Action insertColumnAfter = new Action("Insert New Column After") {
//			public void run() {
//				addColumnToVerifiedViewer(activeColumn + 1);
//				verifiedViewer.refresh();
//			}
//		};
//		insertColumnAfter.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_COLUMN_ADD));
//
//
//		final Action removeColumn = new Action("Remove Column") {
//			public void run() {
//				removeColumn(activeColumn);
//			}
//		};
//		removeColumn.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_COLUMN_DELETE));


		final Action revealNode = new Action("Reveal In Editor") {
			public void run() {
				revealNode(tableViewer, activeColumn);
			}
		};
		//removeColumn.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_COLUMN_DELETE));



		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
//				if(tableViewer.equals(verifiedViewer)) {
//					if (table.getColumnCount() == 1) {
//						manager.add(insertColumnAfter);
//					} else {
//						manager.add(insertColumnAfter);
//						manager.add(removeColumn);
//					}
//				}
				manager.add(revealNode);
			}


		});

		table.setMenu(mgr.createContextMenu(table));
	}

	protected void revealNode(TableViewer viewer, int activeColumn) {
		ISelection selection = viewer.getSelection();
		IStructuredSelection sel = (IStructuredSelection) selection;
		if(sel.isEmpty()) 
			return;
		Collection tuple = (Collection) sel.getFirstElement();
		Object element = nth(tuple, activeColumn);
		if(element instanceof ASTNode) {
			ASTNode astNode = (ASTNode) element;
			MarkerUtility.getInstance().createMarkerAndGoto(astNode);
		}
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

	private Object nth(Collection coll, int n) {
		Iterator iterator = coll.iterator();
		for(int i=0; i<n; i++){
			iterator.next();
		}
		return iterator.next();
	}

	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(200);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		return column;
	}


	protected TableViewerColumn addColumnToVerifiedViewer(final int columnIndex) {
		String attributeName = getAttributeName();
		if(attributeName == null)
			return null;
		TableViewerColumn col = addColumn(verifiedViewer, columnIndex, attributeName);
		col.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("rawtypes")
			@Override
			public String getText(Object element) {
				Collection tuple = (Collection) element;
				Object identifier = nth(tuple, columnIndex - 1); //0th col is the +/- img
				Object value = projectvalueForIdentifier(identifier);
				return ekekoLabelProvider.getText(value);
			}			
		});
		return col;
	}


	public boolean isPositive(Collection<Object> tuple) {
		Object tupleIdentifier = projectTupleIdentifier(tuple);
		return isPositiveIdentifier(tupleIdentifier);
	}

	public boolean isNegative(Collection<Object> tuple) {
		Object tupleIdentifier = projectTupleIdentifier(tuple);
		return isNegativeIdentifier(tupleIdentifier);
	}

	private boolean isNegativeIdentifier(Object tupleIdentifier) {
		return negativeIDs.contains(tupleIdentifier);
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
