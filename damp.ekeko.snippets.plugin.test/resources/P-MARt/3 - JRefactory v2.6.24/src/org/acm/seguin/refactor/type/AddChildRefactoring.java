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

import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Adds a child class and makes sure that the rest of the system is ready to
 *  have a class with that name.
 *
 *@author    Chris Seguin
 */
public class AddChildRefactoring extends AddClassRefactoring
{
	private String packageName;


	/**
	 *  Constructor for the AddChildRefactoring object
	 */
	protected AddChildRefactoring()
	{
		super();
		packageName = null;
	}


	/**
	 *  Sets the name of the child class to be created
	 *
	 *@param  value  the name of the child class
	 */
	public void setChildName(String value)
	{
		setNewClassName(value);
	}


	/**
	 *  Sets the ChildClass attribute of the AddClassRefactoring object
	 *
	 *@param  packageName  The feature to be added to the ChildClass attribute
	 *@param  className    The feature to be added to the ChildClass attribute
	 */
	public void setParentClass(String packageName, String className)
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
	public void setParentClass(TypeSummary summary)
	{
		addTargetClass(summary);
	}


	/**
	 *  Sets the name of the package of the new child class
	 *
	 *@param  value  the package name
	 */
	public void setPackageName(String value)
	{
		packageName = value;
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Adds a child class named " + getNewClassName();
	}


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public int getID()
	{
		return ADD_CHILD;
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
			CreateClass cc = new CreateClass(existingType, className, false);
			if (packageName != null)
			{
				cc.setPackageName(packageName);
			}
			cc.setAbstract(false);
			File newFile = cc.run();
			getComplexTransform().createFile(newFile);
		}
		catch (RefactoringException re)
		{
			System.out.println(re.getMessage());
		}
	}


	/**
	 *  Transforms the original AST. For a child refactoring, we don't need to do
	 *  anything special to the original type.
	 *
	 *@param  typeSummary  the particular type that is being changed
	 */
	protected void transformOriginal(TypeSummary typeSummary)
	{
	}
}
