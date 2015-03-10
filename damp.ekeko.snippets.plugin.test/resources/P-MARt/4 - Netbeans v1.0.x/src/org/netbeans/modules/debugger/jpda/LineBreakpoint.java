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

import java.util.ResourceBundle;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.modules.debugger.jpda.util.*;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Location;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.StepEvent;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.LineBreakpointEvent;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.StopAction;


/**
*
* @author   Jan Jancura
*/
public class LineBreakpoint extends LineBreakpointEvent implements Executor, StopEvent {

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private ReferenceType                 tryClass;

    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new LineBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {
        //S ystem.out.println ();
        //Threa d.dumpStack();
        //S ystem.out.println ("  " + this + ":set TRY  in " + getDebugger () ); // NOI18N


        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if ((getClassName () == null) || (getClassName ().trim ().length () < 1)) return false;
        if (getLineNumber () < 0) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            if (debugger.virtualMachine == null) return false;
            requestor.removeRequests ();

            // For unloaded class
            //S ystem.out.println ();
            //S ystem.out.println ("    " + this + ":set class br. " + getClassName () + "*"); // NOI18N
            ClassPrepareRequest cpr = debugger.requestManager.createClassPrepareRequest ();
            cpr.addClassFilter (getClassName () + "*"); // NOI18N
            cpr.setSuspendPolicy (ClassPrepareRequest.SUSPEND_ALL);
            debugger.operator.register (cpr, this);
            requestor.add (cpr);
            cpr.enable ();

            List l = debugger.virtualMachine.classesByName (getClassName ());
            if (l.size () == 0) return false;

            // Known classes
            //S ystem.out.println ();
            //S ystem.out.println ("    " + this + ":set " + l.size () + " are loaded"); // NOI18N
            ArrayList list = new ArrayList (l);
            boolean set = false;
            int i = 0;
            while (i < list.size ()) {
                ReferenceType ref = (ReferenceType) list.get (i);
                if (set (ref))
                    set = true;

                List nested = null;
                try {
                    nested = ref.nestedTypes ();
                } catch (ObjectCollectedException ex) {
                }
                if (nested != null)
                    list.addAll (nested);
                i++;
    	    }
            return set;
        } catch (VMDisconnectedException e) {
        }
        return false;
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {
        //S ystem.out.println (); // NOI18N
        //S ystem.out.println ("  " + this + ":remove "); // NOI18N
        if (requestor != null)
            requestor.removeRequests ();
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new PrintAction (PrintAction.BREAKPOINT_TEXT),
                                            };
        CoreBreakpoint.Action[] actions = new CoreBreakpoint.Action [super.getBreakpointActions ().length + myActions.length];
        System.arraycopy (super.getBreakpointActions (), 0, actions, 0, super.getBreakpointActions ().length);
        System.arraycopy (myActions, 0, actions, super.getBreakpointActions ().length, myActions.length);
        return actions;
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public AbstractThread getThread () {
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        return debugger.threadManager.getThread (thread);
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public CallStackFrame[] getCallStack () {
        return getThread ().getCallStack ();
    }

    /**
    * Aditional ifno about debugger state when this event occures.
    * If event do not produce this type of info, null is returned.
    */
    public AbstractVariable getVariable () {
        return null;
    }


    // interface Executor .....................................................................

    /**
    * Executes breakpoint hit event.
    */
    public void exec (com.sun.jdi.event.Event event) {
        //S ystem.out.println ();
        //S ystem.out.println (this + ":exec " + event); // NOI18N
        if (event instanceof ClassPrepareEvent) {
            tryClass = ((ClassPrepareEvent) event).referenceType ();
            boolean v = set (tryClass);
            if (v && !getBreakpoint ().isValid ()) setValid (true);
            ((JPDADebugger) getDebugger ()).operator.resume ();
            return;
        }
        thread = ((BreakpointEvent) event).thread ();
        if (((JPDADebugger) getDebugger ()).resolveCanBeCurrent (thread))
            return;
        perform ();
    }


    // StopEvent impl ......................................................................................

    /**
    * Performs stop action.
    */
    public void stop (boolean stop) {
        ((JPDADebugger) getDebugger ()).stop (stop, getThread ());
    }


    // other methods .........................................................................

    /**
    * Sets breakpoint for given class.
    */
    public boolean set (ReferenceType clazz) {
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if (getLineNumber () < 0) return false;
        try {
            // Known class
            List locL = null;
            try {
                locL = clazz.locationsOfLine (getLineNumber ());
            } catch (Exception e) {
                //S ystem.out.println ("    " + this + ":set2 " + e); // NOI18N
            }
            //S ystem.out.println ("  " + this + ":set2 exec br. for class filter " + clazz + " lines numb(rt.locationsOfLine): " + (locL == null ? "null" : "" + locL.size ())); // NOI18N
            if ((locL == null) || (locL.size () < 1)) return false;
            Location loc = (Location) locL.get (0);

            // PATCH for breakpoints in top-level package private classes, try to obtain Lin
            try {
                CoreBreakpoint breakpoint = getBreakpoint ();
                if ((breakpoint.getLine () == null) && (!breakpoint.isHidden ())) {
                    Line newLine = Utils.getLineForSource (className, loc.sourceName (), getLineNumber ());
                    if (newLine != null)
                        breakpoint.setLine (newLine);
                }
            } catch (AbsentInformationException e) {
            }
            // end of PATC
        	
            // add breakpoint
            BreakpointRequest br = debugger.requestManager.createBreakpointRequest (loc);
            br.setSuspendPolicy (BreakpointRequest.SUSPEND_ALL);
            debugger.operator.register (br, this);
            requestor.add (br);
            br.enable ();
            //S ystem.out.println ("  " + this + ":set2 OK! "); // NOI18N
            return true;
        } catch (VMDisconnectedException e) {
        }
        return false;
    }

    public String toString () {
        return "JPDALineBreakpoint " + getClassName () + "."  + getLineNumber (); // NOI18N
    }
}

/*
* Log
* $
*/
