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

/** Provides support for compilation of
* <code>CompilerJob</code>s; not needed directly by modules.
* There is only one instance in the system,
* accessible via {@link TopManager#getCompilationEngine}.
*
* @author Jaroslav Tulach
*/
public abstract class CompilationEngine extends Object {
    /** Start asynchronous compilation of a compiler job.
    * @param job the job to compile
    * @return the task object representing the compilation
    */
    protected abstract CompilerTask start (CompilerJob job);

    /** Analyze dependencies between sets of compilers.
    *
    * Creates list of sets of compilers that describe the
    * levels that compose the compilation. Each level contains
    * a set of compilers that do not depend on each other.
    * The first level contains compilers that do not depend
    * on any other compiler; each next level holds compilers
    * that depend on at least one compiler from the previous level.
    *
    * @param job the compiler job to analyze
    * @return list of {@link Set}s of {@link Compiler}s
    * @exception DependencyException if there is a dependency between the
    *    compilers in the job
    */
    protected static List createComputationLevels (CompilerJob job)
    throws DependencyException {
        return job.computationLevels ();
    }

    /** Group a number of compilers together into compiler groups.
    * The groups then collect all tasks needed for compilation of those compilers.
    *
    * @param compilers collection of {@link Compiler}s
    * @return collection of {@link CompilerGroup}s
    * @exception CompilerGroupException if a compiler group instance cannot be created
    */
    protected static Collection createCompilerGroups (Collection compilers)
    throws CompilerGroupException {
        // map from Class to CompilerGroup
        HashMap groups = new HashMap ();

        Iterator it = compilers.iterator ();
        while (it.hasNext ()) {
            Compiler c = (Compiler)it.next ();
            if (c.isUpToDate()) {
                continue;
            }
            Object key = c.compilerGroupKey ();
            CompilerGroup group = (CompilerGroup)groups.get (key);
            if (group == null) {
                // create new group, register it
                Class clazz = c.compilerGroupClass ();
                try {
                    group = (CompilerGroup)clazz.newInstance ();
                } catch (Exception ex) {
                    throw new CompilerGroupException (clazz, ex);
                }
                groups.put (key, group);
            }

            // add the compiler to the group
            group.add (c);
        }

        // collection of compiler groups
        return groups.values ();
    }

    /** Method to obtain default instance of the engine.
    * @return the engine
    */
    static CompilationEngine getDefault () {
        return org.openide.TopManager.getDefault ().getCompilationEngine ();
    }

}

/*
 * Log
 *  12   Gandalf   1.11        12/23/99 Jaroslav Tulach Enhancing compiler API 
 *       to makefile capabilities
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         9/29/99  Ales Novak      isUpToDate check
 *  9    Gandalf   1.8         9/29/99  Ales Novak      isUpToDate check
 *  8    Gandalf   1.7         9/10/99  Jesse Glick     Small API change: 
 *       ExternalCompiler.compilerType -> Compiler.compilerGroupKey.
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  5    Gandalf   1.4         5/10/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         5/7/99   Ales Novak      getAllLibraries moved to
 *       CompilationEngine
 *  3    Gandalf   1.2         3/24/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         1/26/99  Petr Hamernik   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach converted to new nodes
 *  0    Tuborg    0.15        --/--/98 Jaroslav Tulach lazy implementation
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    bugfix (synchronization on waiter)
 *  0    Tuborg    0.17        --/--/98 Petr Hamernik   depth added to interface
 *  0    Tuborg    0.18        --/--/98 Jan Formanek    minor tweak to compile under M$
 *  0    Tuborg    0.19        --/--/98 Jan Formanek    compile returns boolean value (result of the compilation)
 *  0    Tuborg    0.22        --/--/98 Jan Formanek    reflecting changes in cookies system
 */
