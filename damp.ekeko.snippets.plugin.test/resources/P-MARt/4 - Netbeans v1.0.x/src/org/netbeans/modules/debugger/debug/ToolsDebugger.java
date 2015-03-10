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

package org.netbeans.modules.debugger.debug;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.SwingUtilities;

import sun.tools.debug.*;
import sun.tools.java.Type;

import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.debugger.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.filesystems.FileSystemCapability;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;
import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.support.DebuggerInfoProducer;
import org.netbeans.modules.debugger.support.util.*;


/**
* Main Corona debugger class
*
* @author   Jan Jancura, Jaroslav Tulach
* @version  0.47, May 26, 1998
*/
public class ToolsDebugger extends AbstractDebugger {


    // static ........................................................................

    static final long                         serialVersionUID = 2791375515739651906L;

    /** bundle to obtain text information from */
    static ResourceBundle                     bundle = org.openide.util.NbBundle.getBundle
            (ToolsDebugger.class);

    static final int                          TIMEOUT = 5000;

    private static CoreBreakpoint.Event[]      breakpointEvents;
    private static CoreBreakpoint.Action[]     breakpointActions;
    private static String                      host = "localhost"; // NOI18N


    static {
        breakpointEvents = new CoreBreakpoint.Event[] {
                               new LineBreakpoint (),
                               new MethodBreakpoint (),
                               new ExceptionBreakpoint ()
                           };
        breakpointActions = new CoreBreakpoint.Action[] {
                            };
    }


    // variables .................................................................

    /* Helper for synchronizing sun.tools.debug */
    transient RequestSynchronizer             synchronizer;
    /* sun.tools.debug main debugger class */
    transient RemoteDebugger                  remoteDebugger = null;
    /* debugged process */
    transient private Process                 process;
    private transient String                  hostName;
    private transient String                  password;
    private transient String                  mainClassName;
    private transient String                  stopClassName;

    // threads
    private transient ToolsThread             currentThread = null;
    transient RemoteThread                    lastCurrentThread = null;
    protected transient ToolsThreadGroup      threadGroup = new ToolsThreadGroup (this, null);

    /** Refresh thread. */
    transient private Thread                  debuggerThread;

    // properties
    private transient String                  sourcePath = null;
    private transient String []               exceptionCatchList = null;
    private transient DebuggerInfo            debuggerInfo;

    transient RequestSynchronizer.RequestWaiter killer;

    transient boolean                         stopOnMainFlag;

    // init .......................................................................

    public ToolsDebugger () {
        this (false, null);
    }

    public ToolsDebugger (boolean multisession, Validator validator) {
        super (multisession, validator);
        killer = new RequestSynchronizer.RequestWaiter () {
                     public void run (Thread t) {
                         //S ystem.out.println ("KILL!!!"); // NOI18N
                         //T hread.dumpStack ();
                         if (process != null) {
                             if (System.getProperty ("netbeans.full.hack") == null) {//[PENDING]
                                 process.destroy ();
                                 t.stop ();
                             }
                             TopManager.getDefault ().notify (new NotifyDescriptor.Message (
                                                                  new MessageFormat (bundle.getString ("EXC_Deadlock")).
                                                                  format (new Object[] {t.getName ()})
                                                              ));
                         }
                     }
                 };
    }

    /**
    * Deserializes debugger.
    */
    protected void setDebugger (AbstractDebugger debugger) {
        super.setDebugger (debugger);
    }


    // Debugger implementation .................................................................

    /** Starts the debugger. The method stops the current debugging (if any)
    * and takes information from the provided info (containing the class to start and
    * arguments to pass it and name of class to stop debugging in) and starts
    * new debugging session.
    *
    * @param info debugger info about class to start
    * @exception DebuggerException if an error occures during the start of the debugger
    */
    public void startDebugger (DebuggerInfo info) throws DebuggerException {
        debuggerInfo = info;
        if (remoteDebugger != null)
            finishDebugger ();
        //S ystem.out.println("startDebugger " + info); // NOI18N
        // RemoteDebugging support
        hostName = null;
        password = null;
        boolean local = true;
        if (info instanceof ReconnectDebuggerInfo) {
            ReconnectDebuggerInfo rdi = (ReconnectDebuggerInfo) info;
            hostName = rdi.getHostName ();
            password = rdi.getPassword ();
            local = false;
        } else
            if (info instanceof RemoteDebuggerInfo) {
                hostName = ((RemoteDebuggerInfo) info).getHostName ();
                password = ((RemoteDebuggerInfo) info).getPassword ();
                local = false;
            }
        boolean stopOnMain = info.getStopClassName () != null;
        stopOnMainFlag = stopOnMain;
        //S ystem.out.println ("ToolsDebugger.startDebugger " + info.getStopClassName ()); // NOI18N
        //T hread.dumpStack ();

        synchronizer = new RequestSynchronizer ();

        // open output window ...
        super.startDebugger (info);

        // start & init remote debugger ................................................
        //    process = null;
        if (local) {
            // create process & read password for local debugging

            // create starting string & NbProcessDescriptor
            NbProcessDescriptor debugerProcess;
            if (info instanceof ProcessDebuggerInfo)
                debugerProcess = ((ProcessDebuggerInfo) info).getDebuggerProcess ();
            else
                debugerProcess = ProcessDebuggerType.DEFAULT_DEBUGGER_PROCESS;
            HashMap map;
            if (info instanceof ToolsDebugger10Info) {
                map = Utils.processDebuggerInfo (
                          info,
                          "-debug", // NOI18N
                          "sun.tools.debug.EmptyApp" // NOI18N
                      );
                map.put (ToolsDebugger10Type.JAVA_HOME_SWITCH, ((ToolsDebugger10Info) info).getJavaHome ());
            } else {
                if (info instanceof ToolsDebugger11Info) {
                    String javaHome11 = ((ToolsDebugger11Info) info).getJavaHome ();
                    if ((javaHome11 == null) || (javaHome11.trim ().length () == 0)) {
                        finishDebugger ();
                        throw new DebuggerException (bundle.getString ("EXC_JDK11_home_is_not_set"));
                    }
                    map = Utils.processDebuggerInfo (
                              info,
                              "-debug -nojit", // NOI18N
                              "sun.tools.debug.EmptyApp" // NOI18N
                          );
                    map.put (ToolsDebugger11Type.JAVA_HOME_SWITCH, javaHome11);
                }
                else {
                    map = Utils.processDebuggerInfo (
                              info,
                              "-Xdebug", // NOI18N
                              "sun.tools.agent.EmptyApp" // NOI18N
                          );
                }
            }
            MapFormat format = new MapFormat (map);
            String s = format.format (
                           debugerProcess.getProcessName () + " " + debugerProcess.getArguments () // NOI18N
                       );
            println (s, ERR_OUT);

            // start process & read password ......................................
            try {
                process = debugerProcess.exec (format);
                BufferedReader bufferedreader = new BufferedReader (
                                                    new InputStreamReader (process.getInputStream ())
                                                );
                password = bufferedreader.readLine ();
                showOutput (process, ERR_OUT, ERR_OUT);
                connectInput (process);
            } catch (java.lang.Exception e) {
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_While_create_debuggee")).
                    format (new Object[] {
                                format.format (debugerProcess.getProcessName ()),
                                e.toString ()
                            }),
                    e
                );
            }
            if (password == null) {
                // no reply
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_While_connect_to_debuggee")).
                    format (new Object[] {
                                format.format (debugerProcess.getProcessName ())
                            })
                );
            }
            if (password.indexOf ("=") < 0) { // NOI18N
                // unexpected reply
                println (bundle.getString ("CTL_Unexpected_reply") +": " + password, ERR_OUT);
                showOutput (process, ERR_OUT + STD_OUT, ERR_OUT);
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_Unecpected_debugger_reply")).
                    format (new Object[] {
                                password
                            })
                );
            }
            password = password.substring (password.indexOf ("=") + 1); // NOI18N
            println (bundle.getString ("CTL_Password") + ": " + password, ERR_OUT);
            hostName = "127.0.0.1"; // NOI18N
        } // end of local debugging specific
        else
            if (info instanceof ReconnectDebuggerInfo) {
                println (bundle.getString ("CTL_Reconnecting"), ERR_OUT | STD_OUT);
            } else
                println (bundle.getString ("CTL_Connecting_to") + ": " + hostName + ":" + password, ERR_OUT);

        // start RemoteDebugger ...................................................
        try {
            remoteDebugger = new RemoteDebugger (
                                 hostName,
                                 password.length () < 1 ? null : password,
                                 new ToolsCallback (this),
                                 isShowMessages ()
                             );
        } catch (java.net.ConnectException e) {
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_Cannot_connect_to_debuggee")).
                format (new Object[] {
                            e.toString ()
                        }),
                e
            );
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) throw (ThreadDeath)e;
            //e.printStackTrace ();
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_Cannot_connect_to_debuggee")).
                format (new Object[] {
                            e.toString ()
                        }),
                e
            );
        }

        // create arguments for main class ...............................................
        mainClassName = info.getClassName ();
        RemoteClass cls;
        String[] args = null;
        if ((mainClassName != null) && (mainClassName.length () > 0)) {
            String[] infoArgs = info.getArguments ();
            args = new String [infoArgs.length + 1];
            args[0] = mainClassName;
            System.arraycopy (infoArgs, 0, args, 1, infoArgs.length);
            // args[0] = name of class
            // args[...] = parameters

            // find main class .........................................................
            try {
                cls = remoteDebugger.findClass (mainClassName);
            } catch (Throwable e) {
                if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_Cannot_find_class")).
                    format (new Object[] {
                                mainClassName,
                                e.toString ()
                            }),
                    e
                );
            }
            if (cls == null) {
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_Cannot_find_class")).
                    format (new Object[] {
                                mainClassName,
                                new ClassNotFoundException ().toString ()
                            })
                );
            }
        }

        // set breakpoint on stop class method ...............................................
        if (stopOnMain) {
            RemoteClass stopClass = null;
            try {
                stopClass = remoteDebugger.findClass (stopClassName = info.getStopClassName ());
            } catch (Throwable e) {
                if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                println (bundle.getString ("MSG_Exc_while_finding_class") + stopClassName + '\n' + e, ERR_OUT);
            }
            if (stopClass == null) {
                println (bundle.getString ("CTL_No_such_class") + ": " + stopClassName, ERR_OUT);
            } else {
                try {
                    RemoteField[] rf = stopClass.getMethods ();
                    int i, k = rf.length;
                    Type t = Type.tMethod (Type.tVoid, new Type [] {Type.tArray (Type.tString)});
                    Type startT = Type.tMethod (Type.tVoid);
                    RemoteField startM = null;
                    RemoteField initM = null;
                    RemoteField constM = null;
                    for (i = 0; i < k; i++) {
                        if (rf [i].getName ().equals ("main") && // NOI18N
                                rf [i].getType ().equals (t))
                            break;
                        else
                            if (rf [i].getName ().equals ("start") && // NOI18N
                                    rf [i].getType ().equals (startT)
                               ) startM = rf [i];
                            else
                                if (rf [i].getName ().equals ("init") && // NOI18N
                                        rf [i].getType ().equals (startT)
                                   ) initM = rf [i];
                                else
                                    if (rf [i].getName ().equals ("<init>") && // NOI18N
                                            rf [i].getType ().equals (startT)
                                       ) constM = rf [i];
                    }
                    if (i < k) //[PENDING] stop on non main too !!!!!!!!!!!!!!!!!!!!!
                        stopClass.setBreakpointMethod (rf [i]); // have main
                    else
                        if (initM != null) stopClass.setBreakpointMethod (initM);
                        else
                            if (startM != null) stopClass.setBreakpointMethod (startM);
                            else
                                if (constM != null) stopClass.setBreakpointMethod (constM);

                    //S ystem.out.println ("Stop: " + (i <k) + " " + initM +" " + startM +" " + constM); // NOI18N
                    /*          pendingBreakpoints = new RemoteField [1];
                              pendingBreakpoints [0] = rf[i];
                              pendingBreakpointsClass = stopClass;*/
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                    println (bundle.getString ("MSG_Exc_while_setting_breakpoint") + '\n' + e, ERR_OUT);
                }
            }
        } // stopOnMain

        setBreakpoints ();
        updateWatches ();
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);

        // run debugged class ...............................................
        if (args != null) {
            RemoteThreadGroup rtg = null;
            try {
                rtg = remoteDebugger.run (args.length, args);
                //        threadGroup.setRemoteThreadGroup (rtg);
            } catch (Throwable e) {
                if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_While_calling_run")).
                    format (new Object[] {
                                mainClassName,
                                e.toString ()
                            }),
                    e
                );
            }
            if (rtg == null) {
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (bundle.getString ("EXC_While_calling_run")).
                    format (new Object[] {
                                mainClassName,
                                "" // NOI18N
                            })
                );
            }
        }

        // start refresh thread .................................................
        if (debuggerThread != null) debuggerThread.stop ();
        debuggerThread = new Thread (new Runnable () {
                                         public void run () {
                                             for (;;) {
                                                 try {
                                                     Thread.sleep (5000);
                                                 } catch (InterruptedException ex) {}
                                                 if (getState () == DEBUGGER_RUNNING)
                                                     try {
                                                         threadGroup.threadChanged ();

                                                     } catch (Throwable e) {
                                                         if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                         if (e instanceof java.net.SocketException) {
                                                             debuggerThread = null;
                                                             try {
                                                                 finishDebugger ();
                                                             } catch (Throwable ee) {
                                                                 if (ee instanceof ThreadDeath) throw (ThreadDeath)ee;
                                                             }
                                                             Thread.currentThread ().stop ();
                                                         }
                                                     }
                                             }
                                         }}, "Debugger refresh thread"); // NOI18N
        debuggerThread.setPriority (Thread.MIN_PRIORITY);
        debuggerThread.start ();
    }

    /**
    * Finishes debugger.
    */
    public void finishDebugger () throws DebuggerException {
        threadGroup.setRemoteThreadGroup (null);
        if (remoteDebugger != null) {
            remoteDebugger.close ();
            remoteDebugger = null;
        }
        if (process != null) process.destroy ();
        if (debuggerThread != null) {
            debuggerThread.interrupt ();
            debuggerThread.stop ();
        }

        super.finishDebugger ();

        synchronizer = null;
    }

    /**
    * Trace into.
    */
    public void traceInto () throws DebuggerException {
        if (currentThread == null) return;

        setLastAction (ACTION_TRACE_INTO);
        currentThread.setLastAction (ACTION_TRACE_INTO);
        new Protector ("AbstractDebugger.traceInto") { // NOI18N
            public Object protect () throws Exception {
                currentThread.getRemoteThread ().step (true);
                ToolsDebugger.super.traceInto ();
                return null;
            }
        }.go (synchronizer, killer);
    }

    /**
    * Trace over.
    */
    public void traceOver () throws DebuggerException {
        if (currentThread == null) return;

        setLastAction (ACTION_TRACE_OVER);
        currentThread.setLastAction (ACTION_TRACE_OVER);
        new Protector ("AbstractDebugger.traceOver") { // NOI18N
            public Object protect () throws Exception {
                currentThread.getRemoteThread ().next ();
                ToolsDebugger.super.traceOver ();
                return null;
            }
        }.go (synchronizer, killer);
    }

    /**
    * Go.
    */
    public void go () throws DebuggerException {
        if (currentThread == null) return;

        setLastAction (ACTION_GO);
        currentThread.setLastAction (ACTION_GO);
        new Protector ("AbstractDebugger.go") { // NOI18N
            public Object protect () throws Exception {
                remoteDebugger.cont ();
                ToolsDebugger.super.go ();
                return null;
            }
        }.go (synchronizer, killer);

        //threadGroup.setSuspended (false);
    }

    /**
    * Step out.
    */
    public void stepOut () throws DebuggerException {
        if (currentThread == null) return;

        setLastAction (ACTION_STEP_OUT);
        currentThread.setLastAction (ACTION_STEP_OUT);
        new Protector ("AbstractDebugger.stepOut") { // NOI18N
            public Object protect () throws Exception {
                currentThread.getRemoteThread ().stepOut ();
                ToolsDebugger.super.stepOut ();
                return null;
            }
        }.go (synchronizer, killer);
    }


    // WATCHES ..............................................................

    /** Creates new uninitialized watch. The watch is visible (not hidden).
    *
    * @return new uninitialized watch
    */
    public Watch createWatch () {
        ToolsWatch w = new ToolsWatch (this);
        watch.addElement (w);
        fireWatchCreated (w);
        return w;
    }

    /** Creates a watch its expression is set to initial value. Also
    * allows to create a watch not presented to the user, for example
    * for internal usage in the editor to obtain values of variables
    * under the mouse pointer.
    *
    * @param expr expresion to watch for
    * @param hidden allows to specify whether the watch should be presented
    *   to the user or not (be only of internal usage of the IDE).
    * @return new watch
    */
    public Watch createWatch(String expr,boolean hidden) {
        ToolsWatch w = new ToolsWatch (this);
        if (!hidden) watch.addElement (w);
        w.setVariableName (expr);
        if (!hidden) fireWatchCreated (w);
        return w;
    }


    // AbstractDebugger implementation ..................................................


    // properties ........................

    /**
    * Returns version of this debugger.
    */
    public String getVersion () {
        return bundle.getString ("CTL_Debugger_version");
    }

    /**
    * Returns size of memory.
    */
    public int getTotalMemory () throws DebuggerException {
        if (remoteDebugger == null) return 0;
        try {
            return ((Integer) new Protector ("AbstractDebugger.getTotalMemory") { // NOI18N
                        public Object protect () throws Exception {
                            return new Integer (remoteDebugger.totalMemory ());
                        }
                    }.throwAndWait (synchronizer, killer)).intValue ();
        } catch (Exception e) {
            throw new DebuggerException (e);
        }
    }

    /**
    * Returns size of free memory.
    */
    public int getFreeMemory () throws DebuggerException {
        if (remoteDebugger == null) return 0;
        try {
            return ((Integer) new Protector ("AbstractDebugger.getFreeMemory") { // NOI18N
                        public Object protect () throws Exception {
                            return new Integer (remoteDebugger.freeMemory ());
                        }
                    }.throwAndWait (synchronizer, killer)).intValue ();
        } catch (Exception e) {
            throw new DebuggerException (e);
        }
    }

    /**
    * @return newly constructed string containing classpath obtained from filesystems
    */
    public String getClasspath () {
        if (remoteDebugger != null) {
            try {
                return remoteDebugger.getSourcePath ();
            } catch (Exception e) {
            }
        }
        return "";//getDefaultClasspath (); // NOI18N
    }

    /**
    * @return Connect Panel for this version of debugger.
    */
    public JComponent getConnectPanel () {
        return new Connector ();
    }

    /**
    * @return name of proces for given DebuggerInfo.
    */
    public String getProcessName (DebuggerInfo info) {
        String n;
        if (info instanceof RemoteDebuggerInfo)
            return ((RemoteDebuggerInfo) info).getHostName () + ":" + // NOI18N
                   ((RemoteDebuggerInfo) info).getPassword ();
        else
            return (info.getStopClassName () != null) ? info.getStopClassName () :
                   info.getClassName ();
    }

    /**
    * @return name of location for given DebuggerInfo.
    */
    public String getLocationName (DebuggerInfo info) {
        String n;
        if (info instanceof RemoteDebuggerInfo)
            return ((RemoteDebuggerInfo) info).getHostName ();
        else
            return "localhost";
    }

    /**
    * Returns false, ToolsDebugger does not support evaluation of expressions.
    */
    public boolean supportsExpressions () {
        return false;
    }

    // breakpoints ........................

    /**
    * Returns events available for this version of debugger.
    */
    public CoreBreakpoint.Event[] getBreakpointEvents () {
        return breakpointEvents;
    }

    /**
    * Returns actions available for this version of debugger.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        return breakpointActions;
    }


    // threads ........................

    /**
    * Returns root of all threads.
    */
    public AbstractThreadGroup getThreadGroupRoot () {
        return threadGroup;
    }

    /**
    * Returns current thread or null.
    */
    public AbstractThread getCurrentThread () {
        return currentThread;
    }

    /**
    * Sets current thread. If thread is null, unsets curent thread.
    */
    public void setCurrentThread (AbstractThread thread) {
        if (currentThread == thread) return;
        Object old = currentThread;
        currentThread = (ToolsThread) thread;
        firePropertyChange (PROP_CURRENT_THREAD, old, thread);
    }


    // support for multisession debugging ................................................................

    /**
    * Disconnects from running debugged process.
    */
    public void disconnect () throws DebuggerException {
        threadGroup.setRemoteThreadGroup (null);
        if (remoteDebugger != null) {
            remoteDebugger.close ();
            remoteDebugger = null;
        }
        if (debuggerThread != null) {
            debuggerThread.interrupt ();
            debuggerThread.stop ();
        }
        super.finishDebugger ();
        synchronizer = null;
    }

    /**
    * Reconnects to disconnected Virtual Machine.
    */
    public void reconnect () throws DebuggerException {
        startDebugger (new ReconnectDebuggerInfo (
                           hostName,
                           password
                       ));
    }

    /**
    * Adds breakpoint on the method specified from the RemoteDebugger.
    * Is called from CoreBreakpoint only.
    *
    * @return true if breakpoint is valid.
    */
    boolean addBreakpoint (final String className, final String method) {
        if (synchronizer == null) return false;
        try {
            return ((Boolean) new Protector ("AbstractDebugger.addBreakpoint1") { // NOI18N
                        public Object protect () throws Exception {
                            RemoteClass cls = remoteDebugger.findClass (className);
                            if (cls == null) return new Integer (0);
                            RemoteField m = cls.getMethod (method);
                            if (m == null) return new Integer (0);
                            String s = cls.setBreakpointMethod (m);
                            if (s.trim ().equals ("")) new Integer (1); // NOI18N
                            println (bundle.getString ("CTL_Cannot_set_breakpoint") + ": " + s, ERR_OUT);
                            return new Boolean (false);
                        }
                    }.throwAndWait (synchronizer, killer)).booleanValue ();
        } catch (Exception e) {
            return false;
        }
    }

    /**
    * Removes breakpoint on the method specified from the RemoteDebugger.
    * Is called from CoreBreakpoint only.
    */
    boolean removeBreakpoint (final String className, final String method) {
        if (synchronizer == null) return false;

        try {
            return ((Boolean) new Protector ("AbstractDebugger.removeBreakpoint") { // NOI18N
                        public Object protect () throws Exception {
                            RemoteClass cls = remoteDebugger.findClass (className);
                            if (cls == null) return new Boolean (false);
                            RemoteField m = cls.getMethod (method);
                            if (m == null) return new Boolean (false);
                            String s = cls.clearBreakpointMethod (m);
                            if (s.trim ().equals ("")) return new Boolean (true); // NOI18N
                            println (bundle.getString ("CTL_Cannot_clear_breakpoint") + ": " + s, ERR_OUT);
                            return new Boolean (false);
                        }
                    }.throwAndWait (synchronizer, killer)).booleanValue ();
        } catch (Exception e) {
            return false;
        }
    }

    /**
    * Sets current line in editor.
    */
    Line getLine (final RemoteStackFrame stackFrame) {
        return (Line) new Protector ("AbstractDebugger.showInEditor") { // NOI18N
                   public Object protect () throws Exception {
                       try {
                           return Utils.getLineForSource (
                                      stackFrame.getRemoteClass ().getName (),
                                      stackFrame.getRemoteClass ().getSourceFileName (),
                                      stackFrame.getLineNumber ()
                                  );
                       } catch (Throwable e) {
                           if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                       }
                       return null;
                   }
               }.wait (synchronizer, killer);
    }

    /**
    * Performs stop action.
    */ 
    void stop (boolean stop, final AbstractThread thread) {
        /*
        int lastAction = null;
        int currentStackDepth = 0;
        int lastStackDepth = 0;
        if (!stop) { // obtain values only if they are really needed
          lastAction = ((ToolsThread)thread).getLastAction ();
          lastStackDepth = ((ToolsThread)thread).getLastStackDepth ();
          try {
            currentStackDepth = ((ToolsThread)thread).getStackDepth ();
          }
          catch (DebuggerException e) {
            currentStackDepth = lastStackDepth + 1; // the condition in the following 'if' will be false
          }
    }
        if (stop || (lastAction == ACTION_TRACE_INTO) ||
          ((lastAction == ACTION_TRACE_OVER) && (currentStackDepth <= lastStackDepth)) ||
          ((lastAction == ACTION_STEP_OUT) && (currentStackDepth < lastStackDepth))
          ) {
        */
        if (stop) {
            setLastAction (ACTION_BREAKPOINT_HIT);
            setDebuggerState (DEBUGGER_STOPPED);
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                try {
                                                    threadGroup.threadChanged ();
                                                    // 'thread' could be created by Event.getThread (), but we must use a ToolsThread created by threadChanged method (line above)
                                                    AbstractThread tt = threadGroup.getThread (((ToolsThread)thread).getRemoteThread ());
                                                    if (tt == null)
                                                        tt = thread;
                                                    // **************************************************************************************
                                                    tt.setCurrent (true);
                                                    ((ToolsThread) tt).setLastAction (ACTION_BREAKPOINT_HIT);
                                                } catch (Throwable e) {
                                                    if (e instanceof ThreadDeath) throw (ThreadDeath)e;
                                                    println (bundle.getString ("EXC_Debugger") + ": " + e, ERR_OUT); // NOI18N
                                                    return;
                                                }
                                                updateWatches ();
                                            }
                                        });
        } else {
            int lastAction = ((ToolsThread)thread).getLastAction ();
            if ((lastAction == ACTION_TRACE_OVER) || (lastAction == ACTION_STEP_OUT))
                new Protector ("AbstractDebugger.stepOut") { // NOI18N
                public Object protect () throws Exception {
                    ((ToolsThread) thread).getRemoteThread ().stepOut ();
                    return null;
                }
            }.go (synchronizer, killer);
            else {
                new Protector ("AbstractDebugger.go") { // NOI18N
                    public Object protect () throws Exception {
                        remoteDebugger.cont ();
                        return null;
                    }
                }.go (synchronizer, killer);
            }
        }
    }


    // innerclasses ......................................................................


    private class ReconnectDebuggerInfo extends RemoteDebuggerInfo {
        private ReconnectDebuggerInfo (
            String hostName,
            String password
        ) {
            super (hostName, password);
        }
    }

    private class Connector extends JPanel implements DebuggerInfoProducer {

        private JTextField tfHost;
        private JTextField tfPassword;

        private Connector () {
            setLayout (new GridBagLayout ());
            //      setBorder (new EmptyBorder (8, 8, 8, 8));
            GridBagConstraints c = new GridBagConstraints ();

            c.insets = new Insets (0, 0, 3, 3);
            c.anchor = GridBagConstraints.WEST;
            add (new JLabel (bundle.getString ("CTL_HostName")), c);

            tfHost = new JTextField (host, 25);
            c = new GridBagConstraints ();
            c.gridwidth = 0;
            c.insets = new Insets (0, 3, 3, 0);
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            add (tfHost, c);

            c = new GridBagConstraints ();
            c.insets = new Insets (3, 0, 0, 3);
            c.anchor = GridBagConstraints.WEST;
            add (new JLabel (bundle.getString ("CTL_Password")), c);

            tfPassword = new JTextField (25);
            c = new GridBagConstraints ();
            c.gridwidth = 0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.insets = new Insets (3, 3, 0, 0);
            add (tfPassword, c);

            c = new GridBagConstraints ();
            c.fill = java.awt.GridBagConstraints.BOTH;
            c.weighty = 1.0;
            JPanel p = new JPanel ();
            p.setPreferredSize (new Dimension (1, 1));
            add (p, c);
        }

        /**
        * Returns DebuggerInfo.
        */
        public DebuggerInfo getDebuggerInfo () {
            return new RemoteDebuggerInfo (
                       tfHost.getText (),
                       tfPassword.getText ()
                   );
        }
    }
}

/*
 * Log
 *  30   Gandalf-post-FCS1.28.4.0    3/28/00  Daniel Prusa    
 *  29   Gandalf   1.28        1/14/00  Daniel Prusa    NOI18N
 *  28   Gandalf   1.27        1/13/00  Daniel Prusa    NOI18N
 *  27   Gandalf   1.26        1/6/00   Jan Jancura     Refresh of Threads & 
 *       Watches, Weakization of Nodes
 *  26   Gandalf   1.25        12/9/99  Daniel Prusa    ExceptionBreakpoint
 *  25   Gandalf   1.24        11/29/99 Jan Jancura     Bug 3341 - bad \n in 
 *       output of debugger  Bug 3930 - comments of exceptions thrown while 
 *       starting of debugger  Some implementation moved to AbstractDebugger
 *  24   Gandalf   1.23        11/8/99  Jan Jancura     Somma classes renamed
 *  23   Gandalf   1.22        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  22   Gandalf   1.21        10/7/99  Jan Jancura     Unification of debugger 
 *       types.
 *  21   Gandalf   1.20        10/1/99  Jan Jancura     Current thread & bug 
 *       4108
 *  20   Gandalf   1.19        9/28/99  Jan Jancura     
 *  19   Gandalf   1.18        9/15/99  Jan Jancura     
 *  18   Gandalf   1.17        8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  17   Gandalf   1.16        8/17/99  Jan Jancura     Actions for session 
 *       added & Thread group current property
 *  16   Gandalf   1.15        8/9/99   Jan Jancura     Move process settings 
 *       from DebuggerSettings to ProcesDebuggerType
 *  15   Gandalf   1.14        8/3/99   Jan Jancura     Current line not cleared
 *  14   Gandalf   1.13        8/2/99   Jan Jancura     
 *  13   Gandalf   1.12        8/2/99   Jan Jancura     A lot of bugs...
 *  12   Gandalf   1.11        7/30/99  Jan Jancura     
 *  11   Gandalf   1.10        7/23/99  Jan Jancura     Bug in support for 
 *       sessions
 *  10   Gandalf   1.9         7/21/99  Jan Jancura     
 *  9    Gandalf   1.8         7/13/99  Jan Jancura     
 *  8    Gandalf   1.7         7/13/99  Jan Jancura     
 *  7    Gandalf   1.6         7/2/99   Jan Jancura     Session debugging 
 *       support
 *  6    Gandalf   1.5         6/11/99  Jan Jancura     
 *  5    Gandalf   1.4         6/10/99  Jan Jancura     
 *  4    Gandalf   1.3         6/9/99   Jan Jancura     
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    reflecing move of DebuggerCookie to org.openide.cookies
 *  0    Tuborg    0.46        --/--/98 Jaroslav Tulach passing arguments to debugged programs
 *  0    Tuborg    0.47        --/--/98 Jan Formanek    reflecting changes in cookies
 */
