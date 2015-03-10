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

import org.acm.seguin.parser.ast.ASTCompilationUnit;
import org.acm.seguin.parser.ast.ASTInterfaceBody;
import org.acm.seguin.parser.ast.ASTInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.TopLevelDirectory;

/**
 *  This object creates an interface from nothing. It is responsible for
 *  building up the parse tree from scratch to create a new interface.
 *
 *@author     Grant Watson
 *@created    November 28, 2000
 */
public class CreateNewInterface {
	private String m_interfaceName;
	private String m_packageName;
	private Summary m_summary;


	/**
	 *  Constructor for the CreateNewInterface object
	 *
	 *@param  interfaceName  Description of Parameter
	 *@param  packageName    Description of Parameter
	 *@param  summary        Description of Parameter
	 */
	public CreateNewInterface(Summary summary, String packageName, String interfaceName)
	{
		m_summary = summary;
		m_packageName = packageName;
		m_interfaceName = interfaceName;
	}


	/**
	 *  Constructor for the CreateNewInterface object
	 */
	CreateNewInterface()
	{
		m_interfaceName = null;
		m_packageName = null;
	}


	/**
	 *  Creates the the designated class
	 *
	 *@return                           Description of the Returned Value
	 *@exception  RefactoringException  Description of Exception
	 */
	public File run() throws RefactoringException
	{
		if (m_packageName == null) {
			throw new RefactoringException("No package name specified");
		}

		if (m_interfaceName == null) {
			throw new RefactoringException("No interface name specified");
		}

		//  Create the AST
		ASTCompilationUnit root = new ASTCompilationUnit(0);

		//  Create the package statement
		int nextIndex = 0;

		if ((m_packageName != null) && (m_packageName.length() > 0)) {
			ASTPackageDeclaration packDecl = createPackageDeclaration();
			root.jjtAddChild(packDecl, 0);
			nextIndex++;
		}

		ASTName parentName = new ASTName(0);

		//  Create the class
		ASTTypeDeclaration td = createTypeDeclaration();
		root.jjtAddChild(td, nextIndex);

		//  Print this new one
		File dest = print(m_interfaceName, root);
		return dest;
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
		packName.fromString(m_packageName);
		packDecl.jjtAddChild(packName, 0);

		return packDecl;
	}


	/**
	 *  Creates the type declaration
	 *
	 *@return    the modified class
	 */
	ASTTypeDeclaration createTypeDeclaration()
	{
		ASTTypeDeclaration td = new ASTTypeDeclaration(0);
		ASTInterfaceDeclaration id = createModifiedClass();
		td.jjtAddChild(id, 0);

		return td;
	}


	/**
	 *  Creates the modified class
	 *
	 *@return    the modified class
	 */
	ASTInterfaceDeclaration createModifiedClass()
	{
		ASTInterfaceDeclaration id = new ASTInterfaceDeclaration(0);
		id.addModifier("public");
		ASTUnmodifiedInterfaceDeclaration uid = createClassBody(m_interfaceName);
		id.jjtAddChild(uid, 0);
		return id;
	}


	/**
	 *  Creates the body. The protection level is package so it can be easily
	 *  tested.
	 *
	 *@param  parentName  Description of Parameter
	 *@return             the class
	 */
	ASTUnmodifiedInterfaceDeclaration createClassBody(String parentName)
	{
		ASTUnmodifiedInterfaceDeclaration uid = new ASTUnmodifiedInterfaceDeclaration(0);
		uid.setName(parentName);
		ASTInterfaceBody ib = new ASTInterfaceBody(0);
		uid.jjtAddChild(ib, 0);
		return uid;
	}


	/**
	 *  Prints the file
	 *
	 *@param  root           Description of Parameter
	 *@param  interfaceName  Description of Parameter
	 *@return                Description of the Returned Value
	 */
	File print(String interfaceName, SimpleNode root)
	{
		File parent = TopLevelDirectory.getPackageDirectory(m_summary, m_packageName);

		// Create directory if it doesn't exist
		if (!parent.exists()) {
			parent.mkdir();
		}
		File destFile = new File(parent, interfaceName + ".java");

		try {
			(new PrettyPrintFile()).apply(destFile, root);
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}

		return destFile;
	}


	/**
	 *  Gets the package summary
	 *
	 *@param  base  Description of Parameter
	 *@return       the package summary
	 */
	private PackageSummary getPackageSummary(Summary base)
	{
		Summary current = base;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		return (PackageSummary) current;
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
}
