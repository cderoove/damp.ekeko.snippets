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

package org.openide.filesystems;

import java.io.IOException;

/** Exception thrown to signal that external
* execution and compilation is not supported on a given filesystem.
*
* @author Jaroslav Tulach
* @version 0.10, Apr 15, 1998
*/
public class EnvironmentNotSupportedException extends IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1138390681913514558L;
    /** the throwing exception */
    private FileSystem fs;

    /**
    * @param fs filesystem that caused the error
    */
    public EnvironmentNotSupportedException (FileSystem fs) {
        this.fs = fs;
    }

    /**
    * @param fs filesystem that caused the error
    * @param reason text description for the error
    */
    public EnvironmentNotSupportedException (FileSystem fs, String reason) {
        super (reason);
        this.fs = fs;
    }

    /** Getter for the filesystem that does not support environment operations.
    */
    public FileSystem getFileSystem () {
        return fs;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/1/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
