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

import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;

/** Interface for filtering and ordering the items in the visual
* presentation of a source element.
* Used to control the children of a source element node.
* <p>Note that this does <em>not</em> fire events for changes
* in its properties; it is expected that a new filter will instead
* be created and applied to the source children.
*
* @see org.openide.src.SourceElement
* @see SourceChildren
* @author Dafe Simonek, Jan Jancura
*/
public class SourceElementFilter {

    /** Specifies a child representing a package or class import. */
    public static final int       IMPORT = 1;
    /** Specifies a child representing a (top-level) class. */
    public static final int       CLASS = 2;
    /** Specifies a child representing a (top-level) interface. */
    public static final int       INTERFACE = 4;
    /** Does not specify any top-level element. */
    public static final int       ALL = IMPORT + CLASS + INTERFACE;

    /** Specifies package-private member access. */
    public static final int       PACKAGE = 65536;
    /** Specifies public member access. */
    public static final int       PUBLIC = Modifier.PUBLIC;
    /** Specifies private member access. */
    public static final int       PRIVATE = Modifier.PRIVATE;
    /** Specifies protected member access. */
    public static final int       PROTECTED = Modifier.PROTECTED;
    /** Does not specify any member access. */
    public static final int       ALL_MODIFIERS = PROTECTED | PUBLIC | PRIVATE | PACKAGE;

    /** Default order of the top-level element types in the hierarchy.
    * A list, each of whose elements is a bitwise disjunction of element types.
    * By default, only classes and interfaces are listed, and these together.
    */
    public static final int[]     DEFAULT_ORDER = {CLASS + INTERFACE};

    /** stores property value */
    private boolean               allClasses = false;
    /** stores property value */
    private int[]                 order = null;
    /** stores property value */
    private int                   modifiers = ALL_MODIFIERS;


    /** Test whether all classes in the source should be recursively shown.
    * @return <code>true</code> to include inner classes/interfaces, <code>false</code> to only
    * include top-level classes/interfaces
    */
    public boolean isAllClasses () {
        return allClasses;
    }

    /** Set whether all classes should be shown.
    * @param type <code>true</code> if so
    * @see #isAllClasses
    */
    public void setAllClasses (boolean allClasses) {
        this.allClasses = allClasses;
    }

    /** Get the current order for elements.
    * @return the current order, as a list of bitwise disjunctions among element
    * types (e.g. {@link #CLASS}). If <code>null</code>, the {@link #DEFAULT_ORDER},
    * or no particular order at all, may be used.
    */
    public int[] getOrder () {
        return order;
    }

    /** Set a new order for elements.
    * Should update the children list of the source element node.
    * @param order the new order, or <code>null</code> for the default
    * @see #getOrder
    */
    public void setOrder (int[] order) {
        this.order = order;
    }

    /**
    * Get permitted access modes.
    * Members with excluded access modes will not be displayed.
    * @return a modifier mask, as a bitwise disjunction of modes, e.g. {@link #PROTECTED}
    */
    public int getModifiers () {
        return modifiers;
    }

    /**
    * Set permitted access modes.
    * @param modifier the new modifier mask
    * @see #getModifiers
    */
    public void setModifiers (int modifiers) {
        this.modifiers = modifiers;
    }
}

/*
* Log
*  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    src-jtulach1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    src-jtulach1.4         4/9/99   Jan Jancura     Fixed 1436 - default 
*       filtering for class elements
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/2/99   Jesse Glick     [JavaDoc]
*  2    src-jtulach1.1         4/2/99   Jan Jancura     ObjectBrowser Support
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
