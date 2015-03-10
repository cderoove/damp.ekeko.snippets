package org.acm.seguin.ide.jbuilder;

import java.io.File;

import org.acm.seguin.ide.common.SourceBrowser;

/**
 *  Base class for source browsing. This is the generic base class.
 *
 *@author    Chris Seguin
 */
public class JBuilderBrowser extends SourceBrowser {
	/**
	 *  Determines if the system is in a state where it can browse the source
	 *  code
	 *
	 *@return    true if the source code browsing is enabled
	 */
	public boolean canBrowseSource() {
		return true;
	}


	/**
	 *  Actually browses to the file
	 *
	 *@param  file  the file
	 *@param  line  the line in the file
	 */
	public void gotoSource(File file, int line) {
		if (file != null) {
			FileNode sourceNode = findSourceFileNode(file);
			showNode(sourceNode);
			gotoLine(line, sourceNode);
		}
	}


	/**
	 *  Get the FileNode that matches a File (in this project).
	 *
	 *@param  file  File to look for in this project.
	 *@return       FileNode The FileNode. *duh
	 */
	protected FileNode findSourceFileNode(File file) {
		Browser browser = Browser.getActiveBrowser();
		Project project = browser.getActiveProject();
		if (project == null) {
			project = browser.getDefaultProject();
		}
		Url url = new Url(file);
		return project.getNode(url);
	}


	/**
	 *  Show a source file.
	 *
	 *@param  node  Source file node to show.
	 */
	protected void showNode(FileNode node) {
		Browser browser = Browser.getActiveBrowser();
		try {
			browser.setActiveNode(node, true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 *  Go to a specific line in a source file.
	 *
	 *@param  lineNumber  Line number to go to.
	 *@param  sourceNode  Source file node.
	 */
	protected void gotoLine(int lineNumber, FileNode sourceNode) {
		AbstractTextNodeViewer sourceViewer =
				(AbstractTextNodeViewer) Browser.getActiveBrowser().getViewerOfType(sourceNode, AbstractTextNodeViewer.class);
		EditorPane editor = sourceViewer.getEditor();
		editor.gotoPosition(lineNumber, 1, false, EditorPane.CENTER_ALWAYS);
	}
}
