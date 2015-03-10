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

package org.openide.util.io;

import java.io.IOException;

/** Special IOException that is used to signal that the write operation
* failed but the underlaying stream is not corrupted and can be used
* for next operations.
*
*
* @author Jaroslav Tulach
*/
public class SafeException extends IOException {
    /** the exception encapsulated */
    private Exception ex;

    static final long serialVersionUID =4365854085401413604L;
    /** Default constructor.
    */
    public SafeException(Exception ex) {
        this.ex = ex;
    }

    /** @return the encapsulated exception.
    */
    public Exception getException () {
        return ex;
    }
}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
