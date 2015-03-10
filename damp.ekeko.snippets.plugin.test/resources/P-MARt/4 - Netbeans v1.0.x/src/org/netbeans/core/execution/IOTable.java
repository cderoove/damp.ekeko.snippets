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

package org.netbeans.core.execution;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import org.openide.TopManager;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.TopComponent;

/** Tasks are supposed to obey following model: every task is a ThreadGroup
* and the ThreadGroup is under another ThreadGroup - called "base".
* Systems threads are not under group base.
* The table keeps couples ThreadGroup:TaskIO; each task is supposed
* to be encapsulate by a ThreadGroup; system's threads have special
* handling @see #systemIO
* Some tasks don't require io operations. For such tasks NullTaskIO is
* created (at ExecutionEngine.RunClass.run()); NullTaskIOs left reusing TaskIOs
*
*
* @author Ales Novak
*/
final class IOTable extends Hashtable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 9096333712401558521L;

    /** ThreadGroup of all tasks */
    private ThreadGroup base;

    /** TaskIO of system's threads */
    private TaskIO systemIO;

    /** hashtable of free TaskIOs - name:TaskIO 
     * @associates TaskIO*/
    private Hashtable freeTaskIOs;

    /** our options */
    private ExecutionSettings options;

    /**
    * @param base is a base ThreadGroup for tasks
    * @param systemIO is a TaskIO instance that is used for system threads
    */
    public IOTable(ThreadGroup base, TaskIO systemIO) {
        this.base = base;
        this.systemIO = systemIO;
        freeTaskIOs = new Hashtable(17);
        options = new ExecutionSettings();
    }

    /** finds top thread group of the calling thread
    * @return null iff the calling thread is not in any exec group
    * or exec group of calling thread
    */
    ThreadGroup findGroup () {
        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        ThreadGroup old = null;
        while (g != null && g != base) {
            old = g;
            g = g.getParent ();
        }
        return (g == null) ? null : old;
    }

    /**
    * @return TaskIO specific for calling thread/threadgroup
    */
    private synchronized TaskIO getIO() {

        InputOutput inout = null;

        if (Thread.currentThread() instanceof IOThreadIfc) {
            inout = ((IOThreadIfc) Thread.currentThread()).getInputOutput();
        }

        IOPermissionCollection iopc = null;

        if (inout == null) {
            iopc = AccController.getIOPermissionCollection();
            if (iopc == null) {
                return systemIO;
            }
            inout = iopc.getIO();
        }

        TaskIO io = (TaskIO) get(inout);

        // this piece of source is duplicated in exec engine
        // needed when classloader defines a class with an InpuOutput
        // but the classloader does not work on behalf of execution

        // following code is executed only if a task is dead but
        // some classes behave like Phoenix - they live again
        if (io == null) {
            if (inout instanceof TopComponent) {
                String allName = ((TopComponent) inout).getName();
                io = getTaskIO(allName);
                if (io == null) { // executed for the first time
                    inout = TopManager.getDefault().getIO(allName);
                    io = new TaskIO(inout, allName);
                } else {
                    inout = io.getInout();
                }
                inout.select();
                inout.setFocusTaken(true);
                if ( iopc!= null) { // IOThreadIfc case
                    iopc.setIO(inout);
                }
                put(inout, io);
            } else {
                return new TaskIO(inout); // foreign inout - just return a TaskIO
            }
        }

        return io;
    }

    /**
    * @param name is a name of the tab
    * @return TaskIO
    */
    synchronized TaskIO getTaskIO(String name) {
        TaskIO ret;
        if (reuseTaskIO())
            if ((ret = getFreeTaskIO(name)) != null) return ret;
        return null;
    }

    /**
    * @return true iff TaskIO are to be reused
    */
    private boolean reuseTaskIO() {
        return options.getReuse();
    }

    /**
    * @return true iff reused TaskIO should be reseted
    */
    private boolean clearTaskIO() {
        return options.getClear();
    }

    /**
    * @return free non-used TaskIO with given name or null
    */
    private TaskIO getFreeTaskIO(String name) {
        TaskIO t = (TaskIO) freeTaskIOs.get(name);
        if (t == null) {
            return null;
        }
        if (clearTaskIO()) {
            try {
                t.getInout().getOut().reset();
                t.getInout().getErr().reset();
            } catch (java.io.IOException e) {
            }
        }
        t.in = null;
        t.getInout().flushReader();
        freeTaskIOs.remove(name);
        return t;
    }

    /** frees resources binded to grp
    * @param grp is a ThreadGroup which TaskIO is to be released
    * @param io key for freed TaskIO
    */
    synchronized void free(ThreadGroup grp, InputOutput io) {
        TaskIO t = (TaskIO) get(io);
        if (t == null) {
            return; // nothing ??
        } else if (t.foreign) {
            return;
        }
        if ((t != TaskIO.Null) &&
                (t.getName() != TaskIO.VOID)) { // Null
            t = (TaskIO) freeTaskIOs.put(t.getName(), t); // free it
            if (t != null) {
                t.getInout().closeInputOutput();  // old one destroy
            }
        }
        remove(io);
    }

    /**
    * @return threadgroup specific input stream
    */
    public InputStream getIn () {
        TaskIO io = getIO ();
        if (io.in == null) io.initIn ();
        return io.in;
    }

    /**
    * @return thread specific PrintStream
    * Two calls in the same threadgroup will return the same PrintStream
    */
    public PrintStream getOut () {
        TaskIO io = getIO ();
        if (io.out == null) io.initOut ();
        return io.out;
    }

    /**
    * @return thread specific PrintStream
    * Two calls in the same threadgroup will return the same PrintStream
    */
    public PrintStream getErr () {
        TaskIO io = getIO ();
        if (io.err == null) io.initErr ();
        return io.err;
    }
}

/*
 * Log
 *  19   Gandalf   1.18        1/12/00  Ales Novak      i18n
 *  18   Gandalf   1.17        1/11/00  Ales Novak      provided InputOutput is 
 *       not handled by execution system
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/8/99  Ales Novak      improved redirection of 
 *       IO operations
 *  15   Gandalf   1.14        10/4/99  Jaroslav Tulach SysProcess deleted.
 *  14   Gandalf   1.13        10/1/99  Ales Novak      major change of 
 *       execution
 *  13   Gandalf   1.12        7/28/99  Ales Novak      new window system/#1409
 *  12   Gandalf   1.11        6/28/99  Ian Formanek    Removed obsoleted 
 *       imports
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/13/99  Ales Novak      bugfix #1453
 *  9    Gandalf   1.8         5/13/99  Ales Novak      bugfix #1453
 *  8    Gandalf   1.7         4/9/99   Ales Novak      
 *  7    Gandalf   1.6         4/8/99   Ales Novak      
 *  6    Gandalf   1.5         4/2/99   Ales Novak      
 *  5    Gandalf   1.4         3/31/99  Ales Novak      
 *  4    Gandalf   1.3         3/24/99  Ales Novak      
 *  3    Gandalf   1.2         1/21/99  Ales Novak      
 *  2    Gandalf   1.1         1/8/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Jancura     tab switching changed
 *  0    Tuborg    0.12        --/--/98 Ales Novak      termination of processes at menu added
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    changed order of terminate and close in context menu
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    terminate disabled if the process is not running
 */
