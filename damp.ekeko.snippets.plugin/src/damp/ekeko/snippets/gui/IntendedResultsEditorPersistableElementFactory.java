package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class IntendedResultsEditorPersistableElementFactory implements IElementFactory {

	public static final String ID = "damp.ekeko.snippets.gui.IntendedResultsEditorPersistableElementFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		IntendedResultsEditorInput intendedResultsEditorInput = new IntendedResultsEditorInput();
		IMemento intendedResultsChild = memento.getChild(IntendedResultsEditorInput.INTENDED_RESULTS_INPUT_MEMENTO_CHILD_ID);
		if(intendedResultsChild == null)
			return intendedResultsEditorInput;
		String filePath = intendedResultsChild.getString(IntendedResultsEditorInput.INTENDED_RESULTS_INPUT_MEMENTO_FILEPATH_ID);
		if(filePath == null)
			return intendedResultsEditorInput;
		intendedResultsEditorInput.setPathToPersistentFile(filePath);
		return intendedResultsEditorInput;
	}

}
