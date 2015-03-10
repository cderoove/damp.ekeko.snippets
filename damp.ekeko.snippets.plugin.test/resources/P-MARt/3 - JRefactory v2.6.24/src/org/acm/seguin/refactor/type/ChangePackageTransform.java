package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.NameFactory;
import org.acm.seguin.refactor.TransformAST;

/**
 *  This object revises the package statement 
 *
 *@author     Chris Seguin 
 *@created    October 23, 1999 
 */
public class ChangePackageTransform extends TransformAST {
	private ASTName name;


	/**
	 *  Constructor for the ChangePackageTransform object 
	 *
	 *@param  name  Description of Parameter 
	 */
	public ChangePackageTransform(ASTName name) {
		this.name = name;
	}


	/**
	 *  Constructor for the ChangePackageTransform object 
	 *
	 *@param  packageName  Description of Parameter 
	 */
	public ChangePackageTransform(String packageName) {
		name = NameFactory.getName(packageName, null);
	}


	/**
	 *  Update the syntax tree 
	 *
	 *@param  root  the root of the syntax tree 
	 */
	public void update(SimpleNode root) {
		if (name.getName().length() == 0) {
			removePackage(root);
		}
		else {
			addPackage(root);
		}
	}


	/**
	 *  Adds a feature to the Package attribute of the ChangePackageTransform 
	 *  object 
	 *
	 *@param  root  The feature to be added to the Package attribute 
	 */
	private void addPackage(SimpleNode root) {
		SimpleNode first = (SimpleNode) root.jjtGetChild(0);

		ASTPackageDeclaration pack = new ASTPackageDeclaration(0);
		pack.jjtAddChild(name, 0);

		if (first instanceof ASTPackageDeclaration) {
			root.jjtAddChild(pack, 0);
		}
		else {
			root.jjtInsertChild(pack, 0);
		}
	}


	/**
	 *  Description of the Method 
	 *
	 *@param  root  Description of Parameter 
	 */
	private void removePackage(SimpleNode root) {
		SimpleNode first = (SimpleNode) root.jjtGetChild(0);
		if (first instanceof ASTPackageDeclaration) {
			root.jjtDeleteChild(0);
		}
	}
}
