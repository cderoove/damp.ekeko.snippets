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

package org.openide.src.nodes;

import java.lang.reflect.Modifier;

/* ? --jglick
* For convenience, implementations of this interface could use
* {@link Modifier} masks.
*/
/** Orders and filters members in a class element node.
* Can be used for methods, fields, inner classes, etc.
* <p>The semantics are very similar to those of <code>SourceElementFilter</code>.
* @see org.openide.src.ClassElement
* @see ClassChildren
*
* @author Dafe Simonek, Jan Jancura
*/
public class ClassElementFilter extends SourceElementFilter {
    // [PENDING] should initializers be included? --jglick

    /** Specifies a child representing a constructor. */
    public static final int     CONSTRUCTOR = 8;
    /** Specifies a child representing a field (instance variable). */
    public static final int     FIELD = 16;
    /** Specifies a child representing a method. */
    public static final int     METHOD = 32;
    /** Specifies a child representing the superclass of the node's class. */
    public static final int     EXTENDS = 64;
    /** Specifies a child representing an implemented interface of the node's class.
    * For a node representing an interface, this would specify an extended interface.
    */
    public static final int     IMPLEMENTS = 128;
    /** Does not specify a child type. */
    public static final int     ALL = SourceElementFilter.ALL | CONSTRUCTOR | FIELD | METHOD |
                                      EXTENDS | IMPLEMENTS;

    /** Default order and filtering.
    * Places all fields, constructors, methods, and inner classes (interfaces) together
    * in one block.
    */
    public static final int[]   DEFAULT_ORDER = {FIELD | CONSTRUCTOR | METHOD | CLASS | INTERFACE};

    // [PENDING] where is this used? --jglick
    /** Specifies a child which is static. */
    public static final int     STATIC = Modifier.STATIC;

    /** stores property value */
    private boolean             sorted = true;

    /** Test whether the elements in one element type group are sorted.
    * @return <code>true</code> if groups in getOrder () field are sorted, <code>false</code> 
    * to default order of elements
    */
    public boolean isSorted () {
        return sorted;
    }

    /** Set whether groups of elements returned by getOrder () should be sorted.
    * @param sorted <code>true</code> if so
    */
    public void setSorted (boolean sorted) {
        this.sorted = sorted;
    }
}

/*
* Log
*  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    src-jtulach1.5         7/1/99   Jan Jancura     Support for sorting
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/2/99   Jesse Glick     [JavaDoc]
*  2    src-jtulach1.1         4/2/99   Jan Jancura     ObjectBrowser Support
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
