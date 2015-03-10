package org.acm.seguin.refactor.type;

import java.io.File;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RemoveImportTransform;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Renames a class from one name to another. 
 *
 *@author    Chris Seguin 
 */
public class RenameClassVisitor extends TypeChangeVisitor {
	//  Instance Variables
	/**
	 *  Description of the Field 
	 */
	protected String packageName;
	/**
	 *  Description of the Field 
	 */
	protected String oldClassName;
	/**
	 *  Description of the Field 
	 */
	protected String newClassName;
	private File base;
	private File targetFile;


	/**
	 *  Determine if anything in this tree references these classes. 
	 *
	 *@param  base         the base directory 
	 *@param  packageName  Description of Parameter 
	 *@param  oldClass     Description of Parameter 
	 *@param  newClass     Description of Parameter 
	 *@param  complex      Description of Parameter 
	 */
	public RenameClassVisitor(String packageName, String oldClass, 
			String newClass, File base, ComplexTransform complex) {
		super(complex);
		this.packageName = packageName;
		this.base = base;
		oldClassName = oldClass;
		newClassName = newClass;
	}


	/**
	 *  Gets the File Specific Transform 
	 *
	 *@param  summary  Gets a file specific transform 
	 *@return          The FileSpecificTransform value 
	 */
	protected TransformAST getFileSpecificTransform(FileSummary summary) {
		if (isRenamingTarget(summary)) {
			ASTName oldName = new ASTName(0);
			oldName.fromString(oldClassName);
			ASTName newName = new ASTName(0);
			newName.fromString(newClassName);

			return new RenameTypeTransform(oldName, newName, 
					GetTypeSummary.query(packageName, oldClassName));
		}

		return null;
	}


	/**
	 *  Gets the New Imports transform 
	 *
	 *@param  node       the file summary 
	 *@param  className  the name of the class that is changing 
	 *@return            The NewImports value 
	 */
	protected AddImportTransform getNewImports(FileSummary node, String className) {
		if (GetTypeSummary.query(node, newClassName) == null) {
			return new AddImportTransform(packageName, newClassName);
		}
		else {
			return null;
		}
	}


	/**
	 *  Gets the Remove Imports transform 
	 *
	 *@param  node  the import summary 
	 *@return       The transform 
	 */
	protected RemoveImportTransform getRemoveImportTransform(ImportSummary node) {
		return new RemoveImportTransform(packageName, oldClassName);
	}


	/**
	 *  Gets the AppropriateClasses attribute of the TypeChangeVisitor object 
	 *
	 *@param  node  Description of Parameter 
	 *@return       The AppropriateClasses value 
	 */
	protected LinkedList getAppropriateClasses(FileSummary node) {
		LinkedList result = new LinkedList();
		result.add(oldClassName);
		return result;
	}


	/**
	 *  Gets the reference to the file where the refactored output should be sent 
	 *
	 *@param  node  the files summary 
	 *@return       The NewFile value 
	 */
	protected File getNewFile(FileSummary node) {
		File current = base;

		StringTokenizer tok = new StringTokenizer(packageName, ".");
		while (tok.hasMoreTokens()) {
			current = new File(current, tok.nextToken());
		}

		File input = new File(current, oldClassName + ".java");
		if (checkFiles(input, node.getFile())) {
			File result = new File(current, newClassName + ".java");
			return result;
		}
		else {
			return node.getFile();
		}
	}


	/**
	 *  Return the current package 
	 *
	 *@return    the current package of the class 
	 */
	protected String getCurrentPackage() {
		return packageName;
	}


	/**
	 *  Gets the new name 
	 *
	 *@return    an ASTName containing the new name 
	 */
	protected ASTName getNewName() {
		ASTName result = new ASTName(0);
		result.fromString(packageName);
		result.addNamePart(newClassName);
		return result;
	}


	/**
	 *  Gets the RenamingTransform 
	 *
	 *@param  refactoring  the refactoring 
	 *@param  node         the file summary to reference 
	 *@param  className    the name of the class that is changing 
	 */
	protected void addRenamingTransforms(ComplexTransform refactoring, 
			FileSummary node, String className) {
		ASTName oldOne = new ASTName(0);
		oldOne.addNamePart(oldClassName);

		TypeSummary importedType = GetTypeSummary.query(node, newClassName);
		if ((importedType != null) && isRenamingTarget(node)) {
			renameRefactoringTarget(refactoring, node, className, oldOne, importedType);
		}
		else if (importedType != null) {
			alreadyImportsType(refactoring, oldOne, node, importedType);
		}
		else {
			simpleRename(refactoring, oldOne);
		}

		refactoring.add(new RenameTypeTransform(getOldName(), getNewName(), 
				GetTypeSummary.query(packageName, oldClassName)));
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
		refactoring.add(new RenameTypeTransform(oldOne, getNewName(), 
				GetTypeSummary.query(packageName, oldClassName)));
	}


	/**
	 *  Checks to see if this is the target 
	 *
	 *@param  summary  the file summary 
	 *@return          true if this file summary represents the file that is 
	 *      being renamed 
	 */
	private boolean isRenamingTarget(FileSummary summary) {
		if (targetFile == null) {
			File current = base;

			StringTokenizer tok = new StringTokenizer(packageName, ".");
			while (tok.hasMoreTokens()) {
				current = new File(current, tok.nextToken());
			}

			targetFile = new File(current, oldClassName + ".java");
		}

		return checkFiles(targetFile, summary.getFile());
	}


	/**
	 *  Creates a name from a type summary 
	 *
	 *@param  summary  the type summary 
	 *@return          the name 
	 */
	private ASTName getImport(TypeSummary summary) {
		ASTName name = new ASTName(0);

		Summary current = summary.getParent();
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}

		name.fromString(((PackageSummary) current).getName());
		name.addNamePart(summary.getName());

		return name;
	}


	/**
	 *  Gets the old name 
	 *
	 *@return    an ASTName containing the old name 
	 */
	private ASTName getOldName() {
		ASTName result = new ASTName(0);
		result.fromString(packageName);
		result.addNamePart(oldClassName);
		return result;
	}


	/**
	 *  This file does not import the type, so the rename operation is simple 
	 *
	 *@param  refactoring  the complex transformation 
	 *@param  oldOne       the old one 
	 */
	private void simpleRename(ComplexTransform refactoring, ASTName oldOne) {
		ASTName newOne = new ASTName(0);
		newOne.addNamePart(newClassName);

		refactoring.add(new RenameTypeTransform(oldOne, newOne, 
				GetTypeSummary.query(packageName, oldClassName)));
	}


	/**
	 *  We are attempting to perform the rename operation on the targeted file. 
	 *  This change is complex because the file already imported another class 
	 *  with the same name 
	 *
	 *@param  refactoring   the complex transformation 
	 *@param  node          the node 
	 *@param  className     the new name 
	 *@param  oldOne        the old class 
	 *@param  importedType  the type that was imported that has the same name 
	 */
	private void renameRefactoringTarget(ComplexTransform refactoring, 
			FileSummary node, String className, ASTName oldOne, 
			TypeSummary importedType) {
		ASTName newOne = new ASTName(0);
		newOne.addNamePart(newClassName);

		ASTName importedTypeName = getImport(importedType);

		refactoring.add(new RenameTypeTransform(newOne, importedTypeName, 
				GetTypeSummary.query(packageName, oldClassName)));
		refactoring.add(new RemoveImportTransform(importedTypeName));
		refactoring.add(new RenameTypeTransform(oldOne, newOne, 
				GetTypeSummary.query(packageName, oldClassName)));
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  file1  Description of Parameter 
	 *@param  file2  Description of Parameter 
	 *@return        Description of the Returned Value 
	 */
	private boolean checkFiles(File file1, File file2) {
		try {
			String one = file1.getCanonicalPath();
			String two = file2.getCanonicalPath();
			return one.equals(two);
		}
		catch (java.io.IOException ioe) {
			return false;
		}
	}
}
