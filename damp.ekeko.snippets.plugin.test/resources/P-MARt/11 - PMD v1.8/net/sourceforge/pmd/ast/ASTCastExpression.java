/* Generated By:JJTree: Do not edit this line. ASTCastExpression.java */

package net.sourceforge.pmd.ast;

public class ASTCastExpression extends SimpleNode {
    public ASTCastExpression(int id) {
        super(id);
    }

    public ASTCastExpression(JavaParser p, int id) {
        super(p, id);
    }


    /** Accept the visitor. **/
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
