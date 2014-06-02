package damp.ekeko.snippets.gui;

import java.util.LinkedList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class OperatorOperandsView extends ViewPart {

	public static final String ID = "damp.ekeko.snippets.gui.OperatorOperandsView"; //$NON-NLS-1$

	private OperatorOperandsViewer operatorOperandsViewer;

	private TemplateGroupViewerNodeSelectionListener listener;
	private LinkedList<TemplateGroupViewer> selectionProviders;
	
	@Override
	public void createPartControl(Composite parent) {
		selectionProviders = new LinkedList<TemplateGroupViewer>();
		
		parent.setLayout(new GridLayout(1, true));
		
		ToolBar snippetOperatorGroupToolbar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		snippetOperatorGroupToolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		ToolItem tltmApplyOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		tltmApplyOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onApplyOperator();
			}
		});
		//tltmApplyOperator.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/etool16/validate.gif"));
		tltmApplyOperator.setImage(EkekoSnippetsPlugin.IMG_OPERATOR_APPLY);
		tltmApplyOperator.setToolTipText("Apply operator to template element");		

		/*
		ToolItem undoOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		undoOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				undo();
			}
		});
		undoOperator.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/undo_edit.gif"));
		undoOperator.setToolTipText("Undo operator application");


		ToolItem redoOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		redoOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				redo();
			}
		});
		redoOperator.setImage(ResourceManager.getPluginImage("org.eclipse.ui", "/icons/full/etool16/redo_edit.gif"));
		redoOperator.setToolTipText("Redo operator application");
		 */


		operatorOperandsViewer = new OperatorOperandsViewer(parent, SWT.NONE);		
		GridData gd_operatorOperandsViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		//gd_operatorOperandsViewer.heightHint = 297;
		operatorOperandsViewer.setLayoutData(gd_operatorOperandsViewer);
		
		listener = new TemplateGroupViewerNodeSelectionListener() {	
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				if(!operatorOperandsViewer.isDisposed()) {
					operatorOperandsViewer.setInput(event.getSelectedTemplateGroup(), event.getSelectedTemplate(),event.getSelectedTemplateNode());	
				}
			}
		};

	}

	protected void onApplyOperator() {
		Object selectedOperator = operatorOperandsViewer.getSelectedOperator();
		Object operands = operatorOperandsViewer.getOperands();
		TemplateGroup templateGroup = operatorOperandsViewer.getTemplateGroup();
		if(operands == null || selectedOperator == null || templateGroup == null)
			return;
		try {
			templateGroup.applyOperator(selectedOperator, operands);
		} catch(IllegalArgumentException e) {
			ErrorDialog.openError(getSite().getShell(), "Could not apply operator", "An error occurred while applying the operator to the template.", new Status(IStatus.ERROR, EkekoSnippetsPlugin.PLUGIN_ID, e.getMessage(), e));

		}
		
		for(TemplateGroupViewer viewer : selectionProviders) {
			if(!viewer.isDisposed()) {
				TemplateEditor parentTemplateEditor = viewer.getParentTemplateEditor();
				if(parentTemplateEditor != null) {
					if(templateGroup.equals(parentTemplateEditor.getGroup())) {
						viewer.updateWidgets();
						parentTemplateEditor.becomeDirty();
					}

				}}
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		//ISelectionService s = getSite().getWorkbenchWindow().getSelectionService();
		//s.removeSelectionListener(myListener);
		for(TemplateGroupViewer viewer : selectionProviders) {
			viewer.removeNodeSelectionListener(listener);
		}
		super.dispose();
	}

	public void shouldRegisterAsListenerTo(TemplateGroupViewer templateGroupViewer) {
		templateGroupViewer.addNodeSelectionListener(listener);
		selectionProviders.add(templateGroupViewer);
	}

}
