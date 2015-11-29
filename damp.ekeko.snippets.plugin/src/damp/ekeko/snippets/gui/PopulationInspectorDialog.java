package damp.ekeko.snippets.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import damp.ekeko.gui.EkekoLabelProvider;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

/**
 * Dialog to browse the individuals of a particular population
 * @author Tim
 */
public class PopulationInspectorDialog extends Dialog {

	private String dataPath;
	private TextViewer templateTextArea;
	private TableViewer populationViewer;
	private EkekoLabelProvider ekekoLabelProvider;
	private List<List<String>> rawData = new ArrayList<List<String>>();
	private List<List<String>> data = new ArrayList<List<String>>();
	private int generation;

	public PopulationInspectorDialog(Shell parentShell, String dataPath, int gen) {
		super(parentShell);
		generation = gen;
		this.dataPath = dataPath;

		// Parse the population.csv file		
		// Columns: ["Id" "Original" "Fitness" "F1" "Partial" "Operator" "Subject" "Operands"]
		try {
			FileInputStream fis = new FileInputStream(new File(dataPath + generation + "/population.csv"));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String line = br.readLine(); // Skip the header line
			int i = 0;
			while ((line = br.readLine()) != null) {
				List<String> split = Arrays.asList(line.split(";"));
				rawData.add(split);

				List<String> filtered = new ArrayList<String>(split);
				filtered.remove(0); // Remove Id and Original columns
				filtered.remove(0);
				filtered.add(0, new Integer(i).toString());
				data.add(filtered);

				i++;
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1,false));

		// **** Toolbar
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));

		ToolItem toolitemOpenInEditor = new ToolItem(toolBar, SWT.NONE);
		toolitemOpenInEditor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)populationViewer.getSelection();
				int i = data.indexOf(selection.getFirstElement());

				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				try {
					IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
					TemplateEditorInput templateEditorInput = new TemplateEditorInput();
					templateEditorInput.setPathToPersistentFile(dataPath + generation + "/individual-" + i + ".ekt");
					IEditorPart openedEditor = window.getActivePage().openEditor(templateEditorInput, TemplateEditor.ID);
					TemplateEditor templateEditor = (TemplateEditor) openedEditor;
					templateEditor.setPreviouslyActiveEditor(activeEditor);
				} catch (PartInitException ex) {
					ex.printStackTrace();
				}
			}

		});
		toolitemOpenInEditor.setImage(EkekoSnippetsPlugin.IMG_TEMPLATE);
		toolitemOpenInEditor.setToolTipText("Open the selected individual in a template editor");

		ToolItem toolitemOpenHistory = new ToolItem(toolBar, SWT.NONE);
		toolitemOpenHistory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openMutationHistory((IStructuredSelection)populationViewer.getSelection());
			}
		});
		toolitemOpenHistory.setImage(EkekoSnippetsPlugin.IMG_HISTORY);
		toolitemOpenHistory.setToolTipText("Open the mutation history of the selected individual");

		// **** Sash with population table + template view
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));

		// *** Population table
		ekekoLabelProvider = new EkekoLabelProvider();
		populationViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		Table populationTable = populationViewer.getTable();
		populationTable.setLinesVisible(true);
		populationTable.setHeaderVisible(true);

		TableViewerColumn[] cols = new TableViewerColumn[7];
		cols[0] = addColumn(populationViewer, 0, "Idx", 35);
		cols[1] = addColumn(populationViewer, 1, "Fitness", 80);
		cols[2] = addColumn(populationViewer, 2, "F1", 80);
		cols[3] = addColumn(populationViewer, 3, "Partial", 100);
		cols[4] = addColumn(populationViewer, 4, "Operator", 160);
		cols[5] = addColumn(populationViewer, 5, "Subject", 160);
		cols[6] = addColumn(populationViewer, 6, "Operands", 160);
		for (int i=0; i< cols.length; i++) {
			final int idx = i;
			cols[i].setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					return ekekoLabelProvider.getText(((List<String>)element).get(idx));
				}
			});
		}

		populationViewer.setContentProvider(new ArrayContentProvider());
		populationViewer.setInput(data);
		
		populationViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				openMutationHistory((IStructuredSelection)event.getSelection());
			}
		});

		// *** Template text area
		templateTextArea = new TextViewer(sash, SWT.BORDER);
		templateTextArea.setEditable(false);
		templateTextArea.getTextWidget().setFont(EkekoSnippetsPlugin.getEditorFont());

		populationViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				int i = data.indexOf(selection.getFirstElement());
				onSelectIndividual(i);
			}
		});
		populationViewer.setSelection(new StructuredSelection(populationViewer.getElementAt(0)),true);

		sash.setWeights(new int[]{3,2});
		sash.forceFocus();
		return container;
	}

	protected void onSelectIndividual(int i) {
		Object clojureTemplateGroup = TemplateEditorInput.deserializeClojureTemplateGroup(dataPath + generation + "/individual-" + i + ".ekt");
		TemplateGroup templateGroup = TemplateGroup.newFromClojureGroup(clojureTemplateGroup);

		TemplatePrettyPrinter pp = new TemplatePrettyPrinter(templateGroup);

		templateTextArea.getTextWidget().setText(pp.prettyPrint());
		for(StyleRange range : pp.getStyleRanges())
			templateTextArea.getTextWidget().setStyleRange(range);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Population inspector (generation " + generation + ")");
	}

	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(width);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		return column;
	}

	private void openMutationHistory(IStructuredSelection selection) {
		int i = data.indexOf(selection.getFirstElement());
		
		MutationHistoryDialog mh = new MutationHistoryDialog(
				PopulationInspectorDialog.this.getShell(), dataPath, generation,i, rawData.get(i).get(0));
		mh.open();
	}
} 