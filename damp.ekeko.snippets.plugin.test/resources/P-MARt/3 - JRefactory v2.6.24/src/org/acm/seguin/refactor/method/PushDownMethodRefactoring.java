/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Performs the push down method refactoring
 *
 *@author    Chris Seguin
 */
public class PushDownMethodRefactoring extends InheretenceMethodRefactoring
{
	private LinkedList destinations;
	private TypeSummary typeSummary;


	/**
	 *  Constructor for the PushDownMethodRefactoring object
	 */
	protected PushDownMethodRefactoring()
	{
		destinations = new LinkedList();
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Moves a method " + methodSummary.getName() +
				" down into the child classes of " + typeSummary.getName();
	}


	/**
	 *  Gets the ID attribute of the PushDownMethodRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return PUSH_DOWN_METHOD;
	}


	/**
	 *  Adds a feature to the Child attribute of the PushDownMethodRefactoring
	 *  object
	 *
	 *@param  type  The feature to be added to the Child attribute
	 */
	public void addChild(TypeSummary type)
	{
		destinations.add(type);
	}



	/**
	 *  This specifies the preconditions for applying the refactoring
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void preconditions() throws RefactoringException
	{
		if (methodSummary == null)
		{
			throw new RefactoringException("No method specified");
		}

		typeSummary = (TypeSummary) methodSummary.getParent();

		Iterator iter = destinations.iterator();
		while (iter.hasNext())
		{
			TypeSummary next = (TypeSummary) iter.next();
			TypeDeclSummary parent = next.getParentClass();
			TypeSummary parentSummary = GetTypeSummary.query(parent);

			if (parentSummary != typeSummary)
			{
				throw new RefactoringException(next.getName() + " is not a subclass of " + typeSummary.getName());
			}

			checkDestination(next);
		}
	}


	/**
	 *  Moves the method to the parent class
	 */
	protected void transform()
	{
		RemoveMethodTransform rft = new RemoveMethodTransform(methodSummary);
		ComplexTransform transform = getComplexTransform();

		removeMethod(typeSummary, transform, rft);

		//  Update the method declaration to have the proper permissions
		SimpleNode methodDecl = rft.getMethodDeclaration();
		if (methodDecl == null)
		{
			return;
		}

		ASTMethodDeclaration decl = updateMethod(methodDecl);

		Iterator iter = destinations.iterator();
		while (iter.hasNext())
		{
			TypeSummary next = (TypeSummary) iter.next();
			addMethodToDest(transform, rft, methodDecl, next);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  source     Description of Parameter
	 *@param  transform  Description of Parameter
	 *@param  rft        Description of Parameter
	 */
	protected void removeMethod(TypeSummary source, ComplexTransform transform, RemoveMethodTransform rft)
	{
		transform.add(new AddAbstractMethod(methodSummary));
		super.removeMethod(source, transform, rft);
	}
}
