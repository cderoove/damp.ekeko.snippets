package damp.ekeko.snippets.gui;

import java.util.LinkedList;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class TableDecorator {
	private Table table;
	private LinkedList<TableEditor> editors;
	
	public TableDecorator(Table table) {
		this.table = table;
		editors = new LinkedList<TableEditor>();
	}
	
	private TableEditor createEditor() {
		TableEditor editor = new TableEditor(table);
		editors.add(editor);
		return editor;
	}
	
	public void removeAllEditors() {
		for (int i=0; i<editors.size(); i++) {
	    	Control control = editors.get(i).getEditor();
	    	if (control != null) control.dispose();
		}
	}
	
	public void setButtonEditorAtNewRow() {
		TableItem item = new TableItem(table, 0);
		setButtonEditor(2, table.getColumnCount() - 1, table.getItemCount() - 1, table.getItemCount() - 1);
	}

	public void setButtonEditor(int col) {
		setButtonEditor(col, col, 0, table.getItemCount() - 1);
	}
	
	public void setButtonEditor(int startCol, int endCol, int startRow, int endRow) {
		for (int col = startCol; col <= endCol; col++) {
		    for (int row = startRow; row <= endRow; row++) {
		    	TableEditor editor = createEditor();
		    	Button button = new Button(table, SWT.PUSH);
	
		    	button.setText("+");
		    	button.setSize(16, 16);
	
		    	editor.horizontalAlignment = SWT.RIGHT;
		    	editor.minimumHeight = button.getSize().y;
		    	editor.minimumWidth = button.getSize().x;
	
		    	editor.setEditor(button, table.getItem(row), col);
	
		    	final int index = col;
		    	final TableItem item = table.getItem(row);
		    	button.addSelectionListener(new SelectionAdapter() {
		    		public void widgetSelected(SelectionEvent event) {
		    			item.setText(index, getSelectedTextFromActiveEditor());
		    		}
		    	});
		    }
	    }
	}
    
	public void setTextEditor(final int textColumn) {
    	final TableEditor editor = createEditor();
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		table.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				Control old = editor.getEditor();
				if (old != null) old.dispose();

				Point pt = new Point(event.x, event.y);

				final TableItem item = table.getItem(pt);
				if (item != null) {
					int column = -1;
					for (int i = 0, n = table.getColumnCount(); i < n; i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							column = i;
							break;
						}
					}

					if (column == textColumn) {
						final Text text = new Text(table, SWT.NONE);
						text.setText(item.getText(column));
						text.setForeground(item.getForeground());
						text.selectAll();
						text.setFocus();

						editor.minimumWidth = text.getBounds().width;

						editor.setEditor(text, item, column);

						final int col = column;
						text.addModifyListener(new ModifyListener() {
							public void modifyText(ModifyEvent event) {
								item.setText(col, text.getText());
							}
						});
					}
				}
			}
		});
	}

	static String getSelectedTextFromActiveEditor() {
		ITextEditor editor =  (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();	
		return selection.getText();
	}
		 
}

