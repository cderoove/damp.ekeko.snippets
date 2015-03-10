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
import javax.swing.text.Segment;

/**
* Debugging stuff for the syntax scanners
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SyntaxDebug {

    public static final String NO_STATE_ASSIGNED = "NO STATE ASSIGNED"; // NOI18N
    public static final String NULL_STATE = "NULL STATE"; // NOI18N
    public static final String NULL_SYNTAX_MARK = "NULL SYNTAX MARK"; // NOI18N

    public Syntax syntax;

    public SyntaxDebug(Syntax syntax) {
        this.syntax = syntax;
    }

    /** Scans the whole file by some syntax scanner.
    * @return number of tokens found
    */
    public int parseFile(String fileName)
    throws IOException {
        char chars[] = Analyzer.loadFile(fileName); // line separator only '\n'
        syntax.load(null, chars, 0, chars.length, true);
        int tokenCnt = debugScan(true);
        return tokenCnt;
    }

    /** Debug scanning on the given string. Write output to console.
    * It returns number of tokens found (excluding EOL and EOT).
    */
    public int debugScan(boolean lastBuffer) {
        int tokenCnt = 0;
        syntax.setLastBuffer(lastBuffer);
        while (true) {
            int tokenID = syntax.nextToken();
            switch (tokenID) {
            case Syntax.EOL:
                System.out.println(syntax.getTokenName(tokenID) + ":\t\t" + this); // NOI18N
                break;
            case Syntax.EOT:
                System.out.println(syntax.getTokenName(tokenID) + ":\t\t" + this); // NOI18N
                return tokenCnt;
            default:
                tokenCnt++;
                System.out.println("TOKEN(" + syntax.getTokenName(tokenID) // NOI18N
                                   + "): '" + EditorDebug.debugChars(syntax.getBuffer(), // NOI18N
                                                                     syntax.getTokenOffset(), syntax.getTokenLength())
                                   + "'<<\n\t\t" + this // NOI18N
                                  );
                break;
            }
        }
    }

    /** Tests if the scanning returns the same number of EOLs as there's actually
    * '\n' characters in the whole buffer. The test is performed on the whole buffer.
    */
    public boolean eolTest(char chars[]) {
        int lfCnt = Analyzer.getLFCount(chars);
        syntax.load(null, chars, 0, chars.length, true);
        int eolCnt = 0;
        int tokenID;
        do {
            tokenID = syntax.nextToken();
            if (tokenID == Syntax.EOL) {
                eolCnt++;
            }
        } while (tokenID != Syntax.EOT);
        if (lfCnt == eolCnt) { // test succeeded
            System.out.println("Test SUCCEEDED. " + lfCnt + " new-lines found."); // NOI18N
        } else {
            System.out.println("Test FAILED! Number of '\\n' chars: " + lfCnt // NOI18N
                               + ", number of EOLs: " + eolCnt); // NOI18N
        }
        return lfCnt == eolCnt;
    }

    /** Create array of arrays of chars containing wrong characters */
    /*  protected abstract char[][] createWrongCharsArray();

      /* Some arrays of typical wrong characters that can appear
      * in the tokens.
      */
    /*  public static final char[] WRONG_NL = new char[] { '\n' };
      public static final char[] WRONG_NL_TAB = new char[] { '\n', '\t' };
      public static final char[] WRONG_NL_TAB_SPC = new char[] { '\n', '\t', ' ' };
      
      /** Wrong character arrays for the tokens */
    /*  protected char[][] wrongCharsArray;

      public boolean checkTokenText(int tokenID) {
        boolean ok = true;
        if (wrongCharsArray == null) {
          wrongCharsArray = createWrongCharsArray();
        }
        if (wrongCharsArray != null) {
          if (tokenID >= wrongCharsArray.length) {
            return false;
          }
          char[] wrongChars = wrongCharsArray[tokenID];
          for (int i = curInd - tokenLength; i < curInd; i++) {
            for (int j = 0; j < wrongChars.length; j++) {
              if (buffer[i] == wrongChars[j]) {
                System.err.println("Token '" + getTokenName(tokenID) + "' having text " // NOI18N
                    + debugTokenArea() + " contains wrong character '" // NOI18N
                    + debugChars(wrongChars, j, 1) + "'. State: " + this); // NOI18N
                ok = false;
              }
            }
          }
        }
        return ok;
      }

      public String toStringArea() {
        return toString() + ", scan area=" + debugBufferArea(); // NOI18N
      }

      public String debugChars(char chars[], int offset, int len) {
        if (len < 0) {
          return "debugChars() ERROR: len=" + len + " < 0"; // NOI18N
        }
        StringBuffer sb = new StringBuffer(len);
        int endOffset = offset + len;
        for (; offset < endOffset; offset++) {
          switch (chars[offset]) {
            case '\n':
              sb.append("\\n"); // NOI18N
              break;
            case '\t':
              sb.append("\\t"); // NOI18N
              break;
            case '\r':
              sb.append("\\r"); // NOI18N
              break;
            default:
              sb.append(chars[offset]);
          }
        }
        return sb.toString();
      }


      /** Return string describing the area between begInd and curInd */
    /*  public String debugTokenArea() {
        return debugBufferArea(0, 0);
      }
        
      public String debugBufferArea() {
        return debugBufferArea(5, 5);
      }
      
      public String debugBufferArea(int preCnt, int postCnt) {
        StringBuffer sb = new StringBuffer();
        int preStart = Math.max(begInd - preCnt, 0);
        preCnt = begInd - preStart;
        if (preCnt > 0) {
          sb.append(" prefix='"); // NOI18N
          sb.append(debugChars(buffer, preStart, preCnt));
          sb.append("' "); // NOI18N
        }
        sb.append("'"); // NOI18N
        sb.append(debugChars(buffer, begInd, curInd - begInd));
        sb.append("'"); // NOI18N
        postCnt = stopInd - curInd;
        if (postCnt > 0) {
          sb.append(" suffix='"); // NOI18N
          sb.append(debugChars(buffer, preStart, preCnt));
          sb.append("' "); // NOI18N
        }
        return sb.toString();
      }

    */

}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.2.0     4/3/00   Miloslav Metelka undo update
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka 
 *  6    Gandalf   1.5         12/28/99 Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/16/99  Miloslav Metelka 
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         9/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

