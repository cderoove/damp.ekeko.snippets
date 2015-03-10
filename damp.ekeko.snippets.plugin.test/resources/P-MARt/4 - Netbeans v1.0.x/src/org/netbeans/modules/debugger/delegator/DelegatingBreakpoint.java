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

import java.util.Arrays;
import java.util.ArrayList;
import org.openide.text.Line;
import org.openide.nodes.Node.Property;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.ClassBreakpointEvent;


/**
* Core breakpoint delegates all functionality on inner instance of breakpint.
* @see org.openide.debugger.Breakpoint
*
* @author   Jan Jancura
*/
public class DelegatingBreakpoint extends CoreBreakpoint {
    /** generated Serialized Version UID */
    static final long               serialVersionUID = 2246204002781932191L;


    // private variables .....................................................

    private transient ArrayList events = new ArrayList ();
    private boolean doNotFire = false;


    // init ....................................................................

    /**
    * Non public constructor called from the AbstractDebugger only.
    * User must create breakpoint from Debugger.getNewBreakpoint () method.
    */
    DelegatingBreakpoint (DelegatingDebugger debugger, boolean hidden) {
        super (debugger, hidden);
    }

    protected void init () throws java.io.IOException {
        events = new ArrayList ();
        super.init ();
    }


    // DelegatingBreakpoint ....................................................................

    public void setEvent (CoreBreakpoint.Event e) {
        clearBreakpoint ();
        Event old = event;
        event = e.get (this);
        initActions ();

        Session[] s = ((DelegatingDebugger) debugger).getSessions ();
        int i, k = s.length;
        for (i = 0; i < k; i++)
            addDebugger (s [i].getDebugger ());
        firePropertyChange (PROP_EVENT, old, event);
    }

    /**
    * Returns current breakpoint event for given instance of debugger (used
    * in multisession debugger implementation).
    */
    public Event getEvent (AbstractDebugger d) {
        if (d == debugger)
            return event;
        synchronized (events) {
            int i, k = events.size ();
            for (i = 0; i < k; i++) {
                Event e = (Event) events.get (i);
                if (d == e.getDebugger ())
                    return e;
            }
        }
        return null;
    }

    /**
    * Sets or updates breakpoint. Calls set method of currently selected event for all debuggers, sets
    * value of valid property and marks lines in the editor.
    */
    protected void setBreakpoint () {
        if (!isEnabled ()) return;
        if (event == null) {
            setValid (false);
            return;
        }
        boolean valid = false;
        /*    try {
              valid = event.set ();
            } catch (ClassCastException e) {
            }*/
        Line[] l = event.getLines ();
        if (l != null)
            remark (new ArrayList (Arrays.asList (l)));
        else
            remark (null);
        //    setValid (valid);

        if (debugger.getState () == AbstractDebugger.DEBUGGER_NOT_RUNNING) {
            removeEvents ();
        } else {
            synchronizeProperties ();
            ArrayList a = (ArrayList) events.clone ();
            int i, k = a.size ();
            Event ee;
            for (i = 0; i < k; i++)
                if ((ee = (Event) a.get (i)).set ()) {
                    valid = true;
                    if (l == null) {
                        l = ee.getLines ();
                        if (l != null)
                            remark (new ArrayList (Arrays.asList (l)));
                    }
                }
        }
        setValid (valid);
    }

    /**
    * Removes breakpoint from this position.
    * Clears Line and breakpoint from RemoteDebugger.
    */
    protected void clearBreakpoint () {
        mark (false);
        lines = null;
        /*    if (event != null)
              try {
                event.remove ();
              } catch (ClassCastException e) {
              }*/
        setValid (false);
        removeEvents ();
    }

    protected Event initEvent (Class cl) {
        Event old = event;
        event = getEvent (cl);
        event = event.get (this);
        removeEvents ();
        initActions ();

        // if event moved form class => line, we let property className
        if ( (old != null) &&
                (old instanceof ClassBreakpointEvent) &&
                (event instanceof ClassBreakpointEvent)
           ) ((ClassBreakpointEvent) event).setClassName (
                   ((ClassBreakpointEvent) old).getClassName ()
               );

        // add events for each started debugger
        Session[] s = ((DelegatingDebugger) debugger).getSessions ();
        int i, k = s.length;
        for (i = 0; i < k; i++)
            addEvent (s [i].getDebugger ());
        return event;
    }

    /**
    * Fires event change and synchronizes properties.
    */
    protected void firePropertyChange (String s, Object o, Object n) {
        if (doNotFire) return;
        super.firePropertyChange (s, o, n);
        synchronizeProperties ();
    }

    /**
    * Fires property change of some event property (Needs re-setBreakpoint).
    */
    protected void fireEventPropertyChange (Event e, String s, Object o, Object n) {
        //S ystem.out.println ("fireEventPropertyChange " + e + " : " + s); // NOI18N
        if (e != event) return;
        //S ystem.out.println ("fireEventPropertyChange OK " + doNotFire); // NOI18N
        firePropertyChange (null, o, n);
        if (isEnabled ()) setBreakpoint ();
    }

    // other methods ................................................................................

    /**
    * Creates breakpoint event for given debugger instance, adds it to 
    * events and calls set for it.
    */
    void addDebugger (AbstractDebugger debugger) {
        if (event == null) return;
        Event e = addEvent (debugger);
        if (e == null) return;
        if (isEnabled ())
            if (e.set ()) setValid (true);
    }

    /**
    * Creates breakpoint event for given debugger instance and adds it to 
    * events.
    */
    Event addEvent (AbstractDebugger debugger) {
        Event e = debugger.getBreakpointEvent (event.getTypeName ());
        if (e == null) return null;
        e = e.get (this, debugger);
        synchronizeProperties (e);
        synchronized (events) {
            events.add (e);
        }
        return e;
    }

    /**
    * Removes debugger event for given debugger instance.
    */
    void removeDebugger (AbstractDebugger debugger) {
        ArrayList a = (ArrayList) events.clone ();
        int i, k = a.size ();
        Event ee;
        for (i = 0; i < k; i++)
            if ((ee = (Event) a.get (i)).getDebugger () == debugger) {
                ee.remove ();
                synchronized (events) {
                    events.remove (ee);
                }
                return;
            }
    }

    /**
    * Synchronizes properties of all events of all debuggers.
    */
    private void synchronizeProperties () {
        ArrayList a = (ArrayList) events.clone ();
        int i, k = a.size ();
        for (i = 0; i < k; i++)
            synchronizeProperties ((Event) a.get (i));
    }

    /**
    * Synchronizes properties with given event. [event => e}
    */
    private void synchronizeProperties (Event e) {
        if (event == null) return;
        doNotFire = true;
        Property[] s = event.getProperties ();
        Property[] d = e.getProperties ();  //S ystem.out.println("DelegatingBreakpoint.synchronize: " + event + " to: " + e); // NOI18N
        int i,k = Math.min (d.length, s.length);
        for (i = 0; i < k; i++)
            try {
                d [i].setValue (s [i].getValue ());  //S ystem.out.println("DelegatingBreakpoint.synchronize: " + s [i].getName () + " to: " + d [i].getName ()); // NOI18N
            } catch (Exception ex) {  //ex.p rintStackTrace();
            }
        doNotFire = false;
    }

    /**
    * Calls remove () for each event, and clears events.
    */
    private void removeEvents () {
        synchronized (events) {
            int i, k = events.size ();
            for (i = 0; i < k; i++) {
                ((Event) events.get (i)).remove ();
            }
            events = new ArrayList ();
        }
    }
}

/*
* Log
* $
*/
