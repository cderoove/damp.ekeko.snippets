package damp.ekeko.snippets.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class TemplateEditorCommandHandler extends AbstractHandler {
	public TemplateEditorCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {

			//copy selected text in current editor
			String selectedText = "";
			ITextEditor editor =  (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if(editor != null) {
				ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();	
				selectedText = selection.getText();
			}
			
			//window.getActivePage().showView(TemplateEditor.ID);
			IEditorPart openedEditor = window.getActivePage().openEditor(new TemplateEditorInput(), TemplateEditor.ID);
			TemplateEditor templateEditor = (TemplateEditor) openedEditor;
			templateEditor.setSelectedText(selectedText);

			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
}
