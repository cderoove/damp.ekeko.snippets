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

/** Exception created when a set of compilers should be compiled and
* there is a cyclic dependency between them.
* <P>
* The exception carries list of objects that form the cycle. 
*
* @author Jaroslav Tulach
*/

public final class DependencyException extends Exception {

    /** array of Compilable */
    private Compilable[] array;

    /** Creates new DependencyException. */
    DependencyException (Compilable[] comp) {
        array = comp;
    }

    /** Getter for list of compilables that form the cycle.
    * @return array of compilables
    */
    public Compilable[] getCompilables () {
        return array;
    }

}

/*
* Log
*  1    Gandalf   1.0         12/23/99 Jaroslav Tulach 
* $ 
*/ 
