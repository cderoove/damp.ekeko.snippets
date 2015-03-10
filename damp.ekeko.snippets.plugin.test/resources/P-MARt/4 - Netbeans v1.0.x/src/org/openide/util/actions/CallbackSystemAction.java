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

/* enabled is old; action perf prop is not public --jglick
*
* <P>
* <TABLE BORDER COLS=3 WIDTH=100%>
* <TR><TH WIDTH=15%>Property<TH WIDTH=15%>Property Type<TH>Description
* <TR><TD> Enabled  <TD> boolean   <TD> The explicite enabled/disabled
*                                       state of the action.
* <TR><TD> ActionPerformer  <TD> ActionPerformer  <TD> The class that performs the action
* </TABLE>
*/
/** Action that can have a performer of the action attached to it at any time,
* or changed.
* The action will be automatically disabled
* when it has no performer.
* <p>Also may be made sensitive to changes in window focus.
* @author   Ian Formanek, Jaroslav Tulach, Petr Hamernik
*/
public abstract class CallbackSystemAction extends CallableSystemAction {
    /** action performer */
    private static final String PROP_ACTION_PERFORMER = "actionPerformer"; // NOI18N
    /** focus listener for survive focus change */
    private static final String PROP_FOCUS_LISTENER = "focusListener"; // NOI18N

    static final long serialVersionUID =-6305817805474624653L;
    /** Initialize the action to have no performer.
    */
    protected void initialize () {
        super.initialize ();
        setEnabled (false);
    }


    /** Get the current action performer.
    * @return the current action performer, or <code>null</code> if there is currently no performer
    */
    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)getProperty (PROP_ACTION_PERFORMER);
    }

    /** Set the action performer.
    * The specified value can be <code>null</code>, which means that the action will have no performer
    * and is disabled. ({@link #isEnabled} will return <code>false</code> regardless its previous state.)
    * @param performer the new action performer or <code>null</code> to disable
    */
    public void setActionPerformer(ActionPerformer performer) {
        ActionPerformer oldValue = (ActionPerformer)putProperty (
                                       PROP_ACTION_PERFORMER, performer
                                   );
        setEnabled (performer != null);
    }

    /** Perform the action.
    * This default implementation calls the assigned action performer if it
    * exists, otherwise does nothing.
    */
    public void performAction() {
        ActionPerformer ap = getActionPerformer ();
        if (ap != null) ap.performAction (this);
    }

    /** Test whether the action will survive a change in focus.
    * By default, it will not.
    * @return <code>true</code> if the enabled state of the action survives focus changes
    */
    public boolean getSurviveFocusChange () {
        return getProperty (PROP_FOCUS_LISTENER) != null;
    }

    /** Set whether the action will survive a change in focus.
    * If <code>false</code>, then the action will be automatically
    * disabled (using {@link #setActionPerformer}) when the window
    * focus changes.
    *
    * @param b <code>true</code> to survive focus changes, <code>false</code> to be sensitive to them
    */
    public void setSurviveFocusChange (boolean b) {
        synchronized (getLock ()) {
            FocusL focusListener = (FocusL)getProperty (PROP_FOCUS_LISTENER);
            if (b) {
                // survive
                if (focusListener != null) {
                    // remove it from the list of top listeners
                    TopComponent.getRegistry ().removePropertyChangeListener (focusListener);
                    putProperty (PROP_FOCUS_LISTENER, null);
                }
            } else {
                // do not survive => register listener
                if (focusListener == null) {
                    // add it from the list of top listeners
                    focusListener = new FocusL (getClass ());
                    putProperty (PROP_FOCUS_LISTENER, focusListener);
                    TopComponent.getRegistry ().addPropertyChangeListener (focusListener);
                }
            }
        }
    }

    /** Listener for survive focus change */
    private static class FocusL implements PropertyChangeListener {
        /** The class we are working for */
        private Class clazz;

        /** @param class of the CallbackSystemAction
        */
        public FocusL (Class clazz) {
            this.clazz = clazz;
        }

        /** Called when a top window lost its focus.
        * @param ev event describing the situation
        */
        public void propertyChange (PropertyChangeEvent ev) {
            CallbackSystemAction a = (CallbackSystemAction)findObject (clazz);

            if (a != null && TopComponent.Registry.PROP_ACTIVATED.equals (ev.getPropertyName ())) {
                // deletes the performer
                a.setActionPerformer (null);
            }
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
 */
