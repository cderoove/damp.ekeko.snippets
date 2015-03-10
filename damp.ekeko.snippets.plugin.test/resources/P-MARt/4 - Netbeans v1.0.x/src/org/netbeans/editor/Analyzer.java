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

import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;

/**
* Various text analyzes over the document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Analyzer {

    /** Platform default line separator */
    private static Object platformLS;

    /** Empty char array */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /** Buffer filled by spaces used for spaces filling and tabs expansion */
    private static char spacesBuffer[] = new char[] { ' ' };

    /** Buffer filled by tabs used for tabs filling */
    private static char tabsBuffer[] = new char[] { '\t' };

    private Analyzer() {
        // no instantiation
    }

    /** Get platform default line separator */
    public static Object getPlatformLS() {
        if (platformLS == null) {
            platformLS = System.getProperty("line.separator"); // NOI18N
        }
        return platformLS;
    }

    /** Test line separator on given semgment. This implementation simply checks
    * the first line of file but it can be redefined to do more thorough test.
    * @param seg segment where analyzes are performed
    * @return line separator type found in the file
    */
    public static String testLS(char chars[], int len) {
        for (int i = 0; i < len; i++) {
            switch (chars[i]) {
            case '\r':
                if (i + 1 < len && chars[i + 1] == '\n') {
                    return BaseDocument.LS_CRLF;
                } else {
                    return BaseDocument.LS_CR;
                }

            case '\n':
                return BaseDocument.LS_LF;
            }
        }
        return null; // signal unspecified line separator
    }

    /** Convert text with generic line separators to line feeds (LF).
    * As the linefeeds are one char long there is no need to allocate
    * another buffer since the only possibility is that the returned
    * length will be smaller than previous (if there were some CRLF separators.
    * @param chars char array with data to convert
    * @param len valid portion of chars array
    * @return new valid portion of chars array after conversion
    */   
    public static int convertLSToLF(char chars[], int len) {
        int tgtOffset = 0;
        short lsLen = 0; // length of separator found
        int moveStart = 0; // start of block that must be moved
        int moveLen; // length of data moved back in buffer

        for (int i = 0; i < len; i++) {
            // first of all - there's no need to handle single '\n'
            if (chars[i] == '\r') { // '\r' found
                if (i + 1 < len && chars[i + 1] == '\n') { // '\n' follows
                    lsLen = 2; // '\r\n'
                } else {
                    lsLen = 1; // only '\r'
                }
            }

            if (lsLen > 0) {
                moveLen = i - moveStart;
                if (moveLen > 0) {
                    if (tgtOffset != moveStart) { // will need to arraycopy
                        System.arraycopy(chars, moveStart, chars, tgtOffset, moveLen);
                    }
                    tgtOffset += moveLen;
                }
                chars[tgtOffset++] = '\n';
                moveStart += moveLen + lsLen; // skip separator
                i += lsLen - 1; // possibly skip '\n'
                lsLen = 0; // signal no separator found
            }
        }

        // now move the rest if it's necessary
        moveLen = len - moveStart;
        if (moveLen > 0) {
            if (tgtOffset != moveStart) {
                System.arraycopy(chars, moveStart, chars, tgtOffset, moveLen);
            }
            tgtOffset += moveLen;
        }

        return tgtOffset; // return current length
    }

    /** Convert string with generic line separators to line feeds (LF).
    * @param text string to convert
    * @return new string with converted LSs to LFs
    */   
    public static String convertLSToLF(String text) {
        char[] tgtChars = null;
        int tgtOffset = 0;
        short lsLen = 0; // length of separator found
        int moveStart = 0; // start of block that must be moved
        int moveLen; // length of data moved back in buffer
        int textLen = text.length();

        for (int i = 0; i < textLen; i++) {
            // first of all - there's no need to handle single '\n'
            if (text.charAt(i) == '\r') { // '\r' found
                if (i + 1 < textLen && text.charAt(i + 1) == '\n') { // '\n' follows
                    lsLen = 2; // '\r\n'
                } else {
                    lsLen = 1; // only '\r'
                }
            }

            if (lsLen > 0) {
                if (tgtChars == null) {
                    tgtChars = new char[textLen];
                    text.getChars(0, textLen, tgtChars, 0); // copy whole array
                }
                moveLen = i - moveStart;
                if (moveLen > 0) {
                    if (tgtOffset != moveStart) { // will need to arraycopy
                        text.getChars(moveStart, moveStart + moveLen, tgtChars, tgtOffset);
                    }
                    tgtOffset += moveLen;
                }
                tgtChars[tgtOffset++] = '\n';
                moveStart += moveLen + lsLen; // skip separator
                i += lsLen - 1; // possibly skip '\n'
                lsLen = 0; // signal no separator found
            }
        }

        // now move the rest if it's necessary
        moveLen = textLen - moveStart;
        if (moveLen > 0) {
            if (tgtOffset != moveStart) {
                text.getChars(moveStart, moveStart + moveLen, tgtChars, tgtOffset);
            }
            tgtOffset += moveLen;
        }

        return (tgtChars == null) ? text : new String(tgtChars, 0, tgtOffset);
    }

    public static boolean isSpace(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    public static boolean isSpace(char[] chars) {
        return isSpace(chars, 0, chars.length);
    }

    /** Return true if the array contains only space chars */
    public static boolean isSpace(char[] chars, int offset, int len) {
        while (len > 0) {
            if (chars[offset++] != ' ') {
                return false;
            }
            len--;
        }
        return true;
    }

    public static boolean isWhitespace(char[] chars) {
        return isWhitespace(chars, 0, chars.length);
    }

    /** Return true if the array contains only space or tab chars */
    public static boolean isWhitespace(char[] chars, int offset, int len) {
        while (len > 0) {
            if (!Character.isWhitespace(chars[offset])) {
                return false;
            }
            offset++;
            len--;
        }
        return true;
    }

    public static int getFirstNonSpace(char[] chars) {
        return getFirstNonSpace(chars, 0, chars.length);
    }

    /** Return the first index that is not space */
    public static int getFirstNonSpace(char[] chars, int offset, int len) {
        while (len > 0) {
            if (chars[offset] != ' ') {
                return offset;
            }
            offset++;
            len--;
        }
        return -1;
    }

    public static int getFirstNonWhite(char[] chars) {
        return getFirstNonWhite(chars, 0, chars.length);
    }

    /** Return the first index that is not space or tab or new-line char */
    public static int getFirstNonWhite(char[] chars, int offset, int len) {
        while (len > 0) {
            switch (chars[offset]) {
            case ' ':
            case '\t':
            case '\n':
                break;

            default:
                return offset;
            }
            offset++;
            len--;
        }
        return -1;
    }

    public static int getLastNonWhite(char[] chars) {
        return getLastNonWhite(chars, 0, chars.length);
    }

    /** Return the last index that is not space or tab or new-line char */
    public static int getLastNonWhite(char[] chars, int offset, int len) {
        int i = offset + len - 1;
        while (i >= offset) {
            switch (chars[i]) {
            case ' ':
            case '\t':
            case '\n':
                break;

            default:
                return i;
            }
            i--;
        }
        return -1;
    }

    /** Count the number of line feeds in char array.
    * @return number of LF characters contained in array.
    */
    public static int getLFCount(char chars[]) {
        return getLFCount(chars, 0, chars.length);
    }

    public static int getLFCount(char chars[], int offset, int len) {
        int lfCnt = 0;
        while (len > 0) {
            if (chars[offset++] == '\n') {
                lfCnt++;
            }
            len--;
        }
        return lfCnt;
    }

    public static int getLFCount(String s) {
        int lfCount = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '\n') {
                lfCount++;
            }
        }
        return lfCount;
    }

    public static int getFirstLFOffset(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '\n') {
                return i;
            }
        }
        return -1;
    }

    /** Get offset of the first found new-line
    * @return offset of '\n' or -1
    */
    public static int getFirstLFOffset(char[] chars) {
        return getFirstLFOffset(chars, 0, chars.length);
    }

    public static int getFirstLFOffset(char[] chars, int offset, int len) {
        while (len > 0) {
            if (chars[offset++] == '\n') {
                return offset - 1;
            }
            len--;
        }
        return -1;
    }

    public static int getFirstTab(char[] chars) {
        return getFirstTab(chars, 0, chars.length);
    }

    public static int getFirstTab(char[] chars, int offset, int len) {
        while (len > 0) {
            if (chars[offset++] == '\t') {
                return offset - 1;
            }
            len--;
        }
        return -1;
    }

    /** Reverses the order of characters in the array. It works from
    * the begining of the array, so no offset is given.
    */
    public static  void reverse(char[] chars, int len) {
        for (int i = ((--len - 1) >> 1); i >= 0; --i) {
            char ch = chars[i];
            chars[i] = chars[len - i];
            chars[len - i] = ch;
        }
    }

    public static boolean equals(String s, char[] chars) {
        return equals(s, chars, 0, chars.length);
    }

    public static boolean equals(String s, char[] chars, int offset, int len) {
        if (s.length() != len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != chars[offset + i]) {
                return false;
            }
        }
        return true;
    }

    /** Do initial reading of document. Translate any line separators
    * found in document to line separators used by document. It also cares
    * for elements that were already created on the empty document. Although
    * the document must be empty there can be already marks created. Initial
    * read is equivalent to inserting the string array of the whole document
    * size at position 0 in the document. Therefore all the marks that are
    * not insertAfter are removed and reinserted to the end of the document
    * after the whole initial read is finished.
    * @param doc document for which the initialization is performed
    * @param reader reader from which document should be read
    * @param lsType line separator type
    * @param testLS test line separator of file and if it's consistent, use it
    * @param markDistance the distance between the new syntax mark is put
    */
    public static void initialRead(BaseDocument doc, Reader reader, boolean testLS)
    throws IOException {
        // document must be empty
        if (doc.getLength() > 0) {
            return; // no read when already initialized
        }

        // for valid reader read the document
        if (reader != null) {
            int readBufferSize = ((Integer)doc.getProperty(
                                      Settings.READ_BUFFER_SIZE)).intValue();
            int markDistance = ((Integer)doc.getProperty(
                                    Settings.READ_MARK_DISTANCE)).intValue();
            Syntax syntax = doc.createSyntax();
            /* buffer into which the data from file will be read */
            char readBuf[] = new char[readBufferSize + 2];
            boolean firstRead = true; // first cycle of reading from stream
            boolean lastRead = false; // last cycle of reading from stream
            /* Was the last char in previous buffer '\r'? This should be
            * naturally boolean, but integer helps better in calculations.
            */
            int lastCR = 0;
            /* The same flag as previous one for currently read buffer */
            int thisCR = 0;
            int readLen = 0; // how many chars was read within cycle
            int bufLen = 0; // Length of readBuf[] used area
            /* Index in buffer where the scanner should stop scanning and return
            * S_EOT to caller. On these places there will be a mark placed.
            */ 
            int pos = 0; // pos in whole document
            int line = 0; // line counter for marks
            int preScan = 0; // prescan needed by syntax scanner
            int lineLimit = 0; // longest line found
            int lineLimitLastOffset = 0;

            synchronized (doc.op) {
                // array for getting mark array from renderer inner class
                final Mark origMarks[][] = new Mark[1][];
                doc.op.renderMarks(new DocMarks.Renderer() {
                                       public void render() {
                                           int markCnt = getMarkCnt();
                                           origMarks[0] = new Mark[markCnt];
                                           System.arraycopy(getMarkArray(), 0, origMarks[0], 0, markCnt);
                                       }
                                   });
                // now remove all the marks that are not insert after
                for (int i = 0; i < origMarks[0].length; i++) {
                    Mark mark = origMarks[0][i];
                    if (!(mark.getInsertAfter()
                            || (mark instanceof MarkFactory.CaretMark))
                       ) {
                        try {
                            mark.remove();
                        } catch (InvalidMarkException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Enter the loop where all data from file will be read
                while (true) {
                    // check preScan and possibly copy some chars from end of previous buffer
                    preScan = syntax.getPreScan(); // get current preScan for syntax scanner
                    if (preScan > 0) {
                        if (preScan > readBuf.length / 2) { // prescan bigger than half of buffer
                            char[] tmp = new char[2 * readBuf.length + preScan]; // extend read buffer
                            System.arraycopy(readBuf, bufLen - preScan, tmp, 0, preScan);
                            readBuf = tmp;
                        } else { // the same buffer
                            System.arraycopy(readBuf, bufLen - preScan, readBuf, 0, preScan);
                        }
                    }

                    // first check if lastCR is set (preScan must be already set
                    // either to 0 for first read or at end of body of while() cycle)
                    if (lastCR == 1) { // last char in previous buffer was CR
                        readBuf[preScan] = '\r';
                    }

                    // read part of document into buffer
                    readLen = 0;
                    while (readLen == 0) { // read non-zero chars for algorithm to work
                        readLen = reader.read(readBuf, preScan + lastCR,
                                              readBuf.length - 2 - preScan - lastCR);
                    }

                    // check readLen value
                    if (readLen == -1) { // no more characters
                        bufLen = preScan + lastCR;
                        lastRead = true;
                    } else { // some chars were read
                        bufLen = preScan + lastCR + readLen;
                        if (readBuf[bufLen - 1] == '\r') { // last char in buffer is '\r'
                            bufLen--; // don't process last character
                            thisCR = 1; // set that there's CR at the end of the buffer
                        }
                    }

                    // check if we need to scan buffer for LS
                    if (firstRead && testLS) {
                        String newLS = testLS(readBuf, bufLen);
                        if (newLS != null) {
                            doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, newLS);
                            if (doc.getProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP) == null) {
                                doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, newLS);
                            }
                        }
                    }

                    // convert the line separators strictly to buffer
                    bufLen = convertLSToLF(readBuf, bufLen);

                    // set scanning indexes
                    if (firstRead) {
                        // for first read set scanning index
                        syntax.load(null, readBuf, preScan, bufLen - preScan, false);
                    }  else {
                        // for next reads only relocate scanning
                        syntax.relocate(readBuf, preScan, bufLen - preScan, false);
                    }

                    // now handle whole buffer - do syntax analyzes and create marks
                    int bufOffset = preScan;
                    while (bufOffset < bufLen) {

                        // compute stop index
                        syntax.setStopOffset(bufOffset = Math.min(bufOffset + markDistance, bufLen));

                        boolean cont = true;
                        while (cont) {
                            switch (syntax.nextToken()) { // ignore regular tokens
                            case Syntax.EOL: // end of line found
                                int offset = syntax.getOffset();
                                lineLimit = Math.max(lineLimit, offset - lineLimitLastOffset);
                                lineLimitLastOffset = offset;
                                line++; // increase line counter
                                break;

                            case Syntax.EOT: // stop index reached
                                MarkFactory.SyntaxMark mark = new MarkFactory.SyntaxMark();
                                mark.updateStateInfo(syntax);
                                try {
                                    doc.op.insertMark(mark, pos + bufOffset - preScan, line);
                                } catch (BadLocationException e) {
                                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                        e.printStackTrace();
                                    }
                                } catch (InvalidMarkException e) {
                                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                        e.printStackTrace();
                                    }
                                }
                                cont = false; // break the while()
                                break;
                            }
                        }
                    }
                    lineLimitLastOffset -= syntax.tokenOffset;

                    // store this buffer into cacheSupport
                    try {
                        doc.op.directCacheWrite(pos, readBuf, preScan, bufLen - preScan);
                    } catch (BadLocationException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    }

                    // initialize cache with first buffer
                    if (firstRead) {
                        doc.op.initCacheContent(readBuf, preScan, bufLen - preScan);
                    }

                    pos += bufLen - preScan;
                    lastCR = thisCR;
                    thisCR = 0;
                    firstRead = false; // must be false after first cycle
                    if (lastRead) { // break while cycle if at last read
                        break;
                    }
                }

                // Now reinsert marks that were removed at begining to the end
                for (int i = 0; i < origMarks[0].length; i++) {
                    Mark mark = origMarks[0][i];
                    if (!(mark.getInsertAfter()
                            || (mark instanceof MarkFactory.CaretMark))
                       ) {
                        try {
                            doc.op.insertMark(origMarks[0][i], pos, line);
                        } catch (InvalidMarkException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        } catch (BadLocationException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Set the line limit document property
                doc.putProperty(BaseDocument.LINE_LIMIT_PROP, new Integer(lineLimit));
                doc.op.initialReadUpdate();
            }
        }
    }

    /** Read from some reader and insert into document */
    static void read(BaseDocument doc, Reader reader, int pos)
    throws BadLocationException, IOException {
        int lastCR = 0;
        int thisCR;
        boolean lastRead = false;
        int readLen;
        int readBufferSize = ((Integer)doc.getProperty(
                                  Settings.READ_BUFFER_SIZE)).intValue();
        char[] readBuf = new char[readBufferSize + 1];
        while (true) {
            // read part of document into buffer
            readLen = 0;
            while (readLen == 0) { // read non-zero chars for algorithm to work
                readLen = reader.read(readBuf, lastCR, readBufferSize);
            }
            thisCR = 0;
            if (readLen == -1) {
                if (lastCR == 0) {
                    break;
                } else {
                    readLen = 0;
                }
            } else { // some chars were read
                if (readBuf[readLen + lastCR - 1] == '\r') {
                    thisCR = 1;
                    readLen--;
                }
            }
            readLen += lastCR;
            readLen = convertLSToLF(readBuf, readLen);
            doc.insertString(pos, new String(readBuf, 0, readLen), null);
            pos += readLen;
            lastCR = thisCR;
        }
    }

    /** Write from document to some writer */
    static void write(BaseDocument doc, Writer writer, int pos, int len)
    throws BadLocationException, IOException {
        String lsType = (String)doc.getProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP);
        if (lsType == null) {
            lsType = (String)doc.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP);
            if (lsType == null) {
                lsType = BaseDocument.LS_LF;
            }
        }
        int writeBufferSize = ((Integer)doc.getProperty(
                                   Settings.WRITE_BUFFER_SIZE)).intValue();
        char[] getBuf = new char[writeBufferSize];
        char[] writeBuf = new char[2 * writeBufferSize];
        while (len > 0) {
            int actLen = Math.min(len, writeBufferSize);
            doc.getChars(pos, getBuf, 0, actLen);
            int tgtLen = convertLFToLS(getBuf, actLen, writeBuf, lsType);
            writer.write(writeBuf, 0, tgtLen);
            pos += actLen;
            len -= actLen;
        }
    }

    /** Get visual column. */
    public static int getColumn(char buffer[], int offset,
                                int len, int tabSize, int startCol) {
        int col = startCol;
        int endOffset = offset + len;
        while (offset < endOffset) {
            switch (buffer[offset++]) {
            case '\t':
                col = (col + tabSize) / tabSize * tabSize;
                break;
            default:
                col++;
            }
        }
        return col;
    }

    /** Get buffer filled with appropriate number of spaces. The buffer
    * can have actually more spaces than requested.
    * @param numSpaces number of spaces
    */
    public static char[] getSpacesBuffer(int numSpaces) {
        // check if there's enough space in white space array
        if (numSpaces > spacesBuffer.length) {
            char tmpBuf[] = new char[numSpaces * 2]; // new buffer

            // initialize new buffer with spaces
            for (int i = 0; i < tmpBuf.length; i += spacesBuffer.length) {
                System.arraycopy(spacesBuffer, 0, tmpBuf, i,
                                 Math.min(spacesBuffer.length, tmpBuf.length - i));
            }
            spacesBuffer = tmpBuf;
        }

        return spacesBuffer;
    }

    public static char[] createSpacesBuffer(int numSpaces) {
        char[] ret = new char[numSpaces];
        System.arraycopy(getSpacesBuffer(numSpaces), 0, ret, 0, numSpaces);
        return ret;
    }

    /** Get buffer filled with appropriate number of tabs. The buffer
    * can have actually more tabs than requested.
    * @param numSpaces number of spaces
    */
    public static char[] getTabsBuffer(int numTabs) {
        // check if there's enough space in white space array
        if (numTabs > tabsBuffer.length) {
            char tmpBuf[] = new char[numTabs * 2]; // new buffer

            // initialize new buffer with spaces
            for (int i = 0; i < tmpBuf.length; i += tabsBuffer.length) {
                System.arraycopy(tabsBuffer, 0, tmpBuf, i,
                                 Math.min(tabsBuffer.length, tmpBuf.length - i));
            }
            tabsBuffer = tmpBuf;
        }

        return tabsBuffer;
    }

    public static char[] getIndentChars(int indent, boolean expandTabs, int tabSize) {
        if (expandTabs) {
            return createSpacesBuffer(indent);
        } else {
            return createWhiteSpaceFillBuffer(0, indent, tabSize);
        }
    }

    /** Get buffer filled with spaces/tabs so that it reaches from
    * some column to some other column.
    */
    public static char[] createWhiteSpaceFillBuffer(int startCol, int endCol,
            int tabSize) {
        if (startCol >= endCol) {
            return EMPTY_CHAR_ARRAY;
        }
        int tabs = 0;
        int spaces = 0;
        int nextTab = (startCol + tabSize) / tabSize * tabSize;
        if (nextTab > endCol) { // only spaces
            spaces += endCol - startCol;
        } else { // at least one tab
            tabs++; // jump to first tab
            int endSpaces = endCol - endCol / tabSize * tabSize;
            tabs += (endCol - endSpaces - nextTab) / tabSize;
            spaces += endSpaces;
        }

        char[] ret = new char[tabs + spaces];
        if (tabs > 0) {
            System.arraycopy(getTabsBuffer(tabs), 0, ret, 0, tabs);
        }
        if (spaces > 0) {
            System.arraycopy(getSpacesBuffer(spaces), 0, ret, tabs, spaces);
        }
        return ret;
    }

    /** Loads the file and performs conversion of line separators to LF.
    * This method can be used in debuging of syntax scanner or somewhere else.
    * @param fileName the name of the file to load
    * @return array of loaded characters with '\n' as line separator
    */
    public static char[] loadFile(String fileName) throws IOException {
        File file = new File(fileName);
        char chars[] = new char[(int)file.length()];
        FileReader reader = new FileReader(file);
        reader.read(chars);
        reader.close();
        int len = Analyzer.convertLSToLF(chars, chars.length);
        if (len != chars.length) {
            char copyChars[] = new char[len];
            System.arraycopy(chars, 0, copyChars, 0, len);
            chars = copyChars;
        }
        return chars;
    }

    /** Convert text with LF line separators to text that uses
    * line separators of the document. This function is used when
    * saving text into the file. Segment's data are converted inside
    * the segment's data or new segment's data array is allocated.
    * NOTE: Source segment must have just LFs as separators! Otherwise
    *   the conversion won't work correctly.
    * @param src source chars to convert from
    * @param len length of valid part of src data
    * @param tgt target chars to convert to. The array MUST have twice
    *   the size of src otherwise index exception can be thrown
    * @param lsType line separator type to be used i.e. LS_LF, LS_CR, LS_CRLF
    * @return length of valid chars in tgt array
    */
    public static int convertLFToLS(char[] src, int len, char[] tgt, String lsType) {
        if (lsType.equals(BaseDocument.LS_CR)) { // CR instead of LF
            System.arraycopy(src, 0, tgt, 0, len);

            // now do conversion for LS_CR
            if (lsType == BaseDocument.LS_CR) { // will convert '\n' to '\r'
                char chars[] = tgt;
                for (int i = 0; i < len; i++) {
                    if (chars[i] == '\n') {
                        chars[i] = '\r';
                    }
                }
            }
            return len;
        } else if (lsType.equals(BaseDocument.LS_CRLF)) {
            int tgtLen = 0;
            int moveStart = 0; // start of block that must be moved
            int moveLen; // length of chars moved

            for (int i = 0; i < len; i++) {
                if (src[i] == '\n') { // '\n' found
                    moveLen = i - moveStart;
                    if (moveLen > 0) { // will need to arraycopy
                        System.arraycopy(src, moveStart, tgt, tgtLen, moveLen);
                        tgtLen += moveLen;
                    }
                    tgt[tgtLen++] = '\r';
                    tgt[tgtLen++] = '\n';
                    moveStart = i + 1; // skip separator
                }
            }

            // now move the rest if it's necessary
            moveLen = len - moveStart;
            if (moveLen > 0) {
                System.arraycopy(src, moveStart, tgt, tgtLen, moveLen);
                tgtLen += moveLen;
            }
            return tgtLen;
        } else { // Using either \n or line separator is unknown
            System.arraycopy(src,0, tgt, 0, len);
            return len;
        }
    }

    public static boolean startsWith(char[] chars, char[] prefix) {
        if (chars == null || chars.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (chars[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean endsWith(char[] chars, char[] suffix) {
        if (chars == null || chars.length < suffix.length) {
            return false;
        }
        for (int i = chars.length - suffix.length; i < chars.length; i++) {
            if (chars[i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

    public static char[] concat(char[] chars1, char[] chars2) {
        if (chars1 == null || chars1.length == 0) {
            return chars2;
        }
        if (chars2 == null || chars2.length == 0) {
            return chars1;
        }
        char[] ret = new char[chars1.length + chars2.length];
        System.arraycopy(chars1, 0, ret, 0, chars1.length);
        System.arraycopy(chars2, 0, ret, chars1.length, chars2.length);
        return ret;
    }

    public static char[] extract(char[] chars, int offset, int len) {
        char[] ret = new char[len];
        System.arraycopy(chars, offset, ret, 0, len);
        return ret;
    }

    public static boolean blocksHit(int[] blocks, int startPos, int endPos) {
        return (blocksIndex(blocks, startPos, endPos) >= 0);
    }

    public static int blocksIndex(int[] blocks, int startPos, int endPos) {
        if (blocks.length > 0) {
            int onlyEven = ~1;
            int low = 0;
            int high = blocks.length - 2;

            while (low <= high) {
                int mid = ((low + high) / 2) & onlyEven;

                if (blocks[mid + 1] <= startPos) {
                    low = mid + 2;
                } else if (blocks[mid] >= endPos) {
                    high = mid - 2;
                } else {
                    return low; // found
                }
            }
        }

        return -1;
    }

    /** Remove all spaces fromt the given string */
    public static String removeSpaces(String s) {
        int spcInd = s.indexOf(' ');
        while (spcInd >= 0) {
            s = s.substring(0, spcInd) + s.substring(spcInd).trim();
            spcInd = s.indexOf(' ');
        }
        return s;
    }

}

/*
 * Log
 *  41   Gandalf-post-FCS1.38.1.1    4/3/00   Miloslav Metelka undo update
 *  40   Gandalf-post-FCS1.38.1.0    3/8/00   Miloslav Metelka 
 *  39   Gandalf   1.38        1/13/00  Miloslav Metelka 
 *  38   Gandalf   1.37        1/10/00  Miloslav Metelka 
 *  37   Gandalf   1.36        1/6/00   Miloslav Metelka Fixed #4584
 *  36   Gandalf   1.35        12/28/99 Miloslav Metelka 
 *  35   Gandalf   1.34        11/14/99 Miloslav Metelka 
 *  34   Gandalf   1.33        11/8/99  Miloslav Metelka 
 *  33   Gandalf   1.32        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  32   Gandalf   1.31        10/10/99 Miloslav Metelka 
 *  31   Gandalf   1.30        9/16/99  Miloslav Metelka 
 *  30   Gandalf   1.29        9/15/99  Miloslav Metelka 
 *  29   Gandalf   1.28        9/10/99  Miloslav Metelka 
 *  28   Gandalf   1.27        8/27/99  Miloslav Metelka 
 *  27   Gandalf   1.26        7/29/99  Miloslav Metelka 
 *  26   Gandalf   1.25        7/26/99  Miloslav Metelka 
 *  25   Gandalf   1.24        7/22/99  Miloslav Metelka 
 *  24   Gandalf   1.23        7/21/99  Miloslav Metelka 
 *  23   Gandalf   1.22        7/21/99  Miloslav Metelka 
 *  22   Gandalf   1.21        7/20/99  Miloslav Metelka 
 *  21   Gandalf   1.20        7/9/99   Miloslav Metelka 
 *  20   Gandalf   1.19        7/2/99   Miloslav Metelka 
 *  19   Gandalf   1.18        6/24/99  Miloslav Metelka Drawing improved
 *  18   Gandalf   1.17        6/10/99  Miloslav Metelka 
 *  17   Gandalf   1.16        6/8/99   Miloslav Metelka 
 *  16   Gandalf   1.15        6/1/99   Miloslav Metelka 
 *  15   Gandalf   1.14        6/1/99   Miloslav Metelka 
 *  14   Gandalf   1.13        5/21/99  Miloslav Metelka 
 *  13   Gandalf   1.12        5/18/99  Miloslav Metelka fixed loading large 
 *       files
 *  12   Gandalf   1.11        5/5/99   Miloslav Metelka 
 *  11   Gandalf   1.10        4/23/99  Miloslav Metelka changes in settings
 *  10   Gandalf   1.9         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  9    Gandalf   1.8         4/6/99   Miloslav Metelka fixed #1437
 *  8    Gandalf   1.7         4/1/99   Miloslav Metelka 
 *  7    Gandalf   1.6         3/30/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/27/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/23/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/9/99   Miloslav Metelka 
 *  2    Gandalf   1.1         2/3/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */
