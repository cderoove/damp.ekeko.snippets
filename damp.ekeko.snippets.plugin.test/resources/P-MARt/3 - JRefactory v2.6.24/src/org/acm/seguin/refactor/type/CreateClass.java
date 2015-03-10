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
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTCompilationUnit;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.refactor.method.AddConcreteMethod;
import org.acm.seguin.refactor.method.AddConstructor;
import org.acm.seguin.refactor.method.AddMethodTypeVisitor;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetPackageSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.summary.query.SamePackage;
import org.acm.seguin.summary.query.TopLevelDirectory;

/**
 *  This object creates a class from nothing. It is responsible for building
 *  up the parse tree from scratch to create a new class.
 *
 *@author    Chris Seguin
 */
public class CreateClass {
	private TypeSummary typeSummary;
	private String newClassName;
	private boolean isParent;
	private boolean isAbstract;
	private boolean isFinal;
	private String packageNameString;
	private String scope;


	/**
	 *  Constructor for the CreateClass object
	 *
	 *@param  existing     The existing class we are building upon
	 *@param  className    The name of the new class
	 *@param  parent       Are we building a parent or child from the existing
	 *      type
	 *@param  packageName  the name of the package that the class is in
	 */
	public CreateClass(TypeSummary existing, String className,
			boolean parent, String packageName)
	{
		typeSummary = existing;
		newClassName = className;
		isParent = parent;
		isAbstract = true;
		isFinal = false;
		packageNameString = packageName;
		scope = "";
	}


	/**
	 *  Constructor for the CreateClass object
	 *
	 *@param  existing   The existing class we are building upon
	 *@param  className  The name of the new class
	 *@param  parent     Are we building a parent or child from the existing type
	 */
	public CreateClass(TypeSummary existing, String className,
			boolean parent)
	{
		typeSummary = existing;
		newClassName = className;
		isParent = parent;
		isAbstract = true;
		isFinal = false;
		packageNameString = null;
		scope = "";
	}


	/**
	 *  Constructor for the CreateClass object
	 */
	CreateClass()
	{
		typeSummary = null;
		newClassName = null;
	}


	/**
	 *  Sets the PackageName attribute of the CreateClass object
	 *
	 *@param  value  The new PackageName value
	 */
	public void setPackageName(String value)
	{
		packageNameString = value;
	}


	/**
	 *  Sets the Scope attribute of the CreateClass object
	 *
	 *@param  value  The new Scope value
	 */
	public void setScope(String value)
	{
		scope = value;
	}


	/**
	 *  Sets the Abstract attribute of the CreateClass object
	 *
	 *@param  way  The new Abstract value
	 */
	public void setAbstract(boolean way)
	{
		isAbstract = way;
	}


	/**
	 *  Sets the Final attribute of the CreateClass object
	 *
	 *@param  way  The new Final value
	 */
	public void setFinal(boolean way)
	{
		isFinal = way;
	}


	/**
	 *  Creates the the designated class
	 *
	 *@return                           Description of the Returned Value
	 *@exception  RefactoringException  Description of Exception
	 */
	public File run() throws RefactoringException
	{
		if (newClassName == null) {
			throw new RefactoringException("No class name specified");
		}

		if (typeSummary == null) {
			throw new RefactoringException("No type to build upon");
		}

		if (packageNameString == null) {
			packageNameString = GetPackageSummary.query(typeSummary).getName();
		}

		//  Create the AST
		ASTCompilationUnit root = new ASTCompilationUnit(0);

		//  Create the package statement
		int nextIndex = 0;

		if ((packageNameString != null) && (packageNameString.length() > 0)) {
			ASTPackageDeclaration packDecl = createPackageDeclaration();
			root.jjtAddChild(packDecl, 0);
			nextIndex++;
		}

		TypeSummary parentSummary = null;
		ASTName parentName;
		if (isParent) {
			TypeDeclSummary parentDecl = typeSummary.getParentClass();
			parentSummary = GetTypeSummary.query(parentDecl);
			if (parentSummary == null) {
				parentSummary = GetTypeSummary.query(
						PackageSummary.getPackageSummary("java.lang"),
						"Object");
			}
		}
		else {
			parentSummary = typeSummary;
		}
		parentName = getNameFromSummary(parentSummary);

		//  If necessary, create the import statement
		int typeIndex = nextIndex;
		boolean added = addImportStatement(parentSummary, parentName, root, nextIndex);
		if (added) {
			typeIndex++;

			parentName = new ASTName(0);
			parentName.addNamePart(parentSummary.getName());
		}

		//  Create the class
		ASTTypeDeclaration td = createTypeDeclaration(parentName);
		root.jjtAddChild(td, typeIndex);

		addConstructors(parentSummary, root);

		if (!isAbstract) {
			addMethods(typeSummary, root);
		}

		//  Print this new one
		File dest = print(newClassName, root);
		return dest;
	}


	/**
	 *  Converts the type summary into a name
	 *
	 *@param  summary  the summary
	 *@return          the name
	 */
	ASTName getNameFromSummary(TypeSummary summary)
	{
		ASTName name = new ASTName(0);
		if ((summary == null) || summary.getName().equals("Object")) {
			name.fromString("Object");
		}
		else {
			PackageSummary packageSummary = getPackageSummary(summary);
			if (packageSummary.isTopLevel()) {
				name.fromString(summary.getName());
			}
			else if (!isSamePackage(packageNameString, summary)) {
				name.fromString(packageSummary.getName() + "." + summary.getName());
			}
			else {
				name.fromString(summary.getName());
			}
		}

		return name;
	}


	/**
	 *  Gets the SamePackage attribute of the AddAbstractParent object
	 *
	 *@param  parentSummary  Description of Parameter
	 *@param  packageName    Description of Parameter
	 *@return                The SamePackage value
	 */
	boolean isSamePackage(String packageName, TypeSummary parentSummary)
	{
		return (parentSummary != null) &&
				SamePackage.query(packageName, parentSummary);
	}


	/**
	 *  Creates the package declaration
	 *
	 *@return    the package declaration
	 */
	ASTPackageDeclaration createPackageDeclaration()
	{
		ASTPackageDeclaration packDecl = new ASTPackageDeclaration(0);
		ASTName packName = new ASTName(0);
		packName.fromString(packageNameString);
		packDecl.jjtAddChild(packName, 0);

		return packDecl;
	}


	/**
	 *  Adds the import statement and returns true if the import statement was
	 *  necessary
	 *
	 *@param  parentSummary  the parent summary
	 *@param  parentName     the parent name
	 *@param  root           the tree being built
	 *@return                true if the import statement was added
	 */
	boolean addImportStatement(TypeSummary parentSummary,
			ASTName parentName, ASTCompilationUnit root,
			int index)
	{
		if (!isImportRequired(parentSummary)) {
			return false;
		}

		//  Create the import statement
		ASTImportDeclaration importDecl = new ASTImportDeclaration(0);
		importDecl.jjtAddChild(parentName, 0);
		root.jjtAddChild(importDecl, index);
		return true;
	}


	/**
	 *  Creates the type declaration
	 *
	 *@param  grandparentName  Description of Parameter
	 *@return                  the modified class
	 */
	ASTTypeDeclaration createTypeDeclaration(ASTName grandparentName)
	{
		ASTTypeDeclaration td = new ASTTypeDeclaration(0);

		ASTClassDeclaration cd = createModifiedClass(grandparentName);
		td.jjtAddChild(cd, 0);

		return td;
	}


	/**
	 *  Creates the modified class
	 *
	 *@param  grandparentName  The name of the parent class
	 *@return                  the modified class
	 */
	ASTClassDeclaration createModifiedClass(ASTName grandparentName)
	{
		ASTClassDeclaration cd = new ASTClassDeclaration(0);
		if (isAbstract) {
			cd.addModifier("abstract");
		}
		if (isFinal) {
			cd.addModifier("final");
		}
		if (scope.length() > 0) {
			cd.addModifier(scope);
		}

		ASTUnmodifiedClassDeclaration ucd = createClassBody(newClassName, grandparentName);
		cd.jjtAddChild(ucd, 0);

		return cd;
	}


	/**
	 *  Creates the body. The protection level is package so it can be easily
	 *  tested.
	 *
	 *@param  parentName       Description of Parameter
	 *@param  grandparentName  Description of Parameter
	 *@return                  the class
	 */
	ASTUnmodifiedClassDeclaration createClassBody(String parentName, ASTName grandparentName)
	{
		ASTUnmodifiedClassDeclaration ucd = new ASTUnmodifiedClassDeclaration(0);
		ucd.setName(parentName);
		ucd.jjtAddChild(grandparentName, 0);
		ucd.jjtAddChild(new ASTClassBody(0), 1);
		return ucd;
	}


	/**
	 *  Prints the file
	 *
	 *@param  name  The name of the object
	 *@param  root  The root of the tree
	 *@return       The file that the parse tree was written to
	 */
	File print(String name, SimpleNode root)
	{
		File parent = getDirectory();
		File destFile = new File(parent, name + ".java");

		try {
			(new PrettyPrintFile()).apply(destFile, root);
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}

		return destFile;
	}


	/**
	 *  Determines if we need to add an import
	 *
	 *@param  parentSummary  the parent summary
	 *@return                true if the import is necessary
	 */
	private boolean isImportRequired(TypeSummary parentSummary)
	{
		return !isSamePackage(packageNameString, parentSummary) &&
				!isSamePackage("java.lang", parentSummary);
	}


	/**
	 *  Gets the package summary
	 *
	 *@param  base  The type whose package was are concerned about
	 *@return       the package summary
	 */
	private PackageSummary getPackageSummary(TypeSummary base)
	{
		return GetPackageSummary.query(base);
	}


	/**
	 *  Gets the SameParent attribute of the AddAbstractParent object
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 *@return      The SameParent value
	 */
	private boolean isSameParent(TypeSummary one, TypeSummary two)
	{
		if (isObject(one)) {
			return isObject(two);
		}

		if (isObject(two)) {
			return false;
		}

		return one.equals(two);
	}


	/**
	 *  Gets the Object attribute of the AddAbstractParent object
	 *
	 *@param  item  Description of Parameter
	 *@return       The Object value
	 */
	private boolean isObject(TypeSummary item)
	{
		if (item == null) {
			return true;
		}

		if (item.getName().equals("Object")) {
			return true;
		}

		return false;
	}


	/**
	 *  Creates a file object pointing to the directory that this class should be
	 *  created into
	 *
	 *@return    the directory
	 */
	private File getDirectory()
	{
		return TopLevelDirectory.getPackageDirectory(typeSummary, packageNameString);
	}


	/**
	 *  Adds the constructors
	 *
	 *@param  parentType  The feature to be added to the Constructors attribute
	 *@param  root        The feature to be added to the Constructors attribute
	 */
	private void addConstructors(TypeSummary parentType, SimpleNode root)
	{
		Iterator iter = parentType.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				if (next.isConstructor()) {
					AddConstructor ac = new AddConstructor(next, newClassName);
					ac.update(root);
					AddMethodTypeVisitor amtv = new AddMethodTypeVisitor(false);
					amtv.visit(next, root);
				}
			}
		}
	}


	/**
	 *  Adds the methods
	 *
	 *@param  type  The feature to be added to the Methods attribute
	 *@param  root  The feature to be added to the Methods attribute
	 */
	private void addMethods(TypeSummary type, SimpleNode root)
	{
		AbstractMethodFinder finder = new AbstractMethodFinder(type);
		LinkedList list = finder.getList();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();
			AddConcreteMethod ac = new AddConcreteMethod(next);
			ac.update(root);
			AddMethodTypeVisitor amtv = new AddMethodTypeVisitor(false);
			amtv.visit(next, root);
		}
	}
}
