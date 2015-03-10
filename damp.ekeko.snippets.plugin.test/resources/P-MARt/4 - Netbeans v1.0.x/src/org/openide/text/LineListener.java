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

import javax.swing.text.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.util.WeakListener;

/** Listener to changes in the document.
*
* @author Jaroslav Tulach
*/
final class LineListener extends Object
    implements javax.swing.event.DocumentListener {
    /** original count of lines */
    private int orig;
    /** document to work with */
    public final StyledDocument doc;
    /** root element of all lines */
    private Element root;
    /** last tested amount of lines */
    private int lines;
    /** operations on lines */
    private LineStruct struct;

    /** Creates new LineListener */
    public LineListener (StyledDocument doc) {
        this.doc = doc;
        this.struct = new LineStruct ();
        root = NbDocument.findLineRootElement (doc);
        orig = lines = root.getElementCount ();
        doc.addDocumentListener (WeakListener.document (this, doc));
    }

    /** Getter for amount of lines */
    public int getOriginalLineCount () {
        return orig;
    }

    /** Convertor between old and new line sets */
    public int getLine (int i) {
        return struct.originalToCurrent (i);
    }

    public void removeUpdate(javax.swing.event.DocumentEvent p0) {
        int elem = root.getElementCount ();
        int delta = lines - elem;
        lines = elem;

        if (delta > 0) {
            struct.deleteLines (
                NbDocument.findLineNumber (doc, p0.getOffset ()),
                delta
            );
        }
    }

    public void changedUpdate(javax.swing.event.DocumentEvent p0) {
    }

    public void insertUpdate(javax.swing.event.DocumentEvent p0) {
        int elem = root.getElementCount ();
        int delta = elem - lines;
        lines = elem;

        if (delta > 0) {
            struct.insertLines (
                NbDocument.findLineNumber (doc, p0.getOffset ()),
                delta
            );
        }
    }
}

/*
* Log
*  3    Gandalf   1.2         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/27/99  Jaroslav Tulach 
* $
*/