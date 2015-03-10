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

import java.io.PrintStream;
import java.io.OutputStream;

import org.openide.windows.InputOutput;

/** demutiplexes err-requests to task specific window
*
* @author Ales Novak
* @version 0.10 Dec 04, 1997
*/
class SysErr extends PrintStream {

    public SysErr () {
        super(createOutputStream());
    }

    private static OutputStream createOutputStream() {
        return new OutputStream() {
                   public void write (int b) {
                       ExecutionEngine.taskIOs.getErr().write (b);
                   }
                   public void write(byte[] buff, int off, int len) {
                       ExecutionEngine.taskIOs.getErr().write(buff, off, len);
                   }
                   public void flush () {
                       ExecutionEngine.taskIOs.getErr().flush ();
                   }
                   public void close () {
                       ExecutionEngine.taskIOs.getErr().close ();
                   }
               };
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
