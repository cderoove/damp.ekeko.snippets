package damp.ekeko.snippets.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import damp.ekeko.snippets.data.TemplateGroup;


public class BoundDirectivesEditorDialog extends Dialog
{

	private BoundDirectivesViewer boundDirectivesViewer;

	private Object cljTemplate, cljNode;
	
	private TemplateGroup templateGroup;

	public BoundDirectivesEditorDialog(Shell parentShell, TemplateGroup templateGroup, Object cljTemplate, Object cljNode) {
		super(parentShell);
		this.templateGroup = templateGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	};


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit directives for template element.");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));

		boundDirectivesViewer = new BoundDirectivesViewer(composite, SWT.NONE);
		boundDirectivesViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		boundDirectivesViewer.setInput(templateGroup, cljTemplate, cljNode);

		return composite;	
	}





}
