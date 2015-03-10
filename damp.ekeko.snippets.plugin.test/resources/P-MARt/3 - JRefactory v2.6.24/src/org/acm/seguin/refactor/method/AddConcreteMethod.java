package org.acm.seguin.refactor.method;

import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.summary.MethodSummary;

/**
 *  Adds a concrete method to a class
 *
 *@author    Chris Seguin
 */
public class AddConcreteMethod extends AddNewMethod {
	/**
	 *  Constructor for the AddConcreteMethod object
	 *
	 *@param  init  Description of Parameter
	 */
	public AddConcreteMethod(MethodSummary init) {
		super(init);
	}


	/**
	 *  Sets up the modifiers
	 *
	 *@param  source  the source holder
	 *@param  dest    the destination holder
	 */
	protected void setupModifiers(ModifierHolder source, ModifierHolder dest) {
		super.setupModifiers(source, dest);
		dest.setAbstract(false);
	}


	/**
	 *  Determines if the method is abstract
	 *
	 *@return    true if the method is abstract
	 */
	protected boolean isAbstract() {
		return false;
	}
}
