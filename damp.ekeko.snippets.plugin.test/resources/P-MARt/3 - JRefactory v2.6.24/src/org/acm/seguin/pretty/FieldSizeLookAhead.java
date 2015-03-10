/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;
import org.acm.seguin.parser.JavaParserConstants;
import org.acm.seguin.parser.Token;
import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  Helps determine the size of a field for spacing purposes
 *
 *@author    Chris Seguin
 */
class FieldSizeLookAhead {
	private FieldSize fieldSize;
	private int code;


	/**
	 *  Constructor for the FieldSizeLookAhead object
	 *
	 *@param  init  Description of Parameter
	 */
	public FieldSizeLookAhead(int init)
	{
		fieldSize = new FieldSize();
		code = init;
	}


	/**
	 *  Main processing method for the FieldSizeLookAhead object
	 *
	 *@param  body  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public FieldSize run(SimpleNode body)
	{
		int last = body.jjtGetNumChildren();
		for (int ndx = 0; ndx < last; ndx++) {
			SimpleNode child = (SimpleNode) body.jjtGetChild(ndx);
			if (child.jjtGetChild(0) instanceof ASTFieldDeclaration) {
				ASTFieldDeclaration field = (ASTFieldDeclaration) child.jjtGetChild(0);
				if ((code != PrintData.DFS_NOT_WITH_JAVADOC) || !isJavadocAttached(field)) {
					int equalsLength = computeEqualsLength(field);
					fieldSize.setMinimumEquals(equalsLength);
				}
			}
		}

		return fieldSize;
	}


	/**
	 *  Compute the size of the modifiers, type, and name
	 *
	 *@param  field  the field in question
	 *@return        the size of the modifiers, type, and name
	 */
	public int computeEqualsLength(ASTFieldDeclaration field)
	{
		int modifierLength = computeModifierLength(field);
		int typeLength = computeTypeLength(field);
		int nameLength = computeNameLength(field);

		int equalsLength = modifierLength + typeLength + nameLength;
		return equalsLength;
	}


	/**
	 *  Computes the length of the field declaration type
	 *
	 *@param  field  the field
	 *@return        the number
	 */
	public int computeTypeLength(ASTFieldDeclaration field)
	{
		ASTType typeNode = (ASTType) field.jjtGetChild(0);
		int typeLength = 2 * typeNode.getArrayCount();
		if (typeNode.jjtGetChild(0) instanceof ASTPrimitiveType) {
			ASTPrimitiveType primitive = (ASTPrimitiveType) typeNode.jjtGetChild(0);
			typeLength += primitive.getName().length();
		}
		else {
			ASTName name = (ASTName) typeNode.jjtGetChild(0);
			typeLength += name.getName().length();
		}
		fieldSize.setTypeLength(typeLength);
		return typeLength;
	}


	/**
	 *  Gets the JavadocAttached attribute of the FieldSizeLookAhead object
	 *
	 *@param  node  Description of Parameter
	 *@return       The JavadocAttached value
	 */
	private boolean isJavadocAttached(ASTFieldDeclaration node)
	{
		return
				hasJavadoc(node.getSpecial("static")) ||
				hasJavadoc(node.getSpecial("transient")) ||
				hasJavadoc(node.getSpecial("volatile")) ||
				hasJavadoc(node.getSpecial("final")) ||
				hasJavadoc(node.getSpecial("public")) ||
				hasJavadoc(node.getSpecial("protected")) ||
				hasJavadoc(node.getSpecial("private")) ||
				hasJavadoc(getInitialToken((ASTType) node.jjtGetChild(0)));
	}


	/**
	 *  Check the initial token, and removes it from the object.
	 *
	 *@param  top  the type
	 *@return      the initial token
	 */
	private Token getInitialToken(ASTType top)
	{
		if (top.jjtGetChild(0) instanceof ASTPrimitiveType) {
			ASTPrimitiveType primitiveType = (ASTPrimitiveType) top.jjtGetChild(0);
			return primitiveType.getSpecial("primitive");
		}
		else {
			ASTName name = (ASTName) top.jjtGetChild(0);
			return name.getSpecial("id0");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  field  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private int computeNameLength(ASTFieldDeclaration field)
	{
		ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) field.jjtGetChild(1).jjtGetChild(0);
		int nameLength = id.getName().length();
		fieldSize.setNameLength(nameLength);
		return nameLength;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  field  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private int computeModifierLength(ASTFieldDeclaration field)
	{
		int fieldLength = field.getModifiersString().length();
		fieldSize.setModifierLength(fieldLength);
		return fieldLength;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  tok  Description of Parameter
	 *@return      Description of the Returned Value
	 */
	private boolean hasJavadoc(Token tok)
	{
		Token current = tok;
		while (current != null) {
			if (current.kind == JavaParserConstants.FORMAL_COMMENT) {
				return true;
			}

			current = current.specialToken;
		}

		return false;
	}
}
