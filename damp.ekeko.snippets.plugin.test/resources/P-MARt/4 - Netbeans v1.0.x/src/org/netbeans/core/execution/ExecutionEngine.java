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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileFilter;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.security.SecureClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Permission;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;


import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.execution.NbClassPath;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;
import org.openide.execution.Executor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.modules.ManifestSection;

/** Execution that provides support for starting a class with main
*
* @author Ales Novak
*
* The class handles redirecting of out/in/err for all tasks in the system.
* First instance of TaskIO is created for Corona. So call System.out.println()
* from Corona is redirected to a window.
* Situation for executed task is following it uses System.out/err/in - because
* the task is in some threadgroup it will be recognized and new panel in window
* for that task will be created. Further System.out.println() means that
* (in System.out is our class now - SysOut) calling thread is found and its threadgroup
* is examined - SysOut propagates call to taskIOs in ExecutionEngine. IOTable
* look for mapping the threadgroup to TaskIO class (it may create that). TaskIO
* is created uninitialized. So if only out is used, err/in are never initialized.
* Initializing is lazy - for request. TaskIO.out is an instance of SysPrintStream,
* that is redirected to OutputWriter that is redirected to a window.
*/
public final class
    ExecutionEngine extends org.openide.execution.ExecutionEngine {

    /** base group for all running tasks */
    public static final ThreadGroup base = new ThreadGroup("base"); // NOI18N

    /** used for naming groups */
    private int number = 1;

    /** IO class for corona */
    public static final TaskIO systemIO = new TaskIO();

    /** maps ThreadGroups to TaskIO */
    static IOTable taskIOs;
    static {
        taskIOs = new IOTable(base, systemIO);
    }

    /* table of window:threadgrp */
    static private WindowTable wtable = new WindowTable();

    /** this class have to force consistency of nodes displaying running processes
    * and actually running processes. This flag indicates consistency check.
    */
    private boolean execNodeInited = false;

    /** list of ExecutionListeners 
     * @associates ExecutionListener*/
    private HashSet executionListeners = new HashSet();

    /** instance of ExecutionEngine */
    private static ExecutionEngine engineRef;

    static {
        systemIO.out = System.out;
        systemIO.err = System.err;
        systemIO.in = System.in;
        engineRef = new ExecutionEngine();
    }

    static final long serialVersionUID =9072488605180080803L;
    private ExecutionEngine () {
        /* SysIn is a class that redirects System.in of some running task to
           a window (probably OutWindow).
           SysOut/Err are classes that redirect out/err to the window
        */
        System.setIn (new SysIn ());
        System.setOut (new SysOut ());
        System.setErr (new SysErr ());
    }

    /**
    */
    public static ExecutionEngine getExecutionEngine() {
        return engineRef;
    }

    /** ExecutionObject is responsible for displaying runnig processes in a tree
    * so it must be created before first process is run
    */
    private void initExecNode() {
        ProcessNode.getExecutionNode();
        execNodeInited = true;
        try {
            if (org.openide.util.Utilities.isUnix()) {
                // init thread "process reaper"
                Class clz = Class.forName("java.lang.UNIXProcess"); // NOI18N
            }
        } catch (ClassNotFoundException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace(); // NOI18N
            }
        }
    }

    /** Should prepare environment for Executor and start it. Is called from
    * Executor.execute method.
    *
    * @param executor to start
    * @param info about class to start
    */
    public ExecutorTask execute(String name, Runnable run, InputOutput inout) {
        if (! execNodeInited) initExecNode();  // initiate checking processes
        TaskThreadGroup g;
        if (name != null && name.indexOf("Applet") >= 0) { // NOI18N
            g = new AppletTaskGroup(base, "exec_" + name + "_" + number); // NOI18N
        } else {
            g = new TaskThreadGroup(base, "exec_" + name + "_" + number); // NOI18N
        }
        ExecutorTaskImpl task = new ExecutorTaskImpl();
        synchronized (task.lock) {
            try {
                new RunClass (g, name, number++, inout, this, task, run);
                task.lock.wait();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        return task;
    }

    /** Method that allows implementor of the execution engine to provide
    * class path to all libraries that one could find useful for development
    * in the system.
    *
    * @return class path to libraries
    */
    protected NbClassPath createLibraryPath() {
        return new NbClassPath(getLibraries());
    }

    /** Should get all jar and zip files that are used by IDE.
    * It should contain modules, and libraries.
    * @return an array of all jar and zip files installed for netbeans
    */
    private final String[] getLibraries() {
        String fileSeparator = java.io.File.separator;
        String netbeansHome = System.getProperty("netbeans.home") + fileSeparator; // NOI18N

        String[][][] libs = new String[2][4][];
        int len = 0;
        // nb home
        libs[0][0]= getLibraryItems(netbeansHome + "modules" + fileSeparator + "patches"); // NOI18N
        len += libs[0][0].length;
        libs[0][1] = getLibraryItems(netbeansHome + "lib", true); // NOI18N
        len += libs[0][1].length;
        libs[0][2] = getLibraryItems(netbeansHome + "modules"); // NOI18N
        len += libs[0][2].length;
        libs[0][3] = getLibraryItems(netbeansHome + "modules" + fileSeparator + "ext"); // NOI18N
        len += libs[0][3].length;
        // user home
        String userHome = System.getProperty("user.home"); // NOI18N
        if (userHome != null) {
            userHome = userHome + fileSeparator;
            libs[1][0]= getLibraryItems(netbeansHome + "modules" + fileSeparator + "patches"); // NOI18N
            len += libs[1][0].length;
            libs[1][1] = getLibraryItems(netbeansHome + "lib"); // NOI18N
            len += libs[1][1].length;
            libs[1][2] = getLibraryItems(netbeansHome + "modules"); // NOI18N
            len += libs[1][2].length;
            libs[1][3] = getLibraryItems(netbeansHome + "modules" + fileSeparator + "ext"); // NOI18N
            len += libs[1][3].length;
        } else {
            libs[1][0] = libs[1][1] = libs[1][2] = libs[1][3] = new String[0];
        }

        String[] ret = new String[len];
        int copiedlen = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                System.arraycopy(libs[j][i], 0, ret, copiedlen, libs[j][i].length);
                copiedlen += libs[j][i].length;
            }
        }

        return ret;
    }

    /** @return all zip and jar files from given directory */
    private static String[] getLibraryItems(String lib) {
        return getLibraryItems(lib, false);
    }

    /** @return all zip and jar files from given directory */
    private static String[] getLibraryItems(String lib, boolean checkClasspath) {
        final File file = new File(lib);
        final File[] files = file.listFiles(ZipFileFilter.instance());
        if (files == null) {
            return new String [0];
        }
        String[] ret = new String[files.length];

        int skipped = 0;
        for (int i = 0; i < ret.length; i++) {
            String sfile = files[i].toString();
            if (checkClasspath && (sfile.endsWith("developer.jar") || sfile.endsWith("openide.jar"))) { // NOI18N
                skipped++;
                continue;
            }
            ret[i] = files[i].toString();
        }

        if (skipped > 0) {
            String[] rep = new String[ret.length - skipped];
            System.arraycopy(ret, 0, rep, 0, rep.length);
            ret = rep;
        }

        return ret;
    }

    /** adds a listener */
    public final void addExecutionListener (ExecutionListener l) {
        synchronized (executionListeners) {
            executionListeners.add(l);
        }
    }

    /** removes a listener */
    public final void removeExecutionListener (ExecutionListener l) {
        synchronized (executionListeners) {
            executionListeners.remove(l);
        }
    }

    /** Creates new PermissionCollection for given CodeSource and given PermissionCollection.
     * @param cs a CodeSource
     * @param io an InputOutput
     * @return PermissionCollection for given CodeSource and InputOutput
     */
    protected final PermissionCollection createPermissions(CodeSource cs, InputOutput io) {
        PermissionCollection pc = Policy.getPolicy().getPermissions(cs);
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        return new IOPermissionCollection(io, pc, (grp instanceof TaskThreadGroup ? (TaskThreadGroup) grp: null));
    }

    /** fires event that notifies about new process */
    protected final void fireExecutionStarted (ExecutionEvent ev) {
        Iterator iter = ((HashSet) executionListeners.clone()).iterator();
        while (iter.hasNext()) {
            ExecutionListener l = (ExecutionListener) iter.next();
            l.startedExecution(ev);
        }
    }

    /** fires event that notifies about the end of a process */
    protected final void fireExecutionFinished (ExecutionEvent ev) {
        Iterator iter = ((HashSet) executionListeners.clone()).iterator();
        while (iter.hasNext()) {
            ExecutionListener l = (ExecutionListener) iter.next();
            l.finishedExecution(ev);
        }
    }

    static void putWindow(java.awt.Window w, TaskThreadGroup tg) {
        wtable.putTaskWindow(w, tg);
    }
    static void closeGroup(ThreadGroup tg) {
        wtable.closeGroup(tg);
    }
    static boolean hasWindows(ThreadGroup tg) {
        return wtable.hasWindows(tg);
    }

    /** simple class for executing tasks in extra threads */
    static final class RunClass extends Thread implements IOThreadIfc {

        /** InputOutput that is to be used */
        private InputOutput io;
        /** name */
        String allName; // used in innerclass Runnable
        /** reference to outer class */
        private final org.netbeans.core.execution.ExecutionEngine engine;
        /** ref to a Task */
        private final ExecutorTaskImpl task;
        /** Task to run */
        private /*final*/ Runnable run;

        /** generated names */
        static int number = 0;

        /**
        * @param base is a ThreadGroup we want to be in
        * @param m is a method to invoke
        * @param argv are params for the method
        */
        public RunClass(TaskThreadGroup base,
                        String name,
                        int number,
                        InputOutput io,
                        org.netbeans.core.execution.ExecutionEngine engine,
                        ExecutorTaskImpl task,
                        Runnable run) {
            super (base, "exec_" + name + "_" + number); // NOI18N
            this.allName = name;
            this.io = io;
            this.engine = engine;
            this.task = task;
            this.run = run;
            this.start();
        }

        /** runs the thread
        */
        public void run() {

            final TaskThreadGroup mygroup = (TaskThreadGroup) getThreadGroup();
            mygroup.setFinalizable(); // mark it finalizable - after the completetion of the current thread it will be finalized

            boolean fire = true;

            if (allName ==  null) {
                allName = generateName();
                fire = false;
            }

            String ioname = java.text.MessageFormat.format(
                                ProcessNode.getBundle().getString("CTL_ProgramIO"),
                                new Object[] { allName }
                            );

            // prepare environment (threads, In/Out, atd.)
            DefaultSysProcess def;
            if (io != null) {
                def = new DefaultSysProcess(this, mygroup, io, allName);
                TaskIO tIO = new TaskIO(io, ioname, true);
                getTaskIOs ().put (io, tIO);
            } else {   // advance TaskIO for this process
                TaskIO tIO = null;
                tIO = getTaskIOs().getTaskIO(ioname);
                if (tIO == null) { // executed for the first time
                    io = TopManager.getDefault().getIO(ioname);
                    tIO = new TaskIO(io, ioname);
                } else {
                    io = tIO.getInout();
                }
                io.select();
                io.setFocusTaken(true);
                getTaskIOs().put(io, tIO);
                def = new DefaultSysProcess(this, mygroup, io, allName);
            }

            ExecutionEvent ev = null;
            try {

                ev = new ExecutionEvent(engine, def);
                if (fire) {
                    engine.fireExecutionStarted(ev);
                }

                synchronized (task.lock) {
                    task.proc = def;
                    task.lock.notifyAll();
                }

                // exec foreign Runnable
                run.run();
                // throw away user runnable
                run = null;

                int result = 2;
                try {
                    result = def.result();
                } catch (ThreadDeath err) { // terminated while executing
                }
                task.result = result;

            } finally {
                if (ev != null) {
                    if (fire) {
                        engine.fireExecutionFinished(ev);
                    }
                }
                closeGroup(mygroup); // free windows
                task.finished();
                getTaskIOs().free(mygroup, io); // closes output
            }
        } // run method

        public InputOutput getInputOutput() {
            return io;
        }

        static String generateName() {
            return java.text.MessageFormat.format(
                       ProcessNode.getBundle().getString("CTL_GeneratedName"),
                       new Object[] {new Integer(number++)}
                   );
        }
    }

    /**
    * @return IOTable with couples ThreadGroup:TaskIO
    */
    static IOTable getTaskIOs() {
        return taskIOs;
    }

    /** finds top thread group of the calling thread
    * @return null iff the calling thread is not in any exec group
    * or exec group of calling thread
    */
    public static ThreadGroup findGroup () {
        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        ThreadGroup old = null;
        while (g != null && g != base) {
            old = g;
            g = g.getParent ();
        }
        return (g == null) ? null : old;
    }

    /** jar and zip FileNameFiletr */
    private static class ZipFileFilter implements FileFilter {

        /** an instance */
        static SoftReference instance;

        public boolean accept(File fname) {
            String name = fname.toString();
            return (name != null) &&
                   (name.endsWith(".jar") || name.endsWith(".zip")) && // NOI18N
                   fname.isFile();
        }

        static ZipFileFilter instance() {
            ZipFileFilter ret;
            if ((instance == null) || ((ret = (ZipFileFilter) instance.get()) == null)) {
                ret = new ZipFileFilter();
                instance = new SoftReference(ret);
                return ret;
            } else {
                return ret;
            }
        }
    }

}

/*
 * Log
 *  43   Gandalf   1.42        1/28/00  Ales Novak      UNIXProcess - "process 
 *       reaper" initialized
 *  42   Gandalf   1.41        1/14/00  Ales Novak      System.err was not 
 *       redirected
 *  41   Gandalf   1.40        1/13/00  Ales Novak      #4995
 *  40   Gandalf   1.39        1/12/00  Ales Novak      i18n
 *  39   Gandalf   1.38        1/11/00  Ales Novak      
 *  38   Gandalf   1.37        1/11/00  Ales Novak      provided InputOutput is 
 *       not handled by execution system
 *  37   Gandalf   1.36        12/15/99 Ales Novak      making IO visible
 *  36   Gandalf   1.35        11/24/99 Ales Novak      closing of OutputWindow 
 *       tabs made late
 *  35   Gandalf   1.34        11/17/99 Ales Novak      #4438
 *  34   Gandalf   1.33        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  33   Gandalf   1.32        10/8/99  Jaroslav Tulach Output tab works for 
 *       processes assigning own InputOutput
 *  32   Gandalf   1.31        10/8/99  Ales Novak      #4374
 *  31   Gandalf   1.30        10/8/99  Ales Novak      IOException removed
 *  30   Gandalf   1.29        10/8/99  Ales Novak      improved redirection of 
 *       IO operations
 *  29   Gandalf   1.28        10/4/99  Jaroslav Tulach SysProcess deleted.
 *  28   Gandalf   1.27        10/1/99  Ales Novak      major change of 
 *       execution
 *  27   Gandalf   1.26        9/10/99  Jaroslav Tulach Services changes.
 *  26   Gandalf   1.25        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  25   Gandalf   1.24        6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  24   Gandalf   1.23        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  23   Gandalf   1.22        6/8/99   Ales Novak      # 2096
 *  22   Gandalf   1.21        5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  21   Gandalf   1.20        5/27/99  Jaroslav Tulach Executors rearanged.
 *  20   Gandalf   1.19        5/25/99  Ales Novak      proc item set 
 *       notification
 *  19   Gandalf   1.18        5/25/99  Ales Novak      SysProcess set in 
 *       ExecutionTaskImpl
 *  18   Gandalf   1.17        5/6/99   Ales Novak      not changed
 *  17   Gandalf   1.16        5/4/99   Jaroslav Tulach Correct processing of 
 *       executors.
 *  16   Gandalf   1.15        4/28/99  Ales Novak      obsolete comment removed
 *  15   Gandalf   1.14        4/20/99  Ales Novak      output was not reused 
 *       when exception occured
 *  14   Gandalf   1.13        4/16/99  Libor Martinek  
 *  13   Gandalf   1.12        4/10/99  Ales Novak      
 *  12   Gandalf   1.11        4/8/99   Ian Formanek    Removed HotJava hack
 *  11   Gandalf   1.10        3/31/99  Ales Novak      
 *  10   Gandalf   1.9         3/31/99  Ales Novak      
 *  9    Gandalf   1.8         3/26/99  Ales Novak      
 *  8    Gandalf   1.7         3/24/99  Ales Novak      
 *  7    Gandalf   1.6         3/19/99  Ales Novak      
 *  6    Gandalf   1.5         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  5    Gandalf   1.4         1/15/99  Ales Novak      
 *  4    Gandalf   1.3         1/12/99  Jaroslav Tulach Temporary disabled 
 *       redirection of I/O
 *  3    Gandalf   1.2         1/8/99   Ales Novak      
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Ales Novak      redesign of execution
 *  0    Tuborg    0.12        --/--/98 Petr Hamernik   dataobject deleted.
 */
