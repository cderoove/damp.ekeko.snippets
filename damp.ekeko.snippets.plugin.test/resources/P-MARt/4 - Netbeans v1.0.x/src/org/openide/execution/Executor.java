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
import java.beans.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;

/** Implements the execution of a class.
* There may be several different types of executors installed in the system,
* some of which may only be appropriate for certain types of objects
* (e.g., applets or servlets).
* The two standard ones, both assuming a main method (i.e. a standalone Java program),
* are {@link ThreadExecutor} (internal execution)
* and {@link ProcessExecutor} (external execution).
* <p>This class <em>currently</em> has a property editor in the default IDE property
* editor search path.
*
* @author Jaroslav Tulach
*/
public abstract class Executor extends ServiceType {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5089771565951633752L;

    /** Execute a class given by name with some arguments in this executor.
    * @param info information describing what to execute
    * @return a task object that can be used to control the running process
    * @exception IOException if the execution cannot be started (class is missing, etc.)
    */
    public abstract ExecutorTask execute(ExecInfo info) throws IOException;

    /** Instruct the execution engine whether
    * the process might need I/O communication with the user.
    * If I/O is needed, a tab in the output window may be opened for the process;
    * otherwise the output is discarded and reads will fail.
    * <p>The default implementation returns <code>true</code>.
    * @return <code>true</code> if the process needs I/O
    */
    public boolean needsIO() {
        return true;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (Executor.class);
    }

    /** Get all registered executors in the system's execution engine.
    * @return enumeration of <code>Executor</code>s
    */
    public static Enumeration executors () {
        return TopManager.getDefault ().getServices ().services (Executor.class);
    }

    /** Find the
    * executor implemented as a given class, among the executors registered to the
    * execution engine.
    * <P>
    * This should be used during (de-)serialization
    * of the specific executor for a data object: only store its class name
    * and then try to find the executor implemented by that class later.
    *
    * @param clazz the class of the executor looked for
    * @return the desired executor or <code>null</code> if it does not exist
    */
    public static Executor find (Class clazz) {
        ServiceType t = TopManager.getDefault ().getServices ().find (clazz);
        if (t instanceof Executor) {
            return (Executor)t;
        } else {
            return null;
        }
    }

    /** Find the
    * executor with requested name, among the executors registered to the
    * execution engine.
    * <P>
    * This should be used during (de-)serialization
    * of the specific executor for a data object: only store its name
    * and then try to find the executor later.
    *
    * @param name (display) name of executor to find
    * @return the desired executor or <code>null</code> if it does not exist
    */
    public static Executor find (String name) {
        ServiceType t = TopManager.getDefault ().getServices ().find (name);
        if (t instanceof Executor) {
            return (Executor)t;
        } else {
            return null;
        }
    }

    /** Get the default executor for the system's execution engine.
    * <p>You may actually want {@link org.openide.loaders.ExecSupport#getExecutor}.
    * @return the default executor
    */
    public static Executor getDefault () {
        Enumeration en = executors ();
        return (Executor)en.nextElement ();
    }

}

/*
 * Log
 *  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        10/1/99  Ales Novak      major change of 
 *       execution
 *  16   Gandalf   1.15        9/10/99  Jaroslav Tulach Extends ServiceType + 
 *       execute throws IOException.
 *  15   Gandalf   1.14        7/2/99   Jesse Glick     Help IDs for debugger & 
 *       executor types.
 *  14   Gandalf   1.13        6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  12   Gandalf   1.11        6/7/99   Jaroslav Tulach FS capabilities.
 *  11   Gandalf   1.10        6/3/99   Jaroslav Tulach Executors are serialized
 *       in project.
 *  10   Gandalf   1.9         5/27/99  Jaroslav Tulach Serialization of 
 *       Executor.Handle works.
 *  9    Gandalf   1.8         5/27/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         5/27/99  Jaroslav Tulach Executors rearanged.
 *  7    Gandalf   1.6         4/26/99  Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         3/31/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/31/99  Ales Novak      
 *  4    Gandalf   1.3         3/25/99  Ales Novak      
 *  3    Gandalf   1.2         3/23/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/19/99  Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    reflecting changes in ExecInfo (#$@$#$@%@$@%!!!!)
 */
