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

package org.openide.compiler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/** Keeps a StringBuffer of read characters.
*/
final class MarkableInputStream extends BufferedInputStream {

    /** Read characters. */
    StringBuffer buff;

    /** @param is an InputStream - like in BufferedInputStream
    */
    public MarkableInputStream(InputStream is) {
        super(is);
        buff = new StringBuffer(300);
    }

    /** Read a char */
    public int read() throws IOException {
        int ret = super.read();
        if (ret >= 0) {
            buff.append((char) ret);
        }
        return ret;
    }

    /**
    * @param start
    * @param len
    * @return substring of the kept StringBuffer
    */
    public String substring(int start, int len) {
        return buff.toString().substring(start, len);
    }

    /**
    * @param start
    * @return substring of the kept StringBuffer
    */
    public String substring(int start) {
        return buff.toString().substring(start);
    }
}

/*
 * Log
 *  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         9/14/99  Ales Novak      upgrade to 
 *       org.netbeans.lib.regexp.*
 *  3    src-jtulach1.2         7/30/99  Ales Novak      OROMatcher is left - GNU
 *       regexp is used
 *  2    src-jtulach1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
