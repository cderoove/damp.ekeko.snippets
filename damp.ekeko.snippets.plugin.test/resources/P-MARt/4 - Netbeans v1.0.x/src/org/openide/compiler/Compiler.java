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
import java.lang.ref.*;

import org.openide.loaders.*;


/** Implementable object representing one task of compilation.
* <p>A module's implementation should typically be instantiated within
* an implementation of {@link org.openide.cookies.CompilerCookie#addToJob},
* or (preferably) {@link Compiler.Manager#prepareJob}.
* It need not do much beyond keep track of the particular file (data object, ...)
* it is associated with and make sure the associated {@link CompilerGroup} implementation
* knows about it (via {@link CompilerGroup#add}).
* <P>
* <B>Important:</B> Each compiler should implement <code>equals (Object)</code> and 
* <code>hashCode ()</code> methods
* to work correctly. That means to check whether the file (data object, ...) that 
* should be compiled is the same or not. Two Compilers should be equal even
* if they have different set of dependencies, they do not matter. <code>hashCode ()</code>
* must be consistent with <code>equals (Object)</code>.
*
* @author Ales Novak, Jaroslav Tulach, Petr Hamernik
*/
public abstract class Compiler extends Object implements Compilable {
    /** Zero level of compilation--leaves should compile themself, but folders should do nothing. */
    public static final Depth DEPTH_ZERO = new Depth();

    /** Penultimate level of compilation--folder should compile just directly contained files, but not recursively. */
    public static final Depth DEPTH_ONE = new Depth();

    /** Infinite level of compilation--compile all folders recursively. */
    public static final Depth DEPTH_INFINITE = new Depth();

    /** set of compilable objects that have to be processed before this
    * compiler can be executed.
    */
    private java.util.Collection dependsOn;

    /** Default constructor.
    */
    public Compiler () {
    }

    /** Create a new compiler belonging to a specified compiler job.
    * This is the most commonly used constructor.
    *
    * @param job the job the compiler belongs to
    * @deprecated use job.add (new Compiler ());
    */
    public Compiler (CompilerJob job) {
        job.add (this);
        registerInJob (job);
    }

    /** Create a new compiler that depends on another.
    * The new compiler is put into the same job as the other compiler.
    * @param c the compiler to depend on
    * @deprecated use new Compiler ().dependsOn (c);
    */
    public Compiler (Compiler c) {
        dependsOn (c);
        registerInJob (c);
    }

    /** Create a new compiler that depends on several others.
    * Placed in the same job as the others.
    * @param dep compilers to depend on
    * @deprecated use new Compiler ().dependsOn (Arrays.asList (dep));
    */
    public Compiler (Compiler[] dep)  {
        dependsOn (Arrays.asList (dep));
        registerInJob (dep[0]);
    }

    /** Check whether the compiler is up to date.
    * This method is called from {@link CompilerJob#isUpToDate}
    * to skip a compilation when it is unnecessary.
    *
    * @return <code>true</code> if up-to-date; <code>false</code> if compilation is needed
    */
    protected abstract boolean isUpToDate ();

    /** Get the associated <code>CompilerGroup</code> container class.
    * The compiler and compiler group should typically be implemented in parallel,
    * though it is possible to subclass a compiler without subclassing the compiler group.
    * The compiler group must be "expecting" a compiler of this class (or a superclass), or else
    * it is free to fail in an unspecified way.
    * All instances of the same compiler class must return the same result from this method.
    * @return a class assignable to {@link CompilerGroup}
    */
    public abstract Class compilerGroupClass ();

    /** Specify a unique key permitting division of a set of compilers into different compiler groups.
    * The compilation engine, in {@link CompilationEngine#createCompilerGroups}, will always separate
    * compilers within a given job into levels based on their stated dependencies; however, it will
    * also split compilers into different groups within each level, based on this key. All compilers
    * with the same key which can fit into the same level will be placed into a single group.
    * No group will contain compilers with different values of the key.
    *
    * <p>This key should only be used for grouping; it is not available to the compiler group, as
    * its content may be idiosyncratic. (Compiler groups requiring specific pieces of information
    * from the compilers added to them must extract this information according to accessible methods
    * or fields of the compilers, as they are {@link CompilerGroup#add added}.) The key must obey
    * the general Java language contract for object comparison, according to {@link Object#equals}
    * and {@link Object#hashCode}.
    *
    * <p>By default, only the {@link #compilerGroupClass} is used. It is required that each compiler
    * class, if it needs to override this method, first call the super method and add distinctions to
    * this result. I.e. a subclass may never produce a key less discriminating than the superclass.
    * The suggested implementation is to call the super method, and then return a {@link java.util.List}
    * containing both the super's result, as well as any information added
    * by this subclass.
    *
    * <p>Additionally, in the unusual but possible case that a compiler is subclassing another
    * compiler, while still using the same compiler group class as its superclass, the subclass compiler <em>must</em>
    * include in the key (e.g. an additional vector element) the class object for the subclass (determined via a static class
    * constant, <em>not</em> {@link Object#getClass}!)--this will ensure that keys from unrelated subclasses of the
    * same compiler superclass will not randomly conflict.
    *
    * @return the grouping key object for this compiler
    */
    public Object compilerGroupKey () {
        return compilerGroupClass ();
    }

    /** Add dependency on a compilable object. That means that this compiler
    * can be started only after all compilable objects has been finished.
    *
    * @param compilable any compilable object (Compiler, CompilerJob, etc.)
    */
    public final void dependsOn (final Compilable compilable) {
        dependsOn (Collections.singleton(compilable));
    }

    /** Add dependency on a compilable object. That means that this compiler
    * can be started only after all compilable objects has been finished.
    *
    * @param compilables collection of Compilable
    */
    public void dependsOn (final Collection compilables) {
        MUTEX.readAccess (new Runnable () {
                              public void run () {
                                  synchronized (Compiler.this) {
                                      if (dependsOn == null) {
                                          dependsOn = new IdSet ();
                                      }
                                      dependsOn.addAll (compilables);
                                  }
                              }
                          });
    }

    /** Returns itself.
    *
    * @return java.util.Collections.singleton (this);
    */
    public final java.util.Collection compilers() {
        return java.util.Collections.singleton (this);
    }

    /** A collection of other Compilable objects that have to be
    * finished before the compilers of this Compilable can be started.
    *
    * @return collection of Compilable
    */
    public final java.util.Collection dependsOn() {
        // JST: Maybe we could return immutable collection but who really cares...
        return dependsOn == null ? java.util.Collections.EMPTY_LIST : dependsOn;
    }

    /** Depth of compilation.
    *
    * @see Compiler#DEPTH_ZERO
    * @see Compiler#DEPTH_ONE
    * @see Compiler#DEPTH_INFINITE
    */
    public static final class Depth extends Object {
        /** Nobody should construct this class. Everybody must use predefined constants */
        Depth() {
        }

        /** Is this the last level (i.e. zero)?
         * @return <code>true</code> if so
        */
        public boolean isLastDepth () {
            return this == DEPTH_ZERO;
        }

        /** Proceed to the next level.
        * @return the next shallower depth
        */
        public Depth nextDepth () {
            if (this == DEPTH_INFINITE) {
                // stay always on infinite depth
                return this;
            } else {
                // aways proceed to first level
                return DEPTH_ZERO;
            }
        }
    }

    //
    // Hack for backward compatibility
    //

    /** map associating compilers with job */
    private static Map jobTable = new WeakHashMap (101);

    /** Registers a compiler with a job */
    final void registerInJob (CompilerJob job) {
        jobTable.put (this, new WeakReference (job));
    }

    /** Finds a job for a compiler and registers it.
    */
    final void registerInJob (Compiler comp) {
        Reference ref = (Reference)jobTable.get (comp);
        CompilerJob job = ref == null ? null : (CompilerJob)ref.get ();
        if (job == null) throw new IllegalStateException ("Argument Compiler is not in any job"); // NOI18N
        jobTable.put (this, ref);


        job.add (this);
    }
}


/*
 * Log
 *  17   Gandalf   1.16        1/18/00  Jaroslav Tulach External Compiler is 
 *       initialized first and than its dependencies are handled (caused 
 *       problems in the hashCode) method.
 *  16   Gandalf   1.15        1/15/00  Petr Jiricka    Javadoc updated.
 *  15   Gandalf   1.14        1/12/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        12/23/99 Jaroslav Tulach Enhancing compiler API 
 *       to makefile capabilities
 *  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   Gandalf   1.11        9/10/99  Jaroslav Tulach CompilerJob has method 
 *       mergeInto (CompilerJob)
 *  11   Gandalf   1.10        9/10/99  Jesse Glick     Small API change: 
 *       ExternalCompiler.compilerType -> Compiler.compilerGroupKey.
 *  10   Gandalf   1.9         9/10/99  Jaroslav Tulach Compiles with Jikes.
 *  9    Gandalf   1.8         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         4/16/99  Jesse Glick     Compiler.Manager.find() 
 *       now takes class rather than instance of DO.
 *  6    Gandalf   1.5         4/1/99   Jaroslav Tulach Not crashes when there 
 *       is no compiler.
 *  5    Gandalf   1.4         3/24/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/24/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/19/99  Jaroslav Tulach More compiler managers.
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Comments, made more abstract (removed array of FileObjects)
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   depth added
 *  0    Tuborg    0.13        --/--/98 Petr Hamernik   interface changed
 */
