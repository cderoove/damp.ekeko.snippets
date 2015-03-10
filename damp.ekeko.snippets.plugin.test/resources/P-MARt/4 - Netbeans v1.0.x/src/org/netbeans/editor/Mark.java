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

import java.util.Enumeration;
import javax.swing.text.BadLocationException;

/**
* Marks hold the relative position in the document.
*
* @author Miloslav Metelka
* @version 1.00
*/


/** Class defining basic type of mark. This is a mark used most frequently.
* It's instances are inserted into the leaf plane of the tree.
*/
public class Mark extends DocMarks.TreeMark {

    /** Is this mark valid - is it currently inserted in the tree? */
    boolean valid;

    /** Document marks where this mark is inserted.
    * This should be in fact regular inner class (not static), however
    * I prefer this solution because of easier manipulation with
    * static inner classes.
    */
    DocMarks marks;

    /** Construct new mark with insertAfter = false. */
    public Mark() {
        this(false);
    }

    /** Construct new mark.
    * @param insertAfter whether the inserts performed right at the position
    *   of this mark will go after this mark i.e. this mark will not move
    *   forward when inserting right at its position. This flag corresponds
    *   to <tt>Position.Bias.Backward</tt>.
    */
    public Mark(boolean insertAfter) {
        this.insertAfter = insertAfter;
    }

    /** Get the position of this mark */
    public int getOffset() throws InvalidMarkException {
        if (marks == null)
            throw new InvalidMarkException();

        synchronized(marks) {
            if (!valid) {
                throw new InvalidMarkException();
            }

            marks.statPosCalled++;
            return getOffsetRec(marks.leafPlane);
        }
    }

    /** Get the line number of this mark */
    public int getLine() throws InvalidMarkException {
        if (marks == null)
            throw new InvalidMarkException();

        synchronized (marks) {
            if (!valid) {
                throw new InvalidMarkException();
            }

            marks.statLineCalled++;
            return getLineRec(marks.leafPlane);
        }
    }

    /** Get position and the line number of this mark. The offset
    * is returned and line is stored in <tt>line[0]</tt>.
    */
    int getOffsetAndLine(int[] line) throws InvalidMarkException {
        if (marks == null)
            throw new InvalidMarkException();

        synchronized (marks) {
            if (!valid) {
                throw new InvalidMarkException();
            }

            marks.statPosCalled++;
            marks.statLineCalled++;
            line[0] = 0;
            return getOffsetAndLineRec(marks.leafPlane, line);
        }
    }

    /** Compare this mark to some position.
    * @param pos tested position
    * @return zero - if the marks have the same position
    *         less than zero - if this mark is before the position
    *         greater than zero - if this mark is after the position
    */
    public final int compare(int pos) throws InvalidMarkException {
        return getOffset() - pos;
    }

    /** Get the insertAfter flag */
    public boolean getInsertAfter() {
        return insertAfter;
    }

    /** Remove mark from tree, so it's no longer valid. It can
    * be hovewer inserted again later even into different instance
    * of <CODE>DocMarks</CODE>.
    */
    public void remove() throws InvalidMarkException {
        if (marks == null)
            throw new InvalidMarkException();

        synchronized (marks) {
            marks.removeMarkImpl(this);
        }
    }

    /** This function is called from removeUpdater when mark occupies
    * the removal area. The mark can decide what to do next.
    * If it doesn't redefine this method it will be simply moved to
    * the begining of removal area. It is valid to add or remove other mark 
    * from this method. It is even possible (but not very useful)
    * to add the mark to the removal area. However that mark will not be
    * notified about current removal.
    */
    protected void removeUpdateAction(int pos, int len) {
    }


    /** Test if this mark is valid */
    public boolean isValid() {
        if (marks == null)
            return false;

        synchronized(marks) {
            return valid;
        }
    }

    /** Get info about <CODE>Mark</CODE>. */
    public String toString() {
        return "relPos=" + relPos // NOI18N
               + ", relLine=" + relLine // NOI18N
               + ", insertAfter=" + insertAfter; // NOI18N
    }

}


/*
 * Log
 *  2    Gandalf-post-FCS1.1         3/15/00  Miloslav Metelka Structural change
 *  1    Gandalf-post-FCS1.0         3/8/00   Miloslav Metelka 
 * $
 */

