/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.summary.TypeSummary;

/**
 *  Provides search data fro the ChildClassSearcher. Contains the type we are 
 *  searching for and the children that are found. 
 *
 *@author    Chris Seguin 
 */
class SearchData {
	private TypeSummary desiredParent;
	private LinkedList children;


	/**
	 *  Constructor for the SearchData object 
	 *
	 *@param  init  Description of Parameter 
	 */
	SearchData(TypeSummary init) {
		desiredParent = init;
		children = new LinkedList();
	}


	/**
	 *  Gets the ParentType attribute of the SearchData object 
	 *
	 *@return    The ParentType value 
	 */
	TypeSummary getParentType() {
		return desiredParent;
	}


	/**
	 *  Gets the Children attribute of the SearchData object 
	 *
	 *@return    The Children value 
	 */
	Iterator getChildren() {
		return children.iterator();
	}


	/**
	 *  Adds a feature to the Child attribute of the SearchData object 
	 *
	 *@param  newChild  The feature to be added to the Child attribute 
	 */
	void addChild(TypeSummary newChild) {
		children.add(newChild);
	}
}
