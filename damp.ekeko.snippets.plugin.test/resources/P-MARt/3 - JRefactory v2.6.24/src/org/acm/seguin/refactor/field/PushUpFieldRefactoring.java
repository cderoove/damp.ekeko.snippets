/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.FieldQuery;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Performs the pullup field refactoring
 *
 *@author    Chris Seguin
 */
public class PushUpFieldRefactoring extends FieldRefactoring {
	private TypeSummary parentType;


	/**
	 *  Constructor for the PushUpFieldRefactoring object
	 */
	protected PushUpFieldRefactoring() { }


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Moves a field " + field +
				" into parent class named " + parentType.getName();
	}


	/**
	 *  Gets the ID attribute of the PushUpFieldRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return PUSH_UP_FIELD;
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

		if (FieldQuery.query(typeSummary, field, FieldQuery.PRIVATE) == null) {
			throw new RefactoringException("Field named " + field + " does not exist in " + typeSummary.getName());
		}

		TypeDeclSummary parentDecl = typeSummary.getParentClass();
		parentType = GetTypeSummary.query(parentDecl);

		if (parentType == null) {
			throw new RefactoringException("Can't push up a field into source code that you don't have");
		}

		checkDestinationFile(parentType, "Can't push up a field into source code that you don't have");

		if (FieldQuery.query(parentType, field, FieldQuery.PRIVATE) != null) {
			throw new RefactoringException("Field named " + field + " already exists in parent class");
		}

		if (FieldQuery.queryAncestors(typeSummary, field, FieldQuery.PRIVATE) != null) {
			throw new RefactoringException("Field named " + field + " already exists in an ancestor class");
		}

		if (((FileSummary) parentType.getParent()).getFile() == null) {
			throw new RefactoringException("Can't push up a field into source code that you don't have");
		}

		if (((FileSummary) typeSummary.getParent()).getFile() == null) {
			throw new RefactoringException("Can't push up a field from source code that you don't have");
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

		ASTFieldDeclaration decl = (ASTFieldDeclaration) fieldDecl.jjtGetChild(0);
		ModifierHolder holder = decl.getModifiers();
		if (!holder.isPublic()) {
			holder.setPrivate(false);
			holder.setProtected(true);
		}

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
		FileSummary parentFileSummary = (FileSummary) parentType.getParent();
		transform.apply(parentFileSummary.getFile(), parentFileSummary.getFile());

		//  Remove the field from all child classes
		(new RemoveFieldFromSubclassVisitor(parentType,
				typeSummary.getField(field), typeSummary,
				transform)).visit(null);
	}
}
