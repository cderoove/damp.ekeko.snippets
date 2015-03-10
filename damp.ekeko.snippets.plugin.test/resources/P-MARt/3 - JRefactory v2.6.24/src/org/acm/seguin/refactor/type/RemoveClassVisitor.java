package org.acm.seguin.refactor.type;

import java.io.File;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  The visitor object for removing a class from the system. 
 *
 *@author    Chris Seguin 
 */
public class RemoveClassVisitor extends RenameClassVisitor {
	private String parentPackage;
	private String oldPackage;


	/**
	 *  Constructor for the remove class visitor object 
	 *
	 *@param  packageName        the package name 
	 *@param  oldClass           the name of the class being deleted 
	 *@param  newClass           the parent class of that being deleted 
	 *@param  base               the base directory 
	 *@param  initParentPackage  Description of Parameter 
	 *@param  complex            Description of Parameter 
	 */
	public RemoveClassVisitor(String packageName, String oldClass, 
			String initParentPackage, String newClass, File base, 
			ComplexTransform complex) {
		super(packageName, oldClass, newClass, base, complex);

		parentPackage = initParentPackage;
		oldPackage = packageName;
	}


	/**
	 *  Gets the New Imports transform 
	 *
	 *@param  node       the file summary 
	 *@param  className  the name of the class that is changing 
	 *@return            The NewImports value 
	 */
	protected AddImportTransform getNewImports(FileSummary node, String className) {
		if (newClassName.equals("Object")) {
			return null;
		}
		else if (GetTypeSummary.query(node, newClassName) == null) {
			return new AddImportTransform(parentPackage, newClassName);
		}
		else {
			return null;
		}
	}


	/**
	 *  Gets the new name 
	 *
	 *@return    an ASTName containing the new name 
	 */
	protected ASTName getNewName() {
		if (newClassName.equals("Object")) {
			ASTName result = new ASTName(0);
			result.addNamePart(newClassName);
			return result;
		}
		if (oldPackage.equals(parentPackage)) {
			return super.getNewName();
		}

		ASTName result = new ASTName(0);
		result.fromString(parentPackage);
		result.addNamePart(newClassName);
		return result;
	}


	/**
	 *  We are performing the transformation on a refactoring that already has 
	 *  that type imported from another class 
	 *
	 *@param  refactoring   the complex transformation 
	 *@param  oldOne        the old class name 
	 *@param  node          the file that is being changed 
	 *@param  importedType  the type that we are supposedly importing 
	 */
	protected void alreadyImportsType(ComplexTransform refactoring, ASTName oldOne, 
			FileSummary node, TypeSummary importedType) {
		if (isSamePackage(node, importedType) || isParent(importedType)) {
			ASTName newOne = new ASTName(0);
			newOne.addNamePart(newClassName);

			refactoring.add(new RenameTypeTransform(oldOne, newOne, null));
		}
		else {
			refactoring.add(new RenameTypeTransform(oldOne, getNewName(), null));
		}
	}


	/**
	 *  Gets the SamePackage attribute of the RemoveClassVisitor object 
	 *
	 *@param  fileSummary  Description of Parameter 
	 *@param  typeSummary  Description of Parameter 
	 *@return              The SamePackage value 
	 */
	private boolean isSamePackage(FileSummary fileSummary, TypeSummary typeSummary) {
		Summary one = fileSummary;
		Summary two = typeSummary;

		while (!(one instanceof PackageSummary)) {
			one = one.getParent();
		}

		while (!(two instanceof PackageSummary)) {
			two = two.getParent();
		}

		return one.equals(two);
	}


	/**
	 *  Gets the Parent attribute of the RemoveClassVisitor object 
	 *
	 *@param  typeSummary  Description of Parameter 
	 *@return              The Parent value 
	 */
	private boolean isParent(TypeSummary typeSummary) {
		Summary one = typeSummary;

		while (!(one instanceof PackageSummary)) {
			one = one.getParent();
		}

		PackageSummary packageSummary = (PackageSummary) one;
		String packageName = packageSummary.getName();

		return packageName.equals(parentPackage);
	}
}
