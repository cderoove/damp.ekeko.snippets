package damp.ekeko.snippets.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


public class TemplateGroupNodeSelectionDialog extends Dialog
{

	private TemplateGroupViewer templateGroupViewer;
	
	private Object cljGroup, cljTemplate, cljNode;
	
	public TemplateGroupNodeSelectionDialog(Shell parentShell, Object cljGroup, Object cljTemplate, Object cljNode) {
		super(parentShell);
		this.cljGroup = cljGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;
	}
	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select a node from the template.");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		 Composite composite = (Composite) super.createDialogArea(parent);
		 composite.setLayout(new GridLayout(1, false));

		 templateGroupViewer = new TemplateGroupViewer(composite, SWT.NONE);
		 GridLayout gridLayout = (GridLayout) templateGroupViewer.getLayout();
		 gridLayout.verticalSpacing = 0;
		 gridLayout.marginWidth = 0;
		 gridLayout.marginHeight = 0;
		 gridLayout.horizontalSpacing = 0;
		 templateGroupViewer.addNodeSelectionListener(new TemplateGroupViewerNodeSelectionListener() {
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				TemplateGroupNodeSelectionDialog.this.cljGroup = event.getSelectedTemplateGroup();
				TemplateGroupNodeSelectionDialog.this.cljTemplate = event.getSelectedTemplate();
				TemplateGroupNodeSelectionDialog.this.cljNode = event.getSelectedTemplateNode();
			}
		});
		templateGroupViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		templateGroupViewer.setInput(cljGroup, cljTemplate, cljNode);
		
		return composite;	
	}


	
	

}
