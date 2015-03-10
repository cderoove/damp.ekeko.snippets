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

import java.util.HashSet;
import java.util.Iterator;

import java.awt.event.ComponentAdapter;
import java.awt.Window;
import java.awt.event.ComponentEvent;

/** Implements state management for state machine attached
* to the WindowManagerImpl class.
* This is singleton, the only instance should be achieved
* via call to static method WindowManagerImpl.stateManager().
* This ideally should be an innerclass of WindowManagerImpl,
* but for the size-of-source-reasons it is separated.
*
* @author Dafe Simonek
*/
final class StateManager extends ComponentAdapter {

    /** Constants for possible main states Window Manager impl
    * can be in */
    public static final int READY = 4;
    public static final int SERIALIZING = 8;
    public static final int DESERIALIZING = 16;
    public static final int SWITCHING = 32;

    /** constants for visibility status of window manager */
    public static final int VISIBLE = 1;
    public static final int INVISIBLE = 2;

    /** holds current main state of window manager */
    private int mainState;
    /** holds current visibility state of window manager */
    private int visibilityState;
    /** listeners set 
     * @associates StateListener*/
    private HashSet listeners;
    /** flag for lazy initialization */
    private boolean initialized = false;

    /** Creates new StateManager. Consider it private,
    * should be called only from WindowManagerImpl.
    */
    StateManager () {
    }

    /** Intiializes state and attach listener for visibility state changes */
    private void initialize () {
        Window mainWindow = WindowManagerImpl.mainWindow();
        // initialize states
        mainState = READY;
        visibilityState = mainWindow.isVisible()
                          ? StateManager.VISIBLE : StateManager.INVISIBLE;
        // attach listener
        mainWindow.addComponentListener(this);
    }

    public synchronized void addStateListener (StateListener sl) {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        if (listeners == null)
            listeners = new HashSet(10);
        listeners.add(sl);
    }

    public synchronized void removeStateListener (StateListener sl) {
        if (listeners != null)
            listeners.remove(sl);
    }

    /** @return current window manager state as bitwise OR of
    * main state and visibility state */
    public int getState () {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        return mainState | visibilityState;
    }

    /** Fires state change event to listeners */
    void fireStateChanged (int state) {
        if (listeners == null)
            return;
        HashSet cloned = null;
        synchronized (this) {
            cloned = (HashSet)listeners.clone();
        }
        for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
            ((StateListener)iter.next()).stateChanged(state);
        }
    }

    /** Sets current stste window manager is in.
    * Should be called only from WindowManagerImpl.
    * Remember, visibility state is handled directly by this
    * object, so there is no need to set it from window manager impl. */
    void setMainState (int state) {
        if (!initialized) {
            initialized = true;
            initialize();
        }
        if (state == mainState)
            return;
        mainState = state;
        fireStateChanged(mainState | visibilityState);
    }

    /** Sets current visibility state window manager is in.
    */
    private void setVisibilityState (int state) {
        if (visibilityState == state)
            return;
        visibilityState = state;
        fireStateChanged(mainState | visibilityState);
    }

    /** when main window hidden, update state accordingly */
    public void componentHidden (ComponentEvent e) {
        setVisibilityState(INVISIBLE);
    }

    /** when main window shown, update state accordingly */
    public void componentShown (ComponentEvent e) {
        setVisibilityState(VISIBLE);
    }

    /** Simple stupid interface for notifying about state changes */
    static interface StateListener {
        /** @param state current state of window manager.
        * It is bitwise OR of main state and visibility state.
        */
        public void stateChanged (int state);
    }

}

/*
* Log
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/15/99  David Simonek   debug prints removed
*  1    Gandalf   1.0         9/8/99   David Simonek   
* $
*/