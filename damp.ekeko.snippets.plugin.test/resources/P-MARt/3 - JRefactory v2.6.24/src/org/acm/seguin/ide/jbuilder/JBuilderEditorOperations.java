/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.jbuilder.node.JavaFileNode;
import com.borland.primetime.editor.EditorPane;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import com.borland.primetime.vfs.Buffer;
import com.borland.primetime.vfs.ReadOnlyException;
import com.borland.primetime.viewer.AbstractTextNodeViewer;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.acm.seguin.ide.common.EditorOperations;

/**
 *  The implementation of the editor operations for JBuilder
 *
 *@author    Chris Seguin
 */
public class JBuilderEditorOperations extends EditorOperations {
	private Buffer buffer;


	/**
	 *  Sets the string in the IDE
	 *
	 *@param  value  The new file contained in a string
	 */
	public void setStringInIDE(String value)
	{
		if (value != null) {
			try {
				buffer.setContent(value.getBytes());
			}
			catch (ReadOnlyException roe) {
				JOptionPane.showMessageDialog(null,
						"The file that you ran the pretty printer on is read only.",
						"Read Only Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	/**
	 *  Sets the line number
	 *
	 *@param  value  The new LineNumber value
	 */
	public void setLineNumber(int value)
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		AbstractTextNodeViewer sourceViewer =
				(AbstractTextNodeViewer) browser.getViewerOfType(active, AbstractTextNodeViewer.class);
		EditorPane editor = sourceViewer.getEditor();
		editor.gotoPosition(value, 1, false, EditorPane.CENTER_ALWAYS);
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	public String getStringFromIDE()
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		if (active instanceof JavaFileNode) {
			JavaFileNode jtn = (JavaFileNode) active;
			try {
				buffer = jtn.getBuffer();
				byte[] contents = buffer.getContent();
				return new String(contents);
			}
			catch (java.io.IOException ioex) {
				ioex.printStackTrace();
			}
		}

		return null;
	}


	/**
	 *  Returns the initial line number
	 *
	 *@return    The LineNumber value
	 */
	public int getLineNumber()
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		if (active == null)
			return -1;

		AbstractTextNodeViewer sourceViewer =
				(AbstractTextNodeViewer) browser.getViewerOfType(active, AbstractTextNodeViewer.class);
		if (sourceViewer == null)
			return -1;

		EditorPane editor = sourceViewer.getEditor();
		if (editor == null)
			return -1;

		int pos = editor.getCaretPosition();
		return editor.getLineNumber(pos);
	}


	/**
	 *  Gets the SelectionFromIDE attribute of the JBuilderExtractMethod object
	 *
	 *@return    The SelectionFromIDE value
	 */
	public String getSelectionFromIDE()
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		AbstractTextNodeViewer sourceViewer =
				(AbstractTextNodeViewer) Browser.getActiveBrowser().getViewerOfType(active, AbstractTextNodeViewer.class);
		EditorPane editor = sourceViewer.getEditor();
		return editor.getSelectedText();
	}


	/**
	 *  Returns the frame that contains the editor. If this is not available or
	 *  you want dialog boxes to be centered on the screen return null from this
	 *  operation.
	 *
	 *@return    the frame
	 */
	public JFrame getEditorFrame()
	{
		return Browser.getActiveBrowser();
	}


	/**
	 *  Returns true if the current file being edited is a java file
	 *
	 *@return    true if the current file is a java file
	 */
	public boolean isJavaFile()
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();
		return (active instanceof JavaFileNode);
	}


	/**
	 *  Gets the file that is being edited
	 *
	 *@return    The File value
	 */
	public File getFile()
	{
		Browser browser = Browser.getActiveBrowser();
		Node active = browser.getActiveNode();

		if (active instanceof FileNode) {
			return ((FileNode) active).getUrl().getFileObject();
		}
		else {
			return null;
		}
	}
}
