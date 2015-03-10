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

import java.util.Arrays;
import java.util.ArrayList;
import org.netbeans.editor.ext.JavaSyntax;
import org.netbeans.editor.ext.HTMLSyntax;
import org.netbeans.editor.Syntax;

/**
* Syntax for JSP files. This is a MultiSyntax consisting of three slave syntaxes: host syntax 
* (for now this is HTMLSyntax), JspTagSyntax and JavaSyntax.
*
* @author Petr Jiricka
* @version 1.00
*/

public class JspMultiSyntax extends BaseMultiSyntax {

    // prefixes for changed names of tokens
    public static final String HTML_PREFIX = "html-";
    public static final String JAVA_PREFIX = "java-";

    // modes of nesting of languages
    public static final int MODE_HOST             = 1;
    public static final int MODE_HOST_JSPTAG      = 2;
    public static final int MODE_HOST_JSPTAG_JAVA = 3;
    public static final int MODE_HOST_JAVA        = 4;

    // constants for result of operation of checking delimiters
    protected static final int DELIMCHECK_NO = -1;
    protected static final int DELIMCHECK_PART = -2;

    // states of this multisyntax
    private static final int ISI_LANGUAGE = 1; // one syntax is active and working
    // states for switching from the host language to JSP tag or Java
    private static final int ISI_HOST_JSPTAG = 2; // just before <jsptag or similar (recognized by JspTagSyntax)
    private static final int ISI_HOST_JAVA = 3; // just before <% or similar, after such a delimiter Java block starts, host language
    private static final int ISI_HOST_JAVA_LT = 4; // as ISI_HOST_JAVA after <
    private static final int ISI_HOST_JAVA_LT_PC = 5; // as ISI_HOST_JAVA after <%
    private static final int ISI_HOST_JAVA_JUMP = 6; // after a Java delimiter in host language, now really switch
    // states for switching from a JSP tag to Java
    private static final int ISI_JSPTAG_JAVA = 7; // just before <% or similar, after such a delimiter Java block starts, JSPTAG language
    private static final int ISI_JSPTAG_JAVA_LT = 8; // as ISI_JSPTAG_JAVA after <
    private static final int ISI_JSPTAG_JAVA_LT_PC = 9; // as ISI_JSPTAG_JAVA after <%
    private static final int ISI_JSPTAG_JAVA_JUMP = 10; // after a Java delimiter in JSPTAG language, now really switch
    // states for switching from Java to a JSP tag
    private static final int ISI_JAVA1_SWITCH = 11; // just before %> in Java (go to JSPTAG)
    private static final int ISI_JAVA1_PC = 12; // as ISI_JAVA1_SWITCH after %
    private static final int ISI_JAVA1_JUMP = 13; // after %> in Java, now really switch to JSPTAG
    // states for switching from Java to host
    private static final int ISI_JAVA2_SWITCH = 14; // just before %> in Java (go to host)
    private static final int ISI_JAVA2_PC = 15; // as ISI_JAVA2_SWITCH after %
    private static final int ISI_JAVA2_JUMP = 16; // after %> in Java, now really switch to host

    // states of the automaton which looks for delimiters in the host language
    private static final int HOST_INIT = 1; // initial state - host language
    private static final int HOST_LT = 2; // after < - host language
    private static final int HOST_LT_PC = 3; // after <% - host language
    private static final int HOST_LT_BLANK = 4; // after < or </ and several blanks or \t - host language
    private static final int HOST_TAG = 5; // inside a tag, don't know whether html or JSP - host language
    private static final int HOST_LT_SLASH = 6; // after </ - host lanaguage

    // states of the automaton which looks for delimiters in the JSP tag
    private static final int JSPTAG_INIT = 1; // initial state - JSP tag
    private static final int JSPTAG_LT = 2; // after < - JSP tag
    private static final int JSPTAG_LT_PC = 3; // after <% - JSP tag

    // states of the automaton which looks for delimiters in Java
    private static final int JAVA_INIT = 1; // initial state - Java block
    private static final int JAVA_PC = 2; // after % - Java block


    protected int nestMode;

    protected SyntaxInfo hostSyntaxInfo;
    protected SyntaxInfo jspTagSyntaxInfo;
    protected SyntaxInfo javaSyntaxInfo;

    /** When returning from parseToken(), contains the state of the 'host' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo hostStateInfo;
    /** When returning from parseToken(), contains the state of the 'jspTag' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo jspTagStateInfo;
    /** When returning from parseToken(), contains the state of the 'java' slave syntax at 'offset'.
    *  Always a part of the StateInfo. */
    protected StateInfo javaStateInfo;

    // Contains the tokenLenght returned by the first call of nextToken() on the slave syntax. May need to be
    // stored in the stateinfo if tokenOffset != offset.
    // If the first call of slave's nextToken() returned EOT, this variable will not be valid, and
    // the next call must update this variable with the correct value
    private int firstTokenLength;

    // Contains the tokenID returned by the first call of nextToken() on the slave syntax. May need to be
    // stored in the stateinfo if tokenOffset != offset.
    // If the first call of slave's nextToken() returned EOT, it will be reflected in this variable, and
    // the next call must update this variable with the correct value
    private int firstTokenID;

    // One of the following stateInfos will be a part of the stateInfo, if we are returning EOT.
    // In such a case it will contain the state of the scanning syntax at 'tokenOffset'.
    private StateInfo helpHostStateInfo;
    private StateInfo helpJspTagStateInfo;
    private StateInfo helpJavaStateInfo;

    // These stateinfos hold the stateinfo after the first token returned by the scanning stave syntax.
    // Only when tokenOffset == offset, in the other case need to rescan the first token before returning.
    private StateInfo firstHostStateInfo;
    private StateInfo firstJspTagStateInfo;
    private StateInfo firstJavaStateInfo;

    private boolean debug = false;

    public JspMultiSyntax() {
        hostSyntaxInfo = registerSyntax(new HTMLSyntax());
        //hostStateInfo  = hostSyntaxInfo.syntax.createStateInfo();
        jspTagSyntaxInfo = registerSyntax(new JspTagSyntax());
        //jspTagStateInfo  = jspTagSyntaxInfo.syntax.createStateInfo();
        javaSyntaxInfo = registerSyntax(new JavaSyntax());
        //javaStateInfo  = javaSyntaxInfo.syntax.createStateInfo();

        // private stateinfos
        firstHostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
        firstJspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
        firstJavaStateInfo = javaSyntaxInfo.syntax.createStateInfo();

        helpHostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
        helpJspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
        helpJavaStateInfo = javaSyntaxInfo.syntax.createStateInfo();

        for (int i = 0; i <= javaSyntaxInfo.syntax.getHighestTokenID(); i++) {
            switch (i) {
                //case JavaSyntax.TEXT:
            case JavaSyntax.ERROR:
                break;
            default:
                changeTokenName(javaSyntaxInfo.syntax, i, JAVA_PREFIX + javaSyntaxInfo.syntax.getTokenName(i));
            }
        }

        if (hostSyntaxInfo.syntax instanceof HTMLSyntax) {
            for (int i = 0; i <= hostSyntaxInfo.syntax.getHighestTokenID(); i++) {
                switch (i) {
                    //case HTMLSyntax.TEXT:
                    //case HTMLSyntax.ERROR:
                    //  break;
                default:
                    changeTokenName(hostSyntaxInfo.syntax, i, HTML_PREFIX + hostSyntaxInfo.syntax.getTokenName(i));
                }
            }
        }

        // print out token IDs
        //if (debug) {
        //for (int i=0; i <= getHighestTokenID(); i++)
        //System.out.println("token " + (i < 10 ? "0" : "") + i + " " + getTokenName(i));
        //}

    }

    /** Parses the next token. Before entering this method the following assumptions hold:
    *  <ul>
    *  <li>'Regular' stateinfos contain the state of all active languages at 'offset'. For inactive 
    *    languages they are <code>null</code>.</li>
    *  <li>If <code>tokenOffset != offset</code>, the scanning 'help' stateinfo contains the state of the scanning
    *    language at 'tokenOffset'.</li>
    *  <li>If <code>tokenOffset != offset</code>, firstTokenID contains the token returned by the first call of
    *    slave's nextToken() in the current token, may be EOT !</li>
    *  </ul>
    */
    protected int parseToken() {
        //debug = (tokenOffset != offset);
        //debug = !((tokenOffset == offset) || (firstTokenID == EOT));  // !firstTokenNotRead

        //if (debug)
        //System.out.println("parseToken tokenOffset=" + tokenOffset + ", offset=" + offset + ", state=" + state +
        //", nestMode=" + nestMode + ", stopOffset=" + stopOffset + ", lastBuffer=" + lastBuffer);

        if (state != ISI_LANGUAGE) {
            char actChar;
            while(offset < stopOffset) {
                actChar = buffer[offset];

                switch (state) {
                case ISI_HOST_JSPTAG: // switch to JspTagSyntax
                    //if (debug)
                    //System.out.println("switching from HOST to JSPTAG, hostState " + ((BaseStateInfo)hostStateInfo).toString(this));
                    nestMode = MODE_HOST_JSPTAG;
                    state = ISI_LANGUAGE;
                    transferMasterToSlave(jspTagSyntaxInfo.syntax, null);
                    //jspTagSyntaxInfo.syntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                    if (jspTagStateInfo == null) {
                        jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                    }
                    jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                    break;

                case ISI_HOST_JAVA: // switch from hostSyntax to JavaSyntax
                    switch (actChar) {
                    case '<':
                        state = ISI_HOST_JAVA_LT;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_HOST_JAVA_LT:
                    switch (actChar) {
                    case '%':
                        state = ISI_HOST_JAVA_LT_PC;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_HOST_JAVA_LT_PC:
                    switch (actChar) {
                    case '!': // declaration
                    case '=': // expression
                        state = ISI_HOST_JAVA_JUMP;
                        offset++;
                        //if (debug)
                        //System.out.println("returning (1) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    default: // assume this is a scriptlet
                        state = ISI_HOST_JAVA_JUMP;
                        //if (debug)
                        //System.out.println("returning (2) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    } // switch (actChar)
                    // break; - not reached

                case ISI_HOST_JAVA_JUMP:
                    nestMode = MODE_HOST_JAVA;
                    state = ISI_LANGUAGE;
                    transferMasterToSlave(javaSyntaxInfo.syntax, null);
                    //javaSyntaxInfo.syntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                    if (javaStateInfo == null) {
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                    }
                    javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    break;

                case ISI_JSPTAG_JAVA: // switch from JSP tag to JavaSyntax
                    switch (actChar) {
                    case '<':
                        state = ISI_JSPTAG_JAVA_LT;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_JSPTAG_JAVA_LT:
                    switch (actChar) {
                    case '%':
                        state = ISI_JSPTAG_JAVA_LT_PC;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_JSPTAG_JAVA_LT_PC:
                    switch (actChar) {
                    case '!': // declaration
                    case '=': // expression
                        state = ISI_JSPTAG_JAVA_JUMP;
                        offset++;
                        //if (debug)
                        //System.out.println("returning (1x) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    default: // assume this is a scriptlet
                        state = ISI_JSPTAG_JAVA_JUMP;
                        //if (debug)
                        //System.out.println("returning (2x) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    } // switch (actChar)
                    // break; - not reached

                case ISI_JSPTAG_JAVA_JUMP:
                    nestMode = MODE_HOST_JSPTAG_JAVA;
                    state = ISI_LANGUAGE;
                    transferMasterToSlave(javaSyntaxInfo.syntax, null);
                    //javaSyntaxInfo.syntax.load(null, buffer, offset, stopOffset - offset, lastBuffer);
                    if (javaStateInfo == null) {
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                    }
                    javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    break;

                    // switching from Java back to JSPTAG
                case ISI_JAVA1_SWITCH:
                    switch (actChar) {
                    case '%':
                        state = ISI_JAVA1_PC;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_JAVA1_PC:
                    switch (actChar) {
                    case '>':
                        state = ISI_JAVA1_JUMP;
                        offset++;
                        //if (debug)
                        //System.out.println("returning (1xx) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    }

                case ISI_JAVA1_JUMP:
                    nestMode = MODE_HOST_JSPTAG;
                    jspTagStateInfo.setPreScan(0);
                    state = ISI_LANGUAGE;
                    javaStateInfo = null;
                    break;

                    // switching from Java back to host
                case ISI_JAVA2_SWITCH:
                    switch (actChar) {
                    case '%':
                        state = ISI_JAVA2_PC;
                        break;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    } // switch (actChar)
                    break;

                case ISI_JAVA2_PC:
                    switch (actChar) {
                    case '>':
                        state = ISI_JAVA2_JUMP;
                        offset++;
                        //if (debug)
                        //System.out.println("returning (2xx) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    default:
                        new Exception("bad hostsyntax analyzer").printStackTrace();
                    }

                case ISI_JAVA2_JUMP:
                    nestMode = MODE_HOST;
                    hostStateInfo.setPreScan(0);
                    state = ISI_LANGUAGE;
                    javaStateInfo = null;
                    break;

                }

                if (state == ISI_LANGUAGE)
                    break;

                offset = ++offset;
            } // end of while(offset...)

            if (state != ISI_LANGUAGE) {
                /** At this stage there's no more text in the scanned buffer.
                * Scanner first checks whether this is completely the last
                * available buffer.
                */
                if (lastBuffer) {
                    switch(state) {
                    case ISI_HOST_JSPTAG:
                    case ISI_HOST_JAVA:
                    case ISI_HOST_JAVA_LT:
                    case ISI_HOST_JAVA_LT_PC:
                    case ISI_HOST_JAVA_JUMP:
                    case ISI_JSPTAG_JAVA:
                    case ISI_JSPTAG_JAVA_LT:
                    case ISI_JSPTAG_JAVA_LT_PC:
                    case ISI_JSPTAG_JAVA_JUMP:
                    case ISI_JAVA1_SWITCH:
                    case ISI_JAVA1_PC:
                    case ISI_JAVA1_JUMP:
                    case ISI_JAVA2_SWITCH:
                    case ISI_JAVA2_PC:
                    case ISI_JAVA2_JUMP:
                        //if (debug)
                        //System.out.println("returning (3) pos " + offset + " symbol " + getTokenName(JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift));
                        return JspTagSyntax.JSP_SYMBOL2 + jspTagSyntaxInfo.tokenIDShift;
                    } // switch (state)
                } // if lastBuffer
                //if (debug)
                //System.out.println("returning (4) pos " + offset + " symbol " + getTokenName(EOT));
                return EOT;
            }  // if state != ISI_LANGUAGE - inner
        } // if state != ISI_LANGUAGE - outer

        // now state is ISI_LANGUAGE
        //if (state != ISI_LANGUAGE) new Exception("state should be ISI_LANGUAGE").printStackTrace();
        int slaveTokenID = INVALID;
        int returnedTokenID;

        int slaveOffset;
        int canBe;
        boolean firstTokenNotRead = ((tokenOffset == offset) || (firstTokenID == EOT));
        boolean equalPositions = (tokenOffset == offset);

        switch (nestMode) {
            // BIG BRANCH - we are in the HOST mode
        case MODE_HOST:
            if (hostStateInfo == null) {
                hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                hostSyntaxInfo.syntax.reset();
                hostSyntaxInfo.syntax.storeState(hostStateInfo);
            }

            /*if (debug) {
            System.out.print("NOT EQUAL tokenOffset=" + tokenOffset + ", offset=" + offset + ", tokenPart='");   
            for (int i = tokenOffset; i<offset;i++) System.out.print(buffer[i]);
            System.out.println("', firstTokenID=" + firstTokenID + ", firstTokenLength=" + firstTokenLength);
            System.out.println("hoststate " + hostStateInfo.getState() + ", prescan=" + hostStateInfo.getPreScan());
            System.out.println("helpstate " + helpHostStateInfo.getState() + ", prescan=" + helpHostStateInfo.getPreScan());
        }         */

            //if (equalPositions && (hostStateInfo.getPreScan() != 0))
            //new Exception("prescan should be 0 !!").printStackTrace();
            //if (debug)
            //System.out.println("html state at offset " + ((BaseStateInfo)hostStateInfo).toString(this));
            if (firstTokenNotRead) {
                // the first step - parse the first token of the slave
                transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
                returnedTokenID = hostSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + hostSyntaxInfo.tokenIDShift;
                slaveOffset = hostSyntaxInfo.syntax.getOffset();
                firstTokenID = slaveTokenID;
                firstTokenLength = hostSyntaxInfo.syntax.getTokenLength();
                if (slaveTokenID == EOT) {
                    offset = slaveOffset;
                    firstTokenLength = -1;
                    // need to property transfer states
                    if (equalPositions) {
                        helpHostStateInfo = hostStateInfo;
                        hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                        hostSyntaxInfo.syntax.storeState(hostStateInfo);
                    }
                    else {
                        if (hostStateInfo == null) {
                            hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                        }
                        hostSyntaxInfo.syntax.storeState(hostStateInfo);
                        new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (1) " + EOT + " at " + offset);
                    return EOT;
                }
                // find out if the token could contain a starting symbol for JspTag or Java
                canBe = canBeHostDelimiter(tokenOffset, slaveOffset, slaveOffset, false);
                if (canBe == DELIMCHECK_NO) { // do not switch
                    offset = slaveOffset;
                    if (hostStateInfo == null) {
                        hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                    }
                    hostSyntaxInfo.syntax.storeState(hostStateInfo);
                    //if (debug)
                    //System.out.println("returnuju (2) " + slaveTokenID + " at " + offset);
                    return slaveTokenID;
                }
                // store the state
                hostSyntaxInfo.syntax.storeState(firstHostStateInfo);
                //if (firstHostStateInfo == hostStateInfo)
                //new Exception("stateinfo instance conflict").printStackTrace();
            }
            else { // first position read - offsets different and firstTokenID is a valid token
                transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
                canBe = DELIMCHECK_PART;
            }

            // we have successfully read the first token, the following statements hold:
            // - canBe is not DELIMCHECK_NO
            // - firstTokenID and firstTokenLength are meaningful
            // - if (equalPositions) then firstHostStateInfo is meaningful
            //if (firstTokenID == EOT) {
            //new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
            //}
            while (canBe == DELIMCHECK_PART) { // need another token
                // now get the new token
                returnedTokenID = hostSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + hostSyntaxInfo.tokenIDShift;
                slaveOffset = hostSyntaxInfo.syntax.getOffset();

                if ((slaveTokenID == EOT) && lastBuffer) {
                    // ask about the delimiter, but with lastPart=true
                    canBe = canBeHostDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true);
                    if (canBe != DELIMCHECK_PART)
                        break;
                }

                if (slaveTokenID == EOT) {
                    if (lastBuffer) {
                        canBe = DELIMCHECK_NO;
                        break;
                    }
                    offset = slaveOffset;
                    if (equalPositions) {
                        helpHostStateInfo = hostStateInfo;
                        hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                        hostSyntaxInfo.syntax.storeState(hostStateInfo);
                    }
                    else {
                        if (hostStateInfo == null) {
                            hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                        }
                        hostSyntaxInfo.syntax.storeState(hostStateInfo);
                        //new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (3) " + EOT + " at " + offset);
                    return EOT;
                }
                canBe = canBeHostDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false);
            }

            // now canBe is not DELIMCHECK_PART
            // now we have read possibly more tokens and know whether to switch or not
            if (canBe == DELIMCHECK_NO) { // do not switch
                offset = tokenOffset + firstTokenLength;
                if (equalPositions) {
                    hostStateInfo = firstHostStateInfo;
                    firstHostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                }
                else {
                    //if (debug)
                    //System.out.println("= imagine - rescan called !!");
                    //if (helpHostStateInfo.getPreScan() != 0)
                    //new Exception("help prescan should be 0 !!").printStackTrace();
                    // we need to rescan the first token to find out the state
                    // now helpHostStateInfo is useful
                    hostSyntaxInfo.syntax.load(helpHostStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer);
                    returnedTokenID = hostSyntaxInfo.syntax.nextToken();
                    //if (tokenOffset != hostSyntaxInfo.syntax.getTokenOffset())
                    //new Exception("starts of tokens do not match").printStackTrace();
                    slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + hostSyntaxInfo.tokenIDShift;
                    if (hostStateInfo == null) {
                        hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                    }
                    hostSyntaxInfo.syntax.storeState(hostStateInfo);
                    //if (slaveTokenID != firstTokenID)
                    //new Exception("token ID does not match !!!!!!!").printStackTrace();
                    //if (offset != hostSyntaxInfo.syntax.getOffset())
                    //new Exception("offset does not match !!!!!!!").printStackTrace();

                    /*if (debug)
                    System.out.println("delimiter NO, equalPositions=false, firstTokenNotRead=" + firstTokenNotRead +
                    ", tokenOffset=" + tokenOffset + ", offset=" + offset + ", slave offset=" + 
                    hostSyntaxInfo.syntax.getOffset() + ", token='" + getToken(hostSyntaxInfo.syntax) +"', tokenID=" +
                    slaveTokenID + ", firstTokenID=" + firstTokenID);*/
                }
                //if (debug)
                //System.out.println("returnuju (4) " + firstTokenID + " at " + offset);

                return firstTokenID;
            }
            else { // we found a delimiter
                //if (canBe >= tokenOffset + firstTokenLength)
                //new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                // now use the saved state
                if (equalPositions) {
                    hostSyntaxInfo.syntax.load(hostStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                else {
                    hostSyntaxInfo.syntax.load(helpHostStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                returnedTokenID = hostSyntaxInfo.syntax.nextToken();
                // we got the StateInfo, which is why we did all this
                if (hostStateInfo == null) {
                    hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
                }
                //if (debug)
                //System.out.println("html state before saving back " + ((BaseStateInfo)hostStateInfo).toString(this));
                hostSyntaxInfo.syntax.storeState(hostStateInfo);
                hostStateInfo.setPreScan(0);
                //if (hostSyntaxInfo.syntax.getOffset() != canBe)
                //new Exception("bad number of characters parsed !!!").printStackTrace();
                offset = canBe;
                /*if (debug) {
                System.out.println("switching from HOST to JSPTAG at offset " + offset + ", hostState " + ((BaseStateInfo)hostStateInfo).toString(this));
                System.out.println("offset of the returned (a)" + hostSyntaxInfo.syntax.getOffset());          
                System.out.println("found delimiter at " + offset);          
                System.out.println("returnuju (5) " + firstTokenID + " at " + offset);            
            }*/
                return firstTokenID;
            }
            //break; //- not reached


            // BIG BRANCH - we are in the HOST_JSPTAG mode
        case MODE_HOST_JSPTAG:
            // check if the JSP tag hasn't finished on its own will
            if ((jspTagStateInfo != null) && (jspTagStateInfo.getState() == JspTagSyntax.ISA_END_JSP)) {
                // give up control
                jspTagStateInfo = null;
                nestMode = MODE_HOST;
                hostStateInfo.setPreScan(0);
                /*if (debug) {
                System.out.println("switching back to HOST from JSPTAG at offset " + offset + ", hostState " + ((BaseStateInfo)hostStateInfo).toString(this));
                System.out.println("returnuju (6) " + JspTagSyntax.TEXT + jspTagSyntaxInfo.tokenIDShift + " at " + offset);            
            }*/
                return JspTagSyntax.TEXT + jspTagSyntaxInfo.tokenIDShift;
            }

            if (jspTagStateInfo == null) {
                jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                jspTagSyntaxInfo.syntax.reset();
                jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
            }
            if (firstTokenNotRead) {
                // the first step - parse the first token of the slave
                transferMasterToSlave(jspTagSyntaxInfo.syntax, jspTagStateInfo);
                returnedTokenID = jspTagSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + jspTagSyntaxInfo.tokenIDShift;
                //if (debug)
                //System.out.println("first JSPtoken returned '" + getToken(jspTagSyntaxInfo.syntax) + "' id " + slaveTokenID);
                slaveOffset = jspTagSyntaxInfo.syntax.getOffset();
                firstTokenID = slaveTokenID;
                firstTokenLength = jspTagSyntaxInfo.syntax.getTokenLength();
                if (slaveTokenID == EOT) {
                    offset = slaveOffset;
                    firstTokenLength = -1;
                    // need to properly transfer states
                    if (equalPositions) {
                        helpJspTagStateInfo = jspTagStateInfo;
                        jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                        jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                    }
                    else {
                        if (jspTagStateInfo == null) {
                            jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                        }
                        jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                        //new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (7) " + EOT + " at " + offset);
                    return EOT;
                }
                // find out if the token could contain a starting symbol for Java
                canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, slaveOffset, false, returnedTokenID == JspTagSyntax.JSP_COMMENT);
                if (canBe == DELIMCHECK_NO) { // do not switch
                    offset = slaveOffset;
                    if (jspTagStateInfo == null) {
                        jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                    }
                    jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                    //if (debug)
                    //System.out.println("returnuju (8) " + slaveTokenID + " at " + offset);
                    return slaveTokenID;
                }
                // store the state
                jspTagSyntaxInfo.syntax.storeState(firstJspTagStateInfo);
            }
            else { // first position read - offsets different and firstTokenID is a valid token
                transferMasterToSlave(jspTagSyntaxInfo.syntax, jspTagStateInfo);
                canBe = DELIMCHECK_PART;
            }

            // we have successfully read the first token, the following statements hold:
            // - canBe is not DELIMCHECK_NO
            // - firstTokenID and firstTokenLength are meaningful
            // - if (equalPositions) then firstJspTagStateInfo is meaningful
            //if (firstTokenID == EOT) {
            //new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
            //}
            while (canBe == DELIMCHECK_PART) { // need another token
                // now get the new token
                returnedTokenID = jspTagSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + jspTagSyntaxInfo.tokenIDShift;
                slaveOffset = jspTagSyntaxInfo.syntax.getOffset();

                if ((slaveTokenID == EOT) && lastBuffer) {
                    // ask about the delimiter, but with lastPart=true
                    canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true, returnedTokenID == JspTagSyntax.JSP_COMMENT);
                    if (canBe != DELIMCHECK_PART)
                        break;
                }

                if (slaveTokenID == EOT) {
                    if (lastBuffer) {
                        canBe = DELIMCHECK_NO;
                        break;
                    }
                    offset = slaveOffset;
                    if (equalPositions) {
                        helpJspTagStateInfo = jspTagStateInfo;
                        jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                        jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                    }
                    else {
                        if (jspTagStateInfo == null) {
                            jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                        }
                        jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                        //new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (9) " + EOT + " at " + offset);
                    return EOT;
                }
                canBe = canBeJspTagDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false, returnedTokenID == JspTagSyntax.JSP_COMMENT);
            }

            // now canBe is not DELIMCHECK_PART
            // now we have read possibly more tokens and know whether to switch or not
            if (canBe == DELIMCHECK_NO) { // do not switch
                offset = tokenOffset + firstTokenLength;
                if (equalPositions) {
                    jspTagStateInfo = firstJspTagStateInfo;
                    firstJspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                }
                else {
                    //if (debug)
                    //System.out.println("= imagine - rescan called !!");
                    // we need to rescan the first token to find out the state
                    // now helpJspTagStateInfo is useful
                    jspTagSyntaxInfo.syntax.load(helpJspTagStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer);
                    returnedTokenID = jspTagSyntaxInfo.syntax.nextToken();
                    slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + jspTagSyntaxInfo.tokenIDShift;
                    if (jspTagStateInfo == null) {
                        jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                    }
                    jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                    //if (slaveTokenID != firstTokenID)
                    //new Exception("token ID does not match !!!!!!!").printStackTrace();
                    //if (offset != jspTagSyntaxInfo.syntax.getOffset())
                    //new Exception("offset does not match !!!!!!!").printStackTrace();
                }
                //if (debug)
                //System.out.println("returnuju (10) " + firstTokenID + " at " + offset);
                return firstTokenID;
            }
            else { // we found a delimiter
                //if (canBe >= tokenOffset + firstTokenLength)
                //new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                // now use the saved state
                if (equalPositions) {
                    jspTagSyntaxInfo.syntax.load(jspTagStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                else {
                    jspTagSyntaxInfo.syntax.load(helpJspTagStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                returnedTokenID = jspTagSyntaxInfo.syntax.nextToken();
                // we got the StateInfo, which is why we did all this
                if (jspTagStateInfo == null) {
                    jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
                }
                jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
                jspTagStateInfo.setPreScan(0);
                //if (jspTagSyntaxInfo.syntax.getOffset() != canBe)
                //new Exception("bad number of characters parsed !!!").printStackTrace();
                offset = canBe;
                /*if (debug) {
                System.out.println("offset of the returned (a)" + jspTagSyntaxInfo.syntax.getOffset());          
                System.out.println("found delimiter at " + offset);          
                System.out.println("returnuju (11) " + firstTokenID + " at " + offset);            
            }*/
                return firstTokenID;
            }
            //break; //- not reached

        case MODE_HOST_JSPTAG_JAVA:
        case MODE_HOST_JAVA:
            if (javaStateInfo == null) {
                javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                javaSyntaxInfo.syntax.reset();
                javaSyntaxInfo.syntax.storeState(javaStateInfo);
            }
            if (firstTokenNotRead) {
                // the first step - parse the first token of the slave
                transferMasterToSlave(javaSyntaxInfo.syntax, javaStateInfo);
                returnedTokenID = javaSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + javaSyntaxInfo.tokenIDShift;
                slaveOffset = javaSyntaxInfo.syntax.getOffset();
                firstTokenID = slaveTokenID;
                firstTokenLength = javaSyntaxInfo.syntax.getTokenLength();
                if (slaveTokenID == EOT) {
                    offset = slaveOffset;
                    firstTokenLength = -1;
                    // need to property transfer states
                    if (equalPositions) {
                        helpJavaStateInfo = javaStateInfo;
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                        javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    }
                    else {
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                        }
                        javaSyntaxInfo.syntax.storeState(javaStateInfo);
                        //new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (12) " + EOT + " at " + offset);
                    return EOT;
                }
                // find out if the token could contain an ending symbol for a Java block
                canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, slaveOffset, false, nestMode);
                if (canBe == DELIMCHECK_NO) { // do not switch
                    offset = slaveOffset;
                    if (javaStateInfo == null) {
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                    }
                    javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    //if (debug)
                    //System.out.println("returnuju (13) " + slaveTokenID + " at " + offset);
                    return slaveTokenID;
                }
                // store the state
                javaSyntaxInfo.syntax.storeState(firstJavaStateInfo);
            }
            else { // first position read - offsets different and firstTokenID is a valid token
                transferMasterToSlave(javaSyntaxInfo.syntax, javaStateInfo);
                canBe = DELIMCHECK_PART;
            }

            // we have successfully read the first token, the following statements hold:
            // - canBe is not DELIMCHECK_NO
            // - firstTokenID and firstTokenLength are meaningful
            // - if (equalPositions) then firstJavaStateInfo is meaningful
            //if (firstTokenID == EOT) {
            //new Exception("invalid firstTokenID !!!!!!!").printStackTrace();
            //}
            while (canBe == DELIMCHECK_PART) { // need another token
                // now get the new token
                returnedTokenID = javaSyntaxInfo.syntax.nextToken();
                slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + javaSyntaxInfo.tokenIDShift;
                slaveOffset = javaSyntaxInfo.syntax.getOffset();

                if ((slaveTokenID == EOT) && lastBuffer) {
                    // ask about the delimiter, but with lastPart=true
                    canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, true, nestMode);
                    if (canBe != DELIMCHECK_PART)
                        break;
                }

                if (slaveTokenID == EOT) {
                    if (lastBuffer) {
                        canBe = DELIMCHECK_NO;
                        break;
                    }
                    offset = slaveOffset;
                    if (equalPositions) {
                        helpJavaStateInfo = javaStateInfo;
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                        javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    }
                    else {
                        if (javaStateInfo == null) {
                            javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                        }
                        javaSyntaxInfo.syntax.storeState(javaStateInfo);
                        //new Exception("returning EOT twice in a row !!!!!!!").printStackTrace();
                    }
                    //if (debug)
                    //System.out.println("returnuju (14) " + EOT + " at " + offset);
                    return EOT;
                }
                canBe = canBeJavaDelimiter(tokenOffset, slaveOffset, tokenOffset + firstTokenLength, false, nestMode);
            }

            // now canBe is not DELIMCHECK_PART
            // now we have read possibly more tokens and know whether to switch or not
            if (canBe == DELIMCHECK_NO) { // do not switch
                offset = tokenOffset + firstTokenLength;
                if (equalPositions) {
                    javaStateInfo = firstJavaStateInfo;
                    firstJavaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                }
                else {
                    //if (debug)
                    //System.out.println("= imagine - rescan called !!");
                    // we need to rescan the first token to find out the state
                    // now helpJavaStateInfo is useful
                    javaSyntaxInfo.syntax.load(helpJavaStateInfo, buffer, tokenOffset, stopOffset - tokenOffset, lastBuffer);
                    returnedTokenID = javaSyntaxInfo.syntax.nextToken();
                    slaveTokenID = returnedTokenID <= EOL ? returnedTokenID : returnedTokenID + javaSyntaxInfo.tokenIDShift;
                    if (javaStateInfo == null) {
                        javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                    }
                    javaSyntaxInfo.syntax.storeState(javaStateInfo);
                    //if (slaveTokenID != firstTokenID)
                    //new Exception("token ID does not match !!!!!!!").printStackTrace();
                    //if (offset != javaSyntaxInfo.syntax.getOffset())
                    //new Exception("offset does not match !!!!!!!").printStackTrace();
                }
                //if (debug)
                //System.out.println("returnuju (15) " + firstTokenID + " at " + offset);
                return firstTokenID;
            }
            else { // we found a delimiter
                //if (canBe >= tokenOffset + firstTokenLength)
                //new Exception("value of canBe is invalid !!!!!!!").printStackTrace();
                // now use the saved state
                if (equalPositions) {
                    javaSyntaxInfo.syntax.load(javaStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                else {
                    javaSyntaxInfo.syntax.load(helpJavaStateInfo, buffer, tokenOffset, canBe - tokenOffset, true);
                }
                returnedTokenID = javaSyntaxInfo.syntax.nextToken();
                // we got the StateInfo, which is why we did all this
                if (javaStateInfo == null) {
                    javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
                }
                javaSyntaxInfo.syntax.storeState(javaStateInfo);
                javaStateInfo.setPreScan(0);
                //if (javaSyntaxInfo.syntax.getOffset() != canBe)
                //new Exception("bad number of characters parsed !!!").printStackTrace();
                offset = canBe;
                /*if (debug) {
                System.out.println("offset of the returned (a)" + javaSyntaxInfo.syntax.getOffset());          
                System.out.println("found delimiter at " + offset);          
                System.out.println("returnuju (16) " + firstTokenID + " at " + offset);            
            }*/
                return firstTokenID;
            }
            // break; //- not reached
        default:
            new Exception("bad nestmode").printStackTrace();
            return 0;
        }
    }

    /** Method for debugging purposes */
    private String getToken(Syntax syntax) {
        StringBuffer token = new StringBuffer();
        for (int i=syntax.getOffset()-syntax.getTokenLength();i<syntax.getOffset();i++)
            token.append(buffer[i]);
        return token.toString();
    }

    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could start a JSP tag of a Java block in
    * the host language, i.e. <code>&lt;jsp:useBean</code> or <code>&lt;%=</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeHostDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart) {
        int offset = tokenOffset;
        char actChar;

        int possibleBeginning = DELIMCHECK_NO;
        StringBuffer tagString = null;
        int delimState = HOST_INIT;

        while(offset < endOffset) {
            actChar = buffer[offset];

            switch (delimState) {
            case HOST_INIT:
                switch (actChar) {
                case '<':
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    delimState = HOST_LT;
                    possibleBeginning = offset;
                    break;
                default:
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    break;
                }
                break;

            case HOST_LT:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) { // possible tag begining
                    delimState = HOST_TAG;
                    tagString = new StringBuffer();
                    tagString.append(actChar);
                    break; // the switch statement
                }

                switch (actChar) {
                case '\n':
                    delimState = HOST_INIT;
                    break;
                case '%':
                    delimState = HOST_LT_PC;
                    break;
                case '/':
                    delimState = HOST_LT_SLASH;
                    break;
                case ' ':
                case '\t':
                    delimState = HOST_LT_BLANK;
                    break;
                default:
                    delimState = HOST_INIT;
                    offset--;
                    break;
                }
                break;

            case HOST_LT_SLASH:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) { // possible tag begining
                    delimState = HOST_TAG;
                    tagString = new StringBuffer();
                    tagString.append(actChar);
                    break; // the switch statement
                }

                switch (actChar) {
                case '\n':
                    delimState = HOST_INIT;
                    break;
                case ' ':
                case '\t':
                    delimState = HOST_LT_BLANK;
                    break;
                default:
                    delimState = HOST_INIT;
                    offset--;
                    break;
                }
                break;

            case HOST_LT_BLANK:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar == '_')
                   ) { // possible tag begining
                    delimState = HOST_TAG;
                    tagString = new StringBuffer(actChar);
                    break; // the switch statement
                }

                switch (actChar) {
                case '\n':
                    delimState = HOST_INIT;
                    break;
                case ' ':
                case '\t':
                    break;
                default:
                    delimState = HOST_INIT;
                    offset--;
                    break;
                }
                break;

            case HOST_TAG:
                if ((actChar >= 'A' && actChar <= 'Z') ||
                        (actChar >= 'a' && actChar <= 'z') ||
                        (actChar >= '0' && actChar <= '9') ||
                        (actChar == ':') ||
                        (actChar == '_')
                   ) { // the tag continues
                    tagString.append(actChar);
                    break; // the switch statement
                }

                switch (actChar) {
                default:
                    if (isJspTag(tagString.toString())) {
                        state = ISI_HOST_JSPTAG;
                        //if (debug)
                        //System.out.println("found beginning of JspTag at " + possibleBeginning);
                        return possibleBeginning;
                    }
                    else {
                        delimState = HOST_INIT;
                        offset--;
                        break;
                    }
                }
                break;

            case HOST_LT_PC:
                switch (actChar) {
                case '@': // directive
                case '-': // JSP comment
                    state = ISI_HOST_JSPTAG;
                    //if (debug)
                    //System.out.println("found beginning of directive/comment at " + possibleBeginning);
                    return possibleBeginning;
                case '!': // declaration
                case '=': // expression
                    state = ISI_HOST_JAVA;
                    //if (debug)
                    //System.out.println("found beginning of declaration/expression at " + possibleBeginning);
                    return possibleBeginning;
                default: // scriptlet
                    state = ISI_HOST_JAVA;
                    //if (debug)
                    //System.out.println("found beginning of scriptlet at " + possibleBeginning);
                    return possibleBeginning;
                }
                //break;
            }

            offset++;
        }

        if (lastPart) {
            switch (delimState) {
            case HOST_LT_PC:
                state = ISI_HOST_JAVA;
                return possibleBeginning;
            case HOST_TAG:
                if (isJspTag(tagString.toString())) {
                    state = ISI_HOST_JSPTAG;
                    //if (debug)
                    //System.out.println("found beginning of JspTag at " + possibleBeginning);
                    return possibleBeginning;
                }
            }
        }

        // we have reached the end of the scanned area
        switch (delimState) {
        case HOST_INIT:
            return DELIMCHECK_NO;
        case HOST_LT:
        case HOST_LT_SLASH:
        case HOST_LT_PC:
        case HOST_LT_BLANK:
        case HOST_TAG:
            return DELIMCHECK_PART;
        default:
            new Exception("invalid state").printStackTrace();
            return DELIMCHECK_NO;
        }
    }

    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could start a Java block inside a JSP tag,
    * i.e. <code>&lt;%=</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeJspTagDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart, boolean isComment) {
        if (isComment)
            return DELIMCHECK_NO;

        int offset = tokenOffset;
        char actChar;

        int possibleBeginning = DELIMCHECK_NO;
        int delimState = JSPTAG_INIT;

        while(offset < endOffset) {
            actChar = buffer[offset];

            switch (delimState) {
            case JSPTAG_INIT:
                switch (actChar) {
                case '<':
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    delimState = JSPTAG_LT;
                    possibleBeginning = offset;
                    break;
                default:
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    break;
                }
                break;

            case JSPTAG_LT:
                switch (actChar) {
                case '\n':
                    delimState = JSPTAG_INIT;
                    break;
                case '%':
                    delimState = JSPTAG_LT_PC;
                    break;
                default:
                    delimState = JSPTAG_INIT;
                    offset--;
                    break;
                }
                break;

            case JSPTAG_LT_PC:
                switch (actChar) {
                case '!': // declaration
                case '=': // expression
                    state = ISI_JSPTAG_JAVA;
                    //if (debug)
                    //System.out.println("found beginning of declaration/expression at " + possibleBeginning);
                    return possibleBeginning;
                case '@': // declaration
                case '-': // comment
                    delimState = JSPTAG_INIT;
                    break;
                default: // scriptlet
                    state = ISI_JSPTAG_JAVA;
                    //if (debug)
                    //System.out.println("found beginning of scriptlet at " + possibleBeginning);
                    return possibleBeginning;
                }
                //break;
            }

            offset++;
        }

        if (lastPart) {
            switch (delimState) {
            case JSPTAG_LT_PC:
                state = ISI_JSPTAG_JAVA;
                //if (debug)
                //System.out.println("found beginning of scriptlet at " + possibleBeginning);
                return possibleBeginning;
            }
        }

        // we have reached the end of the scanned area
        switch (delimState) {
        case JSPTAG_INIT:
            return DELIMCHECK_NO;
        case JSPTAG_LT:
            return DELIMCHECK_PART;
        case JSPTAG_LT_PC:
            return DELIMCHECK_PART;
        default:
            new Exception("invalid state").printStackTrace();
            return DELIMCHECK_NO;
        }
    }

    /** Checks if the part of the buffer starting at tokenOffset and ending just before endOffset
    * contains a "delimiter" or could contain a starting part of a "delimiter", where
    * "delimiter" is a lexical structure which could end a Java block,
    * i.e. <code>%&gt;</code>.
    * @return <ul>
    *      <li><code>DELIMCHECK_NO</code> if the part of the buffer does not contain a delimiter or its part</li>
    *      <li><code>DELIMCHECK_PART</code> if the part of the buffer contains part of the delimiter</li>
    *      <li>index of the starting symbol of the delimiter if the part of the buffer contains the delimiter.
    *           In such a case variable <code>state</code> is set properly.</li>
    *         </ul>
    */
    protected int canBeJavaDelimiter(int tokenOffset, int endOffset, int firstTokenEnd, boolean lastPart, int myNestMode) {
        int offset = tokenOffset;
        char actChar;

        int possibleBeginning = DELIMCHECK_NO;
        int delimState = JAVA_INIT;

        while(offset < endOffset) {
            actChar = buffer[offset];

            switch (delimState) {
            case JAVA_INIT:
                switch (actChar) {
                case '%':
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    delimState = JAVA_PC;
                    possibleBeginning = offset;
                    break;
                default:
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    break;
                }
                break;

            case JAVA_PC:
                switch (actChar) {
                case '>':
                    switch (myNestMode) {
                    case MODE_HOST_JSPTAG_JAVA:
                        state = ISI_JAVA1_SWITCH;
                        //if (debug)
                        //System.out.println("found end of Java at " + possibleBeginning);
                        return possibleBeginning;
                    case MODE_HOST_JAVA:
                        state = ISI_JAVA2_SWITCH;
                        //if (debug)
                        //System.out.println("found end of Java at " + possibleBeginning);
                        return possibleBeginning;
                    }
                    new Exception("bad nestMode").printStackTrace();
                    //break; - not reached
                case '%':
                    if (offset >= firstTokenEnd)
                        return DELIMCHECK_NO;
                    delimState = JAVA_PC;
                    possibleBeginning = offset;
                    break;
                default:
                    delimState = JAVA_INIT;
                    break;
                }
                break;
            }

            offset++;
        }

        // we have reached the end of the scanned area
        switch (delimState) {
        case JAVA_INIT:
            return DELIMCHECK_NO;
        case JAVA_PC:
            return DELIMCHECK_PART;
        default:
            new Exception("invalid state").printStackTrace();
            return DELIMCHECK_NO;
        }
    }

    /** Determines whether a given string is a JSP tag. */
    protected boolean isJspTag(String tagName) {
        boolean canBeJsp = tagName.startsWith("jsp:");
        //if (debug)
        //System.out.println("can be JSP: '" + tagName + "' " + canBeJsp);
        return canBeJsp;
    }

    private void transferMasterToSlave(Syntax slave, StateInfo stateInfo) {
        slave.load(stateInfo, buffer, offset, stopOffset - offset, lastBuffer);
        //slave.setLastBuffer(lastBuffer);  // PENDING - maybe not necessary
        //slave.setStopOffset(stopOffset);  // PENDING - maybe not necessary
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        super.storeState(stateInfo);
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        // nest mode
        jspsi.nestMode = nestMode;
        // regular stateinfos
        if (hostStateInfo == null) {
            jspsi.hostStateInfo = null;
        }
        else {
            jspsi.hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
            hostSyntaxInfo.syntax.load(hostStateInfo, buffer, offset, 0, false);
            hostSyntaxInfo.syntax.storeState(jspsi.hostStateInfo);
        }
        if (jspTagStateInfo == null) {
            jspsi.jspTagStateInfo = null;
        }
        else {
            jspsi.jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
            jspTagSyntaxInfo.syntax.load(jspTagStateInfo, buffer, offset, 0, false);
            jspTagSyntaxInfo.syntax.storeState(jspsi.jspTagStateInfo);
        }
        if (javaStateInfo == null) {
            jspsi.javaStateInfo = null;
        }
        else {
            jspsi.javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
            javaSyntaxInfo.syntax.load(javaStateInfo, buffer, offset, 0, false);
            javaSyntaxInfo.syntax.storeState(jspsi.javaStateInfo);
        }
        // stateOfScanningAtInit, firstTokenID, firstTokenLength
        if (jspsi.isFirstTokenValid()) {
            jspsi.firstTokenID = firstTokenID;
            jspsi.firstTokenLength = firstTokenLength;
            switch (nestMode) {
            case MODE_HOST:
                jspsi.stateOfScanningAtInit = hostSyntaxInfo.syntax.createStateInfo();
                hostSyntaxInfo.syntax.load(helpHostStateInfo, buffer, offset, 0, false);
                hostSyntaxInfo.syntax.storeState(jspsi.stateOfScanningAtInit);
                break;
            case MODE_HOST_JSPTAG:
                jspsi.stateOfScanningAtInit = jspTagSyntaxInfo.syntax.createStateInfo();
                jspTagSyntaxInfo.syntax.load(helpJspTagStateInfo, buffer, offset, 0, false);
                jspTagSyntaxInfo.syntax.storeState(jspsi.stateOfScanningAtInit);
                break;
            case MODE_HOST_JSPTAG_JAVA:
            case MODE_HOST_JAVA:
                jspsi.stateOfScanningAtInit = javaSyntaxInfo.syntax.createStateInfo();
                javaSyntaxInfo.syntax.load(helpJavaStateInfo, buffer, offset, 0, false);
                javaSyntaxInfo.syntax.storeState(jspsi.stateOfScanningAtInit);
                break;
            }
        }
        else {
            jspsi.stateOfScanningAtInit = null;
            jspsi.firstTokenID = EOT;
            jspsi.firstTokenLength = -1;
        }
        /*System.out.print("storing state at offset=" + offset + ", tokenOffset=" + tokenOffset + ", token=");
        for(int i=tokenOffset;i<offset;i++)
        System.out.print(buffer[i]);
        System.out.println();
        System.out.println(((JspStateInfo)stateInfo).toString(this));*/
    }

    public void loadState(StateInfo stateInfo) {
        /*System.out.println("loading state");
        System.out.println(((JspStateInfo)stateInfo).toString(this));    */
        super.loadState(stateInfo);
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        nestMode = jspsi.nestMode;
        // now all the slave states
        if (jspsi.hostStateInfo == null) {
            hostStateInfo = null;
        }
        else {
            hostSyntaxInfo.syntax.load(jspsi.hostStateInfo, buffer, offset, 0, false);
            hostStateInfo = hostSyntaxInfo.syntax.createStateInfo();
            hostSyntaxInfo.syntax.storeState(hostStateInfo);
        }
        if (jspsi.jspTagStateInfo == null) {
            jspTagStateInfo = null;
        }
        else {
            jspTagSyntaxInfo.syntax.load(jspsi.jspTagStateInfo, buffer, offset, 0, false);
            jspTagStateInfo = jspTagSyntaxInfo.syntax.createStateInfo();
            jspTagSyntaxInfo.syntax.storeState(jspTagStateInfo);
        }
        if (jspsi.javaStateInfo == null) {
            javaStateInfo = null;
        }
        else {
            javaSyntaxInfo.syntax.load(jspsi.javaStateInfo, buffer, offset, 0, false);
            javaStateInfo = javaSyntaxInfo.syntax.createStateInfo();
            javaSyntaxInfo.syntax.storeState(javaStateInfo);
        }
        // stateOfScanningAtInit, firstTokenID, firstTokenLength
        if (jspsi.isFirstTokenValid()) {
            firstTokenID = jspsi.firstTokenID;
            firstTokenLength = jspsi.firstTokenLength;
            switch (jspsi.nestMode) {
            case MODE_HOST:
                hostSyntaxInfo.syntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false);
                hostSyntaxInfo.syntax.storeState(helpHostStateInfo);
                break;
            case MODE_HOST_JSPTAG:
                jspTagSyntaxInfo.syntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false);
                jspTagSyntaxInfo.syntax.storeState(helpJspTagStateInfo);
                break;
            case MODE_HOST_JSPTAG_JAVA:
            case MODE_HOST_JAVA:
                javaSyntaxInfo.syntax.load(jspsi.stateOfScanningAtInit, buffer, offset, 0, false);
                javaSyntaxInfo.syntax.storeState(helpJavaStateInfo);
                break;
            }
        }
        else {
            firstTokenID = EOT;
            firstTokenLength = -1;
        }
    }

    public void loadInitState() {
        super.loadInitState();
        nestMode = MODE_HOST;
        state = ISI_LANGUAGE;
        hostStateInfo = null;
        jspTagStateInfo = null;
        javaStateInfo = null;
        firstTokenID = EOT;
        firstTokenLength = -1;
    }

    public void load(StateInfo stateInfo, char buffer[], int offset, int len, boolean lastBuffer) {
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        if (jspsi == null) {
            hostStateInfo = null;
            jspTagStateInfo = null;
            javaStateInfo = null;
            firstTokenID = EOT;
            firstTokenLength = -1;
        }
        super.load(stateInfo, buffer, offset, len, lastBuffer);
    }

    public StateInfo createStateInfo() {
        return new JspStateInfo();
    }

    public int compareState(StateInfo stateInfo) {
        if (super.compareState(stateInfo) == DIFFERENT_STATE)
            return DIFFERENT_STATE;
        JspStateInfo jspsi = (JspStateInfo)stateInfo;
        if (jspsi.nestMode != nestMode)
            return DIFFERENT_STATE;
        if (jspsi.isFirstTokenValid()) {
            if (jspsi.firstTokenID != firstTokenID)
                return DIFFERENT_STATE;
            if (jspsi.firstTokenLength != firstTokenLength)
                return DIFFERENT_STATE;
        }
        int ret;
        switch (nestMode) {
        case MODE_HOST:
            // host
            transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
            ret = hostSyntaxInfo.syntax.compareState(jspsi.hostStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            if (jspsi.isFirstTokenValid()) {
                transferMasterToSlave(hostSyntaxInfo.syntax, helpHostStateInfo);
                ret = hostSyntaxInfo.syntax.compareState(jspsi.stateOfScanningAtInit);
                if (ret == DIFFERENT_STATE) return ret;
            }
            break;
        case MODE_HOST_JSPTAG:
            // host
            transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
            ret = hostSyntaxInfo.syntax.compareState(jspsi.hostStateInfo);
            if (ret == Syntax.DIFFERENT_STATE) return ret;
            // jspTag
            transferMasterToSlave(jspTagSyntaxInfo.syntax, jspTagStateInfo);
            ret = jspTagSyntaxInfo.syntax.compareState(jspsi.jspTagStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            if (jspsi.isFirstTokenValid()) {
                transferMasterToSlave(jspTagSyntaxInfo.syntax, helpJspTagStateInfo);
                ret = jspTagSyntaxInfo.syntax.compareState(jspsi.stateOfScanningAtInit);
                if (ret == DIFFERENT_STATE) return ret;
            }
            break;
        case MODE_HOST_JSPTAG_JAVA:
            // host
            transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
            ret = hostSyntaxInfo.syntax.compareState(jspsi.hostStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            // jspTag
            transferMasterToSlave(jspTagSyntaxInfo.syntax, jspTagStateInfo);
            ret = jspTagSyntaxInfo.syntax.compareState(jspsi.jspTagStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            // java
            transferMasterToSlave(javaSyntaxInfo.syntax, javaStateInfo);
            ret = javaSyntaxInfo.syntax.compareState(jspsi.javaStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            if (jspsi.isFirstTokenValid()) {
                transferMasterToSlave(javaSyntaxInfo.syntax, helpJavaStateInfo);
                ret = javaSyntaxInfo.syntax.compareState(jspsi.stateOfScanningAtInit);
                if (ret == DIFFERENT_STATE) return ret;
            }
            break;
        case MODE_HOST_JAVA:
            // host
            transferMasterToSlave(hostSyntaxInfo.syntax, hostStateInfo);
            ret = hostSyntaxInfo.syntax.compareState(jspsi.hostStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            // java
            transferMasterToSlave(javaSyntaxInfo.syntax, javaStateInfo);
            ret = javaSyntaxInfo.syntax.compareState(jspsi.javaStateInfo);
            if (ret == DIFFERENT_STATE) return ret;
            if (jspsi.isFirstTokenValid()) {
                transferMasterToSlave(javaSyntaxInfo.syntax, helpJavaStateInfo);
                ret = javaSyntaxInfo.syntax.compareState(jspsi.stateOfScanningAtInit);
                if (ret == DIFFERENT_STATE) return ret;
            }
            break;
        }
        return EQUAL_STATE;
    }

    public static class JspStateInfo extends BaseStateInfo {

        int nestMode;
        StateInfo hostStateInfo;
        StateInfo jspTagStateInfo;
        StateInfo javaStateInfo;

        /** State info for the scanning syntax at 'tokenOffset', if tokenOffset != offset (i.e. EOT was returned). */
        StateInfo stateOfScanningAtInit;

        /** Token ID returned by the first call of the scanning slave's nextToken(), possibly EOT. */
        int firstTokenID;

        /** Token length of the token returned by the first call of the scanning slave's nextToken(), possibly invalid. */
        int firstTokenLength;

        public boolean isFirstTokenValid() {
            return (getPreScan() != 0);
        }

        public String toString(Syntax s) {
            //if ((getPreScan() != 0) && (stateOfScanningAtInit != null) && (stateOfScanningAtInit.getPreScan() != 0))
            //new Exception("scanning prescan should be 0").printStackTrace();
            return "JspStateInfo state=" + getState() + ", prescan=" + getPreScan() + ", nestMode=" + nestMode +
                   ((getPreScan() == 0) ? "" : "\n  firstTokenID=" + firstTokenID + ", firstTokenLength=" + firstTokenLength) +
                   "\n  hostStateInfo=" + (hostStateInfo == null ? "null" : ((BaseStateInfo)hostStateInfo).toString(s)) +
                   "\n  jspTagStateInfo=" + (jspTagStateInfo == null ? "null" : ((BaseStateInfo)jspTagStateInfo).toString(s)) +
                   "\n  javaStateInfo=" + (javaStateInfo == null ? "null" : ((BaseStateInfo)javaStateInfo).toString(s)) +
                   "\n  scanning Info=" + (stateOfScanningAtInit == null ? "null" : ((BaseStateInfo)stateOfScanningAtInit).toString(s));
        }
    }

}

/*
 * Log
 *  5    Gandalf-post-FCS1.3.2.0     4/5/00   Petr Jiricka    Token names and examples
 *       from bundles.
 *  4    Gandalf   1.3         3/10/00  Petr Jiricka    Debug stacktraces 
 *       removed. This fixes bug 5955, which is just a debug exception output 
 *       without any further implications.
 *  3    Gandalf   1.2         2/14/00  Petr Jiricka    Fixed copying state 
 *       between variables.
 *  2    Gandalf   1.1         2/11/00  Petr Jiricka    Numerous small fixes.
 *  1    Gandalf   1.0         2/10/00  Petr Jiricka    
 * $
 */

