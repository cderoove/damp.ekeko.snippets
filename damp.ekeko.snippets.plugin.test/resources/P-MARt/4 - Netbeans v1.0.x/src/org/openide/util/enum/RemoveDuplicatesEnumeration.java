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
import java.util.Hashtable;

/** Enumeration that scans through another one and removes duplicates.
* Two objects are duplicate if <CODE>one.equals (another)</CODE>.
*
* @author Jaroslav Tulach
* @version 0.10, May 12, 1998
*/
public class RemoveDuplicatesEnumeration extends FilterEnumeration {
    /** hashtable with all returned objects 
     * @associates Object*/
    private Hashtable all = new Hashtable ();

    /**
    * @param en enumeration to filter
    */
    public RemoveDuplicatesEnumeration (Enumeration en) {
        super (en);
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected boolean accept (Object o) {
        try {
            return all.get (o) == null;
        } finally {
            // adds the object to the queue
            all.put (o, o);
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
