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
 
package org.netbeans.modules.debugger.delegator;

import java.beans.*;
import java.awt.Component;
import java.awt.Window;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.actions.StartDebuggerAction;
import org.openide.debugger.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import org.openide.actions.StartDebuggerAction;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractThreadGroup;
import org.netbeans.modules.debugger.support.DebuggerInfoProducer;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.actions.ConnectAction;
import org.netbeans.modules.debugger.support.actions.SuspendDebuggerAction;
import org.netbeans.modules.debugger.support.actions.ResumeDebuggerAction;
import org.netbeans.modules.debugger.support.util.Validator;

/**
* Main Corona debugger class
*
* @author   Jan Jancura, Jaroslav Tulach
* @version  0.47, May 26, 1998
*/
public class DelegatingDebugger extends AbstractDebugger {


    // static ....................................................................

    static final long                         serialVersionUID =
        7558733005739651906L;
    /** Name of property currentDebugger. Fired when surrent session
        is changed. */
    public static final String                PROP_CURRENT_SESSION =
        "currentSession";
    /** Array of all registered debugger implementations. 
     * @associates Class*/
    private static ArrayList                  registeredDebuggers =
        new ArrayList ();
    /** Array of all closed InputOutput. */
    private static ArrayList                  ioToClose = new ArrayList ();
    /** Common set of breakpoint events. */
    private static DelegatingBreakpoint.Event[]  breakpointEvents =
        new DelegatingBreakpoint.Event [0];
    /** Common set of breakpoint events. */
    private static HashMap                    breakpointEventsRegister =
        new HashMap ();

    static {
        //    bundle = org.openide.util.NbBundle.getBundle
        //      (DelegatingDebugger.class);
    }

    /**
    * Returns default implementation of debugger, or null
    * if non debugger implementation is installed.
    */
    static Class getDefaultDebuggerClass () {
        if (registeredDebuggers.size () < 1)
            return null;
        return (Class) registeredDebuggers.get (0);
    }

    /**
    * Returns registered debuggers.
    */
    static ArrayList getRegisteredDebuggers () {
        return registeredDebuggers;
    }

    /** Switches to running workspace */
    static void changeWorkspace () {
        WindowManager dp = TopManager.getDefault ().getWindowManager ();
        final Workspace d = dp.findWorkspace (StartDebuggerAction.getWorkspace());
        if (d != null)
            SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            d.activate ();
                                        }
                                    });
    }

    /**
    * Registers debugger implementation.
    */
    public static void registerDebugger (AbstractDebugger debugger) {
        boolean isDefault = (debugger.getVersion ().toLowerCase ().indexOf ("default") > -1) || // NOI18N
        (debugger.getClass ().getName ().equals ("org.netbeans.modules.debugger.jpda.JPDADebugger")); // NOI18N
        DelegatingBreakpoint.Event[] br = debugger.getBreakpointEvents ();
        int i, k = br.length;
        for (i = 0; i < k; i++) {
            if (isDefault) // replacing allowed
                breakpointEventsRegister.put (br [i].getTypeName (), br [i]);
            else // add only new events
                if (! breakpointEventsRegister.containsKey (br [i].getTypeName ()))
                    breakpointEventsRegister.put (br [i].getTypeName (), br [i]);
        }
        breakpointEvents = new DelegatingBreakpoint.Event [
                               k = breakpointEventsRegister.size ()
                           ];
        Iterator it = breakpointEventsRegister.values ().iterator ();
        for (i = 0; i < k; i++)
            breakpointEvents [i] = (DelegatingBreakpoint.Event) it.next ();
        if (isDefault)
            registeredDebuggers.add (0, debugger.getClass ());
        else
            registeredDebuggers.add (debugger.getClass ());
    }

    /**
    * Registers debugger implementation.
    */
    public static void unregisterDebugger (Class debugger) {
        registeredDebuggers.remove (debugger);
        if (registeredDebuggers.size () == 0)
            breakpointEvents = new DelegatingBreakpoint.Event [0];
    }


    // variables .................................................................

    /** bundle to obtain text information from */
    private transient ResourceBundle      bundle;
    /** Current debugger session. */
    private transient Session             currentSession;
    /** Currently running debuggers. 
     * @associates Session*/
    private transient ArrayList           debuggers = new ArrayList ();
    /** Stores all currently running (not starting) debuggers.
     * @associates AbstractDebugger*/
    private transient HashMap             runningDebuggers = new HashMap ();

    /**
     * @associates SessionsListener 
     */
    private transient Vector              sessionsListener = new Vector (10,20);
    /** Listens on inner istance of debugger. */
    private transient SessionListener     sessionListener = new SessionListener ();
    /** Delegating root of thread groups. */
    private transient DelegatingThreadGroup     threadGroupRoot = new DelegatingThreadGroup ();
    /** If true the Finish sessions dialog will be showen on debugger finish. */
    protected boolean                     showFinishDialog = true;


    // init .......................................................................

    /**
    * Creates new delegating debugger. 
    */
    public DelegatingDebugger () {
        super (false, null);
        bundle = NbBundle.getBundle (DelegatingDebugger.class);
    }

    /**
    * Deserializes debugger.
    */
    protected void setDebugger (AbstractDebugger debugger) {
        super.setDebugger (debugger);
        if (debugger instanceof DelegatingDebugger) {
            showFinishDialog = ((DelegatingDebugger) debugger).showFinishDialog;
        }
    }


    // Debugger implementation .................................................................

    /** Starts the debugger. The method takes information from the provided
    * info (containing the class to start and
    * arguments to pass it and name of class to stop debugging in) and starts
    * new debugging session.
    *
    * @param info debugger info about class to start
    * @exception DebuggerException if an error occures during the start of the debugger
    */
    public void startDebugger (DebuggerInfo info) throws DebuggerException {
        if (isMultiSession ())
            ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).setMultisession (true);
        ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).changeEnabled (true);

        // start sleeping sessions
        int i = debuggers.size () - 1;
        for (; i >= 0; i--) {
            Session s = (Session) debuggers.get (i);
            s.start ();
        }

        // close old tabs in Output window
        int k = ioToClose.size ();
        for (i = 0; i < k; i++)
            ((InputOutput) ioToClose.get (i)).closeInputOutput ();
        ioToClose = new ArrayList ();

        // start current session
        if (info != null)
            startSession (info);

        // ?!?! why twice Dan?!?!
        if (isMultiSession ())
            ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).setMultisession (true);
        ((StartDebuggerAction) SystemAction.get (StartDebuggerAction.class)).changeEnabled (true);
    }

    /**
    * Finishes debugger.
    */
    public void finishDebugger () throws DebuggerException {
        if (debuggers.size () > 0) {

            // show dialog "Finish debugging sessions" // NOI18N
            if (isMultiSession () && showFinishDialog) {
                final Session[] sessions = (Session[]) debuggers.toArray (new Session [debuggers.size ()]);
                final java.awt.Dialog[] d = new java.awt.Dialog [1];
                final FinishActionPanel panel = new FinishActionPanel ();
                panel.setSessions (sessions);
                panel.setShowFinishDialog (showFinishDialog);
                final boolean[] ok = new boolean [1];
                ok [0] = false;
                DialogDescriptor descriptor = new DialogDescriptor (
                                                  panel,
                                                  bundle.getString ("CTL_Finish_debugging_dialog"),
                                                  true,
                                                  new ActionListener () {
                                                      public void actionPerformed (ActionEvent e) {
                                                          if (DialogDescriptor.OK_OPTION.equals (e.getSource ())) {
                                                              boolean[] state = panel.getState ();
                                                              int j, jj = state.length;
                                                              for (j = 0; j < jj; j ++)
                                                                  sessions [j].setPersistent (state [j]);
                                                              ok [0] = true;
                                                              showFinishDialog = panel.getShowFinishDialog ();
                                                          }
                                                          d [0].dispose ();
                                                      }
                                                  }
                                              );
                descriptor.setHelpCtx (new HelpCtx (DelegatingDebugger.class.getName () + ".dialog")); // NOI18N
                d [0] = TopManager.getDefault ().createDialog (descriptor);
                d [0].setSize (300, 300);
                d [0].show ();
                if (!ok [0]) return;
            }

            // finish all non persistent sessions
            int i = debuggers.size () - 1;
            for (; i >= 0; i--) {
                Session s = (Session) debuggers.get (i);
                s.finish ();
                /*        if (s.isDead ())
                          removeSession (s);*/
            }
        }
        debuggerNotRunning ();
    }

    /**
    * Trace into.
    */
    public void traceInto () throws DebuggerException {
        if (currentSession == null) return;
        setLastAction (ACTION_TRACE_INTO);
        currentSession.getDebugger ().traceInto ();
    }

    /**
    * Trace over.
    */
    public void traceOver () throws DebuggerException {
        if (currentSession == null) return;
        setLastAction (ACTION_TRACE_OVER);
        currentSession.getDebugger ().traceOver ();
    }

    /**
    * Go.
    */
    public void go () throws DebuggerException {
        if (currentSession == null) return;
        setLastAction (ACTION_GO);
        currentSession.getDebugger ().go ();
    }


    /**
    * Step out.
    */
    public void stepOut () throws DebuggerException {
        if (currentSession == null) return;
        setLastAction (ACTION_STEP_OUT);
        currentSession.getDebugger ().stepOut ();
    }


    // BREAKPOINTS .......................................................

    /** Creates new breakpoint.
    *
    * @return new line breakpoint
    */
    public Breakpoint createBreakpoint (boolean hidden) {
        //S ystem.out.println ("JavaD.createBreakpoint "); // NOI18N
        DelegatingBreakpoint b = new DelegatingBreakpoint (this, hidden);
        if (hidden)
            hiddenBreakpoint.addElement (b);
        else
            breakpoint.addElement (b);   //S ystem.out.println ("DelegatingBreakpoint.createBreakpoint " + breakpoint.size () + " : "  + hiddenBreakpoint.size ()); // NOI18N
        fireBreakpointCreated (b);
        return b;
    }


    // WATCHES ..............................................................

    /** Creates new uninitialized watch. The watch is visible (not hidden).
    *
    * @return new uninitialized watch
    */
    public Watch createWatch () {
        DelegatingWatch w = new DelegatingWatch (this);
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
        DelegatingWatch w = new DelegatingWatch (this);
        if (!hidden) watch.addElement (w);
        w.setVariableName (expr);
        if (!hidden) fireWatchCreated (w);
        return w;
    }


    // AbstractDebugger implementation ................................................................


    // properties .......................


    /**
    * Sets debugger state.
    */
    public void setSuspended (boolean suspended) {
        int i = debuggers.size () - 1;
        for (; i >= 0; i--)
            ((Session) debuggers.get (i)).getDebugger ().getThreadGroupRoot ().setSuspended (suspended);
    }
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
        return -1;
    }

    /**
    * Returns size of free memory.
    */
    public int getFreeMemory () throws DebuggerException {
        return -1;
    }

    /**
    * @return newly constructed string containing classpath obtained from filesystems
    */
    public String getClasspath () {
        return "?"; // NOI18N
    }

    /**
    * @return Connect Panel for this version of debugger
    */
    public JComponent getConnectPanel () {
        if (registeredDebuggers.size () == 0)
            return new JLabel (bundle.getString ("CTL_No_debugger_installed"));
        else
            if (registeredDebuggers.size () == 1)
                return createDebugger (
                           (Class) registeredDebuggers.get (0)
                       ).getConnectPanel ();
            else
                return new Connector ();
    }


    /**
    * @return name of proces for given DebuggerInfo
    */
    public String getProcessName (DebuggerInfo info) {
        return "???"; // NOI18N nobody calls this method
    }

    /**
    * @return name of location for given DebuggerInfo
    */
    public String getLocationName (DebuggerInfo info) {
        return "???"; // NOI18N nobody calls this method
    }


    // breakpoints .......................

    /**
    * Returns events available for this version of debugger
    */
    public DelegatingBreakpoint.Event[] getBreakpointEvents () {
        return breakpointEvents;
    }

    /**
    * Returns actions available for this version of debugger
    */
    public DelegatingBreakpoint.Action[] getBreakpointActions () {
        return new DelegatingBreakpoint.Action[0];
    }


    // threads .......................

    /**
    * Returns root of all threads
    */
    public AbstractThreadGroup getThreadGroupRoot () {
        return threadGroupRoot;
    }

    /**
    * Returns current thread or null
    */
    public AbstractThread getCurrentThread () {
        if (getCurrentDebugger () == null) return null;
        return getCurrentDebugger ().getCurrentThread ();
    }

    /**
    * Sets current thread. If thread is null, unsets curent thread
    */
    public void setCurrentThread (AbstractThread thread) {
        thread.setCurrent (true);
    }


    // other methods .........................................................................


    // properties .....................

    /**
    * Getter method for showFinishDialog property.
    */
    public boolean getShowFinishDialog () {
        return showFinishDialog;
    }

    /**
    * Setter method for showFinishDialog property.
    */
    public void setShowFinishDialog (boolean show) {
        showFinishDialog = show;
    }

    /**
    * Returns debugger for curent session.
    */
    public AbstractDebugger getCurrentDebugger () {
        if (currentSession == null) return null;
        return currentSession.getDebugger ();
    }

    /**
    * @return current session.
    */
    public Session getCurrentSession () {
        return currentSession;
    }

    /**
    * Sets given session current.
    */
    public void setCurrentSession (Session s) {
        if (s == null) {
            Session old = currentSession;
            currentSession = null;
            firePropertyChange (PROP_CURRENT_SESSION, old, currentSession);
            return;
        }
        if (s.getCurrentThread () != null)
            s.getCurrentThread ().setCurrent (true);
        else
            setCurrentDebugger (s.getDebugger ());
    }

    /**
    * Returns true, if a debugger supporting expression evaluation is registered
    */ 
    public boolean supportsExpressions () {
        Iterator list = registeredDebuggers.iterator ();
        while (list.hasNext ()) {
            AbstractDebugger debugger = createDebugger ((Class) list.next ());
            if (debugger.supportsExpressions ())
                return true;
        }
        return false;
    }


    // sessions .........................

    /**
    * Returns all currently availabed sessions.
    */
    public Session[] getSessions () {
        Session[] s = new Session [debuggers.size ()];
        return (Session[]) debuggers.toArray (s);
    }

    /**
    * Returns session for gien debugger instance.
    */
    public Session findSession (AbstractDebugger debugger) {
        int i, k = debuggers.size ();
        for (i = 0; i < k; i++)
            if (((Session)debuggers.get (i)).getDebugger ().equals (debugger))
                return (Session)debuggers.get (i);
        return null;
    }

    // other .........................

    /**
    * Creates a new debugger session for given debugger type.
    */
    public void startSession (DebuggerInfo info) throws DebuggerException {
        if (getState () == DEBUGGER_NOT_RUNNING)
            setDebuggerState (DEBUGGER_STARTING);

        // get debugger instance
        Class debuggerClass = null;
        if (info instanceof SessionDebuggerInfo)
            debuggerClass = ((SessionDebuggerInfo) info).getDebuggerType ();
        else
            debuggerClass = getDefaultDebuggerClass ();
        AbstractDebugger debugger = createDebugger (debuggerClass);

        debugger.addPropertyChangeListener (sessionListener);
        debugger.startDebugger (info);
        if (debugger.getState () == DEBUGGER_NOT_RUNNING) {
            debugger.removePropertyChangeListener (sessionListener);
            return;
        }
        threadGroupRoot.setRemoteThreadGroup (debugger.getThreadGroupRoot ());
        addSession (new Session (
                        debugger.getProcessName (info),
                        debugger.getLocationName (info),
                        debugger,
                        info
                    ));
    }


    // private helper methods ....................................................

    /**
    * Sets given debugger current.
    */
    void setCurrentDebugger (AbstractDebugger debugger) {

        // change current session
        if ( (debugger != getCurrentDebugger ()) &&
                (getCurrentDebugger () != null)
           ) {
            // deselect old current thread
            AbstractThread tt = getCurrentDebugger ().getCurrentThread ();
            if (tt != null)
                tt.setCurrent (false);
        }

        // set new one
        Session old = currentSession;
        currentSession = findSession (debugger);
        firePropertyChange (PROP_CURRENT_SESSION, old, currentSession);

        setDebuggerState (debugger.getState ());
        //    if (debugger.getCurrentThread () != null
        //      updateWatches ()
    }

    /**
    * Instantiates debugger of given type.
    */
    protected AbstractDebugger createDebugger (Class debuggerClass) {
        try {
            Constructor c = debuggerClass.getConstructor (
                                new Class [] {
                                    Boolean.TYPE,
                                    Validator.class
                                }
                            );
            AbstractDebugger debugger =  (AbstractDebugger) c.newInstance (
                                             new Object[] {
                                                 new Boolean (isMultiSession ()),
                                                 getValidator ()
                                             }
                                         );
            return debugger;
        } catch (Exception e) {
            return null;
        } catch (Error e) {
            return null;
        }
    }

    /**
    * Runns debugger when some hidden session stops on breakpoint.
    *
    * @return true if debugger gets out from bed...
    */
    boolean wakeUpDebugger (AbstractDebugger d) {
        // wake up from hidden state
        Session s = findSession (d);
        if (TopManager.getDefault ().notify (
                    new NotifyDescriptor.Confirmation (
                        new MessageFormat (
                            bundle.getString ("CTL_Hidden_debugger_on_breakpoint")
                        ).format (new Object [] {
                                      s.getSessionName ()
                                  }),
                        NotifyDescriptor.YES_NO_OPTION
                    ))
                == NotifyDescriptor.YES_OPTION
           ) {
            changeWorkspace ();
            setDebuggerState (DEBUGGER_STARTING);
            setDebuggerState (DEBUGGER_RUNNING);
            try {
                startDebugger (null);
            } catch (DebuggerException ex) {
            }
            return true;
        }
        return false;
    }

    /**
    * Adds new session to the list "debuggers" and fires it.
    */
    private void addSession (Session s) {
        debuggers.add (s);
        setCurrentSession (s);
        fireSessionCreated (s);
    }

    /**
    * Removes session from the list "debuggers" and fires it.
    */
    void removeSession (Session s) {
        if (s.getDebugger () != null)
            s.getDebugger ().removePropertyChangeListener (sessionListener);
        s.getDebugger ().println (bundle.getString ("CTL_Debugger_session_end"), ERR_OUT);
        debuggers.remove (s);
        fireSessionDeath (s);
    }

    /**
    * Called when some debugger instance is running first time.
    */
    protected void firstDebuggerRunning (AbstractDebugger debugger) {
        //    updateWatches ()
        CallbackSystemAction a = (CallbackSystemAction) SystemAction.get (SuspendDebuggerAction.class);
        a.setActionPerformer (new ActionPerformer () {
                                  public void performAction (SystemAction a) {
                                      setSuspended (true);
                                  }
                              });
        a = (CallbackSystemAction) SystemAction.get (ResumeDebuggerAction.class);
        a.setActionPerformer (new ActionPerformer () {
                                  public void performAction (SystemAction a) {
                                      setSuspended (false);
                                  }
                              });
        if (!isMultiSession ()) {
            CallableSystemAction b = (CallableSystemAction) SystemAction.get (ConnectAction.class);
            b.setEnabled (false);
        }
    }

    /**
    * Called when some debugger instance is running first time.
    */
    protected void debugerRunning (AbstractDebugger debugger) {
        addEvents (debugger);
    }

    /**
    * Called when last debugger session is finished.
    */
    protected void debuggerNotRunning () {
        //    setBreakpoints ();
        CallbackSystemAction a = (CallbackSystemAction) SystemAction.get (SuspendDebuggerAction.class);
        a.setActionPerformer (null);
        a = (CallbackSystemAction) SystemAction.get (ResumeDebuggerAction.class);
        a.setActionPerformer (null);
        if (!isMultiSession ()) {
            CallableSystemAction b = (CallableSystemAction) SystemAction.get (ConnectAction.class);
            b.setEnabled (true);
        }
        try {
            super.finishDebugger ();
        } catch (DebuggerException e) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception(
                    e.getTargetException () == null ? e : e.getTargetException (),
                    bundle.getString ("EXC_Debugger") + ": " + e.getMessage ())
            );
        }
        runningDebuggers = new HashMap ();
        println (bundle.getString ("CTL_Debugger_end"), STL_OUT);
    }

    /**
    * Adds breakpoints to the new instance of debugger, when a new session 
    * is started.
    */
    private void addEvents (AbstractDebugger debugger) {  //S ystem.out.println("DelegatingDebugger.addEvents " + debugger); // NOI18N
        Breakpoint[] b = getBreakpoints ();
        int i, k = b.length;  //S ystem.out.println("DelegatingDebugger.addEvents stand. " + k); // NOI18N
        for (i = 0; i < k; i++)
            ((DelegatingBreakpoint) b [i]).addDebugger (debugger);
        b = getHiddenBreakpoints ();
        k = b.length;  //S ystem.out.println("DelegatingDebugger.addEvents hidden. " + k); // NOI18N
        for (i = 0; i < k; i++)
            ((DelegatingBreakpoint) b [i]).addDebugger (debugger);
    }

    /**
    * Removes breakpoints from given instance of debugger.
    */
    private void removeEvents (AbstractDebugger debugger) {
        Breakpoint[] b = getBreakpoints ();
        int i, k = b.length;
        for (i = 0; i < k; i++)
            ((DelegatingBreakpoint) b [i]).removeDebugger (debugger);
        b = getHiddenBreakpoints ();
        k = b.length;
        for (i = 0; i < k; i++)
            ((DelegatingBreakpoint) b [i]).removeDebugger (debugger);
    }


    // SessionListener support ...................................................

    /**
    * This listener notifikates about cahnges of breakpoints, watches and threads
    *
    * @param l listener object
    */
    public void addSessionsListener (SessionsListener l) {
        sessionsListener.addElement (l);
    }

    /**
    * Removes debugger listener
    *
    * @param l listener object
    */
    public void removeSessionsListener (SessionsListener l) {
        sessionsListener.removeElement (l);
    }

    /**
    * Notificates about creating a session
    *
    * @param threa
    */
    protected void fireSessionCreated (final Session s) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = sessionsListener.size ();
                                            for (i = 0; i < k; i++)
                                                ((SessionsListener)sessionsListener.elementAt (i)).sessionCreated (s);
                                        }
                                    });
    }

    /**
    * Notificates about session death
    *
    * @param thread
    */
    protected void fireSessionDeath (final Session s) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            int i, k = sessionsListener.size ();
                                            for (i = 0; i < k; i++)
                                                ((SessionsListener)sessionsListener.elementAt (i)).sessionDeath (s);
                                        }
                                    });
    }

    // innerclasses .............................................................

    /**
    * Listens on each session for current thread changes and debugger state.
    */  
    private class SessionListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
            AbstractDebugger d = (AbstractDebugger) e.getSource ();
            int os = getState ();
            int ns = d.getState ();

            if (e.getPropertyName ().equals (PROP_STATE)) {
                //S ystem.out.println ("Core debugger.state: " + d.getState ())

                if (os == DEBUGGER_NOT_RUNNING) return;

                if ( (ns != DEBUGGER_NOT_RUNNING) &&
                        ( (d == getCurrentDebugger ()) ||
                          (debuggers.size () < 2)
                        )
                   ) {
                    // current session state changed
                    if (os != ns) {
                        setDebuggerState (ns);
                        if ((os == DEBUGGER_STARTING) && (ns == DEBUGGER_RUNNING)) {
                            firstDebuggerRunning (d);
                        }
                        //            if (ns == DEBUGGER_STOPPED)
                        //              updateWatches ();
                    }
                }

                if (ns == DEBUGGER_RUNNING) {
                    if (!runningDebuggers.containsKey (d)) {
                        // add a new debugger
                        debugerRunning (d);
                        runningDebuggers.put (d, d);
                    }
                }

                if (ns == DEBUGGER_NOT_RUNNING) {
                    // debugger session finish
                    runningDebuggers.remove (d);
                    Session s = findSession (d);
                    removeEvents (d);

                    if ((s != null) && !s.isPersistent ()) {// s == null while debugger starts
                        InputOutput io = s.getInputOutput ();
                        if (io != null) ioToClose.add (io);
                        removeSession (s);
                        if (debuggers.size () > 0)
                            println (bundle.getString ("CTL_Debugger_session_end"), STL_OUT);
                    }
                    if (d == getCurrentDebugger ())
                        setCurrentSession (null);
                    if (debuggers.size () == 0)
                        debuggerNotRunning ();
                }

            } else
                if (e.getPropertyName ().equals (PROP_CURRENT_THREAD)) {

                    // some thread from non current debugger sets uncurrent
                    if ( (d != getCurrentDebugger ()) &&
                            (d.getCurrentThread () == null)
                       ) return;

                    // firstly - when I sleep, I must wake up!!!
                    if ( (os == DEBUGGER_NOT_RUNNING) &&
                            !wakeUpDebugger (d)
                       ) return;

                    setCurrentDebugger (d);
                }
        }
    }


    class Connector extends JPanel implements DebuggerInfoProducer,
        ActionListener {

        private JComboBox             cbConnectors;
        private DebuggerInfoProducer  producer;
        private boolean               doNotListen;


        Connector ()  {
            ResourceBundle bundle = NbBundle.getBundle (DelegatingDebugger.class);

            cbConnectors = new JComboBox ();
            int i, k = registeredDebuggers.size ();
            for (i = 0; i < k; i++) {
                AbstractDebugger ad = createDebugger (
                                          (Class) registeredDebuggers.get (i)
                                      );
                cbConnectors.addItem (ad.getVersion ());
            }

            cbConnectors.setActionCommand ("SwitchMe!"); // NOI18N
            cbConnectors.addActionListener (this);

            setLayout (new GridBagLayout ());
            setBorder (new EmptyBorder (8, 8, 8, 8));
            refresh (0);
        }

        private void refresh (int index) {

            GridBagConstraints c = new GridBagConstraints ();
            c.insets = new Insets (0, 0, 6, 3);
            add (new JLabel (bundle.getString ("CTL_Connect_through")), c);
            c = new GridBagConstraints ();
            c.weightx = 1.0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.gridwidth = 0;
            c.insets = new Insets (0, 3, 6, 0);
            doNotListen = true;
            cbConnectors.setSelectedIndex (index);
            doNotListen = false;
            add (cbConnectors, c);

            AbstractDebugger ad = createDebugger (
                                      (Class) registeredDebuggers.get (index)
                                  );
            c = new GridBagConstraints ();
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = java.awt.GridBagConstraints.BOTH;
            c.gridwidth = 0;
            JComponent comp = ad.getConnectPanel ();
            producer = (DebuggerInfoProducer) comp;
            add (comp, c);
        }

        /**
        * Returns DebuggerInfo
        */
        public DebuggerInfo getDebuggerInfo () {
            return producer.getDebuggerInfo ();
        }

        public void actionPerformed (ActionEvent e) {
            if (doNotListen) return;
            if (e.getActionCommand ().equals ("SwitchMe!")); // NOI18N
            removeAll ();
            refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
            Component w = getParent ();
            while (!(w instanceof Window))
                w = w.getParent ();
            if (w != null) ((Window) w).pack (); // ugly hack...
            return;
        }
    }

}

/*
* Log 
*  10   JPDA Debugger (Gandalf)1.7.1.1     02/15/00 Daniel Prusa    recovery after
*       previous merge
*  9    JPDA Debugger (Gandalf)1.7.1.0     02/14/00 Jan Jancura     
*  8    Gandalf   1.7         02/10/00 Daniel Prusa    StartDebugger starts a new
*       session, NewSession removed
*  7    Gandalf   1.6         01/14/00 Daniel Prusa    NOI18N
*  6    Gandalf   1.5         01/13/00 Daniel Prusa    NOI18N
*  5    Gandalf   1.4         01/06/00 Jan Jancura     Refresh of Threads &
*       Watches, Weakization of Nodes
*  4    Gandalf   1.3
*/
