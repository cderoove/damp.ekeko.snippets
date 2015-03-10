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

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
* Attempt to insert or remove from the guarded block has been done.
*
* @version 1.0
* @author Miloslav Metelka
*/

public class GuardedDocumentEvent extends BaseDocumentEvent {

    static final long serialVersionUID =-9204897347010955248L;

    public GuardedDocumentEvent(GuardedDocument doc, int offset, int length,
                                DocumentEvent.EventType type) {
        super(doc, offset, length, type);
    }

    public void undo() throws CannotUndoException {
        GuardedDocument gdoc = (GuardedDocument)getDocument();
        boolean origBreak = gdoc.breakGuarded;
        gdoc.breakGuarded = true;
        super.undo();
        if (!origBreak) {
            gdoc.breakGuarded = false;
        }
    }

    public void redo() throws CannotRedoException {
        GuardedDocument gdoc = (GuardedDocument)getDocument();
        boolean origBreak = gdoc.breakGuarded;
        super.redo();
        if (!origBreak) {
            gdoc.breakGuarded = false;
        }
    }

}

/*
 * Log
 *  3    Gandalf-post-FCS1.2         4/4/00   Miloslav Metelka 
 *  2    Gandalf-post-FCS1.1         4/3/00   Miloslav Metelka undo update
 *  1    Gandalf-post-FCS1.0         3/8/00   Miloslav Metelka 
 * $
 */

