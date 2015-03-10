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

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Adds an abstract parent to a class or a set of classes in the same
 *  package. The parent class that is created must be in the same package as
 *  the child classes that extend it. It is created with the package level
 *  scope, to minimize interactions elsewhere in the system. However, the
 *  other source files that import this class are proactively updated to
 *  minimize the name conflicts in the event that the user later wants to make
 *  the abstract class a public class.
 *
 *@author    Chris Seguin
 */
public class AddAbstractParent extends AddClassRefactoring
{
	/**
	 *  Constructor for the AddAbstractParent object
	 */
	protected AddAbstractParent()
	{
		super();
	}


	/**
	 *  Sets the ParentName attribute of the AddAbstractParent object
	 *
	 *@param  parent  The new ParentName value
	 */
	public void setParentName(String parent)
	{
		setNewClassName(parent);
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Adds a parent class named " + getNewClassName();
	}


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public int getID()
	{
		return ADD_PARENT;
	}


	/**
	 *  Sets the ChildClass attribute of the AddClassRefactoring object
	 *
	 *@param  packageName  The feature to be added to the ChildClass attribute
	 *@param  className    The feature to be added to the ChildClass attribute
	 */
	public void addChildClass(String packageName, String className)
	{
		addTargetClass(GetTypeSummary.query(
				PackageSummary.getPackageSummary(packageName),
				className));
	}


	/**
	 *  Sets the ChildClass attribute of the AddClassRefactoring object
	 *
	 *@param  summary  The feature to be added to the ChildClass attribute
	 */
	public void addChildClass(TypeSummary summary)
	{
		addTargetClass(summary);
	}


	/**
	 *  Creates a class
	 *
	 *@param  existingType  the existing type
	 *@param  className     the name of the new class
	 */
	protected void createClass(TypeSummary existingType, String className)
	{
		try
		{
			CreateClass cc = new CreateClass(existingType, className, true);
			File newFile = cc.run();
			getComplexTransform().createFile(newFile);
		}
		catch (RefactoringException re)
		{
			System.out.println(re.getMessage());
		}
	}


	/**
	 *  Transforms the original AST
	 *
	 *@param  typeSummary  the particular type that is being changed
	 */
	protected void transformOriginal(TypeSummary typeSummary)
	{
		FileSummary fileSummary = (FileSummary) typeSummary.getParent();
		File file = fileSummary.getFile();
		PackageSummary packageSummary = (PackageSummary) fileSummary.getParent();
		ComplexTransform ref = getComplexTransform();
		ref.add(createRenameType(typeSummary, packageSummary));
		ref.apply(file, file);
	}


	/**
	 *  Creates a rename parent type transformation
	 *
	 *@param  typeSummary     the type to be changed
	 *@param  packageSummary  the package to be changed
	 *@return                 the transform
	 */
	RenameParentTypeTransform createRenameType(TypeSummary typeSummary, PackageSummary packageSummary)
	{
		RenameParentTypeTransform rptt = new RenameParentTypeTransform();

		rptt.setNewName(getNewClassName());
		rptt.setOldName(typeSummary.getName());

		return rptt;
	}
}
