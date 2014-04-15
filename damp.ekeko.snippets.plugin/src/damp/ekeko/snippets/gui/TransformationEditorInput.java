package damp.ekeko.snippets.gui;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class TransformationEditorInput  extends ClojureFileEditorInput  implements IEditorInput,  IPersistableElement {

	private TemplateEditorInput subjectsEditorInput;
	private TemplateEditorInput rewritesEditorInput;
	
	public TransformationEditorInput() {
		subjectsEditorInput = new TemplateEditorInput(); //marks as subjects?
		rewritesEditorInput = new TemplateEditorInput(); //mark as rewrites?
	}
	@Override
	public boolean exists() {
		return associatedPersistentFileExists();	
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(EkekoSnippetsPlugin.IMG_TRANSFORMATION);
	}

	@Override
	public String getName() {
		if(!exists())
			return "New Transformation";
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
	
	public final static String TRANSFORMATION_EDITORINPUT_MEMENTO_CHILD_ID = "Transformation";
	public final static String TRANSFORMATION_EDITORINPUT_MEMENTO_FILEPATH_ID = "filePath";


	@Override
	public void saveState(IMemento memento) {
		IMemento storedTemplate = memento.createChild(TRANSFORMATION_EDITORINPUT_MEMENTO_CHILD_ID);
		storedTemplate.putString(TRANSFORMATION_EDITORINPUT_MEMENTO_FILEPATH_ID, getPathToPersistentFile());		
	}

	@Override
	public String getFactoryId() {
		return TransformationEditorPersistableElementFactory.ID;
	}

	public TemplateEditorInput getSubjectsEditorInput() {
		return subjectsEditorInput;
	}
	
	public TemplateEditorInput getRewritesEditorInput() {
		return rewritesEditorInput;
	}
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
