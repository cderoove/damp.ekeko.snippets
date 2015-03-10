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

import java.beans.PropertyChangeEvent;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.ext.ExtSettings;

/**
* Java indentation services are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaFormatter extends BaseFormatter {

    public static final char[] semiCompounds = new char[] { ';', '{', '}' };

    public static final String SEMICOLON_RESOLVER_NAME
    = "semicolon-resolver"; // NOI18N

    public static final String SEMICOLON_RESOLVER_DESC
    = "After finding semicolon goes back one line and searches " // NOI18N
      + "for either '{', '}' or ';'. When one of those characters " // NOI18N
      + "is found, go to the next line (not for '}') and return indentation " // NOI18N
      + "for it."; // NOI18N

    public static final String LEFT_COMPOUND_BRACKET_RESOLVER_NAME
    = "left-compound-bracket-resolver"; // NOI18N

    public static final String LEFT_COMPOUND_BRACKET_RESOLVER_DESC
    = "After finding left compound bracket it searches for the begining " // NOI18N
      + "of the line with this bracket and adds one indent level."; // NOI18N

    public static final String RIGHT_COMPOUND_BRACKET_RESOLVER_NAME
    = "right-compound-bracket-resolver"; // NOI18N

    public static final String RIGHT_COMPOUND_BRACKET_RESOLVER_DESC
    = "After finding right compound bracket it returns the indentation " // NOI18N
      + "of the line with this bracket."; // NOI18N

    public static final String COLON_RESOLVER_NAME
    = "colon-resolver"; // NOI18N

    public static final String COLON_RESOLVER_DESC
    = "After finding colon, it adds one indent level and returns " // NOI18N
      + "the indentation."; // NOI18N

    private boolean parenthesisAddSpace;

    private boolean compoundBracketAddNL;

    private boolean commentAddStar;

    private boolean commentAddSpace;

    private boolean lastTokenWS;

    public JavaFormatter() {
        addResolver(new SemicolonResolver());
        addResolver(new LeftCompoundBracketResolver());
        addResolver(new RightCompoundBracketResolver());
        addResolver(new ColonResolver());
        //    debugMode |= 65535; // !!!
    }


    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
        super.settingsChange(evt, kitClass);
        String settingName = (evt != null) ? evt.getSettingName() : null;
        if (settingName == null || ExtSettings.FORMAT_PARENTHESIS_ADD_SPACE.equals(settingName)) {
            parenthesisAddSpace = SettingsUtil.getBoolean(kitClass,
                                  ExtSettings.FORMAT_PARENTHESIS_ADD_SPACE, false);
        }
        if (settingName == null || ExtSettings.FORMAT_COMPOUND_BRACKET_ADD_NL.equals(settingName)) {
            compoundBracketAddNL = SettingsUtil.getBoolean(kitClass,
                                   ExtSettings.FORMAT_COMPOUND_BRACKET_ADD_NL, false);
        }
        if (settingName == null || ExtSettings.FORMAT_COMMENT_ADD_STAR.equals(settingName)) {
            commentAddStar = SettingsUtil.getBoolean(kitClass,
                             ExtSettings.FORMAT_COMMENT_ADD_STAR, false);
        }
        if (settingName == null || ExtSettings.FORMAT_COMMENT_ADD_SPACE.equals(settingName)) {
            commentAddSpace = SettingsUtil.getBoolean(kitClass,
                              ExtSettings.FORMAT_COMMENT_ADD_SPACE, false);
        }
    }

    public boolean getParenthesisAddSpace() {
        return parenthesisAddSpace;
    }

    public boolean getCompoundBracketAddNL() {
        return compoundBracketAddNL;
    }

    public boolean getCommentAddStar() {
        return commentAddStar;
    }

    public boolean getCommentAddSpace() {
        return commentAddSpace;
    }


    /** Indents the current line. Should not affect any other
    * lines.
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset of the original character
    */
    public int indentLine (Document d, int offset) {
        if (d instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)d;
            int indent;
            try {
                indent = findAnyIndent((BaseDocument)doc, offset);
                if (indent < 0) {
                    indent = 0;
                }
                indent = roundIndent(indent);

                int firstNB = Utilities.getRowFirstNonWhite(doc, offset);
                if (firstNB >= 0) {
                    if (doc.getChars(firstNB, 1)[0] == '}') {
                        indent = Math.max(indent - getShiftWidth(), 0);
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
                indent = 0;
            }
            offset += indent;
        }

        return offset;
    }

    public class SemicolonResolver implements Resolver {

        public String getName() {
            return SEMICOLON_RESOLVER_NAME;
        }

        public String getDesc() {
            return SEMICOLON_RESOLVER_DESC;
        }

        public char getHotChar() {
            return ';';
        }

        public int resolve(BaseDocument doc, int pos, int hotCharPos) {
            try {
                SyntaxSupport sup = doc.getSyntaxSupport();
                int curBolPos = Utilities.getRowStart(doc, hotCharPos, -1);
                while (curBolPos != -1) {
                    if (!sup.isRowValid(curBolPos)) {
                        int lastCharPos = sup.getRowLastValidChar(curBolPos);
                        if (lastCharPos != -1) {
                            char lastChar = doc.getChars(lastCharPos, 1)[0];
                            switch (lastChar) {
                            case ';':
                            case '}':
                            case '{':
                            case ':':
                                int nextBol = Utilities.getRowStart(doc, curBolPos, +1);
                                return Utilities.getRowIndent(doc, nextBol, true);
                            }
                        } else {
                            break;
                        }
                    }
                    curBolPos = Utilities.getRowStart(doc, curBolPos, -1);
                }
                // no previous line found, use the line with ';'
                return Utilities.getRowIndent(doc, hotCharPos);
            } catch (BadLocationException e) {
                return -1;
            }
        }

    }

    public class LeftCompoundBracketResolver implements Resolver {

        public String getName() {
            return LEFT_COMPOUND_BRACKET_RESOLVER_NAME;
        }

        public String getDesc() {
            return LEFT_COMPOUND_BRACKET_RESOLVER_DESC;
        }

        public char getHotChar() {
            return '{';
        }

        public int resolve(BaseDocument doc, int pos, int hotCharPos) {
            try {
                SyntaxSupport sup = doc.getSyntaxSupport(); // !!! matchbracket
                return Utilities.getRowIndent(doc, hotCharPos)
                       + getShiftWidth();
            } catch (BadLocationException e) {
                return -1;
            }
        }

    }

    public class RightCompoundBracketResolver implements Resolver {

        public String getName() {
            return RIGHT_COMPOUND_BRACKET_RESOLVER_NAME;
        }

        public String getDesc() {
            return RIGHT_COMPOUND_BRACKET_RESOLVER_DESC;
        }

        public char getHotChar() {
            return '}';
        }

        public int resolve(BaseDocument doc, int pos, int hotCharPos) {
            try {
                return Utilities.getRowIndent(doc, hotCharPos);
            } catch (BadLocationException e) {
                return -1;
            }
        }

    }

    public class ColonResolver implements Resolver {

        public String getName() {
            return COLON_RESOLVER_NAME;
        }

        public String getDesc() {
            return COLON_RESOLVER_DESC;
        }

        public char getHotChar() {
            return ':';
        }

        public int resolve(BaseDocument doc, int pos, int hotCharPos) {
            try {
                return Utilities.getRowIndent(doc, hotCharPos)
                       + getShiftWidth();
            } catch (BadLocationException e) {
                return -1;
            }
        }

    }

    protected Writer createFormatWriter(Document doc, Syntax syntax,
                                        Writer underWriter, int startIndent, boolean atLineStart) {
        return new JavaFormatWriter(this, syntax, underWriter, startIndent, atLineStart);
    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/8/00   Miloslav Metelka 
 *  9    Gandalf   1.8         2/15/00  Miloslav Metelka parenthesis formatting
 *  8    Gandalf   1.7         1/13/00  Miloslav Metelka Localization
 *  7    Gandalf   1.6         1/10/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/6/00   Miloslav Metelka 
 *  5    Gandalf   1.4         11/14/99 Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/10/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

