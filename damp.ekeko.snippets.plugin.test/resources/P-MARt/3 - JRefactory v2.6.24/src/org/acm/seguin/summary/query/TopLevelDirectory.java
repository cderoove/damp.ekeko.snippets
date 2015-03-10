/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;
import org.acm.seguin.parser.query.PackageNameGetter;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;

/**
 *  This class is used to infer the top level directory
 *
 *@author    Chris Seguin
 */
public class TopLevelDirectory {
	/**
	 *  Gets the PackageDirectory attribute of the TopLevelDirectory class
	 *
	 *@param  initialSummary  Description of Parameter
	 *@param  packageName     Description of Parameter
	 *@return                 The PackageDirectory value
	 */
	public static File getPackageDirectory(Summary initialSummary, String packageName)
	{
		File rootDir = null;

		if (initialSummary != null) {
			FileSummary fileSummary = getFileSummary(initialSummary);
			rootDir = query(fileSummary);
		}

		if (rootDir == null) {
			rootDir = query();
		}

		StringTokenizer tok = new StringTokenizer(packageName, ".");
		File current = rootDir;
		while (tok.hasMoreTokens()) {
			current = new File(current, tok.nextToken());
		}

		return current;
	}


	/**
	 *  Return the top level directory from a FileSummary
	 *
	 *@param  fileSummary  Description of Parameter
	 *@return              Description of the Returned Value
	 */
	public static File query(FileSummary fileSummary)
	{
		File current = fileSummary.getFile();
		if (current == null) {
			return null;
		}

		File currentDir = current.getParentFile();
		PackageSummary packageSummary = (PackageSummary) fileSummary.getParent();

		String name = packageSummary.getName();
		if (name.length() == 0) {
			return currentDir;
		}

		int index = name.indexOf(".");
		currentDir = currentDir.getParentFile();

		while (index != -1) {
			index = name.indexOf(".", index + 1);
			currentDir = currentDir.getParentFile();
		}

		return currentDir;
	}


	/**
	 *  Return the top level directory
	 *
	 *@param  initialDir  Description of Parameter
	 *@param  filename    Description of Parameter
	 *@return             the top level directory
	 */
	public static File query(File initialDir, String filename)
	{
		//  Create a factory to get a root
		File inputFile = new File(initialDir, filename);
		ParserFactory factory = new FileParserFactory(inputFile);
		SimpleNode root = factory.getAbstractSyntaxTree(false);

		File topLevel = getParent(inputFile);
		ASTName packageName = PackageNameGetter.query(root);
		if (packageName != null) {
			for (int ndx = 0; ndx < packageName.getNameSize(); ndx++) {
				topLevel = getParent(topLevel);
			}
		}

		//  Return that directory
		return topLevel;
	}


	/**
	 *  Given a file, it returns the parent file
	 *
	 *@param  input  the input file
	 *@return        the parent of that file
	 */
	private static File getParent(File input)
	{
		try {
			String path = input.getCanonicalPath();
			File temp = new File(path);
			return temp.getParentFile();
		}
		catch (IOException ioe) {
		}

		return input.getParentFile();
	}


	/**
	 *  Gets the FileSummary attribute of the TopLevelDirectory class
	 *
	 *@param  initialSummary  Description of Parameter
	 *@return                 The FileSummary value
	 */
	private static FileSummary getFileSummary(Summary initialSummary)
	{
		Summary currentSummary = initialSummary;
		while (!(currentSummary instanceof FileSummary)) {
			currentSummary = currentSummary.getParent();
			if (currentSummary == null) {
				return null;
			}
		}
		return (FileSummary) currentSummary;
	}


	/**
	 *  Searches all the packages for an appropriate file and infers the source
	 *  root directory
	 *
	 *@return    the root directory
	 */
	private static File query()
	{
		FileSummary appropriate = findFileSummary();
		if (appropriate == null) {
			return new File(System.getProperty("user.dir"));
		}

		PackageSummary packageSummary = (PackageSummary) appropriate.getParent();
		String packageName = packageSummary.getName();
		StringTokenizer tok = new StringTokenizer(packageName, ".");
		File startingPoint = null;
		try {
			startingPoint = new File(appropriate.getFile().getCanonicalPath());
		}
		catch (IOException ioe) {
			startingPoint = appropriate.getFile();
		}

		File current = startingPoint.getParentFile();
		while (tok.hasMoreTokens()) {
			current = current.getParentFile();
			String value = tok.nextToken();
		}

		return current;
	}


	/**
	 *  Searches for a file summary with a file (rather than a null) in the file
	 *
	 *@return    the file summary
	 */
	private static FileSummary findFileSummary()
	{
		Iterator iter = PackageSummary.getAllPackages();
		if (iter != null) {
			while (iter.hasNext()) {
				PackageSummary next = (PackageSummary) iter.next();
				Iterator iter2 = next.getFileSummaries();
				while ((iter2 != null) && iter2.hasNext()) {
					FileSummary fileSummary = (FileSummary) iter2.next();
					if (fileSummary.getFile() != null) {
						return fileSummary;
					}
				}
			}
		}
		return null;
	}
}
