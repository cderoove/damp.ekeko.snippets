/* Generated By:JJTree: Do not edit this line. ASTInterfaceDeclaration.java */

package net.sourceforge.pmd.ast;

public class ASTInterfaceDeclaration extends AccessNode  {
    public ASTInterfaceDeclaration(int id) {
        super(id);
    }

    public ASTInterfaceDeclaration(JavaParser p, int id) {
        super(p, id);
    }

    public ASTUnmodifiedInterfaceDeclaration getUnmodifedInterfaceDeclaration() {
        return (ASTUnmodifiedInterfaceDeclaration) jjtGetChild(0);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void dump(String prefix) {
        System.out.println(collectDumpedModifiers(prefix));
        dumpChildren(prefix);
    }

}
