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

import java.util.ArrayList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
* Document implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseDocumentEvent extends AbstractDocument.DefaultDocumentEvent {

    private DocOp.ModifyUndoEdit modifyUndoEdit;

    /** Previous event in the chain of the events that were
    * connected together to be undone/redone at once.
    */
    private BaseDocumentEvent previous;

    private boolean inUndo;

    private boolean inRedo;

    static final long serialVersionUID =-7624299835780414963L;

    /** Construct document event instance.
    * @param offset position in the document where the insert/remove/change
    *   occured
    * @param length number of the characters affected by the event
    * @param type type of the event - INSERT/REMOVE/CHANGE
    */
    public BaseDocumentEvent(BaseDocument doc, int offset, int length,
                             DocumentEvent.EventType type) {
        ((AbstractDocument)doc).super(offset, length, type);
    }

    protected UndoableEdit findEdit(Class editClass) {
        int cnt = edits.size();
        for (int i = 0; i < cnt; i++) {
            Object edit = edits.get(i);
            if (editClass.isInstance(edit)) {
                return (UndoableEdit)edit;
            }
        }
        return null;
    }

    private DocOp.ModifyUndoEdit getModifyUndoEdit() {
        if (getType() == DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Cannot be called for CHANGE events."); // NOI18N
        }

        if (modifyUndoEdit == null) {
            modifyUndoEdit = (DocOp.ModifyUndoEdit)findEdit(DocOp.ModifyUndoEdit.class);
        }
        return modifyUndoEdit;
    }

    /** Gets the characters that were inserted/removed or null
    * for change event.
    * Characters must be used only in readonly mode as the
    * character array is shared by all listeners and also by 
    * modification event itself.
    */
    public char[] getChars() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getChars() : null;
    }

    /** Get the text that was inserted/removed or null
    * for change event.
    */
    public String getText() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getText() : null;
    }

    /** Get the line at which the insert/remove occured */
    public int getLine() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getLine() : 0;
    }

    /** Get the count of '\n' (line-feeds) contained in the inserted/removed text. */
    public int getLFCount() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getLFCount() : 0;
    }

    /** Get the offset at which the updating of the syntax stopped so there
    * are no more changes in the tokens after this point.
    */
    public int getSyntaxUpdateOffset() {
        return (getModifyUndoEdit() != null) ? getModifyUndoEdit().getSyntaxUpdateOffset() : 0;
    }

    public String getDrawLayerName() {
        if (getType() != DocumentEvent.EventType.CHANGE) {
            throw new IllegalStateException("Can be called for CHANGE events only."); // NOI18N
        }

        DrawLayerChange dlc = (DrawLayerChange)findEdit(DrawLayerChange.class);

        return (dlc != null) ? dlc.getDrawLayerName() : null;
    }

    /** Whether this event is being fired because it's being undone. */
    public boolean isInUndo() {
        return inUndo;
    }

    /** Whether this event is being fired because it's being redone. */
    public boolean isInRedo() {
        return inRedo;
    }

    public void undo() throws CannotUndoException {
        inUndo = true;
        super.undo();
        if (previous != null) {
            previous.undo();
        }
        inUndo = false;
    }

    public void redo() throws CannotRedoException {
        inRedo = true;
        if (previous != null) {
            previous.redo();
        }
        super.redo();
        inRedo = false;
    }

    public String getUndoPresentationName() {
        return "";
    }

    public String getRedoPresentationName() {
        return "";
    }

    protected final BaseDocumentEvent getPrevious() {
        return previous;
    }

    /** Returns true if this event can be merged by the previous
    * one (given as parameter) in the undo-manager queue.
    */
    public boolean canMerge(BaseDocumentEvent evt) {
        if (getType() == DocumentEvent.EventType.INSERT) { // last was insert
            if (evt.getType() == DocumentEvent.EventType.INSERT) { // adding insert to insert
                String text = getText();
                String evtText = evt.getText();
                if ((getLength() == 1 || (getLength() > 1 && Analyzer.isSpace(text)))
                        && (evt.getLength() == 1 || (evt.getLength() > 1
                                                     && Analyzer.isSpace(evtText)))
                        && (evt.getOffset() + evt.getLength() == getOffset()) // this follows the previous
                   ) {
                    BaseDocument doc = (BaseDocument)getDocument();
                    boolean thisWord = doc.isIdentifierPart(text.charAt(0));
                    boolean lastWord = doc.isIdentifierPart(evtText.charAt(0));
                    if (thisWord && lastWord) { // add word char to word char(s)
                        return true;
                    }
                    boolean thisWhite = doc.isWhitespace(text.charAt(0));
                    boolean lastWhite = doc.isWhitespace(evtText.charAt(0));
                    if ((lastWhite && thisWhite)
                            || (!lastWhite && !lastWord && !thisWhite && !thisWord)
                       ) {
                        return true;
                    }
                }
            } else { // adding remove to insert
            }
        } else { // last was remove
            if (evt.getType() == DocumentEvent.EventType.INSERT) { // adding insert to remove
            } else { // adding remove to remove
            }
        }
        return false;
    }

    /** Try to determine whether this event can replace the old one.
    * This is used to batch the one-letter modifications into larger
    * parts (words) and undoing/redoing them at once.
    * This method returns true whether 
    */
    public boolean replaceEdit(UndoableEdit anEdit) {
        BaseDocument doc = (BaseDocument)getDocument();
        if (anEdit instanceof BaseDocumentEvent) {
            BaseDocumentEvent evt = (BaseDocumentEvent)anEdit;

            if (!doc.undoMergeReset && canMerge(evt)) {
                //        System.out.println("BaseDocumentEvent.java:185 this=" + this + ", merged with evt=" + evt);
                previous = evt;
                return true;
            }
        }
        doc.undoMergeReset = false;
        return false;
    }

    public void die() {
        previous = null;
    }

    public String toString() {
        return System.identityHashCode(this) + " " + super.toString()
               + ", type=" + getType()
               + ((getType() != DocumentEvent.EventType.CHANGE)
                  ? ("text='" + getText() + "'") : "");
    }

    /** Edit describing the change of the document draw-layers */
    static class DrawLayerChange extends AbstractUndoableEdit {

        String drawLayerName;

        DrawLayerChange(String drawLayerName) {
            this.drawLayerName = drawLayerName;
        }

        public String getDrawLayerName() {
            return drawLayerName;
        }

    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.5         4/5/00   Miloslav Metelka undo/redo naming removed
 *  5    Gandalf-post-FCS1.4         4/4/00   Miloslav Metelka 
 *  4    Gandalf-post-FCS1.3         4/3/00   Miloslav Metelka undo update
 *  3    Gandalf-post-FCS1.2         3/31/00  Miloslav Metelka fix
 *  2    Gandalf-post-FCS1.1         3/27/00  Miloslav Metelka 
 *  1    Gandalf-post-FCS1.0         3/8/00   Miloslav Metelka 
 * $
 */

