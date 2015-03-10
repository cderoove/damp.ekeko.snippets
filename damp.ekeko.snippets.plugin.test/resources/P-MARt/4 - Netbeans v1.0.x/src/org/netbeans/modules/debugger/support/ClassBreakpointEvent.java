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
import javax.swing.JComponent;

import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;


/**
* Abstract implementation of breakpoint event on class.
*
* @author   Jan Jancura
*/
public abstract class ClassBreakpointEvent extends CoreBreakpoint.Event {

    static final long serialVersionUID = -4544769666886838818L;

    protected String className = ((AddBreakpointAction) SystemAction.
                                  get (AddBreakpointAction.class)).getCurrentClassName ();


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
                               setClassName (((String) val).trim ());
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
        return "Class"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (ClassBreakpointEvent.class).getString ("CTL_Class_event");
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return null;
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new ClassBreakpointPanel (this);
    }


    // properties ........................................................................................

    /**
    * Get name of class to stop on.
    */
    public String getClassName () {
        return className;
    }

    /**
    * Set name of class to stop on.
    */
    public void setClassName (String cn) {
        Object old = className;
        className = cn;
        firePropertyChange (CoreBreakpoint.PROP_CLASS_NAME, old, className);
    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.8.3.1     4/11/00  Daniel Prusa    bugfix for 
 *       deserialization
 *  10   Gandalf-post-FCS1.8.3.0     3/28/00  Daniel Prusa    
 *  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         1/4/00   Jan Jancura     Use trim () on user 
 *       input.
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  4    Gandalf   1.3         7/13/99  Jan Jancura     
 *  3    Gandalf   1.2         6/10/99  Jan Jancura     
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
