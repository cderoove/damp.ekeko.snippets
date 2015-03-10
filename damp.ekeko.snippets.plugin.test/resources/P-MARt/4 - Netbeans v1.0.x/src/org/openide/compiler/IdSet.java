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

import java.util.*;

/** Set that uses == to compare its objects
*
* @author  Jaroslav Tulach
*/
final class IdSet extends TreeSet implements Comparator {
    /** the Id comparator. That is the reason we implements Comparator */
    private static Comparator ID_COMPARATOR = new IdSet ();

    /** Creates new IdSet. */
    public IdSet() {
        super (ID_COMPARATOR);
    }

    public int compare(final java.lang.Object p1,final java.lang.Object p2) {
        return System.identityHashCode(p1) - System.identityHashCode (p2);
    }
}

/*
* Log
*  1    Gandalf   1.0         12/23/99 Jaroslav Tulach 
* $ 
*/ 
