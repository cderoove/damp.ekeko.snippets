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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.TaskListener;
import org.openide.util.Task;
import org.openide.util.io.FoldingIOException;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/** Executes a class externally (in a separate process). Provides
* basic implementation that allows to specify the process to 
* execute, its parameters and also to substitute the content of repositorypath,
* classpath, bootclasspath and librarypath. This is done by inner class Format.
* <P>
* The behaviour described here can be overriden by subclasses to use different
* format (extend the set of recognized tags), execute the 
* process with additional environment properties, etc.
*
* @author Ales Novak, Jaroslav Tulach
*/
public class ProcessExecutor extends Executor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1440216248312461457L;

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle(ProcessExecutor.class);

    /** default descriptor to use */
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                "{" + Format.TAG_JAVAHOME + "}{" + Format.TAG_SEPARATOR + "}bin{" + Format.TAG_SEPARATOR + "}java", // /usr/local/bin/java // NOI18N
                "-cp {" + Format.TAG_REPOSITORY + "}" +  // -cp {REPOSITORY}:{CLASSPATH} {CLASSNAME} {ARGUMENTS} // NOI18N
                "{" + Format.TAG_PATHSEPARATOR + "}" + "{" + Format.TAG_CLASSPATH + "}" + // NOI18N
                "{" + Format.TAG_PATHSEPARATOR + "}" + "{" + Format.TAG_LIBRARY + "} " + // NOI18N
                "{" + Format.TAG_CLASSNAME + "} " + // NOI18N
                "{" + Format.TAG_ARGUMENTS + '}', // NOI18N
                bundle.getString ("MSG_ExecutorHint")
            );

    /** external process - like java.exe - property */
    private NbProcessDescriptor externalExecutor;

    /** class path settings or null */
    private NbClassPath classPath;

    /** boot class path or null */
    private NbClassPath bootClassPath;

    /** environment vars or null */
    private String[] envp = null;

    /** working directory or null */
    private File cwd = null;

    /** Create a new executor.
    * The default Java launcher associated with this VM's installation will be used,
    * and the user repository entries will be used for the class path.
    */
    public ProcessExecutor() {
        externalExecutor = DEFAULT;
    }

    /** Set a new external execution command.
    * @param desc the settings for the new external executor
    */
    public synchronized void setExternalExecutor (NbProcessDescriptor desc) {
        NbProcessDescriptor old = externalExecutor;
        externalExecutor = desc;
        firePropertyChange ("externalExecutor", old, desc); // NOI18N
    }

    /* Default human-presentable name of the executor.
    * In the default implementation, just the class name.
    * @return initial value of the human-presentable name
    */
    protected String displayName () {
        return bundle.getString ("ExternalExecutionDisplayName");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ProcessExecutor.class);
    }

    /** Get the current external execution command.
    * @return the settings for the current external executor
    */
    public NbProcessDescriptor getExternalExecutor() {
        return externalExecutor;
    }

    /** Getter for class path associated with this executor.
    */
    public NbClassPath getClassPath () {
        return classPath == null ? NbClassPath.createClassPath () : classPath;
    }

    /** Setter for class path for this executor.
    */
    public synchronized void setClassPath (NbClassPath path) {
        NbClassPath old = classPath;
        classPath = path;
        firePropertyChange ("classPath", old, path); // NOI18N
    }

    /** Getter for boot class path associated with this executor.
    */
    public NbClassPath getBootClassPath () {
        return bootClassPath == null ? NbClassPath.createBootClassPath () : bootClassPath;
    }

    /** Setter for boot class path for this executor.
    */
    public synchronized void setBootClassPath (NbClassPath path) {
        NbClassPath old = bootClassPath;
        bootClassPath = path;
        firePropertyChange ("bootClassPath", old, path); // NOI18N
    }

    /** Getter for repository path. It is immutable reflecting
    * NbClassPath.createRepositoryPath (). Is here only to be displayed in property sheet.
    */
    public NbClassPath getRepositoryPath () {
        return NbClassPath.createRepositoryPath (FileSystemCapability.EXECUTE);
    }

    /** Getter for repository path. It is immutable reflecting
    * NbClassPath.createLibraryPath (). Is here only to be displayed in property sheet.
    */
    public NbClassPath getLibraryPath () {
        return NbClassPath.createLibraryPath ();
    }

    /** Get environment variables.
    * @return the <code><i>NAME</i>=<i>VALUE</i></code> pairs, or <code>null</code> (typically, inherit that of parent)
    */
    public String[] getEnvironmentVariables () {
        return envp;
    }

    /** Set environment variables.
    * @param nue the new variables
    * @see #getEnvironmentVariables
    */
    public synchronized void setEnvironmentVariables (String[] nue) {
        String[] old = envp;
        envp = nue;
        firePropertyChange ("environmentVariables", old, nue); // NOI18N
    }

    /** Get the working directory.
    * Note that using a nondefault working directory will only work on JDK 1.3.
    * @return the working directory to use, or <code>null</code> (use that of parent)
    */
    public File getWorkingDirectory () {
        return cwd;
    }

    /** Set the working directory.
    * @param nue the new directory
    * @see #getWorkingDirectory
    */
    public synchronized void setWorkingDirectory (File nue) {
        File old = cwd;
        cwd = nue;
        firePropertyChange ("workingDirectory", old, nue); // NOI18N
    }

    /** Called to create the java.lang.Process for given exec info.
    * Current implementation scans creates new Format with provided
    * exec info and asks the current executor to start with that
    * format.
    * <P>
    * Subclasses can override this to achive the right behaviour, add
    * system properties, own format, etc.
    * 
    * @param info exec info 
    * @return the executed process
    * @exception IOException if the action fails
    */
    protected Process createProcess (ExecInfo info) throws IOException {
        return getExternalExecutor ().exec (new Format (
                                                info,
                                                getClassPath (),
                                                getBootClassPath (),
                                                getRepositoryPath (),
                                                getLibraryPath ()
                                            ), envp, cwd);
    }


    /* Executes given class by creating new process in underlting operating system.
    * @param ctx used to write to the Output Window
    * @param info information about the class to be executed
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        PERunnable run = new PERunnable(info);
        synchronized (run) {
            InputOutput inout = (needsIO() ? null : InputOutput.NULL);
            ExecutorTask et = TopManager.getDefault().getExecutionEngine().execute(info.getClassName(), run, inout);
            run.setExecutorTask(et);
            try {
                run.wait();
                Throwable t = run.getException();
                if (t != null) {
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else if (t instanceof Error) {
                        throw (Error) t;
                    } else if (t instanceof IOException) {
                        throw (IOException) t;
                    } else {
                        throw new FoldingIOException(t);
                    }
                }
                return run.getExecutorTask();
            } catch (InterruptedException e) {
                throw new FoldingIOException(e);
            }
        }
    }

    private class PERunnable implements Runnable {

        private ExecInfo info;
        private ExecutorTask fromEngine;
        ExecutorTask fromMe;
        private Throwable t;

        PERunnable(ExecInfo info) {
            this.info = info;
        }

        public synchronized void run() {
            try {
                String className = info.getClassName ();

                Process process = createProcess (info);
                Thread[] copyMakers = new Thread[3];

                (copyMakers[0] = new CopyMaker(fromEngine.getInputOutput().getIn(), new OutputStreamWriter(process.getOutputStream()), true, className)).start();
                (copyMakers[1] = new CopyMaker(new InputStreamReader(process.getInputStream()), fromEngine.getInputOutput().getOut(), false, className)).start();
                (copyMakers[2] = new CopyMaker(new InputStreamReader(process.getErrorStream()), fromEngine.getInputOutput().getErr(), false, className)).start();
                fromMe = new ExternalExecutorTask(this, fromEngine, process, copyMakers);
            } catch (ThreadDeath tt) {
                throw tt;
            } catch (Throwable tt) {
                t = tt;
            } finally {
                notifyAll();
            }
        }

        public void setExecutorTask(ExecutorTask fromEngine) {
            this.fromEngine = fromEngine;
            TaskListener tl = new TaskListener() {
                                  public void taskFinished(Task t) {
                                      if (fromMe != null) {
                                          fromMe.stop();
                                      }
                                  }
                              };
            fromEngine.addTaskListener(tl);
        }
        public ExecutorTask getExecutorTask() {
            return fromMe;
        }
        public Throwable getException() {
            return t;
        }
    }

    /** Default format that can format tags related to execution. These include settings of classpath
    * (can be composed from repository, class path, boot class path and libraries), putting somewhere
    * the name of executed class and its arguments.
    */
    public static class Format extends MapFormat {
        /** Tag replaced with ProcessExecutors.getClassPath () */
        public static final String TAG_CLASSPATH = "classpath"; // NOI18N
        /** Tag replaced with ProcessExecutors.getBootClassPath () */
        public static final String TAG_BOOTCLASSPATH = "bootclasspath"; // NOI18N
        /** Tag replaced with ProcessExecutors.getRepositoryPath () */
        public static final String TAG_REPOSITORY = "filesystems"; // NOI18N
        /** Tag replaced with ProcessExecutors.getLibraryPath () */
        public static final String TAG_LIBRARY = "library"; // NOI18N
        /** Tag replaced with name of executed class */
        public static final String TAG_CLASSNAME = "classname"; // NOI18N
        /** Tag replaced with arguments of the program */
        public static final String TAG_ARGUMENTS = "arguments"; // NOI18N
        /** Tag replaced with install directory of JDK */
        public static final String TAG_JAVAHOME = "java.home"; // NOI18N
        /** Tag replaced with separator between filename components */
        public static final String TAG_SEPARATOR = "/"; // NOI18N
        /** Tag replaced with separator between path components */
        public static final String TAG_PATHSEPARATOR = ":"; // NOI18N

        static final long serialVersionUID =1105067849363827986L;
        /** All values for the paths takes from NbClassPath.createXXX methods.
        *
        @param info exec info about class to execute 
        */
        public Format (ExecInfo info) {
            this (
                info,
                NbClassPath.createClassPath (),
                NbClassPath.createBootClassPath (),
                NbClassPath.createRepositoryPath (FileSystemCapability.EXECUTE),
                NbClassPath.createLibraryPath ()
            );
        }

        /** @param info exec info about class to execute
        * @param classPath to substitute instead of CLASSPATH
        * @param bootClassPath boot class path
        * @param repository repository path
        * @param library library path
        */
        public Format (
            ExecInfo info,
            NbClassPath classPath,
            NbClassPath bootClassPath,
            NbClassPath repository,
            NbClassPath library
        ) {
            super (new java.util.HashMap (7));

            java.util.Map map = getMap ();

            map.put (TAG_CLASSPATH, classPath.getClassPath ());
            map.put (TAG_BOOTCLASSPATH, bootClassPath.getClassPath ());
            map.put (TAG_REPOSITORY, repository.getClassPath ());
            map.put (TAG_LIBRARY, library.getClassPath ());
            map.put (TAG_CLASSNAME, info.getClassName ());
            map.put (TAG_JAVAHOME, System.getProperty("java.home"));
            map.put (TAG_SEPARATOR, java.io.File.separator);
            map.put (TAG_PATHSEPARATOR, java.io.File.pathSeparator);

            // JST:
            // it is not too nice that we have to create string from string[]
            // and the string will be later parsed again, but hopefully it
            // will work
            StringBuffer sb = new StringBuffer ();
            String[] args = info.getArguments ();
            for (int i = 0; i < args.length; i++) {
                sb.append ('\"');
                sb.append (args[i]);
                sb.append ('\"');
                sb.append (' ');
            }

            map.put (TAG_ARGUMENTS, sb.toString ());
        }


    }

    /** This thread simply reads from given Reader and writes read chars to given Writer. */
    private static class CopyMaker extends Thread {
        final Writer os;
        final Reader is;
        /** while set to false at streams that writes to the OutputWindow it must be
        * true for a stream that reads from the window.
        */
        final boolean autoflush;
        final String permName;

        CopyMaker(Reader is, Writer os, boolean b, String className) {
            this.os = os;
            this.is = is;
            autoflush = b;
            permName = className;
        }

        /* Makes copy. */
        public void run() {
            int read;
            try {
                while ((read = is.read()) >= 0x0) {
                    os.write(read);
                    if (autoflush) os.flush();
                }
            } catch (IOException ex) {
            }
        }
    } // end of CopyMaker

    /** SysProcess that describes the external process.
    */
    static class ExternalExecutorTask extends ExecutorTask {
        Process proc;
        Thread[] copyMakers;
        ExecutorTask foreign;

        ExternalExecutorTask(Runnable run, ExecutorTask etask, Process proc, Thread[] copyMakers) {
            super(run);
            this.proc = proc;
            this.copyMakers = copyMakers;
            foreign = etask;
            TaskListener tl = new TaskListener() {
                                  public void taskFinished(Task t) {
                                      stop();
                                  }
                              };
            etask.addTaskListener(tl);
            new Thread() {
                public void run() {
                    result();
                }
            }.start();
        }

        public void stop() {
            copyMakers[0].interrupt();
            copyMakers[1].interrupt();
            copyMakers[2].interrupt();
            proc.destroy();
        }

        public int result() {
            try {
                int ret = proc.waitFor();
                Thread.sleep(2000);  // time for copymakers
                stop();
                return ret;
            } catch (InterruptedException e) {
                return 1;   // 0 is success
            } finally {
                notifyFinished();
            }
        }

        public InputOutput getInputOutput() {
            return foreign.getInputOutput();
        }

        public void run() {
        }
    } // end of ExternalProcess
}

/*
 * Log
 *  48   Gandalf-post-FCS1.46.2.0    4/5/00   Ales Novak      Readers used instead of 
 *       InputStreams
 *  47   Gandalf   1.46        1/13/00  Ian Formanek    NOI18N
 *  46   Gandalf   1.45        1/12/00  Ian Formanek    NOI18N
 *  45   Gandalf   1.44        12/27/99 Ian Formanek    Repositor2Filesystems 
 *       change
 *  44   Gandalf   1.43        12/21/99 Jesse Glick     External executors can 
 *       set envvars and (on 1.3) cwd.
 *  43   Gandalf   1.42        11/24/99 Ales Novak      thread that waits for 
 *       the end of an external process moved from ExecSupport here
 *  42   Gandalf   1.41        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  41   Gandalf   1.40        10/8/99  Ales Novak      killing of tasks 
 *       improved
 *  40   Gandalf   1.39        10/1/99  Jesse Glick     Cleanup of service type 
 *       name presentation.
 *  39   Gandalf   1.38        10/1/99  Ales Novak      major change of 
 *       execution
 *  38   Gandalf   1.37        9/29/99  Ales Novak      FilesystemCapability.EXECUTION
 *        used
 *  37   Gandalf   1.36        9/10/99  Petr Jiricka    handleExecute now throws
 *       IOException
 *  36   Gandalf   1.35        9/10/99  Jaroslav Tulach Services changes.
 *  35   Gandalf   1.34        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  34   Gandalf   1.33        8/8/99   Ian Formanek    Hints for executor 
 *       arguments
 *  33   Gandalf   1.32        7/19/99  Martin Ryzl     tags for process name 
 *       added
 *  32   Gandalf   1.31        7/2/99   Jesse Glick     Help IDs for debugger & 
 *       executor types.
 *  31   Gandalf   1.30        6/11/99  Ian Formanek    Fixed creation of 
 *       cmd-line for external executor (part of build 339)
 *  30   Gandalf   1.29        6/11/99  Ales Novak      library added
 *  29   Gandalf   1.28        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  28   Gandalf   1.27        6/8/99   Ales Novak      FSCapability.EXECUTE 
 *       used
 *  27   Gandalf   1.26        6/7/99   Jaroslav Tulach FS capabilities.
 *  26   Gandalf   1.25        6/3/99   Jaroslav Tulach Executors are serialized
 *       in project.
 *  25   Gandalf   1.24        6/2/99   Petr Jiricka    Fixed bug - initializing
 *       the name in the constructor
 *  24   Gandalf   1.23        6/1/99   Jaroslav Tulach 
 *  23   Gandalf   1.22        6/1/99   Jaroslav Tulach Allows subclasses forbid
 *       change of name when executor is changed.
 *  22   Gandalf   1.21        5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  21   Gandalf   1.20        5/27/99  Jesse Glick     [JavaDoc]
 *  20   Gandalf   1.19        5/27/99  Jaroslav Tulach Executors rearanged.
 *  19   Gandalf   1.18        5/25/99  Petr Jiricka    Default external 
 *       classpath now includes SYSTEM
 *  18   Gandalf   1.17        5/18/99  Petr Hamernik   frixed bug #1638 once 
 *       more
 *  17   Gandalf   1.16        5/17/99  Petr Hamernik   fixed bug #1638
 *  16   Gandalf   1.15        4/21/99  Ales Novak      commandline parsing + 
 *       no_classpath
 *  15   Gandalf   1.14        4/9/99   Ales Novak      fix for newlines
 *  14   Gandalf   1.13        4/7/99   Ales Novak      
 *  13   Gandalf   1.12        3/31/99  Jesse Glick     [JavaDoc]
 *  12   Gandalf   1.11        3/31/99  Ales Novak      
 *  11   Gandalf   1.10        3/24/99  Ales Novak      
 *  10   Gandalf   1.9         3/23/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  8    Gandalf   1.7         3/4/99   Ales Novak      
 *  7    Gandalf   1.6         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  6    Gandalf   1.5         2/10/99  Ales Novak      
 *  5    Gandalf   1.4         2/1/99   Ian Formanek    CLASSPATH changes for 
 *       JDK 1.2
 *  4    Gandalf   1.3         1/15/99  Ales Novak      
 *  3    Gandalf   1.2         1/13/99  Ales Novak      
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
