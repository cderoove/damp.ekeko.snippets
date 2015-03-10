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

package org.netbeans.editor.ext;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;

/**
* Syntax analyzes for Java source files.
* Tokens and internal states are given below. 
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSyntax extends Syntax {

    // Token IDs
    public static final int TEXT = 0;             // plain text - tabs and spaces
    public static final int ERROR = 1;            // errorneous text
    public static final int KEYWORD = 2;          // keyword
    public static final int IDENTIFIER = 3;       // identifier
    public static final int FUNCTION = 4;         // function call i.e. name()
    public static final int OPERATOR = 5;         // operators like '+', '*=' etc.
    public static final int LINE_COMMENT = 6;     // comment till end of line
    public static final int BLOCK_COMMENT = 7;    // block comment
    public static final int CHAR = 8;             // char constant e.g. 'c'
    public static final int STRING = 9;           // string constant e.g. "string" // NOI18N
    public static final int INT = 10;             // integer constant e.g. 1234
    public static final int HEX = 11;             // hex constant e.g. 0x5a
    public static final int OCTAL = 12;           // octal constant e.g. 0123
    public static final int LONG = 13;            // long constant e.g. 12L
    public static final int FLOAT = 14;           // float constant e.g. 1.5e+43
    public static final int DOUBLE = 15;           // double constant e.g. 1.5e+43

    // Operator IDs
    public static final int EQ = 0;               // =
    public static final int LT = 1;               // <
    public static final int GT = 2;               // >
    public static final int LLT = 3;              // <<
    public static final int GGT = 4;              // >>
    public static final int GGGT = 5;             // >>>
    public static final int PLUS = 6;             // +
    public static final int MINUS = 7;            // -
    public static final int MUL = 8;              // *
    public static final int DIV = 9;              // /
    public static final int AND = 10;             // &
    public static final int OR = 11;              // |
    public static final int XOR = 12;             // ^
    public static final int MOD = 13;             // %
    public static final int NOT = 14;             // !
    public static final int NEG = 15;             // ~

    public static final int EQ_EQ = 16;           // ==
    public static final int LE = 17;              // <=
    public static final int GE = 18;              // >=
    public static final int LLE = 19;             // <<=
    public static final int GGE = 20;             // >>=
    public static final int GGGE = 21;            // >>>=
    public static final int PLUS_EQ = 22;         // +=
    public static final int MINUS_EQ = 23;        // -=
    public static final int MUL_EQ = 24;          // *=
    public static final int DIV_EQ = 25;          // /=
    public static final int AND_EQ = 26;          // &=
    public static final int OR_EQ = 27;           // |=
    public static final int XOR_EQ = 28;          // ^=
    public static final int MOD_EQ = 29;          // %=
    public static final int NOT_EQ = 30;          // !=

    public static final int DOT = 31;             // .
    public static final int COMMA = 32;           // ,
    public static final int COLON = 33;           // :
    public static final int SEMICOLON = 34;       // ;
    public static final int QUESTION = 35;        // ?
    public static final int LEFT_PARENTHESES = 36;        // (
    public static final int RIGHT_PARENTHESES = 37;       // )
    public static final int LEFT_SQUARE_BRACKET = 38;     // [
    public static final int RIGHT_SQUARE_BRACKET = 39;    // ]
    public static final int LEFT_BRACE = 40;      // {
    public static final int RIGHT_BRACE = 41;     // }
    public static final int PLUS_PLUS = 42;       // ++
    public static final int MINUS_MINUS = 43;     // --
    public static final int AND_AND = 44;         // &&
    public static final int OR_OR = 45;           // ||

    // Internal states
    private static final int ISI_ERROR = 1; // after carriage return
    private static final int ISI_TEXT = 2; // inside white space
    private static final int ISI_WS_P_IDENTIFIER = 3; // inside WS past identifier
    private static final int ISI_LINE_COMMENT = 4; // inside line comment //
    private static final int ISI_BLOCK_COMMENT = 5; // inside block comment /* ... */
    private static final int ISI_STRING = 6; // inside string constant
    private static final int ISI_STRING_A_BSLASH = 7; // inside string constant after backslash
    private static final int ISI_CHAR = 8; // inside char constant
    private static final int ISI_CHAR_A_BSLASH = 9; // inside char constant after backslash
    private static final int ISI_IDENTIFIER = 10; // inside identifier
    private static final int ISA_SLASH = 11; // slash char
    private static final int ISA_EQ = 12; // after '='
    private static final int ISA_GT = 13; // after '>'
    private static final int ISA_GTGT = 14; // after '>>'
    private static final int ISA_GTGTGT = 15; // after '>>>'
    private static final int ISA_LT = 16; // after '<'
    private static final int ISA_LTLT = 17; // after '<<'
    private static final int ISA_PLUS = 18; // after '+'
    private static final int ISA_MINUS = 19; // after '-'
    private static final int ISA_STAR = 20; // after '*'
    private static final int ISA_STAR_I_BLOCK_COMMENT = 21; // after '*'
    private static final int ISA_PIPE = 22; // after '|'
    private static final int ISA_PERCENT = 23; // after '%'
    private static final int ISA_AND = 24; // after '&'
    private static final int ISA_XOR = 25; // after '^'
    private static final int ISA_EXCLAMATION = 26; // after '!'
    private static final int ISA_ZERO = 27; // after '0'
    private static final int ISI_INT = 28; // integer number
    private static final int ISI_OCTAL = 29; // octal number
    private static final int ISI_DOUBLE = 30; // double number
    private static final int ISI_DOUBLE_EXP = 31; // double number
    private static final int ISI_HEX = 32; // hex number
    private static final int ISA_DOT = 33; // after '.'

    // Texts of the operators
    private static final String[] operatorTexts = new String[] {
                "=", "<", ">", "<<", ">>", ">>>", "+", "-", // NOI18N
                "*", "/", "&", "|", "^", "%", "!", "~",  // NOI18N
                "==", "<=", ">=", "<<=", ">>=", ">>>=", "+=", "-=",  // NOI18N
                "*=", "/=", "&=", "|=", "^=", "%=", "!=",  // NOI18N
                ".", ",", ":", ";", "?", "(", ")", "[", "]", "{", "}", // NOI18N
                "++", "--", "&&", "||" // NOI18N
            };


    /** Helper index used for function coloring */
    int methodNameEndOffset = -1; // -1 means invalid value

    public JavaSyntax() {
        highestTokenID = DOUBLE;
    }

    protected int parseToken() {
        char actChar;

        while(offset < stopOffset) {
            actChar = buffer[offset];

            switch (state) {
            case INIT:
                switch (actChar) {
                case '\n':
                    offset++;
                    return EOL;
                case ' ':
                case '\t':
                    state = ISI_TEXT;
                    break;
                case '"': // NOI18N
                    state = ISI_STRING;
                    break;
                case '\'':
                    state = ISI_CHAR;
                    break;
                case '/':
                    state = ISA_SLASH;
                    break;
                case '=':
                    state = ISA_EQ;
                    break;
                case '>':
                    state = ISA_GT;
                    break;
                case '<':
                    state = ISA_LT;
                    break;
                case '+':
                    state = ISA_PLUS;
                    break;
                case '-':
                    state = ISA_MINUS;
                    break;
                case '*':
                    state = ISA_STAR;
                    break;
                case '|':
                    state = ISA_PIPE;
                    break;
                case '%':
                    state = ISA_PERCENT;
                    break;
                case '&':
                    state = ISA_AND;
                    break;
                case '^':
                    state = ISA_XOR;
                    break;
                case '~':
                    offset++;
                    helperID = NEG;
                    return OPERATOR;
                case '!':
                    state = ISA_EXCLAMATION;
                    break;
                case '0':
                    state = ISA_ZERO;
                    break;
                case '.':
                    state = ISA_DOT;
                    break;
                case ',':
                    offset++;
                    helperID = COMMA;
                    return OPERATOR;
                case ';':
                    offset++;
                    helperID = SEMICOLON;
                    return OPERATOR;
                case ':':
                    offset++;
                    helperID = COLON;
                    return OPERATOR;
                case '?':
                    offset++;
                    helperID = QUESTION;
                    return OPERATOR;
                case '(':
                    offset++;
                    helperID = LEFT_PARENTHESES;
                    return OPERATOR;
                case ')':
                    offset++;
                    helperID = RIGHT_PARENTHESES;
                    return OPERATOR;
                case '[':
                    offset++;
                    helperID = LEFT_SQUARE_BRACKET;
                    return OPERATOR;
                case ']':
                    offset++;
                    helperID = RIGHT_SQUARE_BRACKET;
                    return OPERATOR;
                case '{':
                    offset++;
                    helperID = LEFT_BRACE;
                    return OPERATOR;
                case '}':
                    offset++;
                    helperID = RIGHT_BRACE;
                    return OPERATOR;

                default:
                    if (actChar >= '1' && actChar <= '9') { // '0' already handled
                        state = ISI_INT;
                        break;
                    }

                    if (Character.isJavaIdentifierStart(actChar)) { // identifier
                        state = ISI_IDENTIFIER;
                        break;
                    }

                    offset++;
                    return ERROR;
                }
                break;

            case ISI_ERROR:
                switch (actChar) {
                case ' ':
                case '\t':
                case '\n':
                    state = INIT;
                    return ERROR;
                }
                break;

            case ISI_TEXT: // white space
                if (actChar != ' ' && actChar != '\t') {
                    state = INIT;
                    return TEXT;
                }
                break;

            case ISI_WS_P_IDENTIFIER:
                switch (actChar) {
                case ' ':
                case '\t':
                    break;
                default:
                    offset = methodNameEndOffset;
                    methodNameEndOffset = -1; // make methodNameEndOffset invalid
                    state = INIT;
                    return (actChar == '(') ? FUNCTION : IDENTIFIER;
                }
                break;

            case ISI_LINE_COMMENT:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return LINE_COMMENT;
                }
                break;

            case ISI_BLOCK_COMMENT:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // only '\n'
                        offset++;
                        return EOL; // stay in ISI_BLOCK_COMMENT state for next line
                    } else { // return comment token to qualify for previous if()
                        return BLOCK_COMMENT;
                    }
                case '*':
                    state = ISA_STAR_I_BLOCK_COMMENT;
                    break;
                }
                break;

            case ISI_STRING:
                switch (actChar) {
                case '\\':
                    state = ISI_STRING_A_BSLASH;
                    break;
                case '\n':
                    state = INIT;
                    return STRING;
                case '"': // NOI18N
                    offset++;
                    state = INIT;
                    return STRING;
                }
                break;

            case ISI_STRING_A_BSLASH:
                switch (actChar) {
                case '"': // NOI18N
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_STRING;
                break;

            case ISI_CHAR:
                switch (actChar) {
                case '\\':
                    state = ISI_CHAR_A_BSLASH;
                    break;
                case '\n':
                    state = INIT;
                    return CHAR;
                case '\'':
                    offset++;
                    state = INIT;
                    return CHAR;
                }
                break;

            case ISI_CHAR_A_BSLASH:
                switch (actChar) {
                case '\'':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                state = ISI_CHAR;
                break;

            case ISI_IDENTIFIER:
                if (!(Character.isJavaIdentifierPart(actChar))) {
                    helperID = JavaKeywords.getKeyword(buffer, tokenOffset, offset - tokenOffset);
                    if (helperID >= 0) {
                        state = INIT;
                        return KEYWORD;
                    } else {
                        switch (actChar) {
                        case '(': // it's function
                            state = INIT;
                            return FUNCTION;
                        case ' ':
                        case '\t':
                            state = ISI_WS_P_IDENTIFIER;
                            methodNameEndOffset = offset; // end of identifier
                            break;
                        default:
                            state = INIT;
                            return IDENTIFIER;
                        }
                    }
                }
                break;

            case ISA_SLASH:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = DIV_EQ;
                    return OPERATOR;
                case '/':
                    state = ISI_LINE_COMMENT;
                    break;
                case '*':
                    state = ISI_BLOCK_COMMENT;
                    break;
                default:
                    state = INIT;
                    helperID = DIV;
                    return OPERATOR;
                }
                break;

            case ISA_EQ:
                switch (actChar) {
                case '=':
                    offset++;
                    helperID = EQ_EQ;
                    return  OPERATOR;
                default:
                    state = INIT;
                    helperID = EQ;
                    return OPERATOR;
                }
                // break;

            case ISA_GT:
                switch (actChar) {
                case '>':
                    state = ISA_GTGT;
                    break;
                case '=':
                    offset++;
                    helperID = GE;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = GT;
                    return OPERATOR;
                }
                break;

            case ISA_GTGT:
                switch (actChar) {
                case '>':
                    state = ISA_GTGTGT;
                    break;
                case '=':
                    offset++;
                    helperID = GGE;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = GGT;
                    return OPERATOR;
                }
                break;

            case ISA_GTGTGT:
                switch (actChar) {
                case '=':
                    offset++;
                    helperID = GGGE;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = GGGT;
                    return OPERATOR;
                }
                // break;


            case ISA_LT:
                switch (actChar) {
                case '<':
                    state = ISA_LTLT;
                    break;
                case '=':
                    offset++;
                    helperID = LE;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = LT;
                    return OPERATOR;
                }
                break;

            case ISA_LTLT:
                switch (actChar) {
                case '<':
                    state = ISI_ERROR;
                    break;
                case '=':
                    offset++;
                    helperID = LLE;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = LLT;
                    return OPERATOR;
                }
                break;

            case ISA_PLUS:
                switch (actChar) {
                case '+':
                    offset++;
                    helperID = PLUS_PLUS;
                    return OPERATOR;
                case '=':
                    offset++;
                    helperID = PLUS_EQ;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = PLUS;
                    return OPERATOR;
                }
                // break;

            case ISA_MINUS:
                switch (actChar) {
                case '-':
                    offset++;
                    helperID = MINUS_MINUS;
                    return OPERATOR;
                case '=':
                    offset++;
                    helperID = MINUS_EQ;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = MINUS;
                    return OPERATOR;
                }
                // break;

            case ISA_STAR:
                switch (actChar) {
                case '=':
                    offset++;
                    helperID = MUL_EQ;
                    return OPERATOR;
                case '/':
                    offset++;
                    state = INIT;
                    return ERROR; // '*/' outside comment
                default:
                    state = INIT;
                    helperID = MUL;
                    return OPERATOR;
                }
                // break;

            case ISA_STAR_I_BLOCK_COMMENT:
                switch (actChar) {
                case '/':
                    offset++;
                    state = INIT;
                    return BLOCK_COMMENT;
                default:
                    offset--;
                    state = ISI_BLOCK_COMMENT;
                    break;
                }
                break;

            case ISA_PIPE:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = OR_EQ;
                    return OPERATOR;
                case '|':
                    offset++;
                    state = INIT;
                    helperID = OR_OR;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = OR;
                    return OPERATOR;
                }
                // break;

            case ISA_PERCENT:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = MOD_EQ;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = MOD;
                    return OPERATOR;
                }
                // break;

            case ISA_AND:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = AND_EQ;
                    return OPERATOR;
                case '&':
                    offset++;
                    state = INIT;
                    helperID = AND_AND;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = AND;
                    return OPERATOR;
                }
                // break;

            case ISA_XOR:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = XOR_EQ;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = XOR;
                    return OPERATOR;
                }
                // break;

            case ISA_EXCLAMATION:
                switch (actChar) {
                case '=':
                    offset++;
                    state = INIT;
                    helperID = NOT_EQ;
                    return OPERATOR;
                default:
                    state = INIT;
                    helperID = NOT;
                    return OPERATOR;
                }
                // break;

            case ISA_ZERO:
                switch (actChar) {
                case '.':
                    state = ISI_DOUBLE;
                    break;
                case 'x':
                case 'X':
                    state = ISI_HEX;
                    break;
                case 'l':
                case 'L':
                    offset++;
                    state = INIT;
                    return LONG;
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return FLOAT;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return DOUBLE;
                case '8': // it's error to have '8' and '9' in octal number
                case '9':
                    state = ISI_ERROR;
                    break;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (actChar >= '0' && actChar <= '7') {
                        state = ISI_OCTAL;
                        break;
                    }
                    state = INIT;
                    return INT;
                }
                break;

            case ISI_INT:
                switch (actChar) {
                case 'l':
                case 'L':
                    offset++;
                    state = INIT;
                    return LONG;
                case '.':
                    state = ISI_DOUBLE;
                    break;
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return FLOAT;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return DOUBLE;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!(actChar >= '0' && actChar <= '9')) {
                        state = INIT;
                        return INT;
                    }
                }
                break;

            case ISI_OCTAL:
                if (!(actChar >= '0' && actChar <= '7')) {

                    state = INIT;
                    return OCTAL;
                }
                break;

            case ISI_DOUBLE:
                switch (actChar) {
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return FLOAT;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return DOUBLE;
                case 'e':
                case 'E':
                    state = ISI_DOUBLE_EXP;
                    break;
                default:
                    if (!((actChar >= '0' && actChar <= '9')
                            || actChar == '.')) {

                        state = INIT;
                        return DOUBLE;
                    }
                }
                break;

            case ISI_DOUBLE_EXP:
                switch (actChar) {
                case 'f':
                case 'F':
                    offset++;
                    state = INIT;
                    return FLOAT;
                case 'd':
                case 'D':
                    offset++;
                    state = INIT;
                    return DOUBLE;
                default:
                    if (!((actChar >= '0' && actChar <= '9')
                            || actChar == '-' || actChar == '+')) {
                        state = INIT;
                        return DOUBLE;
                    }
                }
                break;

            case ISI_HEX:
                if (!((actChar >= 'a' && actChar <= 'f')
                        || (actChar >= 'A' && actChar <= 'F')
                        || (actChar >= '0' && actChar <= '9'))) {

                    state = INIT;
                    return HEX;
                }
                break;

            case ISA_DOT:
                if (actChar >= '0' && actChar <= '9') {
                    state = ISI_DOUBLE;
                    break;
                }
                state = INIT;
                helperID = DOT;
                return OPERATOR;

            } // end of switch(state)

            offset = ++offset;
        } // end of while(offset...)

        /** At this stage there's no more text in the scanned buffer.
        * Scanner first checks whether this is completely the last
        * available buffer.
        */

        if (lastBuffer) {
            switch(state) {
            case ISI_ERROR:
                return ERROR;
            case ISI_TEXT:
                return TEXT;
            case ISI_IDENTIFIER:
                helperID = JavaKeywords.getKeyword(buffer, tokenOffset, offset - tokenOffset);
                return (helperID >= 0) ? KEYWORD : IDENTIFIER;
            case ISI_WS_P_IDENTIFIER:
                offset = methodNameEndOffset;
                methodNameEndOffset = -1;
                state = INIT;
                return IDENTIFIER;
            case ISI_LINE_COMMENT:
                return LINE_COMMENT;
            case ISI_BLOCK_COMMENT:
            case ISA_STAR_I_BLOCK_COMMENT:
                return BLOCK_COMMENT;
            case ISI_STRING:
            case ISI_STRING_A_BSLASH:
                return STRING;
            case ISI_CHAR:
            case ISI_CHAR_A_BSLASH:
                return CHAR;
            case ISA_ZERO:
            case ISI_INT:
                return INT;
            case ISI_OCTAL:
                return OCTAL;
            case ISI_DOUBLE:
                return DOUBLE;
            case ISI_DOUBLE_EXP:
                return DOUBLE;
            case ISI_HEX:
                return HEX;
            case ISA_DOT:
                helperID = DOT;
                return OPERATOR;
            case ISA_SLASH:
                helperID = DIV;
                return OPERATOR;
            case ISA_EQ:
                helperID = EQ;
                return OPERATOR;
            case ISA_GT:
                helperID = GT;
                return OPERATOR;
            case ISA_GTGT:
                helperID = GGT;
                return OPERATOR;
            case ISA_GTGTGT:
                helperID = GGGT;
                return OPERATOR;
            case ISA_LT:
                helperID = LT;
                return OPERATOR;
            case ISA_LTLT:
                helperID = LLT;
                return OPERATOR;
            case ISA_PLUS:
                helperID = PLUS;
                return OPERATOR;
            case ISA_MINUS:
                helperID = MINUS;
                return OPERATOR;
            case ISA_STAR:
                helperID = MUL;
                return OPERATOR;
            case ISA_PIPE:
                helperID = OR;
                return OPERATOR;
            case ISA_PERCENT:
                helperID = MOD;
                return OPERATOR;
            case ISA_AND:
                helperID = AND;
                return OPERATOR;
            case ISA_XOR:
                helperID = XOR;
                return OPERATOR;
            case ISA_EXCLAMATION:
                helperID = NOT;
                return OPERATOR;
            }
        }

        /* At this stage there's no more text in the scanned buffer, but
        * this buffer is not the last so the scan will continue on another buffer.
        * The scanner tries to minimize the amount of characters
        * that will be prescanned in the next buffer by returning the token
        * where possible.
        */

        switch (state) {
        case ISI_ERROR:
            return ERROR;
        case ISI_TEXT:
            return TEXT;
        }

        return EOT;

    }

    public void relocate(char buffer[], int offset, int len, boolean lastBuffer) {
        if (methodNameEndOffset >= 0) { // relocate methodNameEndOffset before calling super.relocScan()
            methodNameEndOffset += (offset - this.offset);
        }
        super.relocate(buffer, offset, len, lastBuffer);
    }

    /** Create scan state appropriate for particular scanner */
    public StateInfo createStateInfo() {
        return new JavaStateInfo();
    }

    /** Store state of this scanner into given scan state. */
    public void storeState(StateInfo stateInfo) {
        super.storeState(stateInfo);
        ((JavaStateInfo)stateInfo).methodNameEndPreScan = (methodNameEndOffset >= 0)
                ? (offset - methodNameEndOffset) : -1;
    }

    /** Load state into scanner. Indexes are already initialized
    * when this function is called.
    */
    public void loadState(StateInfo stateInfo) {
        super.loadState(stateInfo);
        int mtdEnd = ((JavaStateInfo)stateInfo).methodNameEndPreScan;
        methodNameEndOffset = (mtdEnd >= 0) ? (offset - mtdEnd) : -1;
    }

    /** Initialize scanner in case the state stored in syntax mark
    * is null.
    */
    public void loadInitState() {
        super.loadInitState();
        methodNameEndOffset = -1;
    }

    public String getTokenName(int tokenID) {
        switch (tokenID) {
        case TEXT:
            return TN_TEXT;
        case ERROR:
            return TN_ERROR;
        case KEYWORD:
            return TN_KEYWORD;
        case IDENTIFIER:
            return TN_IDENTIFIER;
        case FUNCTION:
            return TN_FUNCTION;
        case OPERATOR:
            return TN_OPERATOR;
        case LINE_COMMENT:
            return TN_LINE_COMMENT;
        case BLOCK_COMMENT:
            return TN_BLOCK_COMMENT;
        case CHAR:
            return TN_CHAR;
        case STRING:
            return TN_STRING;
        case INT:
            return TN_INT;
        case HEX:
            return TN_HEX;
        case OCTAL:
            return TN_OCTAL;
        case LONG:
            return TN_LONG;
        case FLOAT:
            return TN_FLOAT;
        case DOUBLE:
            return TN_DOUBLE;
        default:
            return super.getTokenName(tokenID);
        }
    }

    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case ISI_ERROR:
            return "ISI_ERROR"; // NOI18N
        case ISI_TEXT:
            return "ISI_TEXT"; // NOI18N
        case ISI_WS_P_IDENTIFIER:
            return "ISI_WS_P_IDENTIFIER"; // NOI18N
        case ISI_LINE_COMMENT:
            return "ISI_LINE_COMMENT"; // NOI18N
        case ISI_BLOCK_COMMENT:
            return "ISI_BLOCK_COMMENT"; // NOI18N
        case ISI_STRING:
            return "ISI_STRING"; // NOI18N
        case ISI_STRING_A_BSLASH:
            return "ISI_STRING_A_BSLASH"; // NOI18N
        case ISI_CHAR:
            return "ISI_CHAR"; // NOI18N
        case ISI_CHAR_A_BSLASH:
            return "ISI_CHAR_A_BSLASH"; // NOI18N
        case ISI_IDENTIFIER:
            return "ISI_IDENTIFIER"; // NOI18N
        case ISA_SLASH:
            return "ISA_SLASH"; // NOI18N
        case ISA_EQ:
            return "ISA_EQ"; // NOI18N
        case ISA_GT:
            return "ISA_GT"; // NOI18N
        case ISA_GTGT:
            return "ISA_GTGT"; // NOI18N
        case ISA_GTGTGT:
            return "ISA_GTGTGT"; // NOI18N
        case ISA_LT:
            return "ISA_LT"; // NOI18N
        case ISA_LTLT:
            return "ISA_LTLT"; // NOI18N
        case ISA_PLUS:
            return "ISA_PLUS"; // NOI18N
        case ISA_MINUS:
            return "ISA_MINUS"; // NOI18N
        case ISA_STAR:
            return "ISA_STAR"; // NOI18N
        case ISA_STAR_I_BLOCK_COMMENT:
            return "ISA_STAR_I_BLOCK_COMMENT"; // NOI18N
        case ISA_PIPE:
            return "ISA_PIPE"; // NOI18N
        case ISA_PERCENT:
            return "ISA_PERCENT"; // NOI18N
        case ISA_AND:
            return "ISA_AND"; // NOI18N
        case ISA_XOR:
            return "ISA_XOR"; // NOI18N
        case ISA_EXCLAMATION:
            return "ISA_EXCLAMATION"; // NOI18N
        case ISA_ZERO:
            return "ISA_ZERO"; // NOI18N
        case ISI_INT:
            return "ISI_INT"; // NOI18N
        case ISI_OCTAL:
            return "ISI_OCTAL"; // NOI18N
        case ISI_DOUBLE:
            return "ISI_DOUBLE"; // NOI18N
        case ISI_DOUBLE_EXP:
            return "ISI_DOUBLE_EXP"; // NOI18N
        case ISI_HEX:
            return "ISI_HEX"; // NOI18N
        case ISA_DOT:
            return "ISA_DOT"; // NOI18N

        default:
            return super.getStateName(stateNumber);
        }
    }

    public static String getOperatorText(int operatorID) {
        return (operatorID >= 0 && operatorID < operatorTexts.length)
               ? operatorTexts[operatorID] : ("Unknown operator " + operatorID); // NOI18N
    }

    public String toString() {
        String s = super.toString();
        s += ", methodNameEndOffset=" + methodNameEndOffset; // NOI18N
        return s;
    }

    class JavaStateInfo extends Syntax.BaseStateInfo {

        /** Helper prescan for function coloring */
        int methodNameEndPreScan;

    }

}

/*
 * Log
 *  24   Gandalf   1.23        1/14/00  Miloslav Metelka Localization
 *  23   Gandalf   1.22        1/13/00  Miloslav Metelka Localization
 *  22   Gandalf   1.21        12/28/99 Miloslav Metelka 
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/10/99 Miloslav Metelka 
 *  19   Gandalf   1.18        10/7/99  Miloslav Metelka int to double fix
 *  18   Gandalf   1.17        10/4/99  Miloslav Metelka 
 *  17   Gandalf   1.16        9/16/99  Miloslav Metelka 
 *  16   Gandalf   1.15        9/15/99  Miloslav Metelka 
 *  15   Gandalf   1.14        9/10/99  Miloslav Metelka 
 *  14   Gandalf   1.13        8/27/99  Miloslav Metelka 
 *  13   Gandalf   1.12        8/9/99   Petr Jiricka    Corrections to changes 
 *       made for JSP support - recognition of %> tag. Java (non-jsp) syntax is 
 *       not affected
 *  12   Gandalf   1.11        8/6/99   Petr Jiricka    Added hook to recognize 
 *       end of a Java block in JSP. Java syntax (without JSP) is not affected.
 *  11   Gandalf   1.10        7/20/99  Miloslav Metelka 
 *  10   Gandalf   1.9         6/22/99  Miloslav Metelka 
 *  9    Gandalf   1.8         6/8/99   Miloslav Metelka 
 *  8    Gandalf   1.7         6/1/99   Miloslav Metelka 
 *  7    Gandalf   1.6         5/24/99  Miloslav Metelka 
 *  6    Gandalf   1.5         5/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         5/13/99  Miloslav Metelka 
 *  4    Gandalf   1.3         5/5/99   Miloslav Metelka 
 *  3    Gandalf   1.2         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  2    Gandalf   1.1         3/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

