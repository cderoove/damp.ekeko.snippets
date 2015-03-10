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

package org.netbeans.modules.projects;

import java.io.*;

/**
 * Binary file
 * @author Petr Zajac
 * 
 */
public class BinFile extends RandomAccessFile {

    /**
     * Constructor declaration
     * 
     * @param folder folder of file
     * @param name name of file
     * 
     * @see
     */
    public BinFile (File folder, String name) throws IOException {
        super (folder, name);

        searchDirection = 1;

        top ();
    }

    /**
     * Constructor declaration
     * @param folder
     * @param name
     */
    public BinFile (String folder, String name) throws IOException {
        super (folder, name);

        searchDirection = 1;

        top ();
    }

    /**
     * Method declaration
     * 
     * @throws IOException
     */
    public final void bottom () throws IOException {
        seek (length ());
    }

    /**
     * Method declaration
     * 
     * @param abyte0 first array
     * @param abyte1 second array
     * 
     * @return true if arrays are equivalent
     */
    private boolean compareBytes (byte abyte0[], byte abyte1[]) {
        int length1 = abyte0.length;

        if (length1 != abyte1.length) {
            return false;
        }

        int i = 0;

        for (int j = length1; i < j; i++) {
            if (abyte0[i] != abyte1[i]) {
                return false;

            }
        }

        return true;
    }

    /**
     * Method declaration
     * @return SEARCH_FORWARD or SEARCH_FORWARD
     */
    public final int getSearchDirection () {
        return searchDirection;
    }

    /**
     * Method declaration
     * @return int value which is readed
     * @throws IOException
     */
    public final int readIntValue () throws IOException {
        int i = read ();
        int j = read ();
        int k = read ();
        int l = read ();

        if ((i | j | k | l) < 0) {
            throw new EOFException ();
        } else {
            return (l << 24) + (k << 16) + (j << 8) + (i << 0);
        }
    }

    /**
     * read shorr
     * @return short
     * @throws IOException
     */
    public final short readShortValue () throws IOException {
        int i = read ();
        int j = read ();

        if ((i | j) < 0) {
            throw new EOFException ();
        } else {
            return (short) ((j << 8) + (i << 0));
        }
    }

    /**
     * Read unsigned short value
     * @return readed value
     * 
     * @throws IOException
     */
    public final int readUnsignedShortValue () throws IOException {
        int i = read ();
        int j = read ();

        if ((i | j) < 0) {
            throw new EOFException ();
        } else {
            return (j << 8) + (i << 0);
        }
    }

    /**
     * Search sequence of bytes
     * @param abyte0 input sequence
     * @return offset in file or -1 if it isn't founded
     */
    public final long search (byte abyte0[]) {
        long  l = -1L;
        long  l1 = 0L;
        long  length = abyte0.length;
        int   searchDirection = getSearchDirection ();
        byte  abyte1[] = new byte[(int) length];

        try {
            long  offset1 = getFilePointer ();
            long  fileLength = length ();

            if (searchDirection == 1) {
                if (offset1 + length > fileLength) {
                    return l;
                }
            } else {
                if (offset1 - length < 0L) {
                    return l;
                }

                offset1 -= length;
            }

            seek (offset1);

            if (searchDirection == 1) {
                while (offset1 + length <= fileLength) {
                    readFully (abyte1);

                    if (compareBytes (abyte0, abyte1)) {
                        l = 0L;

                        break;
                    }

                    offset1++;

                    seek (offset1);
                }

            } else {
                while (offset1 > 0L) {
                    readFully (abyte1);

                    if (compareBytes (abyte0, abyte1)) {
                        l = 0L;

                        break;
                    }

                    offset1--;

                    seek (offset1);
                }
            }

            if (l != -1L) {
                l = offset1;
            }
        } catch (IOException ioexception) {
            System.err.println (ioexception.getMessage ());
            ioexception.printStackTrace (System.err);

            l = -1L;
        }

        return l;
    }

    /**
     * Set search direction.
     * @param i can be SEARCH_FORWARD or SEARCH_FORWARD
     */
    public final void setSearchDirection (int i) {
        searchDirection = i;
    }

    /**
     * Seek to top of file.
     * @throws IOException
     */
    public final void top () throws IOException {
        seek (1L);
    }

    public static final int SEARCH_FORWARD = 1;
    public static final int SEARCH_BACKWARD = 2;
    private int             searchDirection;
}

/*
 * Log
 *  1    Gandalf   1.0         1/3/00   Martin Ryzl     
 * $
 */
