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

/**
* Attempt to insert or remove from the guarded block has been done.
*
* @version 1.0
* @author Miloslav Metelka
*/

public class GuardedException extends BadLocationException {

    static final long serialVersionUID =-8139460534188487509L;
    public GuardedException(String s, int offs) {
        super (s, offs);
    }

}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         3/18/99  Miloslav Metelka 
 * $
 */

