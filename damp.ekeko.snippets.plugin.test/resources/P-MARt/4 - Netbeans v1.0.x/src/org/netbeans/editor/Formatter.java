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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.Writer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

/** Various services related to indentation and text formatting
* are located here. Each kit can have different formatter
* so the first action should be getting the right formatter by calling
* Formatter.getFormatter(kitClass).
*
* @author Miloslav Metelka
* @version 1.00
*/

public class Formatter {
    /**
     * @associates Formatter 
     */
    private static Map kitFormatters = new HashMap();

    /** Listener to changes in settings */
    private static SettingsChangeListener settingsListener;

    static {
        settingsListener = new SettingsChangeListener() {
                               public void settingsChange(SettingsChangeEvent evt) {
                                   String settingName = (evt != null) ? evt.getSettingName() : null;
                                   synchronized (Formatter.class) {
                                       if (Settings.FORMATTER.equals(settingName)) {
                                           kitFormatters.clear();
                                       } else { // other settings were changed
                                           Iterator it = kitFormatters.entrySet().iterator();
                                           while (it.hasNext()) {
                                               Map.Entry me = (Map.Entry)it.next();
                                               Formatter f = (Formatter)me.getValue();
                                               f.settingsChange(evt, (Class)me.getKey());
                                           }
                                       }
                                   }
                               }
                           };
        Settings.addSettingsChangeListener(settingsListener);
    }

    public static synchronized Formatter getFormatter(Class kitClass) {
        if (kitClass == null) {
            kitClass = BaseKit.class;
        }
        Formatter f = (Formatter)kitFormatters.get(kitClass);
        if (f == null) {
            f = (Formatter)Settings.getValue(kitClass, Settings.FORMATTER);
            if (f == null) {
                f = new Formatter(); // create basic indentation
            }
            f.settingsChange(null, kitClass);
            kitFormatters.put(kitClass, f);
        }
        return f;
    }

    private int tabSize;

    private int shiftWidth;

    private boolean expandTabs;

    private int spacesPerTab;

    private Acceptor hotCharAcceptor;


    protected void settingsChange(SettingsChangeEvent evt, Class kitClass) {
        String settingName = (evt != null) ? evt.getSettingName() : null;
        if (settingName == null || Settings.TAB_SIZE.equals(settingName)) {
            tabSize = SettingsUtil.getInteger(kitClass, Settings.TAB_SIZE,
                                              DefaultSettings.defaultTabSize);
        }
        //    if (settingName == null || Settings.INDENT_SHIFT_WIDTH.equals(settingName)) {
        // depend on expand tabs etc.
        shiftWidth = SettingsUtil.getInteger(kitClass, Settings.INDENT_SHIFT_WIDTH,
                                             DefaultSettings.defaultShiftWidth);
        //    }
        if (settingName == null || Settings.EXPAND_TABS.equals(settingName)) {
            expandTabs = SettingsUtil.getBoolean(kitClass, Settings.EXPAND_TABS, false);
        }
        if (settingName == null || Settings.SPACES_PER_TAB.equals(settingName)) {
            spacesPerTab = SettingsUtil.getInteger(kitClass, Settings.SPACES_PER_TAB,
                                                   DefaultSettings.defaultSpacesPerTab);
        }
        if (settingName == null || Settings.INDENT_HOT_CHAR_ACCEPTOR.equals(settingName)) {
            hotCharAcceptor = SettingsUtil.getAcceptor(kitClass, Settings.INDENT_HOT_CHAR_ACCEPTOR,
                              AcceptorFactory.FALSE);
        }
    }

    public int getTabSize() {
        return tabSize;
    }

    /** Get size of one indentation level */
    public int getShiftWidth() {
        return shiftWidth;
    }

    public boolean expandTabs() {
        return expandTabs;
    }

    public int getSpacesPerTab() {
        return spacesPerTab;
    }

    /** Is the given character an indentation hotcharacter? */
    public boolean isHotChar(char ch) {
        return hotCharAcceptor.accept(ch);
    }

    public char[] getIndentChars(int indent) {
        return Analyzer.getIndentChars(indent, expandTabs(), getTabSize());
    }

    /** Change the indent of the given row. Document is atomically locked
    * during this operation.
    */
    public void changeRowIndent(BaseDocument doc, int pos, int newIndent)
    throws BadLocationException {
        doc.atomicLock();
        try {
            if (newIndent < 0) {
                newIndent = 0;
            }
            int firstNW = Utilities.getRowFirstNonWhite(doc, pos);
            if (firstNW == -1) { // valid first non-blank
                firstNW = Utilities.getRowEnd(doc, pos);
            }
            int bolPos = Utilities.getRowStart(doc, pos);
            doc.remove(bolPos, firstNW - bolPos); // !!! indent by spaces/tabs
            char[] fillBuf = getIndentChars(newIndent);
            doc.insertString(bolPos, new String(fillBuf), null);
        } finally {
            doc.atomicUnlock();
        }
    }

    /** Increase/decrease indentation of the block of the code. Document
    * is atomically locked during the operation.
    * @param doc document to operate on
    * @param startPos starting line position
    * @param endPos ending line position
    * @param shiftCnt positive/negative count of shiftwidths by which indentation
    *   should be shifted right/left
    */
    public void changeBlockIndent(BaseDocument doc, int startPos, int endPos,
                                  int shiftCnt) throws BadLocationException {
        doc.atomicLock();
        try {

            int indentDelta = shiftCnt * getShiftWidth();
            if (endPos > 0 && Utilities.getRowStart(doc, endPos) == endPos) {
                endPos--;
            }

            int pos = startPos;
            for (int lineCnt = Utilities.getRowCount(doc, startPos, endPos);
                    lineCnt > 0; lineCnt--
                ) {
                int indent = Utilities.getRowIndent(doc, pos);
                if (Utilities.isRowWhite(doc, pos)) {
                    indent = -indentDelta; // zero indentation for white line
                }
                changeRowIndent(doc, pos, Math.max(indent + indentDelta, 0));
                pos = Utilities.getRowStart(doc, pos, +1);
            }

        } finally {
            doc.atomicUnlock();
        }
    }

    /** Change row indent by the appropriate indentation engine */
    public void updateRowIndent(BaseDocument doc, int pos)
    throws BadLocationException {
        int bolPos = Utilities.getRowStart(doc, pos);
        int indentPos = indentLine(doc, bolPos);
        changeRowIndent(doc, bolPos, indentPos - bolPos);
    }

    /** Update indentation of the block of the code. Document
    * is atomically locked during the operation.
    * @param doc document to operate on
    * @param startPos starting line position
    * @param endPos ending line position
    * @param shiftCnt positive/negative count of shiftwidths by which indentation
    *   should be shifted right/left
    */
    public void updateBlockIndent(BaseDocument doc, int startPos, int endPos)
    throws BadLocationException {
        doc.atomicLock();
        try {
            Writer origWriter = new java.io.StringWriter();
            Writer w = createWriter(doc, startPos, origWriter);
            char[] chars = doc.getChars(startPos, endPos - startPos);
            w.write(chars, 0, chars.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            doc.atomicUnlock();
        }
    }

    public void shiftLine(BaseDocument doc, int dotPos, boolean right)
    throws BadLocationException {
        int ind = getShiftWidth();
        if (!right) {
            ind = -ind;
        }

        if (Utilities.isRowWhite(doc, dotPos)) {
            ind += dotPos - Utilities.getRowStart(doc, dotPos);
        } else {
            ind += Utilities.getRowIndent(doc, dotPos);
        }
        ind = Math.max(ind, 0);
        changeRowIndent(doc, dotPos, ind);
    }

    /** Indents the current line. Should not affect any other
    * lines.
    * @param doc the document to work on
    * @param pos the pos of a character on the line
    * @return new pos of the original character
    */
    public int indentLine (Document d, int pos) {
        if (d instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)d;
            try {
                int nwPos = Utilities.getRowFirstNonWhite(doc, pos);
                if (nwPos >= 0) {
                    pos = nwPos;
                }
            } catch (BadLocationException e) {
            }
        }
        return pos;
    }

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param pos the pos of a character on the line
    * @return new pos to place cursor to
    */
    public int indentNewLine (Document d, int pos) {
        if (d instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument)d;
            boolean newLineInserted = false;

            doc.atomicLock();
            try {
                doc.insertString(pos, "\n", null); // NOI18N
                pos++;
                newLineInserted = true;
                int indentPos = indentLine(doc, pos);
                changeRowIndent(doc, pos, indentPos - pos);
                pos = indentPos;
            } catch (GuardedException e) {
                // possibly couldn't insert additional indentation
                // at the begining of the guarded block
                // but the initial '\n' could be fine
                if (!newLineInserted) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            } finally {
                doc.atomicUnlock();
            }

        } else { // not BaseDocument
            try {
                d.insertString (pos, "\n", null); // NOI18N
                pos++;
            } catch (BadLocationException ex) {
            }
        }

        return pos;
    }

    /** Creates writer that formats text that is inserted into it.
    * The writer should not modify the document but use the 
    * provided writer to write to. Usually the underlaying writer
    * will modify the document itself and optionally it can remember
    * the current position in document. That is why the newly created
    * writer should do no buffering.
    * <P>
    * The provided document and pos are only informational,
    * should not be modified but only used to find correct indentation
    * strategy.
    *
    * @param doc document 
    * @param pos position to begin inserts at
    * @param writer writer to write to
    * @return new writer that will format written text and pass it
    *   into the writer
    */
    public Writer createWriter (Document doc, int pos, Writer writer) {
        return writer;
    }

}

/*
 * Log
 *  16   Gandalf-post-FCS1.11.1.3    4/18/00  Miloslav Metelka shift-width refresh
 *  15   Gandalf-post-FCS1.11.1.2    4/6/00   Miloslav Metelka fixed getTabSize()
 *  14   Gandalf-post-FCS1.11.1.1    4/3/00   Miloslav Metelka undo update
 *  13   Gandalf-post-FCS1.11.1.0    3/8/00   Miloslav Metelka 
 *  12   Gandalf   1.11        1/13/00  Miloslav Metelka 
 *  11   Gandalf   1.10        1/10/00  Miloslav Metelka 
 *  10   Gandalf   1.9         1/6/00   Miloslav Metelka 
 *  9    Gandalf   1.8         11/14/99 Miloslav Metelka 
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/15/99  Miloslav Metelka 
 *  6    Gandalf   1.5         9/10/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/19/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

