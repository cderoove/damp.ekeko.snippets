package damp.ekeko.snippets.gui;

import java.util.LinkedList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class TemplateEditorActionBarContributor extends EditorActionBarContributor {

	TemplateEditor activeTemplateEditor;
	private LinkedList<Action> actions;
	private Action matchTemplateAction;
	private Action inspectQueryAction;

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		if(targetEditor instanceof TemplateEditor)
			activeTemplateEditor = (TemplateEditor) targetEditor;		 
	}

	
	public TemplateEditorActionBarContributor() {
		createActions();
	}

	protected void createActions() {
		actions = new LinkedList<Action>();
		matchTemplateAction = new Action("Match template") {
			public void run() {
				if(activeTemplateEditor != null)
					activeTemplateEditor.runQuery();
			}
		};

		matchTemplateAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_MATCH));
		matchTemplateAction.setToolTipText("Match template");
		actions.add(matchTemplateAction);

		inspectQueryAction = new Action("Inspect corresponding query") {
			public void run() {
				if(activeTemplateEditor != null)
					activeTemplateEditor.viewQuery();
			}
		};
		inspectQueryAction.setImageDescriptor(ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TEMPLATE_INSPECT));
		inspectQueryAction.setToolTipText("Inspect corresponding query");
		actions.add(inspectQueryAction);

	}


	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		for(Action action : actions) {
			toolBarManager.add(action);
		}
	}

	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
			}

	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
	}
}
