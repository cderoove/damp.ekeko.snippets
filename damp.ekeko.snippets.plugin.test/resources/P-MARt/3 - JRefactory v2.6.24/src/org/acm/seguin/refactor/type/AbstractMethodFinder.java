/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.type;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  This class searches the type heirarchy looking for abstract methods that
 *  have not yet been instantiated. Abstract methods are gathered into a
 *  linked list. <P>
 *
 *  This object is used by CreateClass to build a list of abstract methods
 *  that the user will have to overload.
 *
 *@author    Chris Seguin
 */
public class AbstractMethodFinder {
	private LinkedList list;
	private TypeSummary leaf;


	/**
	 *  Constructor for the AbstractMethodFinder object
	 *
	 *@param  init  the type summary
	 */
	public AbstractMethodFinder(TypeSummary init)
	{
		leaf = init;
		list = new LinkedList();
		load();
	}


	/**
	 *  Constructor for the AbstractMethodFinder object used for unit testing
	 *
	 *@param  init     the type summary
	 *@param  testing  dummy variable
	 */
	AbstractMethodFinder(TypeSummary init, boolean testing)
	{
		leaf = init;
		list = new LinkedList();
	}


	/**
	 *  Gets the List attribute of the AbstractMethodFinder object
	 *
	 *@return    The List value
	 */
	public LinkedList getList()
	{
		return list;
	}


	/**
	 *  Loads the interface methods
	 */
	void loadInterfaceMethods()
	{
		TypeSummary current = leaf;
		while (current != null) {
			//  Get the interfaces
			Iterator iter = current.getImplementedInterfaces();
			if (iter != null) {
				while (iter.hasNext()) {
					TypeDeclSummary nextDecl = (TypeDeclSummary) iter.next();
					TypeSummary nextType = GetTypeSummary.query(nextDecl);
					loadInterface(nextType);
				}
			}

			current = nextType(current);
		}
	}


	/**
	 *  Removes those methods which have been instantiated (and adds abstract
	 *  methods). Uses recursive processing to traverse through the parent
	 *  classes first.
	 *
	 *@param  current  The current type summary
	 */
	void filter(TypeSummary current)
	{
		if (current == null) {
			return;
		}

		//  Visit parent first
		TypeSummary next = nextType(current);
		filter(next);

		//  Consider each method of the current
		Iterator iter = current.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary nextMethod = (MethodSummary) iter.next();
				if (nextMethod.getModifiers().isAbstract()) {
					add(nextMethod);
				}
				else {
					removeImplementations(nextMethod);
				}
			}
		}
	}


	/**
	 *  Loads the interface methods and then filters out those that have been
	 *  instatiated
	 */
	private void load()
	{
		loadInterfaceMethods();
		filter(leaf);
	}


	/**
	 *  Loads a particular interface type summary
	 *
	 *@param  type  the interface
	 */
	private void loadInterface(TypeSummary type)
	{
		if (type == null) {
			return;
		}

		Iterator iter = type.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				add((MethodSummary) iter.next());
			}
		}

		iter = type.getImplementedInterfaces();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeDeclSummary nextDecl = (TypeDeclSummary) iter.next();
				TypeSummary nextType = GetTypeSummary.query(nextDecl);
				loadInterface(nextType);
			}
		}
	}


	/**
	 *  Gets the next type - finds the parent class type summary
	 *
	 *@param  current  the current type summary
	 *@return          the parent type summary
	 */
	private TypeSummary nextType(TypeSummary current)
	{
		return GetTypeSummary.query(current.getParentClass());
	}


	/**
	 *  Removes the implementation of a particular method from the linked list
	 *
	 *@param  methodSummary  the method whose signature we can remove
	 */
	private void removeImplementations(MethodSummary methodSummary)
	{
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			if (methodSummary.checkSignature(next)) {
				iter.remove();
			}
		}
	}


	/**
	 *  Adds a method summary
	 *
	 *@param  methodSummary  the method whose signature we want to add
	 */
	private void add(MethodSummary methodSummary)
	{
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			if (methodSummary.checkSignature(next)) {
				return;
			}
		}

		list.add(methodSummary);
	}
}
