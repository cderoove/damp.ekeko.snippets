package org.acm.seguin.parser.query;

import org.acm.seguin.parser.Node;

/**
 *  Description of where the search ended.  The results
 *  include the node where the search found the parse
 *  tree and the index into the children of that node.
 *
 *@author    Chris Seguin
 */
public class Found {
	private Node root;
	private int index;


	/**
	 *  Constructor for the Found object
	 *
	 *@param  initRoot   Description of Parameter
	 *@param  initIndex  Description of Parameter
	 */
	public Found(Node initRoot, int initIndex) {
		root = initRoot;
		index = initIndex;
	}


	/**
	 *  Gets the Index attribute of the Found object
	 *
	 *@return    The Index value
	 */
	public int getIndex() {
		return index;
	}


	/**
	 *  Gets the Root attribute of the Found object
	 *
	 *@return    The Root value
	 */
	public Node getRoot() {
		return root;
	}
}
