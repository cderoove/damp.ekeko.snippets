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

package org.netbeans.editor;

/**
* Lexical analyzer that works on a given text buffer. It allows
* to sequentially parse a given character buffer by calling
* <tt>nextToken()</tt> that returns the token-ids. Token-ids are
* the integer constants greater or equal to zero. They are usually
* sequential but generally they don't have to. The highest token-id
* must be assigned to the <tt>highestTokenID</tt> variable
* in the constructor of the given analyzer. The token-id numbers can be
* translated to the meaningful names by calling <tt>getTokenName()</tt>.
*
* After the token is found by calling the <tt>nextToken</tt> method,
* the <tt>getTokenOffset()</tt> method can be used
* to get the starting offset of the current
* token in the buffer. The <tt>getTokenLength()</tt> gives the length
* of the current token.
*
* The heart of the analyzer is the <tt>parseToken()</tt> method which
* parses the text and returns the token-id of the last token found.
* The <tt>parseToken()</tt> method is called from the <tt>nextToken()</tt>.
* It operates with two important variables. The <tt>offset</tt>
* variable identifies the currently scanned character in the buffer.
* The <tt>tokenOffset</tt> is the begining of the current token.
* The <tt>state</tt> variable that identifies the current internal
* state of the analyzer is set accordingly when the characters are parsed.
* If the <tt>parseToken()</tt> recognizes a token, it returns its ID
* and the <tt>tokenOffset</tt> is its begining in the buffer and
* <tt>offset - tokenOffset</tt> is its length. When the token is processed
* the value of <tt>tokenOffset</tt> is set to be the same as current
* value of the <tt>offset</tt> and the parsing continues.
*
* Internal states are the integer constants used internally by analyzer.
* They are assigned to the <tt>state</tt> variable to express
* that the analyzer has moved from one state to another.
* They are usually numbered starting from zero but they don't
* have to. The only reserved value is -1 which is reserved
* for the INIT state - the initial internal state of the analyzer.
*
* There is also the support for defining the persistent info about
* the current state of the analyzer. This info can be later used
* to restore the parsing from some particular state instead of
* parsing from the begining of the buffer. This feature is very
* useful if there are the modifications performed in the document.
* The info is stored in the <tt>StateInfo</tt> interface
* with the <tt>BaseStateInfo</tt> as the basic implementation.
* It enables to get and set the two important values
* from the persistent point of view.
* The first one is the value of the <tt>state</tt> variable.
* The other one is the difference <tt>offset - tokenOffset</tt>
* which is called pre-scan. The particular analyzer can define
* additional values important for the persistent storage.
* The <tt>createStateInfo()</tt> can be overriden to create
* custom state-info and <tt>loadState()</tt> and <tt>storeState()</tt>
* can be overriden to get/set the additional values.
*
* The <tt>load()</tt> method sets the buffer to be parsed.
*
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Syntax {

    /** Is the state of analyzer equal to a given state info? */
    public static final int EQUAL_STATE = 0;

    /** Is the state of analyzer different from given state info? */
    public static final int DIFFERENT_STATE = 1;


    /** Initial internal state of the analyzer */
    public static final int INIT = -1;


    /** Special token ID signaling invalid token. */
    public static final int INVALID = -3;

    /** Special token ID signaling that the end of the text buffer was reached. */
    public static final int EOT = -2;

    /** Special token ID signaling that the end of line was found. */
    public static final int EOL = -1;


    /** Token name describing invalid token ID */
    public static final String TN_INVALID = "INVALID"; // NOI18N

    /** Token name describing EOL */
    public static final String TN_EOL = "EOL"; // NOI18N

    /** Token name describing EOT */
    public static final String TN_EOT = "EOT"; // NOI18N


    // Some most common token names follow.
    /** Token name describing plain text */
    public static final String TN_TEXT = "text"; // NOI18N

    /** Token name describing errorneous text */
    public static final String TN_ERROR = "error"; // NOI18N

    /** Token name describing a keyword */
    public static final String TN_KEYWORD = "keyword"; // NOI18N

    /** Token name describing an identifier */
    public static final String TN_IDENTIFIER = "identifier"; // NOI18N

    /** Token name describing a function call */
    public static final String TN_FUNCTION = "function"; // NOI18N

    /** Token name describing an identifier */
    public static final String TN_OPERATOR = "operator"; // NOI18N

    /** Token name describing line comment */
    public static final String TN_LINE_COMMENT = "line-comment"; // NOI18N

    /** Token name describing block comment */
    public static final String TN_BLOCK_COMMENT = "block-comment"; // NOI18N

    /** Token name describing character constant */
    public static final String TN_CHAR = "char"; // NOI18N

    /** Token name describing string constant */
    public static final String TN_STRING = "string"; // NOI18N

    /** Token name describing integer constant */
    public static final String TN_INT = "int"; // NOI18N

    /** Token name describing hexadecimal constant */
    public static final String TN_HEX = "hex"; // NOI18N

    /** Token name describing octal constant */
    public static final String TN_OCTAL = "octal"; // NOI18N

    /** Token name describing long constant */
    public static final String TN_LONG = "long"; // NOI18N

    /** Token name describing float constant */
    public static final String TN_FLOAT = "float"; // NOI18N

    /** Token name describing double constant */
    public static final String TN_DOUBLE = "double"; // NOI18N



    /** Internal state of the lexical analyzer. At the begining
    * it's set to INIT value but it is changed by <tt>parseToken()</tt>
    * as the characters are processed one by one.
    */
    protected int state = INIT;

    /** Text buffer to scan */
    protected char buffer[];

    /** Current offset in the buffer */
    protected int offset;

    /** Offset holding the begining of the current token */
    protected int tokenOffset;

    /** Holds the additional information about the token parsed.
    * It can hold the concreate type of the keyword or operator
    * for example. The filling 
    * and each token ID.
    */
    protected int helperID;

    /** This field is reserved for the future use. */
    protected boolean lightError;

    /** This variable is the length of the token that was found */
    protected int tokenLength;

    /** Setting this flag to true means that there will be no more
    * buffers available so that analyzer should return all the tokens
    * including those whose successful scanning would be otherwise
    * left for later when the next buffer will be available.
    */
    protected boolean lastBuffer;

    /** On which offset in the buffer scanning should stop. */
    protected int stopOffset;

    /** The variable identifying the highest token ID used
    * by the syntax or -1 if the syntax defines no tokens.
    * This variable is used by <tt>getHighestTokenID()</tt>
    * and should be assigned in the constructor.
    */
    protected int highestTokenID = -1; // no tokens defined here


    /** Function that should be called externally to scan the text.
    * It manages the call to parseToken() and cares about the proper
    * setting of the offsets.
    * It can be extended to support any custom debugging required.
    */
    public int nextToken() {
        // Return immediately when at the end of buffer
        if (offset >= stopOffset) {
            tokenLength = 0;
            return EOT;
        }

        // Divide non-debug and debug sections
        int tokenID = parseToken();
        if (tokenID >= EOL) { // regular token found
            tokenLength = offset - tokenOffset;
            tokenOffset = offset;
            if (tokenLength == 0) { // test for empty token
                return nextToken(); // repeat until non-empty token is found
            }
        } else { // EOT returned
            tokenLength = 0;
        }
        return tokenID;
    }

    /** This is core function of analyzer and it returns one of following numbers:
    * a) token number of next token from scanned text
    * b) EOL when end of line was found in scanned buffer
    * c) EOT when there is no more chars available in scanned buffer.
    *
    * The function scans the active character and does one or more
    * of the following actions:
    * 1. change internal analyzer state (state = new-state)
    * 2. return token ID (return token-ID)
    * 3. adjust current position to signal different end of token;
    *    the character that offset points to is not included in the token
    */
    protected int parseToken() {
        return EOT;
    }

    /** Load the state from syntax mark into analyzer. This method is used when
    * @param chain chain of the mark states. It can be null
    * @param buffer buffer that will be scanned
    * @param offset offset of the first character that will be scanned
    * @param len length of the area to be scanned
    * @param lastBuffer whether this is the last buffer in the document. All the tokens
    *   will be returned including the last possibly incomplete one.
    */
    public void load(StateInfo stateInfo, char buffer[], int offset, int len, boolean lastBuffer) {
        this.buffer = buffer;
        this.offset = offset;
        this.tokenOffset = offset;
        this.stopOffset = offset + len;
        this.lastBuffer = lastBuffer;

        if (stateInfo != null) {
            loadState(stateInfo);
        } else {
            loadInitState();
        }
    }

    /** Relocate scanning to another buffer.
    * This is used to continue scanning after previously
    * reported EOT. Relocation delta between current offset and the requested offset
    * is computed and all the offsets are relocated. If there's a non-zero preScan
    * in the analyzer, it is a caller's responsibility to provide all the preScan
    * characters in the relocation buffer.
    * @param buffer next buffer where the scan will continue.
    * @param offset offset where the scan will continue.
    *   It's not decremented by the current preScan.
    * @param len length of the area to be scanned.
    *   It's not extended by the current preScan.
    * @param lastBuffer whether this is the last buffer in the document. All the tokens
    *   will be returned including the last possibly incomplete one.
    */
    public void relocate(char buffer[], int offset, int len, boolean lastBuffer) {
        this.buffer = buffer;
        this.lastBuffer = lastBuffer;

        int delta = offset - this.offset; // delta according to current offset
        this.offset += delta;
        this.tokenOffset += delta;
        this.stopOffset = offset + len;
    }

    /** Set if this buffer is the last one. */
    public void setLastBuffer(boolean lastBuffer) {
        this.lastBuffer = lastBuffer;
    }

    /** Set the offset in buffer where scnning should stop.
    * It forces the analyzer to stop explicitly at some
    * offset in the buffer. It's used for example when the document
    * is read initially.
    */
    public void setStopOffset(int stopOffset) {
        this.stopOffset = stopOffset;
    }

    /** Get the current buffer */
    public final char[] getBuffer() {
        return buffer;
    }

    /** Get the current scanning offset */
    public final int getOffset() {
        return offset;
    }

    /** Get start of token in scanned buffer. */
    public final int getTokenOffset() {
        return offset - tokenLength;
    }

    /** Get length of token in scanned buffer. */
    public final int getTokenLength() {
        return tokenLength;
    }

    /** Return the token ID in respect to specific syntax class.
    * This method becomes handy when the syntax is composed
    * from several other syntaxes.
    */
    public int translateTokenID(int tokenID, Class syntaxClass) {
        if (syntaxClass == null || this.getClass() == syntaxClass) {
            return tokenID;
        } else {
            return INVALID;
        }
    }

    /** Get the highest token ID. This method can be redefined although
    * usually it's enough to assign the <tt>highestTokenID</tt> variable
    * in the syntax constructor.
    */
    public int getHighestTokenID() {
        return highestTokenID;
    }

    /** Returns the token helper ID that if filled by the analyzer
    * holds the additional information about the token parsed.
    */
    public final int getHelperID() {
        return helperID;
    }

    /** Get the pre-scan which is a number
    * of characters between offset and tokenOffset.
    * If there's no more characters in the current buffer,
    * the analyzer returns EOT, but it can be in a state when
    * there are already some characters parsed at the end of
    * the current buffer but the token
    * is still incomplete and it cannot be returned yet.
    * The pre-scan value helps to determine how many characters
    * from the end of the current buffer should be present
    * at the begining of the next buffer so that the current
    * incomplete token can be returned as the first token
    * when parsing the next buffer.
    */
    public int getPreScan() {
        return offset - tokenOffset;
    }

    /** Initialize the analyzer when scanning from the begining
    * of the document or when the state stored in syntax mark
    * is null for some reason or to explicitly reset the analyzer
    * to the initial state. The offsets must not be touched by this method.
    */
    public void loadInitState() {
        state = INIT;
    }

    public void reset() {
        tokenLength = stopOffset = tokenOffset = offset = 0;
        loadInitState();
    }

    /** Load valid mark state into the analyzer. Offsets
    * are already initialized when this method is called. This method
    * must get the state from the mark and set it to the analyzer. Then
    * it must decrease tokenOffset by the preScan stored in the mark state.
    * @param markState mark state to be loaded into syntax. It must be non-null value.
    */
    public void loadState(StateInfo stateInfo) {
        state = stateInfo.getState();
        tokenOffset -= stateInfo.getPreScan();
    }

    /** Store state of this analyzer into given mark state. */
    public void storeState(StateInfo stateInfo) {
        stateInfo.setState(state);
        stateInfo.setPreScan(getPreScan());
    }

    /** Compare state of this analyzer to given state info */
    public int compareState(StateInfo stateInfo) {
        if (stateInfo != null) {
            return ((stateInfo.getState() == state) && stateInfo.getPreScan() == getPreScan())
                   ? EQUAL_STATE : DIFFERENT_STATE;
        } else {
            return DIFFERENT_STATE;
        }
    }

    /** Create state info appropriate for particular analyzer */
    public StateInfo createStateInfo() {
        return new BaseStateInfo();
    }

    /** Get the name of the token by knowing the tokenID. This method
    * is used for finding the proper coloring and for the debugging purposes too.
    */
    public String getTokenName(int tokenID) {
        // test special token IDs
        switch (tokenID) {
        case EOL:
            return TN_EOL;
        case EOT:
            return TN_EOT;
        case INVALID:
            return TN_INVALID;
        default: // token ID not recognized
            return "Unknown token ID " + tokenID; // NOI18N
        }
    }

    /** Get state name as string. It can be used for debugging purposes
    * by developer of new syntax analyzer. The states that this function
    * recognizes can include all constants used in analyzer so that it can
    * be used everywhere in analyzer to convert numbers to more practical strings.
    */
    public String getStateName(int stateNumber) {
        switch(stateNumber) {
        case INIT:
            return "INIT"; // NOI18N

        default:
            return "Unknown state " + stateNumber; // NOI18N
        }
    }

    /** Syntax information as String */
    public String toString() {
        return "tokenOffset=" + tokenOffset // NOI18N
               + ", offset=" + offset // NOI18N
               + ", state=" + getStateName(state) // NOI18N
               + ", stopOffset=" + stopOffset // NOI18N
               + ", lastBuffer=" + lastBuffer; // NOI18N
    }


    /** Interface that stores two basic pieces of information about
    * the state of the whole lexical analyzer - its internal state and preScan.
    */
    public interface StateInfo {

        /** Get the internal state */
        public int getState();

        /** Store the internal state */
        public void setState(int state);

        /** Get the preScan value */
        public int getPreScan();

        /** Store the preScan value */
        public void setPreScan(int preScan);

    }


    /** Base implementation of the StateInfo interface */
    public static class BaseStateInfo implements StateInfo {

        /** analyzer state */
        private int state;

        /** Pre-scan length */
        private int preScan;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getPreScan() {
            return preScan;
        }

        public void setPreScan(int preScan) {
            this.preScan = preScan;
        }

        public String toString(Syntax syntax) {
            return "state=" + syntax.getStateName(getState()) + ", preScan=" + getPreScan(); // NOI18N
        }

    }

}

/*
 * Log
 *  30   Gandalf   1.29        1/13/00  Miloslav Metelka 
 *  29   Gandalf   1.28        1/7/00   Miloslav Metelka 
 *  28   Gandalf   1.27        1/6/00   Miloslav Metelka 
 *  27   Gandalf   1.26        1/4/00   Miloslav Metelka 
 *  26   Gandalf   1.25        12/28/99 Miloslav Metelka 
 *  25   Gandalf   1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        9/16/99  Miloslav Metelka 
 *  23   Gandalf   1.22        9/15/99  Miloslav Metelka 
 *  22   Gandalf   1.21        9/10/99  Miloslav Metelka 
 *  21   Gandalf   1.20        8/27/99  Miloslav Metelka 
 *  20   Gandalf   1.19        8/17/99  Miloslav Metelka 
 *  19   Gandalf   1.18        7/26/99  Miloslav Metelka 
 *  18   Gandalf   1.17        7/20/99  Miloslav Metelka 
 *  17   Gandalf   1.16        7/2/99   Miloslav Metelka 
 *  16   Gandalf   1.15        6/22/99  Miloslav Metelka 
 *  15   Gandalf   1.14        6/8/99   Miloslav Metelka 
 *  14   Gandalf   1.13        6/1/99   Miloslav Metelka 
 *  13   Gandalf   1.12        5/24/99  Miloslav Metelka 
 *  12   Gandalf   1.11        5/21/99  Miloslav Metelka endInd removed; fix
 *  11   Gandalf   1.10        5/15/99  Miloslav Metelka fixes
 *  10   Gandalf   1.9         5/13/99  Miloslav Metelka 
 *  9    Gandalf   1.8         5/5/99   Miloslav Metelka 
 *  8    Gandalf   1.7         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  7    Gandalf   1.6         3/30/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/27/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/23/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         2/9/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

