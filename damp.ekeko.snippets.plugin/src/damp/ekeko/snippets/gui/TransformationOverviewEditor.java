package damp.ekeko.snippets.gui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;


public class TransformationOverviewEditor extends EditorPart {

	private TransformationEditor transformationEditor;
	private TextViewer textViewerSnippet;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		//setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void createPartControl(Composite composite) {
		composite.setLayout(new FillLayout());
		textViewerSnippet = new TextViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		StyledText styledText = textViewerSnippet.getTextWidget();
		//GridData gd_styledText = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		//gd_styledText.heightHint = 100;
		//styledText.setLayoutData(gd_styledText);
		textViewerSnippet.setEditable(false);		
		styledText.setFont(EkekoSnippetsPlugin.getEditorFont());
		styledText.setCaret(null);
	}

	@Override
	public void setFocus() {
		//Object cljTransformation = transformationEditor.getTransformation();
				
		TemplateGroup lhs = transformationEditor.getSubjectsEditor().getGroup();
		TemplatePrettyPrinter pp = new TemplatePrettyPrinter(lhs);
		pp.prettyPrint();
		
		pp.prettyPrintArrow();
		
		pp.setTemplateGroup(transformationEditor.getRewritesEditor().getGroup());
		String transformationString = pp.prettyPrint();
		
		textViewerSnippet.getTextWidget().setText(transformationString);
		for(StyleRange range : pp.getStyleRanges())
			textViewerSnippet.getTextWidget().setStyleRange(range);


		textViewerSnippet.getControl().setFocus();
		
	}

	public void setTransformationEditor(TransformationEditor transformationEditor) {
		this.transformationEditor = transformationEditor;
	}

}
