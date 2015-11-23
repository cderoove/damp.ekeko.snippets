package damp.ekeko.snippets.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RecommendationEditorCommandHandler extends AbstractHandler {
	public RecommendationEditorCommandHandler() {}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			IEditorPart openedEditor = window.getActivePage().openEditor(new RecommendationEditorInput(), RecommendationEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
}
