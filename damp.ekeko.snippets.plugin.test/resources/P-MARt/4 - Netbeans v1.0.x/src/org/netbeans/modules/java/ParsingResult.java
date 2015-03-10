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

package org.netbeans.modules.java;

import java.util.LinkedList;
import java.util.HashMap;

import org.openide.text.PositionBounds;
import org.openide.src.*;

/** This class holds the result of the parsing and transports it
* from the parser to the SourceElementImpl.
*
* @author Petr Hamernik
*/
final class ParsingResult extends Object {
    /** Package */
    Identifier packageId;
    /** Package bounds */
    PositionBounds packageBounds;

    /** Imports (class Import) */
    LinkedList imports;
    /** Imports bounds (class PositionBounds) */
    LinkedList importsBounds;

    /** Classes (class ParsingResult.Class) */
    LinkedList classes;
	
    /** Creates new empty result. */
    ParsingResult() {
        imports = new LinkedList();
        importsBounds = new LinkedList();
        classes = new LinkedList();
    }

    /** This object represents one class in the parsing result. */
    final static class Class extends Object {
        /** Implementation for this class */
        ClassElementImpl impl;

        /** Arrays of the members of this class */
        LinkedList initializers;
        LinkedList fields;
        LinkedList constructors;
        LinkedList methods;
        LinkedList classes;

        /** Creates new Class for the impl */
        Class(ClassElementImpl impl) {
            this.impl = impl;
            initializers = new LinkedList();
            fields = new LinkedList();
            constructors = new LinkedList();
            methods = new LinkedList();
            classes = new LinkedList();
        }
    }
}

/*
* Log
*  5    Gandalf-post-FCS1.3.2.0     2/24/00  Ian Formanek    Post FCS changes
*  4    src-jtulach1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    src-jtulach1.1         4/1/99   Petr Hamernik   
*  1    src-jtulach1.0         3/29/99  Petr Hamernik   
* $
*/
