/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.FieldSummary;

/**
 *  A transform that renames a specific field
 *
 *@author    Chris Seguin
 */
public class RenameFieldTransform extends TransformAST {
	private FieldSummary oldField;
	private String newName;


	/**
	 *  Constructor for the RemoveFieldTransform object
	 *
	 *@param  oldName  Description of Parameter
	 *@param  newName  Description of Parameter
	 */
	public RenameFieldTransform(FieldSummary oldField, String newName)
	{
		this.oldField = oldField;
		this.newName = newName;
	}


	/**
	 *  Updates the root
	 *
	 *@param  root  the root node
	 */
	public void update(SimpleNode root)
	{
		RenameFieldVisitor rfv = new RenameFieldVisitor();
		rfv.visit(root, new RenameFieldData(oldField, newName));
	}
}
