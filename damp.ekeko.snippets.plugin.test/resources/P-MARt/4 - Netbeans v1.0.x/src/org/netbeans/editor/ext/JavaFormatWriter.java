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
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.DefaultSettings;
import org.netbeans.editor.Syntax;

/**
* Java format writer used to format the java source text.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaFormatWriter extends BaseFormatWriter {

    private JavaFormatter formatter;

    protected boolean lastTokenWS;

    public JavaFormatWriter(BaseFormatter formatter, Syntax syntax,
                            Writer underWriter, int startIndent, boolean atLineStart) {
        super(formatter, syntax, underWriter, startIndent, atLineStart);
        this.formatter = (JavaFormatter)formatter;
        ignoreInitWS = true;
    }

    protected boolean isWhitespaceToken(int tokenID, int helperID, String token) {
        return (tokenID == JavaSyntax.TEXT);
    }

    protected String processToken(int tokenID, int helperID, String token) {
        boolean currentTokenWS = false;

        switch (tokenID) {
        case JavaSyntax.TEXT:
            currentTokenWS = isWhitespaceToken(tokenID, helperID, token);
            break;

        case JavaSyntax.OPERATOR:
            switch (helperID) {
            case JavaSyntax.LEFT_PARENTHESES:
                if (formatter.getParenthesisAddSpace() && !lastTokenWS) {
                    token = " " + token;
                }
                break;

            case JavaSyntax.LEFT_BRACE:
                if ((debugMode & DEBUG_TOKEN) != 0) {
                    System.out.println("'{' found. compoundBracketAddNL=" + formatter.getCompoundBracketAddNL());  // NOI18N
                }
                if (formatter.getCompoundBracketAddNL()) {
                    finishLine();
                }
                changeNextLineIndent(+formatter.getShiftWidth());
                break;

            case JavaSyntax.RIGHT_BRACE:
                if ((debugMode & DEBUG_TOKEN) != 0) {
                    System.out.println("'}' found. "); // NOI18N
                }
                // decrease indent of current and next line
                int shw = formatter.getShiftWidth();
                changeIndent(-shw);
                changeNextLineIndent(-shw);
                break;
            }
            break;

        case JavaSyntax.BLOCK_COMMENT:
            if (firstTokenOnLine) { // first token on the line
                int tokenLen = token.length();
                int i;
                for (i = 0; i < tokenLen; i++) {
                    if (!Character.isWhitespace(token.charAt(i))) {
                        break;
                    }
                }
                token = token.substring(i);
                if (token.length() > 0 && token.charAt(0) == '*') {
                    // prefix it by one space
                    token = " " + token;
                }
            }
            /*        int firstNW = Analyzer.getFirstNonWhite(inToken);
                    boolean addStar = commentAddStar;
                    if (firstNW != -1) { // some text in the comment
                      if (Analyzer.startsWith(inToken, SLASH_STAR)
                          || Analyzer.startsWith(inToken, STAR)
                      ) {
                        addStar = false;
                      }
                    }
                    if (addStar) {
                      if (commentAddSpace) {
                        outPrefix = Analyzer.concat(outPrefix, SPACE_STAR);
                      } else {
                        outPrefix = Analyzer.concat(outPrefix, SPACE_STAR);
                      }
                    }
            */
            break;

        case Syntax.EOL:
            currentTokenWS = true;
            break;

        }

        lastTokenWS = currentTokenWS;

        return token;
    }

    protected boolean processEOL() {
        return true;
    }

}

/*
 * Log
 *  11   Gandalf   1.10        2/15/00  Miloslav Metelka parenthesis formatting
 *  10   Gandalf   1.9         1/18/00  Miloslav Metelka 
 *  9    Gandalf   1.8         1/13/00  Miloslav Metelka Localization
 *  8    Gandalf   1.7         1/7/00   Miloslav Metelka 
 *  7    Gandalf   1.6         1/6/00   Miloslav Metelka 
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/15/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/27/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

