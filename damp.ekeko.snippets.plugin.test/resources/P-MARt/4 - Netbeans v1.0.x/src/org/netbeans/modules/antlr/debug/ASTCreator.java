/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.antlr.debug;

import antlr.*;
import antlr.collections.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class ASTCreator extends Object {
    ASTFactory astFactory;
    AST root = null;

    /** Creates new ASTCreator */
    public ASTCreator() { init(null); }
    public ASTCreator(int type) { init(type); }
    public ASTCreator(int type,String text) { init(type,text); }

    public void init(AST ast) {
        astFactory = new ASTFactory();
        root = ast;
    }
    public void init(int type) {
        astFactory = new ASTFactory();
        root = c(type);
    }
    public void init(int type,String text) {
        astFactory = new ASTFactory();
        root = c(type,text);
    }

    public void p1() {
        System.out.println( root.toStringList() );
    }
    public void p2() {
        DumpASTVisitor visitor = new DumpASTVisitor();
        visitor.visit(root);
    }
    public AST c(int type) {
        AST ast = astFactory.create(type);
        return ast;
    }
    public AST c(int type,String text) {
        AST ast = astFactory.create(type,text);
        return ast;
    }
    public AST a() { return root; }

    public AST a(AST c1) { return a(new AST[]{c1}); }
    public AST a(AST c1, AST c2) { return a(new AST[]{c1,c2}); }
    public AST a(AST c1, AST c2, AST c3) { return a(new AST[]{c1,c2,c3}); }
    public AST a(AST[] ast) {
        for (int i=0;i<ast.length;i++) {
            if (ast[i]==null) { continue; }
            root.addChild(ast[i]);
        }
        return root;
    }

    public AST a(int c1) { return a(new int[]{c1}); }
    public AST a(int c1, int c2) { return a(new int[]{c1,c2}); }
    public AST a(int c1, int c2, int c3) { return a(new int[]{c1,c2,c3}); }
    public AST a(int[] ast) {
        for (int i=0;i<ast.length;i++) {
            root.addChild(c(ast[i]));
        }
        return root;
    }

    public AST a(int type, String text) {
        root.addChild(c(type,text));
        return root;
    }
}