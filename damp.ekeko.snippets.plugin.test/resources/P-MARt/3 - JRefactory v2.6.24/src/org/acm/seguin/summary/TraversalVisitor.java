/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *  All items that want to visit a summary tree should implement this
 *  interface.
 *
 *@author     Chris Seguin
 *@created    May 15, 1999
 */
public class TraversalVisitor implements SummaryVisitor {
	/**
	 *  Visit a summary node. This is the default method.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(Summary node, Object data) {
		//  Shouldn't have to do anything about one of these nodes
		return data;
	}


	/**
	 *  Visit all nodes.
	 *
	 *@param  data  the data that was passed in
	 */
	public void visit(Object data) {
		Iterator iter = PackageSummary.getAllPackages();
		if (iter != null) {
			//  Create a temporary list to avoid concurrant modification problems
			LinkedList list = new LinkedList();
			while (iter.hasNext()) {
				list.add(iter.next());
			}

			iter = list.iterator();
			while (iter.hasNext()) {
				PackageSummary summary = (PackageSummary) iter.next();
				summary.accept(this, data);
			}
		}
	}


	/**
	 *  Visit a package summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(PackageSummary node, Object data) {
		Iterator iter = node.getFileSummaries();

		if (iter != null) {
			while (iter.hasNext()) {
				FileSummary next = (FileSummary) iter.next();
				next.accept(this, data);
			}
		}

		return data;
	}


	/**
	 *  Visit a file summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FileSummary node, Object data) {
		//  Over the imports
		Iterator iter = node.getImports();

		if (iter != null) {
			while (iter.hasNext()) {
				ImportSummary next = (ImportSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Over the types
		iter = node.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Return some value
		return data;
	}


	/**
	 *  Visit a import summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ImportSummary node, Object data) {
		//  No children so just return
		return data;
	}


	/**
	 *  Visit a type summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeSummary node, Object data) {
		//  Over the fields
		Iterator iter = node.getFields();
		if (iter != null) {
			while (iter.hasNext()) {
				FieldSummary next = (FieldSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Over the methods
		iter = node.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Over the types
		iter = node.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Return some value
		return data;
	}


	/**
	 *  Visit a method summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MethodSummary node, Object data) {
		//  First visit the return type
		if (node.getReturnType() != null) {
			node.getReturnType().accept(this, data);
		}

		//  Then visit the parameter types
		Iterator iter = node.getParameters();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				next.accept(this, data);
			}
		}

		//  Third visit the exceptions
		iter = node.getExceptions();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				next.accept(this, data);
			}
		}

		//  Finally visit the dependencies
		iter = node.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				next.accept(this, data);
			}
		}

		return data;
	}


	/**
	 *  Visit a field summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldSummary node, Object data) {
		node.getTypeDecl().accept(this, data);
		return data;
	}


	/**
	 *  Visit a parameter summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ParameterSummary node, Object data) {
		node.getTypeDecl().accept(this, data);
		return data;
	}


	/**
	 *  Visit a local variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(LocalVariableSummary node, Object data) {
		node.getTypeDecl().accept(this, data);
		return data;
	}


	/**
	 *  Visit a variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(VariableSummary node, Object data) {
		node.getTypeDecl().accept(this, data);
		return data;
	}


	/**
	 *  Visit a type declaration summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeDeclSummary node, Object data) {
		//  This is a leaf.  It does not contain any summary objects
		return data;
	}


	/**
	 *  Visit a message send summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MessageSendSummary node, Object data) {
		TypeDeclSummary typeDecl = node.getTypeDecl();
		if (typeDecl != null) {
			typeDecl.accept(this, data);
		}
		return data;
	}


	/**
	 *  Visit a field access summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldAccessSummary node, Object data) {
		TypeDeclSummary typeDecl = node.getTypeDecl();
		if (typeDecl != null) {
			typeDecl.accept(this, data);
		}
		return data;
	}
}
