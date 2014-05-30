package damp.ekeko.snippets.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class TransformationEditorCommandHandler extends AbstractHandler {
	public TransformationEditorCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			IEditorPart openedEditor = window.getActivePage().openEditor(new TransformationEditorInput(), TransformationEditor.ID);
			TransformationEditor transformationEditor = (TransformationEditor) openedEditor;
			transformationEditor.setPreviouslyActiveEditor(activeEditor);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
}
