package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class TemplateEditorPersistableElementFactory implements IElementFactory {

	public static final String ID = "damp.ekeko.snippets.gui.TemplateEditorPersistableElementFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		TemplateEditorInput templateEditorInput = new TemplateEditorInput();
		IMemento templateChild = memento.getChild(TemplateEditorInput.TEMPLATEEDITORINPUT_MEMENTO_CHILD_ID);
		if(templateChild == null)
			return templateEditorInput;
		String filePath = templateChild.getString(TemplateEditorInput.TEMPLATEEDITORINPUT_MEMENTO_FILEPATH_ID);
		if(filePath == null)
			return templateEditorInput;
		templateEditorInput.setPathToPersistentFile(filePath);
		return templateEditorInput;
	}

}
