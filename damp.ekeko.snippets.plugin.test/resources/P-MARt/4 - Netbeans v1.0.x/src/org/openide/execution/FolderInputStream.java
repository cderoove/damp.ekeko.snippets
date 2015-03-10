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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.openide.filesystems.FileObject;

/** The class allows reading of folder via URL. Because of html
* oriented user interface the document has html format.
*
* @author Ales Novak
* @version 0.10 May 15, 1998
*/
class FolderInputStream
    extends InputStream {

    /** delegated reader that reads the document */
    private StringReader reader;

    /**
    * @param folder is a folder
    */
    public FolderInputStream (FileObject folder)
    throws IOException {
        reader = new StringReader(createDocument(folder));
    }

    /** creates html document as string */
    private String createDocument(FileObject folder)
    throws IOException {
        StringBuffer buff = new StringBuffer(150);
        StringBuffer lit = new StringBuffer(15);
        FileObject[] fobia = folder.getChildren();
        String name;

        buff.append("<HTML>\n"); // NOI18N
        buff.append("<BODY>\n"); // NOI18N

        FileObject parent = folder.getParent();
        if (parent != null) {
            lit.setLength(0);
            lit.append('/').append(parent.getPackageName('/'));
            buff.append("<P>"); // NOI18N
            buff.append("<A HREF=").append(lit).append(">").append("..").append("</A>").append("\n"); // NOI18N
            buff.append("</P>"); // NOI18N
        }

        for (int i = 0; i < fobia.length; i++) {
            lit.setLength(0);
            lit.append('/').append(fobia[i].getPackageName('/'));
            name = fobia[i].getName();
            if (!fobia[i].isFolder()) {
                lit.append(".").append(fobia[i].getExt()); // NOI18N
                name = name + "." + fobia[i].getExt(); // NOI18N
            }
            buff.append("<P>"); // NOI18N
            buff.append("<A HREF=").append(lit).append(">").append(name).append("</A>").append("\n"); // NOI18N
            buff.append("</P>"); // NOI18N
        }

        buff.append("</BODY>\n"); // NOI18N
        buff.append("</HTML>\n"); // NOI18N
        return buff.toString();
    }

    //************************************** stream methods **********
    public int read() throws IOException {
        return reader.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
        char[] ch = new char[len];
        int r = reader.read(ch, 0, len);
        for (int i = 0; i < r; i++)
            b[off + i] = (byte) ch[i];
        return r;
    }

    public long skip(long skip) throws IOException {
        return reader.skip(skip);
    }

    public void close() throws IOException {
        reader.close();
    }

    public void reset() throws IOException {
        reader.reset();
    }

    public void mark(int i) {
        try {
            reader.mark(i);
        } catch (IOException ex) {
        }
    }

    public boolean markSupported() {
        return reader.markSupported();
    }
}

/*
 * Log
 *  4    src-jtulach1.3         1/12/00  Ian Formanek    NOI18N
 *  3    src-jtulach1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/26/99  Jaroslav Tulach 
 * $
 */
