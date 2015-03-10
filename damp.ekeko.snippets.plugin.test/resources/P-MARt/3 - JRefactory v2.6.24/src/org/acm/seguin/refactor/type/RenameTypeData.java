package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Object responsible for renaming a type 
 *
 *@author     Chris Seguin 
 *@created    September 18, 1999 
 */
public class RenameTypeData {
	//  Instance Variables
	private ASTName oldName;
	private ASTName newName;
	private TypeSummary summary;


	/**
	 *  Constructor for the RenameTypeData object 
	 *
	 *@param  oldValue  The old name 
	 *@param  newValue  The new name 
	 *@param  init      Description of Parameter 
	 */
	public RenameTypeData(ASTName oldValue, ASTName newValue, TypeSummary init) {
		oldName = oldValue;
		newName = newValue;
		summary = init;
	}


	/**
	 *  Constructor for the RenameTypeData object 
	 *
	 *@param  oldValue  The old name 
	 *@param  newValue  The new name 
	 *@param  init      Description of Parameter 
	 */
	public RenameTypeData(String oldValue, String newValue, TypeSummary init) {
		oldName = new ASTName(0);
		oldName.addNamePart(oldValue);
		newName = new ASTName(0);
		newName.addNamePart(newValue);
		summary = init;
	}


	/**
	 *  Return the oldname 
	 *
	 *@return    the old name 
	 */
	public ASTName getOld() {
		return oldName;
	}


	/**
	 *  Return the new name 
	 *
	 *@return    the new name 
	 */
	public ASTName getNew() {
		return newName;
	}


	/**
	 *  Get the type summary associated with the type 
	 *
	 *@return    the type summary 
	 */
	public TypeSummary getTypeSummary() {
		return summary;
	}
}
