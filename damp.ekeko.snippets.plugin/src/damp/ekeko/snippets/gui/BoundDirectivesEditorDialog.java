package damp.ekeko.snippets.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public class BoundDirectivesEditorDialog extends Dialog
{

	private BoundDirectivesViewer boundDirectivesViewer;

	private Object cljGroup, cljTemplate, cljNode;
	
	public Object getUpdatedGroup() {
		return boundDirectivesViewer.getUpdatedGroup();
	}


	public BoundDirectivesEditorDialog(Shell parentShell, Object cljGroup, Object cljTemplate, Object cljNode) {
		super(parentShell);
		this.cljGroup = cljGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}


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
				
		/*
		 boundDirectivesViewer.addNodeSelectionListener(new TemplateGroupViewerNodeSelectionListener() {
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				BoundDirectivesEditorDialog.this.cljGroup = event.getSelectedTemplateGroup();
				BoundDirectivesEditorDialog.this.cljTemplate = event.getSelectedTemplate();
				BoundDirectivesEditorDialog.this.cljNode = event.getSelectedTemplateNode();
			}
		});
		 */

		boundDirectivesViewer.setInput(cljGroup, cljTemplate, cljNode);

		return composite;	
	}





}
