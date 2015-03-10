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

package org.netbeans.core.execution;

import java.io.IOException;
import java.io.InputStream;

/** demutiplexes in-requests to task specific window
*
* @author Ales Novak
* @version 0.10 Dec 04, 1997
*/
class
    SysIn extends InputStream {

    public SysIn () {
    }

    /** reads one char */
    public int read () throws IOException {
        return ExecutionEngine.taskIOs.getIn().read ();
    }

    /** reads an array of bytes */
    public int read(byte[] b, int off, int len) throws IOException {
        return ExecutionEngine.taskIOs.getIn().read (b, off, len);
    }

    /** gives number of bytes that can be read without blocking */
    public int available () throws IOException {
        return ExecutionEngine.taskIOs.getIn().available ();
    }

    /** closes the stream */
    public void close () throws IOException {
        ExecutionEngine.taskIOs.getIn().close ();
    }

    /** marks position at position <code>x</code> */
    public void mark (int x) {
        ExecutionEngine.taskIOs.getIn().mark (x);
    }

    /** resets the stream */
    public void reset () throws IOException {
        ExecutionEngine.taskIOs.getIn().reset ();
    }

    /**
    * @return true iff mark is supported false otherwise
    */
    public boolean markSupported () {
        return ExecutionEngine.taskIOs.getIn().markSupported ();
    }

    /** skips <code>l</code> bytes
    * @return number of skipped bytes
    */
    public long skip (long l) throws IOException {
        return ExecutionEngine.taskIOs.getIn().skip (l);
    }
}

/*
 * Log
 *  3    Gandalf   1.2         11/4/99  Ales Novak      #4555
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
