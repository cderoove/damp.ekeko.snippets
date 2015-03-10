/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.NameFactory;
import org.acm.seguin.summary.PackageSummary;

/**
 *  This object revises the import statements in the tree. 
 *
 *@author    Chris Seguin 
 */
public class RemoveImportTransform extends TransformAST {
	private ASTName name;
	private boolean packageImport;


	/**
	 *  Constructor for the RemoveImportTransform object 
	 *
	 *@param  name  Description of Parameter 
	 */
	public RemoveImportTransform(ASTName name) {
		this.name = name;
		packageImport = false;
	}


	/**
	 *  Constructor for the RemoveImportTransform object 
	 *
	 *@param  packageName  Description of Parameter 
	 *@param  className    Description of Parameter 
	 */
	public RemoveImportTransform(String packageName, String className) {
		name = NameFactory.getName(packageName, className);
		packageImport = false;
	}


	/**
	 *  Constructor for the RemoveImportTransform object 
	 *
	 *@param  summary  Description of Parameter 
	 */
	public RemoveImportTransform(PackageSummary summary) {
		this.name = new ASTName(0);
		this.name.fromString(summary.getName());
		packageImport = true;
	}


	/**
	 *  Update the syntax tree 
	 *
	 *@param  root  Description of Parameter 
	 */
	public void update(SimpleNode root) {
		//  Local Variables
		int ndx = 0;

		//  While we aren't done
		while (ndx < root.jjtGetNumChildren()) {
			if (isInvalid((SimpleNode) root.jjtGetChild(ndx))) {
				//  Delete this child
				root.jjtDeleteChild(ndx);
			}
			else {
				ndx++;
			}
		}
	}


	/**
	 *  This method determines if the particular child of the compilation unit 
	 *  should be deleted. 
	 *
	 *@param  child  Description of Parameter 
	 *@return        Description of the Returned Value 
	 */
	protected boolean isInvalid(SimpleNode child) {
		if (child instanceof ASTImportDeclaration) {
			//  Cast this to an import
			ASTImportDeclaration importDecl = (ASTImportDeclaration) child;

			//  Check each of the targets
			if (!packageImport) {
				return importDecl.jjtGetChild(0).equals(name);
			}
			else {
				ASTName nameNode = (ASTName) importDecl.jjtGetChild(0);
				String code = nameNode.getName();
				String packageName = name.getName();

				return (code.equals(packageName));
			}
		}

		//  We have passed all the tests - so it can't be the one
		return false;
	}
}
