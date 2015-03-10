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

import java.awt.Rectangle;
import java.awt.Frame;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.EditorKit;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;

/**
* Various useful editor functions. Some of the methods have
* the same names and signatures like in javax.swing.Utilities but
* there is also many other useful methods.
* All the methods are static so there's no reason to instantiate Utilities.
*
* All the methods working with the document rely on that it is locked against
* modification so they don't acquire document read/write lock by themselves
* to guarantee the full thread safety of the execution.
* It's the user's task to lock the document appropriately
* before using methods described here.
*
* Most of the methods require org.netbeans.editor.BaseDocument instance
* not just the javax.swing.text.Document.
* The reason for that is to mark that the methods work on BaseDocument
* instances only, not on generic documents. To convert the Document
* to BaseDocument the simple conversion (BaseDocument)target.getDocument()
* can be done or the method getDocument(target) can be called.
* There are also other conversion methods like getExtUI(), getKit()
* or getKitClass().
*
* @author Miloslav Metelka
* @version 0.10
*/

public class Utilities {

    private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N
    private static final String WRONG_POSITION_DEFAULT = "Wrong position"; // NOI18N

    /** Switch the case to capital letters. Used in changeCase() */
    public static final int CASE_UPPER = 0;

    /** Switch the case to small letters. Used in changeCase() */
    public static final int CASE_LOWER = 1;

    /** Switch the case to reverse. Used in changeCase() */
    public static final int CASE_SWITCH = 2;


    private Utilities() {
        // instantiation has no sense
    }

    /** Get the starting position of the row.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(JTextComponent c, int offset)
    throws BadLocationException {
        return getRowStart((BaseDocument)c.getDocument(), offset, 0);
    }

    /** Get the starting position of the row.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(BaseDocument doc, int offset)
    throws BadLocationException {
        return getRowStart(doc, offset, 0);
    }

    /** Get the starting position of the row while providing relative count
    * of row how the given position should be shifted. This is the most
    * efficient way how to move by lines in the document based on some
    * position. There is no similair getRowEnd() method that would have
    * shifting parameter.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param relLine shift offset forward/back relatively by some amount of lines
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(BaseDocument doc, int offset, int relLine)
    throws BadLocationException {
        if (relLine != 0) {
            return doc.op.getBOLRelLine(offset, relLine);
        } else { // no shift
            return doc.op.getBOL(offset);
        }
    }

    /** Get the first non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the first non-white char on the line or -1
    *   if there's no non-white character on that line.
    */
    public static int getRowFirstNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteFwd(doc, doc.op.getBOL(offset), doc.op.getEOL(offset));
    }

    /** Get the last non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the last non-white char on the line or -1
    *   if there's no non-white character on that line.
    */
    public static int getRowLastNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteBwd(doc, doc.op.getEOL(offset), doc.op.getBOL(offset));
    }

    /** Get indentation on the current line. If this line is white then
    * return -1.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return indentation or -1 if the line is white
    */
    public static int getRowIndent(BaseDocument doc, int offset)
    throws BadLocationException {
        offset = getRowFirstNonWhite(doc, offset);
        if (offset == -1) {
            return -1;
        }
        return doc.op.getVisColFromPos(offset);
    }

    /** Get indentation on the current line. If this line is white then
    * go either up or down an return indentation of the first non-white row.
    * The <tt>getRowFirstNonWhite()</tt> is used to find the indentation
    * on particular line.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @param downDir if this flag is set to true then if the row is white
    *   then the indentation of the next first non-white row is returned. If it's
    *   false then the indentation of the previous first non-white row is returned.
    * @return indentation or -1 if there's no non-white line in the specified direction
    */
    public static int getRowIndent(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        int p = getRowFirstNonWhite(doc, offset);
        if (p == -1) {
            p = getFirstNonWhiteRow(doc, offset, downDir);
            if (p == -1) {
                return -1; // non-white line not found
            }
            p = getRowFirstNonWhite(doc, p);
            if (p == -1) {
                return -1; // non-white line not found
            }
        }
        return doc.op.getVisColFromPos(p);
    }

    /** Get the end position of the row right before the new-line character.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @param relLine shift offset forward/back by some amount of lines
    * @return position of the end of the row or -1 for invalid position
    */
    public static int getRowEnd(JTextComponent c, int offset)
    throws BadLocationException {
        return getRowEnd((BaseDocument)c.getDocument(), offset);
    }

    public static int getRowEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.op.getEOL(offset);
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param doc document to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    */
    public static int getPositionAbove(JTextComponent c, int offset, int x)
    throws BadLocationException {
        BaseDocument doc = (BaseDocument)c.getDocument();
        BaseTextUI ui = (BaseTextUI)c.getUI();
        offset = ui.viewToModel(c, x, ui.getYFromPos(offset) - ui.getExtUI().charHeight);
        return offset;
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    */
    public static int getPositionBelow(JTextComponent c, int offset, int x)
    throws BadLocationException {
        BaseDocument doc = (BaseDocument)c.getDocument();
        BaseTextUI ui = (BaseTextUI)c.getUI();
        offset = ui.viewToModel(c, x, ui.getYFromPos(offset) + ui.getExtUI().charHeight);
        return offset;
    }

    /** Get start of the current word. If there are no more words till
    * the begining of the document, this method returns -1.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    */
    public static int getWordStart(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordStart((BaseDocument)c.getDocument(), offset);
    }

    public static int getWordStart(BaseDocument doc, int offset)
    throws BadLocationException {
        int docLen = doc.getLength();
        return doc.find(new FinderFactory.PreviousWordBwdFinder(doc, false, true),
                        offset, 0);
    }

    public static int getWordEnd(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordEnd((BaseDocument)c.getDocument(), offset);
    }

    public static int getWordEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.find(new FinderFactory.NextWordFwdFinder(doc, false, true),
                        offset, -1);
    }

    public static int getNextWord(JTextComponent c, int offset)
    throws BadLocationException {
        return getNextWord((BaseDocument)c.getDocument(), offset);
    }

    public static int getNextWord(BaseDocument doc, int offset)
    throws BadLocationException {
        Finder nextWordFinder = (Finder)doc.getProperty(Settings.NEXT_WORD_FINDER);
        offset = doc.find(nextWordFinder, offset, -1);
        if (offset < 0) {
            offset = doc.getLength();
        }
        return offset;
    }

    public static int getPreviousWord(JTextComponent c, int offset)
    throws BadLocationException {
        return getPreviousWord((BaseDocument)c.getDocument(), offset);
    }

    public static int getPreviousWord(BaseDocument doc, int offset)
    throws BadLocationException {
        Finder prevWordFinder = (Finder)doc.getProperty(Settings.PREVIOUS_WORD_FINDER);
        offset = doc.find(prevWordFinder, offset, 0);
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstWhiteFwd(doc, offset, -1);
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.WhiteFwdFinder(doc), offset, limitPos);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteFwd(doc, offset, -1);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.NonWhiteFwdFinder(doc), offset, limitPos);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstWhiteBwd(doc, offset, 0);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.WhiteBwdFinder(doc), offset, limitPos);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteBwd(doc, offset, 0);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.NonWhiteBwdFinder(doc), offset, limitPos);
    }

    /** Return line offset (line number - 1) for some position in the document
    * @param doc document to operate on
    * @param offset position in document where to start searching
    */
    public static int getLineOffset(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.op.getLine(offset);
    }

    /** Get the total count of lines in the document */
    public static int getLineCount(BaseDocument doc) {
        return doc.op.getLineCount();
    }

    /** Return start offset of the line
    * @param lineOffset line offset starting from 0
    * @return start position of the line or -1 if lineOffset was invalid
    */
    public static int getRowStartFromLineOffset(BaseDocument doc, int lineOffset) {
        return doc.op.getBOLFromLine(lineOffset);
    }

    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.op.getVisColFromPos(offset);
    }

    /** Get the identifier around the given position or null if there's no identifier
    * @see getIdentifierBlock()
    */
    public static String getIdentifier(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] blk = getIdentifierBlock(doc, offset);
        return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
    }

    /** Get the identifier around the given position or null if there's no identifier
    * around the given position. The identifier must be
    * accepted by SyntaxSupport.isIdnetifier() otherwise null is returned.
    * @param doc document to work on
    * @param offset position in document - usually the caret.getDot()
    * @return the block (starting and ending position) enclosing the identifier
    *   or null if no identifier was found
    */
    public static int[] getIdentifierBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] ret = null;
        int idStart = getWordStart(doc, offset);
        if (idStart >= 0) {
            int idEnd = getWordEnd(doc, idStart);
            if (idEnd >= 0) {
                String id = doc.getText(idStart, idEnd - idStart);
                if (doc.getSyntaxSupport().isIdentifier(id)) {
                    ret = new int[] { idStart, idEnd };
                } else { // not identifier by syntax support
                    id = getWord(doc, offset); // try right at offset
                    if (doc.getSyntaxSupport().isIdentifier(id)) {
                        ret = new int[] { offset, offset + id.length() };
                    }
                }
            }
        }
        return ret;
    }

    /** Get the identifier before the given position (ending at given offset)
    * or null if there's no identifier
    */
    public static String getIdentifierBefore(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordStart = getWordStart(doc, offset);
        if (wordStart != -1) {
            String word = new String(doc.getChars(wordStart,
                                                  offset - wordStart), 0, offset - wordStart);
            if (doc.getSyntaxSupport().isIdentifier(word)) {
                return word;
            }
        }
        return null;
    }

    /** Get the selection if there's any or get the identifier around
    * the position if there's no selection.
    */
    public static String getSelectionOrIdentifier(JTextComponent c, int offset)
    throws BadLocationException {
        BaseDocument doc = (BaseDocument)c.getDocument();
        Caret caret = c.getCaret();
        String ret;
        if (caret.isSelectionVisible()) {
            ret = c.getSelectedText();
        } else {
            ret = Utilities.getIdentifier(doc, caret.getDot());
        }
        return ret;
    }

    /** Get the selection or identifier at the current caret position */
    public static String getSelectionOrIdentifier(JTextComponent c) {
        try {
            return getSelectionOrIdentifier(c, c.getCaret().getDot());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /** Get the word at given position.
    */
    public static String getWord(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordEnd = getWordEnd(doc, offset);
        if (wordEnd != -1) {
            return new String(doc.getChars(offset, wordEnd - offset), 0,
                              wordEnd - offset);
        }
        return null;
    }


    /** Change the case for specified part of document
    * @param doc document to operate on
    * @param offset position in document determines the changed area begining
    * @param len number of chars to change
    * @param type either CASE_CAPITAL, CASE_SMALL or CASE_SWITCH
    */
    public static boolean changeCase(BaseDocument doc, int offset, int len, int type)
    throws BadLocationException {
        char[] orig = doc.getChars(offset, len);
        char[] changed = (char[])orig.clone();
        for (int i = 0; i < orig.length; i++) {
            switch (type) {
            case CASE_UPPER:
                changed[i] = Character.toUpperCase(orig[i]);
                break;
            case CASE_LOWER:
                changed[i] = Character.toLowerCase(orig[i]);
                break;
            case CASE_SWITCH:
                if (Character.isUpperCase(orig[i])) {
                    changed[i] = Character.toLowerCase(orig[i]);
                } else if (Character.isLowerCase(orig[i])) {
                    changed[i] = Character.toUpperCase(orig[i]);
                }
                break;
            }
        }
        // check chars for difference and possibly change document
        for (int i = 0; i < orig.length; i++) {
            if (orig[i] != changed[i]) {
                doc.atomicLock();
                try {
                    doc.insertString(offset + orig.length, new String(changed), null);
                    doc.remove(offset, orig.length);
                } finally {
                    doc.atomicUnlock();
                }
                return true; // changed
            }
        }
        return false;
    }

    /** Tests whether the line contains no characters except the ending new-line.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    */
    public static boolean isRowEmpty(BaseDocument doc, int offset)
    throws BadLocationException {
        return (doc.op.getBOL(offset) == doc.op.getEOL(offset));
    }

    public static int getFirstNonEmptyRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        while (offset != -1 && isRowEmpty(doc, offset)) {
            offset = getRowStart(doc, offset, downDir ? +1 : -1);
        }
        return offset;
    }

    /** Tests whether the line contains only whitespace characters.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    */
    public static boolean isRowWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        offset = doc.find(new FinderFactory.NonWhiteFwdFinder(doc),
                          doc.op.getBOL(offset), doc.op.getEOL(offset));
        return (offset == -1);
    }

    public static int getFirstNonWhiteRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        if (isRowWhite(doc, offset)) {
            if (downDir) { // search down for non-white line
                offset = getFirstNonWhiteFwd(doc, offset);
            } else { // search up for non-white line
                offset = getFirstNonWhiteBwd(doc, offset);
            }
        }
        return offset;
    }

    /** Get indentation on the line that conatins given position.
    * @param doc document to operate on
    * @param offset position in documenta while
    * @return indentation on the current line or -1 if there's no text
    * on the line and the stopOnEOL is false
    */
    public static Formatter getFormatter(BaseDocument doc) {
        return Formatter.getFormatter(doc.kitClass);
    }

    /** Count of rows between these two positions */
    public static int getRowCount(BaseDocument doc, int startPos, int endPos)
    throws BadLocationException {
        if (startPos > endPos) {
            return 0;
        }
        return doc.op.getLine(endPos) - doc.op.getLine(startPos) + 1;
    }

    public static String getTabInsertString(BaseDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        Formatter f = getFormatter(doc);
        boolean expandTabs = f.expandTabs();
        if (expandTabs) {
            int spacesPerTab = f.getSpacesPerTab();
            int len = (col + spacesPerTab) / spacesPerTab * spacesPerTab - col;
            return new String(Analyzer.getSpacesBuffer(len), 0, len);
        } else { // insert pure tab
            return "\t"; // NOI18N
        }
    }

    public static int getNextTabColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        Formatter f = getFormatter(doc);
        boolean expandTabs = f.expandTabs();
        int tabSize = expandTabs ? f.getSpacesPerTab() : doc.getTabSize();
        return (col + tabSize) / tabSize * tabSize;
    }

    public static void setStatusText(JTextComponent c, String text) {
        StatusBar sb = getExtUI(c).getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text);
        }
    }

    public static void setStatusText(JTextComponent c, String text,
                                     Coloring extraColoring) {
        StatusBar sb = getExtUI(c).getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
        }
    }

    public static void setStatusBoldText(JTextComponent c, String text) {
        StatusBar sb = getExtUI(c).getStatusBar();
        if (sb != null) {
            sb.setBoldText(StatusBar.CELL_MAIN, text);
        }
    }

    public static String getStatusText(JTextComponent c) {
        StatusBar sb = getExtUI(c).getStatusBar();
        return (sb != null) ? sb.getText(StatusBar.CELL_MAIN) : null;
    }

    public static void clearStatusText(JTextComponent c) {
        setStatusText(c, ""); // NOI18N
    }

    public static void insertMark(BaseDocument doc, Mark mark, int offset)
    throws BadLocationException, InvalidMarkException {
        doc.op.insertMark(mark, offset);
    }

    public static void moveMark(BaseDocument doc, Mark mark, int newOffset)
    throws BadLocationException, InvalidMarkException {
        doc.op.moveMark(mark, newOffset);
    }

    public static void returnFocus() {
                                 JTextComponent c = getLastActiveComponent();
                                 if (c != null) {
                                     requestFocus(c);
                                 }
                             }

                             public static void requestFocus(JTextComponent c) {
                                 if (c != null) {
                                     boolean ok = false;
                                     BaseKit kit = getKit(c);
                                     if (kit != null) {
                                         Class fcc = kit.getFocusableComponentClass(c);
                                         if (fcc != null) {
                                             Container container = SwingUtilities.getAncestorOfClass(fcc, c);
                                             if (container != null) {
                                                 container.requestFocus();
                                                 ok = true;
                                             }
                                         }
                                     }

                                     if (!ok) {
                                         Frame f = ExtUI.getParentFrame(c);
                                         if (f != null) {
                                             f.requestFocus();
                                         }
                                         c.requestFocus();
                                     }
                                 }
                             }

                             public static void runInEventDispatchThread(Runnable r) {
                                 if (SwingUtilities.isEventDispatchThread()) {
                                     r.run();
                                 } else {
                                     SwingUtilities.invokeLater(r);
                                 }
                             }

                             public static String debugPosition(BaseDocument doc, int offset) {
                                 String ret;

                                 if (offset >= 0) {
                                     try {
                                         int line = getLineOffset(doc, offset) + 1;
                                         int col = getVisualColumn(doc, offset) + 1;
                                         ret = String.valueOf(line) + ":" + String.valueOf(col); // NOI18N
                                     } catch (BadLocationException e) {
                                         ret = LocaleSupport.getString(WRONG_POSITION_LOCALE, WRONG_POSITION_DEFAULT)
                                               + ' ' + offset + " > " + doc.getLength(); // NOI18N
                                     }
                                 } else {
                                     ret = String.valueOf(offset);
                                 }

                                 return ret;
                             }

                             public static void performAction(Action a, ActionEvent evt, JTextComponent target) {
                                 if (a instanceof BaseAction) {
                                     ((BaseAction)a).actionPerformed(evt, target);
                                 } else {
                                     a.actionPerformed(evt);
                                 }
                             }

                             /** Get the height of the editor line */
                             public static int getLineHeight(JTextComponent target) {
                                 return getExtUI(target).charHeight;
                             }

                             public static JTextComponent getLastActiveComponent() {
                                 return Registry.getMostActiveComponent();
                             }

                             /** Helper method to obtain instance of ExtUI (extended UI)
                             * from the existing JTextComponent.
                             * It doesn't require any document locking.
                             * @param target JTextComponent for which the extended UI should be obtained
                             * @return extended ui instance
                             */
                             public static ExtUI getExtUI(JTextComponent target) {
                                 return ((BaseTextUI)target.getUI()).getExtUI();
                             }

                             /** Helper method to obtain instance of editor kit from existing JTextComponent.
                             * If the kit of the component is not an instance
                             * of the <tt>org.netbeans.editor.BaseKit</tt> the method returns null.
                             * The method doesn't require any document locking.
                             * @param target JTextComponent for which the editor kit should be obtained
                             * @return BaseKit instance or null
                             */
                             public static BaseKit getKit(JTextComponent target) {
                                 EditorKit ekit = target.getUI().getEditorKit(target);
                                 return (ekit instanceof BaseKit) ? (BaseKit)ekit : null;
                             }

                             /** Helper method to obtain editor kit class from existing JTextComponent.
                             * This method is useful for example when dealing with Settings.
                             * The method doesn't require any document locking.
                             * @param target JTextComponent for which the editor kit should be obtained
                             * @return editor kit class
                             */
                             public static Class getKitClass(JTextComponent target) {
                                 return (target != null) ? target.getUI().getEditorKit(target).getClass() : null;
                             }

                             /** Helper method to obtain instance of BaseDocument from JTextComponent.
                             * If the document of the component is not an instance
                             * of the <tt>org.netbeans.editor.BaseDocument</tt> the method returns null.
                             * The method doesn't require any document locking.
                             * @param target JTextComponent for which the document should be obtained
                             * @return BaseDocument instance or null
                             */
                             public static BaseDocument getDocument(JTextComponent target) {
                                 Document doc = target.getDocument();
                                 return (doc instanceof BaseDocument) ? (BaseDocument)doc : null;
                             }

                             /** Get the syntax-support class that belongs to the document of the given
                             * component. Besides using directly this method, the <tt>SyntaxSupport</tt>
                             * can be obtained by calling <tt>doc.getSyntaxSupport()</tt>.
                             * The method can return null in case the document is not
                             * an instance of the BaseDocument.
                             * The method doesn't require any document locking.
                             * @param target JTextComponent for which the syntax-support should be obtained
                             * @return SyntaxSupport instance or null
                             */
                             public static SyntaxSupport getSyntaxSupport(JTextComponent target) {
                                 Document doc = target.getDocument();
                                 return (doc instanceof BaseDocument) ? ((BaseDocument)doc).getSyntaxSupport() : null;
                             }

                         }

                         /*
                          * Log
                          *  35   Gandalf-post-FCS1.32.1.1    4/3/00   Miloslav Metelka undo update
                          *  34   Gandalf-post-FCS1.32.1.0    3/8/00   Miloslav Metelka 
                          *  33   Gandalf   1.32        1/16/00  Miloslav Metelka 
                          *  32   Gandalf   1.31        1/13/00  Miloslav Metelka 
                          *  31   Gandalf   1.30        1/11/00  Miloslav Metelka 
                          *  30   Gandalf   1.29        1/4/00   Miloslav Metelka 
                          *  29   Gandalf   1.28        12/28/99 Miloslav Metelka 
                          *  28   Gandalf   1.27        11/14/99 Miloslav Metelka 
                          *  27   Gandalf   1.26        11/9/99  Miloslav Metelka 
                          *  26   Gandalf   1.25        11/8/99  Miloslav Metelka 
                          *  25   Gandalf   1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
                          *       Microsystems Copyright in File Comment
                          *  24   Gandalf   1.23        10/10/99 Miloslav Metelka 
                          *  23   Gandalf   1.22        9/22/99  Miloslav Metelka changeCase() 
                          *       insert/remove inverted
                          *  22   Gandalf   1.21        9/15/99  Miloslav Metelka 
                          *  21   Gandalf   1.20        9/10/99  Miloslav Metelka 
                          *  20   Gandalf   1.19        8/27/99  Miloslav Metelka 
                          *  19   Gandalf   1.18        8/17/99  Miloslav Metelka 
                          *  18   Gandalf   1.17        7/26/99  Miloslav Metelka 
                          *  17   Gandalf   1.16        7/22/99  Miloslav Metelka 
                          *  16   Gandalf   1.15        7/9/99   Miloslav Metelka 
                          *  15   Gandalf   1.14        6/29/99  Miloslav Metelka Scrolling and patches
                          *  14   Gandalf   1.13        6/25/99  Miloslav Metelka from floats back to ints
                          *  13   Gandalf   1.12        6/10/99  Miloslav Metelka 
                          *  12   Gandalf   1.11        6/8/99   Miloslav Metelka 
                          *  11   Gandalf   1.10        6/1/99   Miloslav Metelka 
                          *  10   Gandalf   1.9         5/15/99  Miloslav Metelka fixes
                          *  9    Gandalf   1.8         5/7/99   Miloslav Metelka line numbering and fixes
                          *  8    Gandalf   1.7         5/5/99   Miloslav Metelka 
                          *  7    Gandalf   1.6         4/23/99  Miloslav Metelka Undo added and internal 
                          *       improvements
                          *  6    Gandalf   1.5         4/8/99   Miloslav Metelka 
                          *  5    Gandalf   1.4         4/1/99   Miloslav Metelka 
                          *  4    Gandalf   1.3         3/30/99  Miloslav Metelka 
                          *  3    Gandalf   1.2         3/27/99  Miloslav Metelka 
                          *  2    Gandalf   1.1         3/23/99  Miloslav Metelka 
                          *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
                          * $
                          */

