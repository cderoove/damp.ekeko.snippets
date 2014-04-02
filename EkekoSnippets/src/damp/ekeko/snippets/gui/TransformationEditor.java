package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class TransformationEditor extends MultiPageEditorPart {

	public static final String ID = "damp.ekeko.snippets.gui.TransformationEditor"; //$NON-NLS-1$
	private SubjectsTemplateEditor subjectsEditor;
	private int subjectsEditorPageIndex;
	private RewritesTemplateEditor rewritesEditor;
	private int rewritesEditorPageIndex;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof TransformationEditorInput))
            throw new PartInitException("Invalid input for Ekeko/X Transformation editor: " + input);
		super.init(site, input);
		
		
		initActions();
		
		//extract input for sub-editors? 
	}
	
	private void initActions() {
		
		
		Action transformAction = new Action() {
			public void run() {
				onExecuteTransformation();
			}
		};
		transformAction.setText("Execute transformation");
		transformAction.setToolTipText("Applies the rewrite actions to all transformation subjects");
		transformAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TRANSFORM));
		
		IToolBarManager toolbarManager = getEditorSite().getActionBars().getToolBarManager();
		toolbarManager.add(transformAction);

	}
		
	protected void onExecuteTransformation() {
		// CompareUI.openCompareDialog(input);
		// TODO Auto-generated method stub
		TemplateGroup.transformBySnippetGroups(subjectsEditor.getGroup().getGroup(), rewritesEditor.getGroup().getGroup());
		
	}

	@Override
	protected void createPages() {
		try {
			createSubjectsPage();
			createRewritesPage();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TransformationEditorInput getTransformationEditorInput() {
		return (TransformationEditorInput) getEditorInput();
	}
	
	public TemplateEditorInput getSubjectsEditorInput() {
		return getTransformationEditorInput().getSubjectsEditorInput();
	}
	
	public TemplateEditorInput getRewritesEditorInput() {
		return getTransformationEditorInput().getRewritesEditorInput();
	}

	private void createRewritesPage() throws PartInitException {
		rewritesEditor = new RewritesTemplateEditor();
		rewritesEditorPageIndex = addPage(rewritesEditor, getRewritesEditorInput());
		setPageText(rewritesEditorPageIndex, "Rewrites");
		setPageImage(rewritesEditorPageIndex, EkekoSnippetsPlugin.IMG_TRANSFORMATION);
	}

	private void createSubjectsPage() throws PartInitException {
		subjectsEditor = new SubjectsTemplateEditor();
		subjectsEditorPageIndex = addPage(subjectsEditor, getSubjectsEditorInput());
		setPageText(subjectsEditorPageIndex, "Subjects");
		setPageImage(subjectsEditorPageIndex, EkekoSnippetsPlugin.IMG_TEMPLATE);
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
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
		
}
