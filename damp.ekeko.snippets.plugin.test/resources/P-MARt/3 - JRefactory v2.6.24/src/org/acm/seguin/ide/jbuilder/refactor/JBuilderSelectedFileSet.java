/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder.refactor;

import com.borland.jbuilder.node.JavaFileNode;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.Node;
import com.borland.primetime.vfs.Buffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.ide.common.action.SelectedFileSet;
import org.acm.seguin.ide.jbuilder.UMLNodeViewerFactory;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  The concrete implementation of this class for JBuilder
 *
 *@author    Chris Seguin
 */
public class JBuilderSelectedFileSet extends SelectedFileSet {
	private Node[] initialNodes;


	/**
	 *  Constructor for the JBuilderSelectedFileSet object
	 *
	 *@param  init  Description of Parameter
	 */
	public JBuilderSelectedFileSet(Node[] init)
	{
		initialNodes = init;
	}


	/**
	 *  Gets the AllJava attribute of the SelectedFileSet object
	 *
	 *@return    The AllJava value
	 */
	public boolean isAllJava()
	{
		Node[] nodeArray = getNodes();
		for (int ndx = 0; ndx < nodeArray.length; ndx++) {
			if (!(nodeArray[0] instanceof JavaFileNode)) {
				return false;
			}
		}

		return true;
	}



	/**
	 *  Gets the SingleJavaFile attribute of the SelectedFileSet object
	 *
	 *@return    The SingleJavaFile value
	 */
	public boolean isSingleJavaFile()
	{
		Node[] nodeArray = getNodes();
		return (nodeArray.length == 1) && (nodeArray[0] instanceof JavaFileNode);
	}


	/**
	 *  Gets the TypeSummaryArray attribute of the SelectedFileSet object
	 *
	 *@return    The TypeSummaryArray value
	 */
	public TypeSummary[] getTypeSummaryArray()
	{
		Node[] nodeArray = getNodes();

		TypeSummary[] typeSummaryArray = new TypeSummary[nodeArray.length];

		for (int ndx = 0; ndx < nodeArray.length; ndx++) {
			TypeSummary typeSummary = getTypeSummaryFromNode(nodeArray[ndx]);
			if (typeSummary == null) {
				return null;
			}
			typeSummaryArray[ndx] = typeSummary;
		}

		return typeSummaryArray;
	}


	/**
	 *  Gets the TypeSummaryFromNode attribute of the AddParentClassAction object
	 *
	 *@param  node  Description of Parameter
	 *@return       The TypeSummaryFromNode value
	 */
	private TypeSummary getTypeSummaryFromNode(Node node)
	{
		FileSummary fileSummary = reloadNode(node);
		if (fileSummary == null) {
			return null;
		}

		return getTypeSummary(fileSummary);
	}


	/**
	 *  Gets the Nodes attribute of the JBuilderRefactoringAction object
	 *
	 *@return    The Nodes value
	 */
	private Node[] getNodes()
	{
		if (initialNodes == null) {
			Node[] nodeArray = new Node[1];
			Browser browser = Browser.getActiveBrowser();
			nodeArray[0] = browser.getActiveNode();
			return nodeArray;
		}
		else {
			return initialNodes;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private FileSummary reloadNode(Node node)
	{
		try {
			if (node instanceof JavaFileNode) {
				JavaFileNode jtn = (JavaFileNode) node;

				Buffer buffer = jtn.getBuffer();
				byte[] contents = buffer.getContent();
				ByteArrayInputStream bais = new ByteArrayInputStream(contents);

				return reloadFile(jtn.getUrl().getFileObject(), bais);
			}
		}
		catch (IOException ioe) {
			//  Unable to get the buffer for that node, so fail
		}

		return null;
	}
}
