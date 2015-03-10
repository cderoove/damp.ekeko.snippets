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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.beans.PropertyChangeEvent;
import javax.swing.text.BadLocationException;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SyntaxSupport {

    private static final char[] baseLeftBrackets = new char[] {
                '{', '(', '['
            };

    private static final char[] baseRightBrackets = new char[] {
                '}', ')', ']'
            };

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private static final Acceptor[] EXPRESSION_ACCEPTORS = new Acceptor[] { // !!!
                AcceptorFactory.JAVA_IDENTIFIER, AcceptorFactory.DOT
            };
    private static final int ID = 0; // index of java identifier in acceptor array // !!!
    private static final int DOT = 1; // index of dot in acceptor array // !!!

    private static final int LAST_COUNT = 3;

    /** Document to work with */
    protected BaseDocument doc;

    /** Actual left brackets. Should be reassigned in children's
    * constructor if necessary.
    */
    protected char[] leftBrackets;

    /** Actual right brackets. Should be reassigned in children's
    * constructor if necessary.
    */
    protected char[] rightBrackets;

    protected Acceptor endVarAcceptor;

    protected Acceptor varAcceptor;

    private int[] tokenBlocks = EMPTY_INT_ARRAY;

    private int[][] lastTokenArrays = new int[LAST_COUNT][];

    private boolean[][] lastMatchArrays = new boolean[LAST_COUNT][];

    public SyntaxSupport(BaseDocument doc) {
        this.doc = doc;
        leftBrackets = baseLeftBrackets;
        rightBrackets = baseRightBrackets;
        endVarAcceptor = AcceptorFactory.LETTER_DIGIT;
        varAcceptor = AcceptorFactory.LETTER_DIGIT;
    }

    /** Getter for the document that this support is associated to. */
    public BaseDocument getDocument() {
        return doc;
    }

    public final char[] getLeftBrackets() {
        return leftBrackets;
    }

    public final char[] getRightBrackets() {
        return rightBrackets;
    }

    /** Return the index of the char in leftBrackets array
    * if it's contained there. Otherwise return -1.
    */
    final int getLeftBracketIndex(char ch) {
        for (int i = 0; i < leftBrackets.length; i++) {
            if (ch == leftBrackets[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Return the index of the char in leftBrackets array
    * if it's contained there. Otherwise return -1.
    */
    final int getRightBracketIndex(char ch) {
        for (int i = 0; i < rightBrackets.length; i++) {
            if (ch == rightBrackets[i]) {
                return i;
            }
        }
        return -1;
    }

    public boolean isLeftBracket(char ch) {
        return getLeftBracketIndex(ch) >= 0;
    }

    public boolean isRightBracket(char ch) {
        return getRightBracketIndex(ch) >= 0;
    }

    public boolean isBracket(char ch) {
        return isLeftBracket(ch) || isRightBracket(ch);
    }

    public boolean isWhitespaceToken(int tokenID, int helperID,
                                     char[] buffer, int offset, int tokenLength) {
        return Analyzer.isWhitespace(buffer, offset, tokenLength);
    }

    public boolean isCommentOrWhitespace(int startPos, int endPos)
    throws BadLocationException {
        CommentOrWhitespaceTP tp= new CommentOrWhitespaceTP(getCommentTokens());
        tokenizeText(tp, startPos, endPos, true);
        return !tp.nonEmpty;
    }

    /** Find matching bracket for char on actual position.
    * @param pos position of the starting bracket
    * @param simple whether the search should skip comment and possibly other areas.
    *   This can be useful when the speed is critical, because the simple
    *   search is faster.
    * @return position of matching brace or -1 if no match found
    *   or char on pos is not valid brace
    */

    public int findMatchingBracket(int pos, boolean simpleSearch)
    throws BadLocationException {
        char bracketChar = doc.getChars(pos, 1)[0];
        final BracketFinder bf = new BracketFinder(bracketChar);
        int foundPos = -1;
        if (bf.moveCount != 0) {
            boolean fwd = (bf.moveCount > 0);
            if (!simpleSearch) {
                int tokenID = getTokenID(pos);
                int[] bst = getBracketSkipTokens();
                for (int i = bst.length - 1; i >= 0; i--) {
                    if (tokenID == bst[i]) {
                        simpleSearch = true; // turn to simple search
                        break;
                    }
                }
            }

            if (simpleSearch) { // don't exclude comments etc.
                if (fwd) {
                    foundPos = doc.find(bf, pos, -1);
                } else {
                    foundPos = doc.find(bf, pos + 1, 0);
                }
            } else { // exclude comments etc. from the search
                TextBatchProcessor tbp = new TextBatchProcessor() {
                                             public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                                                         boolean lastBatch) {
                                                 try {
                                                     int[] blks = getTokenBlocks(startPos, endPos, getBracketSkipTokens());
                                                     return findOutsideBlocks(bf, startPos, endPos, blks);
                                                 } catch (BadLocationException e) {
                                                     return -1;
                                                 }
                                             }
                                         };
                if (fwd) {
                    foundPos = doc.processText(tbp, pos, -1);
                } else {
                    foundPos = doc.processText(tbp, pos + 1, 0);
                }
            }
        }

        return foundPos;
    }

    /** Find the first unmatched bracket from the given position.
    * The search direction is determined from the type of the bracket
    * characters provided in the brackets array.
    * @param pos position where the search will start
    * @param bracketChars array of the bracket characters to search for. They
    *   must be all from either the left or right group of the available
    *   brackets. It means that '(', '{' is allowed but '(', '}' not.
    * @return position of the next unmatched bracket.
    */
    public int findUnmatchedBracket(int pos, char[] bracketChars)
    throws BadLocationException {
        final UnmatchedBracketFinder ubf = new UnmatchedBracketFinder(bracketChars);
        int foundPos = -1;
        if (ubf.moveCount != 0) {
            TextBatchProcessor tbp = new TextBatchProcessor() {
                                         public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                                                     boolean lastBatch) {
                                             try {
                                                 int[] blks = getTokenBlocks(startPos, endPos, getBracketSkipTokens());
                                                 return findOutsideBlocks(ubf, startPos, endPos, blks);
                                             } catch (BadLocationException e) {
                                                 return -1;
                                             }
                                         }
                                     };
            if (ubf.moveCount > 0) { // forward
                foundPos = doc.processText(tbp, pos, -1);
            } else {
                foundPos = doc.processText(tbp, pos, 0);
            }
        }
        return foundPos;
    }

    /** Gets the last non-blank and non-comment character on the given line.
    */
    public int getRowLastValidChar(int pos)
    throws BadLocationException {
        return Utilities.getRowLastNonWhite(doc, pos);
    }

    /** Does the line contain some valid code besides of possible white space
    * and comments?
    */
    public boolean isRowValid(int pos)
    throws BadLocationException {
        return Utilities.isRowWhite(doc, pos);
    }

    private boolean[] getMatchArray(int[] tokenArray) {
        boolean[] matchArray = null;
        int ind;
        for (ind = 0; ind < LAST_COUNT; ind++) {
            // Test only on array equality, not Arrays.equals(Ob1[], Ob2[])
            // Supposing they will be static
            if (tokenArray == lastTokenArrays[ind]) {
                matchArray = lastMatchArrays[ind];
                break;
            }
        }

        if (matchArray == null) { // not found in cache
            int maxToken = -1;
            if (tokenArray != null) {
                for (int i = 0; i < tokenArray.length; i++) {
                    if (tokenArray[i] > maxToken) {
                        maxToken = tokenArray[i];
                    }
                }
            }

            matchArray = new boolean[maxToken + 1];
            for (int i = 0; i < tokenArray.length; i++) {
                matchArray[tokenArray[i]] = true;
            }
        }

        if (ind > 0) {
            ind = Math.min(ind, LAST_COUNT - 1);
            System.arraycopy(lastTokenArrays, 0, lastTokenArrays, 1, ind);
            System.arraycopy(lastMatchArrays, 0, lastMatchArrays, 1, ind);
            lastTokenArrays[0] = tokenArray;
            lastMatchArrays[0] = matchArray;
        }

        return matchArray;
    }

    /** Get position pairs covering the blocks that include only the tokens
    * from the given token array. Although the startPos can be greater than
    * endPos, the blocks are always returned in the natural order.
    * @param doc document to work with
    * @param startPos starting position of the requested document area.
    *  It can be -1 to indicate the end of document.
    * @param endPos ending position of the requested document area
    *  It can be -1 to indicate the end of document.
    * @tokenArray the array of the token IDs that should be added to the blocks.
    */
    public synchronized int[] getTokenBlocks(int startPos, int endPos,
            int[] tokenArray) throws BadLocationException {
        doc.readLock();
        try {
            boolean matchArray[] = getMatchArray(tokenArray);
            int blkInd = 0;
            int docLen = doc.getLength();
            if (endPos == -1) {
                endPos = docLen;
            }
            if (startPos == -1) {
                startPos = docLen;
            }
            if (startPos > endPos) { // swap
                int tmp = startPos;
                startPos = endPos;
                endPos = tmp;
            }

            SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
            Syntax syntax = doc.getFreeSyntax();
            try {
                int preScan = doc.op.prepareSyntax(slot, syntax,
                                                   doc.op.getLeftSyntaxMark(startPos), startPos, endPos - startPos);
                syntax.setLastBuffer(true); // to process all chars till the end

                int pos = startPos - preScan;
                int blkStart = -1;

                boolean cont = true;
                while (cont) {
                    int tokenID = syntax.nextToken();
                    switch (tokenID) {
                    case Syntax.EOT: // end of scanning
                        cont = false;
                        break;
                    case Syntax.EOL:
                        pos++;
                        break;
                    default:
                        if (tokenID < matchArray.length && matchArray[tokenID]) {
                            if (blkStart >= 0) {
                                // still in token block
                            } else {
                                blkStart = Math.max(pos, startPos);
                            }
                        } else { // not searched token
                            if (blkStart >= 0) {
                                tokenBlocks = addTokenBlock(tokenBlocks, blkInd, blkStart, pos);
                                blkInd += 2;
                                blkStart = -1;
                            } else {
                                // not in comment
                            }
                        }
                        pos += syntax.getTokenLength();
                        break;
                    }
                }

                if (blkStart >= 0) { // was in comment
                    tokenBlocks = addTokenBlock(tokenBlocks, blkInd, blkStart, endPos);
                    blkInd += 2;
                }

            } finally {
                doc.releaseSyntax(syntax);
                SyntaxSeg.releaseSlot(slot);
            }

            int[] ret = new int[blkInd];
            System.arraycopy(tokenBlocks, 0, ret, 0, blkInd);
            return ret;
        } finally {
            doc.readUnlock();
        }
    }

    private int[] addTokenBlock(int[] blks, int blkInd, int blkStartPos, int blkEndPos) {
        if (blks.length < blkInd + 2) {
            int[] tmp = new int[Math.max(2, blks.length * 2)];
            System.arraycopy(blks, 0, tmp, 0, blkInd);
            blks = tmp;
        }

        blks[blkInd++] = blkStartPos;
        blks[blkInd] = blkEndPos;
        return blks;
    }

    /** Get the array of token IDs that denote the comments.
    * Returns empty array by default.
    */
    public int[] getCommentTokens() {
        return EMPTY_INT_ARRAY;
    }

    /** Get the blocks consisting of comments in a specified document area.
    * @param doc document to work with
    * @param startPos starting position of the searched document area
    * @param endPos ending position of the searched document area
    */
    public int[] getCommentBlocks(int startPos, int endPos)
    throws BadLocationException {
        return getTokenBlocks(startPos, endPos, getCommentTokens());
    }

    /** Get the array of token IDs that should be skipped when
    * searching for matching bracket. It usually includes comments
    * and character and string constants. Returns empty array by default.
    */
    public int[] getBracketSkipTokens() {
        return EMPTY_INT_ARRAY;
    }

    public int findInsideBlocks(Finder finder,
                                int startPos, int endPos, int[] blocks) throws BadLocationException {
        int docLen = doc.getLength();
        if (startPos == -1) {
            startPos = docLen;
        }
        if (endPos == -1) {
            endPos = docLen;
        }
        boolean fwd = (startPos <= endPos);

        if (fwd) {
            for (int i = 0; i < blocks.length; i += 2) {
                int pos = doc.find(finder, blocks[i], blocks[i + 1]);
                if (pos >= 0) {
                    return pos;
                }
            }
        } else { // find backward
            for (int i = blocks.length - 2; i >= 0; i -= 2) {
                int pos = doc.find(finder, blocks[i + 1], blocks[i]);
                if (pos >= 0) {
                    return pos;
                }
            }
        }
        return -1;
    }

    public int findOutsideBlocks(Finder finder,
                                 int startPos, int endPos, int[] blocks) throws BadLocationException {
        int docLen = doc.getLength();
        if (startPos == -1) {
            startPos = docLen;
        }
        if (endPos == -1) {
            endPos = docLen;
        }
        boolean fwd = (startPos <= endPos);

        if (fwd) {
            int pos = doc.find(finder, startPos, (blocks.length > 0) ? blocks[0] : endPos);
            if (pos >= 0) {
                return pos;
            }

            int ind = 2;
            while (ind <= blocks.length) {
                pos = doc.find(finder, blocks[ind - 1], (ind >= blocks.length) ? endPos : blocks[ind]);
                if (pos >= 0) {
                    return pos;
                }
                ind += 2;
            }
        } else { // find backward
            int pos = doc.find(finder, startPos, (blocks.length > 0) ? blocks[blocks.length - 1] : endPos);
            if (pos >= 0) {
                return pos;
            }

            int ind = blocks.length - 2;
            while (ind >= 0) {
                pos = doc.find(finder, blocks[ind], (ind == 0) ? endPos : blocks[ind - 1]);
                if (pos >= 0) {
                    return pos;
                }
                ind -= 2;
            }
        }
        return -1;
    }

    public void initSyntax(Syntax syntax, int startPos, int endPos)
    throws BadLocationException {
        SyntaxSeg.Slot slot = null;
        doc.readLock();
        try {
            slot = SyntaxSeg.getFreeSlot();
            int docLen = doc.getLength();
            if (endPos == -1) {
                endPos = docLen;
            }
            doc.op.prepareSyntax(slot, syntax, doc.op.getLeftSyntaxMark(startPos),
                                 startPos, 0);
            int preScan = syntax.getPreScan();
            char[] buffer = doc.getChars(startPos - preScan, endPos - startPos + preScan);
            syntax.relocate(buffer, preScan, endPos - startPos, (endPos == docLen));
        } finally {
            if (slot != null) {
                SyntaxSeg.releaseSlot(slot);
            }
            doc.readUnlock();
        }
    }

    /** Parse the text and pass the resulting tokens to the token processor.
    * @param tp token processor that will be informed about the found tokens.
    * @param startPos starting position in the text
    * @param endPos ending position in the text
    * @param forceLastBuffer force the syntax scanner to think that the requested
    *   area is the last in the document.
    */
    public void tokenizeText(TokenProcessor tp, int startPos, int endPos, boolean forceLastBuffer)
    throws BadLocationException {
        SyntaxSeg.Slot slot = null;
        Syntax syntax = null;
        doc.readLock();
        try {
            slot = SyntaxSeg.getFreeSlot();
            syntax = doc.getFreeSyntax();
            int docLen = doc.getLength();
            if (startPos == -1) {
                startPos = docLen;
            }
            if (endPos == -1) {
                endPos = docLen;
            }
            int preScan = doc.op.prepareSyntax(slot, syntax, doc.op.getLeftSyntaxMark(startPos),
                                               startPos, endPos - startPos);
            boolean lastBuffer = forceLastBuffer || (endPos == docLen);
            syntax.setLastBuffer(lastBuffer);
            tp.nextBuffer(slot.array, syntax.getOffset(), endPos - startPos,
                          startPos, preScan, lastBuffer);

            boolean cont = true;
            while (cont) {
                int tokenID = syntax.nextToken();
                switch (tokenID) {
                case Syntax.EOT: // end of scanning
                    int nextLen = tp.eot(syntax.tokenOffset);
                    if (nextLen == 0) {
                        cont = false;
                    } else { // continue
                        preScan = syntax.getPreScan();
                        slot.load(doc, endPos - preScan, endPos + nextLen);
                        lastBuffer = forceLastBuffer || (endPos + nextLen == docLen);
                        syntax.relocate(slot.array, slot.offset + preScan, nextLen, lastBuffer);
                        tp.nextBuffer(slot.array, syntax.getOffset(), nextLen,
                                      endPos, preScan, lastBuffer);
                        endPos += nextLen;
                    }
                    break;
                default:
                    if (!tp.token(tokenID, syntax.helperID, syntax.getTokenOffset(),
                                  syntax.getTokenLength())
                       ) {
                        cont = false;
                    }
                    break;
                }
            }
        } finally {
            if (syntax != null) {
                doc.releaseSyntax(syntax);
            }
            if (slot != null) {
                SyntaxSeg.releaseSlot(slot);
            }
            doc.readUnlock();
        }
    }

    /** Gets the token-id of the token at the given position.
    * @param pos position at which the token should be returned
    * @return token-id of the token at the requested position. If there's no more
    *   tokens in the text, the <tt>Syntax.INVALID</tt> is returned.
    */
    public int getTokenID(int pos) throws BadLocationException {
        TokenIDTP titp = new TokenIDTP();
        tokenizeText(titp, pos, -1, true);
        return titp.getTokenID();
    }

    public String getTokenName(int tokenID) {
        Syntax syntax = doc.getFreeSyntax();
        String ret = syntax.getTokenName(tokenID);
        doc.releaseSyntax(syntax);
        return ret;
    }

    public boolean isIdentifier(String word) {
        if (word == null || word.length() == 0) {
            return false; // not qualified as word
        }

        for (int i = 0; i < word.length(); i++) {
            if (!doc.isIdentifierPart(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Is the identifier at the position a function call?
    * It first checks whether there is a identifier under
    * the cursor and then it searches for the function call
    * character - usually '('.
    * @param identifierBlock int[2] block delimiting the identifier
    * @return int[2] block or null if there's no function call
    */
    public int[] getFunctionBlock(int[] identifierBlock) throws BadLocationException {
        if (identifierBlock != null) {
            int nwPos = Utilities.getFirstNonWhiteFwd(doc, identifierBlock[1]);
            if ((nwPos >= 0) && (doc.getChars(nwPos, 1)[0] == '(')) {
                return new int[] { identifierBlock[0], nwPos + 1 };
            }
        }
        return null;
    }

    public int[] getFunctionBlock(int pos) throws BadLocationException {
        return getFunctionBlock(Utilities.getIdentifierBlock(doc, pos));
    }

    /** Finder for the matching bracket. It gets the original bracket char
    * and searches for the appropriate matching bracket character.
    */
    class BracketFinder extends FinderFactory.GenericFinder {

        /** Original bracket char */
        private char bracketChar;

        /** Matching bracket char */
        private char matchChar;

        /** Depth of original brackets */
        private int depth;

        /** Will it be a forward finder +1 or backward finder -1 or 0 when
        * the given character is not bracket character.
        */
        int moveCount;

        /**
        * @param bracketChar bracket char
        */
        BracketFinder(char bracketChar) {
            this.bracketChar = bracketChar;

            int ind = getRightBracketIndex(bracketChar);
            if (ind >= 0) { // right bracket
                matchChar = leftBrackets[ind];
                moveCount = -1;
            } else { // not right bracket
                ind = getLeftBracketIndex(bracketChar);
                if (ind >= 0) { // left bracket
                    matchChar = rightBrackets[ind];
                    moveCount = +1;
                }
            }

            forward = (moveCount > 0);
        }

        protected int scan(char ch, boolean lastChar) {
            if (ch == bracketChar) {
                depth++;
            } else if (ch == matchChar) {
                if (--depth == 0) {
                    found = true;
                    return 0;
                }
            }
            return moveCount;
        }

    }

    /** Finder for the unmatched opening/closing bracket. It gets the array
    * of the opening/closing brackets to search for. It finds
    * the first occurence of the unmatched bracket from
    * the given array.
    * Example: If the array contains the '}' and ')' and '{}()}()'
    * then the position of the fifth bracket will be returned
    * because it's the first unmatched bracket.
    *  
    */
    class UnmatchedBracketFinder extends FinderFactory.GenericFinder {

        private char[] bracketChars;

        private char[] matchChars;

        private int[] depths;

        int moveCount;

        UnmatchedBracketFinder(char[] bracketChars) {
            this.bracketChars = bracketChars;
            for (int i = 0; i < bracketChars.length; i++) {
                int ind = getRightBracketIndex(bracketChars[i]);
                if (ind >= 0) { // right bracket
                    matchChars[i] = leftBrackets[ind];
                    if (moveCount > 0) { // check for previous brackets of opposite type
                        moveCount = 0;
                        break;
                    }
                    moveCount = -1;
                } else { // not right bracket
                    ind = getLeftBracketIndex(bracketChars[i]);
                    if (ind >= 0) { // left bracket
                        matchChars[i] = rightBrackets[ind];
                        if (moveCount < 0) { // check for previous brackets of opposite type
                            moveCount = 0;
                            break;
                        }
                        moveCount = +1;
                    }
                }
            }

            forward = (moveCount > 0);
        }

        protected int scan(char ch, boolean lastChar) {
            for (int i = 0; i < bracketChars.length; i++) {
                if (bracketChars[i] == ch) {
                    if (depths[i]-- == 0) {
                        found = true;
                        return 0;
                    }
                    return moveCount;
                }
            }

            for (int i = 0; i < matchChars.length; i++) {
                if (matchChars[i] == ch) {
                    depths[i]++;
                    break;
                }
            }

            return moveCount;
        }

    }

    /** Token processor that matches either the comments or whitespace */
    class CommentOrWhitespaceTP implements TokenProcessor {

        private char[] buffer;

        private int[] commentTokens;

        boolean nonEmpty;

        CommentOrWhitespaceTP(int[] commentTokens) {
            this.commentTokens = commentTokens;
        }

        public boolean token(int tokenID, int helperID, int offset, int tokenLength) {
            for (int i = 0; i < commentTokens.length; i++) {
                if (tokenID == commentTokens[i]) {
                    return true; // comment token found
                }
            }
            boolean nonWS = isWhitespaceToken(tokenID, helperID, buffer, offset, tokenLength);
            if (nonWS) {
                nonEmpty = true;
            }
            return nonWS;
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
        }

    }

    class TokenIDTP implements TokenProcessor {

        private int tokenID = Syntax.INVALID;

        public int getTokenID() {
            return tokenID;
        }

        public boolean token(int tokenID, int helperID, int offset, int tokenLen) {
            this.tokenID = tokenID;
            return false; // no more tokens
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
        }

    }

}

/*
 * Log
 *  19   Gandalf-post-FCS1.17.1.0    3/8/00   Miloslav Metelka 
 *  18   Gandalf   1.17        1/10/00  Miloslav Metelka 
 *  17   Gandalf   1.16        1/4/00   Miloslav Metelka 
 *  16   Gandalf   1.15        12/28/99 Miloslav Metelka 
 *  15   Gandalf   1.14        11/8/99  Miloslav Metelka 
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/10/99 Miloslav Metelka 
 *  12   Gandalf   1.11        9/30/99  Miloslav Metelka 
 *  11   Gandalf   1.10        9/16/99  Miloslav Metelka 
 *  10   Gandalf   1.9         9/15/99  Miloslav Metelka 
 *  9    Gandalf   1.8         9/10/99  Miloslav Metelka 
 *  8    Gandalf   1.7         8/27/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/17/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/30/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
 *  4    Gandalf   1.3         6/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         6/8/99   Miloslav Metelka 
 *  2    Gandalf   1.1         6/1/99   Miloslav Metelka 
 *  1    Gandalf   1.0         5/24/99  Miloslav Metelka 
 * $
 */

