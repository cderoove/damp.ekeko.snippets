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
import javax.swing.event.*;

import org.openide.util.WeakListener;

/** Position that stays at the same place if someone inserts
* directly to its offset.
*
* @author Jaroslav Tulach
*/
class BackwardPosition extends Object
    implements Position, DocumentListener {
    /** positions current offset */
    private int offset;

    /** Constructor.
    */
    private BackwardPosition(int offset) {
        this.offset = offset;
    }

    /** @param doc document
    * @param offset offset
    * @return new instance of the position
    */
    public static Position create (Document doc, int offset) {
        BackwardPosition p = new BackwardPosition (offset);
        doc.addDocumentListener (WeakListener.document (p, doc));
        return p;
    }

    //
    // Position
    //

    /** @return the offset
    */
    public int getOffset () {
        return offset;
    }

    //
    // document listener
    //


    /** Updates */
    public void insertUpdate(DocumentEvent e) {
        // less, not less and equal
        if (e.getOffset () < offset) {
            offset += e.getLength ();
        }
    }

    /** Updates */
    public void removeUpdate(DocumentEvent e) {
        int o = e.getOffset ();
        if (o < offset) {
            offset -= e.getLength ();
            // was the position in deleted range? => go to its beginning
            if (offset < o) {
                offset = o;
            }
        }
    }

    /** Nothing */
    public void changedUpdate(DocumentEvent e) {
    }
}


/*
* Log
*  4    src-jtulach1.3         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    src-jtulach1.0         1/29/99  Jaroslav Tulach 
* $
*/
