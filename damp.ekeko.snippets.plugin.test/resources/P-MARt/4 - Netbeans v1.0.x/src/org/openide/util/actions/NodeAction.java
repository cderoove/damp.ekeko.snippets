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

import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** An action which can listen to the activated node selection.
* This means that the set of nodes active in a window
* may change the enabled state of the action according to {@link #enable}.
*
* @author   Jan Jancura, Ian Formanek, Jaroslav Tulach
*/
public abstract class NodeAction extends CallableSystemAction {
    static final long serialVersionUID =-5672895970450115226L;

    /** the listener for this action */
    private static final String PROP_LISTENER = "listener"; // NOI18N

    /* Initialize the listener.
    */
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
            performAction (new Node[] { (Node) s });
        } else {
            performAction ();
        }
    }

    /** Performs the action.
    * In the default implementation, calls {@link #performAction(Node[])}.
    * In general you need not override this.
    */
    public void performAction() {
        performAction (getActivatedNodes ());
    }

    /** Get the currently activated nodes.
    * @return the nodes (may be empty but not <code>null</code>)
    */
    public final Node[] getActivatedNodes () {
        return TopComponent.getRegistry ().getActivatedNodes ();
    }

    /** Specify the behavior of the action when a window with no
    * activated nodes is selected.
    * If the action should then be disabled,
    * return <code>false</code> here; if the action should stay in the previous state,
    * return <code>true</code>.
    * <p>Note that {@link #getActivatedNodes} and {@link #performAction} are still
    * passed the set of selected nodes from the old window, if you keep this feature on.
    * This is useful, e.g., for an action like Compilation which should remain active
    * even if the user switches to a window like the Output Window that has no associated nodes;
    * then running the action will still use the last selection from e.g. an Explorer window
    * or the Editor, if there was one to begin with.
    *
    * @return <code>true</code> in the default implementation
    */
    protected boolean surviveFocusChange() {
        return true;
    }

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected abstract void performAction (Node[] activatedNodes);

    /**
    * Test whether the action should be enabled based
    * on the currently activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    * @return <code>true</code> to be enabled, <code>false</code> to be disabled
    */
    protected abstract boolean enable (Node[] activatedNodes);


    /** Node listener to check whether the action is enabled or not
    */
    private static final class NodesL
        implements PropertyChangeListener, Runnable {
        /** the class we work with */
        private Class clazz;
        /** active or not */
        private boolean active;
        /** a task to delay a while updating of action state */
        private RequestProcessor.Task task;

        /** delay before updating */
        private static final int DELAY = 150;

        /** Constructor that checks the current state
        */
        public NodesL (Class clazz) {
            this.clazz = clazz;
            task = RequestProcessor.createRequest(this);
            task.setPriority (Thread.MAX_PRIORITY - 1);
        }

        /** Activates/passivates the listener.
        */
        void setActive (boolean active) {
            if (active != this.active) {
                this.active = active;
                if (active) {
                    TopComponent.Registry r = TopComponent.getRegistry();
                    r.addPropertyChangeListener (this);
                    checkEnabled (action ());
                } else {
                    TopComponent.getRegistry().removePropertyChangeListener(this);
                }
            }
        }

        /** Is the listener active?
        */
        boolean isActive () {
            return active;
        }

        /** Property change listener.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            NodeAction a = action ();
            if (a.surviveFocusChange () && TopComponent.Registry.PROP_ACTIVATED_NODES.equals (ev.getPropertyName ())) {
                task.schedule (DELAY);
                return;
            }
            if (!a.surviveFocusChange () && TopComponent.Registry.PROP_CURRENT_NODES.equals (ev.getPropertyName ())) {
                task.schedule (DELAY);
                return;
            }
        }

        /** Updates the state of the action.
        */
        public void run() {
            NodeAction a = action ();
            checkEnabled (a);
        }

        /** test on deactivating nodes *
        public void nodesDeactivated (NodesEvent event) {
          Node[] n = event.getActivatedNodes();

          NodeAction a = action ();
          if (a == null) return;

          if (n != null) {
            a.setEnabled (a.enable (nodes = n));
          } else {
            if (!a.surviveFocusChange ()) {
              a.setEnabled (false);
            }
          }
          }
          */

        /** Getter for activated nodes
        * @return array
        */
        public Node[] getActivatedNodes (boolean survive) {
            if (survive) {
                return TopComponent.getRegistry ().getActivatedNodes ();
            } else {
                return TopComponent.getRegistry ().getCurrentNodes ();
            }
        }

        /** Obtains the action.
        * @return the node action we belong to or null if there is none
        */
        private NodeAction action () {
            return (NodeAction)findObject (clazz);
        }

        /** Checks the state of the action.
        */
        void checkEnabled (NodeAction a) {
            // no action no work
            if (a == null) return;

            Node[] n = getActivatedNodes (a.surviveFocusChange ());
            boolean b = n != null && a.enable (n);
            a.setEnabled (b);
        }
    }
}

/*
 * Log
 *  3    Tuborg    1.2         07/29/98 Jaroslav Tulach Removed internal field
 *                                                      because of quick
 *                                                      initialization.
 *
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach minimal class
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    surviveFocusChange
 *  0    Tuborg    0.15        --/--/98 Jan Jancura     getActivatedNodes method added.
 */
