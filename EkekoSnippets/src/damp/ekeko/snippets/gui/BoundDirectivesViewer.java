package damp.ekeko.snippets.gui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BoundDirectivesViewer extends Composite {

	public BoundDirectivesViewer(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);
		
		//very similar to operatoroperandsbinding
		
		//table of directives
		
		//explanation of current directive
		
		//table with arguments for current directive

	}
	
	


}
