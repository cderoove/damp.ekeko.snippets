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

package org.netbeans.modules.antlr.editor.g;

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

import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ANTLRParser extends antlr.LLkParser
            implements ANTLRTokenTypes
{

    protected ANTLRParser(TokenBuffer tokenBuf, int k) {
        super(tokenBuf,k);
        tokenNames = _tokenNames;
    }

    public ANTLRParser(TokenBuffer tokenBuf) {
        this(tokenBuf,2);
    }

    protected ANTLRParser(TokenStream lexer, int k) {
        super(lexer,k);
        tokenNames = _tokenNames;
    }

    public ANTLRParser(TokenStream lexer) {
        this(lexer,2);
    }

    public ANTLRParser(ParserSharedInputState state) {
        super(state,2);
        tokenNames = _tokenNames;
    }

    public final void grammar() throws ParserException, IOException {


        try {      // for error handling
            matchNot(EOF);
            $setType(C_TEXT);
        }
        catch (ParserException ex) {
            reportError(ex);
            consume();
            consumeUntil(_tokenSet_0);
        }
    }


    public static final String[] _tokenNames = {
        "<0>",
        "EOF",
        "<2>",
        "NULL_TREE_LOOKAHEAD",
        "\"TextColor\"",
        "\"tokens\"",
        "\"options\"",
        "DOC_COMMENT",
        "OPTIONS",
        "TOKENS",
        "SEMPRED",
        "WS",
        "COMMENT",
        "SL_COMMENT",
        "ML_COMMENT",
        "COMMA",
        "QUESTION",
        "TREE_BEGIN",
        "LPAREN",
        "RPAREN",
        "COLON",
        "STAR",
        "PLUS",
        "ASSIGN",
        "IMPLIES",
        "SEMI",
        "CARET",
        "BANG",
        "OR",
        "WILDCARD",
        "RANGE",
        "NOT_OP",
        "RCURLY",
        "CHAR_LITERAL",
        "STRING_LITERAL",
        "ESC",
        "DIGIT",
        "XDIGIT",
        "VOCAB",
        "INT",
        "ARG_ACTION",
        "NESTED_ARG_ACTION",
        "ACTION",
        "NESTED_ACTION",
        "TOKEN_REF",
        "RULE_REF",
        "WS_LOOP",
        "INTERNAL_RULE_REF",
        "WS_OPT",
        "NOT_USEFUL"
    };

    private static final long _tokenSet_0_data_[] = { 2L, 0L };
    public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);

}
