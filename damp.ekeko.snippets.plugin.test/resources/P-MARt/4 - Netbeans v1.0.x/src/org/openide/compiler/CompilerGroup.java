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

import java.util.HashSet;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

/** Cluster of compiler objects that actually runs the compilation.
* Should be implemented by a module author in conjunction with {@link Compiler}.
* <P>
* The group is created by obtaining a class name from
* {@link Compiler#compilerGroupClass}
* and instantiating it. Then all compilers
* that use the same class are added to the group with {@link #add}.
* <P>
* The group can assume that everything is prepared for compilation.
* It can be invoked by actions to compile, build or
* clean.
*
* @author Jaroslav Tulach
*/
public abstract class CompilerGroup extends Object {
    /** listener support */
    private EventListenerList listeners;

    /** Add a compiler to the group. Should absorb all information
    * contained in the compiler.
    * <p>This method is important for module authors, as it should
    * keep track of the essential data from the compiler added to it,
    * so that the group knows what files (e.g.) to compile.
    *
    * @param c the compiler to consume
    * @exception IllegalArgumentException if the compiler
    *   does not belong to this group (the group's class is not
    *   assignable to the one returned from {@link Compiler#compilerGroupClass})
    */
    public abstract void add (Compiler c) throws IllegalArgumentException;

    /** Start compilation. Should check which files really need to be
    * compiled and compile only those. The compilation should be synchronous
    * (i.e. occupy this thread).
    * <P>
    * The compilation should report its progress to status listeners and report
    * all errors to error listeners.
    *
    * @return <code>true</code> if successful, <code>false</code> if the compilation failed
    */
    public abstract boolean start ();


    /** Add a listener.
    * @param l the listener to add
    */  
    public synchronized void addCompilerListener (CompilerListener l) {
        if (listeners == null ) {
            listeners = new javax.swing.event.EventListenerList();
        }
        listeners.add (org.openide.compiler.CompilerListener.class, l);
    }

    /** Remove a listener.
    * @param l the listener to remove
    */
    public synchronized void removeCompilerListener (CompilerListener l) {
        if (listeners == null) return;
        listeners.remove (org.openide.compiler.CompilerListener.class, l);
    }

    /** Fire a progress event to all listeners.
    * @param ev the event to fire
    */
    protected final void fireProgressEvent (ProgressEvent ev) {
        if (this.listeners == null) return;

        Object[] listeners = this.listeners.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==org.openide.compiler.CompilerListener.class) {
                ((org.openide.compiler.CompilerListener)listeners[i+1]).compilerProgress (ev);
            }
        }
    }

    /** Fires an error event to all listeners.
    * @param ev the event to fire
    */
    protected final void fireErrorEvent (ErrorEvent ev) {
        if (this.listeners == null) return;

        Object[] listeners = this.listeners.getListenerList ();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==org.openide.compiler.CompilerListener.class) {
                ((org.openide.compiler.CompilerListener)listeners[i+1]).compilerError (ev);
            }
        }
    }

}

/*
* Log
*  5    Gandalf   1.4         12/23/99 Jaroslav Tulach Enhancing compiler API to
*       makefile capabilities
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         3/24/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
