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
import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.LocatableEvent;
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

import org.netbeans.modules.debugger.jpda.util.*;

import org.netbeans.modules.debugger.support.MethodBreakpointEvent;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.CallStackFrame;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class MethodBreakpoint extends MethodBreakpointEvent implements Executor, StopEvent {

    // static ....................................................................................

    /** Property name constant. */
    public static final String            PROP_ALL_METHODS = "allMethods"; // NOI18N


    // variables ....................................................................................

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private ReferenceType                 tryClass;
    /** Curent method lines. */
    private LinkedList                    lines = new LinkedList ();
    private Line[]                        linesArray;
    private boolean                       allMethods = false;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new MethodBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {
        //S ystem.out.println ();
        //S ystem.out.println ("  " + this + ":set TRY  in " + getDebugger () ); // NOI18N
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if ((getClassName () == null) || (getClassName ().trim ().length () < 1)) return false;
        if ( (!getAllMethods ()) &&
                ( (getMethodName () == null) ||
                  (getMethodName ().trim ().length () < 1)
                )
           ) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            requestor.removeRequests ();

            if (allMethods) {
                MethodEntryRequest mer = debugger.requestManager.createMethodEntryRequest ();
                mer.addClassFilter (getClassName ());
                mer.setSuspendPolicy (MethodEntryRequest.SUSPEND_ALL);
                debugger.operator.register (mer, this);
                requestor.add (mer);
                mer.enable ();
                //S ystem.out.println ();
                //S ystem.out.println ("    " + this + ":set ALL METHODS " + getClassName ()); // NOI18N
                return true;
            }

            // For unloaded class
            //S ystem.out.println ();
            //S ystem.out.println ("    " + this + ":set class br. " + getClassName ()); // NOI18N
            ClassPrepareRequest cpr = debugger.requestManager.createClassPrepareRequest ();
            cpr.addClassFilter (getClassName ()); // NOI18N
            cpr.setSuspendPolicy (BreakpointRequest.SUSPEND_ALL);
            debugger.operator.register (cpr, this);
            requestor.add (cpr);
            cpr.enable ();

            List l = debugger.virtualMachine.classesByName (getClassName ());
            if (l.size () == 0) return false;

            // Known classes
            //S ystem.out.println ();
            //S ystem.out.println ("    " + this + ":set " + l.size () + " are loaded"); // NOI18N
            int i, k = l.size ();
            boolean set = false;
            for (i = 0; i < k; i++)
                if (set ((ReferenceType) l.get (i)))
                    set = true;
            return set;
        } catch (VMDisconnectedException e) {
        }
        return false;
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {  //S ystem.out.println ("MethodBreakpoint.remove " + this); // NOI18N
        //S ystem.out.println (); // NOI18N
        //S ystem.out.println ("  " + this + ":remove "); // NOI18N
        if (requestor != null)
            requestor.removeRequests ();
        linesArray = null;
        lines = new LinkedList ();
    }

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        ResourceBundle bundle = NbBundle.getBundle (VariableBreakpoint.class);
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (
                       CoreBreakpoint.PROP_CLASS_NAME,
                       String.class,
                       bundle.getString ("PROP_breakpoint_class_name"),
                       bundle.getString ("HINT_breakpoint_class_name")
                   ) {
                       public Object getValue () {
                           return getClassName ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setClassName (((String)val).trim ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       CoreBreakpoint.PROP_METHOD_NAME,
                       String.class,
                       bundle.getString ("PROP_breakpoint_method_name"),
                       bundle.getString ("HINT_breakpoint_method_name")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return getMethodName ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setMethodName (((String)val).trim ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       PROP_ALL_METHODS,
                       Boolean.TYPE,
                       bundle.getString ("PROP_breakpoint_all_methods"),
                       bundle.getString ("HINT_breakpoint_all_methods")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return new Boolean (getAllMethods ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setAllMethods (((Boolean) val).booleanValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   }
               };
    }

    /**
    * Returns line of breakpoint.
    */
    public Line[] getLines () {
        if (linesArray != null) return linesArray;
        if (lines.size () == 0) return null;
        linesArray = (Line[]) lines.toArray (new Line [lines.size ()]);  //S ystem.out.println("MethodBreakpoint.getLines : " + lines.size ()); // NOI18N
        return linesArray;
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new MethodBreakpointPanel (this);
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        if (getAllMethods ())
            return new MessageFormat (
                       NbBundle.getBundle (MethodBreakpoint.class).getString ("CTL_All_method_event_name")
                   ).format (new Object[] {getClassName ()});
        else
            return new MessageFormat (
                       NbBundle.getBundle (MethodBreakpoint.class).getString ("CTL_Method_event_name")
                   ).format (new Object[] {getClassName (), getMethodName ()});
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new PrintAction (PrintAction.BREAKPOINT_METHOD_TEXT),
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
        if (thread == null) return null;
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
        /*    try {
              RemoteStackVariable rsv = thread.getCurrentFrame ().getLocalVariable ("this");
              return new ToolsVariable (
                (ToolsDebugger) getDebugger (),
                rsv.getName (),
                rsv.getValue (), 
                rsv.getType ().toString ()
              );
            } catch (Exception e) {
              return null;
            }  */
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
        thread = ((LocatableEvent) event).thread ();
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        try {
            if (thread.frame (0).location ().method ().isSynthetic ()) {
                debugger.operator.resume ();
                return;
            }
        }
        catch (Exception e) {
        }
        if (debugger.resolveCanBeCurrent (thread))
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


    // other methods ......................................................................................

    /**
    * 
    */
    public boolean getAllMethods () {
        return allMethods;
    }

    /**
    * 
    */
    public void setAllMethods (boolean all) {
        if (allMethods == all) return;
        boolean old = allMethods;
        allMethods = all;
        firePropertyChange (PROP_ALL_METHODS, new Boolean (old), new Boolean (allMethods));
    }

    /**
    * Sets breakpoint for given class.
    */
    public boolean set (ReferenceType clazz) {
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if ( (!getAllMethods ()) &&
                ( (getMethodName () == null) ||
                  (getMethodName ().trim ().length () < 1)
                )
           ) return false;
        try {
            List methods = clazz.methods ();
            //S ystem.out.println ("  " + this + ":set2 all methods numb. " +
            //                    methods.size ()); // NOI18N
            boolean ok = false;
            int j, jj = methods.size ();
            for (j = 0; j < jj; j++) {
                Method method = (Method) methods.get (j);
                if (!method.name ().equals (getMethodName ())) continue;
                Location loc = method.location ();
                if (loc == null) {
                    //S ystem.out.println ("  " + this + ":set2 method " + method.name () +
                    //                    " :loc. : null"); // NOI18N
                    continue;
                }
                addLine (loc);
                //S ystem.out.println ("  " + this + ":set2 method " + method.name () + " :loc. : " + loc.lineNumber ()); // NOI18N
                //removeRequests ();
                BreakpointRequest br = debugger.requestManager.createBreakpointRequest (loc);
                br.setSuspendPolicy (BreakpointRequest.SUSPEND_ALL);
                debugger.operator.register (br, this);
                requestor.add (br);
                br.enable ();
                ok = true;
                //S ystem.out.println ("  " + this + ":set2 method OK !!!!!!!"); // NOI18N
            }
            return ok;
        } catch (Exception e) {
            //S ystem.out.println ("    " + this + ":set2 " + e); // NOI18N
        }
        return false;
    }

    /**
    * Sets current method line.
    */
    private void addLine (Location l) {
        linesArray = null;
        Line line = Utils.getLine (className, l.lineNumber ());
        if (line != null)
            lines.add (line);
    }

    public String toString () {
        return "JPDAMethodBreakpoint " + getClassName () + "."  + getMethodName (); // NOI18N
    }
}

/*
* Log
* $
*/
