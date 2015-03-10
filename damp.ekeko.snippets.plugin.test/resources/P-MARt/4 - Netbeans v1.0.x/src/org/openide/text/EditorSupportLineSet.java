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
import javax.swing.event.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.openide.loaders.*;
import org.openide.util.WeakListener;

/** Line set for an EditorSupport.
*
* @author Jaroslav Tulach
*/
final class EditorSupportLineSet extends DocumentLine.Set {
    /** support we are attached to */
    private EditorSupport support;

    /** Constructor.
    * @param support support to work with
    * @param doc document to use
    */
    public EditorSupportLineSet(EditorSupport support, StyledDocument doc) {
        super (doc);
        this.support = support;
    }

    /** Creates a Line for given offset.
    * @param offset the begining of line
    * @return line that should represent the given line
    */
    public Line createLine (int offset) {
        StyledDocument doc = support.getDocument();
        if (doc == null)
            // do nothing - document was probably closed
            return null;

        PositionRef ref = new PositionRef(
                              support.getPositionManager (), offset, Position.Bias.Forward
                          );
        return new SupportLine (support.findDataObject(), ref, support);
    }

    /** Line for my work.
    */
    private static final class SupportLine extends DocumentLine {

        static final long serialVersionUID =7282223299866986051L;
        /** Position reference to a place in document
        */
        public SupportLine (DataObject obj, PositionRef ref, EditorSupport support) {
            super (obj, ref);
        }

        /** Shows the line.
        * @param kind one of SHOW_XXX constants.
        * @column the column of this line which should be selected
        */
        public void show (int kind, int column) {

            EditorSupport support = pos.getEditorSupport();

            if (kind == SHOW_TRY_SHOW && !support.isDocumentLoaded ()) return;

            EditorSupport.Editor editor = support.openAt(pos, column);

            if (kind == SHOW_GOTO) {
                editor.requestFocus ();
            }
        }

    }

    /** Line set for closed EditorSupport.
    *
    * @author Jaroslav Tulach
    */
    static class Closed extends Line.Set implements ChangeListener {
        /** support we are attached to */
        private EditorSupport support;
        /** line set to delegate to or null if the editor is still closed,
        * is set to non null when the editor opens
        */
        private Line.Set delegate;

        /** Constructor.
        * @param support support to work with
        * @param doc document to use
        */
        public Closed (EditorSupport support) {
            this.support = support;
            support.addChangeListener (WeakListener.change (this, support));
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of element type {@link Line}
        */
        public java.util.List getLines () {
            if (delegate != null) {
                return delegate.getLines ();
            }
            // PENDING
            return new java.util.ArrayList ();
        }

        /** Find line object in the line set corresponding to original line number.
        * That is, finds the line in the current document which originally had the indicated line number.
        * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getOriginal (int line) throws IndexOutOfBoundsException {
            if (delegate != null) {
                return delegate.getOriginal (line);
            }

            return getCurrent (line);
        }

        /** Find line object in the line set corresponding to current line number.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getCurrent (int line) throws IndexOutOfBoundsException {
            PositionRef ref = new PositionRef (support.getPositionManager (), line, 0, Position.Bias.Forward);

            Line l = registerLine (new SupportLine (support.findDataObject(), ref, support));
            return l;
        }


        /** Arrives when the document is opened.
        */
        public synchronized void stateChanged (ChangeEvent ev) {
            if (delegate == null) {
                StyledDocument doc = support.getDocument ();
                if (doc != null)
                    delegate = new EditorSupportLineSet (support, doc);
            }
        }
    }

}


/*
* Log
*  19   src-jtulach1.18        1/15/00  Daniel Prusa    Line registration fixed
*  18   src-jtulach1.17        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  17   src-jtulach1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  16   src-jtulach1.15        9/27/99  Petr Jiricka    Fixed 
*       NullPointerException when document is null
*  15   src-jtulach1.14        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  14   src-jtulach1.13        7/30/99  Ales Novak      an impl of Line.show(int,
*       int) does not ignore column now
*  13   src-jtulach1.12        7/30/99  Jaroslav Tulach getOriginal & getCurrent 
*       in Line
*  12   src-jtulach1.11        7/27/99  Jaroslav Tulach Faster lines.
*  11   src-jtulach1.10        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  10   src-jtulach1.9         5/14/99  Ales Novak      getDataObjectAccess not 
*       used
*  9    src-jtulach1.8         4/9/99   David Simonek   bugfix #1429
*  8    src-jtulach1.7         3/19/99  Jaroslav Tulach 
*  7    src-jtulach1.6         3/19/99  Jaroslav Tulach 
*  6    src-jtulach1.5         3/18/99  Petr Hamernik   
*  5    src-jtulach1.4         3/17/99  Jaroslav Tulach Output Window fixing.
*  4    src-jtulach1.3         3/10/99  Jaroslav Tulach Creates line set even the
*       document is not opened
*  3    src-jtulach1.2         2/19/99  Petr Hamernik   changes with 
*       Position.Bias
*  2    src-jtulach1.1         2/15/99  Jaroslav Tulach Jumps to current line
*  1    src-jtulach1.0         2/3/99   Jaroslav Tulach 
* $
*/
