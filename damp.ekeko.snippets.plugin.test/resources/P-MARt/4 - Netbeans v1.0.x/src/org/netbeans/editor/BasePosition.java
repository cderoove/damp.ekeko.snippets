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

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Document;

/**
* Position in document. This is enhanced version of 
* Swing <CODE>Position</CODE> interface. It supports
* insert after feature. If Position has
* <CODE>insertAfter</CODE> flag set and text is inserted
* right at the mark's position, the position will NOT move.
*
* @author Miloslav Metelka
* @version 1.00
*/

class BasePosition implements Position {

    /** The mark that serves this position */
    private Mark mark;

    /** Construct new position at specified offset */
    BasePosition(DocOp op, int offset) throws BadLocationException {
        this(op,  offset, Position.Bias.Forward);
    }

    /** Construct new position with insert after flag specified */
    BasePosition(DocOp op, int offset, Position.Bias bias)
    throws BadLocationException {
        mark = op.insertMark(offset, bias == Position.Bias.Backward);
    }

    /** Get offset in document for this position */
    public int getOffset() {
        try {
            return mark.getOffset();
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return 0;
        }
    }

    /** Remove mark in finalize method */
    protected void finalize() throws Throwable {
        mark.remove();
        super.finalize();
    }

}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.1.0     3/8/00   Miloslav Metelka 
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/10/00  Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/10/99 Miloslav Metelka 
 *  3    Gandalf   1.2         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  2    Gandalf   1.1         3/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

