/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.refactor.type.TypeChangeVisitor;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class EliminatePackageImportVisitor extends TypeChangeVisitor {
	private PackageSummary packageSummary;
	private LinkedList filterList = new LinkedList();


	/**
	 *  Constructor for the EliminatePackageImportVisitor object
	 *
	 *@param  complex  Description of Parameter
	 */
	public EliminatePackageImportVisitor(ComplexTransform complex) {
		super(complex);
	}


	/**
	 *  Sets the Package attribute of the EliminatePackageImportVisitor object
	 *
	 *@param  summary  The new Package value
	 */
	public void setPackageSummary(PackageSummary summary) {
		packageSummary = summary;
	}


	/**
	 *  Adds a feature to the FilterClass attribute of the
	 *  EliminatePackageImportVisitor object
	 *
	 *@param  name  The feature to be added to the FilterClass attribute
	 */
	public void addFilterClass(String name) {
		filterList.add(name);
	}


	/**
	 *  Gets the File Specific Transform
	 *
	 *@param  summary  Description of Parameter
	 *@return          The FileSpecificTransform value
	 */
	protected TransformAST getFileSpecificTransform(FileSummary summary) {
		return new RemoveImportTransform(packageSummary);
	}


	/**
	 *  Gets the New Imports transform
	 *
	 *@param  node       Description of Parameter
	 *@param  className  Description of Parameter
	 *@return            The NewImports value
	 */
	protected AddImportTransform getNewImports(FileSummary node, String className) {
		return new AddImportTransform(packageSummary.getName(), className);
	}


	/**
	 *  Gets the Remove Imports transform
	 *
	 *@param  node  Description of Parameter
	 *@return       The transform
	 */
	protected RemoveImportTransform getRemoveImportTransform(ImportSummary node) {
		return null;
	}


	/**
	 *  Gets the list of classes to iterate over
	 *
	 *@param  node  Description of Parameter
	 *@return       The list
	 */
	protected LinkedList getAppropriateClasses(FileSummary node) {
		LinkedList list = new LinkedList();

		Iterator fileIterator = packageSummary.getFileSummaries();
		if (fileIterator != null) {
			while (fileIterator.hasNext()) {
				FileSummary fileSummary = (FileSummary) fileIterator.next();

				addTypesFromFile(fileSummary, list);
			}
		}

		return list;
	}


	/**
	 *  Gets the reference to the file where the refactored output should be sent
	 *
	 *@param  node  Description of Parameter
	 *@return       The NewFile value
	 */
	protected File getNewFile(FileSummary node) {
		return node.getFile();
	}


	/**
	 *  Return the current package
	 *
	 *@return    the current package of the class
	 */
	protected String getCurrentPackage() {
		return packageSummary.getName();
	}


	/**
	 *  Checks any preconditions
	 *
	 *@param  summary  Description of Parameter
	 *@return          Description of the Returned Value
	 */
	protected boolean preconditions(FileSummary summary) {
		if (summary.getParent() == packageSummary) {
			return false;
		}

		Iterator iter = summary.getImports();
		if (iter != null) {
			while (iter.hasNext()) {
				ImportSummary next = (ImportSummary) iter.next();
				if (isImportingPackage(next)) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 *  Gets the RenamingTransform
	 *
	 *@param  refactoring  The feature to be added to the RenamingTransforms
	 *      attribute
	 *@param  node         The feature to be added to the RenamingTransforms
	 *      attribute
	 *@param  className    The feature to be added to the RenamingTransforms
	 *      attribute
	 */
	protected void addRenamingTransforms(ComplexTransform refactoring,
			FileSummary node, String className) {
	}


	/**
	 *  Gets the InFilter attribute of the EliminatePackageImportVisitor object
	 *
	 *@param  type  Description of Parameter
	 *@return       The InFilter value
	 */
	private boolean isInFilter(TypeSummary type) {
		Iterator iter = filterList.iterator();
		String name = type.getName();
		while (iter.hasNext()) {
			if (name.equals(iter.next())) {
				return true;
			}
		}
		return false;
	}


	/**
	 *  Determines if we are importing the package that we are eliminiating
	 *
	 *@param  next  the import statement in question
	 *@return       true if this is the import statement
	 */
	private boolean isImportingPackage(ImportSummary next) {
		return (next.getType() == null) &&
				(next.getPackage() == packageSummary);
	}


	/**
	 *  Adds a feature to the TypesFromFile attribute of the
	 *  EliminatePackageImportVisitor object
	 *
	 *@param  fileSummary  The feature to be added to the TypesFromFile attribute
	 *@param  list         The feature to be added to the TypesFromFile attribute
	 */
	private void addTypesFromFile(FileSummary fileSummary, LinkedList list) {
		Iterator typeIterator = fileSummary.getTypes();
		if (typeIterator != null) {
			while (typeIterator.hasNext()) {
				TypeSummary next = (TypeSummary) typeIterator.next();
				if (next.getModifiers().isPublic() && !isInFilter(next)) {
					list.add(next.getName());
				}
			}
		}
	}
}
