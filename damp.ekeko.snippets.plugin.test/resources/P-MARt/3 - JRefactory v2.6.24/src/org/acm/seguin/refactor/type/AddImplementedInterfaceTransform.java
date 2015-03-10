/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.type;

import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;

/**
 *  This object will add a new interface to the implements clause of a class
 *  declaration. If no implements clause exists, one will be added.
 *
 *@author     Grant Watson
 *@created    December 1, 2000
 */
public class AddImplementedInterfaceTransform extends TransformAST {
	private ASTName m_interfaceName;


	/**
	 *  Constructor for the AddImplementsTransform object
	 *
	 *@param  interfaceName  Description of Parameter
	 */
	public AddImplementedInterfaceTransform(ASTName interfaceName)
	{
		m_interfaceName = interfaceName;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  root  Description of Parameter
	 */
	public void update(SimpleNode root)
	{
		AddImplementedInterfaceVisitor aiiv = new AddImplementedInterfaceVisitor();
		root.jjtAccept(aiiv, m_interfaceName);
	}

}
