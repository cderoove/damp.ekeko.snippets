/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import java.util.Iterator;

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;

/**
 *  Renames a field
 *
 *@author    Chris Seguin
 */
public class RenameFieldRefactoring extends FieldRefactoring {
	private String newName;
	private FieldSummary oldField;


	/**
	 *  Constructor for the RenameFieldRefactoring object
	 */
	public RenameFieldRefactoring()
	{
		super();
	}


	/**
	 *  Sets the NewName attribute of the RenameFieldRefactoring object
	 *
	 *@param  value  The new NewName value
	 */
	public void setNewName(String value)
	{
		newName = value;
	}


	/**
	 *  Gets the Description attribute of the RenameFieldRefactoring object
	 *
	 *@return    The Description value
	 */
	public String getDescription()
	{
		return "Renames the field " + field +
				" to " + newName;
	}


	/**
	 *  Gets the ID attribute of the RenameFieldRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return RENAME_FIELD;
	}


	/**
	 *  Check that thsi refactoring can be performed
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void preconditions() throws RefactoringException
	{
		Iterator iter = typeSummary.getFields();
		if (iter == null) {
			throw new RefactoringException(typeSummary.getName() + " has no fields associated with it, so it cannot be renamed");
		}

		boolean found = false;
		while (iter.hasNext()) {
			FieldSummary next = (FieldSummary) iter.next();
			if (next.getName().equals(field)) {
				found = true;
				oldField = next;
			}
			if (next.getName().equals(newName)) {
				throw new RefactoringException("A field named " + newName + " already exists in class " + typeSummary.getName());
			}
		}

		if (!found) {
			throw new RefactoringException("No field named " + field + " is contained in " + typeSummary.getName());
		}
	}


	/**
	 *  Applies the transformation to the system to rename the method
	 */
	protected void transform()
	{
		FileSummary fileSummary = (FileSummary) getFileSummary(typeSummary);
		RenameFieldTransform rft = new RenameFieldTransform(oldField, newName);
		ComplexTransform transform = getComplexTransform();
		transform.add(rft);
		transform.apply(fileSummary.getFile(), fileSummary.getFile());

		if (oldField.getModifiers().isPrivate()) {
			//  We are done
		}
		else if (oldField.getModifiers().isPackage()) {
			RenameSystemTraversal rsv = new RenameSystemTraversal();
			rsv.visit(getPackage(), new RenameFieldData(oldField, newName, transform));
		}
		else {
			RenameSystemTraversal rsv = new RenameSystemTraversal();
			rsv.visit(new RenameFieldData(oldField, newName, transform));
		}
	}


	/**
	 *  Gets the Package attribute of the RenameFieldRefactoring object
	 *
	 *@return    The Package value
	 */
	private PackageSummary getPackage()
	{
		Summary current = oldField;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}

		return (PackageSummary) current;
	}
}
