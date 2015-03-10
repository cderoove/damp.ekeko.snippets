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

/** Composes more enumerations into one.
*
* @author Jaroslav Tulach
* @version 0.10, Apr 07, 1998
*/
public class SequenceEnumeration extends Object implements Enumeration {
    /** enumeration of Enumerations */
    private Enumeration en;
    /** current enumeration */
    private Enumeration current;

    /** Constructs new enumeration from already existing. The elements
    * of <CODE>en</CODE> should be also enumerations. The resulting
    * enumeration contains elements of such enumerations.
    *
    * @param en enumeration of Enumerations that should be sequenced
    */
    public SequenceEnumeration (Enumeration en) {
        this.en = en;
    }

    /** Composes two enumerations into one.
    * @param first first enumeration
    * @param second second enumeration
    */
    public SequenceEnumeration (Enumeration first, Enumeration second) {
        this (new ArrayEnumeration (new Enumeration[] { first, second }));
    }

    /** Ensures that current enumeration is set.
    * @return the current enumeration or null if there is no next enumeration
    */
    private Enumeration ensureCurrent () {
        while (current == null || !current.hasMoreElements ()) {
            if (en.hasMoreElements ()) {
                current = (Enumeration)en.nextElement ();
            } else {
                // no next valid enumeration
                return null;
            }
        }
        return current;
    }

    /** @return true if we have more elements */
    public boolean hasMoreElements () {
        return ensureCurrent () != null;
    }

    /** @return next element
    * @exception NoSuchElementException if there is no next element
    */
    public synchronized Object nextElement () {
        if (ensureCurrent () != null) {
            return current.nextElement ();
        } else {
            throw new java.util.NoSuchElementException ();
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         1/5/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
