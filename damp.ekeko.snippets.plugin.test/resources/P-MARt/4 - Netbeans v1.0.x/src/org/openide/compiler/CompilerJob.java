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

import org.openide.util.Mutex;

/** A compiler job consists of more {@link Compiler}s with dependencies
* between each other. The compiler job can compiled, built or
* cleaned. To handle each of these jobs the instance of the
* compilation engine is obtained and its is up to it to
* decide whether it will compile in one thread, a thread group, etc.
*
* <p>A module author only needs to instantiate this class if it is
* desired to initiate a whole compilation process from scratch;
* normally {@link org.openide.actions.AbstractCompileAction} does
* this. Usually it is only used as the argument to a constructor for
* a {@link Compiler} implementation.
*
* @author Jaroslav Tulach, Ales Novak */
public final class CompilerJob extends Object implements Compilable {
    /** collection of all added compilers */
    private Collection compilers = new IdSet ();

    /** name of the job */
    private String name = ""; // NOI18N

    /** the initial depth the job is started for */
    private Compiler.Depth depth;

    /** computed graph that represent this object.
    */
    private Graph graph;

    /** collection of objects this job depends on */
    private Collection dependsOn;

    /** Create a new job with the given initial depth.
    * @param depth initial depth of the job; usually {@link Compiler#DEPTH_ZERO} for files, and either {@link Compiler#DEPTH_ONE} or {@link Compiler#DEPTH_INFINITE} for folders
    */
    public CompilerJob (Compiler.Depth depth) {
        this.depth = depth;
    }

    /** Get the depth of the job. This indicates
    * the depth at which the job was started.
    * @return the compiler depth
    */
    public Compiler.Depth getInitialDepth () {
        return depth;
    }


    /** Start asynchronous compilation of the job.
    * <p>Usually used by, e.g., <code>AbstractCompileAction</code>.
    * @return a compiler task to track the state of the compilation
    */
    public CompilerTask start () {
        return CompilationEngine.getDefault ().start (this);
    }

    /** Test if the set of compilers in the job still needs to be compiled.
    * Scans the compilers looking for any that are not up to date.
    *
    * @return <code>true</code> if every compiler is up to date, false if at least one compilation
    *   is needed
    */
    public final boolean isUpToDate () {
        return getGraph ().isUpToDate ();
    }

    /** Set the display name of this job.
    * @param s the human readable name of this job
    */
    public void setDisplayName (String s) {
        name = s;
    }

    /** Takes all compilers in this job and merges them into another job.
    * If any other compiler should depend on result of this compilation,
    * then it should depend on the returned compiler.
    *
    * @param target job to merge into
    * @return compiler compiler representing compilation of whole job in
    *   the target job
    *
    public synchronized Compiler mergeInto (CompilerJob target) {
      return target.add (this);
}
    */

    /** Get the display name of the job
    * @return a human readable name of this job
    */
    public String getDisplayName () {
        return name;
    }

    /** Adds a compiler into the job.
    * @param comp the compiler
    */
    public void add (Compiler comp) {
        add (Collections.singleton(comp));
    }

    /** Adds compilers into the job.
    * @param comps collection of Compiler
    */
    public void add (final Collection comps) {
        MUTEX.readAccess (new Runnable () {
                              public void run () {
                                  synchronized (CompilerJob.this) {
                                      graph = null;
                                      compilers.addAll (comps);
                                  }
                              }
                          });
    }

    /** Adds a dependency. Before any compiler added into this job
    * will be started, given dependency has to be satified (compiled).
    *
    * @param c compilable 
    */
    public void dependsOn (Compilable c) {
        dependsOn (Collections.singleton (c));
    }

    /** Adds a dependency. Before any compiler added into this job
    * will be started, given dependency has to be satified (compiled).
    *
    * @param arr collection of Compilable objects
    */
    public void dependsOn (final Collection arr) {
        MUTEX.readAccess (new Runnable () {
                              public void run () {
                                  synchronized (CompilerJob.this) {
                                      graph = null;
                                      if (dependsOn == null) {
                                          dependsOn = new IdSet ();
                                      }
                                      dependsOn.addAll (arr);
                                  }
                              }
                          });
    }

    /** Called from CompilationEngine.
    * @return list of {@link Set}s of {@link Compiler}s
    */
    final List computationLevels () throws DependencyException {
        return getGraph ().getLevels ();
    }

    /** Getter for graph of references.
    * @return graph the graph
    */
    private Graph getGraph () {
        Graph g = graph;
        if (g != null) return g;

        return graph = (Graph)MUTEX.writeAccess (new Mutex.Action () {
                           public Object run () {
                               return new Graph (CompilerJob.this);
                           }
                       });
    }

    /** A collection of all compilers that have been added by add (...)
    * methods.
    *
    * @return collection of Compiler
    */
    public final Collection compilers() {
        return compilers;
    }

    /**
    * @return collection with depenencies of Compilable
    */
    public final Collection dependsOn() {
        return dependsOn == null ? Collections.EMPTY_SET : dependsOn;
    }

    /** @return the name of the job.
    */
    public String toString () {
        return getDisplayName ();
    }
}

/*
* Log
*  13   Gandalf   1.12        1/12/00  Ian Formanek    NOI18N
*  12   Gandalf   1.11        12/23/99 Jaroslav Tulach Enhancing compiler API to
*       makefile capabilities
*  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         10/7/99  Martin Ryzl     bug in mergeInto 
*       corrected
*  9    Gandalf   1.8         10/7/99  Jaroslav Tulach #4332
*  8    Gandalf   1.7         10/1/99  Ales Novak      major change of execution
*  7    Gandalf   1.6         9/10/99  Jaroslav Tulach CompilerJob has method 
*       mergeInto (CompilerJob)
*  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    Gandalf   1.4         5/15/99  Jesse Glick     [JavaDoc]
*  4    Gandalf   1.3         3/24/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         3/24/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         3/18/99  Ian Formanek    Fixed bug which caused 
*       ClassCastException from method isUpToDate
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
