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

package org.netbeans.modules.debugger.jpda;

import java.beans.*;
import java.io.*;
import java.text.MessageFormat;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.debugger.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.NbClassPath;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;
import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.DebuggerInfoProducer;
import org.netbeans.modules.debugger.support.util.Validator;
import org.netbeans.modules.debugger.jpda.util.*;


/**
* Main Corona debugger class
*
* @author   Jan Jancura
* @version  0.47, May 26, 1998
*/
public class JPDADebugger extends AbstractDebugger implements Executor {


    // static ........................................................................

    static final long                          serialVersionUID = 2797853329739651906L;

    /** bundle to obtain text information from */
    static ResourceBundle                      bundle = org.openide.util.NbBundle.getBundle
            (JPDADebugger.class);

    private static CoreBreakpoint.Event[]      breakpointEvents;
    private static CoreBreakpoint.Action[]     breakpointActions;
    private static Random                      random = new Random ();

    static {
        breakpointEvents = new CoreBreakpoint.Event[] {
                               //      new InstanceCounter (),
                               new LineBreakpoint (),
                               new MethodBreakpoint (),
                               new ExceptionBreakpoint (),
                               new VariableBreakpoint (),
                               new ThreadBreakpoint (),
                               new ClassBreakpoint ()
                           };
        breakpointActions = new CoreBreakpoint.Action[] {
                            };
    }


    // variables .................................................................

    transient VirtualMachine                  virtualMachine = null;
    transient EventRequestManager             requestManager = null;
    transient protected ThreadManager         threadManager;
    transient protected Operator              operator;
    transient private Process                 process;
    transient StepRequest                     stepRequest;
    private transient MethodEntryRequest      findSourceMER;
    private transient StepRequest             findSourceSR;
    private transient int                     findSourceCounter = 0;
    transient private Thread                  debuggerThread;

    private transient AttachingConnector      connector;
    private transient Map                     args;
    private transient String                  mainClassName;
    private transient String                  stopClassName;

    // threads
    private transient JPDAThread              currentThread = null;
    protected transient JPDAThreadGroup       threadGroup = new JPDAThreadGroup (null);

    private transient boolean                 stopOnMain = false;
    private transient DebuggerInfo            debuggerInfo;

    private static transient String []        stopMethodNames = {"main", "start", "init", "<init>"}; // NOI18N
    private transient CoreBreakpoint []       breakpointMain = null;

    // init .......................................................................

    public JPDADebugger () {
        this (false, null);
    }

    public JPDADebugger (boolean multisession, Validator validator) {
        super (multisession, validator);
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
        if (virtualMachine != null)
            finishDebugger ();

        stopOnMain = info.getStopClassName () != null;
        mainClassName = info.getClassName ();  //S ystem.out.println ("JPDADebugger stop on " + info.getStopClassName ()); // NOI18N

        // open output window ...
        super.startDebugger (info);

        // stop on main
        if (stopOnMain) {
            try {
                String stopClassName = debuggerInfo.getStopClassName ();
                AbstractDebugger d = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
                breakpointMain = new CoreBreakpoint [stopMethodNames.length];
                for (int x = 0; x < breakpointMain.length; x++) {
                    breakpointMain [x] = (CoreBreakpoint) d.createBreakpoint (true);
                    breakpointMain [x].setClassName (""); // NOI18N
                    breakpointMain [x].setMethodName (stopMethodNames [x]); // NOI18N
                    CoreBreakpoint.Action[] a = breakpointMain [x].getActions ();
                    int i, ii = a.length;
                    for (i = 0; i < ii; i ++)
                        if (a [i] instanceof PrintAction) {
                            ((PrintAction) a [i]).setPrintText (bundle.getString ("CTL_Stop_On_Main_print_text"));
                        }
                    breakpointMain [x].setClassName (stopClassName);
                }

                addPropertyChangeListener (new PropertyChangeListener () {
                                               public void propertyChange (PropertyChangeEvent ev) {
                                                   if (ev.getPropertyName ().equals (PROP_STATE)) {
                                                       if ((((Integer)ev.getNewValue ()).intValue () == DEBUGGER_STOPPED) ||
                                                               (((Integer)ev.getNewValue ()).intValue () == DEBUGGER_NOT_RUNNING)) {
                                                           if (breakpointMain != null) {
                                                               for (int x = 0; x < breakpointMain.length; x++)
                                                                   breakpointMain [x].remove();
                                                               breakpointMain = null;
                                                           }
                                                           removePropertyChangeListener(this);
                                                       }
                                                   }
                                               }
                                           });

            } catch (DebuggerException e) {
                e.printStackTrace();
            }
        }

        // start & init remote debugger ............................................
        boolean launch = false;
        if (info instanceof ReconnectDebuggerInfo) {
            virtualMachine = reconnect ((ReconnectDebuggerInfo) info);
        } else
            if (info instanceof RemoteDebuggerInfo) {
                virtualMachine = connect ((RemoteDebuggerInfo) info);
            } else {
                virtualMachine = launch (info);
                process = virtualMachine.process ();
                showOutput (process, STD_OUT, STD_OUT);
                connectInput (process);
                launch = true;
            }
        requestManager = virtualMachine.eventRequestManager ();
        operator = new Operator (
                       virtualMachine,
                       launch ?
                       new Runnable () {
                           public void run () {
                               startDebugger ();
                           }
                       } :
                       null,
                       new Runnable () {
                           public void run () {
                               try {
                                   finishDebugger ();
                               } catch (DebuggerException e) {
                               }
                           }
                       }
                   );
        operator.start ();
        if (!launch) startDebugger ();
    }

    /**
    * Finishes debugger.
    */
    public void finishDebugger () throws DebuggerException {
        if (breakpointMain != null) {
            for (int x = 0; x < breakpointMain.length; x++)
                breakpointMain [x].remove ();
            breakpointMain = null;
        }
        try {
            if (virtualMachine != null) virtualMachine.exit (0);
        } catch (VMDisconnectedException e) {
        }
        if (threadManager != null) threadManager.finish ();
        if (debuggerThread != null) {
            debuggerThread.interrupt ();
            debuggerThread.stop ();
        }
        super.finishDebugger ();
    }

    /**
    * Trace into.
    */
    synchronized public void traceInto () throws DebuggerException {
        if (virtualMachine == null) return;
        removeStepRequest ();
        try {
            setLastAction (ACTION_TRACE_INTO);
            stepRequest = requestManager.createStepRequest (
                              currentThread.getThreadReference (),
                              StepRequest.STEP_LINE,
                              StepRequest.STEP_INTO
                          );
            stepRequest.addCountFilter (1);
            stepRequest.putProperty ("traceInto", "traceInto"); // NOI18N
            stepRequest.setSuspendPolicy (EventRequest.SUSPEND_ALL);
            operator.register (stepRequest, this);
            stepRequest.enable ();
            virtualMachine.resume ();
            super.traceInto ();
        } catch (DuplicateRequestException e) {
            e.printStackTrace ();
        }
    }

    /**
    * Trace over.
    */
    synchronized public void traceOver () throws DebuggerException {
        if (virtualMachine == null) return;
        removeStepRequest ();
        try {
            setLastAction (ACTION_TRACE_OVER);
            stepRequest = requestManager.createStepRequest (
                              currentThread.getThreadReference (),
                              StepRequest.STEP_LINE,
                              StepRequest.STEP_OVER
                          );
            stepRequest.addCountFilter (1);
            stepRequest.setSuspendPolicy (EventRequest.SUSPEND_ALL);
            operator.register (stepRequest, this);
            stepRequest.enable ();
            virtualMachine.resume ();
            super.traceOver ();
        } catch (DuplicateRequestException e) {
            e.printStackTrace ();
        }
    }

    /**
    * Go.
    */
    synchronized public void go () throws DebuggerException {
        if (virtualMachine == null) return;
        setLastAction (ACTION_GO);
        removeStepRequest ();
        virtualMachine.resume ();
        threadGroup.refresh ();
        super.go ();
    }

    /**
    * Step out.
    */
    synchronized public void stepOut () throws DebuggerException {
        if (virtualMachine == null) return;
        removeStepRequest ();
        try {
            setLastAction (ACTION_STEP_OUT);
            stepRequest = requestManager.createStepRequest (
                              currentThread.getThreadReference (),
                              StepRequest.STEP_LINE,
                              StepRequest.STEP_OUT
                          );
            stepRequest.addCountFilter (1);
            stepRequest.setSuspendPolicy (EventRequest.SUSPEND_ALL);
            operator.register (stepRequest, this);
            stepRequest.enable ();
            virtualMachine.resume ();
            super.stepOut ();
        } catch (DuplicateRequestException e) {
            e.printStackTrace ();
        }
    }


    // WATCHES ..............................................................

    /** Creates new uninitialized watch. The watch is visible (not hidden).
    *
    * @return new uninitialized watch
    */
    public Watch createWatch () {
        JPDAWatch w = new JPDAWatch (this);
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
    public Watch createWatch (String expr, boolean hidden) {
        JPDAWatch w = new JPDAWatch (this);
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
        /*    if (virtualMachine != null)
              return virtualMachine.versionDescription () + " (" + 
                     virtualMachine.majorVersion () + "/" +
                     virtualMachine.minorVersion () + ")";
            else  
              return bundle.getString ("CTL_Debugger_version");*/
    }

    /**
    * Returns size of memory.
    */
    public int getTotalMemory () throws DebuggerException {
        if (virtualMachine == null) return 0;
        return 0;
    }

    /**
    * Returns size of free memory.
    */
    public int getFreeMemory () throws DebuggerException {
        if (virtualMachine == null) return 0;
        return 0;
    }

    /**
    * @return newly constructed string containing classpath obtained from filesystems
    */
    public String getClasspath () {
        return ""; // NOI18N
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
        if (info instanceof RemoteDebuggerInfo) {
            if ( ((RemoteDebuggerInfo) info).getConnector ().
                    transport ().name ().equals ("dt_shmem")
               ) {
                Argument a = (Argument) ((RemoteDebuggerInfo) info).getArgs ().
                             get ("name");
                if (a == null)
                    return "localhost:???";
                else
                    return "localhost:" + a.value ();
            } else
                if ( ((RemoteDebuggerInfo) info).getConnector ().
                        transport ().name ().equals ("dt_socket")
                   ) {
                    Argument name = (Argument) ((RemoteDebuggerInfo) info).getArgs ().
                                    get ("hostname");
                    Argument port = (Argument) ((RemoteDebuggerInfo) info).getArgs ().
                                    get ("port");
                    return ((name == null) ? "???:" : (name.value () + ":")) +
                           ((port == null) ? "???" : (port.value ()));
                } else
                    return "???";
        } else
            return (info.getStopClassName () != null) ? info.getStopClassName () :
                   info.getClassName ();
    }

    /**
    * @return name of location for given DebuggerInfo.
    */
    public String getLocationName (DebuggerInfo info) {
        if (info instanceof RemoteDebuggerInfo) {
            if ( ((RemoteDebuggerInfo) info).getConnector ().transport ().
                    name ().equals ("dt_shmem")
               ) {
                return "localhost";
            } else
                if ( ((RemoteDebuggerInfo) info).getConnector ().transport ().
                        name ().equals ("dt_socket")
                   ) {
                    Argument name = (Argument) ((RemoteDebuggerInfo) info).getArgs ().
                                    get ("hostname");
                    return name == null ? "localhost" : name.value ();
                } else
                    return "localhost";
        } else
            return "localhost";
    }

    /**
    * Returns true - JPDADebugger supports evaluation of expressions.
    */
    public boolean supportsExpressions () {
        return true;
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
        currentThread = (JPDAThread) thread;
        firePropertyChange (PROP_CURRENT_THREAD, old, thread);
    }


    // interface Executor .....................................................................

    /**
    * Executes breakpoint hit event.
    */
    public void exec (com.sun.jdi.event.Event ev) {
        //S ystem.out.println ("exec "); // NOI18N
        removeStepRequest ();
        StepEvent event = (StepEvent) ev;
        ThreadReference tr = event.thread ();
        Location loc = event.location ();
        int ln = -1;
        String methodName = "?"; // NOI18N
        String className = "?"; // NOI18N
        String lineNumber = "?"; // NOI18N
        String threadName = tr.name ();
        Line l = null;

        if (loc != null) {
            if (loc.method () != null)
                methodName = loc.method ().name ();
            className = loc.declaringType ().name ();
            ln = loc.lineNumber ();
            if (ln >= 0)
                lineNumber = "" + loc.lineNumber ();
        }

        if (ln != -1)
            try {
                l = Utils.getLineForSource (
                        className,
                        loc.sourceName (),
                        ln
                    );
            } catch (AbsentInformationException e) {
                l = Utils.getLine (
                        className,
                        ln
                    );
            }


        if (resolveCanBeCurrent (tr, l))
            // if this line can not be current resolveCanBeCurrent () calls stepOver
            return;
        // line can be current

        if ( (l == null) &&
                (getLastAction () == ACTION_TRACE_INTO)
           )
            // try to find another "intelligent" line of code
            traceToSourceCode (tr);
        // you know - intelligent means that one with source code
        else {
            makeCurrent (
                threadName,
                className,
                methodName,
                lineNumber,
                l != null,
                tr
            );
            operator.stopRequest ();
        }
    }



    // support for multisession debugging ................................................................

    /**
    * Disconnects from running debugged process.
    */
    public void disconnect () throws DebuggerException {
        if (breakpointMain != null) {
            for (int x = 0; x < breakpointMain.length; x++)
                breakpointMain [x].remove ();
            breakpointMain = null;
        }
        try {
            if (virtualMachine != null) virtualMachine.dispose ();
        } catch (VMDisconnectedException e) {
        }
        if (threadManager != null) threadManager.finish ();
        if (debuggerThread != null) {
            debuggerThread.interrupt ();
            debuggerThread.stop ();
        }
        super.finishDebugger ();
    }

    /**
    * Reconnects to disconnected Virtual Machine.
    */
    public void reconnect () throws DebuggerException {
        startDebugger (new ReconnectDebuggerInfo (
                           connector,
                           args
                       ));
    }


    // helper private methods .........................................................................

    /**
    * Finds the first executed line with source code.
    */
    public void traceToSourceCode (ThreadReference thread) {
        //S ystem.out.println ("Start finding!!! "); // NOI18N

        // create Step Request for searching a source code
        try {
            findSourceSR = requestManager.createStepRequest (
                               thread,
                               StepRequest.STEP_LINE,
                               StepRequest.STEP_OUT
                           );
            findSourceSR.addCountFilter (1);
            findSourceSR.setSuspendPolicy (EventRequest.SUSPEND_ALL);
            operator.register (findSourceSR, this);
            findSourceSR.enable ();
        } catch (DuplicateRequestException e) {
            e.printStackTrace ();
        }

        // create Method Entry Request for searching a source code
        findSourceMER = requestManager.createMethodEntryRequest ();
        findSourceMER.setSuspendPolicy (EventRequest.SUSPEND_ALL);
        findSourceMER.addThreadFilter (thread);
        findSourceCounter = 0;
        operator.register (findSourceMER, new Executor () {
                               public void exec (com.sun.jdi.event.Event event) {
                                   if (findSourceCounter == 500) {
                                       // finding source takes a long time
                                       operator.resume ();
                                       if (findSourceMER != null) {
                                           requestManager.deleteEventRequest (findSourceMER);
                                           findSourceMER = null;
                                       }
                                       return;
                                   }
                                   findSourceCounter++;

                                   Location loc = ((MethodEntryEvent) event).location ();
                                   if (loc == null) {
                                       // no line => continue finding
                                       operator.resume ();
                                       return;
                                   }
                                   String className = loc.declaringType ().name ();
                                   int ln = loc.lineNumber ();
                                   //S ystem.out.println ("FIND " + className + " : " + ln); // NOI18N
                                   try {
                                       Line l = null;
                                       if ( (l = Utils.getLineForSource (className, loc.sourceName (), ln))
                                               == null
                                          ) {
                                           // no line => continue finding
                                           operator.resume ();
                                           return;
                                       }

                                       // WOW I have a nice line!
                                       ThreadReference tr = ((MethodEntryEvent) event).thread ();
                                       if (resolveCanBeCurrent (tr, l))
                                           // if can not be current => steps to some line
                                           return;

                                       // line can be current!
                                       String threadName = tr.name ();
                                       String methodName = loc.method () != null ? loc.method ().name () : ""; // NOI18N
                                       String lineNumber = ln == -1 ? "?" : "" + ln; // NOI18N
                                       makeCurrent (
                                           threadName,
                                           className,
                                           methodName,
                                           lineNumber,
                                           true,
                                           tr
                                       );
                                       operator.stopRequest ();
                                   } catch (AbsentInformationException e) {
                                   }
                               }
                           });
        findSourceMER.enable ();

        operator.resume ();
        return;
    }

    /**
    * if this line can not be current => stepOver & return true.
    * return false on the other hand.
    */
    boolean resolveCanBeCurrent (ThreadReference tr) {
        try {
            Location l = tr.frame (0).location ();
            if (l == null) return false;
            return resolveCanBeCurrent (
                       tr,
                       Utils.getLineForSource (
                           l.declaringType ().name (),
                           l.sourceName (),
                           l.lineNumber ()
                       )
                   );
        } catch (Exception e) {
        }
        return false;
    }

    /**
    * If this line can not be current => stepOver & return true.
    * {support for non java languages}
    *
    * return false on the other hand.
    */
    boolean resolveCanBeCurrent (ThreadReference tr, Line l) {
        if ( (l != null) &&
                (!canBeCurrent (l, false))
           ) {
            try {
                removeStepRequest ();
                findSourceSR = requestManager.createStepRequest (
                                   tr,
                                   StepRequest.STEP_LINE,
                                   StepRequest.STEP_OVER
                               );
                findSourceSR.addCountFilter (1);
                findSourceSR.setSuspendPolicy (EventRequest.SUSPEND_ALL);
                operator.register (findSourceSR, this);
                findSourceSR.enable ();
                operator.resume ();
            } catch (DuplicateRequestException e) {
                e.printStackTrace ();
            }
            return true;
        }
        return false;
    }

    /**
    * Sets curent line. It means: change debugger state to stopped, 
    * shows message, sets current thread and updates watches.
    */
    private void makeCurrent (
        final String  threadName,
        final String  className,
        final String  methodName,
        final String  lineNumber,
        final boolean hasSource,
        final ThreadReference tr
    ) {
        setDebuggerState (DEBUGGER_STOPPED);

        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            // show message
                                            if (isFollowedByEditor ()) {
                                                if (hasSource) {
                                                    println (
                                                        new MessageFormat (bundle.getString ("CTL_Thread_stopped")).
                                                        format (
                                                            new Object[] {
                                                                threadName,
                                                                className,
                                                                methodName,
                                                                lineNumber
                                                            }
                                                        ),
                                                        ERR_OUT + STL_OUT
                                                    );
                                                } else {
                                                    println (
                                                        new MessageFormat (bundle.getString
                                                                           ("CTL_Thread_stopped_no_source")).format (
                                                            new Object[] {
                                                                threadName,
                                                                className,
                                                                methodName,
                                                                lineNumber
                                                            }
                                                        ),
                                                        ERR_OUT + STL_OUT
                                                    );
                                                }
                                            } else
                                                println (
                                                    new MessageFormat (bundle.getString ("CTL_Thread_stopped")).
                                                    format (
                                                        new Object[] {
                                                            threadName,
                                                            className,
                                                            methodName,
                                                            lineNumber
                                                        }
                                                    ),
                                                    ERR_OUT + STL_OUT
                                                );

                                            // refresh all
                                            JPDAThread tt = threadManager.getThread (tr);
                                            tt.setCurrent (true);
                                            updateWatches ();
                                        }
                                    });
    }

    /**
    * Second part of debugger start procedure.
    */
    private void startDebugger () {
        threadManager = new ThreadManager (this);

        setBreakpoints ();
        updateWatches ();
        println (bundle.getString ("CTL_Debugger_running"), STL_OUT);
        setDebuggerState (DEBUGGER_RUNNING);

        virtualMachine.resume ();

        // start refresh thread .................................................
        if (debuggerThread != null) debuggerThread.stop ();
        debuggerThread = new Thread (new Runnable () {
                                         public void run () {
                                             for (;;) {
                                                 try {
                                                     Thread.sleep (5000);
                                                 } catch (InterruptedException ex) {}
                                                 if (getState () == DEBUGGER_RUNNING)
                                                     threadGroup.refresh ();
                                             }
                                         }}, "Debugger refresh thread"); // NOI18N
        debuggerThread.setPriority (Thread.MIN_PRIORITY);
        debuggerThread.start ();
    }

    /**
    * Removes last step request.
    */
    void removeStepRequest () {
        if (stepRequest != null) {
            requestManager.deleteEventRequest (stepRequest);
            stepRequest = null;
        }
        if (findSourceMER != null) {
            requestManager.deleteEventRequest (findSourceMER);
            findSourceMER = null;
        }
        if (findSourceSR != null) {
            requestManager.deleteEventRequest (findSourceSR);
            findSourceSR = null;
        }
    }

    private static String generatePassword () {
        StringBuffer sb = new StringBuffer ();
        for (int i = 0; i < 4; i++)
            sb.append ((char) (random.nextInt (26) + 'a'));
        return new String (sb);
    }

    private VirtualMachine launch (DebuggerInfo info) throws DebuggerException {
        // create process & read password for local debugging

        // create main class & arguments ...............................................
        StringBuffer sb = new StringBuffer ();
        sb.append (mainClassName);
        String[] infoArgs = info.getArguments ();
        int i, k = infoArgs.length;
        for (i = 0; i < k; i++)
            sb.append (" \"").append (infoArgs [i]).append ('"'); // NOI18N
        String main = new String (sb);

        // create connector ..............................................................
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
        java.util.List lcs = vmm.launchingConnectors ();
        k = lcs.size ();
        for (i = 0; i < k; i++)
            if ( ((LaunchingConnector) lcs.get (i)).name ().
                    indexOf ("RawCommandLineLaunch") >= 0
               ) // NOI18N
                break;
        if (i == k) {
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_Cannot_find_launcher")).
                format (new Object[] {
                            "RawCommandLineLaunch" // NOI18N
                        })
            );
        }
        LaunchingConnector lc = (LaunchingConnector) lcs.get (i);
        String transport = lc.transport ().name ();

        // create commandLine & NbProcessDescriptor ..............................
        NbProcessDescriptor debugerProcess;
        if (info instanceof ProcessDebuggerInfo)
            debugerProcess = ((ProcessDebuggerInfo) info).getDebuggerProcess ();
        else
            debugerProcess = ProcessDebuggerType.DEFAULT_DEBUGGER_PROCESS;

        // generate password
        String password;
        if (transport.equals ("dt_shmem")) { // NOI18N
            connector = getAttachingConnectorFor ("dt_shmem");
            password = generatePassword ();
            args = connector.defaultArguments ();
            ((Argument) args.get ("name")).setValue (password);
        } else {
            try {
                java.net.ServerSocket ss = new java.net.ServerSocket (0);
                password = "" + ss.getLocalPort (); // NOI18N
                ss.close ();
            } catch (java.io.IOException e) {
                finishDebugger ();
                throw new DebuggerException (
                    new MessageFormat (
                        bundle.getString ("EXC_Cannot_find_empty_local_port")
                    ).format (new Object[] {
                                  e.toString ()
                              })
                );
            }
            connector = getAttachingConnectorFor ("dt_socket");
            args = connector.defaultArguments ();
            ((Argument) args.get ("port")).setValue (password);
        }
        HashMap map = Utils.processDebuggerInfo (
                          info,
                          "-Xdebug -Xnoagent -Xrunjdwp:transport=" + // NOI18N
                          transport +
                          ",address=" + // NOI18N
                          password +
                          ",suspend=y ", // NOI18N
                          main
                      );
        MapFormat format = new MapFormat (map);
        String commandLine = format.format (
                                 debugerProcess.getProcessName () + " " + // NOI18N
                                 debugerProcess.getArguments ()
                             );
        println (commandLine, ERR_OUT);
        /*
            We mus wait on process start to connect...
            try {
              process = debugerProcess.exec (format);
            } catch (java.io.IOException exc) {
              finishDebugger ();
              throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_While_create_debuggee")).
                  format (new Object[] {
                    debugerProcess.getProcessName (),
                    exc.toString ()
                  }),
                exc
              );
            }
            return connect (
              null,
              connector,
              args
            );*/

        /*      S ystem.out.println ("attaching: ");
            Utils.showConnectors (vmm.attachingConnectors ());

            S ystem.out.println ("launching: ");
            Utils.showConnectors (vmm.launchingConnectors ());

            S ystem.out.println ("listening: ");
            Utils.showConnectors (vmm.listeningConnectors ());*/


        // set debugger-start arguments
        Map params = lc.defaultArguments ();
        ((Argument) params.get ("command")).setValue ( // NOI18N
            commandLine
        );
        ((Argument) params.get ("address")).setValue ( // NOI18N
            password
        );

        // launch VM
        try {
            return lc.launch (params);
        } catch (VMStartException exc) {
            showOutput (process = exc.process (), ERR_OUT, ERR_OUT);
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_While_create_debuggee")).
                format (new Object[] {
                            format.format (debugerProcess.getProcessName ()),
                            exc.toString ()
                        }),
                exc
            );
        } catch (Exception exc) {
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_While_create_debuggee")).
                format (new Object[] {
                            format.format (debugerProcess.getProcessName ()),
                            exc.toString ()
                        }),
                exc
            );
        }
    }

    private VirtualMachine reconnect (ReconnectDebuggerInfo info)
    throws DebuggerException {
        return connect (
                   "CTL_Reconnecting_to",
                   info.getConnector (),
                   info.getArgs ()
               );
    }

    private VirtualMachine connect (RemoteDebuggerInfo info)
    throws DebuggerException {
        return connect (
                   "CTL_Connecting_to",
                   connector = info.getConnector (),
                   args = info.getArgs ()
               );
    }

    private VirtualMachine connect (
        String bndlPrefix,
        AttachingConnector connector,
        Map args
    ) throws DebuggerException {
        if (bndlPrefix != null) {
            if (connector.transport ().name ().equals ("dt_shmem")) {
                Argument a = (Argument) args.get ("name");
                if (a == null)
                    println (bundle.getString (bndlPrefix + "_shmem_noargs"), ERR_OUT);
                else
                    println (
                        new MessageFormat (bundle.getString (bndlPrefix + "_shmem")).
                        format (new Object[] {
                                    a.value ()
                                }),
                        ERR_OUT
                    );
            } else
                if (connector.transport ().name ().equals ("dt_socket")) {
                    Argument name = (Argument) args.get ("hostname");
                    Argument port = (Argument) args.get ("port");
                    if ((name == null) || (port== null))
                        println (bundle.getString (bndlPrefix + "_socket_noargs"), ERR_OUT);
                    else
                        println (
                            new MessageFormat (bundle.getString (bndlPrefix + "_socket")).
                            format (new Object[] {
                                        name.value (),
                                        port.value ()
                                    }),
                            ERR_OUT
                        );
                } else
                    println (bundle.getString (bndlPrefix), ERR_OUT);
        }

        // launch VM
        try {           //S ystem.out.println ("attach to:" + ac + " : " + password); // NOI18N
            return connector.attach (args);
        } catch (Exception e) {
            finishDebugger ();
            throw new DebuggerException (
                new MessageFormat (bundle.getString ("EXC_While_connecting_to_debuggee")).
                format (new Object[] {
                            e.toString ()
                        }),
                e
            );
        }
    }

    /**
    * Performs stop action.
    */
    void stop (boolean stop, final AbstractThread thread) {
        final ResourceBundle bundle = NbBundle.getBundle (JPDADebugger.class);
        if (stop) {
            removeStepRequest ();
            setLastAction (ACTION_BREAKPOINT_HIT);
            setDebuggerState (DEBUGGER_STOPPED);
            operator.stopRequest ();
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                thread.setCurrent (true);
                                                updateWatches ();
                                                threadGroup.refresh ();
                                            }
                                        });
        } else
            operator.resume ();
    }

    private static AttachingConnector getAttachingConnectorFor (String transport) {
        List acs = Bootstrap.virtualMachineManager ().
                   attachingConnectors ();
        AttachingConnector ac;
        int i, k = acs.size ();
        for (i = 0; i < k; i++)
            if ( (ac = (AttachingConnector) acs.get (i)).transport ().
                    name ().equals (transport)
               ) return ac;
        return null;
    }

    /**
    * Setter method for debugger state property.
    *
    * @param newState
    */
    synchronized public void setDebuggerState (final int newState) {
        super.setDebuggerState (newState);
    }

    // innerclasses ..............................................................

    private class ReconnectDebuggerInfo extends RemoteDebuggerInfo {
        private ReconnectDebuggerInfo (
            AttachingConnector connector,
            Map args
        ) {
            super (connector, args);
        }
    }

    class Connector extends JPanel implements DebuggerInfoProducer,
        ActionListener {

        private JComboBox             cbConnectors;
        private Map                   args;
        private java.util.List        acs;
        private JTextField[]          tfParams;
        private AttachingConnector    ac;


        Connector () {
            VirtualMachineManager vmm = Bootstrap.virtualMachineManager ();
            acs = vmm.attachingConnectors ();
            setLayout (new GridBagLayout ());
            refresh (0);
        }

        private void refresh (int index) {
            GridBagConstraints c = new GridBagConstraints ();

            // No connector ................
            if (acs.size () == 0) {
                add (new JLabel (bundle.getString ("CTL_No_Connector")), c);
                return;
            }

            // Connector switch ................
            if (acs.size () > 1) {
                c.insets = new Insets (0, 0, 3, 3);
                add (new JLabel (
                         bundle.getString ("CTL_Connector")
                     ), c);

                cbConnectors = new JComboBox ();
                int i, k = acs.size ();
                for (i = 0; i < k; i++) {
                    AttachingConnector ac = (AttachingConnector) acs.get (i);
                    int jj = ac.name ().lastIndexOf ('.');
                    String s = (jj < 0) ? ac.name () : ac.name ().substring (jj + 1);
                    cbConnectors.addItem (s + " (" + ac.description () + ")");
                }
                c = new GridBagConstraints ();
                c.insets = new Insets (0, 3, 3, 0);
                c.weightx = 1.0;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                c.gridwidth = 0;
                cbConnectors.setSelectedIndex (index);
                cbConnectors.setActionCommand ("SwitchMe!");
                cbConnectors.addActionListener (this);
                add (cbConnectors, c);
            }

            ac = (AttachingConnector) acs.get (index);

            // Transport ................
            c = new GridBagConstraints ();
            c.insets = new Insets (3, 0, 0, 3);
            add (new JLabel (bundle.getString ("CTL_Transport")), c);

            JTextField tfTransport = new JTextField (ac.transport ().name ());
            tfTransport.setEnabled (false);
            c = new GridBagConstraints ();
            c.gridwidth = 0;
            c.insets = new Insets (3, 3, 0, 0);
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            add (tfTransport, c);

            // Other params ................
            args = ac.defaultArguments ();
            tfParams = new JTextField [args.size ()];
            Iterator it = args.keySet ().iterator ();
            int i = 0;
            while (it.hasNext ()) {
                String name = (String) it.next ();
                Argument a = (Argument) args.get (name);

                c = new GridBagConstraints ();
                c.insets = new Insets (6, 0, 0, 3);
                c.anchor = GridBagConstraints.WEST;
                add (new JLabel (a.label () + ": "), c);

                JTextField tfParam = new JTextField (a.value ());
                tfParams [i ++] = tfParam;
                tfParam.setName (name);
                c = new GridBagConstraints ();
                c.gridwidth = 0;
                c.insets = new Insets (6, 3, 0, 0);
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                add (tfParam, c);
            }

            c = new GridBagConstraints ();
            c.weighty = 1.0;
            JPanel p = new JPanel ();
            p.setPreferredSize (new Dimension (1, 1));
            add (p, c);
        }

        /**
        * Returns DebuggerInfo.
        */
        public DebuggerInfo getDebuggerInfo () {
            int i, k = tfParams.length;
            for (i = 0; i < k; i++) {
                Argument a = (Argument) args.get (tfParams [i].getName ());
                a.setValue (tfParams [i].getText ());
            }
            return new RemoteDebuggerInfo (ac, args);
        }

        public void actionPerformed (ActionEvent e) {
            removeAll ();
            refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
            Component w = getParent ();
            while (!(w instanceof Window))
                w = w.getParent ();
            if (w != null) ((Window) w).pack (); // ugly hack...
            return;
        }

        private String translate (String name) {
            /*      if (name.equals ("SwitchMe!"))
                    return 
                  else*/
            return name;
        }
    }
}

/*
* Log
*  22   Gandalf   1.21        01/14/00 Daniel Prusa    NOI18N
*  21   Gandalf   1.20        01/13/00 Daniel Prusa    NOI18N
*  20   Gandalf   1.19        01/06/00 Jan Jancura     Refresh of Threads &
*       Watches, Weakization of Nodes
*  19   Gandalf   1.18        12/07/99 Daniel Prusa    getLastAtion renamed to
*       getLastAction
*  18   Gandalf   1.17        11/29/99 Jan Jancura     Bug 3341 - bad \n in output
*       of debugger
*       Bug 3930 - comments of exceptions thrown while starting of debugger
*       Some implementation moved to AbstractDebugger
*       Support for TraceInto - finding source.
*  17   Gandalf   1.16        11/08/99 Jan Jancura     Somma classes renamed
*  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
*       Microsystems Copyright in File Comment
*  15   Gandalf   1.14        10/15/99 Jan Jancura     Bug with launching JPDA on
*       Solaris.
*  14   Gandalf   1.13        10/13/99 Jan Jancura     Destroy action
*       bug in deleting watches
*       deserializing of main window
*  13   Gandalf   1.12        10/07/99 
*/