package damp.ekeko.snippets.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
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

import baristaui.util.MarkerUtility;
import clojure.lang.IFn;
import damp.ekeko.gui.EkekoLabelProvider;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

/**
 * The recommendation editor provides the GUI to automatically generalize or refine a template group, given a set of desired matches
 * @author Tim
 */
public class RecommendationEditor extends EditorPart {
	public RecommendationEditor() {
	}

	public static final String ID = "damp.ekeko.snippets.gui.RecommendationEditor"; //$NON-NLS-1$

	public static IFn FN_PROJECT_VALUE_IDENTIFIER;
	public static IFn FN_PROJECT_TUPLE_IDENTIFIER;
	public static IFn FN_IDENTIFIER_CORRESPONDING_PROJECT_VALUE;
	public static IFn FN_EVOLVE;
	
	private TemplateEditor inputTemplateEditor;
	private TemplateEditor linkedTemplateEditor;
	private ToolBar toolBar;

	private TableViewer matchesViewer;
	private Table matchesViewerTable;
	private int activeColumn = -1;

	private Table verifiedViewerTable;

	private Link linkStatus;

	private ToolItem toolitemAddFromTemplate;
	private ToolItem toolitemSetInputTemplate;

	private EkekoLabelProvider ekekoLabelProvider;

	private Set<Collection<Object>> desiredMatches = new HashSet<>();
	private List<List<Object>> evolveResults = new ArrayList<List<Object>>();
	private List<Object> bestTemplatePerGen = new ArrayList<Object>();

	private ToolItem toolitemDeleteDesiredMatch;
	
	// Default options of the underlying genetic search algorithm; see search.clj/config-default for more information
	private String evolveConfig = ":max-generations 100\n"
			+ ":population-size 20\n"
			+ ":selection-weight 1/4\n"
			+ ":mutation-weight 3/4\n"
			+ ":crossover-weight 0/4\n"
			+ ":fitness-weights [18/20 2/20 0/20]\n"
			+ ":fitness-threshold 0.95\n"
			+ ":output-dir nil\n"
			+ ":partial-matching true\n"
			+ ":quick-matching false\n"
			+ ":match-timeout 10000\n"
			+ ":tournament-rounds 7";
	private String outputDir = "";

	// Components of the fitness chart
	private Series generationAxis;
	private LineSeries f1Data;
	private LineSeries partialData;
	private ChartCanvas canvasView;

	private TextViewer bestTemplateTextArea;

	private TableViewer resultsTableViewer;

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
	
	public static Object evolve(Object inputTemplate, Object desiredMatches, RecommendationEditor gui, String config) {
		return FN_EVOLVE.invoke(inputTemplate, desiredMatches, gui, "{" + config + "}");
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
		
		// **** Toolbar at the top of the editor
		toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
		
		toolitemSetInputTemplate = new ToolItem(toolBar, SWT.NONE);
		toolitemSetInputTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEditLink();
			}
		});
		toolitemSetInputTemplate.setImage(EkekoSnippetsPlugin.IMG_ADD);
		toolitemSetInputTemplate.setToolTipText("Set the input template that needs recommendations");
		
		toolitemAddFromTemplate = new ToolItem(toolBar, SWT.NONE);
		toolitemAddFromTemplate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddMatchesFromTemplate();
			}
		});
		toolitemAddFromTemplate.setImage(EkekoSnippetsPlugin.IMG_TEMPLATE_ADD);
		toolitemAddFromTemplate.setToolTipText("Add the matches of a template as desired matches");

		toolitemDeleteDesiredMatch = new ToolItem(toolBar, SWT.NONE);
		toolitemDeleteDesiredMatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDeleteFromDesiredMatches();
			}
		});
		toolitemDeleteDesiredMatch.setImage(EkekoSnippetsPlugin.IMG_TEMPLATE_DELETE);
		toolitemDeleteDesiredMatch.setToolTipText("Delete from desired matches");

		ToolItem toolitemPopInspector = new ToolItem(toolBar, SWT.NONE);
		toolitemPopInspector.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPopulationInspector((IStructuredSelection) resultsTableViewer.getSelection());
			}
		});
		toolitemPopInspector.setImage(EkekoSnippetsPlugin.IMG_PROPERTIES);
		toolitemPopInspector.setToolTipText("Open inspector for the selected generation");
		
		ToolItem toolitemConfig = new ToolItem(toolBar, SWT.NONE);
		toolitemConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEditConfig();
			}
		});
		toolitemConfig.setImage(EkekoSnippetsPlugin.IMG_TRANSFORMATION);
		toolitemConfig.setToolTipText("Edit configuration");
		
		final ToolItem toolitemEvolve = new ToolItem(toolBar, SWT.NONE);
		toolitemEvolve.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (inputTemplateEditor == null) {
					ErrorDialog.openError(Display.getCurrent().getActiveShell(), 
							"No input template", null, 
							new Status(IStatus.INFO, EkekoSnippetsPlugin.PLUGIN_ID, 
									"Please choose an input template. The recommendation system will try to automatically improve this template.", null));
				} else if (desiredMatches.isEmpty()) {
					ErrorDialog.openError(Display.getCurrent().getActiveShell(), 
							"No desired matches", null, 
							new Status(IStatus.INFO, EkekoSnippetsPlugin.PLUGIN_ID, 
									"Please add one or more desired matches. These are required as the recommmendation system aims to find a template that produces these desired matches.", null));
				} else {
					onEvolve();
					toolitemEvolve.setEnabled(false); // TODO For now, cannot restart the genetic algorithm yet
				}
			}
		});
		toolitemEvolve.setImage(EkekoSnippetsPlugin.IMG_RECOMMENDATION);
		toolitemEvolve.setToolTipText("Suggest suitable modifications to the input template");

		linkStatus = new Link(parent, SWT.NONE);
		linkStatus.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.text.equals("input template")) {
					onEditLink();
				} else {
					onRevealLinkedEditor();
				}
			}});

		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// **** Top component of the SashForm: List of desired matches
		matchesViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		matchesViewerTable = matchesViewer.getTable();
		matchesViewerTable.setLinesVisible(true);
		matchesViewerTable.setHeaderVisible(true);


		matchesViewer.setContentProvider(new ArrayContentProvider());	
		matchesViewer.setInput(desiredMatches);

		matchesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
			}
		});
		
		// **** Bottom component of the sash form: Results of genetic algorithm
		
		TabFolder tabs = new TabFolder(sash, SWT.BOTTOM);
		
		// *** Sash: Results table + best template
		TabItem resultsTab = new TabItem(tabs, SWT.NULL);
		resultsTab.setText("Results");
		SashForm resultSash = new SashForm(tabs, SWT.HORIZONTAL);
		resultSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		resultsTab.setControl(resultSash);

		resultsTableViewer = new TableViewer(resultSash, SWT.BORDER | SWT.FULL_SELECTION);
		Table resultsTable = resultsTableViewer.getTable();
		resultsTable.setLinesVisible(true);
		resultsTable.setHeaderVisible(true);

		resultsTableViewer.setContentProvider(new ArrayContentProvider());
		resultsTableViewer.setInput(evolveResults);

		TableViewerColumn[] cols = new TableViewerColumn[4];
		cols[0] = addColumn(resultsTableViewer, 0, "Gen", 35);
		cols[1] = addColumn(resultsTableViewer, 1, "Best Fitness", 100);
		cols[2] = addColumn(resultsTableViewer, 2, "Best F1", 100);
		cols[3] = addColumn(resultsTableViewer, 3, "Best Partial", 100);
		for (int i=0; i< cols.length; i++) {
			final int idx = i;
			cols[i].setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					return ekekoLabelProvider.getText(nth((Collection) element, idx));
				}
			});
		}
		
		resultsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				openPopulationInspector((IStructuredSelection)event.getSelection());
			}
		});
		
		resultsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				int i = evolveResults.indexOf(selection.getFirstElement());
				updateBestTemplate(bestTemplatePerGen.get(i));
			}
		});
		
		// ** Best template
		bestTemplateTextArea = new TextViewer(resultSash, SWT.BORDER);
		bestTemplateTextArea.setEditable(false);
		bestTemplateTextArea.getTextWidget().setFont(EkekoSnippetsPlugin.getEditorFont());
		
		// *** Fitness chart
		        
        TabItem fitnessTab = new TabItem(tabs, SWT.NULL);
        fitnessTab.setText("Fitness chart");
        
        canvasView = new ChartCanvas(tabs, SWT.NO_BACKGROUND);
		//canvasView.setChart(chart);		
		fitnessTab.setControl(canvasView);	
		
		addActiveColumnListener(matchesViewerTable);
		addMenu(matchesViewer);
		ekekoLabelProvider = new EkekoLabelProvider();
		linkToEditor(null);
		updateLinkWidget();
	}
	
	/**
	 * To be called by the evolve function (in search.clj) on each generation
	 */
	public void onNewGeneration(Integer generation, Double bestFitness, Double bestF1, Double bestPartial, 
			Object bestTemplate, String outputDir) {
		// This method will be called from a separate thread.
		// We need to make sure all GUI changes happen on the SWT event thread using asyncExec.
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	RecommendationEditor.this.outputDir = outputDir;
		    	addToChart(generation, bestF1, bestPartial);
		    	addToTable(generation, bestFitness, bestF1, bestPartial);
		    	
		    	bestTemplatePerGen.add(bestTemplate);
		    	updateBestTemplate(bestTemplate);
		    }
		});
	}
	
	private void updateBestTemplate(Object clojureTemplateGroup) {
		TemplateGroup templateGroup = TemplateGroup.newFromClojureGroup(clojureTemplateGroup);
		
		TemplatePrettyPrinter pp = new TemplatePrettyPrinter(templateGroup);
		String printed = pp.prettyPrint();
		
		bestTemplateTextArea.getTextWidget().setText(printed);
		for(StyleRange range : pp.getStyleRanges())
			bestTemplateTextArea.getTextWidget().setStyleRange(range);
	}
	
	private void addToTable(Integer generation, Double bestFitness, Double bestF1, Double bestPartial) {
		ArrayList<Object> newRow = new ArrayList<>();
		newRow.add(generation);
		newRow.add(bestFitness);
		newRow.add(bestF1);
		newRow.add(bestPartial);
		
		evolveResults.add(newRow);
		resultsTableViewer.setInput(evolveResults);
	}
	
	private Chart createChart() {
		ChartWithAxes chart = ChartWithAxesImpl.create();
		chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);
		chart.getPlot().setBackground(ColorDefinitionImpl.WHITE());
		chart.getLegend().setItemType(LegendItemType.SERIES_LITERAL);
		chart.getLegend().setVisible(true);
		chart.getTitle().setVisible(false);

		Axis xAxis = ((ChartWithAxes) chart).getPrimaryBaseAxes()[0];
        xAxis.getTitle().setVisible(true);
        xAxis.getTitle().getCaption().setValue("Generation");
        
        Axis yAxis = ((ChartWithAxes) chart).getPrimaryOrthogonalAxis(xAxis);
        yAxis.getTitle().setVisible(false);
        yAxis.getScale().setStep(0.2);
        yAxis.getScale().setMin(NumberDataElementImpl.create(0.0));
        yAxis.getScale().setMax(NumberDataElementImpl.create(1.0));

        NumberDataSet xValues = NumberDataSetImpl.create(new Integer[]{});
        generationAxis = SeriesImpl.create();
        generationAxis.setDataSet(xValues);
        SeriesDefinition sdX = SeriesDefinitionImpl.create();
        sdX.getSeriesPalette().update(1);
        xAxis.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(generationAxis);
        
        NumberDataSet y1DataSet = NumberDataSetImpl.create(new Double[]{});
        f1Data = (LineSeries) LineSeriesImpl.create();
        f1Data.setDataSet(y1DataSet);
        
        NumberDataSet y2DataSet = NumberDataSetImpl.create(new Double[]{});
        partialData = (LineSeries) LineSeriesImpl.create();
        partialData.setDataSet(y2DataSet);
        
        SeriesDefinition sdY = SeriesDefinitionImpl.create();
        yAxis.getSeriesDefinitions().add(sdY);
        f1Data.setSeriesIdentifier("F1 score");
        partialData.setSeriesIdentifier("Partial score");
        sdY.getSeries().add(f1Data);
        sdY.getSeries().add(partialData);
        
        return chart;
		
	}
	
	private void addToChart(Integer generation, Double bestFitness, Double bestPartial) {
		Chart chart = canvasView.getChart();
		if (chart==null) {
			chart=createChart();
			canvasView.setChart(chart);
		}
		
		NumberDataSetImpl xData = (NumberDataSetImpl)generationAxis.getDataSet();
		Integer[] rawVals = (Integer[])(xData.getValues());
		List<Integer> vals = new ArrayList<Integer>(Arrays.asList(rawVals));
		vals.add(generation);
		generationAxis.setDataSet(NumberDataSetImpl.create(vals.toArray(new Integer[0])));
		
		NumberDataSetImpl y1Data = (NumberDataSetImpl)f1Data.getDataSet();
		Double[] rawY1Vals = (Double[])(y1Data.getValues());
		List<Double> y1vals = new ArrayList<Double>(Arrays.asList(rawY1Vals));
		y1vals.add(bestFitness);
		f1Data.setDataSet(NumberDataSetImpl.create(y1vals.toArray(new Double[0])));

		NumberDataSetImpl y2Data = (NumberDataSetImpl)partialData.getDataSet();
		Double[] rawY2Vals = (Double[])(y2Data.getValues());
		List<Double> y2vals = new ArrayList<Double>(Arrays.asList(rawY2Vals));
		y2vals.add(bestPartial);
		partialData.setDataSet(NumberDataSetImpl.create(y2vals.toArray(new Double[0])));

		canvasView.redraw();
	}

	protected void onDeleteFromDesiredMatches() {
		ISelection selection = matchesViewer.getSelection();
		IStructuredSelection sel = (IStructuredSelection) selection;
		
		for (Object elem:sel.toArray()) {
			desiredMatches.remove(elem);
		}
		
		matchesViewer.refresh();
	}

	protected void onEvolve() {
		evolve(inputTemplateEditor.getGroup().getGroup(), desiredMatches, this, evolveConfig);
	}
	
	protected void onEditConfig() {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Edit configuration", "Recommendation configuration (See the documentation in search.clj)", evolveConfig, null) {
			protected int getInputTextStyle() {
				return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
			}

			protected Control createDialogArea(Composite parent) {
				Control res = super.createDialogArea(parent);
				((GridData) this.getText().getLayoutData()).heightHint = 160;
				return res;
			}
		};
		int open = dlg.open();
		if(open == dlg.OK) {
			evolveConfig = dlg.getValue();
		}
	}

	protected void onAddMatchesFromTemplate() {
		// First pick a template or transformation
		ListDialog listSelectionDialog = new ListDialog(getSite().getShell());
		listSelectionDialog.setContentProvider(new ArrayContentProvider());
		listSelectionDialog.setLabelProvider(new WorkbenchPartLabelProvider());
		listSelectionDialog.setInput(getTemplateEditors(true).toArray());
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
				}
				if(ed instanceof TemplateEditor) {
					linkedTemplateEditor = (TemplateEditor) ed;
				}
			}
		}
		
		// Create the columns for the table, if needed
//		for (TableColumn tableColumn : matchesViewerTable.getColumns()) {
//			tableColumn.dispose();
//		}
		
		if (desiredMatches.isEmpty()) { 
			for (Object object : getResultVariables()) {
				String varname = (String) object;
				final int columnIndex = matchesViewerTable.getColumnCount();
				TableViewerColumn column = addColumn(matchesViewer, columnIndex, varname, 200);

				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						return ekekoLabelProvider.getText(nth((Collection) element, columnIndex));
					}
				});
			}
		} else if (matchesViewerTable.getColumns().length != getResultVariables().size()) {
			ErrorDialog.openError(Display.getCurrent().getActiveShell(), 
					"Incompatible template group", null, 
					new Status(IStatus.WARNING, EkekoSnippetsPlugin.PLUGIN_ID, 
							"Cannot use the matches from this template group, as its number of templates does not correspond.", null));
			return;
		}
		
		// Add the matches to the table
		matchesViewerTable.layout(true);
		desiredMatches.addAll(getResults());
//		desiredMatches = new HashSet(getResults());
		matchesViewer.setInput(desiredMatches);
		
	}

	protected void onRevealLinkedEditor() {
		if(inputTemplateEditor != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(inputTemplateEditor);
		}
	}

	private List<IEditorPart> getTemplateEditors(boolean includeTransformations) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		LinkedList<IEditorPart> editors = new LinkedList<>();
		for(IEditorReference ref : editorReferences) {
			IEditorPart editor = ref.getEditor(true);
			if(editor instanceof TemplateEditor) {
				editors.add(editor);
			}
			if(includeTransformations && editor instanceof TransformationEditor) {
				editors.add(editor);
			}
		}
		return editors;
	}

	protected void onEditLink() {
		ListDialog listSelectionDialog = new ListDialog(getSite().getShell());
		listSelectionDialog.setContentProvider(new ArrayContentProvider());
		listSelectionDialog.setLabelProvider(new WorkbenchPartLabelProvider());
		listSelectionDialog.setInput(getTemplateEditors(false).toArray());
		listSelectionDialog.setTitle("Select Input Template");
		listSelectionDialog.setMessage("Select the Ekeko/X template for which recommendations are needed.");
		int open = listSelectionDialog.open();
		if(open == listSelectionDialog.OK) {
			Object[] result = listSelectionDialog.getResult();
			if(result.length == 1) {
				inputTemplateEditor = (TemplateEditor) result[0];
				updateLinkWidget();
			}
		}
	}

	private void updateLinkWidget() {
		if(inputTemplateEditor == null) {
			linkStatus.setText("No <a>input template</a> selected.");
		} else {
			linkStatus.setText("Input template: <a>" + inputTemplateEditor.getEditorInput().getName() + "</a>");
		}
		linkStatus.pack();
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
		}
		if(editor instanceof TemplateEditor) {
			linkedTemplateEditor = (TemplateEditor) editor;
		}
		if(editor == null) {
			linkedTemplateEditor = null;
		}
		updateLinkWidget();
	}

	private void addMenu(final TableViewer tableViewer) {
		final Table table = tableViewer.getTable();
		final MenuManager mgr = new MenuManager();

		final Action revealNode = new Action("Reveal In Editor") {
			public void run() {
				revealNode(tableViewer, activeColumn);
			}
		};



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

	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(width);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		return column;
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

	private void openPopulationInspector(IStructuredSelection selection) {
		try {
			int i = evolveResults.indexOf(selection.getFirstElement());
			
			PopulationInspectorDialog pi = new PopulationInspectorDialog(
					getSite().getShell(), outputDir, i);
			pi.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
