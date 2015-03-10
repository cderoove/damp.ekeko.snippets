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

package org.netbeans.modules.emacs;

import java.io.PrintWriter;

public class EmacsException extends RuntimeException {

    private Exception e;

    public EmacsException () {
        e = null;
    }

    public EmacsException (String msg) {
        super (msg);
        e = null;
    }

    public EmacsException (Exception e) {
        super (e.getMessage ());
        this.e = e;
    }

    public Exception getException () {
        return e;
    }

    public void printStackTrace (PrintWriter pw) {
        super.printStackTrace (pw);
        if (e != null)
            e.printStackTrace (pw);
    }

}
