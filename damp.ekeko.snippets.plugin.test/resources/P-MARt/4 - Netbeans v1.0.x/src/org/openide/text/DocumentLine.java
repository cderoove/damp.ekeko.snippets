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
import java.util.*;
import java.lang.ref.*;
import java.util.WeakHashMap;
import javax.swing.text.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.util.WeakListener;
import org.openide.util.Task;
import org.openide.util.RequestProcessor;

/** Implementation of a line in a {@link StyledDocument}.
* One object
* of this class represents a line in the document by holding
* a {@link PositionRef}, which can represent a position in an open or
* closed document.
*
* @author Jaroslav Tulach
*/
public abstract class DocumentLine extends Line {
    /** reference to one position on the line */
    protected PositionRef pos;

    /** is breakpoint there - presistent state */
    private boolean breakpoint;

    /** error line  - transient state */
    private transient boolean error;

    /** current line - transient state */
    private transient boolean current;

    /** listener for changes of state of the document */
    private transient Listener listener;

    /** weak document listener assigned to the document or null */
    private transient DocumentListener docL;

    /** weak map that assignes to editor supports whether they have current or error line
    * selected. (EditorSupport, DocumentLine[2]), where Line[0] is current and Line[1] is error */
    private static WeakHashMap assigned = new WeakHashMap (5);


    static final long serialVersionUID =3213776466939427487L;
    /** Constructor.
    * @param obj data object we belong to
    * @param pos position on the line
    */
    public DocumentLine (DataObject obj, PositionRef pos) {
        super (obj);
        this.pos = pos;
    }

    /** Init listeners
    */
    private void init () {
        listener = new Listener ();
        pos.getEditorSupport ().addChangeListener (WeakListener.change (listener, pos.getEditorSupport ()));
    }

    /* Get the line number.
     * The number may change if the
    * text is modified.
    *
    * @return Returns current line number.
    */
    public int getLineNumber () {
        try {
            return pos.getLine ();
        } catch (IOException ex) {
            // what else?
            return 0;
        }
    }

    /* Shows the line.
    * @param kind one of SHOW_XXX constants.
    * @column the column of this line which should be selected
    */
    public abstract void show(int kind, int column);

    /* Sets the breakpoint. */
    public void setBreakpoint(boolean b) {
        if (breakpoint != b) {
            breakpoint = b;
            refreshState ();
        }
    }

    /* Tests if the breakpoint is set. */
    public boolean isBreakpoint () {
        return breakpoint;
    }

    /* Marks the error. */
    public void markError () {
        DocumentLine previous = registerLine (0, this);
        if (previous != null) {
            previous.error = false;
            previous.refreshState ();
        }

        error = true;

        refreshState ();
    }

    /* Unmarks error at this line. */
    public void unmarkError () {
        error = false;
        registerLine (1, null);

        refreshState ();
    }

    /* Marks this line as current. */
    public void markCurrentLine () {
        DocumentLine previous = registerLine (0, this);
        if (previous != null) {
            previous.current = false;
            previous.refreshState ();
        }

        current = true;
        refreshState ();
    }

    /* Unmarks this line as current. */
    public void unmarkCurrentLine () {
        current = false;
        registerLine (0, null);

        refreshState ();
    }

    /** Refreshes the current line.
    */
    synchronized void refreshState () {
        StyledDocument doc = pos.getEditorSupport ().getDocument ();

        if (doc != null) {
            // the document is in memory, mark the state

            if (docL != null) {
                doc.removeDocumentListener (docL);
            }

            // error line
            if (error) {
                NbDocument.markError (doc, pos.getOffset ());

                doc.addDocumentListener (docL = WeakListener.document (listener, doc));

                return;
            }

            // current line
            if (current) {
                NbDocument.markCurrent (doc, pos.getOffset ());
                return;
            }

            // breakpoint line
            if (breakpoint) {
                NbDocument.markBreakpoint (doc, pos.getOffset ());
                return;
            }

            NbDocument.markNormal (doc, pos.getOffset ());
            return;
        }
    }

    public int hashCode () {
        return pos.getEditorSupport ().hashCode ();
    }

    public boolean equals (Object o) {
        if (o instanceof DocumentLine) {
            DocumentLine dl = (DocumentLine)o;
            if (dl.pos.getEditorSupport () == pos.getEditorSupport ()) {
                return dl.getLineNumber () == getLineNumber ();
            }
        }
        return false;
    }


    //
    // Work with global hash table
    //

    /** Register this line as the one stored
    * under indx-index (0 = current, 1 = error).
    *
    * @param indx index to register
    * @param line value to add (this or null)
    * @return the previous value
    */
    private DocumentLine registerLine (int indx, DocumentLine line) {
        DocumentLine prev;

        EditorSupport es = pos.getEditorSupport ();

        DocumentLine[] arr = (DocumentLine[])assigned.get (es);

        if (arr != null) {
            // remember the previous
            prev = arr[indx];
        } else {
            // create new array
            arr = new DocumentLine[2];
            assigned.put (es, arr);
            prev = null;
        }
        arr[indx] = line;
        return prev;

    }


    //
    // Serialization
    //

    /** Write fields.
    */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        // do not do default read/write object
        oos.writeObject (pos);
        oos.writeBoolean (breakpoint);
    }

    /** Read important fields.
    */
    private void readObject (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        pos = (PositionRef)ois.readObject ();
        setBreakpoint (ois.readBoolean ());
    }

    /** Register line.
    */
    Object readResolve() throws ObjectStreamException {
        return Set.registerLine (this);
    }

    /** Listener to Position.Ref manager's state (in memory, on disk).
    */
    private final class Listener implements ChangeListener, DocumentListener {
        public void stateChanged (ChangeEvent ev) {
            refreshState ();
        }

        public void removeUpdate(final javax.swing.event.DocumentEvent p0) {
            unmarkError ();
        }

        public void insertUpdate(final javax.swing.event.DocumentEvent p0) {
            unmarkError ();
        }

        public void changedUpdate(final javax.swing.event.DocumentEvent p0) {
        }
    }

    /** Abstract implementation of {@link Line.Set}.
     *  Defines
    * ways to obtain a line set for documents following
    * NetBeans conventions.
    */
    public static abstract class Set extends Line.Set {
        /** listener on document changes */
        private LineListener listener;
        /** all lines in the set or null */
        private java.util.List list;
        /** map to hold all existing lines (Line, Reference (Line)) */
        private static WeakHashMap allLines = new WeakHashMap (37);

        /** Constructor.
        * @param doc document to work on
        */
        public Set (StyledDocument doc) {
            listener = new LineListener (doc);
        }

        /* Returns an unmodifiable set of Lines sorted by their
        * line numbers that contains all lines holded by this
        * Line.Set.
        *
        * @return list of Line objects
        */
        public java.util.List getLines () {
            if (list == null) {
                int cnt = listener.getOriginalLineCount ();
                java.util.List l = new java.util.LinkedList ();
                for (int i = 0; i < cnt; i++) {
                    l.add (getOriginal (i));
                }
                list = l;
            }
            return list;
        }

        /* Finder method that for the given line number finds right
        * Line object that represent as closely as possible the line number
        * in the time when the Line.Set has been created.
        *
        * @param line is a number of the line (text line) we want to acquire
        * @exception IndexOutOfBoundsException if <code>line</code> is invalid.
        */
        public Line getOriginal (int line) throws IndexOutOfBoundsException {
            int newLine = listener.getLine (line);
            int offset = NbDocument.findLineOffset (listener.doc, newLine);
            //      System.out.println("Then: " + line + " now: " + newLine + " offset: " + offset); // NOI18N
            Line ll = registerLine (createLine (offset));

            return ll;
        }

        /* Creates current line.
        *
        * @param line is a number of the line (text line) we want to acquire
        * @exception IndexOutOfBoundsException if <code>line</code> is invalid.
        */
        public Line getCurrent (int line) throws IndexOutOfBoundsException {
            int offset = NbDocument.findLineOffset (listener.doc, line);
            //      System.out.println("Then: " + line + " now: " + newLine + " offset: " + offset); // NOI18N
            Line ll = registerLine (createLine (offset));

            return ll;
        }

        /** Creates a {@link Line} for a given offset.
        * @param offset the beginning offset of the line
        * @return line object representing the line at this offset
        */
        protected abstract Line createLine (int offset);

        /** Registers the line.
        * @param l line to register
        * @return the line l or line previously registered
        */
        static synchronized Line registerLine (Line l) {
            Reference ref = (Reference)allLines.get (l);
            Line prev = ref == null ? null : (Line)ref.get ();
            if (prev == null) {
                if (l instanceof DocumentLine)
                    ((DocumentLine)l).init ();
                allLines.put (l, new WeakReference (l));
                return l;
            } else {
                return prev;
            }
        }
    }


}


/*
* Log
*  22   src-jtulach1.21        1/15/00  Daniel Prusa    Line registration fixed
*  21   src-jtulach1.20        1/14/00  Jaroslav Tulach Lines are really hold 
*       weakly.
*  20   src-jtulach1.19        1/13/00  Ian Formanek    NOI18N
*  19   src-jtulach1.18        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  18   src-jtulach1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  17   src-jtulach1.16        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  16   src-jtulach1.15        8/3/99   Jaroslav Tulach Project settings node.
*  15   src-jtulach1.14        8/2/99   Jaroslav Tulach 
*  14   src-jtulach1.13        7/30/99  Jaroslav Tulach getOriginal & getCurrent 
*       in Line
*  13   src-jtulach1.12        7/27/99  Jaroslav Tulach Faster lines.
*  12   src-jtulach1.11        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  11   src-jtulach1.10        4/9/99   David Simonek   bugfix #1429
*  10   src-jtulach1.9         3/19/99  Jaroslav Tulach 
*  9    src-jtulach1.8         3/19/99  Jaroslav Tulach 
*  8    src-jtulach1.7         3/18/99  Jaroslav Tulach 
*  7    src-jtulach1.6         2/11/99  Jaroslav Tulach 
*  6    src-jtulach1.5         2/10/99  Jesse Glick     [JavaDoc]
*  5    src-jtulach1.4         2/10/99  Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         2/3/99   Jaroslav Tulach 
*  3    src-jtulach1.2         2/2/99   Jaroslav Tulach 
*  2    src-jtulach1.1         1/29/99  Jaroslav Tulach SortedSet changed to List
*  1    src-jtulach1.0         1/29/99  Jaroslav Tulach 
* $
*/
