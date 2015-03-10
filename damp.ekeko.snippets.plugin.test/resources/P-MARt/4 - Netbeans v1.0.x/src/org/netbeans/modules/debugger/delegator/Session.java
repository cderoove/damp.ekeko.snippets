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


import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.debugger.*;
import org.openide.windows.InputOutput;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.AbstractThread;


/**
* Represents one debugged session.
*
* @author Jan Jancura
*/
public class Session {

    /** Property connectionState value constant. */
    public static final String        STATE_CONNECTED =       "Connected"; // NOI18N
    /** Property connectionState value constant. */
    public static final String        STATE_DISCONNECTED =    "Disconnected"; // NOI18N
    /** Property connectionState value constant. */
    public static final String        STATE_HIDDEN =          "Hidden"; // NOI18N
    /** Property connectionState value constant. */
    public static final String        STATE_NOT_RUNNING =     "Not Running"; // NOI18N

    /** Finish action value constant. */
    public static final int           ACTION_FINISH =         0;
    /** Finish action value constant. */
    public static final int           ACTION_DISCONNECT =     1;
    /** Finish action value constant. */
    public static final int           ACTION_HIDE =           2;

    /** Property name constant. */
    public static final String        PROP_HIDDEN =           "hidden"; // NOI18N
    /** Property name constant. */
    public static final String        PROP_PERSISTENT =       "persistent"; // NOI18N
    /** Property name constant. */
    public static final String        PROP_CONNECTION_STATE = "connectionState"; // NOI18N
    /** Property name constant. */
    public static final String        PROP_CURRENT =          "current"; // NOI18N
    /** Property name constant. */
    public static final String        PROP_CONNECTED =        "connected"; // NOI18N
    /** Property name constant. */
    public static final String        PROP_ACTION_ON_FINISH = "actionOnFinish"; // NOI18N


    // variables ..................................................................................

    /** State of connected property. */
    private boolean                   connected = true;
    /** State of dead property. */
    private boolean                   dead = false;
    /** true is session proces is running. */
    private boolean                   running = true;
    private boolean                   persistent = false;
    private String                    sessionName;
    private String                    locationName;
    private String                    connectionState = STATE_CONNECTED;
    private int                       actionOnFinish = ACTION_HIDE;
    private PropertyChangeSupport     pcs;
    /** I/O tab used by this session. */
    private InputOutput               inputOutput;
    /** Instance of debugger of this session. */
    private AbstractDebugger          debugger;
    /** Last curent thread of this session. */
    private AbstractThread            currentThread;
    /** Instance of delegating debugger. */
    private DelegatingDebugger        delegatingDebugger;
    /** Listens on delegating debugger and this session debugger. */
    private DebuggerListener          debuggerListener;
    /** DebuggerInfo used to restart this session. */
    private DebuggerInfo              debuggerInfo;

    // session properties ........................................................................


    /**
    * Creates the new Session for given debuger instance.
    */
    public Session (
        String sessionName,
        String locationName,
        AbstractDebugger debugger,
        DebuggerInfo info
    ) {
        pcs = new PropertyChangeSupport (this);
        this.sessionName = sessionName;
        this.locationName = locationName;
        this.debugger = debugger;
        this.debuggerInfo = info;
        try {
            delegatingDebugger = (DelegatingDebugger) TopManager.getDefault ().
                                 getDebugger ();
        } catch (DebuggerNotFoundException e) {
            // impossible!
        }
        inputOutput = debugger.getInputOutput ();
        debuggerListener = new DebuggerListener ();
        debugger.addPropertyChangeListener (debuggerListener);
        delegatingDebugger.addPropertyChangeListener (debuggerListener);
    }


    // session properties ........................................................................

    /**
    * Get persistent property value.
    */
    public boolean isPersistent () {
        return persistent;
    }

    /**
    * Set persistent property value.
    */
    public void setPersistent (boolean p) {
        if (persistent == p) return;
        persistent = p;
        firePropertyChange (PROP_PERSISTENT, new Boolean (!persistent), new Boolean (persistent));
    }

    /**
    * Returns instance of debugger for this session.
    */
    public AbstractDebugger getDebugger () {
        return debugger;
    }

    /**
    * Returns current thread for this session.
    */
    public AbstractThread getCurrentThread () {
        return currentThread;
    }

    /**
    * Returns instance of debuggerInfo for this session.
    */
    protected DebuggerInfo getDebuggerInfo () {
        return debuggerInfo;
    }

    /**
    * Get InputOutput of session.
    */  
    public InputOutput getInputOutput () {
        return inputOutput;
    }

    /**
    * Return name of session.
    */  
    public String getSessionName () {
        return sessionName;
    }

    /**
    * Return name of location.
    */  
    public String getLocationName () {
        return locationName;
    }

    /**
    * Returns action on finish property value.
    */
    public int getActionOnFinish () {
        return actionOnFinish;
    }

    /**
    * Returns action on finish property value.
    */
    public void setActionOnFinish (int action) {
        if (actionOnFinish == action) return;
        int old = actionOnFinish;
        actionOnFinish = action;
        firePropertyChange (PROP_ACTION_ON_FINISH, new Integer (old), new Integer (actionOnFinish));
    }

    /**
    * Returns connectionState of session.
    */
    public String getConnectionState () {
        return connectionState;
    }

    /**
    * Returns connectionState of session.
    */
    protected void setConnectionState (String connectionState) {
        if (this.connectionState == connectionState) return;
        if ( (connectionState != STATE_CONNECTED) &&
                (connectionState != STATE_DISCONNECTED) &&
                (connectionState != STATE_HIDDEN) &&
                (connectionState != STATE_NOT_RUNNING)
           ) throw new InternalError ("Unknown connectionState constant"); // NOI18N
        String old = this.connectionState;
        this.connectionState = connectionState;
        firePropertyChange (PROP_CONNECTION_STATE, old, connectionState);
    }

    /**
    * Returns true if debugger is currently connected to debugged remote VM.
    */  
    public boolean isCurrent () {
        return debugger == delegatingDebugger.getCurrentDebugger ();
    }

    /**
    * Returns true if debugger is currently connected to debugged remote VM.
    */  
    public boolean isConnected () {
        return connected;
    }

    /**
    * Connects to / disconnects from debugged remote VM.
    */  
    public void setConnected (boolean connected) {
        if (connected == this.connected) return;
        //    this.connected = connected;
        if (connected) {
            // true to connect (wait for DEBUGGER_STARTING)
            try {
                ((AbstractDebugger) getDebugger ()).reconnect ();
            } catch (DebuggerException ex) {
                TopManager.getDefault ().notify (
                    new NotifyDescriptor.Exception (
                        ex.getTargetException () == null ? ex : ex.getTargetException (),
                        NbBundle.getBundle (Session.class).getString ("EXC_Debugger") +
                        ": " + ex.getMessage ()) // NOI18N
                );
            }
            //      setConnectionState (STATE_CONNECTED);
            firePropertyChange (
                PROP_CONNECTED,
                new Boolean (false),
                new Boolean (true)
            );
        } else {
            //DISCONNECT
            try {
                ((AbstractDebugger) getDebugger ()).disconnect ();
            } catch (DebuggerException ex) {
                TopManager.getDefault ().notify (
                    new NotifyDescriptor.Exception (
                        ex.getTargetException () == null ? ex : ex.getTargetException (),
                        NbBundle.getBundle (Session.class).getString ("EXC_Debugger") +
                        ": " + ex.getMessage ()) // NOI18N
                );
            }
            this.connected = false;
            setConnectionState (STATE_DISCONNECTED);
            firePropertyChange (
                PROP_CONNECTED,
                new Boolean (true),
                new Boolean (false)
            );
        }
    }

    /**
    * Returns true if this session VM is running (must return valid value even
    * if session is hidden).
    */  
    public boolean isRunning () {
        return running;
    }

    /**
    * Returns true if this session is hidden.
    */  
    public boolean isHidden () {
        return connectionState == STATE_HIDDEN;
    }

    /**
    * Hides / shows this session.
    */  
    public void setHidden (boolean hidden) {
        if (isHidden () == hidden) return;
        if (hidden) {
            debugger.unmarkCurrent ();
            setConnectionState (STATE_HIDDEN);
            firePropertyChange (
                PROP_HIDDEN,
                new Boolean (false),
                new Boolean (true)
            );
        } else {
            if (isRunning ())
                if (isConnected ())
                    setConnectionState (STATE_CONNECTED);
                else
                    setConnectionState (STATE_DISCONNECTED);
            else
                setConnectionState (STATE_NOT_RUNNING);
            firePropertyChange (
                PROP_HIDDEN,
                new Boolean (true),
                new Boolean (false)
            );
        }
    }

    /**
    * Starts hidden, disonnected or finished sessions (sets connectionState to connected).
    */
    public void start () {
        //S ystem.out.println("Session.start " + this); // NOI18N
        setHidden (false);
        if (!running) {
            try {
                ((AbstractDebugger) getDebugger ()).startDebugger (getDebuggerInfo ());
            } catch (DebuggerException ex) {
                TopManager.getDefault ().notify (
                    new NotifyDescriptor.Exception (
                        ex.getTargetException () == null ? ex : ex.getTargetException (),
                        NbBundle.getBundle (Session.class).getString ("EXC_Debugger") +
                        ": " + ex.getMessage ()) // NOI18N
                );
            }
            return;
        }
        if (!isConnected ())
            setConnected (true);
    }

    /**
    * Finish this session. If this session is not persistent - kills debugged process.
    * Action selected in actionOnFinish property is performed on persistent sessions.
    */
    public void finish () {
        if (isPersistent ()) {
            switch (getActionOnFinish ()) {
            case ACTION_FINISH:
                finishIn ();
                break;
            case ACTION_DISCONNECT:
                setConnected (false);
                break;
            case ACTION_HIDE:
                setHidden (true);
                break;
            }
        } else {
            dead = true;
            if (isConnected ())
                finishIn ();
            else {
                try {
                    ((DelegatingDebugger) TopManager.getDefault ().getDebugger ()).
                    removeSession (this);
                } catch (DebuggerException e) {
                }
            }
        }
    }

    /**
    * If returns true, session will be deleted. Only when finish is called on 
    * non persistent session (not disconnect on non persistent).
    */
    public boolean isDead () {
        return dead;
    }


    // Property Support .................................................................................

    /**
    * Adds PropertyChangeListener to this session instance.
    */
    public void addPropertyChangeListener (PropertyChangeListener listener ) {
        pcs.addPropertyChangeListener (listener);
    }

    /**
    * Adds PropertyChangeListener to this session instance.
    */
    public void removePropertyChangeListener (PropertyChangeListener listener ) {
        pcs.removePropertyChangeListener (listener);
    }

    /**
    * Fires change of property.
    */
    protected void firePropertyChange (String name, Object oldValue, Object newValue) {
        pcs.firePropertyChange (name, oldValue, newValue);
    }


    // other methods .....................................................................................

    protected void debuggerStateChanged (int debuggerState) {
        //S ystem.out.println("Session.debuggerStateChanged " + debuggerState); // NOI18N
        switch (debuggerState) {
        case AbstractDebugger.DEBUGGER_NOT_RUNNING:
            if (connected) {
                //S ystem.out.println("Session.debuggerStateChanged not running "); // NOI18N
                connected = false;
                firePropertyChange (PROP_CONNECTED, new Boolean (true), new Boolean (false));
            }
            running = false;
            setConnectionState (STATE_NOT_RUNNING);
            break;
        case AbstractDebugger.DEBUGGER_STARTING:
            if (getConnectionState () == STATE_HIDDEN) {
                setConnectionState (STATE_DISCONNECTED);
                return;
            }
            connected = true;
            setConnectionState (STATE_CONNECTED);
            firePropertyChange (PROP_CONNECTED, new Boolean (false), new Boolean (true));
            break;
        }
    }

    private void finishIn () {
        try {
            ((AbstractDebugger) getDebugger ()).finishDebugger ();
            //      setConnectionState (STATE_NOT_RUNNING);
        } catch (DebuggerException ex) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception (
                    ex.getTargetException () == null ? ex : ex.getTargetException (),
                    NbBundle.getBundle (Session.class).getString ("EXC_Debugger") +
                    ": " + ex.getMessage () // NOI18N
                )
            );
        }
    }


    // innerclasses ..............................................................

    /**
    * Listens on this session's debugger and delegating debugger on changes
    * of current session and state of debuggers.
    */
    private class DebuggerListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == null) return;
            if ( e.getPropertyName ().equals (DelegatingDebugger.PROP_STATE) &&
                    (e.getSource () == debugger)
               ) {
                // current debugger state has been changed....
                debuggerStateChanged (getDebugger ().getState ());
            } else
                if (e.getPropertyName ().equals (
                            DelegatingDebugger.PROP_CURRENT_SESSION
                        )) {
                    // current session has been changed....
                    pcs.firePropertyChange (
                        PROP_CURRENT,
                        ! isCurrent (),
                        isCurrent ()
                    );
                } else
                    if ( e.getPropertyName ().equals (
                                DelegatingDebugger.PROP_CURRENT_THREAD
                            ) &&
                            (e.getSource () == debugger) &&
                            (debugger.getCurrentThread () != null)
                       ) {
                        // session's current thread has been changed....
                        currentThread = debugger.getCurrentThread ();
                    }
        }
    }
}

/*
* Log
*  1    Jaga      1.0         2/25/00  Daniel Prusa    
* $
*/
