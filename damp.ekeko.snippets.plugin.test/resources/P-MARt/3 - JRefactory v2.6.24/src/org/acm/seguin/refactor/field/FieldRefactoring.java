/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Base class for all field refactorings
 *
 *@author    Chris Seguin
 */
abstract class FieldRefactoring extends Refactoring {
	/**
	 *  The name of the field
	 */
	protected String field;
	/**
	 *  The type summary that contains the field
	 */
	protected TypeSummary typeSummary;


	/**
	 *  Constructor for the FieldRefactoring object
	 */
	public FieldRefactoring()
	{
		super();
	}


	/**
	 *  Sets the Class attribute of the PullupFieldRefactoring object
	 *
	 *@param  packageName  the package name
	 *@param  className    the class name
	 */
	public void setClass(String packageName, String className)
	{
		setClass(GetTypeSummary.query(
				PackageSummary.getPackageSummary(packageName),
				className));
	}


	/**
	 *  Sets the Class attribute of the PullupFieldRefactoring object
	 *
	 *@param  init  The new Class value
	 */
	public void setClass(TypeSummary init)
	{
		typeSummary = init;
	}


	/**
	 *  Sets the Field attribute of the PullupFieldRefactoring object
	 *
	 *@param  fieldName  The new Field value
	 */
	public void setField(String fieldName)
	{
		field = fieldName;
	}


	/**
	 *  Determines if the specified type is in java.lang package
	 *
	 *@param  type  the type
	 *@return       true if it is in the package
	 */
	protected boolean isInJavaLang(ASTName type)
	{
		return (type.getNameSize() == 3) &&
				(type.getNamePart(0).equals("java")) &&
				(type.getNamePart(1).equals("lang"));
	}


	/**
	 *  Determines if the specified type is in java.lang package
	 *
	 *@param  type  the type
	 *@return       true if it is in the package
	 */
	protected boolean isInJavaLang(TypeSummary type)
	{
		return getPackage(type).getName().equals("java.lang");
	}


	/**
	 *  Gets the package summary for the specific object
	 *
	 *@param  current  the summary
	 *@return          the package summary
	 */
	protected PackageSummary getPackage(Summary current)
	{
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		return (PackageSummary) current;
	}


	/**
	 *  Gets the package summary for the specific object
	 *
	 *@param  current  the summary
	 *@return          the package summary
	 */
	protected FileSummary getFileSummary(Summary current)
	{
		while (!(current instanceof FileSummary)) {
			current = current.getParent();
		}
		return (FileSummary) current;
	}


	/**
	 *  Gets the FieldType attribute of the PullupFieldRefactoring object
	 *
	 *@param  node         Description of Parameter
	 *@param  fileSummary  Description of Parameter
	 *@return              The FieldType value
	 */
	protected Object getFieldType(SimpleNode node, FileSummary fileSummary)
	{
		ASTFieldDeclaration child = (ASTFieldDeclaration) node.jjtGetChild(0);
		ASTType type = (ASTType) child.jjtGetChild(0);
		if (type.jjtGetChild(0) instanceof ASTPrimitiveType) {
			return null;
		}
		ASTName name = (ASTName) type.jjtGetChild(0);
		if (name.getNameSize() == 1) {
			return GetTypeSummary.query(fileSummary, name.getName());
		}
		else {
			return name;
		}
	}
}
