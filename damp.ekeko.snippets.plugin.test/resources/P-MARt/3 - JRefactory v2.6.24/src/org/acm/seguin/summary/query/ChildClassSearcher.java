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

import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Searches the set of summaries for all the classes that extend a particular 
 *  class. 
 *
 *@author    Chris Seguin 
 */
public class ChildClassSearcher extends TraversalVisitor {
	/**
	 *  Visit a file summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result public Object visit(FileSummary node, Object data) 
	 *      { if (node.getFile() == null) { return data; } return 
	 *      super.visit(node, data); } 
	 */

	/**
	 *  Visit a file summary. 
	 *
	 *  Visit a type summary. 
	 *
	 *@param  node  the summary that we are visiting 
	 *@param  data  the data that was passed in 
	 *@return       the result public Object visit(FileSummary node, Object data) 
	 *      { if (node.getFile() == null) { return data; } return 
	 *      super.visit(node, data); } 
	 */
	public Object visit(TypeSummary node, Object data) {
		SearchData search = (SearchData) data;

		TypeDeclSummary parentDecl = node.getParentClass();
		if (parentDecl == null) {
			return data;
		}
		TypeSummary parentTypeSummary = GetTypeSummary.query(parentDecl);

		if (parentTypeSummary == search.getParentType()) {
			search.addChild(node);
		}

		//  Return some value
		return data;
	}


	/**
	 *  Generates a list of classes that extend type 
	 *
	 *@param  type  the desired parent class 
	 *@return       an iterator of type summaries 
	 */
	public static Iterator query(TypeSummary type) {
		SearchData search = new SearchData(type);
		(new ChildClassSearcher()).visit(search);
		return search.getChildren();
	}
}
