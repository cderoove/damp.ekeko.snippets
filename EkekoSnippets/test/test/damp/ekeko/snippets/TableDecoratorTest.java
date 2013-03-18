package test.damp.ekeko.snippets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import damp.ekeko.snippets.gui.TableDecorator;

public class TableDecoratorTest {
  // Number of rows and columns
  private static final int NUM = 5;

  /**
   * Runs the application
   */
  public void run() {
	System.out.println("here");
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("Text Table Editor");
    createContents(shell);
    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }

  /**
   * Creates the main window's contents
   * 
   * @param shell the main window
   */
  private void createContents(final Shell shell) {
    shell.setLayout(new FillLayout());

    // Create the table
    Table table = new Table(shell, SWT.SINGLE | SWT.FULL_SELECTION
        | SWT.HIDE_SELECTION);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);

    // Create five columns
    for (int i = 0; i < NUM; i++) {
      TableColumn column = new TableColumn(table, SWT.CENTER);
      column.setText("Column " + (i + 1));
      column.pack();
    }


    // Create five rows and the editors for those rows. The first column has the
    // color change buttons. The second column has dropdowns. The final three
    // have text fields.
    for (int i = 0; i < NUM; i++) {
      // Create the row
      TableItem item = new TableItem(table, SWT.NONE);
      item.setText(new String[] {"1", "2", "3", "4", "5"});
    }
    
    TableDecorator tableDecorator = new TableDecorator(table);
    tableDecorator.setTextEditor(3);
    tableDecorator.setButtonEditorAtNewRow();
    tableDecorator.removeAllEditors();
  }

  /**
   * The application entry point
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    new TableDecoratorTest().run();
  }
}
