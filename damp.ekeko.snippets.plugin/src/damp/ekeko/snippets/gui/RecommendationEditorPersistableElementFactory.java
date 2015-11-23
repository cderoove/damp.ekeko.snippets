package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class RecommendationEditorPersistableElementFactory implements IElementFactory {

	public static final String ID = "damp.ekeko.snippets.gui.RecommendationEditorPersistableElementFactory"; //$NON-NLS-1$
	
	@Override
	public IAdaptable createElement(IMemento memento) {
		RecommendationEditorInput intendedResultsEditorInput = new RecommendationEditorInput();
		IMemento recommendationChild = memento.getChild(RecommendationEditorInput.RECOMMENDATION_INPUT_MEMENTO_CHILD_ID);
		if(recommendationChild == null)
			return intendedResultsEditorInput;
		String filePath = recommendationChild.getString(RecommendationEditorInput.RECOMMENDATION_INPUT_MEMENTO_FILEPATH_ID);
		if(filePath == null)
			return intendedResultsEditorInput;
		intendedResultsEditorInput.setPathToPersistentFile(filePath);
		return intendedResultsEditorInput;
	}

}
