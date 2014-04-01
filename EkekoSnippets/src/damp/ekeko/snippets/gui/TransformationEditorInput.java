package damp.ekeko.snippets.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class TransformationEditorInput implements IEditorInput {

	private TemplateEditorInput subjectsEditorInput;
	private TemplateEditorInput rewritesEditorInput;

	public TransformationEditorInput() {
		subjectsEditorInput = new TemplateEditorInput(); //marks as subjects?
		rewritesEditorInput = new TemplateEditorInput(); //mark as rewrites?
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

	public TemplateEditorInput getSubjectsEditorInput() {
		return subjectsEditorInput;
	}
	
	public TemplateEditorInput getRewritesEditorInput() {
		return rewritesEditorInput;
	}
	

}
