/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.query;

import java.util.Iterator;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Gets the type summary associated with a particular type declaration, file,
 *  or package.
 *
 *@author     Chris Seguin
 *@created    November 30, 1999
 */
public class GetTypeSummary {
	/**
	 *  Get the type summary that this object refers to. If the type summary is
	 *  not found or the type is primitive, a null is returned. If the input is
	 *  null, the output is also null.
	 *
	 *@param  typeDecl  the place to start the search
	 *@return           the type summary or null
	 */
	public static TypeSummary query(TypeDeclSummary typeDecl)
	{
		if (typeDecl == null) {
			return null;
		}

		//  Check if it is primitive
		if (typeDecl.isPrimitive()) {
			return null;
		}

		String packageName = typeDecl.getPackage();
		String typeName = typeDecl.getType();
		if (packageName != null) {
			//  Look up the type in the specified package
			PackageSummary packageSummary = PackageSummary.getPackageSummary(packageName);
			return query(packageSummary, typeName);
		}
		else {
			//  Find file summary in parent
			Summary next = typeDecl.getParent();
			while (!(next instanceof FileSummary)) {
				next = next.getParent();
			}

			FileSummary fileSummary = (FileSummary) next;

			//  Look up the type in the file summary
			return query(fileSummary, typeName);
		}
	}


	/**
	 *  Searches a package for a particular type. This method returns the type,
	 *  if it is found. If it is not found or the name is null, this method
	 *  returns null.
	 *
	 *@param  fileSummary  the file summary
	 *@param  name         the name of the type summary
	 *@return              the type summary if it is found and null otherwise
	 */
	public static TypeSummary query(FileSummary fileSummary, String name)
	{
		//System.out.println("Seaching " + fileSummary.getName() + "(FileSummary) for " + name);
		//  Check for null pointers
		if (name == null) {
			return null;
		}

		TypeSummary result = null;

		//  First try with the package
		result = query((PackageSummary) fileSummary.getParent(), name);
		if (result != null) {
			//System.out.println("Found " + name + " in the current package");
			return result;
		}

		//  Then try each import statement
		result = checkImports(fileSummary, name);
		if (result != null) {
			//System.out.println("Found " + name + " in an import statement");
			return result;
		}

		//  Try in java.lang
		PackageSummary nextPackage = PackageSummary.getPackageSummary("java.lang");
		result = query(nextPackage, name);

		if (result != null) {
			//System.out.println("Found " + name + " in java.lang");
		}

		//  Not found
		return result;
	}


	/**
	 *  Searches a package for a particular type. This method returns the type,
	 *  if it is found. If it is not found or the name is null, this method
	 *  returns null.
	 *
	 *@param  packageSummary  the package summary
	 *@param  name            the name of the type summary
	 *@return                 the type summary if it is found and null otherwise
	 */
	public static TypeSummary query(PackageSummary packageSummary, String name)
	{
		//System.out.println("Seaching " + packageSummary.getName() + "(PackageSummary) for " + name);
		//  Check for null pointers
		if (name == null) {
			return null;
		}

		Iterator fileIterator = packageSummary.getFileSummaries();
		if (fileIterator != null) {
			while (fileIterator.hasNext()) {
				FileSummary nextFile = (FileSummary) fileIterator.next();

				TypeSummary result = checkType(nextFile, name);
				if (result != null) {
					//System.out.println("Found " + name + " in " + nextFile.getName());
					return result;
				}
			}
		}

		//  Not found
		return null;
	}


	/**
	 *  Searches a package for a particular type. This method returns the type,
	 *  if it is found. If it is not found or the name is null, this method
	 *  returns null.
	 *
	 *@param  packageName  the package name
	 *@param  name         the name of the type summary
	 *@return              the type summary if it is found and null otherwise
	 */
	public static TypeSummary query(String packageName, String name)
	{
		return query(PackageSummary.getPackageSummary(packageName), name);
	}


	/**
	 *  Finds a nested type based on the name of the object
	 *
	 *@param  parent  the parent type
	 *@param  name    the name of the method
	 *@return         the type summary if found or null otherwise
	 */
	public static TypeSummary query(TypeSummary parent, String name)
	{
		Iterator iter = parent.getTypes();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();
			if (next.getName().equals(name)) {
				return next;
			}
		}

		return null;
	}


	/**
	 *  Finds a nested type based on the name of the object
	 *
	 *@param  parent  the parent type
	 *@param  name    the name of the method
	 *@return         the type summary if found or null otherwise
	 */
	public static TypeSummary query(MethodSummary parent, String name)
	{
		Iterator iter = parent.getDependencies();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			Summary next = (Summary) iter.next();
			if (next instanceof TypeSummary) {
				TypeSummary consider = (TypeSummary) next;
				if (consider.getName().equals(name)) {
					return consider;
				}
			}
		}

		return null;
	}


	/**
	 *  Searches for the type based on the imports
	 *
	 *@param  fileSummary  the file summaries
	 *@param  name         the name we are looking for
	 *@return              Description of the Returned Value
	 */
	private static TypeSummary checkImports(FileSummary fileSummary, String name)
	{
		Iterator iter = fileSummary.getImports();
		if (iter != null) {
			while (iter.hasNext()) {
				ImportSummary next = (ImportSummary) iter.next();
				//System.out.println("Seaching " + next.getPackage().getName()
				//	+ ((next.getType() == null) ? ".*" : ("." + next.getType()))
				//	+ "(ImportSummary) for " + name);
				String nextTypeName = next.getType();
				if ((nextTypeName == null) || (nextTypeName.equals(name))) {
					PackageSummary nextPackage = next.getPackage();
					TypeSummary result = query(nextPackage, name);
					if (result != null) {
						return result;
					}
				}
			}
		}

		return null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  summary  Description of Parameter
	 *@param  name     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	private static TypeSummary checkType(FileSummary summary, String name)
	{
		Iterator typeIterator = summary.getTypes();
		if (typeIterator != null) {
			while (typeIterator.hasNext()) {
				TypeSummary nextType = (TypeSummary) typeIterator.next();
				//System.out.println("Seaching " + nextType.getName() + "(TypeSummary) for " + name);
				if ((nextType != null) && nextType.getName().equals(name)) {
					return nextType;
				}
			}
		}

		return null;
	}
}
