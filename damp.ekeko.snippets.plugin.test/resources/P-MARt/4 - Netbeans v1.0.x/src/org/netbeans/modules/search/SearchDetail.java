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

package org.netbeans.modules.search;

import java.util.*;

import org.openide.util.enum.*;

import org.openidex.search.*;

/**
*/
public class SearchDetail implements DetailCookie {

    private final Vector detail;
    private boolean empty = true;

    public SearchDetail() {
        this.detail = new Vector();
    }

    public void add(Object detail) {
        this.detail.add(detail);
        empty = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    public Enumeration detail() {
        return new ArrayEnumeration(detail.toArray());
    }
}


/*
* Log
*  2    Gandalf-post-FCS1.0.2.0     2/24/00  Ian Formanek    Post FCS changes
*  1    Gandalf   1.0         1/11/00  Petr Kuzel      
* $ 
*/ 

