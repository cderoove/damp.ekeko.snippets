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
import java.util.LinkedList;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.FieldQuery;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Performs the push down field refactoring
 *
 *@author    Chris Seguin
 */
public class PushDownFieldRefactoring extends FieldRefactoring {
	private LinkedList childTypes;


	/**
	 *  Constructor for the PushDownFieldRefactoring object
	 */
	protected PushDownFieldRefactoring()
	{
		childTypes = new LinkedList();
		field = null;
		typeSummary = null;
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Moves a field " + field +
				" into child classes of " + typeSummary.getName();
	}


	/**
	 *  Gets the ID attribute of the PushDownFieldRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return PUSH_DOWN_FIELD;
	}


	/**
	 *  Adds a child class where the field should be pushed into
	 *
	 *@param  packageName  the package name
	 *@param  className    the class name
	 */
	public void addChild(String packageName, String className)
	{
		addChild(GetTypeSummary.query(
				PackageSummary.getPackageSummary(packageName),
				className));
	}


	/**
	 *  Adds a child class where the field should be pushed into
	 *
	 *@param  init  The new Class value
	 */
	public void addChild(TypeSummary init)
	{
		if (init != null) {
			System.out.println("Adding " + init.getName());
			childTypes.add(init);
		}
	}


	/**
	 *  Preconditions that must be true for the refactoring to work
	 *
	 *@exception  RefactoringException  a problem with performing this
	 *      refactoring
	 */
	protected void preconditions() throws RefactoringException
	{
		if (field == null) {
			throw new RefactoringException("No field specified");
		}

		if (typeSummary == null) {
			throw new RefactoringException("No type specified");
		}

		if (childTypes.size() == 0) {
			throw new RefactoringException("No child types specified");
		}

		if (FieldQuery.query(typeSummary, field, FieldQuery.PRIVATE) == null) {
			throw new RefactoringException("Field named " + field + " does not exist in " + typeSummary.getName());
		}

		if (((FileSummary) typeSummary.getParent()).getFile() == null) {
			throw new RefactoringException("Can't push down a field from source code that you don't have");
		}

		Iterator iter = childTypes.iterator();
		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();

			if (next == null) {
				throw new RefactoringException("Can't push down a field into source code that you don't have");
			}

			if (FieldQuery.query(next, field, FieldQuery.PRIVATE) != null) {
				throw new RefactoringException("Field named " + field + " already exists in " + next.getName());
			}

			if (((FileSummary) next.getParent()).getFile() == null) {
				throw new RefactoringException("Can't push up a field into source code that you don't have");
			}

			TypeDeclSummary parentDecl = next.getParentClass();
			TypeSummary parentTypeSummary = GetTypeSummary.query(parentDecl);

			if (parentTypeSummary != typeSummary) {
				throw new RefactoringException("Trying to push a field from " +
						typeSummary.getName() + " to " + next.getName() +
						" and the destination is not a subclass of the source");
			}
		}
	}


	/**
	 *  Actually update the files
	 */
	protected void transform()
	{
		FileSummary fileSummary = (FileSummary) getFileSummary(typeSummary);
		RemoveFieldTransform rft = new RemoveFieldTransform(field);
		ComplexTransform transform = getComplexTransform();
		transform.add(rft);
		transform.apply(fileSummary.getFile(), fileSummary.getFile());

		//  Update the field declaration to have the proper permissions
		SimpleNode fieldDecl = rft.getFieldDeclaration();
		if (fieldDecl == null) {
			return;
		}

		Iterator iter = childTypes.iterator();
		while (iter.hasNext()) {
			AddFieldTransform aft = new AddFieldTransform(fieldDecl);
			transform.clear();
			transform.add(aft);
			Object fieldType = getFieldType(fieldDecl, fileSummary);
			if (fieldType == null) {
				//  Do nothing
			}
			else if ((fieldType instanceof TypeSummary) &&
					!isInJavaLang((TypeSummary) fieldType)) {
				transform.add(new AddImportTransform((TypeSummary) fieldType));
			}
			else if ((fieldType instanceof ASTName) &&
					!isInJavaLang((ASTName) fieldType)) {
				transform.add(new AddImportTransform((ASTName) fieldType));
			}

			TypeSummary next = (TypeSummary) iter.next();
			FileSummary nextFileSummary = (FileSummary) next.getParent();
			transform.apply(nextFileSummary.getFile(), nextFileSummary.getFile());
		}
	}
}
