package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class TransformationEditorPersistableElementFactory implements IElementFactory {
	public static final String ID = "damp.ekeko.snippets.gui.TransformationEditorPersistableElementFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		TransformationEditorInput transformationEditorInput = new TransformationEditorInput();
		IMemento templateChild = memento.getChild(TransformationEditorInput.TRANSFORMATION_EDITORINPUT_MEMENTO_CHILD_ID);
		if(templateChild == null)
			return transformationEditorInput;
		String filePath = templateChild.getString(TransformationEditorInput.TRANSFORMATION_EDITORINPUT_MEMENTO_FILEPATH_ID);
		if(filePath == null)
			return transformationEditorInput;
		transformationEditorInput.setPathToPersistentFile(filePath);
		return transformationEditorInput;
	}


}
