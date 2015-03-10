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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/** Encapsulates an exception.
*
* @author Ales Novak
*/
public class FoldingIOException extends IOException {

    /** Foreign exception */
    private Throwable t;

    static final long serialVersionUID =1079829841541926901L;
    /**
    * @param t a foreign folded Throwable
    */
    public FoldingIOException(Throwable t) {
        super(t.getMessage());
        this.t = t;
    }
    /** Prints stack trace of the foreign exception */
    public void printStackTrace() {
        t.printStackTrace();
    }
    /** Prints stack trace of the foreign exception */
    public void printStackTrace(java.io.PrintStream s) {
        t.printStackTrace(s);
    }
    /** Prints stack trace of the foreign exception */
    public void printStackTrace(java.io.PrintWriter s) {
        t.printStackTrace(s);
    }
    /**
    * @return toString of the foreign exception
    */
    public String toString() {
        return t.toString();
    }
    /**
    * @return getLocalizedMessage of the foreign exception 
    */
    public String getLocalizedMessage() {
        return t.getLocalizedMessage();
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/17/00  Ales Novak      #5399
 *  3    Gandalf   1.2         11/26/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/1/99  Ales Novak      
 * $
 */
