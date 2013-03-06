package damp.ekeko.snippets.gui;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public class CodeViewer extends StyledText {
	private MainView mainView;

	public CodeViewer(MainView mainView, Composite parent, int style) {
		super(parent, style);
		this.mainView = mainView;
	}

	public void setInput(String text) {
		setText(text);
	}
	
}
