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

import java.beans.PropertyEditor;
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
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.StepEvent;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.jpda.util.*;

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractThread;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ThreadBreakpoint extends CoreBreakpoint.Event implements Executor, StopEvent {

    // static ....................................................................................

    /** Property name constant. */
    public static final String            PROP_TYPE = "type"; // NOI18N
    /** Property type constant. */
    public static final int               TYPE_START = 1;
    /** Property type constant. */
    public static final int               TYPE_DEATH = 2;


    // variables ....................................................................................

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private int                           type = TYPE_START;
    private transient String              action;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new ThreadBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () { //S ystem.out.println ("ThreadBreakpoint.set TRY " + this + " in " + getDebugger () ); // NOI18N
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            requestor.removeRequests ();

            if ((type & TYPE_START) != 0) {
                ThreadStartRequest tsr = debugger.requestManager.createThreadStartRequest ();
                tsr.setSuspendPolicy (ThreadStartRequest.SUSPEND_ALL);
                debugger.operator.register (tsr, this);
                requestor.add (tsr);
                tsr.enable (); //S ystem.out.println ("ThreadBreakpoint.set START "); // NOI18N
            }
            if ((type & TYPE_DEATH) != 0) {
                ThreadDeathRequest tdr = debugger.requestManager.createThreadDeathRequest ();
                tdr.setSuspendPolicy (ThreadDeathRequest.SUSPEND_ALL);
                debugger.operator.register (tdr, this);
                requestor.add (tdr);
                tdr.enable (); //S ystem.out.println ("ThreadBreakpoint.set DEATH "); // NOI18N
            }
        } catch (VMDisconnectedException e) {
        }
        return true;
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {  //S ystem.out.println ("ThreadBreakpoint.remove " + this); // NOI18N
        if (requestor != null)
            requestor.removeRequests ();
    }

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        final ResourceBundle bundle = NbBundle.getBundle (VariableBreakpoint.class);
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (
                       PROP_TYPE,
                       Integer.TYPE,
                       bundle.getString ("PROP_breakpoint_type"),
                       bundle.getString ("HINT_breakpoint_type")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return new Integer (getType ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setType (((Integer) val).intValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                       public PropertyEditor getPropertyEditor () {
                           return new ChoicePropertyEditor (
                                      new int[] {
                                          TYPE_START,
                                          TYPE_DEATH,
                                          TYPE_START | TYPE_DEATH
                                      },
                                      new String[] {
                                          bundle.getString ("CTL_Property_type_start_name"),
                                          bundle.getString ("CTL_Property_type_death_name"),
                                          bundle.getString ("CTL_Property_type_both_name"),
                                      }
                                  );
                       }
                   }
               };
    }

    /**
    * Returns line of breakpoint.
    */
    public Line[] getLines () {
        return null;
    }


    /**
    * Returns name of type of this event.
    */
    public String getTypeName () {
        return "Thread"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Thread_event_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        if (type == TYPE_START)
            return NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Thread_start_event_name");
        else
            if (type == TYPE_DEATH)
                return NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Thread_death_event_name");
        return NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Thread_event_name");
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnThread"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new ThreadBreakpointPanel (this);
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new ThreadPrintAction (),
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
    }


    // interface Executor .....................................................................

    /**
    * Executes breakpoint hit event.
    */
    public void exec (com.sun.jdi.event.Event event) {  //S ystem.out.println ("ThreadBreakpoint.exec! " + this + " : " + event); // NOI18N
        if (event instanceof ThreadStartEvent) {
            thread = ((ThreadStartEvent) event).thread ();
            action = NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Started");
        } else {
            thread = ((ThreadDeathEvent) event).thread ();
            action = NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Death");
        }
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
    * Returns type of breakpoint.
    */
    public int getType () {
        return type;
    }

    /**
    * Sets type of breakpoint.
    */
    public void setType (int type) {
        if (type == this.type) return;
        if ((type & (TYPE_START | TYPE_DEATH)) == 0)
            throw new IllegalArgumentException  ();
        int old = type;
        this.type = type;
        firePropertyChange (PROP_TYPE, new Integer (old), new Integer (type));
    }

    public String toString () {
        return "JPDAThreadBreakpoint " + getType (); // NOI18N
    }


    // innerclasses ......................................................................................

    class ThreadPrintAction extends PrintAction {

        /**
        * Creates the new Thrad Print action with default text.
        */
        ThreadPrintAction () {
            super (
                NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Thread_print_name")
            );
        }

        /**
        * Returns new initialized instance of Thrad Print action.
        */
        protected CoreBreakpoint.Action getNewInstance () {
            return new ThreadPrintAction ();
        }

        /**
        * Resolving special tags:
        *   action          name of action
        */
        protected void resolveTag (String tag, CoreBreakpoint.Event event, StringBuffer sb) {
            if (tag.equals ("action")) { // NOI18N
                sb.append (((ThreadBreakpoint) event).action);
            } else
                super.resolveTag (tag, event, sb);
        }
    }
}

/*
* Log
*  7    Gandalf-post-FCS1.5.4.0     3/28/00  Daniel Prusa    
*  6    Gandalf   1.5         1/14/00  Daniel Prusa    NOI18N
*  5    Gandalf   1.4         1/13/00  Daniel Prusa    NOI18N
*  4    Gandalf   1.3         11/8/99  Jan Jancura     Somma classes renamed
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/28/99  Jan Jancura     
*  1    Gandalf   1.0         9/28/99  Jan Jancura     
* $
*/
