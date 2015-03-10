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

import java.io.*;

/**
* This class convert Reader to InputStream.
*
* @author   Petr Hamernik
* @version  0.10, January 22, 1998
*/
public class ReaderInputStream extends InputStream {

    /** Input Reader class. */
    Reader reader;

    /** We use this buffer to store second byte of read char, if it was not still
    * read from InputStream */
    int buffer;

    /** Signals, if buffer is currently used.*/
    boolean bufferUsed = false;

    /** Creates new input stream from the given reader.
    * @param reader Input reader
    */
    public ReaderInputStream(Reader reader) {
        this.reader = reader;
    }

    /** @return next byte from the Reader */
    private int nextByte() throws IOException {
        if (bufferUsed) {
            bufferUsed = false;
            return buffer;
        }
        else {
            int c = reader.read();
            if (c == -1)
                return -1;
            buffer = (c & 0x00FF);
            bufferUsed = true;
            return (c >> 8);
        }
    }

    /**
    * Reads the next byte of data from this input stream. The value
    * byte is returned as an <code>int</code> in the range
    * <code>0</code> to <code>255</code>. If no byte is available
    * because the end of the stream has been reached, the value
    * <code>-1</code> is returned. This method blocks until input data
    * is available, the end of the stream is detected, or an exception
    * is thrown.
    * <p>
    *
    * @return     the next byte of data, or <code>-1</code> if the end of the
    *             stream is reached.
    * @exception  IOException  if an I/O error occurs.
    */
    public int read() throws IOException {
        return nextByte();
    }

    /**
    * Skips over and discards <code>n</code> bytes of data from this
    * input stream. The <code>skip</code> method may, for a variety of
    * reasons, end up skipping over some smaller number of bytes,
    * possibly <code>0</code>. The actual number of bytes skipped is
    * returned.
    *
    * @param      n   the number of bytes to be skipped.
    * @return     the actual number of bytes skipped.
    * @exception  IOException  if an I/O error occurs.
    */
    public long skip(long n) throws IOException {
        int realySkip = 0;
        while (reader.ready()) {
            nextByte();
            realySkip++;
            if (realySkip >= n)
                break;
        }
        return realySkip;
    }

    /**
    * Returns the number of bytes that can be read from this input
    * stream without blocking.
    *
    * @return     the number of bytes that can be read from this input stream
    *             without blocking.
    * @exception  IOException  if an I/O error occurs.
    */
    public int available() throws IOException {
        return (reader.ready() ? 2 : 0);
    }

    /**
    * Closes this input stream and releases any system resources
    * associated with the stream.
    *
    * @exception  IOException  if an I/O error occurs.
    */
    public void close() throws IOException {
        reader.close();
    }
}

/*
 * Log
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
