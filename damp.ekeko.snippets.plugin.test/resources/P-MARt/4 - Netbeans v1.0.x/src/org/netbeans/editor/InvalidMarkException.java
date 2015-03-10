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

package org.netbeans.editor;

/**
* This exception is thrown either if the mark is invalid and it should
* be valid (<CODE>getOffset(), getLine(), remove()</CODE>) or on
* the oposite side if the mark is valid and it shouldn't be
* i.e. <CODE>insertMark()</CODE>
*
* @author Miloslav Metelka
* @version 1.00
*/

public class InvalidMarkException extends Exception {

    static final long serialVersionUID =-7408566695283816594L;
    InvalidMarkException() {
        super();
    }

    InvalidMarkException(String s) {
        super(s);
    }

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.1.0     3/8/00   Miloslav Metelka 
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

