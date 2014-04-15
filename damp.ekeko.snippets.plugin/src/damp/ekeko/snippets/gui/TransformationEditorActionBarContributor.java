package damp.ekeko.snippets.gui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class TransformationEditorActionBarContributor extends MultiPageEditorActionBarContributor {

	TemplateEditor activeTemplateEditor;
	TransformationEditor activeTransformationEditor;

	private List<Action> actions;
	private Action matchTemplateAction;
	private Action inspectQueryAction;


	@Override
	public void setActiveEditor(IEditorPart activeEditor) {
		super.setActiveEditor(activeEditor);
		if(activeEditor instanceof TransformationEditor)
			activeTransformationEditor = (TransformationEditor) activeEditor;		 
	}

	@Override
	public void setActivePage(IEditorPart activeEditor) {
		if(activeEditor instanceof TemplateEditor)
			activeTemplateEditor = (TemplateEditor) activeEditor;		 
		if(activeEditor instanceof SubjectsTemplateEditor) {
			enableLHSActions(true);
		} else {
			enableLHSActions(false);
		}
	}

	private void enableLHSActions(boolean enabled) {
		matchTemplateAction.setEnabled(enabled);
		inspectQueryAction.setEnabled(enabled);
	}

	public TransformationEditorActionBarContributor() {
		createActions();
	}

	protected void createActions() {
		actions = new LinkedList<Action>();
		Action transformAction = new Action() {
			public void run() {
				if(activeTransformationEditor != null)
					activeTransformationEditor.onExecuteTransformation();
			}
		};
		transformAction.setText("Execute transformation");
		transformAction.setToolTipText("Applies the rewrite actions to all transformation subjects");
		transformAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TRANSFORM));
		actions.add(transformAction);
		
		
		matchTemplateAction = new Action("Match LHS template") {
			public void run() {
				if(activeTemplateEditor instanceof SubjectsTemplateEditor)
					activeTemplateEditor.runQuery();
			}
		};

		matchTemplateAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_MATCH));
		matchTemplateAction.setToolTipText("Match LHS template");
		actions.add(matchTemplateAction);

		inspectQueryAction = new Action("Inspect query for LHS template") {
			public void run() {
				if(activeTemplateEditor instanceof SubjectsTemplateEditor)
					activeTemplateEditor.viewQuery();
			}
		};
		inspectQueryAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_INSPECT));
		inspectQueryAction.setToolTipText("Inspect query corresponding to LHS template");
		actions.add(inspectQueryAction);
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		for(Action action : actions) {
			toolBarManager.add(action);
		}
	}
}