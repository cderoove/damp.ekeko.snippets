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

package org.openidex.search;

import org.openide.nodes.*;
import java.util.*;

/**
 * Searched node can be equipped with this detail cookie.
 * Currently is recognized String and StructuredDetail detail.
 *
 * @author  Petr Kuzel
 * @version 
 */
public interface DetailCookie extends Node.Cookie {

    /**
    * @return Enumeration of SearchType.getDetailClesses details. 
    */
    public Enumeration detail();
}