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

import java.util.Collection;

import org.openide.util.Mutex;

/** Basic interface that defines the object that can be compiled.
* Each such object has to hold a set of its dependencies and also a set
* of Compilers that should be used to compile it.
* <P>
* The semantics says that before the Compilers can run all other 
* Compileable objects that this one depends on must already be compiled.
* <P>
* The third important method of Compileable is that it should have 
* equals method overriden to correctly recognize all other compileable
* objects that represents the same compilation. But *WITHOUT* checking
* 
*
* @author Jaroslav Tulach
*/
public interface Compilable {
    /** Mutex to synchronize in adding/changing and reading dependencies
    * between compilable objects. When a dependency that influence result 
    * of dependsOn method is changing the thread should first request
    * MUTEX.readAccess to do it. So when someone wants to construct the 
    * tree of all dependencies (like CompilationEngine) it can aquire 
    * MUTEX.writeAccess and it should be guaranteed that no modification
    * to dependsOn method occures.
    */
    public static Mutex MUTEX = new Mutex ();

    /** A collection of all compilers that have to be compiled
    * to finish compilation of this Compilable
    *
    * @return collection of Compiler
    */
    public Collection compilers ();

    /** A collection of other Compilable objects that have to be
    * finished before the compilers of this Compilable can be started.
    *
    * @return collection of Compilable
    */
    public Collection dependsOn ();

    /** Equal method should be implemented to return true for all
    * Compilable objects that are "compiled in the same way" but
    * ignoring their dependencies.
    *
    * @param other the other object
    * @return true if both look like equal
    */
    public boolean equals (Object other);
}

/*
* Log
*  1    Gandalf   1.0         12/23/99 Jaroslav Tulach 
* $ 
*/ 
