package damp.ekeko.snippets.gui;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import clojure.lang.IFn;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class IntendedResultsEditorInput extends ClojureFileEditorInput implements IEditorInput, IPersistableElement {

	/*
	public static IFn FN_SERIALIZE_TEMPLATEGROUP;
	public static IFn FN_DESERIALIZE_TEMPLATEGROUP;

	public static void serializeClojureTemplateGroup(Object cljTemplateGroup, String fullPathToFile) {
		FN_SERIALIZE_TEMPLATEGROUP.invoke(fullPathToFile, cljTemplateGroup);
	}

	public static Object deserializeClojureTemplateGroup(String fullPathToFile) {
		return FN_DESERIALIZE_TEMPLATEGROUP.invoke(fullPathToFile);
	}

	 */
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		return associatedPersistentFileExists();	
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_INTENDED_RESULTS);
	}

	@Override
	public String getName() {
		if(!exists())
			return "New Intended Results";
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
	public final static String INTENDED_RESULTS_INPUT_MEMENTO_CHILD_ID = "IntendedResults";
	public final static String INTENDED_RESULTS_INPUT_MEMENTO_FILEPATH_ID = "filePath";


	@Override
	public void saveState(IMemento memento) {
		IMemento storedTemplate = memento.createChild(INTENDED_RESULTS_INPUT_MEMENTO_CHILD_ID);
		storedTemplate.putString(INTENDED_RESULTS_INPUT_MEMENTO_FILEPATH_ID, getPathToPersistentFile());		
	}

	@Override
	public String getFactoryId() {
		return IntendedResultsEditorPersistableElementFactory.ID;
	}

}
