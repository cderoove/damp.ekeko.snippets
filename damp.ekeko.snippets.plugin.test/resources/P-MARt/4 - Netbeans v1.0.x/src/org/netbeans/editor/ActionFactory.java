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
import java.awt.event.ActionEvent;
import java.io.Writer;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.Caret;
import javax.swing.text.Position;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
* Actions that are not considered basic and therefore
* they are not included directly in BaseKit, but here.
* Their names however are still part of BaseKit.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ActionFactory {

    private ActionFactory() {
        // no instantiation
    }

    public static class RemoveTabAction extends BaseAction {

        static final long serialVersionUID =-1537748600593395706L;

        public RemoveTabAction() {
            super(BaseKit.removeTabAction, MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                if (caret.isSelectionVisible()) { // block selected
                    try {
                        Utilities.getFormatter(doc).changeBlockIndent(doc,
                                target.getSelectionStart(), target.getSelectionEnd(), -1);
                    } catch (GuardedException e) {
                        target.getToolkit().beep();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                } else { // no selected text
                    // !!! TO DO
                }
            }

        }

    }

    public static class RemoveWordAction extends BaseAction {

        static final long serialVersionUID =9193117196412195554L;

        public RemoveWordAction() {
            super(BaseKit.removeWordAction, MAGIC_POSITION_RESET
                  | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                try {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dotPos = caret.getDot();
                    int bolPos = Utilities.getRowStart(doc, dotPos);
                    int wsPos = Utilities.getPreviousWord(target, dotPos);
                    wsPos = (dotPos == bolPos) ? wsPos : Math.max(bolPos, wsPos);
                    doc.remove(wsPos, dotPos - wsPos);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class RemoveLineBeginAction extends BaseAction {

        static final long serialVersionUID =9193117196412195554L;

        public RemoveLineBeginAction() {
            super(BaseKit.removeLineBeginAction, MAGIC_POSITION_RESET
                  | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                try {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dotPos = caret.getDot();
                    int bolPos = Utilities.getRowStart(doc, dotPos);
                    if (dotPos == bolPos) { // at begining of the line
                        if (dotPos > 0) {
                            doc.remove(dotPos - 1, 1); // remove previous new-line
                        }
                    } else { // not at the line begining
                        if (Analyzer.isWhitespace(doc.getChars(bolPos, dotPos - bolPos))) {
                            doc.remove(bolPos, dotPos - bolPos); // remove whitespace
                        } else {
                            int firstNW = Utilities.getRowFirstNonWhite(doc, bolPos);
                            if (firstNW >= 0 && firstNW < dotPos) {
                                doc.remove(firstNW, dotPos - firstNW);
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class RemoveLineAction extends BaseAction {

        static final long serialVersionUID =-536315497241419877L;

        public RemoveLineAction() {
            super(BaseKit.removeLineAction, MAGIC_POSITION_RESET
                  | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                try {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dotPos = caret.getDot();
                    int bolPos = Utilities.getRowStart(target, dotPos);
                    int eolPos = Utilities.getRowEnd(target, dotPos);
                    eolPos = Math.min(eolPos + 1, doc.getLength()); // include '\n'
                    doc.remove(bolPos, eolPos - bolPos);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /* Useful for popup menu - remove selected block or do nothing */
    public static class RemoveSelectionAction extends BaseAction {

        static final long serialVersionUID =-1419424594746686573L;

        public RemoveSelectionAction() {
            super(BaseKit.removeSelectionAction, MAGIC_POSITION_RESET
                  | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            putValue ("helpID", RemoveSelectionAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                target.replaceSelection(null);
            }
        }
    }

    /** Switch to overwrite mode or back to insert mode */
    static class ToggleTypingModeAction extends BaseAction {

        static final long serialVersionUID =-2431132686507799723L;

        ToggleTypingModeAction() {
            super(BaseKit.toggleTypingModeAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                ExtUI extUI = Utilities.getExtUI(target);
                Boolean overwriteMode = (Boolean)extUI.getProperty(ExtUI.OVERWRITE_MODE_PROPERTY);
                // Now toggle
                overwriteMode = (overwriteMode == null || !overwriteMode.booleanValue())
                                ? Boolean.TRUE : Boolean.FALSE;
                extUI.putProperty(ExtUI.OVERWRITE_MODE_PROPERTY, overwriteMode);
            }
        }
    }

    static class ToggleBookmarkAction extends BaseAction {

        static final long serialVersionUID =-8438899482709646741L;

        ToggleBookmarkAction() {
            super(BaseKit.toggleBookmarkAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    doc.toggleBookmark(caret.getDot());
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    static class GotoNextBookmarkAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-5169554640178645108L;

        GotoNextBookmarkAction(String nm, boolean select) {
            super(BaseKit.gotoNextBookmarkAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int dotPos = doc.getNextBookmark(caret.getDot(), true); // wrap
                    if (dotPos >= 0) {
                        if (select) {
                            caret.moveDot(dotPos);
                        } else {
                            caret.setDot(dotPos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    static class AbbrevExpandAction extends BaseAction {

        static final long serialVersionUID =-2124569510083544403L;

        AbbrevExpandAction() {
            super(BaseKit.abbrevExpandAction,
                  MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                ExtUI extUI = ((BaseTextUI)target.getUI()).getExtUI();
                try {
                    extUI.getAbbrev().checkAndExpand(evt);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class AbbrevResetAction extends BaseAction {

        static final long serialVersionUID =-2807497346060448395L;

        public AbbrevResetAction() {
            super(BaseKit.abbrevResetAction, ABBREV_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    static class ChangeCaseAction extends BaseAction {

        int changeCaseMode;

        static final long serialVersionUID =5680212865619897402L;

        ChangeCaseAction(String name, int changeCaseMode) {
            super(name, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.changeCaseMode = changeCaseMode;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    if (caret.isSelectionVisible()) { // valid selection
                        int startPos = target.getSelectionStart();
                        int endPos = target.getSelectionEnd();
                        Utilities.changeCase(doc, startPos, endPos - startPos, changeCaseMode);
                        caret.setSelectionVisible(false);
                        caret.setDot(endPos);
                    } else { // no selection - change current char
                        int dotPos = caret.getDot();
                        Utilities.changeCase(doc, dotPos, 1, changeCaseMode);
                        caret.setDot(dotPos + 1);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }


    static class ToggleCaseIdentifierBeginAction extends BaseAction {

        static final long serialVersionUID =584392193824931979L;

        ToggleCaseIdentifierBeginAction() {
            super(BaseKit.toggleCaseIdentifierBeginAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int[] idBlk = Utilities.getIdentifierBlock(doc, caret.getDot());
                    if (idBlk != null) {
                        Utilities.changeCase(doc, idBlk[0], 1, Utilities.CASE_SWITCH);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class FindNextAction extends BaseAction {

        static final long serialVersionUID =6878814427731642684L;

        public FindNextAction() {
            super(BaseKit.findNextAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                FindSupport.getFindSupport().find(null, false);
            }
        }
    }

    public static class FindPreviousAction extends BaseAction {

        static final long serialVersionUID =-43746947902694926L;

        public FindPreviousAction() {
            super(BaseKit.findPreviousAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                FindSupport.getFindSupport().find(null, true);
            }
        }
    }

    /** Finds either selection or if there's no selection it finds
    * the word where the cursor is standing.
    */
    public static class FindSelectionAction extends BaseAction {

        static final long serialVersionUID =-5601618936504699565L;

        public FindSelectionAction() {
            super(BaseKit.findSelectionAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                FindSupport findSupport = FindSupport.getFindSupport();
                Caret caret = target.getCaret();
                int dotPos = caret.getDot();
                HashMap props = new HashMap(findSupport.getFindProperties());
                String searchWord = null;

                if (caret.isSelectionVisible()) { // valid selection
                    searchWord = target.getSelectedText();
                    props.put(Settings.FIND_WHOLE_WORDS, Boolean.FALSE);
                } else { // no selection, get current word
                    try {
                        searchWord = Utilities.getIdentifier((BaseDocument)target.getDocument(),
                                                             dotPos);
                        props.put(Settings.FIND_WHOLE_WORDS, Boolean.TRUE);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }

                if (searchWord != null) {
                    props.put(Settings.FIND_WHAT, searchWord);
                    findSupport.putFindProperties(props);
                    findSupport.find(null, false);
                }
            }
        }
    }

    public static class ToggleHighlightSearchAction extends BaseAction {

        static final long serialVersionUID =4603809175771743200L;

        public ToggleHighlightSearchAction() {
            super(BaseKit.toggleHighlightSearchAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Boolean cur = (Boolean)FindSupport.getFindSupport().getFindProperty(
                                  Settings.FIND_HIGHLIGHT_SEARCH);
                if (cur == null || cur.booleanValue() == false) {
                    cur = Boolean.TRUE;
                } else {
                    cur = Boolean.FALSE;
                }
                FindSupport.getFindSupport().putFindProperty(
                    Settings.FIND_HIGHLIGHT_SEARCH, cur);
            }
        }
    }

    public static class UndoAction extends BaseAction {

        static final long serialVersionUID =8628586205035497612L;

        public UndoAction() {
            super(BaseKit.undoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            Document doc = target.getDocument();
            UndoableEdit undoMgr = (UndoableEdit)doc.getProperty(
                                       BaseDocument.UNDO_MANAGER_PROP);
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.undo();
                } catch (CannotUndoException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class RedoAction extends BaseAction {

        static final long serialVersionUID =6048125996333769202L;

        public RedoAction() {
            super(BaseKit.redoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            Document doc = target.getDocument();
            UndoableEdit undoMgr = (UndoableEdit)doc.getProperty(
                                       BaseDocument.UNDO_MANAGER_PROP);
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.redo();
                } catch (CannotRedoException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class WordMatchAction extends BaseAction {

        private boolean direction;

        static final long serialVersionUID =595571114685133170L;

        public WordMatchAction(String name, boolean direction) {
            super(name, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            this.direction = direction;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                ExtUI extUI = Utilities.getExtUI(target);
                Caret caret = target.getCaret();
                final BaseDocument doc = Utilities.getDocument(target);

                // Possibly remove selection
                if (caret.isSelectionVisible()) {
                    target.replaceSelection(null);
                }

                int dotPos = caret.getDot();
                String s = extUI.getWordMatch().getMatchWord(dotPos, direction);
                String prevWord = extUI.getWordMatch().getPreviousWord();
                if (s != null) {
                    doc.atomicLock();
                    try {
                        int pos = dotPos;
                        if (prevWord != null && prevWord.length() > 0) {
                            pos -= prevWord.length();
                            doc.remove(pos, prevWord.length());
                        }
                        doc.insertString(pos, s, null);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            }
        }
    }


    public static class BracketMatchAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-184887499045886231L;

        public BracketMatchAction(String name, boolean select) {
            super(name, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = Utilities.getDocument(target);
                    int dotPos = caret.getDot();
                    SyntaxSupport sup = doc.getSyntaxSupport();
                    if (dotPos > 0) {
                        int matchPos = sup.findMatchingBracket(dotPos - 1, false);
                        if (matchPos >= 0) {
                            if (select) {
                                caret.moveDot(matchPos + 1);
                            } else {
                                caret.setDot(matchPos + 1);
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class ShiftLineAction extends BaseAction {

        boolean right;

        static final long serialVersionUID =-5124732597493699582L;

        public ShiftLineAction(String name, boolean right) {
            super(name, MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            this.right = right;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = Utilities.getDocument(target);
                    if (caret.isSelectionVisible()) {
                        Utilities.getFormatter(doc).changeBlockIndent(doc,
                                target.getSelectionStart(), target.getSelectionEnd(),
                                right ? +1 : -1
                                                                     );
                    } else {
                        Utilities.getFormatter(doc).shiftLine(doc, caret.getDot(), right);
                    }
                } catch (GuardedException e) {
                    target.getToolkit().beep();
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class AdjustWindowAction extends BaseAction {

        int percentFromWindowTop;

        static final long serialVersionUID =8864278998999643292L;
        public AdjustWindowAction(String name, int percentFromWindowTop) {
            super(name);
            this.percentFromWindowTop = percentFromWindowTop;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getExtUI(target).adjustWindow(percentFromWindowTop);
            }
        }
    }

    public static class AdjustCaretAction extends BaseAction {

        int percentFromWindowTop;

        static final long serialVersionUID =3223383913531191066L;
        public AdjustCaretAction(String name, int percentFromWindowTop) {
            super(name);
            this.percentFromWindowTop = percentFromWindowTop;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getExtUI(target).adjustCaret(percentFromWindowTop);
            }
        }
    }

    public static class FormatAction extends BaseAction {

        static final long serialVersionUID =-7666172828961171865L;

        public FormatAction() {
            super(BaseKit.formatAction,
                  ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            putValue ("helpID", FormatAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                GuardedDocument gdoc = (doc instanceof GuardedDocument)
                                       ? (GuardedDocument)doc : null;

                doc.atomicLock();
                try {

                    int caretLine = Utilities.getLineOffset(doc, caret.getDot());
                    int startPos;
                    Position endPosition;
                    if (caret.isSelectionVisible()) {
                        startPos = target.getSelectionStart();
                        endPosition = doc.createPosition(target.getSelectionEnd());
                    } else {
                        startPos = 0;
                        endPosition = doc.createPosition(doc.getLength());
                    }

                    int pos = startPos;
                    if (gdoc != null) {
                        pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                    }

                    while (pos < endPosition.getOffset()) {
                        int stopPos = endPosition.getOffset();
                        if (gdoc != null) { // adjust to start of the next guarded block
                            stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos);
                            if (stopPos == -1) {
                                stopPos = endPosition.getOffset();
                            }
                        }

                        CharArrayWriter cw = new CharArrayWriter();
                        Writer w = Utilities.getFormatter(doc).createWriter(doc, pos, cw);
                        w.write(doc.getChars(pos, stopPos - pos));
                        w.close();

                        String out = new String(cw.toCharArray());
                        doc.remove(pos, stopPos - pos);
                        doc.insertString(pos, out, null);
                        pos += out.length(); // go to the end of the area inserted

                        if (gdoc != null) { // adjust to end of current block
                            pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                        }
                    }

                    // Restore the line
                    pos = Utilities.getRowStartFromLineOffset(doc, caretLine);
                    if (pos >= 0) {
                        caret.setDot(pos);
                    }
                } catch (GuardedException e) {
                    target.getToolkit().beep();
                } catch (BadLocationException e) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
                        e.printStackTrace();
                    }
                } finally {
                    doc.atomicUnlock();
                }

            }
        }
    }

    public static class FirstNonWhiteAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =-5888439539790901158L;

        public FirstNonWhiteAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos = Utilities.getRowFirstNonWhite((BaseDocument)target.getDocument(),
                                                            caret.getDot());
                    if (pos >= 0) {
                        if (select) {
                            caret.moveDot(pos);
                        } else {
                            caret.setDot(pos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class LastNonWhiteAction extends BaseAction {

        boolean select;

        static final long serialVersionUID =4503533041729712917L;

        public LastNonWhiteAction(String nm, boolean select) {
            super(nm, MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET);
            this.select = select;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos = Utilities.getRowLastNonWhite((BaseDocument)target.getDocument(),
                                                           caret.getDot());
                    if (pos >= 0) {
                        if (select) {
                            caret.moveDot(pos);
                        } else {
                            caret.setDot(pos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class SelectIdentifierAction extends BaseAction {

        static final long serialVersionUID =-7288216961333147873L;
        public SelectIdentifierAction() {
            super(BaseKit.selectIdentifierAction, MAGIC_POSITION_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    if (caret.isSelectionVisible()) {
                        caret.setSelectionVisible(false); // unselect if anything selected
                    } else { // selection not visible
                        int block[] = Utilities.getIdentifierBlock((BaseDocument)target.getDocument(),
                                      caret.getDot());
                        if (block != null) {
                            caret.setDot(block[0]);
                            caret.moveDot(block[1]);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class SelectNextParameterAction extends BaseAction {

        static final long serialVersionUID =8045372985336370934L;
        public SelectNextParameterAction() {
            super(BaseKit.selectNextParameterAction, MAGIC_POSITION_RESET | CLEAR_STATUS_TEXT);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = caret.getDot();
                int selectStartPos = -1;
                try {
                    if (dotPos > 0) {
                        if (doc.getChars(dotPos - 1, 1)[0] == ',') { // right after the comma
                            selectStartPos = dotPos;
                        }
                    }
                    if (dotPos < doc.getLength()) {
                        char dotChar = doc.getChars(dotPos, 1)[0];
                        if (dotChar == ',') {
                            selectStartPos = dotPos + 1;
                        } else if (dotChar == ')') {
                            caret.setDot(dotPos + 1);
                        }
                    }
                    if (selectStartPos >= 0) {
                        int selectEndPos = doc.find(
                                               new FinderFactory.CharArrayFwdFinder( new char[] { ',', ')' }),
                                               selectStartPos, -1
                                           );
                        if (selectEndPos >= 0) {
                            target.select(selectStartPos, selectEndPos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class JumpListNextAction extends BaseAction {

        static final long serialVersionUID =6891721278404990446L;
        public JumpListNextAction() {
            super(BaseKit.jumpListNextAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpNext(target);
            }
        }
    }

    public static class JumpListPrevAction extends BaseAction {

        static final long serialVersionUID =7174907031986424265L;
        public JumpListPrevAction() {
            super(BaseKit.jumpListPrevAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpPrev(target);
            }
        }
    }

    public static class JumpListNextComponentAction extends BaseAction {

        static final long serialVersionUID =-2059070050865876892L;
        public JumpListNextComponentAction() {
            super(BaseKit.jumpListNextComponentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpNextComponent(target);
            }
        }
    }

    public static class JumpListPrevComponentAction extends BaseAction {

        static final long serialVersionUID =2032230534727849525L;
        public JumpListPrevComponentAction() {
            super(BaseKit.jumpListPrevComponentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpPrevComponent(target);
            }
        }
    }

    public static class ScrollUpAction extends BaseAction {

        public ScrollUpAction() {
            super(BaseKit.scrollUpAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                ExtUI extUI = Utilities.getExtUI(target);
                Rectangle bounds = extUI.getExtentBounds();
                bounds.y += extUI.charHeight;
                extUI.scrollRectToVisible(bounds, ExtUI.SCROLL_SMALLEST);
            }
        }

    }

    public static class ScrollDownAction extends BaseAction {

        public ScrollDownAction() {
            super(BaseKit.scrollDownAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                ExtUI extUI = Utilities.getExtUI(target);
                Rectangle bounds = extUI.getExtentBounds();
                bounds.y -= extUI.charHeight;
                extUI.scrollRectToVisible(bounds, ExtUI.SCROLL_SMALLEST);
            }
        }

    }

    public static class BraceCodeSelectAction extends BaseAction {

        static final long serialVersionUID =4033474080778585860L;

        public BraceCodeSelectAction() {
            super(BaseKit.braceCodeSelectAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                BaseDocument doc = (BaseDocument)target.getDocument();
                SyntaxSupport sup = doc.getSyntaxSupport();
                Caret caret = target.getCaret();
                try {
                    int bracketPos = sup.findUnmatchedBracket(caret.getDot(), sup.getRightBrackets());
                    if (bracketPos >= 0) {
                        caret.setDot(bracketPos);
                        /*            while (true) {
                                      int bolPos = Utilities.getRowStart(doc, bracketPos);
                                      boolean isWSC = sup.isCommentOrWhitespace(bolPos, bracketPos);
                                      if (isWSC) { // get previous line end
                                        
                                      }
                        */
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class EscapeAction extends BaseAction {

        public EscapeAction() {
            super(BaseKit.escapeAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getExtUI(target).hidePopupMenu();
            }
        }
    }



}

/*
 * Log
 *  15   Gandalf-post-FCS1.11.1.2    4/6/00   Miloslav Metelka undo action
 *  14   Gandalf-post-FCS1.11.1.1    4/3/00   Miloslav Metelka undo update
 *  13   Gandalf-post-FCS1.11.1.0    3/8/00   Miloslav Metelka 
 *  12   Gandalf   1.11        1/19/00  Jesse Glick     Context help.
 *  11   Gandalf   1.10        1/18/00  Miloslav Metelka 
 *  10   Gandalf   1.9         1/13/00  Miloslav Metelka 
 *  9    Gandalf   1.8         1/11/00  Miloslav Metelka 
 *  8    Gandalf   1.7         1/10/00  Miloslav Metelka 
 *  7    Gandalf   1.6         12/28/99 Miloslav Metelka 
 *  6    Gandalf   1.5         11/29/99 Miloslav Metelka 
 *  5    Gandalf   1.4         11/29/99 Miloslav Metelka repaired previous change
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         11/24/99 Miloslav Metelka 
 *  2    Gandalf   1.1         11/14/99 Miloslav Metelka 
 *  1    Gandalf   1.0         11/8/99  Miloslav Metelka 
 * $
 */

