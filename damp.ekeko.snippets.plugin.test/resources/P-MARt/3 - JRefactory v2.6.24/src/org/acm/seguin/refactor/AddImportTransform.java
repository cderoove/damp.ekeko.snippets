/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.NameFactory;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  This object revises the import statements in the tree.
 *
 *@author    Chris Seguin
 */
public class AddImportTransform extends TransformAST {
	private ASTName name;
	private boolean ignorePackageName;


	/**
	 *  Constructor for the AddImportTransform object
	 *
	 *@param  name  Description of Parameter
	 */
	public AddImportTransform(ASTName name) {
		this.name = name;
		ignorePackageName = false;
	}


	/**
	 *  Constructor for the AddImportTransform object
	 *
	 *@param  packageName  the package name
	 *@param  className    the class name
	 */
	public AddImportTransform(String packageName, String className) {
		name = NameFactory.getName(packageName, className);
		ignorePackageName = false;
	}


	/**
	 *  Constructor for the AddImportTransform object
	 *
	 *@param  typeSummary  the type symmary
	 */
	public AddImportTransform(TypeSummary typeSummary) {
		Summary current = typeSummary;
		while (!(current instanceof PackageSummary)) {
			current = current.getParent();
		}
		PackageSummary packageSummary = (PackageSummary) current;
		name = NameFactory.getName(packageSummary.getName(), typeSummary.getName());
		ignorePackageName = false;
	}


	/**
	 *  Sets the IgnorePackageName attribute of the AddImportTransform object
	 *
	 *@param  value  The new IgnorePackageName value
	 */
	public void setIgnorePackageName(boolean value) {
		ignorePackageName = value;
	}


	/**
	 *  Update the syntax tree
	 *
	 *@param  root  Description of Parameter
	 */
	public void update(SimpleNode root) {
		if ((name.getNameSize() == 3) &&
				(name.getNamePart(0).equals("java")) &&
				(name.getNamePart(1).equals("lang"))) {
			return;
		}

		int nFirstImportSpot = findLastImport(root);
		if (nFirstImportSpot == -1) {
			return;
		}

		//  Create the import
		ASTImportDeclaration importDecl = new ASTImportDeclaration(0);
		importDecl.jjtAddChild(name, 0);
		importDecl.setImportPackage(false);

		//  Add it to the source tree
		root.jjtInsertChild(importDecl, nFirstImportSpot);
	}


	/**
	 *  Determine where the first import should go
	 *
	 *@param  root  Description of Parameter
	 *@return       the index where new import statements should go
	 */
	protected int findLastImport(SimpleNode root) {
		int last = root.jjtGetNumChildren();
		for (int ndx = 0; ndx < last; ndx++) {
			Node child = root.jjtGetChild(ndx);

			//  Check to see if we have already imported this
			if ((!ignorePackageName) && (child instanceof ASTPackageDeclaration)) {
				ASTPackageDeclaration decl = (ASTPackageDeclaration) child;
				ASTName packageName = (ASTName) child.jjtGetChild(0);
				if (packageName.getNameSize() + 1 == name.getNameSize()) {
					boolean done = true;
					for (int ndx2 = 0; ndx2 < packageName.getNameSize(); ndx2++) {
						if (!packageName.getNamePart(ndx2).equals(name.getNamePart(ndx2))) {
							done = false;
						}
					}

					if (done) {
						return -1;
					}
				}
			}

			//  Check to see if we have already imported this
			if (child instanceof ASTImportDeclaration) {
				ASTImportDeclaration decl = (ASTImportDeclaration) child;
				ASTName importName = (ASTName) child.jjtGetChild(0);
				if (importName.equals(name)) {
					return -1;
				}
			}

			//  Found a type declaration, time to return the index
			if (root.jjtGetChild(ndx) instanceof ASTTypeDeclaration) {
				return ndx;
			}
		}

		//  No point - there aren't any types defined.
		return -1;
	}
}
