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

package org.netbeans.modules.web.core.syntax;

import org.netbeans.editor.Syntax;

/**
* Syntax class for JSP tags. It is not meant to be used by itself, but as one of syntaxes with
* MultiSyntax. Recognizes JSP tags, comments and directives. Does not recognize scriptlets, 
* expressions and declarations, which should be rocognized by the master syntax, as expressions
* can appear embedded in a JSP tag. Moreover, they all share Java syntax.
*
* @author Petr Jiricka
* @version 1.00
*/

public class JspTagSyntax extends Syntax {

    // Token names
    public static final String TN_JSP_TAG = "jsp-tag-directive";
    public static final String TN_JSP_SYMBOL = "jsp-symbol";
    public static final String TN_JSP_COMMENT = "jsp-comment";
    public static final String TN_JSP_ATTRIBUTE = "jsp-attribute-name";
    public static final String TN_JSP_ATTR_VALUE = "jsp-attribute-value";
    public static final String TN_JSP_SYMBOL2 = "jsp-scriptlet-delimiter";

    // Token IDs
    public static final int TEXT           = 0; // plain text
    public static final int ERROR          = 1; // errorneous text
    public static final int JSP_TAG        = 2; // html tag
    public static final int JSP_SYMBOL     = 3; // operators like '+', '*=' etc.
    public static final int JSP_COMMENT    = 4; // block comment
    public static final int JSP_ATTRIBUTE  = 5; // argument
    public static final int JSP_ATTR_VALUE = 6; // string constant e.g. "string"
    public static final int JSP_SYMBOL2    = 7; // java code delimiters (<%, <%=, <%!, %>

    // Internal states
    // general
    private static final int ISI_ERROR           =  1; // when the fragment does not start with <
    private static final int ISA_LT              =  2; // after '<' char
    // tags and directives
    private static final int ISI_TAG             =  3; // inside JSP tag
    private static final int ISI_DIR             =  4; // inside JSP directive
    private static final int ISP_TAG             =  5; // after JSP tag
    private static final int ISP_DIR             =  6; // after JSP directive
    private static final int ISI_TAG_I_WS        =  7; // inside JSP tag after whitespace
    private static final int ISI_DIR_I_WS        =  8; // inside JSP directive after whitespace
    private static final int ISI_ENDTAG          =  9; // inside end JSP tag
    private static final int ISI_TAG_ATTR        = 10; // inside tag attribute
    private static final int ISI_DIR_ATTR        = 11; // inside directive attribute
    private static final int ISP_TAG_EQ          = 12; // just after '=' in tag
    private static final int ISP_DIR_EQ          = 13; // just after '=' in directive
    private static final int ISI_TAG_STRING      = 14; // inside string (value - "") in tag
    private static final int ISI_DIR_STRING      = 15; // inside string (value - "") in directive
    private static final int ISI_TAG_STRING_B    = 16; // inside string (value - "") after backslash in tag
    private static final int ISI_DIR_STRING_B    = 17; // inside string (value - "") after backslash in directive
    private static final int ISI_TAG_STRING2     = 18; // inside string (value - '') in tag
    private static final int ISI_DIR_STRING2     = 19; // inside string (value - '') in directive
    private static final int ISI_TAG_STRING2_B   = 20; // inside string (value - '') after backslash in tag
    private static final int ISI_DIR_STRING2_B   = 21; // inside string (value - '') after backslash in directive
    private static final int ISA_ENDSLASH        = 22; // after ending '/' in JSP tag
    private static final int ISA_ENDPC           = 23; // after ending '%' in JSP directive
    // comments (+directives)
    private static final int ISA_LT_PC           = 24; // after '<%' (comment or directive)
    private static final int ISI_JSP_COMMENT     = 25; // after <%-
    private static final int ISI_JSP_COMMENT_M   = 26; // inside JSP comment after -
    private static final int ISI_JSP_COMMENT_MM  = 27; // inside JSP comment after --
    private static final int ISI_JSP_COMMENT_MMP = 28; // inside JSP comment after --%
    // end state
    static final int ISA_END_JSP                 = 29; // JSP fragment has finished and control
    // should be returned to master syntax
    // more errors
    private static final int ISI_TAG_ERROR       = 30; // error in tag, can be cleared by > or \n
    private static final int ISI_DIR_ERROR       = 31; // error in directive, can be cleared by %> or \n
    private static final int ISI_DIR_ERROR_P     = 32; // error in directive after %, can be cleared by > or \n
    // additional states which had to be added in the process of improving this class
    private static final int ISA_LT_PC_AT        = 33; // after '<%@' (directive)



    public JspTagSyntax() {
        highestTokenID = JSP_SYMBOL2;
    }

    public boolean isIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
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
                case '<':
                    state = ISA_LT;
                    break;
                default:
                    state = ISI_ERROR;
                    break;
                }
                break;

            case ISA_LT:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) { // possible tag begining
                    state = ISI_TAG;
                    return JSP_SYMBOL;
                }

                switch (actChar) {
                case '/':
                    offset++;
                    state = ISI_ENDTAG;
                    return JSP_SYMBOL;
                case '\n':
                    state = ISI_TAG_ERROR;
                    return JSP_SYMBOL;
                case '%':
                    state = ISA_LT_PC;
                    break;
                default:
                    state = ISI_TAG_ERROR;
                    break;
                }
                break;

            case ISI_TAG:
            case ISI_DIR:
                if (!((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar >= '0' && actChar <= '9') ||
                        (actChar == '_') ||
                        (actChar == ':'))
                   ) { // not alpha
                    state = ((state == ISI_TAG) ? ISP_TAG : ISP_DIR);
                    return JSP_TAG;
                }
                break;

            case ISP_TAG:
            case ISP_DIR:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) {
                    state = ((state == ISP_TAG) ? ISI_TAG_ATTR : ISI_DIR_ATTR);
                    break;
                }
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return string first
                        return JSP_TAG;
                    }
                case '>': // for tags
                    if (state == ISP_TAG) {
                        if (offset == tokenOffset) {  // no char
                            offset++;
                            state = ISA_END_JSP;
                            return JSP_SYMBOL;
                        }
                        else { // return string first
                            return JSP_TAG;
                        }
                    }
                    else { // directive
                        //state = ISI_DIR_ERROR;
                        //commented out to minimize errors during the process of writing directives
                        break;
                    }
                case '/': // for tags
                    if (state == ISP_TAG) {
                        if (offset == tokenOffset) {  // no char
                            state = ISA_ENDSLASH;
                            break;
                        }
                        else { // return string first
                            return JSP_TAG;
                        }
                    }
                    else { // directive
                        //state = ISI_DIR_ERROR;
                        //commented out to minimize errors during the process of writing directives
                        break;
                    }
                case '%': // for directives
                    if (state == ISP_DIR) {
                        if (offset == tokenOffset) {  // no char
                            state = ISA_ENDPC;
                            break;
                        }
                        else { // return string first
                            return JSP_TAG;
                        }
                    }
                    else { // tag
                        state = ISI_TAG_ERROR;
                        break;
                    }
                case '=':
                    offset++;
                    state = ((state == ISP_TAG) ? ISP_TAG_EQ : ISP_DIR_EQ);
                    return JSP_SYMBOL;
                case ' ':
                case '\t':
                    state = ((state == ISP_TAG) ? ISI_TAG_I_WS : ISI_DIR_I_WS);
                    break;
                    /*case '<': // assume that this is the start of the next tag
                      state = ISA_END_JSP;
                      return JSP_TAG;*/
                }
                break;

            case ISI_TAG_I_WS:
            case ISI_DIR_I_WS:
                switch (actChar) {
                case ' ':
                case '\t':
                    break;
                default:
                    state = ((state == ISI_TAG_I_WS) ? ISP_TAG : ISP_DIR);
                    return JSP_TAG; // currently color as text
                }
                break;

            case ISI_ENDTAG:
                if (!((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar >= '0' && actChar <= '9') ||
                        (actChar == '_') ||
                        (actChar == ':'))
                   ) { // not alpha
                    state = ISP_TAG;
                    return JSP_TAG;
                }
                break;

            case ISI_TAG_ATTR:
            case ISI_DIR_ATTR:
                if (!((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar >= '0' && actChar <= '9') ||
                        (actChar == '_') ||
                        (actChar == '-'))
                   ) { // not alpha or '-' (http-equiv)
                    state = ((state == ISI_TAG_ATTR) ? ISP_TAG : ISP_DIR);
                    return JSP_ATTRIBUTE;
                }
                break;

            case ISP_TAG_EQ:
            case ISP_DIR_EQ:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return string first
                        return JSP_ATTR_VALUE;
                    }
                case '"':
                    state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING : ISI_DIR_STRING);
                    break;
                case '\'':
                    state = ((state == ISP_TAG_EQ) ? ISI_TAG_STRING2 : ISI_DIR_STRING2);
                    break;
                case ' ':
                case '\t':
                    // don't change the state
                    break;
                default:
                    state = ((state == ISP_TAG_EQ) ? ISP_TAG : ISP_DIR);
                    return JSP_ATTR_VALUE;
                }
                break;

            case ISI_TAG_STRING:
            case ISI_DIR_STRING:
            case ISI_TAG_STRING2:
            case ISI_DIR_STRING2:
                if ((actChar == '"') && ((state == ISI_TAG_STRING) || (state == ISI_DIR_STRING))) {
                    offset++;
                    state = ((state == ISI_TAG_STRING) ? ISP_TAG : ISP_DIR);
                    return JSP_ATTR_VALUE;
                }

                if ((actChar == '\'') && ((state == ISI_TAG_STRING2) || (state == ISI_DIR_STRING2))) {
                    offset++;
                    state = ((state == ISI_TAG_STRING2) ? ISP_TAG : ISP_DIR);
                    return JSP_ATTR_VALUE;
                }

                switch (actChar) {
                case '\\':
                    switch (state) {
                    case ISI_TAG_STRING:
                        state = ISI_TAG_STRING_B;
                        break;
                    case ISI_DIR_STRING:
                        state = ISI_DIR_STRING_B;
                        break;
                    case ISI_TAG_STRING2:
                        state = ISI_TAG_STRING2_B;
                        break;
                    case ISI_DIR_STRING2:
                        state = ISI_DIR_STRING2_B;
                        break;
                    }
                    break;
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return string first
                        return JSP_ATTR_VALUE;
                    }
                }
                break;

            case ISI_TAG_STRING_B:
            case ISI_DIR_STRING_B:
            case ISI_TAG_STRING2_B:
            case ISI_DIR_STRING2_B:
                switch (actChar) {
                case '"':
                case '\'':
                case '\\':
                    break;
                default:
                    offset--;
                    break;
                }
                switch (state) {
                case ISI_TAG_STRING_B:
                    state = ISI_TAG_STRING;
                    break;
                case ISI_DIR_STRING_B:
                    state = ISI_DIR_STRING;
                    break;
                case ISI_TAG_STRING2_B:
                    state = ISI_TAG_STRING2;
                    break;
                case ISI_DIR_STRING2_B:
                    state = ISI_DIR_STRING2;
                    break;
                }
                break;

            case ISA_ENDSLASH:
                switch (actChar) {
                case '>':
                    offset++;
                    state = ISA_END_JSP;
                    return JSP_SYMBOL;
                case '\n':
                    state = ISI_TAG_ERROR;
                    return JSP_SYMBOL;
                default:
                    state = ISP_TAG;
                    return JSP_SYMBOL;
                }
                //break; not reached

            case ISA_ENDPC:
                switch (actChar) {
                case '>':
                    offset++;
                    state = ISA_END_JSP;
                    return JSP_SYMBOL;
                case '\n':
                    state = ISI_DIR_ERROR;
                    return JSP_SYMBOL;
                default:
                    state = ISP_DIR;
                    return JSP_SYMBOL;
                }
                //break; not reached

            case ISA_LT_PC:
                switch (actChar) {
                case '@':
                    offset++;
                    state = ISA_LT_PC_AT;
                    return JSP_SYMBOL;
                case '-':
                    state = ISI_JSP_COMMENT;
                    break;
                default: // just cut it, because this will be recognized
                    // by master syntax as a Java scriptlet/expression/declaration
                    state = ISA_END_JSP;
                    return JSP_SYMBOL;
                }
                break;


                // JSP states
            case ISI_JSP_COMMENT:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return block comment first
                        return JSP_COMMENT;
                    }
                case '-':
                    state = ISI_JSP_COMMENT_M;
                    break;
                }
                break;

            case ISI_JSP_COMMENT_M:
                switch (actChar) {
                case '\n':
                    state = ISI_JSP_COMMENT;
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return block comment first
                        return JSP_COMMENT;
                    }
                case '-':
                    state = ISI_JSP_COMMENT_MM;
                    break;
                default:
                    state = ISI_JSP_COMMENT;
                    break;
                }
                break;

            case ISI_JSP_COMMENT_MM:
                switch (actChar) {
                case '\n':
                    state = ISI_JSP_COMMENT;
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return block comment first
                        return JSP_COMMENT;
                    }
                case '%':
                    state = ISI_JSP_COMMENT_MMP;
                    break;
                case '-':
                    state = ISI_JSP_COMMENT_MM;
                    break;
                default:
                    state = ISI_JSP_COMMENT;
                    break;
                }
                break;

            case ISI_JSP_COMMENT_MMP:
                switch (actChar) {
                case '\n':
                    state = ISI_JSP_COMMENT;
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    } else { // return block comment first
                        return JSP_COMMENT;
                    }
                case '>':
                    state = ISA_END_JSP;
                    offset++;
                    return JSP_COMMENT;
                default:
                    state = ISI_JSP_COMMENT;
                    break;
                }
                break;

            case ISI_ERROR:
                switch (actChar) {
                case '\n':
                    state = INIT;
                    return ERROR;
                case '<':
                    state = ISA_LT;
                    return ERROR;
                }
                break;

            case ISI_TAG_ERROR:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        state = ISI_TAG_I_WS;
                        return EOL;
                    } else { // return error first
                        return ERROR;
                    }
                case '>':
                    state = ISI_TAG_I_WS;
                    return ERROR;
                }
                break;

            case ISI_DIR_ERROR:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        state = ISI_DIR_I_WS;
                        return EOL;
                    } else { // return error first
                        return ERROR;
                    }
                case '%':
                    state = ISI_DIR_ERROR_P;
                    break;
                }
                break;

            case ISI_DIR_ERROR_P:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        state = ISI_DIR_I_WS;
                        return EOL;
                    } else { // return error first
                        return ERROR;
                    }
                case '>':
                    offset--;
                    state = ISI_DIR_I_WS;
                    return ERROR;
                }
                break;

            case ISA_END_JSP:
                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    }
                    else {
                        return TEXT;
                    }
                }
                break;

                // added states
            case ISA_LT_PC_AT:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) { // the directive starts
                    state = ISI_DIR;
                    return JSP_TAG;
                }

                switch (actChar) {
                case '\n':
                    if (offset == tokenOffset) { // no char
                        offset++;
                        return EOL;
                    }
                    else {
                        return JSP_TAG;
                    }
                }
                break;

            }

            offset = ++offset;
        } // end of while(offset...)

        // At this stage there's no more text in the scanned buffer.
        // Scanner first checks whether this is completely the last
        // available buffer.

        if (lastBuffer) {
            switch(state) {
            case ISI_ERROR:
            case ISI_TAG_ERROR:
            case ISI_DIR_ERROR:
            case ISI_DIR_ERROR_P:
                return ERROR;
            case ISA_LT:
            case ISA_LT_PC:
            case ISA_ENDSLASH:
            case ISA_ENDPC:
            case ISP_TAG_EQ:
            case ISP_DIR_EQ:
                return JSP_SYMBOL;
            case ISI_TAG:
            case ISI_DIR:
            case ISI_ENDTAG:
                return JSP_TAG;
            case ISP_TAG:
            case ISP_DIR:
            case ISI_TAG_I_WS:
            case ISI_DIR_I_WS:
            case ISA_LT_PC_AT:
                return JSP_TAG;
            case ISI_TAG_ATTR:
            case ISI_DIR_ATTR:
                return JSP_ATTRIBUTE;
            case ISI_TAG_STRING:
            case ISI_DIR_STRING:
            case ISI_TAG_STRING_B:
            case ISI_DIR_STRING_B:
            case ISI_TAG_STRING2:
            case ISI_DIR_STRING2:
            case ISI_TAG_STRING2_B:
            case ISI_DIR_STRING2_B:
                return JSP_ATTR_VALUE;
            case ISI_JSP_COMMENT:
            case ISI_JSP_COMMENT_M:
            case ISI_JSP_COMMENT_MM:
            case ISI_JSP_COMMENT_MMP:
                return JSP_COMMENT;
            case ISA_END_JSP:
                return TEXT;
            default:
                if (Boolean.getBoolean("netbeans.debug.exceptions"))
                    new Exception("Unhandled state : " + getStateName(state)).printStackTrace();
            }
        }

        // At this stage there's no more text in the scanned buffer, but
        // this buffer is not the last so the scan will continue on another buffer.
        // The scanner tries to minimize the amount of characters
        // that will be prescanned in the next buffer.

        // pending
        switch(state) {
        case ISI_ERROR:
        case ISI_TAG_ERROR:
        case ISI_DIR_ERROR:
        case ISI_DIR_ERROR_P:
            return ERROR;
        case ISA_LT:
        case ISA_LT_PC:
        case ISA_ENDSLASH:
        case ISA_ENDPC:
        case ISP_TAG_EQ:
        case ISP_DIR_EQ:
            return JSP_SYMBOL;
        case ISI_TAG:
        case ISI_DIR:
        case ISI_ENDTAG:
            return JSP_TAG;
        case ISP_TAG:
        case ISP_DIR:
        case ISI_TAG_I_WS:
        case ISI_DIR_I_WS:
        case ISA_LT_PC_AT:
            return JSP_TAG;
        case ISI_TAG_ATTR:
        case ISI_DIR_ATTR:
            return JSP_ATTRIBUTE;
        case ISI_TAG_STRING:
        case ISI_DIR_STRING:
        case ISI_TAG_STRING_B:
        case ISI_DIR_STRING_B:
        case ISI_TAG_STRING2:
        case ISI_DIR_STRING2:
        case ISI_TAG_STRING2_B:
        case ISI_DIR_STRING2_B:
            return JSP_ATTR_VALUE;
        case ISI_JSP_COMMENT:
        case ISI_JSP_COMMENT_M:
        case ISI_JSP_COMMENT_MM:
        case ISI_JSP_COMMENT_MMP:
            return JSP_COMMENT;
        case ISA_END_JSP:
            return TEXT;
        }

        return EOT;

    }


    public String getTokenName(int tokenID) {
        switch (tokenID) {
        case TEXT:
            return TN_TEXT;
        case ERROR:
            return TN_ERROR;
        case JSP_TAG:
            return TN_JSP_TAG;
        case JSP_SYMBOL:
            return TN_JSP_SYMBOL;
        case JSP_COMMENT:
            return TN_JSP_COMMENT;
        case JSP_ATTRIBUTE:
            return TN_JSP_ATTRIBUTE;
        case JSP_ATTR_VALUE:
            return TN_JSP_ATTR_VALUE;
        case JSP_SYMBOL2:
            return TN_JSP_SYMBOL2;
        default:
            return super.getTokenName(tokenID);
        }
    }

    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case ISI_ERROR           : return "jsptag_ISI_ERROR";
        case ISA_LT              : return "jsptag_ISA_LT";
        case ISI_TAG             : return "jsptag_ISI_TAG";
        case ISI_DIR             : return "jsptag_ISI_DIR";
        case ISP_TAG             : return "jsptag_ISP_TAG";
        case ISP_DIR             : return "jsptag_ISP_DIR";
        case ISI_TAG_I_WS        : return "jsptag_ISI_TAG_I_WS";
        case ISI_DIR_I_WS        : return "jsptag_ISI_DIR_I_WS";
        case ISI_ENDTAG          : return "jsptag_ISI_ENDTAG";
        case ISI_TAG_ATTR        : return "jsptag_ISI_TAG_ATTR";
        case ISI_DIR_ATTR        : return "jsptag_ISI_DIR_ATTR";
        case ISP_TAG_EQ          : return "jsptag_ISP_TAG_EQ";
        case ISP_DIR_EQ          : return "jsptag_ISP_DIR_EQ";
        case ISI_TAG_STRING      : return "jsptag_ISI_TAG_STRING";
        case ISI_DIR_STRING      : return "jsptag_ISI_DIR_STRING";
        case ISI_TAG_STRING_B    : return "jsptag_ISI_TAG_STRING_B";
        case ISI_DIR_STRING_B    : return "jsptag_ISI_DIR_STRING_B";
        case ISI_TAG_STRING2     : return "jsptag_ISI_TAG_STRING2";
        case ISI_DIR_STRING2     : return "jsptag_ISI_DIR_STRING2";
        case ISI_TAG_STRING2_B   : return "jsptag_ISI_TAG_STRING2_B";
        case ISI_DIR_STRING2_B   : return "jsptag_ISI_DIR_STRING2_B";
        case ISA_ENDSLASH        : return "jsptag_ISA_ENDSLASH";
        case ISA_ENDPC           : return "jsptag_ISA_ENDPC";
        case ISA_LT_PC           : return "jsptag_ISA_LT_PC";
        case ISI_JSP_COMMENT     : return "jsptag_ISI_JSP_COMMENT";
        case ISI_JSP_COMMENT_M   : return "jsptag_ISI_JSP_COMMENT_M";
        case ISI_JSP_COMMENT_MM  : return "jsptag_ISI_JSP_COMMENT_MM";
        case ISI_JSP_COMMENT_MMP : return "jsptag_ISI_JSP_COMMENT_MMP";
        case ISA_END_JSP         : return "jsptag_ISA_END_JSP";
        case ISI_TAG_ERROR       : return "jsptag_ISI_TAG_ERROR";
        case ISI_DIR_ERROR       : return "jsptag_ISI_DIR_ERROR";
        case ISI_DIR_ERROR_P     : return "jsptag_ISI_DIR_ERROR_P";
        case ISA_LT_PC_AT        : return "jsptag_ISA_LT_PC_AT";
        default:
            return super.getStateName(stateNumber);
        }
    }

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.2.0     4/5/00   Petr Jiricka    Token names and examples
 *       from bundles.
 *  3    Gandalf   1.2         2/14/00  Petr Jiricka    Eased conditions for 
 *       syntax of directives to prevent bogus red error text.
 *  2    Gandalf   1.1         2/11/00  Petr Jiricka    Numerous small fixes.
 *  1    Gandalf   1.0         2/10/00  Petr Jiricka    
 * $
 */

