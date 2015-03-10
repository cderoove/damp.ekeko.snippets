/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.acm.seguin.ide.common.UMLIcon;

/**
 *  Stores a leaf node for a UML class diagram 
 *
 *@author    Chris Seguin 
 */
class UMLLeaf implements TNode {
	private TNode parent;
	private File file;
	private UMLDocManager docManager;
	private String packageName;


	/**
	 *  Constructor for the UMLLeaf object 
	 *
	 *@param  parent      the parent file 
	 *@param  file        the file 
	 *@param  docManager  the document manager 
	 */
	public UMLLeaf(TNode parent, File file, UMLDocManager docManager) {
		this.parent = parent;
		this.file = file;
		this.docManager = docManager;

		loadPackageName();
	}


	/**
	 *  Sets a new name for the node. 
	 *
	 *@param  name  The new Name value 
	 */
	public void setName(String name) {
		file = new File(name);
	}


	/**
	 *  Sets a new parent for the node. 
	 *
	 *@param  value  The new Parent value 
	 */
	public void setParent(TParent value) {
		parent = value;
	}


	/**
	 *  Can we add children to this 
	 *
	 *@return    The AllowsChildren value 
	 */
	public boolean getAllowsChildren() {
		return false;
	}


	/**
	 *  Return the child from an index 
	 *
	 *@param  idx  Description of Parameter 
	 *@return      The ChildAt value 
	 */
	public TreeNode getChildAt(int idx) {
		return null;
	}


	/**
	 *  Count the children 
	 *
	 *@return    The ChildCount value 
	 */
	public int getChildCount() {
		return 0;
	}


	/**
	 *  Get the full name of the node, usually composed by walking the TreePath 
	 *  a/b/c etc. 
	 *
	 *@return    The FullName value 
	 */
	public String getFullName() {
		try {
			return file.getCanonicalPath();
		}
		catch (IOException ioe) {
			return file.getPath();
		}
	}


	/**
	 *  Get the icon to show in the tree 
	 *
	 *@param  expanded  Description of Parameter 
	 *@return           The Icon value 
	 */
	public Icon getIcon(boolean expanded) {
		return new UMLIcon();
	}


	/**
	 *  Return the index of a child. This has no children so always returns -1. 
	 *
	 *@param  child  Description of Parameter 
	 *@return        The Index value 
	 */
	public int getIndex(TreeNode child) {
		return -1;
	}


	/**
	 *  Get the name to show in the tree 
	 *
	 *@return    The Name value 
	 */
	public String getName() {
		return packageName + " Class Diagram";
	}


	/**
	 *  Gets the Parent attribute of the UMLLeaf object 
	 *
	 *@return    The Parent value 
	 */
	public TreeNode getParent() {
		return parent;
	}


	/**
	 *  Get the popup menu to show for this node 
	 *
	 *@return    The PopupMenu value 
	 */
	public JPopupMenu getPopupMenu() {
		JPopupMenu result = new JPopupMenu();
		JMenuItem item = new JMenuItem("Open");
		item.addActionListener(new OpenFileAdapter(getFullName()));
		result.add(item);
		return result;
	}


	/**
	 *  Return the tooltip help to be shown when the mouse is over this node. 
	 *
	 *@return    The ToolTipText value 
	 */
	public String getToolTipText() {
		if (packageName.length() > 0) {
			return "The class diagram for the package " + packageName;
		}
		else {
			return "The class diagram for the top level package";
		}
	}


	/**
	 *  Return the model which this node belongs to 
	 *
	 *@return    The TreeModel value 
	 */
	public TModel getTreeModel() {
		return parent.getTreeModel();
	}


	/**
	 *  Get the TreePath which represents this node 
	 *
	 *@return    The TreePath value 
	 */
	public TreePath getTreePath() {
		return parent.getTreePath().pathByAddingChild(this);
	}


	/**
	 *  Gets the Leaf attribute of the UMLLeaf object 
	 *
	 *@return    The Leaf value 
	 */
	public boolean isLeaf() {
		return true;
	}


	/**
	 *  Gets an enumeration of the children 
	 *
	 *@return    An empty enumeration 
	 */
	public Enumeration children() {
		Vector result = new Vector();
		return result.elements();
	}


	/**
	 *  Perform double-click action. Hopefully this will open the file. 
	 */
	public void doDoubleClick() {
		FrameManager.current().open(getFullName());
	}


	/**
	 *  Notify the TreeModel and hence the TreeModel listeners that this node has 
	 *  changed 
	 */
	public void fireChanged() {
	}


	/**
	 *  Sort the children of this node based on the comparator. Since there are 
	 *  no children, this does nothing. 
	 *
	 *@param  c  the comparator 
	 */
	public void sortChildren(SortUtil.Comparator c) {
	}


	/**
	 *  Return the name of the node as its String representation 
	 *
	 *@return    Gets the string representation of this node 
	 */
	public String toString() {
		return getName();
	}


	/**
	 *  Loads the package name from the file 
	 */
	private void loadPackageName() {
		try {
			packageName = "Unknown";

			BufferedReader input = new BufferedReader(new FileReader(file));

			String line = input.readLine();
			if (line.charAt(0) == 'V') {
				StringTokenizer tok = new StringTokenizer(line, "[:]");
				if (tok.hasMoreTokens()) {
					// Skip the first - it is the letter v
					String temp = tok.nextToken();
					if (tok.hasMoreTokens()) {
						// Skip the second - it is the version (1.1)
						temp = tok.nextToken();
						if (tok.hasMoreTokens()) {
							//  Third item is the package name
							packageName = tok.nextToken();
						}
					}
				}
			}

			input.close();
		}
		catch (IOException ioe) {
		}
	}


	/**
	 *  Opens a file when the button is pressed 
	 *
	 *@author    Chris Seguin 
	 */
	private class OpenFileAdapter implements ActionListener {
		private String name;


		/**
		 *  Constructor for the OpenFileAdapter object 
		 *
		 *@param  init  the name of the file 
		 */
		public OpenFileAdapter(String init) {
			name = init;
		}


		/**
		 *  Opens the file 
		 *
		 *@param  evt  the action event 
		 */
		public void actionPerformed(ActionEvent evt) {
			FrameManager.current().open(name);
		}
	}
}
