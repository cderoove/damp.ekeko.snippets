package org.acm.seguin.ide.cafe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.refactor.ExtractMethodDialog;

/**
 *  Performs the extract method refactoring for visual cafe
 *
 *@author    Chris Seguin
 */
class CafeExtractMethod extends ExtractMethodDialog implements ActionListener{
	private SourceFile sourceFile;
	
	
	/**
	 *  Constructor for the CafeExtractMethod object
	 */
	public CafeExtractMethod() throws RefactoringException {
		super(null);
	}


	/**
	 *  Sets the StringInIDE attribute of the CafeExtractMethod object
	 *
	 *@param  value  The new StringInIDE value
	 */
	protected void setStringInIDE(String value) {
		if (sourceFile == null) {
			return;
		}
		sourceFile.setText(value);
		sourceFile = null;
	}


	/**
	 *  Gets the StringFromIDE attribute of the CafeExtractMethod object
	 *
	 *@return    The StringFromIDE value
	 */
	protected String getStringFromIDE() {
		//  Get the data from the window
		VisualCafe vc = VisualCafe.getVisualCafe();
		if (vc == null) { return ""; }
		sourceFile = vc.getFrontmostSourceFile();
		if (sourceFile == null) { return ""; }
		return sourceFile.getTextString();
	}


	/**
	 *  Gets the SelectionFromIDE attribute of the CafeExtractMethod object
	 *
	 *@return    The SelectionFromIDE value
	 */
	protected String getSelectionFromIDE() {
		if (sourceFile == null) {
			VisualCafe vc = VisualCafe.getVisualCafe();
    		if (vc == null) { return ""; }
			sourceFile = vc.getFrontmostSourceFile();
    		if (sourceFile == null) { return ""; }
		}
		Range range = sourceFile.getSelectionRange();
		return sourceFile.getRangeTextString(range);
	}


	/**
	 *  What to do when someone selects the extract method refactoring
	 *
	 *@param  e  the button event
	 */
	public void actionPerformed(ActionEvent e) {
		show();
	}
}
