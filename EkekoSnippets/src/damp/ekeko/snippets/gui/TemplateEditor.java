package damp.ekeko.snippets.gui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateEditor extends EditorPart {

	public static final String ID = "damp.ekeko.snippets.gui.TemplateEditor"; //$NON-NLS-1$
	//private String viewID;

	private List<Action> actions; 	
	private TemplateGroup templateGroup;
	private TemplateGroupViewer templateGroupViewer;
	private TemplateTreeContentProvider contentProvider;
	//private OperatorOperandsViewer operatorOperandsViewer;
	
	//private BoundDirectivesViewer boundDirectivesViewer;
	private String lastSelectedWorkspaceTextString;
	protected Action matchTemplateAction;
	protected Action inspectQueryAction;

	

	public TemplateEditor() {
		templateGroup = TemplateGroup.newFromGroupName("Anonymous Template Group");		
	}
	
	public void setGroup(TemplateGroup group) {
		templateGroup = group;
		refreshWidgets();
	}

	public TemplateGroup getGroup() {
		return templateGroup;
	}
	
	
	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
				
		parent.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setOrientation(SWT.RIGHT_TO_LEFT);		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		ToolItem tltmAdd = new ToolItem(toolBar, SWT.NONE);
		tltmAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSnippet();
			}
		});
		
		tltmAdd.setImage(EkekoSnippetsPlugin.IMG_ADD);
		tltmAdd.setToolTipText("Add template");
				
		
		final ToolItem tltmRemove = new ToolItem(toolBar, SWT.NONE);
		tltmRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSnippet();
			}
		});
		tltmRemove.setImage(EkekoSnippetsPlugin.IMG_DELETE);
		tltmRemove.setDisabledImage(EkekoSnippetsPlugin.IMG_DELETE_DISABLED);
		tltmRemove.setToolTipText("Delete template");
		tltmRemove.setEnabled(false);


		final ToolItem tltmEditBoundDirectives = new ToolItem(toolBar, SWT.NONE);
		tltmEditBoundDirectives.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEditBoundDirectives(templateGroup, templateGroupViewer.getSelectedSnippet(), templateGroupViewer.getSelectedSnippetNode());
			}
		});
		//tltmEditBoundDirectives.setImage(EkekoSnippetsPlugin.IMG_EDIT_TEMPLATE);
		tltmEditBoundDirectives.setToolTipText("Edit directives of template element");
		tltmEditBoundDirectives.setEnabled(false);
			
		
		templateGroupViewer = new TemplateGroupViewer(parent, SWT.NONE);		GridData gd_templateGroupViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);		gd_templateGroupViewer.heightHint = 400;		templateGroupViewer.setLayoutData(gd_templateGroupViewer);
		templateGroupViewer.setInput(templateGroup, null, null);

		templateGroupViewer.addNodeSelectionListener(new TemplateGroupViewerNodeSelectionListener() {	
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				if(event.getSelectedTemplateNode() == null) {
					tltmRemove.setEnabled(false);
					tltmEditBoundDirectives.setEnabled(false);
				} else {
					tltmEditBoundDirectives.setEnabled(true);
					if(event.getSelectedTemplateNode().equals(TemplateGroup.getRootOfSnippet(event.getSelectedTemplate())))
						tltmRemove.setEnabled(true);
					else
						tltmRemove.setEnabled(false);
				}
			}
		});
					
		
		/*

		Group snippetOperatorGroup = new Group(container, SWT.NONE);
		snippetOperatorGroup.setLayout(new GridLayout(1, false));
		
		ToolBar snippetOperatorGroupToolbar = new ToolBar(snippetOperatorGroup, SWT.FLAT | SWT.RIGHT);
		snippetOperatorGroupToolbar.setOrientation(SWT.RIGHT_TO_LEFT);
		snippetOperatorGroupToolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Label lblOperator = new Label(snippetOperatorGroup, SWT.NONE);
		GridData gd_lblOperator = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblOperator.heightHint = 22;
		lblOperator.setLayoutData(gd_lblOperator);
		lblOperator.setText("");
		
		*/
		
		/*
		ToolItem tltmApplyOperator = new ToolItem(snippetOperatorGroupToolbar, SWT.NONE);
		tltmApplyOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onApplyOperator();
			}
		});
		tltmApplyOperator.setImage(ResourceManager.getPluginImage("org.eclipse.pde.ui", "/icons/etool16/validate.gif"));
		tltmApplyOperator.setToolTipText("Apply Operator");
		*/
		
		
		
		
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

		
		/*
		operatorOperandsViewer = new OperatorOperandsViewer(snippetOperatorGroup, SWT.NONE);				GridData gd_operatorOperandsViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);		gd_operatorOperandsViewer.heightHint = 297;		operatorOperandsViewer.setLayoutData(gd_operatorOperandsViewer);
		*/
		
		/*
		boundDirectivesViewer = new BoundDirectivesViewer(snippetOperatorGroup, SWT.NONE);
		GridData gd_boundDirectivesViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_boundDirectivesViewer.heightHint = 297;
		boundDirectivesViewer.setLayoutData(gd_boundDirectivesViewer);
		
		
		templateGroupViewer.addNodeSelectionListener(new TemplateGroupViewerNodeSelectionListener() {
			@Override
			public void nodeSelected(TemplateGroupViewerNodeSelectionEvent event) {
				//event.getSelectedTemplateGroup();
				//event.getSelectedTemplate();
				//event.getSelectedTemplateNode();
				updateOperators();
				updateBoundDirectives();
			}
		});
		 */

		
		templateGroupViewer.addNodeDoubleClickListener(new TemplateGroupViewerNodeDoubleClickListener() {
			@Override
			public void nodeDoubleClicked(TemplateGroupViewerNodeSelectionEvent event) {
				onEditBoundDirectives(event.getSelectedTemplateGroup(), event.getSelectedTemplate(), event.getSelectedTemplateNode());
			}
		});


		
		
		
		
		ISelectionListener sl = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection sel) {
				if(sel instanceof ITextSelection) {
					ITextSelection lastSelectedText = (ITextSelection) sel;
					if(!lastSelectedText.isEmpty())
						lastSelectedWorkspaceTextString = lastSelectedText.getText();
				}
			}
		};

		//code also listen for JavaEditor selections only (JavaUI.ID_CU_EDITOR),
		//but people might be using different editors (e.g., WindowBuilder)
		getSite().getPage().addSelectionListener(sl);

		
	    createActions();
		initializeToolBar();
		//initializeMenu();
		
		//does not work reliably, so reverting
		//getSite().setSelectionProvider(templateGroupViewer);
		
		try {
			IViewPart view = getSite().getPage().showView(OperatorOperandsView.ID);
			OperatorOperandsView operatorOperandsView = (OperatorOperandsView) view;
			operatorOperandsView.shouldRegisterAsListenerTo(templateGroupViewer);
		} catch (PartInitException e1) {
			e1.printStackTrace();
		}
				
	}


	protected void onEditBoundDirectives(TemplateGroup oldTemplateGroup, Object selectedTemplate, Object selectedNode) {
		
		BoundDirectivesEditorDialog dialog = new BoundDirectivesEditorDialog(getSite().getShell(), oldTemplateGroup, selectedTemplate, selectedNode);
		int open = dialog.open();
		if(open == BoundDirectivesEditorDialog.CANCEL)
			return;
		templateGroup = TemplateGroup.newFromClojureGroup(dialog.getUpdatedGroup());
		refreshWidgets();
	}

	protected void createActions() {
		// Create the actions
		
		actions = new LinkedList<Action>();
				
		matchTemplateAction = new Action("Match template") {
			public void run() {
				runQuery();
			}
		};
		
		matchTemplateAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_MATCH));
		matchTemplateAction.setToolTipText("Match template");
		actions.add(matchTemplateAction);
		
		inspectQueryAction = new Action("Inspect corresponding query") {
			public void run() {
				viewQuery();
			}
		};
		//inspectQuery.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.ui", "/icons/full/eview16/new_persp.gif"));
		inspectQueryAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_INSPECT));
		inspectQueryAction.setToolTipText("Inspect corresponding query");
		actions.add(inspectQueryAction);
		
		/*
		Action inspectMatches = new Action("Inspect matches") {
			public void run() {
				checkResult();
			}
		};
		inspectMatches.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.pde.ui", "/icons/obj16/tsk_alert_obj.gif"));
		inspectMatches.setToolTipText("Inspect matches");
		actions.add(inspectMatches);

*/
		
		/*
		Action actTrans = new Action("Program Transformation") {			public void run() {
				transformation();
			}
		};
		actTrans.setImageDescriptor(ResourceManager.getPluginImageDescriptor("org.eclipse.egit.ui", "/icons/elcl16/filterresource.gif"));
		actTrans.setToolTipText("Program Transformation");
		*/
	
	}

	/**
	 * Initialize the toolbar.
	 */
	protected void initializeToolBar() {
		IToolBarManager toolbarManager = getEditorSite().getActionBars().getToolBarManager();
		for(Action action : actions) {
			toolbarManager.add(action);
		}
	}

	protected void initializeMenu() {
		IMenuManager menuManager = getEditorSite().getActionBars().getMenuManager();
		for(Action action : actions) {
			menuManager.add(action);
		}
	}

	@Override
	public void setFocus() {
		templateGroupViewer.setFocus();
	}

	
	
	String getSelectedTextFromJavaEditor() {
		
		/*
		 * can no longer use this snippet as we have become an editor ourselves, 
		 * and are active at the moment the user calls this method
		 * 
		ITextEditor editor =  (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();	
		return selection.getText();
		*/
		return lastSelectedWorkspaceTextString;
	}
			
	public TemplateTreeContentProvider getContentProvider() {
		return contentProvider;
	}

	/*
	public void setViewID(String secondaryId) {
		this.viewID = secondaryId;
	}
	*/
				
	public void addSnippet() {
		String code = getSelectedTextFromJavaEditor();
		if (code != null && !code.isEmpty()) {
			//throws NPE when selected text cannot be parsed as the starting point for a template
			templateGroup.addSnippetCode(code);
			templateGroupViewer.clearSelection();
			refreshWidgets();
		}
	}
	
	public void viewSnippet() {
		//removed plain snippet viewer
		//snippetGroup.viewSnippet(getSelectedSnippet());
	}

	public void removeSnippet() {
		Object selected = templateGroupViewer.getSelectedSnippet();
		if(selected == null)
			return;
		if(!MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Delete template", "Are you sure you want to delete the selected template?")) 
			return;
		templateGroup.removeSnippet(selected);
		templateGroupViewer.clearSelection();
		refreshWidgets();
	}

	public void viewQuery() {
		String query = templateGroup.getQuery( templateGroupViewer.getSelectedSnippet());
		QueryInspectorDialog dlg = new QueryInspectorDialog(Display.getCurrent().getActiveShell(),
				"Query", query, "\nExecute the Query?", null, null);
		dlg.create();
		
		if (dlg.open() == Window.OK) 
			runQuery();
	}

	public void runQuery() {
		templateGroup.runQuery(templateGroupViewer.getSelectedSnippet());
	}
	
	class QueryResultThread extends Thread {
		Object selectedSnippet;
		
		public QueryResultThread (Object selectedSnippet) {
			this.selectedSnippet = selectedSnippet;
		}
		
        public void run() {
        	//result check view only for group
			final Object[] result = templateGroup.getQueryResult("Group");
			
    		Display.getDefault().syncExec(new Runnable() {    			
    		    public void run() {
    				try {
    					ResultCheckView view = (ResultCheckView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.ResultCheckView");
    					view.setResult(result);
    					view.setGroup(templateGroup);
    					//view.setSnippet(snippetGroup.getSnippet(selectedSnippet));
    					view.setSnippet(null);
    					view.putData();
    				} catch (PartInitException e) {
    					e.printStackTrace();
    				}
    		    }
    		});
        }
    }	
		
	public void checkResult() {
		QueryResultThread qsThread = new QueryResultThread(templateGroupViewer.getSelectedSnippet());
		qsThread.start();
	}
	
	private void onApplyOperator() {
		/*
		Object operands = operatorOperandsViewer.getOperands();
		if(operands == null)
			return;
		applyOperator(operatorOperandsViewer.getSelectedOperator(), operands);
		*/
	}
	
	
	private void updateOperators() {
		/*
		operatorOperandsViewer.setInput(templateGroup.getGroup(), templateGroupViewer.getSelectedSnippet(), templateGroupViewer.getSelectedSnippetNode());
		*/
	}
	
	
	private void updateTemplate() {
		templateGroupViewer.setInput(templateGroup, templateGroupViewer.getSelectedSnippet(), templateGroupViewer.getSelectedSnippetNode());
		
	}
	
	
	private void refreshWidgets() {
		updateTemplate();
		//updateBoundDirectives();
		//updateOperators();
	}

	/*
	public void undo() {
		snippetGroupHistory.undoOperator();
		renderSnippet();
	}

	public void redo() {
		snippetGroupHistory.redoOperator();
		renderSnippet();
	}
	*/
	
	public void transformation() {
		/*
		try {
			TransformsView view = (TransformsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("damp.ekeko.snippets.gui.TransformsView");
			view.setRewrittenGroup(groups, templateGroup);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
			setInput(input);
			setSite(site);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setSelectedText(String selectedText) {
		lastSelectedWorkspaceTextString = selectedText;
	}
	
}
