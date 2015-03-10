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

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;

import org.openide.nodes.Node;

/** Provides ability for an object to be compiled.
* A method is provided for testing what recursive depth (into subfolders) of compilation
* is supported, and then a set of inner classes for variant
* compilation tasks such as compile, build, and clean tasks.
* Also permits adding of the compiler to a compiler job.
*
* @author Jaroslav Tulach
*/
public interface CompilerCookie extends Node.Cookie {
    /** Tests whether a specified depth is supported.
    * @param depth the depth to test
    * @return <code>true</code> if this cookie supports such depth
    */
    public boolean isDepthSupported (Compiler.Depth depth);

    /** Allows the cookie to add its compiler(s)
    * into a compiler job.
    * The <code>depth</code> parameter specifies whether or not
    * the cookie should continue with its children.
    * If the {@link Compiler.Depth#isLastDepth} is true then no children
    * should be processed. Otherwise process the children with
    * a new depth obtained by calling {@link Compiler.Depth#nextDepth}.
    *
    * @param job the compiler job to add the compiler for this cookie to
    * @param depth the depth to use for compilation
    */
    public void addToJob (CompilerJob job, Compiler.Depth depth);

    /** A cookie for conditional compilation.
     * Should be used by objects supporting compilation only when out of date.
    */
    public static interface Compile extends CompilerCookie {
    }

    /** A cookie for unconditional compilation.
     * The compile should be forced.
    */
    public static interface Build extends CompilerCookie {
    }

    /** A cookie for cleaning before compilation. Classes
    * supporting this cookie should delete every file that
    * can be produced by a compilation to clean up before
    * another one.
    */
    public static interface Clean extends CompilerCookie {
    }
}

/*
* Log
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/10/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
