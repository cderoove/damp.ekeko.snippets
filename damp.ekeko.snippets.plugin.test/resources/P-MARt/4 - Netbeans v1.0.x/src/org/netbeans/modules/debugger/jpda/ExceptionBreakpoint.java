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
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.StepEvent;

import org.openide.TopManager;
import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.jpda.util.*;

import org.netbeans.modules.debugger.support.*;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;


/**
* Implementation of breakpoint on exception.
*
* @author   Jan Jancura
*/
public class ExceptionBreakpoint extends ClassBreakpointEvent implements Executor, StopEvent {


    // static ......................................................................................

    /** Property name constant. */
    public static final String                  PROP_CATCH_TYPE = "catchType"; // NOI18N

    /** Catch type property value constant. */
    public static final int                     EXCEPTION_CATCHED = 1;
    /** Catch type property value constant. */
    public static final int                     EXCEPTION_UNCATCHED = 2;


    // variables ......................................................................................

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private ReferenceType                 tryClass;
    private JPDAVariable                  exception;
    private int                           catchType = EXCEPTION_UNCATCHED;

    {
        className = ((AddBreakpointAction) SystemAction.get
                     (AddBreakpointAction.class)).getCurrentIdentifier ();
    }


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new ExceptionBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {  //S ystem.out.println("ExceptionBreakpoint.set TRY " + this + " in " + getDebugger () ); // NOI18N
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if ((getClassName () == null) || (getClassName ().trim ().length () < 1)) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            requestor.removeRequests ();

            // For unloaded classes
            ClassPrepareRequest cpr = debugger.requestManager.createClassPrepareRequest ();  //S ystem.out.println ("ExceptionBreakpoint.set CLASSES " + getClassName ()); // NOI18N
            cpr.addClassFilter (getClassName ());
            cpr.setSuspendPolicy (ExceptionRequest.SUSPEND_ALL);
            debugger.operator.register (cpr, this);
            requestor.add (cpr);
            cpr.enable ();

            List l = debugger.virtualMachine.classesByName (getClassName ());
            if (l.size () == 0) return false;
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                set (((ReferenceType) l.get (i)));
            return true;
        } catch (VMDisconnectedException e) {
        }
        return false;
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {  //S ystem.out.println ("ExceptionBreakpoint.remove " + this); // NOI18N
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
                       CoreBreakpoint.PROP_CLASS_NAME,
                       String.class,
                       bundle.getString ("PROP_Exception_class_name"),
                       bundle.getString ("HINT_Exception_class_name")
                   ) {
                       public Object getValue () {
                           return getClassName ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setClassName (((String) val).trim ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       PROP_CATCH_TYPE,
                       Integer.TYPE,
                       bundle.getString ("PROP_Exception_type_name"),
                       bundle.getString ("HINT_Exception_type_name")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return new Integer (getCatchType ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setCatchType (((Integer) val).intValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                       public PropertyEditor getPropertyEditor () {
                           return new ChoicePropertyEditor (
                                      new int[] {
                                          EXCEPTION_CATCHED,
                                          EXCEPTION_UNCATCHED,
                                          EXCEPTION_CATCHED | EXCEPTION_UNCATCHED
                                      },
                                      new String[] {
                                          bundle.getString ("CTL_Exception_type_catched"),
                                          bundle.getString ("CTL_Exception_type_uncatched"),
                                          bundle.getString ("CTL_Exception_type_both")
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
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new PrintAction (PrintAction.BREAKPOINT_EXCEPTION_TEXT),
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
        return exception;
    }

    /**
    * Returns name of type of this event.
    */
    public String getTypeName () {
        return "Exception"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (ExceptionBreakpoint.class).getString ("CTL_Exception_event_name_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        if ((getCatchType () & EXCEPTION_CATCHED) != 0)
            if ((getCatchType () & EXCEPTION_UNCATCHED) != 0)
                return new MessageFormat (
                           NbBundle.getBundle (ExceptionBreakpoint.class).getString ("CTL_Exception_event_name")
                       ).format (new Object[] {getClassName ()});
            else
                return new MessageFormat (
                           NbBundle.getBundle (ExceptionBreakpoint.class).getString ("CTL_Exception_event_name_catched")
                       ).format (new Object[] {getClassName ()});
        else
            return new MessageFormat (
                       NbBundle.getBundle (ExceptionBreakpoint.class).getString ("CTL_Exception_event_name_uncatched")
                   ).format (new Object[] {getClassName ()});
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnException"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new ExceptionBreakpointPanel (this);
    }


    // interface Executor .....................................................................

    /**
    * Executes exception hit event.
    */
    public void exec (com.sun.jdi.event.Event event) {  //S ystem.out.println ("ExceptionBreakpoint.exec! " + this + " : " + event); // NOI18N
        if (event instanceof ClassPrepareEvent) {
            tryClass = ((ClassPrepareEvent) event).referenceType ();
            boolean v = set (tryClass);
            if (v && !getBreakpoint ().isValid ()) setValid (true);
            ((JPDADebugger) getDebugger ()).operator.resume ();
            return;
        }
        thread = ((ExceptionEvent) event).thread ();
        exception = new JPDAVariable (
                        (JPDADebugger) getDebugger (),
                        "Exception", // NOI18N
                        ((ExceptionEvent) event).exception (),
                        "" // NOI18N
                    );
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
    * Returns catchType property value.
    *
    * @return catchType property value.
    */
    public int getCatchType () {
        return catchType;
    }

    /**
    * Sets catchType property value.
    *
    * @param new value of catchType property.
    */
    public void setCatchType (int catchType) {
        if (catchType == this.catchType) return;
        if ( (catchType & (EXCEPTION_CATCHED | EXCEPTION_UNCATCHED)) == 0
           ) throw new IllegalArgumentException  ();
        int old = this.catchType;
        this.catchType = catchType;
        firePropertyChange (PROP_CATCH_TYPE, new Integer (old), new Integer (catchType));
    }

    /**
    * Sets exception breakpoint for given class.
    */
    public boolean set (ReferenceType rt) {
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;

        try {  //S ystem.out.println ("ExceptionBreakpoint.set for class " + rt); // NOI18N
            ExceptionRequest er = debugger.requestManager.createExceptionRequest (
                                      rt,
                                      (EXCEPTION_CATCHED & catchType) != 0,
                                      (EXCEPTION_UNCATCHED & catchType) != 0
                                  );
            er.setSuspendPolicy (ExceptionRequest.SUSPEND_ALL);
            debugger.operator.register (er, this);
            requestor.add (er);
            er.enable ();
            return true;
        } catch (VMDisconnectedException e) {
        } catch (VMMismatchException e) {
        } catch (Exception e) {  //S ystem.out.println ("ExceptionBreakpoint.set class exc. " + e); // NOI18N
            //e.p rintStackTrace ();
        }
        return false;
    }

    public String toString () {
        return "JPDAExceptionBreakpoint " + getClassName (); // NOI18N
    }
}

/*
* Log
*  12   Gandalf-post-FCS1.10.4.0    3/28/00  Daniel Prusa    
*  11   Gandalf   1.10        1/14/00  Daniel Prusa    NOI18N
*  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
*  9    Gandalf   1.8         1/4/00   Jan Jancura     Use trim () on user 
*       input.
*  8    Gandalf   1.7         11/9/99  Jan Jancura     printstacktrace commented
*       out
*  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
*  6    Gandalf   1.5         11/5/99  Jan Jancura     Empty values support
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         9/28/99  Jan Jancura     
*  3    Gandalf   1.2         9/15/99  Jan Jancura     
*  2    Gandalf   1.1         9/3/99   Jan Jancura     
*  1    Gandalf   1.0         9/2/99   Jan Jancura     
* $
*/
