package damp.ekeko.snippets.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import clojure.lang.LazySeq;
import clojure.lang.PersistentVector;
import damp.ekeko.snippets.SnippetGroup;

public class NodeViewer {
	private Table table;
	private MainView mainView;

	public NodeViewer(MainView mainView, Composite parent, int style) {
		table = new Table(parent, style);
		this.mainView = mainView;
		setActions();
	}
	
	public void clearInput() {
		table.removeAll();
		for (int y = 2 ; y < table.getColumnCount(); y++) {
			table.getColumn(y).dispose();
		}
	}
	
	public void setInput(LazySeq rows) {
		clearInput();
		
		if (rows.size() > 0) {
			PersistentVector columns = (PersistentVector) rows.get(0);
			int cols = columns.size() * 2;
		    for (int i = 2; i < cols; i++) {
		        TableColumn column = new TableColumn(table, SWT.CENTER);
		        column.setWidth(150);
		        if (i % 2 == 0)
		        	column.setText("Type");
		        else
		        	column.setText("Node");
		    }
		    table.setHeaderVisible(true);
		}
		
	    for (int i = 0; i < rows.size(); i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			PersistentVector columns = (PersistentVector) rows.get(i);
			String[] str = new String[columns.size() * 2];

			int k = 0;
			for (int j = 0; j < columns.size(); j++) {
				str[k++] = SnippetGroup.getTypeValue(columns.get(j));
				str[k++] = mainView.getSnippetGroup().getObjectValue(columns.get(j)).toString();
			}
			item.setText(str);
			item.setData(columns);
		}	
	}

	public void setActions() {
	    table.setBounds(0, 0, 100, 100);
	    table.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event e) {
	        mainView.onNodeSelection(getSelected());
	      }
	    });

        TableColumn column = new TableColumn(table, SWT.CENTER);
        column.setWidth(150);
       	column.setText("Type");

       	column = new TableColumn(table, SWT.CENTER);
        column.setWidth(150);
       	column.setText("Node");
	}

	public PersistentVector getSelected() {
        TableItem[] selection = table.getSelection();
        return (PersistentVector) selection[0].getData();
	}

}
