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

package org.netbeans.modules.debugger.support;

import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.JComponent;

import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;


/**
* Abstract implementation of breakpoint event on method.
*
* @author   Jan Jancura
*/
public abstract class MethodBreakpointEvent extends ClassBreakpointEvent {

    // static ....................................................................................

    static final long serialVersionUID =1311192209820078715L;


    // variables ....................................................................................

    private String methodName = ((AddBreakpointAction) SystemAction.
                                 get (AddBreakpointAction.class)).getCurrentMethodName ();


    // Event implementation ....................................................................................

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        ResourceBundle bundle = NbBundle.getBundle (CoreBreakpoint.class);
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
                   }
               };
    }

    /**
    * Returns name of type of this event.
    */
    public String getTypeName () {
        return "Method"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (MethodBreakpointEvent.class).getString ("CTL_Method_event_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        return new MessageFormat (
                   NbBundle.getBundle (MethodBreakpointEvent.class).getString ("CTL_Method_event_name")
               ).format (new Object[] {getClassName (), getMethodName ()});
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnMethod"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new MethodBreakpointPanel (this);
    }


    // properties ........................................................................................

    /**
    * Get name of method to stop on.
    */
    public String getMethodName () {
        return methodName;
    }

    /**
    * Set name of method to stop on.
    */
    public void setMethodName (String mn) {
        String old = methodName;
        methodName = mn;
        firePropertyChange (CoreBreakpoint.PROP_LINE_NUMBER, old, mn);
    }
}

/*
* Log
*  13   Gandalf-post-FCS1.11.3.0    3/28/00  Daniel Prusa    
*  12   Gandalf   1.11        1/13/00  Daniel Prusa    NOI18N
*  11   Gandalf   1.10        1/4/00   Jan Jancura     Use trim () on user 
*       input.
*  10   Gandalf   1.9         11/8/99  Jan Jancura     Somma classes renamed
*  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         9/28/99  Jan Jancura     
*  7    Gandalf   1.6         8/18/99  Jan Jancura     Localization & Current 
*       thread & Current session
*  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  5    Gandalf   1.4         7/14/99  Jan Jancura     
*  4    Gandalf   1.3         7/13/99  Jan Jancura     
*  3    Gandalf   1.2         7/2/99   Jan Jancura     Session debugging support
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
