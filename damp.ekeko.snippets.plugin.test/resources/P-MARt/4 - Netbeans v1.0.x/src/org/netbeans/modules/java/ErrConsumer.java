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

package org.netbeans.modules.java;

import org.openide.filesystems.FileObject;

/**
*
* @author Ales Novak
* @version 0.10, Dec 04, 1997
*/

public interface ErrConsumer {
    public void pushError (FileObject errorFile,
                           int line,
                           int column,
                           String message,
                           String referenceText);
}

/*
 * Log
 *  4    src-jtulach1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         4/23/99  Petr Hrebejk    Classes temporay made 
 *       public
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
