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

package org.openide.text;

import java.io.*;
import java.util.Date;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/** Represents one line in a text document.
 * The line number may change
* when the text is modified, but the identity of the line is retained. It is designed to allow line-dependent
* modules of the IDE (such as the compiler and debugger) to make use of a line consistently even as the text is modified.
*
* @author Ales Novak, Petr Hamernik, Jan Jancura, Jaroslav Tulach
*/
public abstract class Line extends Object implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 9113186289600795476L;

    /** DataObject that is parent of the line */
    private DataObject dataObject;

    /**
     * Create a new line object based on a given data object.
     * This implementation is abstract, so the specific line number is not used here. Subclasses should somehow specify the position.
     * @param obj the data object this line is a part of
     */
    public Line(DataObject obj) {
        dataObject = obj;
    }

    /** Shows the line only if the editor is open.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_TRY_SHOW = 0;

    /** Opens the editor if necessary and shows the line.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_SHOW     = 1;

    /** Opens the editor if necessary, shows the line, and takes the focus.
     * @see #show(int) <code>show</code>
     */
    public final static int SHOW_GOTO     = 2;

    /**
     * Get the data object this line is a part of.
    * @return data object
    */
    public final DataObject getDataObject () {
        return dataObject;
    }

    /** Get the line number.
    *
    * @return current line number (may change as text is edited)
    */
    public abstract int getLineNumber ();

    /** Show the line.
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, or {@link #SHOW_GOTO}
    * @param column the column of this line which should be selected
    */
    public abstract void show(int kind, int column);

    /** Shows the line (at the first column).
    * @param kind one of {@link #SHOW_TRY_SHOW}, {@link #SHOW_SHOW}, or {@link #SHOW_GOTO}
    * @see #show(int, int)
    */
    public void show(int kind) {
        show(kind, 0);
    }

    /** Set or clear a (debugger) breakpoint at this line.
     * @param b <code>true</code> to turn on
     */
    public abstract void setBreakpoint(boolean b);

    /** Test if there is a breakpoint set at this line.
     * @return <code>true</code> is there is
     */
    public abstract boolean isBreakpoint();

    /** Mark an error at this line. */
    public abstract void markError();

    /** Unmark error at this line. */
    public abstract void unmarkError();

    /** Mark this line as current. */
    public abstract void markCurrentLine();

    /** Unmark this line as current. */
    public abstract void unmarkCurrentLine();

    /** Object that represents a snapshot of lines at the time it was created.
    * It is used to create a mapping from line
    * numbers to line objects, for example when the file is saved.
    * Such a mapping can then be used by the compiler, e.g., to find
    * the correct {@link Line} object, assuming it has a line number.
    * <P>
    * Mappings of line numbers to line objects will survive modifications
    * of the text, and continue to represent the original lines as close as possible.
    * For example: if a new line is inserted at the 10th line of a document
    * and the compiler module asks for the 25th line (because the compiler reports an error at line 25 in the saved file) via the line set, the 26th line
    * of the current document will be marked as being in error.
    */
    public static abstract class Set extends Object {
        /** date when the object has been created */
        private Date date;

        /** Create a new snapshot. Remembers the date when it was created. */
        public Set () {
            date = new Date ();
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of element type {@link Line}
        */
        public abstract java.util.List getLines ();

        /** Get creation time for this line set.
         * @return time
        */
        public final Date getDate() {
            return date;
        }

        /** Find line object in the line set corresponding to original line number.
         * That is, finds the line in the current document which originally had the indicated line number.
         * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getOriginal (int line) throws IndexOutOfBoundsException;

        /** Find line object representing the line in current document.
        * 
        *
        * @param line number of the line in current state of the document
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public abstract Line getCurrent (int line) throws IndexOutOfBoundsException;

    }
}


/*
 * Log
 *  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    src-jtulach1.5         7/30/99  Jaroslav Tulach getOriginal & getCurrent
 *       in Line
 *  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    src-jtulach1.3         2/10/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         1/29/99  Jaroslav Tulach SortedSet changed to 
 *       List
 *  2    src-jtulach1.1         1/29/99  Jaroslav Tulach 
 *  1    src-jtulach1.0         1/28/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Jaroslav Tulach serializable
 *  0    Tuborg    0.16        --/--/98 Petr Hamernik   unmark error added
 */
