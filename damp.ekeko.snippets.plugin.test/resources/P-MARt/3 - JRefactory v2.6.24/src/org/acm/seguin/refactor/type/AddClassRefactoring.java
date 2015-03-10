package org.acm.seguin.refactor.type;

import java.util.Enumeration;
import java.util.Vector;

import org.acm.seguin.refactor.EliminatePackageImportVisitor;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Adds a class that is either a parent or a child of an existing class. 
 *
 *@author    Chris Seguin 
 */
public abstract class AddClassRefactoring extends Refactoring {
	private Vector summaryList;
	private String className = null;


	/**
	 *  Constructor for the AddClassRefactoring object 
	 */
	public AddClassRefactoring() {
		summaryList = new Vector();
	}


	/**
	 *  Sets the name of the new class 
	 *
	 *@param  value  the name of the new class 
	 */
	protected void setNewClassName(String value) {
		className = value;
	}


	/**
	 *  Gets the name of the new class 
	 *
	 *@return    the name 
	 */
	protected String getNewClassName() {
		return className;
	}


	/**
	 *  Adds a target class - either the parent or the child, depending on what 
	 *  we are adding 
	 *
	 *@param  summary  the summary to be extended 
	 */
	protected void addTargetClass(TypeSummary summary) {
		if (summary != null) {
			summaryList.addElement(summary);
		}
	}


	/**
	 *  Describes the preconditions that must be true for this refactoring to be 
	 *  applied 
	 *
	 *@exception  RefactoringException  thrown if one or more of the 
	 *      preconditions is not satisfied. The text of the exception provides a 
	 *      hint of what went wrong. 
	 */
	protected void preconditions() throws RefactoringException {
		if (summaryList.size() == 0) {
			throw new RefactoringException("Unable to find type to extend");
		}

		if (className == null) {
			throw new RefactoringException("New class name is not specified");
		}

		//  Get the package
		PackageSummary summary = getPackageSummary((Summary) summaryList.elementAt(0));
		TypeSummary abstractParent = GetTypeSummary.query(summary, className);
		if (abstractParent != null) {
			throw new RefactoringException("Type with that name already exists");
		}

		TypeSummary anySummary = 
				(TypeSummary) summaryList.elementAt(0);
		TypeSummary originalParent = 
				GetTypeSummary.query(anySummary.getParentClass());
		Enumeration enum_ = summaryList.elements();
		while (enum_.hasMoreElements()) {
			TypeSummary typeSummary = (TypeSummary) enum_.nextElement();
			if (typeSummary.isInterface()) {
				throw new RefactoringException("This refactoring only works for classes, not interfaces");
			}

			FileSummary fileSummary = (FileSummary) typeSummary.getParent();
			TypeSummary referenced = GetTypeSummary.query(fileSummary, className);
			if (referenced != null) {
				throw new RefactoringException("New class already exists relative to " + typeSummary.getName());
			}

			if (!isSameParent(originalParent, 
					GetTypeSummary.query(typeSummary.getParentClass()))) {
				throw new RefactoringException("Existing types don't share the same original parent");
			}
		}
	}


	/**
	 *  Performs the transform on the rest of the classes 
	 */
	protected void transform() {
		Enumeration enum_ = summaryList.elements();
		while (enum_.hasMoreElements()) {
			TypeSummary typeSummary = (TypeSummary) enum_.nextElement();
			transformOriginal(typeSummary);
		}

		createClass((TypeSummary) summaryList.elementAt(0), className);

		EliminatePackageImportVisitor epiv = new EliminatePackageImportVisitor(getComplexTransform());
		epiv.setPackageSummary(getPackageSummary((Summary) summaryList.elementAt(0)));
		epiv.visit(null);
	}


	/**
	 *  Creates a class 
	 *
	 *@param  existingType  the existing type 
	 *@param  className     the name of the new class 
	 */
	protected abstract void createClass(TypeSummary existingType, String className);


	/**
	 *  Transforms the original AST 
	 *
	 *@param  typeSummary  the particular type that is being changed 
	 */
	protected abstract void transformOriginal(TypeSummary typeSummary);


	/**
	 *  Gets the package summary 
	 *
	 *@param  base  Description of Parameter 
	 *@return       the package summary 
	 */
	private PackageSummary getPackageSummary(Summary base) {
		Summary current = base;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		return (PackageSummary) current;
	}


	/**
	 *  Gets the SameParent attribute of the AddClassRefactoring object 
	 *
	 *@param  one  Description of Parameter 
	 *@param  two  Description of Parameter 
	 *@return      The SameParent value 
	 */
	private boolean isSameParent(TypeSummary one, TypeSummary two) {
		if (isObject(one)) {
			return isObject(two);
		}

		if (isObject(two)) {
			return false;
		}

		return one.equals(two);
	}


	/**
	 *  Gets the Object attribute of the AddClassRefactoring object 
	 *
	 *@param  item  Description of Parameter 
	 *@return       The Object value 
	 */
	private boolean isObject(TypeSummary item) {
		if (item == null) {
			return true;
		}

		if (item.getName().equals("Object")) {
			return true;
		}

		return false;
	}
}
