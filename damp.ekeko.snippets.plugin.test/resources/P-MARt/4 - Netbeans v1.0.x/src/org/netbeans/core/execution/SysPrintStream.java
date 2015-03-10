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

import java.io.OutputStream;
import java.io.PrintStream;

import org.openide.TopManager;
import org.openide.windows.OutputWriter;

/** used for redirection System.out/err
*
* @author Ales Novak
* @version 0.10 Dec 03, 1997
*/
public class SysPrintStream extends PrintStream {

    /** OutputWriter */
    private OutputWriter ow;

    public SysPrintStream (final OutputWriter ow) {
        super(createOutputStream (ow));

        this.ow = ow;
    }

    private static OutputStream createOutputStream(final OutputWriter ow) {
        return new OutputStream () {
                   public void write(int b) {
                       new WriteDispatcher(ow, null, b, -1);
                   }

                   public void write(byte[] buff, int off, int len) {
                       String s = new String(buff, off, len);
                       byte[] nbuff = new byte[len];
                       System.arraycopy(buff, off, nbuff, 0, len);
                       new WriteDispatcher(ow, nbuff, off, len);
                   }

                   public void flush () {
                       ow.flush ();
                   }

                   public void close () {
                       ow.close ();
                   }
               };
    }


    /** resets this stream */
    void reset() {
        try {
            ow.reset();
        } catch (java.io.IOException ex) {
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         2/4/99   Petr Hamernik   changes to be compiled 
 *       by jikes
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
