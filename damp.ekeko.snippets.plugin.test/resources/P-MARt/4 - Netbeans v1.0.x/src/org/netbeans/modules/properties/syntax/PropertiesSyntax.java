/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.Syntax;

/**
* Syntax analyzes for properties files.
* Tokens and internal states are given below. 
*
* @author Petr Jiricka, Miloslav Metelka
* @version 1.00
*/

public class PropertiesSyntax extends Syntax {

    // Token names
    public static final String TN_KEY = "key";
    public static final String TN_EQ = "equal-sign";
    public static final String TN_VALUE = "value";


    // Token IDs
    public static final int TEXT = 0;
    public static final int LINE_COMMENT = 2; // line comment
    public static final int KEY = 3; // key
    public static final int EQ = 4; // equal-sign
    public static final int VALUE = 5; // value

    // Internal states
    //  private static final int ISI_ERROR = 1; // after carriage return
    private static final int ISI_LINE_COMMENT = 2; // inside line comment
    private static final int ISI_KEY = 3; // inside a key
    private static final int ISI_KEY_A_BSLASH = 4; // inside a key after backslash
    private static final int ISI_EQUAL = 5; // inside an equal sign
    private static final int ISI_EQUAL2 = 6; // inside/after an equal sign after the first =/:
    private static final int ISI_VALUE = 7; // inside a value
    private static final int ISI_VALUE_A_BSLASH = 8; // inside a value after backslash
    private static final int ISI_VALUE_AT_NL = 9; // inside a value at new line


    public PropertiesSyntax() {
        highestTokenID = VALUE;
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
                case '\t':
                case '\f':
                case ' ':
                    offset++;
                    return TEXT;
                case '#':
                case '!':
                    state = ISI_LINE_COMMENT;
                    break;
                default:
                    state = ISI_KEY;
                    break;
                }
                break; // end state INIT

            case ISI_LINE_COMMENT:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return LINE_COMMENT;
                }
                break; // end state ISI_LINE_COMMENT

            case ISI_KEY:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return KEY;
                case '\\':
                    state = ISI_KEY_A_BSLASH;
                    break;
                case '=':
                case ':':
                    state = ISI_EQUAL;
                    return KEY;
                }
                break; // end state ISI_KEY

            case ISI_KEY_A_BSLASH:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return KEY;
                default:
                    state = ISI_KEY;
                }
                break; // end state ISI_KEY_A_BSLASH

            case ISI_EQUAL:
                switch (actChar) {
                case '=':
                case ':':
                    state = ISI_EQUAL2;
                    break;
                default:
                    throw new Error("Something smells");
                }
                break; // end state ISI_KEY

            case ISI_EQUAL2:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return EQ;
                case '=':
                case ':':
                    offset++;
                    state = ISI_VALUE;
                    return EQ;
                default:
                    state = ISI_VALUE;
                    return EQ;
                }
                //break; // end state ISI_KEY

            case ISI_VALUE:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return VALUE;
                case '\\':
                    state = ISI_VALUE_A_BSLASH;
                    break;
                }
                break; // end state ISI_KEY

            case ISI_VALUE_A_BSLASH:
                switch (actChar) {
                case '\n':
                    state = ISI_VALUE_AT_NL;
                    return VALUE;
                default:
                    state = ISI_VALUE;
                }
                break; // end state ISI_KEY

            case ISI_VALUE_AT_NL:
                switch (actChar) {
                case '\n':
                    offset++;
                    state = ISI_VALUE;
                    return EOL;
                default:
                    throw new Error("Something smells 2");
                }
                //break; // end state ISI_KEY

            default:
                throw new Error("Unhandled state " + state);

            } // end of the outer switch statement

            offset = ++offset;

        } // end of while loop

        /* At this stage there's no more text in the scanned buffer. */

        if (lastBuffer || !lastBuffer) {
            switch(state) {
            case ISI_LINE_COMMENT:
                return LINE_COMMENT;
            case ISI_KEY:
            case ISI_KEY_A_BSLASH:
                return KEY;
            case ISI_EQUAL:
            case ISI_EQUAL2:
                return EQ;
            case ISI_VALUE:
            case ISI_VALUE_A_BSLASH:
                return VALUE;
            case ISI_VALUE_AT_NL:
                throw new Error("Something smells 3");
            }
        }

        return EOT;

    } // parseToken


    public String getTokenName(int tokenID) {
        switch (tokenID) {
        case LINE_COMMENT:
            return TN_LINE_COMMENT;
        case KEY:
            return TN_KEY;
        case EQ:
            return TN_EQ;
        case VALUE:
            return TN_VALUE;
        default:
            return super.getTokenName(tokenID);
        }
    }


    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case ISI_LINE_COMMENT:
            return "ISI_LINE_COMMENT";
        case ISI_KEY:
            return "ISI_KEY";
        case ISI_KEY_A_BSLASH:
            return "ISI_KEY_A_BSLASH";
        case ISI_EQUAL:
            return "ISI_EQUAL";
        case ISI_EQUAL2:
            return "ISI_EQUAL2";
        case ISI_VALUE:
            return "ISI_VALUE";
        case ISI_VALUE_A_BSLASH:
            return "ISI_VALUE_A_BSLASH";
        case ISI_VALUE_AT_NL:
            return "ISI_VALUE_AT_NL";
        default:
            return super.getStateName(stateNumber);
        }
    }


}

/*
 * <<Log>>
 *  4    Gandalf   1.3         1/12/00  Petr Jiricka    Syntax coloring API 
 *       fixes
 *  3    Gandalf   1.2         12/28/99 Miloslav Metelka Structural change and 
 *       some renamings
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */

