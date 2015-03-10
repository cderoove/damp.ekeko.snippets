package org.acm.seguin.refactor.method;

import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.SameMethod;

/**
 *  Moves a method between levels in the inheretence hierarchy
 *
 *@author    Chris Seguin
 */
abstract class InheretenceMethodRefactoring extends MethodRefactoring {
	/**
	 *  the method that is being moved around
	 */
	protected MethodSummary methodSummary = null;


	/**
	 *  Sets the Method attribute of the PushUpMethodRefactoring object
	 *
	 *@param  value  The new Method value
	 */
	public void setMethod(MethodSummary value) {
		methodSummary = value;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  dest                      Description of Parameter
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void checkDestination(TypeSummary dest) throws RefactoringException {
		MethodSummary alternate = SameMethod.find(dest, methodSummary);
		if (alternate != null) {
			ModifierHolder holder = alternate.getModifiers();
			if (!holder.isAbstract()) {
				throw new RefactoringException("A method with the same signature (name and parameter types) already exists in the " + dest.getName() + " class");
			}
		}
		else if (SameMethod.findConflict(dest, methodSummary) != null) {
			throw new RefactoringException("A method with the conflicting signature (name and parameter types) already exists in the " + dest.getName() + " class");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  methodDecl  Description of Parameter
	 *@return             Description of the Returned Value
	 */
	protected ASTMethodDeclaration updateMethod(SimpleNode methodDecl) {
		ASTMethodDeclaration decl = (ASTMethodDeclaration) methodDecl.jjtGetChild(0);
		ModifierHolder holder = decl.getModifiers();
		if (!holder.isPublic()) {
			holder.setPrivate(false);
			holder.setProtected(true);
		}
		return decl;
	}


	/**
	 *  Adds the method to the destination class
	 *
	 *@param  transform   The feature to be added to the MethodToDest attribute
	 *@param  rft         The feature to be added to the MethodToDest attribute
	 *@param  methodDecl  The feature to be added to the MethodToDest attribute
	 *@param  dest        The feature to be added to the MethodToDest attribute
	 */
	protected void addMethodToDest(ComplexTransform transform,
			RemoveMethodTransform rft,
			SimpleNode methodDecl,
			TypeSummary dest) {
		transform.clear();
		transform.add(rft);
		AddMethodTransform aft = new AddMethodTransform(methodDecl);
		transform.add(aft);

		AddMethodTypeVisitor visitor =
				new AddMethodTypeVisitor();
		methodSummary.accept(visitor, transform);

		//  Add appropriate import statements - to be determined later
		FileSummary parentFileSummary = (FileSummary) dest.getParent();
		transform.apply(parentFileSummary.getFile(), parentFileSummary.getFile());
	}
}
