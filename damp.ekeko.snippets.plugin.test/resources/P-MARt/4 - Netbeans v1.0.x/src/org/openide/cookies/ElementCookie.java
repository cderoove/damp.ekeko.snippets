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

package org.openide.cookies;

import org.openide.nodes.Node;

/**
* Cookie permitting an object to be used in the Object Browser differently than in the Explorer.
* Should be implemented by nodes (e.g. Java data objects) which want to provide a variant
* representation.
* @see org.openide.loaders.DataObjectFilter
* @author Jaroslav Tulach
*/
public interface ElementCookie extends Node.Cookie {
    /**
     * Get the alternate node representation.
     * @return the node
     * @see org.openide.loaders.DataObject#getNodeDelegate
    */
    public Node getElementsParent ();

}

/*
* Log
*  5    src-jtulach1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    src-jtulach1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    src-jtulach1.2         4/2/99   Jesse Glick     [JavaDoc]
*  2    src-jtulach1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
