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

package org.openide.execution;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownServiceException;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/** Output stream for URLConnection classes that connects FileObjects
* with another party
*
* @author Ales Novak
* @version 0.10 May 15, 1998
*/
class FileObjectOutputStream
    extends BufferedOutputStream {

    /** underlying OutputStream */
    private OutputStream fos;

    /** lock */
    private FileLock flock;

    /**
    * @param os is an OutputStream for writing in
    * @param lock is a lock for the stream
    */
    public FileObjectOutputStream(OutputStream os, FileLock lock)
    throws IOException {
        super(os);
        flock = lock;
    }

    /** overriden */
    public void close()
    throws IOException {
        flock.releaseLock();
        super.close();
    }
}

/*
 * Log
 *  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 */
