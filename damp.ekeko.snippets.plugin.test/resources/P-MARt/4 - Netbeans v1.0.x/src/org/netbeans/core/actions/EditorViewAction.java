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

package org.netbeans.core.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;
import org.openide.text.EditorSupport;
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.WindowManagerImpl;

/** The action which shows last activated editor component.
*
* @author Dafe Simonek
*/
public final class EditorViewAction extends CallableSystemAction {

    static final long serialVersionUID =-6878655133790342844L;

    /** the listener for this action */
    private static final String PROP_LISTENER = "listener"; // NOI18N

    /* Initialize the listener. */
    protected void initialize () {
        super.initialize();
        ActivationListener l = new ActivationListener(getClass());
        putProperty(PROP_LISTENER, l);
    }

    /* Adds listener to registry. */
    protected void addNotify () {
        // initializes the listener
        ActivationListener l = (ActivationListener)getProperty(PROP_LISTENER);
        l.setActive(true);
    }

    /* Removes listener to changes of activated nodes */
    protected void removeNotify () {
        ActivationListener l = (ActivationListener)getProperty(PROP_LISTENER);
        l.setActive(false);
    }

    /** Actually performs the action of showing last activated
    * editor component. */
    public void performAction () {
        WindowManagerImpl wm = (WindowManagerImpl)TopManager.getDefault().
                               getWindowManager();
        wm.reactivateComponent(EditorSupport.Editor.class);
    }

    public String getName() {
        return NbBundle.getBundle(EditorViewAction.class).getString("EditorView");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (EditorViewAction.class);
    }

    /** Resource name for the action's icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/frames/editor.gif"; // NOI18N
    }

    /** Node listener to check whether the action is enabled or not
    */
    private static final class ActivationListener implements PropertyChangeListener {
        /** the class we work with */
        private Class clazz;
        /** listener */
        private PropertyChangeListener listener;

        /** Constructor that checks the current state
        */
        public ActivationListener (Class clazz) {
            this.clazz = clazz;
        }

        /** Activates/passivates the listener. */
        void setActive (boolean active) {
            if (listener == null && active) {
                listener = WeakListener.propertyChange(this, TopComponent.getRegistry());
                TopComponent.getRegistry().addPropertyChangeListener(listener);
                checkEnabled(action());
                return;
            }

            if (listener != null && !active) {
                TopComponent.getRegistry().removePropertyChangeListener(listener);
                listener = null;
            }
        }

        /** Is the listener active?
        */
        boolean isActive () {
            return listener != null;
        }

        /** Property change listener. */
        public void propertyChange (PropertyChangeEvent ev) {
            EditorViewAction a = action();
            if (TopComponent.Registry.PROP_ACTIVATED.equals(ev.getPropertyName())) {
                // activated component changed, update action status
                checkEnabled(action());
                return;
            }
        }

        /** Obtains the action.
        * @return the node action we belong to or null if there is none
        */
        private EditorViewAction action () {
            return (EditorViewAction)findObject(clazz);
        }

        /** Checks the state of the action.
        */
        void checkEnabled (EditorViewAction a) {
            // no action no work
            if (a == null) return;
            // set state
            WindowManagerImpl wm = (WindowManagerImpl)TopManager.getDefault().
                                   getWindowManager();
            a.setEnabled(wm.lastActivated(EditorSupport.Editor.class) != null);
        }
    } // end of ActivationListener inner class

}

/*
* Log
*  13   src-jtulach1.12        1/12/00  Ales Novak      i18n
*  12   src-jtulach1.11        1/5/00   David Simonek   debug prints removed
*  11   src-jtulach1.10        1/4/00   David Simonek   totally rewritten to 
*       liasten to top component activation changes
*  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    src-jtulach1.8         8/19/99  David Simonek   unfinaled parameters
*  8    src-jtulach1.7         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  7    src-jtulach1.6         7/11/99  David Simonek   window system change...
*  6    src-jtulach1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  3    src-jtulach1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  2    src-jtulach1.1         3/24/99  Ian Formanek    WorkspazitiveAction 
*       renamed to WorkspaceSensitiveAction
*  1    src-jtulach1.0         3/12/99  David Simonek   
* $
*/
