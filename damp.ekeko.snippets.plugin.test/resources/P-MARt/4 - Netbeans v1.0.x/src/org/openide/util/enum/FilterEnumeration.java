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

package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/** Abstract class that takes an enumeration and filters its elements.
* To get this class fully work one must override <CODE>accept</CODE> method.
* Objects in the enumeration must not be <CODE>null</CODE>.
*
* @author Jaroslav Tulach
* @version 0.10 September 11, 1997
*/
public abstract class FilterEnumeration extends Object implements Enumeration {
    /** enumeration to filter */
    private Enumeration en;

    /** object to be returned next time */
    private Object next;

    /**
    * @param en enumeration to filter
    */
    public FilterEnumeration (Enumeration en) {
        this.en = en;
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected abstract boolean accept (Object o);

    /** Tries to find next object. And sets it to the next field.
    * @return true if there is such one, false if there is not
    */
    private boolean getNext () {
        if (next != null) {
            // there is a object already prepared
            return true;
        }
        while (en.hasMoreElements ()) {
            // read next
            next = en.nextElement ();
            if (accept (next)) {
                // if the object is accepted
                return true;
            };
        }
        next = null;
        return false;
    }

    /** @return true if there is more elements in the enumeration
    */
    public boolean hasMoreElements () {
        return getNext ();
    }

    /** @return next object in the enumeration
    * @exception NoSuchElementException can be thrown if there is no next object
    *   in the enumeration
    */
    public Object nextElement () {
        if (!getNext ()) {
            throw new NoSuchElementException ();
        }
        Object res = next;
        next = null;
        return res;
    }
}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         4/16/99  Libor Martinek  
 *  2    Gandalf   1.1         1/5/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
