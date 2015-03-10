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

import java.io.IOException;
import antlr.TokenBuffer;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.ParserException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;
public class CalcParser extends antlr.LLkParser
            implements CalcParserTokenTypes
{

    protected CalcParser(TokenBuffer tokenBuf, int k) {
        super(tokenBuf,k);
        tokenNames = _tokenNames;
    }

    public CalcParser(TokenBuffer tokenBuf) {
        this(tokenBuf,1);
    }

    protected CalcParser(TokenStream lexer, int k) {
        super(lexer,k);
        tokenNames = _tokenNames;
    }

    public CalcParser(TokenStream lexer) {
        this(lexer,1);
    }

    public CalcParser(ParserSharedInputState state) {
        super(state,1);
        tokenNames = _tokenNames;
    }

    public final void expr() throws ParserException, IOException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        AST expr_AST = null;

        try {      // for error handling
            mexpr();
            astFactory.addASTChild(currentAST, returnAST);
            {
_loop3:
                do {
                    if ((LA(1)==PLUS)) {
                        AST tmp3_AST = null;
                        tmp3_AST = (AST)astFactory.create(LT(1));
                        astFactory.makeASTRoot(currentAST, tmp3_AST);
                        match(PLUS);
                        mexpr();
                        astFactory.addASTChild(currentAST, returnAST);
                    }
                    else {
                        break _loop3;
                    }

                } while (true);
            }
            AST tmp4_AST = null;
            tmp4_AST = (AST)astFactory.create(LT(1));
            match(SEMI);
            expr_AST = (AST)currentAST.root;
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
        returnAST = expr_AST;
    }

    public final void mexpr() throws ParserException, IOException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        AST mexpr_AST = null;

        try {      // for error handling
            atom();
            astFactory.addASTChild(currentAST, returnAST);
            {
_loop6:
                do {
                    if ((LA(1)==STAR)) {
                        AST tmp5_AST = null;
                        tmp5_AST = (AST)astFactory.create(LT(1));
                        astFactory.makeASTRoot(currentAST, tmp5_AST);
                        match(STAR);
                        atom();
                        astFactory.addASTChild(currentAST, returnAST);
                    }
                    else {
                        break _loop6;
                    }

                } while (true);
            }
            mexpr_AST = (AST)currentAST.root;
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_1);
        }
        returnAST = mexpr_AST;
    }

    public final void atom() throws ParserException, IOException {

        returnAST = null;
        ASTPair currentAST = new ASTPair();
        AST atom_AST = null;

        try {      // for error handling
            AST tmp6_AST = null;
            tmp6_AST = (AST)astFactory.create(LT(1));
            astFactory.addASTChild(currentAST, tmp6_AST);
            match(INT);
            atom_AST = (AST)currentAST.root;
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_2);
        }
        returnAST = atom_AST;
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

    private static final long _tokenSet_0_data_[] = { 2L, 0L };
    public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
    private static final long _tokenSet_1_data_[] = { 48L, 0L };
    public static final BitSet _tokenSet_1 = new BitSet(_tokenSet_1_data_);
    private static final long _tokenSet_2_data_[] = { 112L, 0L };
    public static final BitSet _tokenSet_2 = new BitSet(_tokenSet_2_data_);

}
