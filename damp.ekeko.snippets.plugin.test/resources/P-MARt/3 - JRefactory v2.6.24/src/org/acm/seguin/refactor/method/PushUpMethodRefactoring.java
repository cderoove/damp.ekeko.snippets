/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Pushes up a method into a parent class
 *
 *@author     Chris Seguin
 *@created    April 5, 2000
 */
public class PushUpMethodRefactoring extends InheretenceMethodRefactoring
{
	private TypeSummary typeSummary;
	private TypeSummary parentType;


	/**
	 *  Constructor for the PushUpMethodRefactoring object
	 */
	protected PushUpMethodRefactoring()
	{
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Moves a method " + methodSummary.getName() +
				" down into the parent class " + parentType.getName();
	}


	/**
	 *  Gets the ID attribute of the PushUpMethodRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return PUSH_UP_METHOD;
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
		TypeDeclSummary parent = typeSummary.getParentClass();
		parentType = GetTypeSummary.query(parent);

		checkDestination(parentType);
		checkDestinationFile(parentType, "Can't push up a method into source code that you don't have");

		NearMissVisitor nmv = new NearMissVisitor(parentType,
				methodSummary, typeSummary);
		nmv.visit(null);

		if (nmv.getProblem() != null)
		{
			throw new RefactoringException("Method with a signature of " +
					nmv.getProblem().toString() + " found in child of " +
					parentType.getName());
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

		addMethodToDest(transform, rft, methodDecl, parentType);

		//  Remove the method from all child classes
		(new RemoveMethodFromSubclassVisitor(parentType,
				methodSummary, typeSummary, transform)).visit(null);
	}
}
