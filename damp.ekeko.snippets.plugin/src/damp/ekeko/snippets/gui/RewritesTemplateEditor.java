package damp.ekeko.snippets.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.TemplateGroup;

public class RewritesTemplateEditor extends TemplateEditor {
		
	private TransformationEditor transformationEditor;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		ToolItem toollitemCopyLHS = new ToolItem(toolBar, SWT.NONE);
		//todo: dropdown menu with choice for group or current selection
		toollitemCopyLHS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyTemplateGroupFromLHS();
			}
		});
		toollitemCopyLHS.setImage(EkekoSnippetsPlugin.IMG_TEMPLATE_COPY_FROM_LHS);
		toollitemCopyLHS.setToolTipText("Copy LHS template");
	}

	private void copyTemplateGroupFromLHS() {
		TemplateEditor subjectsEditor = transformationEditor.getSubjectsEditor();
		TemplateGroup lhsGroup = subjectsEditor.getGroup();
		getGroup().addCopyOfSnippetGroup(lhsGroup);
		refreshWidgets();
		becomeDirty();
	}

	public void setTransformationEditor(TransformationEditor transformationEditor) {
		this.transformationEditor = transformationEditor;
	}

}
