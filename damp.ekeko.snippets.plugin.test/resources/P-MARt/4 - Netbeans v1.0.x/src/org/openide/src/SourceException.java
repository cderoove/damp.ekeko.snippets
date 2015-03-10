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

package org.openide.src;

/** General exception for the source elements hierarchy. */
public class SourceException extends Exception {
    static final long serialVersionUID =4472081442050042697L;
    /** Create an exception. */
    public SourceException() {
        this(""); // NOI18N
    }
    /** Create an exception with a detail message.
    * @param msg the message
    */
    public SourceException(String msg) {
        super(msg);
    }
}


/*
 * Log
 *  8    src-jtulach1.7         1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    src-jtulach1.5         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         2/17/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/16/99  Petr Hamernik   
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 */
