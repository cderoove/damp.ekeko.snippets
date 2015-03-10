/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.type;

import java.io.File;
import java.io.IOException;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.query.PackageNameGetter;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.EliminatePackageImportVisitor;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.summary.query.PackageContainsClass;
import org.acm.seguin.summary.query.TopLevelDirectory;

/**
 *  Main program for renaming a class.
 *
 *@author    Chris Seguin
 */
public class RenameClassRefactoring extends Refactoring {
	//  Instance Variables
	private String initDir;
	private String oldPackage;
	private String oldClassName;
	private String newClassName;
	private String srcPackage;
	private File base;


	/**
	 *  Constructor for repackage
	 */
	protected RenameClassRefactoring()
	{
		initDir = System.getProperty("user.dir");
	}


	/**
	 *  Set the directory
	 *
	 *@param  dir  the initial directory
	 */
	public void setDirectory(String dir)
	{
		initDir = dir;
	}


	/**
	 *  Sets the OldClassName attribute of the RenameClass object
	 *
	 *@param  value  The new OldClassName value
	 */
	public void setOldClassName(String value)
	{
		oldClassName = value;
	}


	/**
	 *  Sets the NewClassName attribute of the RenameClass object
	 *
	 *@param  value  The new NewClassName value
	 */
	public void setNewClassName(String value)
	{
		newClassName = value;
	}


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public String getDescription()
	{
		return "Renames a class from " + oldClassName +
				" to " + newClassName;
	}


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public int getID()
	{
		return RENAME_CLASS;
	}


	/**
	 *  Gets the file summary that we are changing
	 *
	 *@return    The FileSummary value
	 */
	protected FileSummary getFileSummary()
	{
		PackageSummary packageSummary = PackageSummary.getPackageSummary(srcPackage);
		TypeSummary typeSummary = GetTypeSummary.query(packageSummary, oldClassName);
		return (FileSummary) typeSummary.getParent();
	}


	/**
	 *  Preconditions for the refactoring to be applied
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void preconditions() throws RefactoringException
	{
		if (oldClassName == null) {
			throw new RefactoringException("No old class specified");
		}

		if (newClassName == null) {
			throw new RefactoringException("No new class specified");
		}

		File startDir = new File(initDir);
		String firstFilename = oldClassName + ".java";
		ASTName srcPackageName = PackageNameGetter.query(startDir, firstFilename);
		srcPackage = "";
		if (srcPackageName != null) {
			srcPackage = srcPackageName.getName();
		}

		base = TopLevelDirectory.query(startDir, firstFilename);

		String topLevelDir = base.getPath();
		try {
			topLevelDir = base.getCanonicalPath();
		}
		catch (IOException ioe) {
		}
		(new SummaryTraversal(topLevelDir)).go();

		if (PackageContainsClass.query(srcPackage, newClassName)) {
			throw new RefactoringException(srcPackage + " already contains a class named " + newClassName);
		}
	}


	/**
	 *  The transformation of all the source files
	 */
	protected void transform()
	{
		System.out.println("Renaming " + oldClassName + " to " + newClassName);

		ComplexTransform complex = getComplexTransform();

		EliminatePackageImportVisitor epiv = new EliminatePackageImportVisitor(complex);
		epiv.setPackageSummary(PackageSummary.getPackageSummary(srcPackage));
		epiv.addFilterClass(oldClassName);
		epiv.visit(null);

		RenameClassVisitor rcv = new RenameClassVisitor(srcPackage,
				oldClassName, newClassName, base, complex);
		rcv.visit(null);
	}
}
