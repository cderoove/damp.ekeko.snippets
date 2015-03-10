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

package org.netbeans.editor.ext;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import org.netbeans.editor.Analyzer;

/**
* Management of storage of the data for the java completion.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class FileStorage {

    /** Constant for checking the maximum size of the string.
    * If the string size exceeds this value the error is thrown
    * as there's very likely corruption of the file.
    */
    private static final int MAX_STRING = 60000;

    private static final int BYTES_INCREMENT = 2048;

    private static final byte[] EMPTY_BYTES = new byte[0];

    File file;

    boolean openedForWrite;

    /** File descriptor */
    RandomAccessFile raf;

    /** Current offset in the bytes array */
    int offset;

    /** Byte array holding the data that were read from file */
    byte[] bytes = EMPTY_BYTES;

    /** Shared char array to use for reading strings */
    char[] chars = Analyzer.EMPTY_CHAR_ARRAY;

    /** String cache */
    StringCache strCache;

    /** @param fileName name of file to operate over
    */
    public FileStorage(String fileName) {
        this(fileName, new StringCache());
    }

    public FileStorage(String fileName, StringCache strCache) {
        file = new File(fileName);
        this.strCache = strCache;
    }

    public void open(boolean requestWrite) throws IOException {
        if (raf != null) {
            if (openedForWrite == requestWrite) {
                raf.seek(file.length());
                return; // already opened with correct type
            } else { // opened with different type
                close();
            }
        }

        if (requestWrite) { // check existency
            if (!file.exists()) {
                file.createNewFile();
            }
        }

        // open the file
        raf = new RandomAccessFile(file, requestWrite ? "rw" : "r"); // NOI18N
        raf.seek(file.length());
        openedForWrite = requestWrite;
    }

    public void close() throws IOException {
        if (raf != null) {
            raf.close();
            raf = null;
        }
    }

    /** Check size of bytes[] array */
    private void checkBytesSize(int len) {
        if (bytes.length < len) {
            byte[] newBytes = new byte[len + BYTES_INCREMENT];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }
    }

    /** Read some part of the file into the begining of bytes array
    * and reset offset to zero.
    */
    public void read(int len) throws IOException {
        checkBytesSize(len);
        raf.readFully(bytes, 0, len);
        offset = 0;
    }

    /** Write bytes array (with offset length) to the file */
    public void write() throws IOException {
        if (offset > 0) {
            raf.write(bytes, 0, offset);
        }
        offset = 0;
    }

    public void seek(int filePointer) throws IOException {
        raf.seek(filePointer);
    }

    public String getFileName() {
        return file.getAbsolutePath();
    }

    public int getFilePointer() throws IOException {
        return (int)raf.getFilePointer();
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getFileLength() throws IOException {
        return (int)file.length();
    }

    public void resetBytes() {
        bytes = EMPTY_BYTES;
    }

    /** Reset the size of the file and set current offset to zero. */
    public void resetFile() throws IOException {
        open(true);
        offset = 0;
        raf.setLength(0);
        close();
    }

    /** Get the integer value from the bytes[] array */
    public int getInteger() {
        int i = bytes[offset++];
        i = (i << 8) + (bytes[offset++] & 255);
        i = (i << 8) + (bytes[offset++] & 255);
        i = (i << 8) + (bytes[offset++] & 255);
        return i;
    }

    /** Get the string value from the bytes[] array */
    public String getString() {
        int len = getInteger(); // length of string
        if (len < 0) {
            if (len == -1) {
                return null;
            } else { // consistency error
                throw new Error("Consistency error: read string length=" + len); // NOI18N
            }
        }

        if (len > MAX_STRING) {
            throw new Error("FileStorage: String len is " + len // NOI18N
                            + ". There's probably a corruption in the file '" // NOI18N
                            + getFileName() + "'."); // NOI18N
        }

        if (chars.length < len) { // check chars array size
            chars = new char[2 * len];
        }
        for (int i = 0; i < len; i++) {
            chars[i] = (char)((bytes[offset] << 8) + (bytes[offset + 1] & 255));
            offset += 2;
        }

        String s = null;
        if (len >= 0) {
            if (strCache != null) {
                s = strCache.getString(chars, 0, len);
            } else { // no string cache
                s = new String(chars, 0, len);
            }
        }

        return s;
    }

    /** Put the integer into bytes[] array. It is stored as four bytes
    * in big endian.
    */
    public void putInteger(int i) {
        checkBytesSize(offset + 4); // int size
        bytes[offset + 3] = (byte)(i & 255);
        i >>>= 8;
        bytes[offset + 2] = (byte)(i & 255);
        i >>>= 8;
        bytes[offset + 1] = (byte)(i & 255);
        i >>>= 8;
        bytes[offset] = (byte)i;
        offset += 4;
    }

    /** Put the string into bytes[] array. First the length is stored
    * by putInteger() and then all the characters as two bytes each in big
    * endian.
    */
    public void putString(String s) {
        if (s == null) {
            putInteger(-1);
            return;
        }

        int len = s.length();
        putInteger(len);

        if (len > 0) {
            checkBytesSize(offset + len * 2);
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                bytes[offset + 1] = (byte)(ch & 255);
                ch >>>= 8;
                bytes[offset] = (byte)(ch & 255);
                offset += 2;
            }
        }
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Miloslav Metelka Localization
 *  5    Gandalf   1.4         11/14/99 Miloslav Metelka 
 *  4    Gandalf   1.3         11/8/99  Miloslav Metelka 
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/8/99   Miloslav Metelka 
 * $
 */

