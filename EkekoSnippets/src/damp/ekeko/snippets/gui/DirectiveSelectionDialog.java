package damp.ekeko.snippets.gui;

import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import clojure.lang.IFn;

public class DirectiveSelectionDialog extends Dialog {

	
	private TableViewer directivesViewer;
	private Object selectedDirective;

	protected DirectiveSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select the desired directive.");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		directivesViewer = new TableViewer(composite, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		directivesViewer.getControl().setLayoutData(layoutData);
		directivesViewer.getTable().setHeaderVisible(true);
		directivesViewer.addSelectionChangedListener(new ISelectionChangedListener() {	

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedDirective = selection.getFirstElement();
				Button okButton = getButton(IDialogConstants.OK_ID);
				if(selectedDirective != null) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		});
		
		TableViewerColumn directiveNameCol = new TableViewerColumn(directivesViewer, SWT.NONE);
		directiveNameCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getDirectiveName(element);
			}
		});
		TableColumn directiveNameColCol = directiveNameCol.getColumn();
		directiveNameColCol.setText("Directive");
		directiveNameColCol.setWidth(100);

		TableViewerColumn directiveArityCol = new TableViewerColumn(directivesViewer, SWT.NONE);
		directiveArityCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getDirectiveArity(element);
			}
		});
		TableColumn directiveArityColCol = directiveArityCol.getColumn();
		directiveArityColCol.setText("Arity");
		directiveArityColCol.setWidth(50);

		
		TableViewerColumn directiveDescriptionCol = new TableViewerColumn(directivesViewer, SWT.NONE);
		directiveDescriptionCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getDirectiveDescription(element);
			}
		});
		TableColumn directiveDescriptionColCol = directiveDescriptionCol.getColumn();
		directiveDescriptionColCol.setText("Description");
		directiveDescriptionColCol.setWidth(200);

		
		directivesViewer.setContentProvider(new ArrayContentProvider());
		directivesViewer.setInput(getRegisteredDirectives().toArray());
		
		return composite;
	}

	public static IFn FN_REGISTERED_DIRECTIVES;
	public static IFn FN_DIRECTIVE_NAME;
	public static IFn FN_DIRECTIVE_DESCRIPTION;
	public static IFn FN_DIRECTIVE_ARITY;

	
	public static Collection getRegisteredDirectives() {
		return (Collection) FN_REGISTERED_DIRECTIVES.invoke();
	}
	
	public static String getDirectiveName(Object directive) {
		return (String) FN_DIRECTIVE_NAME.invoke(directive);
	}

	public static String getDirectiveDescription(Object directive) {
		return (String) FN_DIRECTIVE_DESCRIPTION.invoke(directive);
	}
	
	public static String getDirectiveArity(Object directive) {
		return "" + FN_DIRECTIVE_ARITY.invoke(directive);
	}
	
	public Object getSelectedDirective() {
		return selectedDirective;
	}
	
	
	
	
}