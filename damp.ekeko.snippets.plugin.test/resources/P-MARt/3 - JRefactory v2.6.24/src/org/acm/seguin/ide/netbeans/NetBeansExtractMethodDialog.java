/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.netbeans;

import javax.swing.*;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.uml.refactor.*;
import org.openide.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.windows.*;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class NetBeansExtractMethodDialog extends ExtractMethodDialog {

	private JEditorPane _editorPane;


	/**
	 *  Constructor for the NetBeansExtractMethodDialog object
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	public NetBeansExtractMethodDialog() throws RefactoringException
	{
		super(null);
	}


	// (PENDING) This constructor doesn't work?
	/**
	 *  Constructor for the NetBeansExtractMethodDialog object
	 *
	 *@param  editorCookie              Description of Parameter
	 *@exception  RefactoringException  Description of Exception
	 */
	public NetBeansExtractMethodDialog(EditorCookie editorCookie)
			 throws RefactoringException
	{
		super(null);
		_editorPane = getCurrentEditorPane(editorCookie);

		if (_editorPane == null) {
			System.out.println("constructor: Editor pane is null!");
		}

	}


	/**
	 *  Sets the string in the IDE
	 *
	 *@param  text   The new StringInIDE value
	 */
	protected void setStringInIDE(String text)
	{
		//_editorPane.setText(text);
		getCurrentEditorPane().setText(text);
	}


	/**
	 *  Gets the SelectionFromIDE attribute of the NetBeansExtractMethodDialog
	 *  object
	 *
	 *@return    The SelectionFromIDE value
	 */
	protected String getSelectionFromIDE()
	{
		//return _editorPane.getSelectedText();
		return getCurrentEditorPane().getSelectedText();
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected String getStringFromIDE()
	{
		//return _editorPane.getText();
		return getCurrentEditorPane().getText();
	}


	/**
	 *  Gets the CurrentEditorPane attribute of the NetBeansExtractMethodDialog
	 *  object
	 *
	 *@return    The CurrentEditorPane value
	 */
	private JEditorPane getCurrentEditorPane()
	{
		return getCurrentEditorPane(null);
	}


	/**
	 *  Gets the CurrentEditorPane attribute of the NetBeansExtractMethodDialog
	 *  object
	 *
	 *@param  cookie  Description of Parameter
	 *@return         The CurrentEditorPane value
	 */
	private JEditorPane getCurrentEditorPane(EditorCookie cookie)
	{
		TopComponent comp =
				TopManager.getDefault().getWindowManager().getRegistry().getActivated();
		Node[] nodes = comp.getRegistry().getActivatedNodes();

		//(NOTE) This is a hack fix
		cookie = null;
		if (nodes.length == 1) {
			cookie = (EditorCookie) nodes[0].getCookie(EditorCookie.class);
		}

		JEditorPane[] panes = cookie.getOpenedPanes();
		if (panes.length == 1) {
			return panes[0];
		}

		return null;
	}

}
