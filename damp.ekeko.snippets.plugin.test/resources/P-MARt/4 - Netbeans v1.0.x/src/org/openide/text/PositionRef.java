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
import java.lang.ref.WeakReference;
import java.util.*;

import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.loaders.DataObject;
import org.openide.cookies.EditorCookie;

/** Reference to one position in a document.
* This position is held as an integer offset, or as a {@link Position} object.
* There is also support for serialization of positions.
*
* @author Petr Hamernik
*/
public final class PositionRef extends Object implements Serializable {
    static final long serialVersionUID = -4931337398907426948L;

    /** Which type of position is currently holded - int X Position */
    transient private Manager.Kind kind;

    /** Manager for this position */
    private Manager manager;

    /** insert after? */
    private boolean insertAfter;

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * position offset.
    * @param manager manager for the position
    * @param offset - position in the document
    * @param bias the bias for the position
    */
    PositionRef (Manager manager, int offset, Position.Bias bias) {
        this (manager, manager.new OffsetKind (offset), bias);
    }

    /** Creates new <code>PositionRef</code> using the given manager at the specified
    * line and column.
    * @param manager manager for the position
    * @param line line number
    * @param column column number
    * @param bias the bias for the position
    */
    PositionRef (Manager manager, int line, int column, Position.Bias bias) {
        this (manager, manager.new LineKind (line, column), bias);
    }

    /** Constructor for everything.
    * @param manager manager that we are refering to
    * @param kind kind of position we hold
    * @param bias bias for the position
    */
    private PositionRef (Manager manager, Manager.Kind kind, Position.Bias bias) {
        this.manager = manager;
        this.kind = kind;
        insertAfter = (bias == Position.Bias.Backward);
        init ();
    }

    /** Initialize variables after construction and after deserialization. */
    private void init() {
        kind = manager.addPosition(this);
    }

    /** Writes the manager and the offset (int). */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean (insertAfter);
        out.writeObject (manager);
        kind.write (out);
    }

    /** Reads the manager and the offset (int). */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        insertAfter = in.readBoolean ();
        manager = (Manager)in.readObject();
        kind = manager.readKind (in);
        init ();
    }

    /** @return the appropriate manager for this position ref.
    */
    public EditorSupport getEditorSupport () {
        return manager.getEditorSupport ();
    }

    /** @return the bias of the position
    */
    public Position.Bias getPositionBias() {
        return insertAfter ? Position.Bias.Backward : Position.Bias.Forward;
    }

    /** @return the position as swing.text.Position object.
    * @exception IOException when an exception occured during reading the file.
    */
    public Position getPosition() throws IOException {
        synchronized (manager.getEditorSupport ().getLock ()) {
            manager.getEditorSupport ().openDocument ();
            Manager.PositionKind p = (Manager.PositionKind)kind;
            return p.pos;
        }
    }

    /** @return the position as offset index in the file.
    */
    public int getOffset() {
        return kind.getOffset ();
    }

    /** Get the line number where this position points to.
    * @return the line number for this position
    * @throws IOException if the document could not be opened to check the line number
    */
    public int getLine() throws IOException {
        return kind.getLine ();
    }

    /** Get the column number where this position points to.
    * @return the column number within a line (counting starts from zero)
    * @exception IOException if the document could not be opened to check the column number
    */
    public int getColumn() throws IOException {
        return kind.getColumn ();
    }

    public String toString() {
        return "Pos[" + getOffset () + "]"; // NOI18N
    }

    /** This class is responsible for the holding the Document object
    * and the switching the status of PositionRef (Position X offset)
    * objects which depends to this manager.
    * It has one abstract method for the creating the StyledDocument.
    */
    static final class Manager extends Object implements Serializable {
        /** List of the WeakReferences to the PositionRef objects
        * created for this manager.
        * @associates WeakReference
        */
        transient LinkedList positions;

        /** support for the editor */
        transient private EditorSupport support;

        /** the document for this manager or null if the manager is not in memory */
        transient private StyledDocument doc;

        static final long serialVersionUID =-4374030124265110801L;
        /** Creates new manager
        * @param supp support to work with
        */
        public Manager(EditorSupport supp) {
            support = supp;
            init();
        }

        /** Initialize the variables to the default values. */
        protected void init() {
            positions = new LinkedList();
        }

        /** Reads the object and initialize */
        private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
            DataObject obj = (DataObject) in.readObject();
            support = (EditorSupport) obj.getCookie(EditorSupport.class);
            if (support == null) {
                //PENDING - what about now ? does exist better way ?
                throw new IOException();
            }
        }

        final Object readResolve () {
            return support.getPositionManager ();
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(support.findDataObject());
        }

        /** @return the styled document or null if the document is not loaded.
        */
        public EditorSupport getEditorSupport () {
            return support;
        }

        /** Converts all positions into document one.
        */
        void documentOpened (StyledDocument doc) {
            this.doc = doc;

            ListIterator it = positions.listIterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference) it.next();
                PositionRef pos = (PositionRef) ref.get();
                if (pos == null)
                    it.remove();
                else {
                    pos.kind = pos.kind.toMemory (pos.insertAfter);
                }
            }
        }

        /** Closes the document and switch all positionRefs to the offset (int)
        * holding status (Position objects willbe forgotten.
        */
        void documentClosed () {
            Iterator it = ((Collection)positions.clone ()).iterator();
            while (it.hasNext()) {
                WeakReference ref = (WeakReference) it.next();
                PositionRef pos = (PositionRef) ref.get();
                if (pos == null)
                    positions.remove(ref);
                else {
                    pos.kind = pos.kind.fromMemory ();
                }
            }

            doc = null;
        }

        /** Adds the position to this manager. */
        Kind addPosition(PositionRef pos) {
            synchronized (getEditorSupport ().getLock ()) {
                positions.add(new WeakReference(pos));

                if (doc != null) {
                    return pos.kind.toMemory (pos.insertAfter);
                } else {
                    return pos.kind;
                }

            }
        }

        //
        // Kinds
        //

        /** Loads the kind from the stream */
        private Kind readKind (DataInput is) throws IOException {
            int offset = is.readInt ();
            int line = is.readInt ();
            int column = is.readInt ();

            if (offset == -1) {
                // line and column must be valid
                return new LineKind (line, column);
            }

            if (line == -1 || column == -1) {
                // offset kind
                return new OffsetKind (offset);
            }

            // out of memory representation
            return new OutKind (offset, line, column);
        }

        /** Base kind with all methods */
        private abstract class Kind extends Object {
            /** Offset */
            public abstract int getOffset ();

            /** Get the line number */
            public abstract int getLine() throws IOException;

            /** Get the column number */
            public abstract int getColumn() throws IOException;

            /** Writes the kind to stream */
            public abstract void write (DataOutput os) throws IOException;

            /** Converts the kind to representation in memory */
            public PositionKind toMemory (boolean insertAfter) {
                // try to find the right position
                Position p;
                try {
                    p = NbDocument.createPosition (doc, getOffset (), insertAfter ? Position.Bias.Forward : Position.Bias.Backward);
                } catch (BadLocationException e) {
                    p = doc.getEndPosition ();
                }
                return new PositionKind (p);
            }

            /** Converts the kind to representation out from memory */
            public Kind fromMemory () {
                return this;
            }
        }

        /** Kind for representing position when the document is
        * in memory.
        */
        private final class PositionKind extends Kind {
            /** position */
            private Position pos;

            /** Constructor */
            public PositionKind (Position pos) {
                this.pos = pos;
            }

            /** Offset */
            public int getOffset () {
                return pos.getOffset ();
            }

            /** Get the line number */
            public int getLine() {
                return NbDocument.findLineNumber(doc, getOffset());
            }

            /** Get the column number */
            public int getColumn() {
                return NbDocument.findLineColumn(doc, getOffset());
            }

            /** Writes the kind to stream */
            public void write (DataOutput os) throws IOException {
                os.writeInt (getOffset ());
                os.writeInt (getLine ());
                os.writeInt (getColumn ());
            }

            /** Converts the kind to representation in memory */
            public PositionKind toMemory (boolean insertAfter) {
                return this;
            }

            /** Converts the kind to representation out from memory */
            public Kind fromMemory () {
                return new OutKind (this);
            }

        }

        /** Kind for representing position when the document is
        * out from memory. There are all infomation about the position,
        * including offset, line and column.
        */
        private final class OutKind extends Kind {
            private int offset;
            private int line;
            private int column;

            /** Constructs the out kind from the position kind.
            */
            public OutKind (PositionKind kind) {
                this.offset = kind.getOffset ();
                this.line = kind.getLine ();
                this.column = kind.getColumn ();
            }

            /** Constructs the out kind.
            */
            OutKind (int offset, int line, int column) {
                this.offset = offset;
                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset () {
                return offset;
            }

            /** Get the line number */
            public int getLine() {
                return line;
            }

            /** Get the column number */
            public int getColumn() {
                return column;
            }

            /** Writes the kind to stream */
            public void write (DataOutput os) throws IOException {
                os.writeInt (offset);
                os.writeInt (line);
                os.writeInt (column);
            }
        } // OutKind

        /** Kind for representing position when the document is
        * out from memory. Represents only offset in the document.
        */
        private final class OffsetKind extends Kind {
            private int offset;

            /** Constructs the out kind from the position kind.
            */
            public OffsetKind (int offset) {
                this.offset = offset;
            }

            /** Offset */
            public int getOffset () {
                return offset;
            }

            /** Get the line number */
            public int getLine() throws IOException {
                return NbDocument.findLineNumber(getEditorSupport().openDocument(), offset);
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                return NbDocument.findLineColumn (getEditorSupport().openDocument(), offset);
            }

            /** Writes the kind to stream */
            public void write (DataOutput os) throws IOException {
                os.writeInt (offset);
                os.writeInt (-1);
                os.writeInt (-1);
            }
        }

        /** Kind for representing position when the document is
        * out from memory. Represents only line and column in the document.
        */
        private final class LineKind extends Kind {
            private int line;
            private int column;

            /** Constructor.
            */
            public LineKind (int line, int column) {
                this.line = line;
                this.column = column;
            }

            /** Offset */
            public int getOffset () {
                try {
                    StyledDocument doc = getEditorSupport().openDocument();
                    return NbDocument.findLineOffset (doc, line) + column;
                } catch (IOException e) {
                    // what to do? hopefully unlikelly
                    return 0;
                }
            }

            /** Get the line number */
            public int getLine() throws IOException {
                return line;
            }

            /** Get the column number */
            public int getColumn() throws IOException {
                return column;
            }

            /** Writes the kind to stream */
            public void write (DataOutput os) throws IOException {
                os.writeInt (-1);
                os.writeInt (line);
                os.writeInt (column);
            }

            /** Converts the kind to representation in memory */
            public PositionKind toMemory (boolean insertAfter) {
                // try to find the right position
                Position p;
                try {
                    p = NbDocument.createPosition (doc, NbDocument.findLineOffset (doc, line) + column, insertAfter ? Position.Bias.Forward : Position.Bias.Backward);
                } catch (BadLocationException e) {
                    p = doc.getEndPosition ();
                }
                return new PositionKind (p);
            }

        }

    }


}

/*
 * Log
 *  27   src-jtulach1.26        1/15/00  Daniel Prusa    serialization fixed
 *  26   src-jtulach1.25        1/13/00  Ian Formanek    NOI18N
 *  25   src-jtulach1.24        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   src-jtulach1.23        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  23   src-jtulach1.22        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  22   src-jtulach1.21        5/14/99  Jaroslav Tulach Bugfixes.
 *  21   src-jtulach1.20        5/5/99   Petr Hamernik   getPositionBias() method
 *       added
 *  20   src-jtulach1.19        4/20/99  Petr Hamernik   small mistake fixed
 *  19   src-jtulach1.18        4/20/99  Jaroslav Tulach Updated to work with 
 *       position biases.
 *  18   src-jtulach1.17        4/9/99   David Simonek   bugfix #1429
 *  17   src-jtulach1.16        3/11/99  Jaroslav Tulach 
 *  16   src-jtulach1.15        3/10/99  Jaroslav Tulach Kinds do not use 
 *       getDocument
 *  15   src-jtulach1.14        3/9/99   Jaroslav Tulach Everything is 
 *       implemented.
 *  14   src-jtulach1.13        3/8/99   Jaroslav Tulach 
 *  13   src-jtulach1.12        3/8/99   Jaroslav Tulach Bundles.
 *  12   src-jtulach1.11        2/26/99  Jesse Glick     [JavaDoc]
 *  11   src-jtulach1.10        2/19/99  Petr Hamernik   
 *  10   src-jtulach1.9         2/19/99  Petr Hamernik   changes with 
 *       Position.Bias
 *  9    src-jtulach1.8         2/17/99  Petr Hamernik   
 *  8    src-jtulach1.7         2/11/99  Jesse Glick     get{Line,Column}() were 
 *       both getting column, and were (gratuitously?) catching & ignoring 
 *       IOExceptions.
 *  7    src-jtulach1.6         2/10/99  Jesse Glick     [JavaDoc]
 *  6    src-jtulach1.5         2/8/99   Petr Hamernik   
 *  5    src-jtulach1.4         2/3/99   Jaroslav Tulach 
 *  4    src-jtulach1.3         2/2/99   Jaroslav Tulach 
 *  3    src-jtulach1.2         1/29/99  Petr Hamernik   
 *  2    src-jtulach1.1         1/29/99  Petr Hamernik   
 *  1    src-jtulach1.0         1/29/99  Petr Hamernik   
 * $
 */
