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

import java.util.List;
import java.util.ArrayList;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorDebug;

/**
* Token processor that parses the text and produces jc expressions.
*
* @author Miloslav Metelka
* @version 1.00
*/

class JCTokenProcessor implements TokenProcessor {

    private static final int CONSTANT = JCExpression.CONSTANT;
    private static final int VARIABLE = JCExpression.VARIABLE;
    private static final int OPERATOR = JCExpression.OPERATOR;
    private static final int UNARY_OPERATOR = JCExpression.UNARY_OPERATOR;
    private static final int DOT = JCExpression.DOT;
    private static final int DOT_OPEN = JCExpression.DOT_OPEN;
    private static final int ARRAY_OPEN = JCExpression.ARRAY_OPEN;
    private static final int ARRAY = JCExpression.ARRAY;
    private static final int PARENTHESIS_OPEN = JCExpression.PARENTHESIS_OPEN;
    private static final int PARENTHESIS = JCExpression.PARENTHESIS;
    private static final int METHOD_OPEN = JCExpression.METHOD_OPEN;
    private static final int METHOD = JCExpression.METHOD;
    private static final int CONSTRUCTOR = JCExpression.CONSTRUCTOR;
    private static final int CONVERSION = JCExpression.CONVERSION;
    private static final int TYPE = JCExpression.TYPE;
    private static final int NEW = JCExpression.NEW;
    private static final int INSTANCEOF = JCExpression.INSTANCEOF;

    private static final int NO_EXP = -1;

    /** Buffer that is scanned */
    private char[] buffer;

    /** Start position of the buffer in the document */
    private int bufferStartPos;

    /** Delta of the token processor buffer offsets against the offsets given
    * in the source buffer.
    */
    private int bufferOffsetDelta;

    /** The scanning was stopped by request by the token processor */
    private boolean stopped;

    /** Stack of the expressions. 
     * @associates JCExpression*/
    private ArrayList expStack = new ArrayList();

    // helper variables
    private int curTokenID;
    private int curHelperID;
    private int curOffset;
    private int curTokenLen;

    /** Last position that was reached */
    private int lastPosition;

    JCTokenProcessor() {
    }

    /** Get the expression stack from the bottom to top */
    final List getStack() {
        return expStack;
    }

    /** Get the last token processed */
    final int getLastTokenID() {
        return curTokenID;
    }

    /** Was the scanning stopped by request by the token processor */
    final boolean isStopped() {
        return stopped;
    }

    final int getLastPosition() {
        return lastPosition;
    }

    final JCExpression getResultExp() {
        return peekExp();
    }

    private void clearStack() {
        expStack.clear();
    }

    /** Push exp to top of stack */
    private void pushExp(JCExpression exp) {
        expStack.add(exp);
    }

    /** Pop exp from top of stack */
    private JCExpression popExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? (JCExpression)expStack.remove(cnt - 1) : null;
    }

    /** Look at the exp at top of stack */
    private JCExpression peekExp() {
        int cnt = expStack.size();
        return (cnt > 0) ? (JCExpression)expStack.get(cnt - 1) : null;
    }

    /** Look at the second exp on stack */
    private JCExpression peekExp2() {
        int cnt = expStack.size();
        return (cnt > 1) ? (JCExpression)expStack.get(cnt - 2) : null;
    }

    /** Look at the third exp on stack */
    private JCExpression peekExp(int ind) {
        int cnt = expStack.size();
        return (cnt >= ind) ? (JCExpression)expStack.get(cnt - ind) : null;
    }

    private JCExpression createExp(int id) {
        return new JCExpression(buffer, bufferStartPos, id);
    }

    private JCExpression createTokenExp(int id) {
        JCExpression exp = new JCExpression(buffer, bufferStartPos, id);
        addTokenTo(exp);
        return exp;
    }

    /** Add the token to a given expression */
    private void addTokenTo(JCExpression exp) {
        exp.addToken(curTokenID, curHelperID, curOffset, curTokenLen);
    }

    private int getValidID(JCExpression exp) {
        return (exp != null) ? exp.getID() : NO_EXP;
    }

    /** Check whether there can be any joining performed
    * for current expressions on the stack.
    * @param tokenID tokenID of the current token
    * @param helperID helperID of the current token
    * @return true to continue, false if errorneous construction found
    */
    private boolean checkJoin(int tokenID, int helperID) {
        boolean ret = true;

        boolean cont = true;
        while (cont) {
            cont = false;
            JCExpression top = peekExp();
            JCExpression top2 = peekExp2();
            int top2ID = getValidID(top2);

            switch (getValidID(top)) {
            case CONSTANT:
            case VARIABLE:
            case METHOD:
            case CONSTRUCTOR:
            case ARRAY:
            case DOT:
            case PARENTHESIS:
            case OPERATOR: // operator on top of stack
                switch (top2ID) {
                case UNARY_OPERATOR:
                    switch (tokenID) {
                    case JavaSyntax.OPERATOR:
                        switch (helperID) {
                        case JavaSyntax.DOT:
                        case JavaSyntax.LEFT_PARENTHESES:
                        case JavaSyntax.LEFT_SQUARE_BRACKET:
                        case JavaSyntax.PLUS_PLUS:
                        case JavaSyntax.MINUS_MINUS:
                            break;

                        default: // Join
                            if (top2.getParameterCount() == 0) {
                                popExp(); // pop top
                                top2.addParameter(top);
                            }
                            break;
                        }
                        break;
                    }
                    break;

                case DOT_OPEN:
                    switch (tokenID) {
                    case JavaSyntax.OPERATOR:
                        switch (helperID) {
                        case JavaSyntax.LEFT_PARENTHESES:
                            break;

                        default:
                            popExp();
                            top2.addParameter(top);
                            top2.setID(DOT);
                        }
                    }
                    break;

                }
                break;
            }
        }

        int leftOpID = JCExpression.getOperatorID(tokenID, helperID);
        if (leftOpID >= 0) {
            switch (JCExpression.getOperatorPrecedence(leftOpID)) {
            case 0: // stop ID - try to join the exps on stack
                JCExpression lastVar = null;
                JCExpression rightOp = peekExp();
                int rightOpID = -1;
                rightOpID = JCExpression.getOperatorID(rightOp);
                switch (JCExpression.getOperatorPrecedence(rightOpID)) {
                case 0: // stop - nothing to join
                    rightOp = null;
                    break;

                case 1: // single item - move to next and add this one
                    lastVar = rightOp;
                    rightOp = peekExp2();
                    rightOpID = JCExpression.getOperatorID(rightOp);
                    switch (JCExpression.getOperatorPrecedence(rightOpID)) {
                    case 0: // stop - only one item on the stack
                        rightOp = null;
                        break;

                    case 1: // two items without operator - error
                        ret = false;
                        rightOp = null;
                        break;

                    default:
                        popExp(); // pop item
                        rightOp.addParameter(lastVar); // add item as parameter
                        lastVar = null;
                    }
                    break;
                }

                if (rightOp != null) {
                    popExp(); // pop rightOp
                    cont = true;
                    ArrayList opStack = new ArrayList(); // operator stack
                    JCExpression leftOp = null;
                    do {
                        if (leftOp == null) {
                            leftOp = popExp();
                            if (leftOp == null) {
                                break;
                            }
                            leftOpID = JCExpression.getOperatorID(leftOp);
                        }
                        switch (JCExpression.getOperatorPrecedence(leftOpID)) {
                        case 0: // stop here
                            pushExp(leftOp); // push last exp back to stack
                            cont = false;
                            break;

                        case 1: // item found
                            lastVar = leftOp;
                            leftOp = null; // ask for next pop
                            break;

                        default: // operator found
                            int leftOpPrec = JCExpression.getOperatorPrecedence(leftOpID);
                            int rightOpPrec = JCExpression.getOperatorPrecedence(rightOpID);
                            boolean rightPrec;
                            if (leftOpPrec > rightOpPrec) { // left has greater priority
                                rightPrec = false;
                            } else if (leftOpPrec < rightOpPrec) { // right has greater priority
                                rightPrec = true;
                            } else { // equal priorities
                                rightPrec = JCExpression.isOperatorRightAssociative(rightOpID);
                            }

                            if (rightPrec) { // right operator has precedence
                                if (lastVar != null) {
                                    rightOp.addParameter(lastVar);
                                }
                                if (opStack.size() > 0) { // at least one right stacked op
                                    lastVar = rightOp; // rightOp becomes item
                                    rightOp = (JCExpression)opStack.remove(opStack.size() - 1); // get stacked op
                                    rightOpID = rightOp.getOperatorID(rightOp);
                                } else { // shift the leftOp to rightOp
                                    leftOp.addParameter(rightOp);
                                    lastVar = null;
                                    rightOp = leftOp;
                                    rightOpID = leftOpID;
                                    leftOp = null; // ask for next poping
                                }
                            } else { // left operator has precedence
                                if (lastVar != null) {
                                    leftOp.addParameter(lastVar);
                                    lastVar = null;
                                }
                                opStack.add(rightOp); // push right operator to stack
                                //                      rightOp.addParameter(leftOp);
                                rightOp = leftOp; // shift left op to right op
                                rightOpID = leftOpID;
                                leftOp = null;
                            }
                        }
                    } while (cont);

                    // add possible valid last item
                    if (lastVar != null) {
                        rightOp.addParameter(lastVar);
                    }

                    // pop the whole stack adding the current right op to the stack exp
                    for (int i = opStack.size() - 1; i >= 0; i--) {
                        JCExpression op = (JCExpression)opStack.get(i);
                        op.addParameter(rightOp);
                        rightOp = op;
                    }

                    rightOp.swapOperatorParms();
                    pushExp(rightOp); // push the top operator
                }
                break;
            }
        }

        return ret;
    }

    public boolean token(int tokenID, int helperID, int offset, int tokenLen) {
        offset += bufferOffsetDelta;
        // assign helper variables
        curTokenID = tokenID;
        curHelperID = helperID;
        curOffset = offset;
        curTokenLen = tokenLen;

        boolean err = false; // whether the parser cannot understand given tokens

        checkJoin(tokenID, helperID);

        JCExpression top = peekExp(); // exp at top of stack
        int topID = getValidID(top); // id of the exp at top of stack

        JCExpression constExp = null; // possibly assign constant into this exp
        JCType kwdType = null; // keyword constant type (used in conversions)

        switch (tokenID) { // test the token ID
        case JavaSyntax.KEYWORD:
            switch (helperID) {
            case JavaKeywords.BOOLEAN:
                kwdType = JCompletion.BOOLEAN_TYPE;
                break;
            case JavaKeywords.BYTE:
                kwdType = JCompletion.BYTE_TYPE;
                break;
            case JavaKeywords.CHAR:
                kwdType = JCompletion.CHAR_TYPE;
                break;
            case JavaKeywords.DOUBLE:
                kwdType = JCompletion.DOUBLE_TYPE;
                break;
            case JavaKeywords.FLOAT:
                kwdType = JCompletion.FLOAT_TYPE;
                break;
            case JavaKeywords.INT:
                kwdType = JCompletion.INT_TYPE;
                break;
            case JavaKeywords.LONG:
                kwdType = JCompletion.LONG_TYPE;
                break;
            case JavaKeywords.SHORT:
                kwdType = JCompletion.SHORT_TYPE;
                break;

            case JavaKeywords.TRUE:
            case JavaKeywords.FALSE:
                constExp = createTokenExp(CONSTANT);
                constExp.setType(JCompletion.BOOLEAN_TYPE);
                break;

            case JavaKeywords.NULL:
                constExp = createTokenExp(CONSTANT);
                constExp.setType(JCompletion.NULL_TYPE);
                break;

            case JavaKeywords.CLASS:
                if (topID == DOT_OPEN) {
                    pushExp(createTokenExp(VARIABLE));
                } else {
                    err = true;
                }
                break;

            case JavaKeywords.NEW:
                switch (topID) {
                case VARIABLE:
                case NEW:
                    err = true;
                    break;

                default:
                    pushExp(createTokenExp(NEW));
                    break;
                }
                break;

            case JavaKeywords.SUPER:
            case JavaKeywords.THIS:
                pushExp(createTokenExp(VARIABLE));
                break;

            case JavaKeywords.INSTANCEOF:
                switch (topID) {
                case CONSTANT:
                case VARIABLE:
                case METHOD:
                case CONSTRUCTOR:
                case ARRAY:
                case DOT:
                case PARENTHESIS:
                    pushExp(createTokenExp(INSTANCEOF));
                    break;
                default:
                    err = true;
                    break;
                }
                break;

            case JavaKeywords.VOID:
            case JavaKeywords.ABSTRACT:
            case JavaKeywords.BREAK:
            case JavaKeywords.CASE:
            case JavaKeywords.CATCH:
            case JavaKeywords.CONST:
            case JavaKeywords.CONTINUE:
            case JavaKeywords.DEFAULT:
            case JavaKeywords.DO:
            case JavaKeywords.ELSE:
            case JavaKeywords.EXTENDS:
            case JavaKeywords.FINAL:
            case JavaKeywords.FINALLY:
            case JavaKeywords.FOR:
            case JavaKeywords.GOTO:
            case JavaKeywords.IF:
            case JavaKeywords.IMPLEMENTS:
            case JavaKeywords.IMPORT:
            case JavaKeywords.INTERFACE:
            case JavaKeywords.NATIVE:
            case JavaKeywords.PACKAGE:
            case JavaKeywords.PRIVATE:
            case JavaKeywords.PROTECTED:
            case JavaKeywords.PUBLIC:
            case JavaKeywords.RETURN:
            case JavaKeywords.STATIC:
            case JavaKeywords.SWITCH:
            case JavaKeywords.SYNCHRONIZED:
            case JavaKeywords.THROW:
            case JavaKeywords.THROWS:
            case JavaKeywords.TRANSIENT:
            case JavaKeywords.TRY:
            case JavaKeywords.VOLATILE:
            case JavaKeywords.WHILE:
            default:
                err = true;
                break;

            } // end of keyword switch(helperID)
            break;

        case JavaSyntax.IDENTIFIER: // identifier found e.g. 'a'
        case JavaSyntax.FUNCTION: // method found - handled in the same way here
            {
                switch (topID) {
                case OPERATOR:
                case DOT_OPEN:
                case ARRAY_OPEN:
                case PARENTHESIS_OPEN:
                case METHOD_OPEN:
                case NEW:
                case CONVERSION:
                case UNARY_OPERATOR:
                case INSTANCEOF:
                case NO_EXP:
                    pushExp(createTokenExp(VARIABLE));
                    break;

                default:
                    err = true;
                    break;
                }
            }
            break;

        case JavaSyntax.OPERATOR: // Operator found
            switch (helperID) {
            case JavaSyntax.EQ: // Assignment operators
            case JavaSyntax.PLUS_EQ:
            case JavaSyntax.MINUS_EQ:
            case JavaSyntax.MUL_EQ:
            case JavaSyntax.DIV_EQ:
            case JavaSyntax.AND_EQ:
            case JavaSyntax.OR_EQ:
            case JavaSyntax.XOR_EQ:
            case JavaSyntax.MOD_EQ:
            case JavaSyntax.LLE:
            case JavaSyntax.GGE:
            case JavaSyntax.GGGE:

            case JavaSyntax.LT: // Binary, result is boolean
            case JavaSyntax.GT:
            case JavaSyntax.LE:
            case JavaSyntax.GE:
            case JavaSyntax.EQ_EQ:
            case JavaSyntax.NOT_EQ:

            case JavaSyntax.AND_AND: // Binary, result is boolean
            case JavaSyntax.OR_OR:

            case JavaSyntax.LLT: // Always binary
            case JavaSyntax.GGT:
            case JavaSyntax.GGGT:
            case JavaSyntax.MUL:
            case JavaSyntax.DIV:
            case JavaSyntax.AND:
            case JavaSyntax.OR:
            case JavaSyntax.XOR:
            case JavaSyntax.MOD:

            case JavaSyntax.QUESTION:
            case JavaSyntax.COLON:

                // Operator handling
                switch (topID) {
                case CONSTANT:
                case VARIABLE:
                case METHOD:
                case CONSTRUCTOR:
                case ARRAY:
                case DOT:
                case PARENTHESIS:
                case OPERATOR:
                case UNARY_OPERATOR:
                    pushExp(createTokenExp(OPERATOR));
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.PLUS_PLUS: // Prefix or postfix
            case JavaSyntax.MINUS_MINUS:
                switch (topID) {
                case METHOD_OPEN:
                case ARRAY_OPEN:
                case PARENTHESIS_OPEN:
                case OPERATOR:
                case UNARY_OPERATOR:
                case NO_EXP:
                    // Prefix operator
                    JCExpression opExp = createTokenExp(UNARY_OPERATOR);
                    pushExp(opExp); // add operator as new exp
                    break;

                case VARIABLE: // is it only one permitted?
                    // Postfix operator
                    opExp = createTokenExp(UNARY_OPERATOR);
                    popExp(); // pop top
                    opExp.addParameter(top);
                    pushExp(opExp);
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.PLUS: // Can be unary or binary
            case JavaSyntax.MINUS:
                switch (topID) {
                case CONSTANT:
                case VARIABLE:
                case METHOD:
                case CONSTRUCTOR:
                case ARRAY:
                case DOT:
                case PARENTHESIS:
                case UNARY_OPERATOR:
                    JCExpression opExp = createTokenExp(OPERATOR);
                    pushExp(opExp);
                    break;

                case METHOD_OPEN:
                case ARRAY_OPEN:
                case PARENTHESIS_OPEN:
                case OPERATOR:
                case NO_EXP:
                    // Unary operator
                    opExp = createTokenExp(UNARY_OPERATOR);
                    pushExp(opExp); // add operator as new exp
                    break;

                default:
                    err = true;
                    break;
                }
                break;


            case JavaSyntax.NEG: // Always unary
            case JavaSyntax.NOT:
                switch (topID) {
                case METHOD_OPEN:
                case ARRAY_OPEN:
                case PARENTHESIS_OPEN:
                case OPERATOR:
                case UNARY_OPERATOR:
                case NO_EXP:
                    // Unary operator
                    JCExpression opExp = createTokenExp(UNARY_OPERATOR);
                    pushExp(opExp); // add operator as new exp
                    break;

                default:
                    err = true;
                    break;
                }

            case JavaSyntax.DOT: // '.' found
                switch (topID) {
                case CONSTANT:
                case VARIABLE:
                case ARRAY:
                case METHOD:
                case CONSTRUCTOR:
                case PARENTHESIS:
                    popExp();
                    JCExpression opExp = createTokenExp(DOT_OPEN);
                    opExp.addParameter(top);
                    pushExp(opExp);
                    break;

                case DOT:
                    addTokenTo(top);
                    top.setID(DOT_OPEN);
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.COMMA: // ',' found
                switch (topID) {
                case ARRAY:
                case DOT:
                case TYPE:
                case CONSTANT:
                case VARIABLE:
                case CONSTRUCTOR:
                case CONVERSION:
                case PARENTHESIS:
                case OPERATOR:
                case UNARY_OPERATOR:
                case INSTANCEOF:
                    JCExpression top2 = peekExp2();
                    switch (getValidID(top2)) {
                    case METHOD_OPEN:
                        popExp();
                        top2.addParameter(top);
                        top = top2;
                        break;
                    }
                    break;

                case METHOD_OPEN:
                    addTokenTo(top);
                    break;

                default:
                    err = true;
                    break;

                }
                break;

            case JavaSyntax.SEMICOLON:
                err = true;
                break;

            case JavaSyntax.LEFT_PARENTHESES:
                switch (topID) {
                case VARIABLE:
                    top.setID(METHOD_OPEN);
                    addTokenTo(top);
                    break;

                case ARRAY: // a[0](
                    popExp();
                    JCExpression mtdExp = createTokenExp(METHOD);
                    mtdExp.addParameter(top);
                    pushExp(mtdExp);
                    break;

                case PARENTHESIS_OPEN:
                case METHOD_OPEN:
                case NO_EXP:
                    pushExp(createTokenExp(PARENTHESIS_OPEN));
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.RIGHT_PARENTHESES:
                boolean mtd = false;
                switch (topID) {
                case CONSTANT:
                case VARIABLE:
                case ARRAY:
                case DOT:
                case TYPE:
                case CONSTRUCTOR:
                case CONVERSION:
                case PARENTHESIS:
                case OPERATOR:
                case UNARY_OPERATOR:
                case INSTANCEOF:
                    JCExpression top2 = peekExp2();
                    switch (getValidID(top2)) {
                    case PARENTHESIS_OPEN:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(JCExpression.isValidType(top) ? CONVERSION : PARENTHESIS);
                        addTokenTo(top2);
                        break;

                    case METHOD_OPEN:
                        popExp();
                        top2.addParameter(top);
                        top = top2;
                        mtd = true;
                        break;

                    default:
                        err = true;
                        break;
                    }
                    break;

                case METHOD_OPEN:
                    mtd = true;
                    break;

                    //              case PARENTHESIS_OPEN: // empty parenthesis
                default:
                    err = true;
                    break;
                }

                if (mtd) {
                    addTokenTo(top);
                    top.setID(METHOD);
                    JCExpression top2 = peekExp2();
                    switch (getValidID(top2)) {
                    case DOT_OPEN:
                        JCExpression top3 = peekExp(3);
                        if (getValidID(top3) == NEW) {
                            popExp(); // pop top
                            top2.addParameter(top); // add METHOD to DOT
                            top2.setID(DOT);
                            popExp(); // pop top2
                            top3.setID(CONSTRUCTOR);
                            top3.addParameter(top2); // add DOT to CONSTRUCTOR
                        }
                        break;

                    case NEW:
                        top2.setID(CONSTRUCTOR);
                        top2.addParameter(top);
                        popExp(); // pop top
                        break;
                    }
                }
                break;

            case JavaSyntax.LEFT_SQUARE_BRACKET:
                switch (topID) {
                case VARIABLE:
                case METHOD:
                case DOT:
                case ARRAY:
                case TYPE: // ... int[ ...
                    popExp(); // top popped
                    JCExpression arrExp = createTokenExp(ARRAY_OPEN);
                    addTokenTo(arrExp);
                    arrExp.addParameter(top);
                    pushExp(arrExp);
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.RIGHT_SQUARE_BRACKET:
                switch (topID) {
                case VARIABLE:
                case METHOD:
                case DOT:
                case ARRAY:
                case PARENTHESIS:
                case CONSTANT:
                case OPERATOR:
                case UNARY_OPERATOR:
                case INSTANCEOF:
                    JCExpression top2 = peekExp2();
                    switch (getValidID(top2)) {
                    case ARRAY_OPEN:
                        popExp(); // top popped
                        top2.addParameter(top);
                        top2.setID(ARRAY);
                        addTokenTo(top2);
                        break;

                    default:
                        err = true;
                        break;
                    }
                    break;

                case ARRAY_OPEN:
                    top.setID(ARRAY);
                    addTokenTo(top);
                    break;

                default:
                    err = true;
                    break;
                }
                break;

            case JavaSyntax.LEFT_BRACE:
                err = true;
                break;

            case JavaSyntax.RIGHT_BRACE:
                err = true;
                break;



            }
            break;

        case JavaSyntax.LINE_COMMENT:
            // Skip line comment
            break;

        case JavaSyntax.BLOCK_COMMENT:
            // Skip block comment
            break;

        case JavaSyntax.CHAR:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.CHAR_TYPE);
            break;

        case JavaSyntax.STRING:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.STRING_TYPE);
            break;

        case JavaSyntax.INT:
        case JavaSyntax.HEX:
        case JavaSyntax.OCTAL:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.INT_TYPE);
            break;

        case JavaSyntax.LONG:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.LONG_TYPE);
            break;

        case JavaSyntax.FLOAT:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.FLOAT_TYPE);
            break;

        case JavaSyntax.DOUBLE:
            constExp = createTokenExp(CONSTANT);
            constExp.setType(JCompletion.DOUBLE_TYPE);
            break;

        } // end of testing keyword type

        // Check whether a constant or data type keyword was found
        if (constExp != null) {
            switch (topID) {
            case DOT_OPEN:
                err = true;
                break;

            case ARRAY_OPEN:
            case PARENTHESIS_OPEN:
            case PARENTHESIS: // can be conversion
            case METHOD_OPEN:
            case OPERATOR:
            case CONVERSION:
            case NO_EXP:
                pushExp(constExp);
                break;

            default:
                err = true;
                break;
            }
        }

        if (kwdType != null) { // keyword constant (in conversions)
            switch (topID) {
            case PARENTHESIS_OPEN: // conversion
                JCExpression kwdExp = createTokenExp(TYPE);
                addTokenTo(kwdExp);
                kwdExp.setType(kwdType);
                pushExp(kwdExp);
                break;

            default: // otherwise not recognized
                err = true;
                break;
            }
        }

        if (err) {
            clearStack();

            switch (tokenID) { // Possibly push current exp. for better heuristics
            case JavaSyntax.IDENTIFIER:
            case JavaSyntax.FUNCTION:
                pushExp(createTokenExp(VARIABLE));
                break;
            }
        }

        lastPosition = bufferStartPos + offset + tokenLen; // !!! move to eot()
        return !stopped;
    }

    public int eot(int offset) {
        // Check for joins
        boolean reScan = true;
        while (reScan) {
            reScan = false;
            JCExpression top = peekExp();
            JCExpression top2 = peekExp2();
            int top2ID = getValidID(top2);
            if (top != null) {
                switch (getValidID(top)) {
                case VARIABLE:
                    switch (top2ID) {
                    case DOT_OPEN:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(DOT);
                        reScan = true;
                        break;
                    case NEW:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(CONSTRUCTOR);
                        reScan = true;
                        break;
                    }
                    break;

                case METHOD_OPEN:
                    // let it flow to METHOD
                case METHOD:
                    switch (top2ID) {
                    case DOT_OPEN:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(DOT);
                        reScan = true;
                        break;
                    case NEW:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(CONSTRUCTOR);
                        reScan = true;
                        break;
                    }
                    break;

                case DOT:
                case DOT_OPEN:
                    switch (top2ID) {
                    case NEW:
                        popExp();
                        top2.addParameter(top);
                        top2.setID(CONSTRUCTOR);
                        reScan = true;
                        break;
                    }
                }
            } else { // nothing on the stack, create empty variable
                pushExp(JCExpression.createEmptyVariable(
                            bufferStartPos + bufferOffsetDelta + offset));
            }
        }
        //    System.out.println(this);
        return 0;
    }

    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer) {
        this.buffer = new char[len + preScan];
        System.arraycopy(buffer, offset - preScan, this.buffer, 0, len + preScan);
        bufferOffsetDelta = preScan - offset;
        this.bufferStartPos = startPos - preScan;
    }

    public String toString() {
        int cnt = expStack.size();
        StringBuffer sb = new StringBuffer();
        if (stopped) {
            sb.append("Parsing STOPPED by request.\n"); // NOI18N
        }
        sb.append("Stack size is " + cnt + "\n"); // NOI18N
        if (cnt > 0) {
            sb.append("Stack expressions:\n"); // NOI18N
            for (int i = 0; i < cnt; i++) {
                JCExpression e = (JCExpression)expStack.get(i);
                sb.append("Stack["); // NOI18N
                sb.append(i);
                sb.append("]: "); // NOI18N
                sb.append(e.toString(0));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/13/00  Miloslav Metelka Localization
 *  11   Gandalf   1.10        1/4/00   Miloslav Metelka 
 *  10   Gandalf   1.9         12/28/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/24/99 Miloslav Metelka 
 *  8    Gandalf   1.7         11/14/99 Miloslav Metelka 
 *  7    Gandalf   1.6         11/9/99  Miloslav Metelka 
 *  6    Gandalf   1.5         11/8/99  Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/10/99 Miloslav Metelka 
 *  3    Gandalf   1.2         10/4/99  Miloslav Metelka 
 *  2    Gandalf   1.1         9/30/99  Miloslav Metelka 
 *  1    Gandalf   1.0         9/15/99  Miloslav Metelka 
 * $
 */

