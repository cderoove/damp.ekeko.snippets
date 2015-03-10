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

package org.netbeans.modules.debugger.debug;

import java.beans.PropertyEditor;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.swing.JComponent;

import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.ClassBreakpointEvent;
import org.netbeans.modules.debugger.support.CoreBreakpoint;

/**
* Abstract implementation of breakpoint event on exception.
*
* @author   Daniel Prusa
*/

public abstract class ExceptionBreakpointEvent extends ClassBreakpointEvent {

    //static final long serialVersionUID =1311192209820078715L;
    static final long serialVersionUID =8888892209820078715L;

    // Event implementation ....................................................................................

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        final ResourceBundle bundle = NbBundle.getBundle (ToolsDebugger.class);
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
                               setClassName ((String)val);
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
        return "Exception"; // NOI18N
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new ExceptionBreakpointPanel (this);
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
        return new MessageFormat (
                   NbBundle.getBundle (ExceptionBreakpoint.class).getString ("CTL_Exception_event_name")
               ).format (new Object[] {getClassName ()});
    }

    /**
    * Returns name of icon.
    */
    public String getIconBase () {
        return "/org/netbeans/modules/debugger/resources/breakpointOnException"; // NOI18N
    }

}
/*
* Log
*  2    Gandalf   1.1         1/13/00  Daniel Prusa    NOI18N
*  1    Gandalf   1.0         12/9/99  Daniel Prusa    
* $
*/
