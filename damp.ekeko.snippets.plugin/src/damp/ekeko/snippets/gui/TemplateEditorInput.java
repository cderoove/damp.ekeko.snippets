package damp.ekeko.snippets.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import clojure.lang.IFn;

public class TemplateEditorInput implements IEditorInput {

	public static IFn serializeClojureTemplateGroup;
	public static IFn deserializeClojureTemplateGroup;
	
	private String pathToFile = null;
	
	public String getPathToPersistentFile() {
		return pathToFile;
	}
	
	public void setPathToPersistentFile(String path) {
		pathToFile = path;
	}
	
	public boolean isAssociatedWithPersistentFile() {
		return pathToFile != null;
	}
	
	public static void serializeClojureTemplateGroup(Object cljTemplateGroup, String fullPathToFile) {
		serializeClojureTemplateGroup.invoke(fullPathToFile, cljTemplateGroup);
	}
	
	public static Object deserializeClojureTemplateGroup(String fullPathToFile) {
		return deserializeClojureTemplateGroup.invoke(fullPathToFile);
	}

	
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

}
