package org.acm.seguin.refactor.type;

import java.io.File;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RemoveImportTransform;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.query.FileSummaryGetter;
import org.acm.seguin.summary.query.MovingTypeList;
import org.acm.seguin.summary.query.StayingTypeList;

/**
 *  Scans through the summary objects to create a list of files that reference 
 *  a particular class. 
 *
 *@author    Chris Seguin 
 */
public class MoveClassVisitor extends TypeChangeVisitor {
	//  Instance Variables
	private String oldPackageName;
	private String newPackageName;
	private File base;


	/**
	 *  Determine if anything in this tree references these classes. 
	 *
	 *@param  oldPackage  the name of the old package 
	 *@param  newPackage  the name of the new package 
	 *@param  base        the base directory 
	 *@param  complex     Description of Parameter 
	 */
	public MoveClassVisitor(String oldPackage, String newPackage, File base, 
			ComplexTransform complex) {
		super(complex);
		oldPackageName = oldPackage;
		newPackageName = newPackage;
		this.base = base;
	}


	/**
	 *  Gets the File Specific Transform 
	 *
	 *@param  summary  Gets a file specific transform 
	 *@return          The FileSpecificTransform value 
	 */
	protected TransformAST getFileSpecificTransform(FileSummary summary) {
		if (summary.isMoving()) {
			return new ChangePackageTransform(newPackageName);
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
		String currentPackage = "";
		String otherPackage = "";

		if (node.isMoving()) {
			currentPackage = oldPackageName;
			otherPackage = newPackageName;
		}
		else {
			currentPackage = newPackageName;
			otherPackage = oldPackageName;
		}

		return new AddImportTransform(currentPackage, className);
	}


	/**
	 *  Gets the Remove Imports transform 
	 *
	 *@param  node  the import summary 
	 *@return       The transform 
	 */
	protected RemoveImportTransform getRemoveImportTransform(ImportSummary node) {
		if (node.getType() == null) {
			return null;
		}
		else {
			return new RemoveImportTransform(oldPackageName, node.getType());
		}
	}


	/**
	 *  Gets the AppropriateClasses attribute of the TypeChangeVisitor object 
	 *
	 *@param  node  Description of Parameter 
	 *@return       The AppropriateClasses value 
	 */
	protected LinkedList getAppropriateClasses(FileSummary node) {
		if (!node.isMoving()) {
			MovingTypeList mtl = new MovingTypeList();
			return mtl.query(oldPackageName);
		}
		else {
			StayingTypeList stl = new StayingTypeList();
			return stl.query(oldPackageName);
		}
	}


	/**
	 *  Gets the reference to the file where the refactored output should be sent 
	 *
	 *@param  node  the files summary 
	 *@return       The NewFile value 
	 */
	protected File getNewFile(FileSummary node) {
		if (!node.isMoving()) {
			return node.getFile();
		}

		File current = base;

		StringTokenizer tok = new StringTokenizer(newPackageName, ".");
		while (tok.hasMoreTokens()) {
			current = new File(current, tok.nextToken());
		}

		return new File(current, node.getName());
	}


	/**
	 *  Return the current package 
	 *
	 *@return    the current package of the class 
	 */
	protected String getCurrentPackage() {
		return oldPackageName;
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
		String currentPackage = "";
		String otherPackage = "";
		if (node.isMoving()) {
			currentPackage = oldPackageName;
			otherPackage = newPackageName;
		}
		else {
			currentPackage = newPackageName;
			otherPackage = oldPackageName;
		}

		if (otherPackage.length() > 0) {
			refactoring.add(new RenameTypeTransform(otherPackage, currentPackage, className));
		}
	}


	/**
	 *  Set the class name. Allows sub classes of this to reuse themselves for 
	 *  different classes in the same package. 
	 *
	 *@param  newClassName  the new class name 
	 */
	protected void add(String newClassName) {
		FileSummary summary = (new FileSummaryGetter()).query(oldPackageName, newClassName);
		if (summary != null) {
			summary.setMoving(true);
		}
		else {
			System.out.println("WARNING:  Unable to find the class " + newClassName + 
					" in the package " + oldPackageName);
		}
	}
}
