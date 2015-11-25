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
 * Dialog to inspect the history of a particular individual; lists all individuals it is based on
 * @author Tim
 */
public class MutationHistoryDialog extends Dialog {

	private String dataPath;
	private TextViewer templateTextArea;
	private TableViewer populationViewer;
	private EkekoLabelProvider ekekoLabelProvider;
//	private List<List<String>> rawData = new ArrayList<List<String>>();
	private List<List<String>> data = new ArrayList<List<String>>();

	private int generation; // The individual belongs to this generation
	private int index; // Index of the individual in the generation

	public MutationHistoryDialog(Shell parentShell, String dataPath, int gen, int index, String id) {
		super(parentShell);
		this.generation = gen;
		this.dataPath = dataPath;
		this.index = index;
		
		// Gather info on the selected individual and all of its prior individuals
		// Columns: ["Id" "Original" "Fitness" "F1" "Partial" "Operator" "Subject" "Operands"]
		List<String> indiv = fetchIndividualFromCsv(dataPath + generation + "/population.csv", id);
		
		String original = indiv.get(1);
		indiv.remove(0);
		indiv.remove(0);
		indiv.add(0, new Integer(generation).toString());
		data.add(indiv);
		int curGen = generation - 1;
		System.out.println("" + curGen);
		while (curGen >= 0) {
			indiv = fetchIndividualFromCsv(dataPath + curGen + "/population.csv", original);
			original = indiv.get(1);
			indiv.remove(0);
			indiv.remove(0);
			indiv.add(0, new Integer(curGen).toString());
			data.add(0,indiv);
			
			curGen = curGen - 1;
		}
	}

	public List<String> fetchIndividualFromCsv(String csvPath, String id) {		
		try {
			FileInputStream fis = new FileInputStream(new File(csvPath));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String line = br.readLine(); // Skip the header line
			int i=0;
			
			while ((line = br.readLine()) != null) {
				List<String> split = Arrays.asList(line.split(";"));
				System.out.println(id);
				System.out.println("$$" + split.get(0));
				if (split.get(0).equals(id)) {
					System.out.println("!!");
					br.close();
					List<String> copy = new ArrayList<String>(split); // Need to make a copy because split is immutable..
					copy.add(new Integer(i).toString()); // Attach the index at the end..
					return copy;
				}
				i++;
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();;
		}
		return null;
	}	

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1,false));

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
		cols[0] = addColumn(populationViewer, 0, "Gen", 35);
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

		// *** Template text area
		templateTextArea = new TextViewer(sash, SWT.BORDER);
		templateTextArea.setEditable(false);
		templateTextArea.getTextWidget().setFont(EkekoSnippetsPlugin.getEditorFont());

		populationViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				int i = data.indexOf(selection.getFirstElement());
				onSelectIndividual(i, Integer.parseInt(data.get(i).get(7)));
			}
		});
		populationViewer.setSelection(new StructuredSelection(populationViewer.getElementAt(0)),true);

		sash.setWeights(new int[]{3,2});
		sash.forceFocus();
		return container;
	}

	protected void onSelectIndividual(int gen, int i) {
		Object clojureTemplateGroup = TemplateEditorInput.deserializeClojureTemplateGroup(dataPath + gen + "/individual-" + i + ".ekt");
		TemplateGroup templateGroup = TemplateGroup.newFromClojureGroup(clojureTemplateGroup);

		TemplatePrettyPrinter pp = new TemplatePrettyPrinter(templateGroup);

		templateTextArea.getTextWidget().setText(pp.prettyPrint());
		for(StyleRange range : pp.getStyleRanges())
			templateTextArea.getTextWidget().setStyleRange(range);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Mutation history (generation " + generation + ", index " + index + ")");
	}

	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(width);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		return column;
	}
} 