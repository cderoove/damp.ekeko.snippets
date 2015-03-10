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

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FieldAccessSummary;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.MessageSendSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;
import org.acm.seguin.summary.query.FieldQuery;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.summary.query.MethodQuery;
import org.acm.seguin.summary.query.SamePackage;

/**
 *  Moves a method from one class to another. Generally used to move a method
 *  into a local variable or a parameter.
 *
 *@author    Chris Seguin
 */
public class MoveMethodRefactoring extends MethodRefactoring
{
	private MethodSummary methodSummary;
	private TypeSummary typeSummary;
	private Summary destination;


	/**
	 *  Constructor for the MoveMethodRefactoring object
	 */
	protected MoveMethodRefactoring()
	{
	}


	/**
	 *  Sets the Method attribute of the MoveMethodRefactoring object
	 *
	 *@param  value  The new Method value
	 */
	public void setMethod(MethodSummary value)
	{
		methodSummary = value;
		Summary current = methodSummary;
		while (!(current instanceof TypeSummary))
		{
			current = current.getParent();
		}
		typeSummary = (TypeSummary) current;
	}


	/**
	 *  Sets the Destination attribute of the MoveMethodRefactoring object
	 *
	 *@param  value  The new Destination value
	 */
	public void setDestination(Summary value)
	{
		destination = value;
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Moving " + methodSummary.toString() +
				" from " + typeSummary.toString() + " to " +
				destination.toString();
	}


	/**
	 *  Gets the ID attribute of the MoveMethodRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return MOVE_METHOD;
	}


	/**
	 *  Describes the preconditions that must be true for this refactoring to be
	 *  applied
	 *
	 *@exception  RefactoringException  thrown if one or more of the
	 *      preconditions is not satisfied. The text of the exception provides a
	 *      hint of what went wrong.
	 */
	protected void preconditions() throws RefactoringException
	{
		Iterator iter = methodSummary.getDependencies();
		while ((iter != null) && (iter.hasNext()))
		{
			Summary next = (Summary) iter.next();
			//  Check to see if we have any private fields without the appropriate getters/setters
			if (next instanceof FieldAccessSummary)
			{
				FieldAccessSummary fas = (FieldAccessSummary) next;
				checkFieldAccess(fas);
			}
			else if (next instanceof MessageSendSummary)
			{
				MessageSendSummary mss = (MessageSendSummary) next;
				checkMessageSend(mss);
			}
		}

		if (destination instanceof VariableSummary)
		{
			VariableSummary varSummary = (VariableSummary) destination;
			TypeDeclSummary typeDecl = varSummary.getTypeDecl();
			TypeSummary destType = GetTypeSummary.query(typeDecl);

			if (destType == null)
			{
				throw new RefactoringException("The parameter " + varSummary.getName() + " is a primitive");
			}

			FileSummary fileSummary = (FileSummary) destType.getParent();
			if (fileSummary.getFile() == null)
			{
				throw new RefactoringException("The source code for " + destType.getName() + " is not modifiable");
			}
		}
	}


	/**
	 *  Performs the transform on the rest of the classes
	 */
	protected void transform()
	{
		ComplexTransform transform = getComplexTransform();

		//  Update the method declaration to have the proper permissions
		SimpleNode methodDecl = removeMethod(typeSummary, transform);
		if (methodDecl == null)
		{
			return;
		}

		update(methodDecl);

		TypeSummary destType;
		if (destination instanceof VariableSummary)
		{
			VariableSummary varSummary = (VariableSummary) destination;
			TypeDeclSummary typeDecl = varSummary.getTypeDecl();
			destType = GetTypeSummary.query(typeDecl);
		}
		else if (destination instanceof TypeSummary)
		{
			destType = (TypeSummary) destination;
		}
		else
		{
			return;
		}

		addMethodToDest(transform,
				methodDecl,
				destType);
	}


	/**
	 *  Removes the method from the source
	 *
	 *@param  source     the source type
	 *@param  transform  the transform
	 *@return            Description of the Returned Value
	 */
	protected SimpleNode removeMethod(TypeSummary source, ComplexTransform transform)
	{
		RemoveMethodTransform rft = new RemoveMethodTransform(methodSummary);
		transform.add(rft);

		InvokeMovedMethodTransform immt = new InvokeMovedMethodTransform(methodSummary, destination);
		transform.add(immt);

		FileSummary fileSummary = (FileSummary) source.getParent();
		transform.apply(fileSummary.getFile(), fileSummary.getFile());

		return rft.getMethodDeclaration();
	}


	/**
	 *  Adds the method to the destination class
	 *
	 *@param  transform   The feature to be added to the MethodToDest attribute
	 *@param  methodDecl  The feature to be added to the MethodToDest attribute
	 *@param  dest        The feature to be added to the MethodToDest attribute
	 */
	protected void addMethodToDest(ComplexTransform transform,
			SimpleNode methodDecl,
			TypeSummary dest)
	{
		transform.clear();
		AddMethodTransform aft = new AddMethodTransform(methodDecl);
		transform.add(aft);

		AddMethodTypeVisitor visitor =
				new AddMethodTypeVisitor();
		methodSummary.accept(visitor, transform);

		//  Add appropriate import statements - to be determined later
		FileSummary parentFileSummary = (FileSummary) dest.getParent();
		transform.apply(parentFileSummary.getFile(), parentFileSummary.getFile());
	}


	/**
	 *  Gets the name of the getter for the field
	 *
	 *@param  summary  the field summary
	 *@return          the getter
	 */
	private String getFieldGetter(FieldSummary summary)
	{
		String typeName = summary.getType();
		String prefix = "get";
		if (typeName.equalsIgnoreCase("boolean"))
		{
			prefix = "is";
		}

		String name = summary.getName();

		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}


	/**
	 *  Gets the name of the setter for the field
	 *
	 *@param  summary  the field summary
	 *@return          the setter
	 */
	private String getFieldSetter(FieldSummary summary)
	{
		String prefix = "set";
		String name = summary.getName();
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}


	/**
	 *  Checks if we can properly transform the field access
	 *
	 *@param  fas                       Description of Parameter
	 *@exception  RefactoringException  Description of Exception
	 */
	private void checkFieldAccess(FieldAccessSummary fas) throws RefactoringException
	{
		if ((fas.getPackageName() == null) &&
				((fas.getObjectName() == null) || fas.getObjectName().equals("this")))
		{
			//  Now we have to find the field
			FieldSummary field = FieldQuery.find(typeSummary, fas.getFieldName());
			if (field != null)
			{
				if (field.getModifiers().isPrivate())
				{
					checkForMethod(fas, field);
				}
			}
		}
	}


	/**
	 *  For a private field, check that we have the correct setters or getters
	 *  (as appropriate)
	 *
	 *@param  fas                       Description of Parameter
	 *@param  field                     Description of Parameter
	 *@exception  RefactoringException  Description of Exception
	 */
	private void checkForMethod(FieldAccessSummary fas, FieldSummary field)
			 throws RefactoringException
	{
		String methodName;
		if (fas.isAssignment())
		{
			methodName = getFieldSetter(field);
		}
		else
		{
			methodName = getFieldGetter(field);
		}
		MethodSummary method = MethodQuery.find(typeSummary, methodName);
		if (method == null)
		{
			throw new RefactoringException("Unable to find the appropriate method (" +
					methodName + ") for private field access in " + typeSummary.getName());
		}
	}


	/**
	 *  Updates the node fore move method
	 *
	 *@param  node  Description of Parameter
	 */
	private void update(SimpleNode node)
	{
		MoveMethodVisitor mmv = new MoveMethodVisitor(typeSummary, methodSummary, destination);
		node.jjtAccept(mmv, null);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  mss                       Description of Parameter
	 *@exception  RefactoringException  Description of Exception
	 */
	private void checkMessageSend(MessageSendSummary mss) throws RefactoringException
	{
		if ((mss.getPackageName() == null) &&
				((mss.getObjectName() == null) || mss.getObjectName().equals("this")))
		{
			MethodSummary method = MethodQuery.find(typeSummary, mss.getMessageName());
			if (method == null)
			{
				throw new RefactoringException("Unable to find the method (" +
						mss.getMessageName() + ") in " + typeSummary.getName());
			}

			if (method.getModifiers().isPrivate())
			{
				throw new RefactoringException("Moving a method (" +
						mss.getMessageName() + ") from " + typeSummary.getName() +
						" that requires private access is illegal");
			}

			if (method.getModifiers().isPackage())
			{
				TypeSummary destType;
				if (destination instanceof VariableSummary)
				{
					VariableSummary varSummary = (VariableSummary) destination;
					TypeDeclSummary typeDecl = varSummary.getTypeDecl();
					destType = GetTypeSummary.query(typeDecl);
				}
				else if (destination instanceof TypeSummary)
				{
					destType = (TypeSummary) destination;
				}
				else
				{
					throw new RefactoringException("Cannot find the type associated with " +
							destination.getName());
				}

				if (!SamePackage.query(typeSummary, destType))
				{
					throw new RefactoringException("Moving a method (" +
							mss.getMessageName() + ") from " + typeSummary.getName() +
							" to a different package that requires package access is illegal.");
				}
			}
		}
	}
}
