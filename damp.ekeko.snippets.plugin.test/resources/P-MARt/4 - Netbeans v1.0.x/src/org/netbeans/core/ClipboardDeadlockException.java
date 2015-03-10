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

package org.netbeans.core;

/** Deadlock in clipboard exception.
*/
class ClipboardDeadlockException extends RuntimeException {
    static final long serialVersionUID =-1364388983136595533L;
    public ClipboardDeadlockException () {
        super ("Clipboard Deadlock"); // NOI18N
    }
}


/*
 * Log
 *  4    src-jtulach1.3         1/13/00  Jaroslav Tulach I18N
 *  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    src-jtulach1.0         2/26/99  Jaroslav Tulach 
 * $
 */
