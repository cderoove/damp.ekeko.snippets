package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;

/**
 *  Description of the Class 
 *
 *@author    Chris Seguin 
 */
public class RemoveSamePackageTransform extends TransformAST {
	/**
	 *  Update the syntax tree 
	 *
	 *@param  root  the root of the syntax tree 
	 */
	public void update(SimpleNode root) {
		ASTName packageName = getPackageName(root);

		int ndx = 0;
		while (ndx < root.jjtGetNumChildren()) {
			Node next = root.jjtGetChild(ndx);
			if (next instanceof ASTImportDeclaration) {
				if (isImporting(packageName, (ASTImportDeclaration) next)) {
					root.jjtDeleteChild(ndx);
				}
				else {
					ndx++;
				}
			}
			else {
				ndx++;
			}
		}
	}


	/**
	 *  Gets the PackageName attribute of the RemoveSamePackageTransform object 
	 *
	 *@param  root  Description of Parameter 
	 *@return       The PackageName value 
	 */
	private ASTName getPackageName(SimpleNode root) {
		SimpleNode node = (SimpleNode) root.jjtGetChild(0);
		if (node instanceof ASTPackageDeclaration) {
			return (ASTName) node.jjtGetChild(0);
		}
		else {
			return null;
		}
	}


	/**
	 *  Gets the Importing attribute of the RemoveSamePackageTransform object 
	 *
	 *@param  packageName  Description of Parameter 
	 *@param  importDecl   Description of Parameter 
	 *@return              The Importing value 
	 */
	private boolean isImporting(ASTName packageName, ASTImportDeclaration importDecl) {
		ASTName name = (ASTName) importDecl.jjtGetChild(0);

		if (packageName == null) {
			return (name.getNameSize() == 1);
		}

		if (importDecl.isImportingPackage()) {
			return name.equals(packageName);
		}
		else {
			return (packageName.getNameSize() + 1 == name.getNameSize()) && 
					name.startsWith(packageName);
		}
	}
}
