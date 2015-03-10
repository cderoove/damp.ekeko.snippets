/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Pushes up the signature of an abstract method into the parent class
 *
 *@author    Chris Seguin
 */
public class PushUpAbstractMethodRefactoring extends PushUpMethodRefactoring
{
	/**
	 *  Constructor for the PushUpAbstractMethodRefactoring object
	 */
	protected PushUpAbstractMethodRefactoring()
	{
	}


	/**
	 *  Gets the ID attribute of the PushUpAbstractMethodRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return PUSH_UP_ABSTRACT_METHOD;
	}


	/**
	 *  Moves the method to the parent class
	 */
	protected void transform()
	{
		ComplexTransform transform = getComplexTransform();
		FileSummary fileSummary;
		TypeSummary typeSummary = (TypeSummary) methodSummary.getParent();

		ModifierHolder holder = methodSummary.getModifiers();
		if (!(holder.isPublic() || holder.isProtected()))
		{
			transform.add(new ChangeMethodScopeTransform(methodSummary, ChangeMethodScopeVisitor.PROTECTED));
			fileSummary = (FileSummary) typeSummary.getParent();
			transform.apply(fileSummary.getFile(), fileSummary.getFile());
			transform.clear();
		}

		TypeDeclSummary parentDecl = typeSummary.getParentClass();
		TypeSummary parentSummary = GetTypeSummary.query(parentDecl);
		transform.add(new AddAbstractMethod(methodSummary));

		AddMethodTypeVisitor visitor =
				new AddMethodTypeVisitor(false);
		methodSummary.accept(visitor, transform);

		fileSummary = (FileSummary) parentSummary.getParent();
		transform.apply(fileSummary.getFile(), fileSummary.getFile());
	}
}
