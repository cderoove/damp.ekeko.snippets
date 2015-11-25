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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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

import damp.ekeko.gui.EkekoLabelProvider;

public class PopulationInspectorDialog extends Dialog {

	private Text templateTextArea;
	private TableViewer populationViewer;
	private EkekoLabelProvider ekekoLabelProvider;
	private List<List<String>> data = new ArrayList<List<String>>();;
	
	public PopulationInspectorDialog(Shell parentShell, String generationPath) {
		super(parentShell);
		
		// Parse the population.csv file		
		try {
		FileInputStream fis = new FileInputStream(new File(generationPath + "population.csv"));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		String line = br.readLine(); // Skip the header line
		while ((line = br.readLine()) != null) {
			data.add(Arrays.asList(line.split(";")));
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

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		// **** Population table
		ekekoLabelProvider = new EkekoLabelProvider();
		populationViewer = new TableViewer(sash, SWT.BORDER | SWT.FULL_SELECTION);
		Table populationTable = populationViewer.getTable();
		populationTable.setLinesVisible(true);
		populationTable.setHeaderVisible(true);
		
		TableViewerColumn[] cols = new TableViewerColumn[7];
		cols[0] = addColumn(populationViewer, 0, "Idx", 35);
		cols[1] = addColumn(populationViewer, 1, "Fitness", 100);
		cols[2] = addColumn(populationViewer, 2, "F1", 100);
		cols[3] = addColumn(populationViewer, 3, "Partial", 100);
		cols[4] = addColumn(populationViewer, 4, "Operator", 100);
		cols[5] = addColumn(populationViewer, 5, "Subject", 100);
		cols[6] = addColumn(populationViewer, 6, "Operands", 100);
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
		
		populationViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
//				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
//				int i = evolveResults.indexOf(selection.getFirstElement());
//				templateTextArea.setText(bestTemplatePerGen.get(i));
			}
		});
		
		// **** Template text area
		templateTextArea = new org.eclipse.swt.widgets.Text(sash, SWT.BORDER);
		templateTextArea.setText("...");
		templateTextArea.setEditable(false);
		

		return container;
	}
	
	public static void main(String[] args) {
		
		
		String bla = "hello;;world";
		System.out.println(bla.split(";")[0]);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Population inspector");
	}

//	@Override
//	protected Point getInitialSize() {
//		return new Point(640, 480);
//	}
	
	protected TableViewerColumn addColumn(TableViewer viewer, final int columnIndex, String attributeName, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE, columnIndex);
		column.getColumn().setWidth(width);
		column.getColumn().setText(attributeName);
		column.getColumn().setMoveable(true);
		return column;
	}
} 