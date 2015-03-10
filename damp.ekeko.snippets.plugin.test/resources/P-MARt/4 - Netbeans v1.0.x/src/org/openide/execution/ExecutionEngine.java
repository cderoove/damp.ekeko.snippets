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

package org.openide.execution;

import java.security.PermissionCollection;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

import org.openide.windows.InputOutput;

/** Engine providing the environment necessary to invoke executors.
* May perform tasks such as setting up thread groups, etc.
* Modules should not implement this class, nor need to use it directly.
* <P>
*
* @author Jaroslav Tulach, Ales Novak
*/
public abstract class ExecutionEngine extends Object {
    /** Prepare the environment for an <code>Executor</code> and start it.
    * Is called from
    * {@link Executor#execute}.
    *
    * @param name a name of the new process
    * @param run a runnable to execute
    * @param io an InputOuptut
    *
    * @return an executor task that can control the execution
    *
    * @see #startExecutor
    */
    public abstract ExecutorTask execute(String name, Runnable run, InputOutput io);


    /** Trap accesses to
     * Users that want to link their classes with the IDE should do this through
     * internal execution. The {@link NbClassLoader} used in internal execution will assume that calling
     * this method and giving the permission collection to the class being defined will
     * trigger automatic redirection of system output, input, and error streams into the given I/O tab.
     * Implementations of the engine should bind the tab and returned permissions.
     * Since the permission collection is on the stack when calling methods on {@link System#out} etc.,
     * it is possible to find the appropriate tab for redirection.
     * @param cs code source to construct the permission collection for
     * @param io an I/O tab
     * @return a permission collection
     */
    protected abstract PermissionCollection createPermissions(CodeSource cs, InputOutput io);

    /** Method that allows implementor of the execution engine to provide
    * class path to all libraries that one could find useful for development
    * in the system.
    *
    * @return class path to libraries
    */
    protected abstract NbClassPath createLibraryPath ();
}

/*
 * Log
 *  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/8/99  Ales Novak      IOException removed
 *  12   Gandalf   1.11        10/1/99  Ales Novak      major change of 
 *       execution
 *  11   Gandalf   1.10        9/10/99  Jaroslav Tulach Deleted executors method
 *       + execute throws IOException.
 *  10   Gandalf   1.9         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  9    Gandalf   1.8         6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  6    Gandalf   1.5         5/27/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         5/27/99  Jaroslav Tulach Executors rearanged.
 *  4    Gandalf   1.3         3/31/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/31/99  Ales Novak      
 *  2    Gandalf   1.1         3/23/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    Commented out lines 125, 126 to prevent hanging during execution
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    uncommented lines 125, 126 because it works ???
 *  0    Tuborg    0.16        --/--/98 Ales Novak      redesigned
 */
