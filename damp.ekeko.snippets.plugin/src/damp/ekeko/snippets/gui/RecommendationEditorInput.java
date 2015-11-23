package damp.ekeko.snippets.gui;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import clojure.lang.IFn;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class RecommendationEditorInput extends ClojureFileEditorInput implements IEditorInput, IPersistableElement {

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return associatedPersistentFileExists();	
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_RECOMMENDATION);
	}

	@Override
	public String getName() {
		if(!exists())
			return "New Recommendation";
		File file = new File(getPathToPersistentFile());
		return file.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		if(exists())
			return this;
		return null;
	}

	@Override
	public String getToolTipText() {
		if(!exists())
			return getName();
		return pathToFile;
	}
	public final static String RECOMMENDATION_INPUT_MEMENTO_CHILD_ID = "Recommendation";
	public final static String RECOMMENDATION_INPUT_MEMENTO_FILEPATH_ID = "filePath";


	@Override
	public void saveState(IMemento memento) {
		IMemento storedTemplate = memento.createChild(RECOMMENDATION_INPUT_MEMENTO_CHILD_ID);
		storedTemplate.putString(RECOMMENDATION_INPUT_MEMENTO_FILEPATH_ID, getPathToPersistentFile());		
	}

	@Override
	public String getFactoryId() {
		return RecommendationEditorPersistableElementFactory.ID;
	}

}
