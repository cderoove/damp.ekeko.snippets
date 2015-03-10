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

/** serves as a bridge between standard corona input in the OutWin and
* standard user input - System.in, usedr for redirecting requests from in
* to win.
*
* @author Ales Novak
* @version 0.10 Dec 03, 1997
*/
public class SysInStream extends java.io.InputStream {
    /** Reader that reads from a window */
    java.io.Reader in;

    /**
    * @param in is a Reader related to the window
    */
    public SysInStream (java.io.Reader in) {
        this.in = in;
    }

    /** reads one char */
    public int read () throws IOException {
        return in.read();
    }

    /** reads an array of bytes */
    public int read(byte[] b, int off, int len) throws IOException {
        char[] buff = new char[len];
        int read = in.read(buff);
        for (int i = read; --i >= 0; ) {
            b[off + i] = (byte) buff[i];
        }
        return read;
    }

    /** gives number of bytes that can be read without blocking */
    public int available () throws IOException {
        return in.ready() ? 1 : 0;
    }

    /** closes the stream */
    public void close () throws IOException {
        in.close ();
    }

    /** marks position at position <code>x</code> */
    public void mark (int x) {
        try {
            in.mark (x);
        } catch (IOException ee) {
        }
    }

    /** resets the stream */
    public void reset () throws IOException {
        in.reset ();
    }

    /**
    * @return true iff mark is supported false otherwise
    */
    public boolean markSupported () {
        return in.markSupported ();
    }

    /** skips <code>l</code> bytes
    * @return number of skipped bytes
    */
    public long skip (long l) throws IOException {
        return in.skip (l);
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
