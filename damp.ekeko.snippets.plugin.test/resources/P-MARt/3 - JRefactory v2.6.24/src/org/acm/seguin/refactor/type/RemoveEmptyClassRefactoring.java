/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.type;

import java.io.File;
import java.io.IOException;

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.summary.query.TopLevelDirectory;

/**
 *  Removes a particular class that is an abstract parent.
 *
 *@author    Chris Seguin
 */
public class RemoveEmptyClassRefactoring extends Refactoring {
	private TypeSummary typeSummary;
	private File base;


	/**
	 *  Constructor for the RemoveEmptyClassRefactoring object
	 */
	protected RemoveEmptyClassRefactoring() { }


	/**
	 *  Sets the ChildClass attribute of the RemoveAbstractParent object
	 *
	 *@param  packageName  The new Class value
	 *@param  className    The new Class value
	 */
	public void setClass(String packageName, String className)
	{
		setClass(GetTypeSummary.query(
				PackageSummary.getPackageSummary(packageName),
				className));
	}


	/**
	 *  Sets the ChildClass attribute of the RemoveAbstractParent object
	 *
	 *@param  summary  The new Class value
	 */
	public void setClass(TypeSummary summary)
	{
		typeSummary = summary;
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Removes the class named " + typeSummary.getName();
	}


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public int getID()
	{
		return REMOVE_CLASS;
	}


	/**
	 *  Gets the FileSummary attribute of the RemoveEmptyClassRefactoring object
	 *
	 *@return    The FileSummary value
	 */
	protected FileSummary getFileSummary()
	{
		FileSummary fileSummary = (FileSummary) typeSummary.getParent();
		return fileSummary;
	}


	/**
	 *  Checks the preconditions that must be true for this refactoring to be
	 *  applied.
	 *
	 *@exception  RefactoringException  The exception
	 */
	protected void preconditions() throws RefactoringException
	{
		if (typeSummary == null) {
			throw new RefactoringException("No type specified");
		}

		TypeDeclSummary parentDecl = typeSummary.getParentClass();
		TypeSummary parentSummary;
		if (parentDecl == null) {
			parentSummary = GetTypeSummary.query(
					PackageSummary.getPackageSummary("java.lang"),
					"Object");
		}
		else {
			parentSummary = GetTypeSummary.query(parentDecl);
		}

		if (parentSummary == null) {
			throw new RefactoringException("Could not find the parent class for the specified class in the metadata");
		}
		FileSummary fileSummary = getFileSummary();

		if (fileSummary.getFile() == null) {
			throw new RefactoringException("This type is contained in a stub.  No refactorings allowed.");
		}

		if (fileSummary.getTypeCount() != 1) {
			throw new RefactoringException("This refactoring works only when the " +
					"type is alone in a file.  Please remove other types from " +
					fileSummary.getFile().getName());
		}

		if ((typeSummary.getFieldCount() > 0) || (typeSummary.getMethodCount() > 0)) {
			throw new RefactoringException("The " + typeSummary.getName() + " class has at least one method or field");
		}

		//  Finish the setup
		File deadFile = fileSummary.getFile();
		String path = null;
		try {
			path = deadFile.getCanonicalPath();
		}
		catch (IOException ioe) {
			path = deadFile.getPath();
		}
		File startDir = (new File(path)).getParentFile();
		String firstFilename = deadFile.getName();
		base = TopLevelDirectory.query(startDir, firstFilename);
	}


	/**
	 *  Performs the refactoring by traversing through the files and updating
	 *  them.
	 */
	protected void transform()
	{
		ComplexTransform complex = getComplexTransform();
		FileSummary fileSummary = getFileSummary();
		complex.removeFile(fileSummary.getFile());

		String srcPackage = ((PackageSummary) fileSummary.getParent()).getName();
		String oldClassName = typeSummary.getName();
		String newClassName;

		TypeDeclSummary parent = typeSummary.getParentClass();
		String destPackage;
		if (parent == null) {
			newClassName = "Object";
			destPackage = "java.lang";
		}
		else {
			newClassName = parent.getType();
			TypeSummary parentTypeSummary = GetTypeSummary.query(parent);
			Summary one = parentTypeSummary.getParent();
			while (!(one instanceof PackageSummary)) {
				one = one.getParent();
			}
			destPackage = ((PackageSummary) one).getName();
		}

		FileSummary.removeFileSummary(fileSummary.getFile());

		RemoveClassVisitor rcv = new RemoveClassVisitor(
				srcPackage, oldClassName,
				destPackage, newClassName,
				base, complex);
		rcv.visit(null);
	}
}
