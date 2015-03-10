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

package org.netbeans.core.windows;

import java.util.ArrayList;

import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.TopManager;

/** Support for deferred performing of various actions which
* need window system in consistent state.
* Clients will implement inner interface DeferredCommand
* and use putRequest method to achieve desired functionality.<p>
*
* Design follows principles of well known "Command" design pattern.
*
* @author  Dafe Simonek
*/
public class DeferredPerformer extends Object implements StateManager.StateListener {

    /** not initialized yet (before first request) */
    static final int NOT_INITIALIZED = 0;
    /** ready for immediate open */
    static final int READY = 1;
    /** immediate open is not possible now, must be deferred */
    static final int DEFERRED = 2;
    /** holds simplified state of window manager
    * either READY or DEFERRED (unitialized at startup) */
    int state;
    /** Array of DeferredEntry entries containing top components
    * whose open is deferred 
    * @associates DeferredEntry*/
    ArrayList requests;
    /** Asociation with window manager's state machine support */
    StateManager stateManager;

    /** Consider it private, should be called only from
    * window manager implementation.
    */
    DeferredPerformer() {
        state = NOT_INITIALIZED;
    }

    /** Puts a command to the deferred performer.
    * If window manager is in consistent state, specified command
    * will be executed immediately, otherwise it will be deferred
    * until window manager comes to consistent state. */
    public synchronized void putRequest (DeferredCommand dc, Object context) {
        if (state == NOT_INITIALIZED) {
            initialize();
        }
        switch (state) {
        case READY:
            // execute immediately
            dc.performCommand(context);
            break;
        case DEFERRED:
            // delay the execution (add to the queue)
            DeferredEntry entry = new DeferredEntry();
            entry.command = dc;
            entry.context = context;
            requests.add(entry);
            break;
        }
    }

    /** @return true if window manager is in the state that allows
    * immediate opening of top components and showing workspaces */
    public boolean canImmediatelly () {
        return state == READY;
    }

    /** Initializes listening to window manager states
    * and update inner state */
    private void initialize () {
        requests = new ArrayList(40);
        stateManager = ((WindowManagerImpl)TopManager.getDefault().
                        getWindowManager()).stateManager();
        updateState();
        stateManager.addStateListener(this);
    }

    public synchronized void stateChanged (int newState) {
        int oldState = state;
        updateState();
        // execute all requests
        // if we changing back to READY state
        if ((state != oldState) && (state == READY)) {
            DeferredEntry[] entries =
                (DeferredEntry[])requests.toArray(new DeferredEntry[0]);
            requests.clear();
            for (int i = 0; i < entries.length; i++) {
                entries[i].command.performCommand(entries[i].context);
            }
            // testing focus
            /*new Thread (new Runnable () {
              public void run () {
                while (true) {
                  TopComponent tc = TopComponent.getRegistry ().getActivated ();
                  java.awt.Component c = javax.swing.SwingUtilities.findFocusOwner (tc);
                  System.out.println("TopComponent: " + tc);
                  System.out.println("-------");
                  System.out.println("Focus owner : " + c);
                  System.out.println("-------");
                  System.out.println("");
                  synchronized (this) {
                    try {
                      wait(5000);
                    } catch (InterruptedException exc) {
                      return;
                    }
                  }
                }
              }
        }).start();*/
        }
    }

    /** Helper method, updates inner state according to
    * current window manager state */
    private void updateState () {
        int wmState = stateManager.getState();
        if (((wmState & StateManager.VISIBLE) != 0) &&
                ((wmState & (StateManager.READY | StateManager.SWITCHING)) != 0)) {
            state = READY;
        } else {
            state = DEFERRED;
        }
    }

    /** Interface for commands to be performed using DeferredPerformer.
    * Clients that want to perform some action which relies on consistent
    * state of window manager will need to implement this interface.
    */
    public interface DeferredCommand {

        /** Performs the command */
        public void performCommand (Object context);

    } // end of DeferredCommand inner interface

    /** Stupid data structure, serves as item in deferred list */
    private static final class DeferredEntry {
        DeferredCommand command;
        Object context;
    }


}

/*
* Log
*  2    Gandalf   1.1         11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  1    Gandalf   1.0         11/6/99  David Simonek   
* $ 
*/ 
