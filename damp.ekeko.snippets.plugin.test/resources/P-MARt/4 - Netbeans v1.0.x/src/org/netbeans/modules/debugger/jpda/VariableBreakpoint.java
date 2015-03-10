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
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.List;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Location;
import com.sun.jdi.Value;
import com.sun.jdi.Field;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.InvalidLineNumberException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.event.AccessWatchpointEvent;
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
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.ClassBreakpointEvent;
import org.netbeans.modules.debugger.support.StopEvent;
import org.netbeans.modules.debugger.support.StopAction;
import org.netbeans.modules.debugger.support.PrintAction;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.jpda.util.*;


/**
* Implementation of breakpoint on variable accecc / modification.
*
* @author   Jan Jancura
*/
public class VariableBreakpoint extends ClassBreakpointEvent implements Executor, StopEvent {


    // static ....................................................................................

    /** Property name constant. */
    public static final String                  PROP_FIELD_NAME = "fieldName"; // NOI18N
    /** Property name constant. */
    public static final String                  PROP_FIELD = "field"; // NOI18N
    /** Property name constant. */
    public static final String                  PROP_TYPE = "type"; // NOI18N
    /** Property type value constant. */
    public static final int                     ACCESS_TYPE = 0;
    /** Property type value constant. */
    public static final int                     MODIFICATION_TYPE = 1;


    // variables ....................................................................................

    /** Thread which stops on this breakpoint. */
    private transient ThreadReference     thread;
    /** Stores all EventRequests produced by this Event. */
    private Requestor                     requestor;
    private transient Field               field;
    private String                        fieldName = ((AddBreakpointAction)
            SystemAction.get (AddBreakpointAction.class)).getCurrentFieldName ();
    private int                           type = MODIFICATION_TYPE;
    private transient AbstractVariable         value;
    private transient String              action;


    // Event impl ......................................................................................

    /**
    * Returns the new instance of Breakpoint.Event.
    */
    public CoreBreakpoint.Event getNewInstance () {
        return new VariableBreakpoint ();
    }

    /**
    * Sets breakpoint with specified properties.
    */
    public boolean set () {  //S ystem.out.println ("VariableBreakpoint.set TRY " + this + " in " + getDebugger () ); // NOI18N
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        if ((getClassName () == null) || (getClassName ().trim ().length () < 1)) return false;
        if (requestor == null) requestor = new Requestor (debugger.requestManager);
        try {
            requestor.removeRequests ();

            // For unoaded class
            ClassPrepareRequest cpr = debugger.requestManager.createClassPrepareRequest ();  //S ystem.out.println ("VariableBreakpoint.set CLASSES " + getClassName ()); // NOI18N
            cpr.addClassFilter (getClassName ());
            cpr.setSuspendPolicy (ClassPrepareRequest.SUSPEND_ALL);
            debugger.operator.register (cpr, this);
            requestor.add (cpr);
            cpr.enable ();

            List l = debugger.virtualMachine.classesByName (getClassName ());   //S ystem.out.println ("VariableBreakpoint.set CLASSES " + l.size ()); // NOI18N
            if (l.size () == 0) return false;

            // Known classes
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
    public void remove () {  //S ystem.out.println ("VariableBreakpoint.remove " + this); // NOI18N
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
                       PROP_FIELD_NAME,
                       String.class,
                       bundle.getString ("PROP_breakpoint_field_name"),
                       bundle.getString ("HINT_breakpoint_field_name")
                   ) {
                       public Object getValue () throws IllegalArgumentException {
                           return getFieldName ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setFieldName (((String) val).trim ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       PROP_TYPE,
                       Integer.TYPE,
                       bundle.getString ("PROP_breakpoint_type_name"),
                       bundle.getString ("HINT_breakpoint_type_name")
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
                                          ACCESS_TYPE,
                                          MODIFICATION_TYPE
                                      },
                                      new String[] {
                                          bundle.getString ("CTL_Breakpoint_type_access_value"),
                                          bundle.getString ("CTL_Breakpoint_type_modification_value")
                                      }
                                  );
                       }
                   }
               };
    }

    /**
    * Returns actions available specially for this version of event.
    */
    public CoreBreakpoint.Action[] getBreakpointActions () {
        CoreBreakpoint.Action[] myActions = new CoreBreakpoint.Action[] {
                                                new StopAction (),
                                                new VariablePrintAction (),
                                            };
        CoreBreakpoint.Action[] actions = new CoreBreakpoint.Action [super.getBreakpointActions ().length + myActions.length];
        System.arraycopy (super.getBreakpointActions (), 0, actions, 0, super.getBreakpointActions ().length);
        System.arraycopy (myActions, 0, actions, super.getBreakpointActions ().length, myActions.length);
        return actions;
    }

    /**
    * Returns lines to highlite in the editor.
    */
    public Line[] getLines () {
        return null;
    }

    /**
    * Returns name of type of this event.
    */
    public String getTypeName () {
        return "Variable"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Variable_event_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        if (type == MODIFICATION_TYPE)
            return new MessageFormat (
                       NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Variable_modification_event_name")
                   ).format (new Object[] {getClassName (), getFieldName ()});
        else
            return new MessageFormat (
                       NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Variable_access_event_name")
                   ).format (new Object[] {getClassName (), getFieldName ()});
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnVariable"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new VariableBreakpointPanel (this);
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
        return value;
    }


    // interface Executor .....................................................................

    /**
    * Executes breakpoint hit event.
    */
    public void exec (com.sun.jdi.event.Event event) {  //S ystem.out.println ("VariableBreakpoint.exec! " + this + " : " + event); // NOI18N
        if (event instanceof ClassPrepareEvent) {
            ReferenceType tryClass = ((ClassPrepareEvent) event).referenceType ();
            boolean v = set (tryClass);
            if (v && !getBreakpoint ().isValid ()) setValid (true);
            ((JPDADebugger) getDebugger ()).operator.resume ();
            return;
        }
        thread = ((WatchpointEvent) event).thread ();
        value = new JPDAVariable (
                    (JPDADebugger) getDebugger (),
                    "Current value", // NOI18N
                    ((WatchpointEvent) event).valueCurrent (),
                    "" // NOI18N
                );
        action = (event instanceof AccessWatchpointEvent) ?
                 NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Access") :
                 NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Modification");
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
    * Set name of class the field is in.
    */
    public void setClassName (String cn) {
        if (className == null) {
            if (cn == null) return;
        } else
            if ((cn != null) && cn.equals (className)) return;
        Object old = className;
        className = cn;
        field = null;
        firePropertyChange (CoreBreakpoint.PROP_CLASS_NAME, old, className);
    }

    /**
    * Returns name of field in the current class.
    */
    public String getFieldName () {
        return fieldName;
    }

    /**
    * Sets name of field in the current class.
    */
    public void setFieldName (String name) {
        if (fieldName == null) {
            if (name == null) return;
        } else
            if ((name != null) && name.equals (fieldName)) return;
        String old = fieldName;
        fieldName = name;
        field = null;
        firePropertyChange (PROP_FIELD_NAME, old, fieldName);
    }

    /**
    * Returns current field.
    */
    public Field getField () {
        return field;
    }

    /**
    * Sets field and className and fieldName.
    */
    public void setField (Field field) {
        if (this.field == field) return;
        Field old = field;
        this.field = field;
        firePropertyChange (PROP_FIELD, old, field);
        if (field == null) return;
        try {
            setClassName (field.declaringType ().name ());
            setFieldName (field.type ().name ());
        } catch (ObjectCollectedException e) {
        } catch (ClassNotLoadedException e) {
        } catch (VMDisconnectedException e) {
        }
    }

    /**
    * Returns current field.
    */
    public int getType () {
        return type;
    }

    /**
    * Sets field and className and fieldName.
    */
    public void setType (int type) {
        if (this.type == type) return;
        if ( (type != MODIFICATION_TYPE) &&
                (type != ACCESS_TYPE)
           ) throw new IllegalArgumentException  ();
        int old = type;
        this.type = type;
        firePropertyChange (PROP_TYPE, new Integer (old), new Integer (type));
    }

    /**
    * Sets breakpoint for given class.
    */
    public boolean set (ReferenceType clazz) {
        JPDADebugger debugger = (JPDADebugger) getDebugger ();
        if (debugger.virtualMachine == null) return false;
        try {
            Field f = null;
            boolean ok = false;
            try {
                f = clazz.fieldByName (getFieldName ());
            } catch (ClassNotPreparedException e) {      //S ystem.out.println ("VariableBreakpoint " + e); // NOI18N
            } catch (ObjectCollectedException e) {       //S ystem.out.println ("VariableBreakpoint " + e); // NOI18N
            }          //S ystem.out.println ("VariableBreakpoint.set classinstance: " + rt + " field: " + getFieldName () + " field: " + f); // NOI18N
            if (f == null) return false;
            if (type == ACCESS_TYPE) {
                AccessWatchpointRequest awr = debugger.requestManager.createAccessWatchpointRequest (f);
                awr.setSuspendPolicy (AccessWatchpointRequest.SUSPEND_ALL);
                awr.addClassFilter ("*"); // NOI18N
                debugger.operator.register (awr, this);
                requestor.add (awr);
                awr.enable ();              //S ystem.out.println ("VariableBreakpoint.OK! a"); // NOI18N
                ok = true;
            } else {
                ModificationWatchpointRequest mwr = debugger.requestManager.createModificationWatchpointRequest (f);
                mwr.setSuspendPolicy (ModificationWatchpointRequest.SUSPEND_ALL);
                mwr.addClassFilter ("*"); // NOI18N
                debugger.operator.register (mwr, this);
                requestor.add (mwr);
                mwr.enable ();              //S ystem.out.println ("VariableBreakpoint.OK! m"); // NOI18N
                ok = true;
            }
            return ok;
        } catch (VMDisconnectedException e) {
        }
        return false;
    }

    public String toString () {
        return "JPDAVariableBreakpoint " + getClassName () + "."  + getFieldName () + "."  + getType (); // NOI18N
    }


    // innerclasses ......................................................................................

    class VariablePrintAction extends PrintAction {

        /**
        * Creates the new Thrad Print action with default text.
        */
        VariablePrintAction () {
            super (
                NbBundle.getBundle (VariableBreakpoint.class).getString ("CTL_Variable_print_name")
            );
        }

        /**
        * Returns new initialized instance of Thrad Print action.
        */
        protected CoreBreakpoint.Action getNewInstance () {
            return new VariablePrintAction ();
        }

        /**
        * Resolving special tags:
        *   variableName    name variable
        *   action          name of action
        */
        protected void resolveTag (String tag, CoreBreakpoint.Event event, StringBuffer sb) {
            if (tag.equals ("variableName")) // NOI18N
                sb.append (((VariableBreakpoint) event).getFieldName ());
            else
                if (tag.equals ("action")) // NOI18N
                    sb.append (((VariableBreakpoint) event).action);
                else
                    super.resolveTag (tag, event, sb);
        }
    }
}

/*
* Log
*  10   Gandalf-post-FCS1.8.4.0     3/28/00  Daniel Prusa    
*  9    Gandalf   1.8         1/14/00  Daniel Prusa    NOI18N
*  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
*  7    Gandalf   1.6         1/4/00   Jan Jancura     Use trim () on user 
*       input.
*  6    Gandalf   1.5         12/10/99 Jan Jancura     Breakpoint on variable 
*       still do not work - bug in JPDA implementation.
*  5    Gandalf   1.4         11/8/99  Jan Jancura     Somma classes renamed
*  4    Gandalf   1.3         11/5/99  Jan Jancura     Empty values support
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         9/28/99  Jan Jancura     
*  1    Gandalf   1.0         9/28/99  Jan Jancura     
* $
*/
