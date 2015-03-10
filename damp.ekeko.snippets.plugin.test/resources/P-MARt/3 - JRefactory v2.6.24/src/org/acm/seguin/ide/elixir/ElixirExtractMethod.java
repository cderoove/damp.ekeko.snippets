/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Publics License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.refactor.ExtractMethodDialog;

/**
 *  ExtractMethod for the elixir editor.
 *
 *@author    Chris Seguin
 *@date      May 31, 1999
 */
public class ElixirExtractMethod extends ExtractMethodDialog {
	private BasicViewManager bvm;


	/**
	 *  Create an ElixirPrettyPrinter object
	 */
	public ElixirExtractMethod() throws RefactoringException {
		super(FrameManager.current().getFrame());
	}


	/**
	 *  Remove \r from buffer
	 *
	 *@param  input  Description of Parameter
	 *@return        a string containing the results
	 */
	public String removeCR(String input) {
		StringBuffer buffer = new StringBuffer();
		int last = input.length();

		for (int ndx = 0; ndx < last; ndx++) {
			char ch = input.charAt(ndx);
			if (ch == '\r') {
				//  Do nothing
			}
			else {
				buffer.append(ch);
			}
		}

		return buffer.toString();
	}



	/**
	 *  Sets the string in the IDE
	 *
	 *@param  value  The new file contained in a string
	 */
	protected void setStringInIDE(String value) {
		bvm.setContentsString(value);
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected String getStringFromIDE() {
		FrameManager fm = FrameManager.current();
		bvm = (BasicViewManager) fm.getViewSite().getCurrentViewManager();
		if (bvm == null) {
			return null;
		}

		return bvm.getContentsString();
	}


	/**
	 *  Gets the SelectionFromIDE attribute of the ElixirExtractMethod object
	 *
	 *@return    The SelectionFromIDE value
	 */
	protected String getSelectionFromIDE() {
		try {
			Object view = bvm.getView();
			System.out.println("View is a :  " + view.getClass().getName());
			JPanel panel = (JPanel) view;

			Component editor = searchPanels(panel, " ");

			if (editor instanceof JTextComponent) {
				JTextComponent comp = (JTextComponent) editor;
				return comp.getSelectedText();
			}

			System.out.println("Not a text component");
			return null;
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}
		return null;
	}


	/**
	 *  Useful program that searches through the different panels
	 *  to find the text panel that we can get the selected code
	 *  from.
	 *
	 *@param  jPanel  Description of Parameter
	 *@param  prefix  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	private Component searchPanels(Container jPanel, String prefix) {
		int last = jPanel.getComponentCount();
		for (int ndx = 0; ndx < last; ndx++) {
			Component next = jPanel.getComponent(ndx);
			System.out.println(prefix + ":" + ndx + "  " + next.getClass().getName());
			if (next instanceof LineEditorPane) {
				return next;
			}
			if (next instanceof Container) {
				Component result = searchPanels((Container) next, prefix + ":" + ndx);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}


	/**
	 *  Reformats the current source code
	 */
	public static void extractMethod() {
		try {
			System.out.println("extract method #1");
			ElixirExtractMethod eem = new ElixirExtractMethod();
			System.out.println("extract method #2");
			eem.show();
			System.out.println("extract method #3");
		}
		catch (RefactoringException re) {
			JOptionPane.showMessageDialog(null, re.getMessage(), "Refactoring Exception",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
