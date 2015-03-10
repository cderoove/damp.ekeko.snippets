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

import java.util.NoSuchElementException;

import org.openide.nodes.Node;
import org.openide.src.SourceElement;

/** A cookie for obtaining a source hierarchy from data objects.
*
* @author Dafe Simonek, Petr Hamernik
*/
public interface SourceCookie extends Node.Cookie {

    /** Returns a source element describing the hierarchy
    * of the source.
    *
    * @return the element
    */
    public SourceElement getSource ();

    /** Extended source cookie permitting for bidirectional translation with Swing text elements.
    */
    public interface Editor extends SourceCookie, EditorCookie {
        /** Translate a source element to text.
        *
        * @param element an element from the source hierarchy
        * @return a text element
        */
        public javax.swing.text.Element sourceToText(org.openide.src.Element element);

        /** Translate a text element to a source element, if it is possible to do so.
        *
        * @param element a text element
        * @return the element from the source hierarchy
        * @exception NoSuchElementException if the text element doesn't match
        *  any element from the source hierarchy
        */
        public org.openide.src.Element textToSource(javax.swing.text.Element element)
        throws NoSuchElementException;

        /** Find the element at the specified offset in the document.
        * @param offset The position of the element
        * @return the element at the position.
        */
        public org.openide.src.Element findElement(int offset);
    }
}

/*
* Log
*  8    src-jtulach1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    src-jtulach1.6         7/13/99  Petr Hamernik   findElement doesn't throw
*       NoSuchElementException
*  6    src-jtulach1.5         7/8/99   Petr Hamernik   SourceCookie.Editor 
*       extends EditorCookie
*  5    src-jtulach1.4         7/3/99   Petr Hamernik   new version of Editor 
*       cookie
*  4    src-jtulach1.3         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    src-jtulach1.2         3/10/99  Jesse Glick     [JavaDoc]
*  2    src-jtulach1.1         3/8/99   Petr Hamernik   
*  1    src-jtulach1.0         2/1/99   David Simonek   
* $
*/
