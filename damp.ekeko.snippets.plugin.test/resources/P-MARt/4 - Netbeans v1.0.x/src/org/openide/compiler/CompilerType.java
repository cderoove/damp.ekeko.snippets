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

import java.util.*;

import org.openide.ServiceType;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;

/** Base class defining method for compilation service.
*
* @author Jaroslav Tulach
*/
public abstract class CompilerType extends ServiceType {
    static final long serialVersionUID =-5093377800217789288L;

    public HelpCtx getHelpCtx () {
        return new HelpCtx (CompilerType.class);
    }

    /** Prepare a data object for compilation.
    * Implementations should create an instance of a
    * suitable subclass of {@link Compiler}, passing
    * the compiler job to the constructor so that the job may
    * register the compiler.
    *
    * @param job compiler job to add compilers to
    * @param type the type of compilation task to manage
    * ({@link org.openide.cookies.CompilationCookie.Compile}, etc.)
    * @param obj data object to prepare for compilation
    */
    public abstract void prepareJob (CompilerJob job, Class type, DataObject obj);

    /** Get all registered compilers.
    * @return enumeration of <code>CompilerType</code>s
    */
    public static Enumeration compilerTypes () {
        return TopManager.getDefault ().getServices ().services (CompilerType.class);
    }

    /** Find the
    * compiler type implemented as a given class, among the services registered to the
    * system.
    * <P>
    * This should be used during (de-)serialization
    * of the specific compiler type for a data object: only store its class name
    * and then try to find the compiler implemented by that class later.
    *
    * @param clazz the class of the debugger looked for
    * @return the desired debugger or <code>null</code> if it does not exist
    */
    public static CompilerType find (Class clazz) {
        ServiceType t = TopManager.getDefault ().getServices ().find (clazz);
        if (t instanceof CompilerType) {
            return (CompilerType)t;
        } else {
            return null;
        }
    }

    /** Find the
    * compiler with requested name, among the services registered to the
    * system.
    * <P>
    * This should be used during (de-)serialization
    * of the specific compiler type for a data object: only store its name
    * and then try to find the debugger later.
    *
    * @param name (display) name of compiler to find
    * @return the desired compiler or <code>null</code> if it does not exist
    */
    public static CompilerType find (String name) {
        ServiceType t = TopManager.getDefault ().getServices ().find (name);
        if (t instanceof CompilerType) {
            return (CompilerType)t;
        } else {
            return null;
        }
    }

    /** Gets the default compiler type in the system.
    */
    public static CompilerType getDefault () {
        return (CompilerType)compilerTypes ().nextElement ();
    }
}

/*
* Log
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/