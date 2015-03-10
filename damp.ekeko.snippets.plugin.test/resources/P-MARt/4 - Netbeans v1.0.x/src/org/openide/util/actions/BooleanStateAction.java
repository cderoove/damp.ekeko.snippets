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

import java.net.URL;
import javax.swing.ImageIcon;

import org.openide.awt.*;

/** An action that can be toggled on or off.
* The actual "performing" of the action is the toggle itself, so
* this action should be used by listening to the {@link #PROP_BOOLEAN_STATE} property.
* <p>The default value of the state is <code>true</code> (on).
*
*
* @author   Ian Formanek, Petr Hamernik
*/
public abstract class BooleanStateAction extends SystemAction
    implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 6394800019181426199L;

    /** Name of property hold the state of the action. */
    public static final String PROP_BOOLEAN_STATE = "booleanState"; // NOI18N

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return new Actions.CheckboxMenuItem(this, true);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * The default implmentation returns the same JMenuItem as the getMenuPresenter.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return new Actions.CheckboxMenuItem(this, false);
    }

    /* Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return new Actions.ToolbarToggleButton(this);
    }

    /** Get the current state.
    * @return <code>true</code> if on
    */
    public boolean getBooleanState() {
        return getProperty (PROP_BOOLEAN_STATE).equals (Boolean.TRUE);
    }

    /** Set the current state.
    * Fires a change event, which should be used to affect other components when
    * its state is toggled.
    * @param value <code>true</code> to turn on, <code>false</code> to turn off
    */
    public void setBooleanState(boolean value) {
        Boolean newValue = new Boolean (value);
        Boolean oldValue = (Boolean)putProperty (PROP_BOOLEAN_STATE, newValue);

        firePropertyChange(PROP_BOOLEAN_STATE, oldValue, newValue);
    }

    /* Initializes its own properties (and let superclass initialize
    * too).
    */
    protected void initialize () {
        putProperty(PROP_BOOLEAN_STATE, Boolean.TRUE);
        super.initialize();
    }

    /* Implementation of method of javax.swing.Action interface.
    * Changes the boolean state.
    *
    * @param ev ignored
    */
    public void actionPerformed (java.awt.event.ActionEvent ev) {
        setBooleanState (!getBooleanState ());
    }
}

/*
 * Log
 *  4    Tuborg    1.3         07/29/98 Petr Hamernik   mnemonics changes -
 *                                                      bugfix337
 *  3    Tuborg    1.2         07/29/98 Jaroslav Tulach Removed internal field
 *                                                      because of quick
 *                                                      initialization.
 *
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 anonymous       extends directly the SystemAction, changed semantics od the setBooleanState
 *  0    Tuborg    0.21        --/--/98 anonymous       changed to reflect moving of presenter classes
 *  0    Tuborg    0.22        --/--/98 Petr Hamernik   bug fix
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    added getPopupPresenter, implements PopupPresenter
 *  0    Tuborg    0.25        --/--/98 Jan Formanek    added icon
 */
