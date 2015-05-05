package damp.ekeko.snippets.gui;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import clojure.lang.IFn;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class TransformationEditor extends MultiPageEditorPart {

	public static IFn FN_SERIALIZE_TRANSFORMATION; 
	public static IFn FN_DESERIALIZE_TRANSFORMATION;

	public static IFn FN_MAKE_TRANSFORMATION;

	public static IFn FN_TRANSFORMATION_LHS; 
	public static IFn FN_TRANSFORMATION_RHS; 


	//returns a fresh clojure representation of a transformation
	//of which the lhs/rhs corresponds to the nested editors
	//might have to be changed to a mutable Java class later on 
	//(analogous to TemplateGroup)
	public Object getTransformation() {
		TemplateGroup lhsGroup = subjectsEditor.getGroup();
		Object cljLHSGroup = lhsGroup.getGroup();
		TemplateGroup rhsGroup = rewritesEditor.getGroup();
		Object cljRHSGroup =  rhsGroup.getGroup();
		return FN_MAKE_TRANSFORMATION.invoke(cljLHSGroup, cljRHSGroup);
	}

	public Object getLHSOfTransformation(Object cljTransformation) {
		return FN_TRANSFORMATION_LHS.invoke(cljTransformation);
	}
	
	

	public Object getRHSOfTransformation(Object cljTransformation) {
		return FN_TRANSFORMATION_RHS.invoke(cljTransformation);
	}

	public static void serializeClojureTransformation(Object transformation, String fullPathToFile) {
		FN_SERIALIZE_TRANSFORMATION.invoke(fullPathToFile, transformation);
	}

	public static Object deserializeClojureTransformation(String fullPathToFile) {
		return FN_DESERIALIZE_TRANSFORMATION.invoke(fullPathToFile);
	}


	public static final String ID = "damp.ekeko.snippets.gui.TransformationEditor"; //$NON-NLS-1$
	private SubjectsTemplateEditor subjectsEditor;
	private int subjectsEditorPageIndex;
	private RewritesTemplateEditor rewritesEditor;
	private int rewritesEditorPageIndex;
	private TemplateGroup lhsTemplateGroup;
	private TemplateGroup rhsTemplateGroup;
	private TransformationOverviewEditor overviewEditor;
	private int overviewEditorPageIndex;


	public void initSubInputsFromTransformationFile(String fullPath) {
		Object cljTransformation = deserializeClojureTransformation(fullPath);
		Object cljLHS = getLHSOfTransformation(cljTransformation);
		Object cljRHS = getRHSOfTransformation(cljTransformation);
		lhsTemplateGroup = TemplateGroup.newFromClojureGroup(cljLHS);
		rhsTemplateGroup = TemplateGroup.newFromClojureGroup(cljRHS);
		
		//still null at this moment
		//subjectsEditor.setGroup(lhsTemplateGroup);
		//rewritesEditor.setGroup(rhsTemplateGroup);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		updateInput(input);
	}

	private void updateInput(IEditorInput input) throws PartInitException {
		setPartName(input.getName());

		lhsTemplateGroup = TemplateGroup.newFromGroupName("LHS");
		rhsTemplateGroup = TemplateGroup.newFromGroupName("RHS");
		
		if(input instanceof FileStoreEditorInput
				|| input instanceof FileEditorInput) {
			String pathToFile = "";
			if(input instanceof FileStoreEditorInput) {
				//outside workspace
				FileStoreEditorInput fileInput = (FileStoreEditorInput) input;
				URI uri = fileInput.getURI();
				pathToFile = uri.getPath();
			} else 
			if(input instanceof FileEditorInput) {
				//within workspace
				FileEditorInput fileInput = (FileEditorInput) input;
				IFile ifile = fileInput.getFile();
				pathToFile = ifile.getLocation().toString();
			} else {
				setInput(new TransformationEditorInput());
				return;
			}
			TransformationEditorInput actualInput = new TransformationEditorInput();
			actualInput.setPathToPersistentFile(pathToFile);	
			setInput(actualInput);
			try {
				initSubInputsFromTransformationFile(pathToFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if(input instanceof TransformationEditorInput) {
			ClojureFileEditorInput actualInput = (ClojureFileEditorInput) input;
			if(actualInput.associatedPersistentFileExists()) {
				try {
					initSubInputsFromTransformationFile(actualInput.getPathToPersistentFile());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			setInput(input);
			return;
		}

		throw new PartInitException("Unexpected input for TransformationEditor: " + input.toString());		
	}

	protected void onExecuteTransformation() {
		// CompareUI.openCompareDialog(input);
		// TODO Auto-generated method stub
		TemplateGroup.transformBySnippetGroups(subjectsEditor.getGroup().getGroup(), rewritesEditor.getGroup().getGroup());

	}

	@Override
	protected void createPages() {
		try {
			createOverviewPage();
			createSubjectsPage();
			createRewritesPage();
			//overviewEditor.setFocus();
			
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TransformationEditorInput getTransformationEditorInput() {
		return (TransformationEditorInput) getEditorInput();
	}

	
	public TemplateEditor getSubjectsEditor() {
		return subjectsEditor;
	}
	
	public TemplateEditor getRewritesEditor() {
		return rewritesEditor;
	}
	
	public TemplateEditorInput getSubjectsEditorInput() {
		return getTransformationEditorInput().getSubjectsEditorInput();
	}

	public TemplateEditorInput getRewritesEditorInput() {
		return getTransformationEditorInput().getRewritesEditorInput();
	}

	private void createRewritesPage() throws PartInitException {
		rewritesEditor = new RewritesTemplateEditor();
		rewritesEditorPageIndex = addPage(rewritesEditor, getRewritesEditorInput());
		rewritesEditor.setGroup(rhsTemplateGroup);
		setPageText(rewritesEditorPageIndex, "Replacement Templates");
		setPageImage(rewritesEditorPageIndex, EkekoSnippetsPlugin.IMG_TRANSFORMATION);
		rewritesEditor.setTransformationEditor(this);
	}


	private void createSubjectsPage() throws PartInitException {
		subjectsEditor = new SubjectsTemplateEditor();
		subjectsEditorPageIndex = addPage(subjectsEditor, getSubjectsEditorInput());
		subjectsEditor.setGroup(lhsTemplateGroup);
		setPageText(subjectsEditorPageIndex, "Search Templates");
		setPageImage(subjectsEditorPageIndex, EkekoSnippetsPlugin.IMG_TEMPLATE);
	}
	
	private void createOverviewPage() throws PartInitException {
		overviewEditor = new TransformationOverviewEditor();
		overviewEditorPageIndex = addPage(overviewEditor, null);
		setPageText(overviewEditorPageIndex, "Overview");
		setPageImage(overviewEditorPageIndex, EkekoSnippetsPlugin.IMG_TEMPLATE);
		overviewEditor.setTransformationEditor(this);

	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		IEditorInput input = getEditorInput();
		if(!(input instanceof TransformationEditorInput))
			return;
		String absoluteFilePathString;	
		ClojureFileEditorInput teinput = (ClojureFileEditorInput) input;
		if(!teinput.isAssociatedWithPersistentFile()) {
			FileDialog fileDialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		    fileDialog.setFilterExtensions(new String[] { "*.ekx" });
		    fileDialog.setFilterNames(new String[] { "Ekeko/X transformation file (*.ekx)" });
		    absoluteFilePathString = fileDialog.open();
		    if(absoluteFilePathString == null)
		    	return;   
		    teinput.setPathToPersistentFile(absoluteFilePathString);
		} else {
			absoluteFilePathString = teinput.getPathToPersistentFile();
		}
		try {
			Object transformation = getTransformation();
			serializeClojureTransformation(transformation, absoluteFilePathString);		
			subjectsEditor.becomeClean();
			rewritesEditor.becomeClean();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {
		IEditorInput input = getEditorInput();
		if(!(input instanceof TransformationEditorInput))
			return;
		TransformationEditorInput newInput = new TransformationEditorInput();
		setInput(newInput); //fires no prop change, only sets field
		try {
			doSave(new NullProgressMonitor()); //serializes to new file
			updateInput(newInput); //deserializes from file
		} catch (Exception e) {
			e.printStackTrace();
			setInput(input);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	protected void handlePropertyChange(int propertyId) {
		if(propertyId == PROP_DIRTY) {
			//one of the nested editors has become dirty
			//could update our clojure representation of the corresponding transformation
			//for now, simply recreating this representation on demand
		}
		super.handlePropertyChange(propertyId);
	}

	public void setPreviouslyActiveEditor(IEditorPart activeEditor) {
		subjectsEditor.setPreviouslyActiveEditor(activeEditor);
		rewritesEditor.setPreviouslyActiveEditor(activeEditor);
	}

}
