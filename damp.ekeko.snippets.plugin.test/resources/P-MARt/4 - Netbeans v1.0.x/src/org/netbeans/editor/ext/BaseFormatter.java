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
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorDebug;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SettingsChangeEvent;

/**
* Base formatter providing basic support for finding indentation
* and creating writers
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseFormatter extends Formatter {

    protected Resolver[] resolvers = new Resolver[0];

    protected char[] hotChars = Analyzer.EMPTY_CHAR_ARRAY;

    private Class kitClass;

    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
        super.settingsChange(evt, kitClass);
        this.kitClass = kitClass; // store it locally here
    }

    /** Create format writer. This is the primary method to be overriden
    * in children to get the appropriate format writer created.
    */
    protected Writer createFormatWriter(Document doc, Syntax syntax,
                                        Writer underWriter, int startIndent, boolean atLineStart) {
        return new BaseFormatWriter(this, syntax, underWriter,
                                    startIndent, atLineStart);
    }

    public Writer createWriter(Document doc, int offset, Writer writer) {
        int startIndent = 0;
        boolean atLineStart = false;
        Syntax syntax;
        try {
            if (doc instanceof BaseDocument) {
                BaseDocument d = (BaseDocument)doc;
                syntax = d.createSyntax();
                startIndent = Math.max(findAnyIndent(d, offset), 0);
                atLineStart = (Utilities.getRowStart(d, offset) == offset);
            } else {
                syntax = BaseKit.getKit(kitClass).createSyntax(null);
            }
            return createFormatWriter(doc, syntax, writer, startIndent, atLineStart);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return writer;
    }

    /** Find the indentation for the line given by pos.
    * @param doc document 
    * @param pos position on the line
    * @return indentation of the given line or -1 if indentation not resolved
    */
    protected int findIndent(BaseDocument doc, final int pos)
    throws BadLocationException {
        final FinderFactory.CharArrayBwdFinder finder
        = new FinderFactory.CharArrayBwdFinder(hotChars);
        final boolean dbg = ((debugMode & DEBUG_FIND_INDENT) != 0);
        if (dbg) {
            System.out.println("findIndent(): Entering with pos=" + pos // NOI18N
                               + ", hotChars='" + EditorDebug.debugChars(hotChars) + "'"); // NOI18N
        }
        final SyntaxSupport sup = doc.getSyntaxSupport();

        TextBatchProcessor tbp = new TextBatchProcessor() {
                                     public int processTextBatch(BaseDocument doc2, int startPos,
                                                                 int endPos, boolean lastBatch) {
                                         try {
                                             int[] commentBlocks = doc2.getSyntaxSupport().getCommentBlocks(endPos, startPos);
                                             if (dbg) {
                                                 System.out.println("findIndent(): Area computed: [" + startPos + ", " // NOI18N
                                                                    + endPos + "] or [" + Utilities.debugPosition(doc2, startPos) // NOI18N
                                                                    + ", " + Utilities.debugPosition(doc2, endPos) + "]\n" // NOI18N
                                                                    + "Blocks:\n" + EditorDebug.debugBlocks(doc2, commentBlocks)); // NOI18N
                                             }

                                             int lastPos = startPos;
                                             while ((lastPos = doc2.find(finder, lastPos, endPos)) != -1) {
                                                 if (!Analyzer.blocksHit(commentBlocks, lastPos, lastPos + 1)) {
                                                     char hotChar = finder.getFoundChar();
                                                     if (dbg) {
                                                         System.out.println("findIndent(): Found hotChar '" + hotChar // NOI18N
                                                                            + "' at pos=" + lastPos + " or " + Utilities.debugPosition(doc2, lastPos)); // NOI18N
                                                     }
                                                     // go through resolvers to find the ones with correct hotChar
                                                     for (int i = 0; i < resolvers.length; i++) {
                                                         if (resolvers[i].getHotChar() == hotChar) {
                                                             if (dbg) {
                                                                 System.out.println("Found resolver '" + resolvers[i].getName() // NOI18N
                                                                                    + "' for hotChar='" + hotChar + "'"); // NOI18N
                                                             }
                                                             int indent = resolvers[i].resolve(doc2, pos, lastPos);
                                                             if (indent != -1) {
                                                                 if (dbg) {
                                                                     System.out.println("findIndent(): Found indent=" + indent // NOI18N
                                                                                        + " by resolver '" + resolvers[i].getName() + "'"); // NOI18N
                                                                 }
                                                                 return indent; // request stop
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         } catch (BadLocationException e) {
                                             e.printStackTrace();
                                         }
                                         return -1; // continue the search
                                     }
                                 };

        return doc.processText(tbp, pos, 0);
    }

    /** First call findIndent() method and if it doesn't return
    * indent, then try to get indent by the previous line indent.
    * If this fails, return -1.
    */
    protected int findAnyIndent(BaseDocument doc, int pos)
    throws BadLocationException {
        int indent = findIndent(doc, pos);
        if (indent < 0) {
            int prevBolPos = Math.max(Utilities.getRowStart(doc, pos, -1), 0);
            indent = Utilities.getRowIndent(doc, prevBolPos, false);
        }
        return indent;
    }

    /** Round the computed indentation to the multiply
    * of the shiftwidth.
    */
    protected int roundIndent(int indent) {
        int shw = getShiftWidth();
        return indent / shw * shw;
    }

    private void updateResList(int ind, Resolver r, boolean add) {
        ArrayList rlist = new ArrayList(Arrays.asList(resolvers));
        if (add) {
            rlist.add(ind, r);
        } else {
            rlist.remove(ind);
        }
        resolvers = (Resolver[])rlist.toArray(resolvers);
        updateHotChars();
    }

    public void addResolver(Resolver r) {
        updateResList(resolvers.length, r, true);
    }

    public void addResolver(int ind, Resolver r) {
        updateResList(ind, r, true);
    }

    public void removeResolver(int ind) {
        updateResList(ind, null, false);
    }

    public String debugResolvers() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < resolvers.length; i++) {
            sb.append("[" + i + "]: "); // NOI18N
            sb.append(resolvers[i].getName());
            sb.append('\n');
        }
        sb.append("\nDescriptions:\n"); // NOI18N
        for (int i = 0; i < resolvers.length; i++) {
            Resolver r = resolvers[i];
            sb.append(r.getName());
            sb.append(": "); // NOI18N
            sb.append(r.getDesc());
            sb.append("\n\n"); // NOI18N
        }
        return sb.toString();
    }

    private char[] addHotChar(char ch, char[] hots) {
        for (int i = 0; i < hots.length; i++) {
            if (hots[i] == ch) {
                return hots; // already in
            }
        }
        char[] tmp = new char[hots.length + 1];
        System.arraycopy(hots, 0, tmp, 0, hots.length);
        tmp[hots.length] = ch;
        return tmp;
    }

    private void updateHotChars() {
        char[] hots = Analyzer.EMPTY_CHAR_ARRAY;
        for (int i = 0; i < resolvers.length; i++) {
            char ch = resolvers[i].getHotChar();
            int j;
            for (j = 0; j < hots.length; j++) {
                if (hots[j] == ch) {
                    break;
                }
            }
            if (j == hots.length) { // not there
                hots = addHotChar(ch, hots);
            }
        }
        hotChars = hots;
    }


    /** Layer that tries to resolve the indentation for a given line.
    * If it doesn't succeed by returning -1, the next resolver in
    * the resolver array is attempted.
    */
    public interface Resolver {

        /** Get the name of this resolver */
        public String getName();

        /** Get the description of this resolver */
        public String getDesc();

        /** Get the hot character for this resolver */
        public char getHotChar();

        /** Try to resolve the indentation and find the indentation
        * @param doc document to work with
        * @param pos original position for which the indentation is being searched
        * @param hotCharPos position where the hot char was found
        */
        public int resolve(BaseDocument doc, int pos, int hotCharPos);

    }


    // Debugging

    public int debugMode; // debugging of the formatter actions

    public static final int DEBUG_FIND_INDENT = 1;

    public static final int DEBUG_FORMAT = 2;

    protected final boolean dbgFI() {
        return ((debugMode & DEBUG_FIND_INDENT) != 0);
    }

    protected final boolean dbgF() {
        return ((debugMode & DEBUG_FORMAT) != 0);
    }


}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/8/00   Miloslav Metelka 
 *  9    Gandalf   1.8         1/13/00  Miloslav Metelka Localization
 *  8    Gandalf   1.7         1/6/00   Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/10/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/26/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

