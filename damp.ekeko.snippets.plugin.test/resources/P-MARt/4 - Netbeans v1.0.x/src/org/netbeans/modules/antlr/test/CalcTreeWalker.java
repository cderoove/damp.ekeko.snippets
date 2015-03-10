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

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.ParserException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;

public class CalcTreeWalker extends antlr.TreeParser
            implements CalcParserTokenTypes
{
    public CalcTreeWalker() {
        tokenNames = _tokenNames;
    }

    public final float  expr(AST _t) throws ParserException {
        float r;

        AST expr_AST_in = (AST)_t;
        AST i = null;

        float a,b;
        r=0;


        try {      // for error handling
            if (_t==null) _t=ASTNULL;
            switch ( _t.getType()) {
            case PLUS:
                {
                    AST __t20 = _t;
                    AST tmp1_AST_in = (AST)_t;
                    match(_t,PLUS);
                    _t = _t.getFirstChild();
                    a=expr(_t);
                    _t = _retTree;
                    b=expr(_t);
                    _t = _retTree;
                    _t = __t20;
                    _t = _t.getNextSibling();
                    r = a+b;
                    break;
                }
            case STAR:
                {
                    AST __t21 = _t;
                    AST tmp2_AST_in = (AST)_t;
                    match(_t,STAR);
                    _t = _t.getFirstChild();
                    a=expr(_t);
                    _t = _retTree;
                    b=expr(_t);
                    _t = _retTree;
                    _t = __t21;
                    _t = _t.getNextSibling();
                    r = a*b;
                    break;
                }
            case INT:
                {
                    i = (AST)_t;
                    match(_t,INT);
                    _t = _t.getNextSibling();
                    r = (float)Integer.parseInt(i.getText());
                    break;
                }
            default:
                {
                    throw new NoViableAltException(_t);
                }
            }
        }
        catch (ParserException ex) {
            reportError(ex);
            if (_t!=null) {_t = _t.getNextSibling();}
        }
        _retTree = _t;
        return r;
    }


    public static final String[] _tokenNames = {
        "<0>",
        "EOF",
        "<2>",
        "NULL_TREE_LOOKAHEAD",
        "PLUS",
        "SEMI",
        "STAR",
        "INT",
        "WS",
        "LPAREN",
        "RPAREN",
        "DIGIT"
    };

}

