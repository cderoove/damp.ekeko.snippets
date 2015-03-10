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

package org.openide.util.actions;

import java.beans.*;

import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.util.WeakListener;

/** Base class for all project sensitive actions. Attaches listener
* to changes of project desktop (TopManager.PROP_PLACES) and also to 
* cookie changes on current project desktop. If either one is changed,
* the enable method is called to enable/disable the action. When 
* enabled and invoked the method performAction (Node) is called.
*
* @author Jaroslav Tulach
*/
public abstract class ProjectSensitiveAction extends CallableSystemAction {
    /** the listener for this action */
    private static final String PROP_LISTENER = "listener"; // NOI18N

    /* Initialize the listener.
    */
    static final long serialVersionUID =1813729754448097488L;
    protected void initialize () {
        super.initialize ();
        NodesL l = new NodesL (getClass ());
        putProperty(PROP_LISTENER, l);
    }

    /* Adds listener to registry.
    */
    protected void addNotify () {
        // initializes the listener
        NodesL l = (NodesL)getProperty (PROP_LISTENER);
        l.setActive (true);
    }

    /* Removes listener to changes of activated nodes */
    protected void removeNotify () {
        NodesL l = (NodesL)getProperty (PROP_LISTENER);
        l.setActive (false);
    }

    /** Test for enablement based on {@link #enable}.
    * You probably ought not override this, except possibly
    * to call the super method and add an additional check.
    * @return <code>true</code> to enable
    */
    public boolean isEnabled () {
        NodesL l = (NodesL)getProperty (PROP_LISTENER);
        if (!l.isActive ()) {
            l.checkEnabled (this);
        }
        return super.isEnabled ();
    }

    /* Implementation of method of javax.swing.Action interface.
    * Checks if the source of event is node and if so, it executes
    * performAction (thatNode). Otherwise simply calls
    * performAction ().
    *
    * @param ev event where to find source
    */
    public void actionPerformed (java.awt.event.ActionEvent ev) {
        Object s = ev == null ? null : ev.getSource ();
        if (s instanceof Node) {
            performAction ((Node)s);
        } else {
            performAction (projectDesktop ());
        }
    }

    /** Performs the action.
    * In the default implementation, calls {@link #performAction(Node[])}.
    * In general you need not override this.
    */
    public void performAction() {
        performAction (projectDesktop ());
    }

    /** Performs the action on the current project desktop node.
    *
    * @param project desktop node
    */
    protected abstract void performAction (Node project);

    /** Performs the action on the current project desktop node.
    *
    * @param project desktop node
    * @return <code>true</code> to be enabled, <code>false</code> to be disabled
    */
    protected abstract boolean enable (Node project);


    /** Project desktop
    */
    static Node projectDesktop () {
        return TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
    }

    /** Node listener to check whether the action is enabled or not
    */
    private static final class NodesL extends NodeAdapter {
        /** the class we work with */
        private Class clazz;
        /** listener */
        private NodeListener listener;

        /** Constructor that checks the current state
        */
        public NodesL (Class clazz) {
            this.clazz = clazz;
        }

        /** Activates/passivates the listener.
        */
        void setActive (boolean active) {
            if (listener == null && active) {
                listener = WeakListener.node (this, TopManager.getDefault ());
                TopManager.getDefault ().addPropertyChangeListener (listener);
                projectDesktop ().addNodeListener (listener);
                checkEnabled (action ());
                return;
            }

            if (listener != null && !active) {
                TopManager.getDefault ().removePropertyChangeListener (listener);
                projectDesktop ().removeNodeListener (listener);
                listener = null;
            }
        }

        /** Is the listener active?
        */
        boolean isActive () {
            return listener != null;
        }

        /** Property change listener.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            ProjectSensitiveAction a = action ();

            if (TopManager.PROP_PLACES.equals (ev.getPropertyName ())) {
                // changed project => release listeners
                setActive (false);
                // attach listeners
                setActive (true);
                return;
            }

            if (Node.PROP_COOKIE.equals (ev.getPropertyName ())) {
                checkEnabled (action ());
                return;
            }
        }

        /** Obtains the action.
        * @return the node action we belong to or null if there is none
        */
        private ProjectSensitiveAction action () {
            return (ProjectSensitiveAction)findObject (clazz);
        }

        /** Checks the state of the action.
        */
        void checkEnabled (ProjectSensitiveAction a) {
            // no action no work
            if (a == null) return;

            a.setEnabled (a.enable (projectDesktop ()));
        }
    }

}

/*
* Log
*  6    Gandalf   1.5         1/12/00  Pavel Buzek     I18N
*  5    Gandalf   1.4         11/29/99 Patrik Knakal   
*  4    Gandalf   1.3         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  1    Gandalf   1.0         8/1/99   Jaroslav Tulach 
* $
*/