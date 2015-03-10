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

import java.io.Writer;
import java.io.IOException;
import javax.swing.text.Segment;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.EditorDebug;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.Syntax;

/**
* Base format writer used to format the text.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseFormatWriter extends Writer {

    /** Formatter reference */
    private BaseFormatter formatter;

    /** Syntax scanner used to process the text */
    private Syntax syntax;

    private boolean syntaxInited;

    /** Underlying writer */
    protected Writer underWriter;

    /** Start indentation on which all additional indentation
    * is based.
    */
    protected int startIndent;

    /** Whether the starting position is at the begining
    * of the first input line.
    */
    protected boolean startAtLineBegin;

    /** Whether the input is currently at the begin of line */
    protected boolean atLineBegin;

    /** Whether the current token is part
    * of the initial white-space of the line
    */
    protected boolean initialWhitespace;

    /** Ignore initial whitespace on the line. Process token
    * is not called for the tokens containing initial whitespace
    * on the line.
    */
    protected boolean ignoreInitWS;

    /** Whether the writer is currently processing the first
    * line of the input characters. There can be some special handling
    * for the first line.
    */
    protected boolean firstInputLine;

    /** Segment with the scanned text. Its offset must be kept at zero. */
    protected String inputString;

    /** Segment for the current line */
    protected StringBuffer lineBuffer;

    /** Output segment. The current line begins at seg.offset and
    * its length is seg.length.
    */
    protected StringBuffer outBuffer;

    /** Indent for the current line. When the line is written
    * to the output. At the startup this is populated by startIndent.
    */
    protected int indent;

    /** Indent of the next line. After the line is written,
    * indent variable is populated by the value of this variable.
    */
    protected int nextLineIndent;

    /** When there was the explicit flush() requested on this writer. */
    protected boolean flushedEarly;

    /** Trim trailing whitespace from each output line */
    protected boolean outRightTrimLine;

    /** First token on the line (right after the previous new-line) */
    protected boolean firstTokenOnLine;

    public BaseFormatWriter(BaseFormatter formatter, Syntax syntax,
                            Writer underWriter, int startIndent, boolean startAtLineBegin) {
        this.formatter = formatter;
        this.underWriter = underWriter;
        this.startIndent = startIndent;
        this.syntax = syntax;
        this.startAtLineBegin = startAtLineBegin;
        atLineBegin = startAtLineBegin;
        firstInputLine = true;
        lineBuffer = new StringBuffer();
        outBuffer = new StringBuffer();
        indent = formatter.roundIndent(startIndent); // keep the real original indent
        nextLineIndent = indent;
        initialWhitespace = startAtLineBegin;
        firstTokenOnLine = startAtLineBegin;
        if (!startAtLineBegin) {
            indent = 0;
        }
        outRightTrimLine = true;
        //    debugMode = Integer.MAX_VALUE;
    }

    public void write(char buf[], int off, int len)
    throws IOException {
        if ((debugMode & DEBUG_INPUT_STRING) != 0) {
            System.out.println("Input off=" + off + ", len=" + len // NOI18N
                               + ", text='" + EditorDebug.debugChars(buf, off, len) + "'"); // NOI18N
        }
        if (len == 0) {
            return;
        }

        int preScan = syntax.getPreScan();
        char[] newBuf = new char[preScan + len];
        if (preScan > 0) {
            int inpLen = inputString.length();
            int preScanOffset = inpLen - preScan;
            inputString.getChars(preScanOffset, inpLen, newBuf, 0);
        }
        System.arraycopy(buf, off, newBuf, preScan, len);
        buf = newBuf;

        inputString = new String(buf, 0, buf.length);
        syntax.relocate(buf, preScan, len, false);
        boolean done = false;
        while (!done) {
            int tokenID = syntax.nextToken();
            int helperID = syntax.getHelperID();
            switch (tokenID) {
            case Syntax.EOT: // end of buffer text reached
                done = true;
                break;

            case Syntax.EOL: // end of line in scanned text
                if (processEOL()) {
                    finishLine();
                }
                initialWhitespace = true;
                firstTokenOnLine = true;
                break;

            default:
                int tokenOffset = syntax.getTokenOffset();
                int tokenLen = syntax.getTokenLength();
                String token = inputString.substring(tokenOffset,
                                                     tokenOffset + tokenLen);
                if (initialWhitespace) {
                    initialWhitespace = isWhitespaceToken(tokenID, helperID, token);
                }
                if (!initialWhitespace || !ignoreInitWS) {
                    lineBuffer.append(processToken(tokenID, helperID, token));
                }
                firstTokenOnLine = false;
                break;
            }
        }
    }

    /** Process the current token.
    */
    protected String processToken(int tokenID, int helperID, String token) {
        return token;
    }

    protected boolean isWhitespaceToken(int tokenID, int helperID, String token) {
        for (int i = token.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(token.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Process end of line */
    protected boolean processEOL() {
        return true;
    }

    protected void changeIndent(int relIndent) {
        if ((debugMode & DEBUG_INDENT) != 0) {
            System.out.println("Changing indent of the current line from " // NOI18N
                               + indent + " to " + (indent + relIndent)); // NOI18N
        }
        indent += relIndent;
    }

    protected void changeNextLineIndent(int relNextIndent) {
        if ((debugMode & DEBUG_INDENT) != 0) {
            System.out.println("Changing indent of the next line from " // NOI18N
                               + indent + " to " + (nextLineIndent + relNextIndent)); // NOI18N
        }
        nextLineIndent += relNextIndent;
    }

    protected char[] getIndentChars() {
        return Analyzer.getIndentChars(Math.max(indent, 0),
                                       formatter.expandTabs(), formatter.getTabSize());
    }

    protected boolean isEndLineWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    /** Used to flush the current line into output buffer
    * @param flushingEarly whether the line is being flushed before it was fully
    *   read from the input stream
    */
    protected void writeLine(boolean flushingEarly) {
        int dbgTrimLen = 0;
        if (outRightTrimLine) { // trim trailing whitespace
            int endOffset = lineBuffer.length();
            if (endOffset > 0 && lineBuffer.charAt(endOffset - 1) == '\n') {
                endOffset--;
            }
            int endWSOffset = endOffset;
            while (endWSOffset > 0) {
                if (isEndLineWhitespace(lineBuffer.charAt(endWSOffset - 1))) {
                    endWSOffset--;
                } else {
                    break;
                }
            }

            dbgTrimLen = endOffset - endWSOffset;
            if (dbgTrimLen > 0) {
                lineBuffer.delete(endWSOffset, endOffset);
            }
        }

        char[] indentChars = null;
        boolean emptyLine // is the currently written line empty?
        = (lineBuffer.length() == 0) // it is if there are no characters on the line
          || (lineBuffer.length() == 1 && lineBuffer.charAt(0) == '\n'); // or just new-line
        if (emptyLine && !flushedEarly && flushingEarly) {
            // the line wasn't flushed early, but now we want to flush it early
            // we don't know whether there will be any other characters but we suppose so
            // in this case, so we'll write the indentation
            // This is because of NetBeans code generator needs to know the positions
            // in the document so it first calls flush() with no generated code
            // and it expects to get the correct indent as an output
            if (firstInputLine) { // enable this behavior only on the first input line
                // on the later lines the flushing adds the indent too early and
                // if there's a '}' the indent is not decreased
                emptyLine = false;
            }
        }

        if (!emptyLine
                && (!firstInputLine || startAtLineBegin)
                && !flushedEarly
           ) { // write indent
            indentChars = getIndentChars();
            outBuffer.append(indentChars);
        }

        if ((debugMode & DEBUG_FORMAT) != 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Creating output text: "); // NOI18N
            if (emptyLine) {
                sb.append("Empty Line"); // NOI18N
            }
            if (dbgTrimLen > 0) {
                if (emptyLine) {
                    sb.append(", "); // NOI18N
                }
                sb.append("Right trim="); // NOI18N
                sb.append(dbgTrimLen);
            }
            if (indentChars != null && indentChars.length > 0) {
                if (emptyLine || dbgTrimLen > 0) {
                    sb.append(", "); // NOI18N
                }
                sb.append("Indentation="); // NOI18N
                sb.append(indentChars.length);
            }
            sb.append(" Text='"); // NOI18N
            sb.append(EditorDebug.debugString(lineBuffer.toString()));
            sb.append("', Length="); // NOI18N
            sb.append(lineBuffer.length());
            System.out.println(sb.toString());
        }

        if (atLineBegin && (indentChars != null || lineBuffer.length() > 0)) {
            atLineBegin = false;
        }

        outBuffer.append(lineBuffer);
        lineBuffer.setLength(0);

    }

    /** This method is called when there's an new-line found
    * in the input string. All the tokens on the line were
    * completed and the line is ready to be written out.
    * It first appends new-line to the end of line
    * then it writes it to the output.
    * Start new-line.
    */
    protected void finishLine() {
        if ((debugMode & DEBUG_LINE) != 0) {
            System.out.println("Finishing line ... Writing line to output ..."); // NOI18N
        }
        lineBuffer.append("\n"); // NOI18N
        writeLine(false);
        firstInputLine = false;
        flushedEarly = false;
        indent = nextLineIndent;
        atLineBegin = true;
    }

    protected void flushLine() {
        if ((debugMode & DEBUG_LINE) != 0) {
            System.out.println("Flushing line ... Writing line to output ..."); // NOI18N
        }
        writeLine(true);
        if (!atLineBegin) {
            if ((debugMode & DEBUG_LINE) != 0) {
                System.out.println("Line flushed early"); // NOI18N
            }
            flushedEarly = true;
        }
    }

    public void flush() throws IOException {
        if ((debugMode & DEBUG_FLUSH_CLOSE) != 0) {
            System.out.println("flush() requested"); // NOI18N
        }
        int preScan = syntax.getPreScan();
        if (preScan > 0) { // some chars at end of buffer
            String preScanStr = inputString.substring(inputString.length() - preScan);
            lineBuffer.append(preScanStr);
            syntax.reset();
        }
        flushLine();

        if ((debugMode & DEBUG_OUTPUT_STRING) != 0) {
            System.out.println("Output text='" + EditorDebug.debugString(outBuffer.toString()) + "'"); // NOI18N
        }

        int outLen = outBuffer.length();
        if (outLen > 0) {
            char[] buf = new char[outLen];
            outBuffer.getChars(0, outLen, buf, 0);
            underWriter.write(buf, 0, outLen);
            outBuffer.setLength(0);
        }
        underWriter.flush();
    }

    public void close() throws IOException {
        if ((debugMode & DEBUG_FLUSH_CLOSE) != 0) {
            System.out.println("close() requested"); // NOI18N
        }
        flush();
        underWriter.close();
    }


    // Debugging

    public int debugMode; // debugging of the formatter actions

    /** Debug formatting of the text */
    public static final int DEBUG_FORMAT = 2;
    /** Debug flushes and closing of the buffer */
    public static final int DEBUG_FLUSH_CLOSE = 4;
    /** Debug the indentation changes */
    public static final int DEBUG_INDENT = 8;
    /** Debug the whole input string */
    public static final int DEBUG_INPUT_STRING = 16;
    /** Debug the whole output (written to underWriter) string */
    public static final int DEBUG_OUTPUT_STRING = 32;
    /** Debug the process of creating the output line */
    public static final int DEBUG_LINE = 64;
    /** Debug the input tokens */
    public static final int DEBUG_TOKEN = 128;

}

/*
 * Log
 *  12   Gandalf   1.11        1/18/00  Miloslav Metelka 
 *  11   Gandalf   1.10        1/13/00  Miloslav Metelka Localization
 *  10   Gandalf   1.9         1/7/00   Miloslav Metelka 
 *  9    Gandalf   1.8         1/6/00   Miloslav Metelka 
 *  8    Gandalf   1.7         12/28/99 Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         9/10/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/27/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

