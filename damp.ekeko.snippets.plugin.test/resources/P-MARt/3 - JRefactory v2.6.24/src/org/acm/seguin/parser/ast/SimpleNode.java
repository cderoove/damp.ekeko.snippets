/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.ast;

import java.util.Vector;

import org.acm.seguin.parser.JavaParser;
import org.acm.seguin.parser.JavaParserTreeConstants;
import org.acm.seguin.parser.JavaParserVisitor;
import org.acm.seguin.parser.NamedToken;
import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.Token;

/**
 *  This object is the base class for all items in the AST (abstract syntax
 *  tree).
 *
 *@author     Chris Seguin
 *@created    May 10, 1999
 */
public class SimpleNode implements Node {
	//  Instance Variables
	/**
	 *  Description of the Field
	 */
	protected Node parent;
	/**
	 *  Description of the Field
	 */
	protected Node[] children;
	/**
	 *  Description of the Field
	 */
	protected int id;
	/**
	 *  Description of the Field
	 */
	protected JavaParser parser;
	/**
	 *  Description of the Field
	 */
	protected Vector specials;


	/**
	 *  Constructor for the SimpleNode object
	 *
	 *@param  i  Description of Parameter
	 */
	public SimpleNode(int i) {
		id = i;
		specials = null;
	}


	/**
	 *  Constructor for the SimpleNode object
	 *
	 *@param  p  Description of Parameter
	 *@param  i  Description of Parameter
	 */
	public SimpleNode(JavaParser p, int i) {
		this(i);
		parser = p;
	}


	/**
	 *  Return the id for this node
	 *
	 *@return    the id
	 */
	public int getID() {
		return id;
	}


	/**
	 *  Gets the special associated with a particular key
	 *
	 *@param  key  the key
	 *@return      the value
	 */
	public Token getSpecial(String key) {
		if ((specials == null) || (key == null)) {
			return null;
		}

		int last = specials.size();
		for (int ndx = 0; ndx < last; ndx++) {
			NamedToken named = (NamedToken) specials.elementAt(ndx);
			if (named.check(key)) {
				return named.getToken();
			}
		}

		return null;
	}


	/**
	 *  Description of the Method
	 */
	public void jjtOpen() {
	}


	/**
	 *  Description of the Method
	 */
	public void jjtClose() {
	}


	/**
	 *  Description of the Method
	 *
	 *@param  n  Description of Parameter
	 */
	public void jjtSetParent(Node n) {
		parent = n;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public Node jjtGetParent() {
		return parent;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  n  Description of Parameter
	 *@param  i  Description of Parameter
	 */
	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		}
		else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
		n.jjtSetParent(this);
	}


	/**
	 *  Insert the node numbered i
	 *
	 *@param  i  The index of the node to remove
	 *@param  n  Description of Parameter
	 */
	public void jjtInsertChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		}
		else {
			Node c[] = new Node[Math.max(children.length + 1, i + 1)];
			System.arraycopy(children, 0, c, 0, i);
			System.arraycopy(children, i, c, i + 1, children.length - i);
			children = c;
		}

		//  Store the node
		children[i] = n;
		n.jjtSetParent(this);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  i  Description of Parameter
	 *@return    Description of the Returned Value
	 */
	public Node jjtGetChild(int i) {
		return children[i];
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean hasAnyChildren() {
		return ((children != null) && (children.length > 0));
	}


	/**
	 *  Remove the node numbered i
	 *
	 *@param  i  The index of the node to remove
	 */
	public void jjtDeleteChild(int i) {
		if ((children == null) || (children.length < i) || (i < 0)) {
			System.out.println("Skipping this delete operation");
		}
		else {
			Node c[] = new Node[children.length - 1];
			System.arraycopy(children, 0, c, 0, i);
			System.arraycopy(children, i + 1, c, i, children.length - i - 1);
			children = c;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  key    Description of Parameter
	 *@param  value  Description of Parameter
	 */
	public void addSpecial(String key, Token value) {
		if (value == null) {
			return;
		}

		if (specials == null) {
			init();
		}

		specials.addElement(new NamedToken(key, value));
	}


	/**
	 *  Removes a special associated with a key
	 *
	 *@param  key  the special to remove
	 */
	public void removeSpecial(String key) {
		if ((specials == null) || (key == null)) {
			return;
		}

		int last = specials.size();
		for (int ndx = 0; ndx < last; ndx++) {
			NamedToken named = (NamedToken) specials.elementAt(ndx);
			if (named.check(key)) {
				specials.removeElementAt(ndx);
				return;
			}
		}
	}


	/**
	 *  Accept the visitor.
	 *
	 *@param  visitor  Description of Parameter
	 *@param  data     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}


	/**
	 *  Accept the visitor.
	 *
	 *@param  visitor  Description of Parameter
	 *@param  data     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	public Object childrenAccept(JavaParserVisitor visitor, Object data) {
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				children[i].jjtAccept(visitor, data);
			}
		}
		return data;
	}


	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped.  If
	 * your output uses more than one line you should override
	 * toString(String), otherwise overriding toString() is probably all
	 * you need to do.
	 */

	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public String toString() {
		return JavaParserTreeConstants.jjtNodeName[id];
	}


	/**
	 *  Description of the Method
	 *
	 *@param  prefix  Description of Parameter
	 *@return         Description of the Returned Value
	 */
	public String toString(String prefix) {
		return prefix + toString();
	}


	/*
	 * Override this method if you want to customize how the node dumps
	 * out its children.
	 */

	/**
	 *  Description of the Method
	 *
	 *@param  prefix  Description of Parameter
	 */
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName());
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}


	/**
	 *  Initializes any variables that are not required
	 */
	protected void init() {
		if (specials == null) {
			specials = new Vector();
		}
	}

	/** Is javadoc required? */
	public boolean isRequired() {
		return false;
	}
}
