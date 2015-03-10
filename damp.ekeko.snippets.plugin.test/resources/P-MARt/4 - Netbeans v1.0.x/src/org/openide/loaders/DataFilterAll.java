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

package org.openide.loaders;

import java.io.Serializable;

/** Filter that accepts everything.
* @author Jaroslav Tulach
*/
class DataFilterAll extends Object implements DataFilter, Serializable {
    static final long serialVersionUID =-760448687111430451L;
    public boolean acceptDataObject (DataObject obj) {
        return true;
    }

    /** Gets a resolvable. */
    public Object writeReplace() {
        return new Replace();
    }

    static class Replace implements Serializable {
        static final long serialVersionUID =3204495526835476127L;
        public Object readResolve() {
            return DataFilter.ALL;
        }
    }
}

/*
* Log
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         1/20/99  Petr Hamernik   
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
