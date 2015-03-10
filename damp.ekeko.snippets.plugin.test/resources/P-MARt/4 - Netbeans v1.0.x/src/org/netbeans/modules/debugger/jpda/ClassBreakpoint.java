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
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
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

import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ClassBreakpoint extends CoreBreakpoint.Event implements Executor, StopEvent {

    // static ....................................................................................

    /** Property name constant. */
    public static final String            PROP_TYPE = "type"; // NOI18N
    /** Property name constant. */
    public static final String            PROP_CLASS_FILTER = "classFilter"; // NOI18N
    /** Property name constant. */
    public static final String            PROP_EXCLUSION_FILTER = "exclusionFilter"; // NOI18N

    /** Property type constant. */
    public static final int               TYPE_PREPARE = 1;
    /** Property type constant. */
    public static final int               TYPE_UNLOAD = 2;


    // variables ....................................................................................

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private int                           type = TYPE_PREPARE;
    private boolean                       exclusionFilter = false;
    private String                        classFilter = ((AddBreakpointAction)
            SystemAction.get (AddBreakpointAction.class)).getCurrentClassName ();
    private transient String              action;
    private transient String              className;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new ClassBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () { //S ystem.out.println ("ClassBreakpoint.set TRY " + this + " in " + getDebugger () ); // NOI18N
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if ((getClassFilter () == null) || (getClassFilter ().trim ().length () < 1)) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            requestor.removeRequests ();

            if ((type & TYPE_PREPARE) != 0) {
                ClassPrepareRequest cpr = debugger.requestManager.createClassPrepareRequest ();
                cpr.setSuspendPolicy (ClassPrepareRequest.SUSPEND_ALL);
                if (exclusionFilter)
                    cpr.addClassExclusionFilter (getClassFilter ());
                else
                    cpr.addClassFilter (getClassFilter ());
                debugger.operator.register (cpr, this);
                requestor.add (cpr);
                cpr.enable (); //S ystem.out.println ("ClassBreakpoint.set PREPARE " + exclusionFilter); // NOI18N
            }
            if ((type & TYPE_UNLOAD) != 0) {
                ClassUnloadRequest cur = debugger.requestManager.createClassUnloadRequest ();
                cur.setSuspendPolicy (ClassUnloadRequest.SUSPEND_ALL);
                if (exclusionFilter)
                    cur.addClassExclusionFilter (getClassFilter ());
                else
                    cur.addClassFilter (getClassFilter ());
                debugger.operator.register (cur, this);
                requestor.add (cur);
                cur.enable (); //S ystem.out.println ("ClassBreakpoint.set UNLOAD " + exclusionFilter); // NOI18N
            }
        } catch (VMDisconnectedException e) {
        }
        return true;
    }

    /**
    * Removes breakpoint.
    */
    public void remove () {  //S ystem.out.println ("ClassBreakpoint.remove " + this); // NOI18N
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
                       PROP_CLASS_FILTER,
                       String.class,
                       bundle.getString ("PROP_class_filter"),
                       bundle.getString ("HINT_class_filter")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return getClassFilter ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           setClassFilter (((String) val).trim ());
                       }
                   },
                   new PropertySupport.ReadWrite (
                       PROP_EXCLUSION_FILTER,
                       Boolean.TYPE,
                       bundle.getString ("PROP_class_exclusion_filter"),
                       bundle.getString ("HINT_class_exclusion_filter")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return new Boolean (getExclusionFilter ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setExclusionFilter (((Boolean) val).booleanValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       PROP_TYPE,
                       Integer.TYPE,
                       bundle.getString ("PROP_class_type"),
                       bundle.getString ("HINT_class_type")
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
                                          TYPE_PREPARE,
                                          TYPE_UNLOAD,
                                          TYPE_PREPARE | TYPE_UNLOAD
                                      },
                                      new String[] {
                                          bundle.getString ("CTL_Breakpoint_type_class_prepare"),
                                          bundle.getString ("CTL_Breakpoint_type_class_unload"),
                                          bundle.getString ("CTL_Breakpoint_type_class_both")
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
        return "Class"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (ClassBreakpoint.class).getString ("CTL_Class_event_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        if (type == TYPE_PREPARE)
            return new MessageFormat (
                       NbBundle.getBundle (ClassBreakpoint.class).getString ("CTL_Class_prepare_event_name")
                   ).format (new Object[] {getClassFilter ()});
        else
            if (type == TYPE_UNLOAD)
                return new MessageFormat (
                           NbBundle.getBundle (ClassBreakpoint.class).getString ("CTL_Class_unload_event_name")
                       ).format (new Object[] {getClassFilter ()});
        return new MessageFormat (
                   NbBundle.getBundle (ClassBreakpoint.class).getString ("CTL_Class_event_name")
               ).format (new Object[] {getClassFilter ()});
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnClass"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new ClassBreakpointPanel (this);
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new ClassPrintAction (),
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
    public void exec (com.sun.jdi.event.Event event) {  //S ystem.out.println ("ClassBreakpoint.exec! " + this + " : " + event); // NOI18N
        if (event instanceof ClassPrepareEvent) {
            thread = ((ClassPrepareEvent) event).thread ();
            action = NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Prepared");
            className = ((ClassPrepareEvent) event).referenceType ().name ();
        } else {
            action = NbBundle.getBundle (ThreadBreakpoint.class).getString ("CTL_Unload");
            className = ((ClassUnloadEvent) event).className ();
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
        if ((type & (TYPE_PREPARE | TYPE_UNLOAD)) == 0)
            throw new IllegalArgumentException  ();
        int old = type;
        this.type = type;
        firePropertyChange (PROP_TYPE, new Integer (old), new Integer (type));
    }

    /**
    * Setter of property exclusion filter.
    */
    public boolean getExclusionFilter () {
        return exclusionFilter;
    }

    /**
    * Getter of property exclusion filter.
    */
    public void setExclusionFilter (boolean exclusionFilter) {
        if (exclusionFilter == this.exclusionFilter) return;
        boolean old = exclusionFilter;
        this.exclusionFilter = exclusionFilter;
        firePropertyChange (PROP_EXCLUSION_FILTER, new Boolean (old), new Boolean (exclusionFilter));
    }

    /**
    * Returns class filter.
    */
    public String getClassFilter () {
        return classFilter;
    }

    /**
    * Sets class filter.
    */
    public void setClassFilter (String classFilter) {
        if (this.classFilter == null) {
            if (classFilter == null) return;
        } else
            if ((classFilter != null) && classFilter.equals (this.classFilter)) return;
        String old = classFilter;
        this.classFilter = classFilter;
        firePropertyChange (PROP_CLASS_FILTER, old, classFilter);
    }

    public String toString () {
        return "JPDAClassBreakpoint " + getClassFilter (); // NOI18N
    }


    // innerclasses ......................................................................................

    class ClassPrintAction extends PrintAction {

        /**
        * Creates the new Thrad Print action with default text.
        */
        ClassPrintAction () {
            super (
                NbBundle.getBundle (ClassBreakpoint.class).getString ("CTL_Class_print_name")
            );
        }

        /**
        * Returns new initialized instance of Thrad Print action.
        */
        protected CoreBreakpoint.Action getNewInstance () {
            return new ClassPrintAction ();
        }

        /**
        * Resolving special tags:
        *   className       name of the class the breakpoint is reached in
        *   action          name of action
        */
        protected void resolveTag (String tag, CoreBreakpoint.Event event, StringBuffer sb) {
            if (tag.equals ("className")) // NOI18N
                sb.append (((ClassBreakpoint) event).className);
            else
                if (tag.equals ("action")) // NOI18N
                    sb.append (((ClassBreakpoint) event).action);
                else
                    super.resolveTag (tag, event, sb);
        }
    }
}

/*
* Log
*  9    Gandalf-post-FCS1.7.4.0     3/28/00  Daniel Prusa    
*  8    Gandalf   1.7         1/14/00  Daniel Prusa    NOI18N
*  7    Gandalf   1.6         1/13/00  Daniel Prusa    NOI18N
*  6    Gandalf   1.5         1/4/00   Jan Jancura     Use trim () on user 
*       input.
*  5    Gandalf   1.4         11/8/99  Jan Jancura     Somma classes renamed
*  4    Gandalf   1.3         11/5/99  Jan Jancura     Empty values support
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/28/99  Jan Jancura     
*  1    Gandalf   1.0         9/28/99  Jan Jancura     
* $
*/
