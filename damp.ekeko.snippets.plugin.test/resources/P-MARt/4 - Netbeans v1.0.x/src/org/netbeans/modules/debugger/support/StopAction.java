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
import javax.swing.SwingUtilities;
import javax.swing.JComponent;

import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
* This actions stops debugging.
* 
* @author   Jan Jancura
*/
public class StopAction extends CoreBreakpoint.Action {

    static final long serialVersionUID =-1116671340606964700L;
    /** Property name constant. */
    public static final String PROP_STOP = "stop"; // NOI18N

    /** Property variable. */
    private boolean stop = true;


    // Action implementation ...................................................................

    /**
    * Returns new initialized instance of action.
    */
    protected CoreBreakpoint.Action getNewInstance () {
        return new StopAction ();
    }

    /**
    * Returns specific properties of this event - stop property.
    */
    public Node.Property[] getProperties () {
        ResourceBundle bundle = NbBundle.getBundle (StopAction.class);
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (
                       StopAction.PROP_STOP,
                       Boolean.TYPE,
                       bundle.getString ("PROP_stop"),
                       bundle.getString ("HINT_stop")
                   ) {
                       public Object getValue () {
                           return new Boolean (getStop ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setStop (((Boolean)val).booleanValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   }
               };
    }

    /**
    * This method is called for each action when some breakpoint event is reached.
    */
    protected void perform (final CoreBreakpoint.Event event) {
        ((StopEvent) event).stop (stop);
    }

    /**
    * Returns customizer visuall component.
    */
    public JComponent getCustomizer () {
        return new StopActionPanel (this);
    }

    // properties ........................................................................................

    /**
    * Gets value of stop property.
    */
    public boolean getStop () {
        return stop;
    }

    /**
    * Sets value of stop property.
    */
    public void setStop (boolean s) {
        if (s == stop) return;
        stop = s;
        firePropertyChange (PROP_STOP, new Boolean (!s), new Boolean (s));
    }
}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Daniel Prusa    NOI18N
*  3    Gandalf   1.2         11/8/99  Jan Jancura     Somma classes renamed
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/2/99   Jan Jancura     
* $
*/
