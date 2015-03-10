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

import javax.swing.SwingUtilities;

import org.openide.windows.OutputWriter;

/** Dispatches writing for being executed tasks
*
* @author Ales Novak
* @version 0.10
*/
class WriteDispatcher
    implements Runnable {

    private OutputWriter ow;
    private byte[] buff;
    private int off, len;

    /** constructs new dispatcher
    * @param ow is an OutputWriter object to which will be written may be null
    * @param off is an offset if buff is null then this character is printed
    * @param len is a length
    */
    WriteDispatcher(OutputWriter ow, byte[] buff, int off, int len) {
        this.ow = ow;
        this.buff = buff;
        this.off = off;
        this.len = len;
        SwingUtilities.invokeLater(this);
    }

    public void run() {
        if (buff == null) { // print only one char
            ow.print((char)off);
        } else {
            ow.print(new String(buff, off, len));
        }
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
