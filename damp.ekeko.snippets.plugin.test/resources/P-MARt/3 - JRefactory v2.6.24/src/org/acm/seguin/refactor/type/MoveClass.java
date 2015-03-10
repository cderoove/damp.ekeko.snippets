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
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.query.PackageNameGetter;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.summary.query.TopLevelDirectory;

/**
 *  Main program for repackaging. This object simply stores the main program
 *  and interprets the command line arguments for repackaging one or more
 *  files.
 *
 *@author     Chris Seguin
 *@created    June 2, 1999
 */
public class MoveClass extends Refactoring {
	//  Instance Variables
	/**
	 *  The directory
	 */
	protected String initDir;
	/**
	 *  The list of filenames
	 */
	protected LinkedList fileList;
	private String oldPackage;
	private File base;
	private String srcPackage;
	private String destPackage;


	/**
	 *  Constructor for repackage
	 */
	protected MoveClass()
	{
		destPackage = null;
		initDir = System.getProperty("user.dir");
		fileList = new LinkedList();
	}


	/**
	 *  Set the destination package
	 *
	 *@param  dest  the package name
	 */
	public void setDestinationPackage(String dest)
	{
		destPackage = dest;
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
	 *  Gets the Description attribute of the MoveClass object
	 *
	 *@return    The Description value
	 */
	public String getDescription()
	{
		return "Repackaging classes from " + srcPackage + " to " + destPackage;
	}


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public int getID()
	{
		return REPACKAGE;
	}


	/**
	 *  Add a file to the list. The file name includes only the name, and not the
	 *  entire path.
	 *
	 *@param  filename  the file to add
	 */
	public void add(String filename)
	{
		fileList.add(filename);
	}


	/**
	 *  Main processing method for the MoveClass object
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	protected void preconditions() throws RefactoringException
	{
		if ((destPackage == null) || (fileList.size() == 0)) {
			return;
		}

		File startDir = new File(initDir);
		String firstFilename = (String) fileList.get(0);
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
	}


	/**
	 *  Performs the transformation of the class
	 */
	protected void transform()
	{
		MoveClassVisitor mcv = new MoveClassVisitor(srcPackage,
				destPackage, base, getComplexTransform());
		Iterator iter = fileList.iterator();
		while (iter.hasNext()) {
			//  Get the next file
			String nextFile = (String) iter.next();

			int start = Math.max(0, nextFile.indexOf(File.separator));
			int end = nextFile.indexOf(".java");

			String nextClass = "";
			if (end > 0) {
				nextClass = nextFile.substring(start, end);
			}
			else {
				nextClass = nextFile.substring(start);
			}

			mcv.add(nextClass);
		}

		mcv.visit(null);
	}
}
