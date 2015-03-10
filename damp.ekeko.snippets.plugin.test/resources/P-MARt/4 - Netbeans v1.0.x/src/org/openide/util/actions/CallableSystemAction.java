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

import org.openide.awt.*;

/** An action which may be called programmatically.
* Typically a presenter will call its {@link #performAction} method,
* which must be implemented.
* <p>Provides default presenters using the {@link Actions} utility class.
*
* @author   Ian Formanek, Jaroslav Tulach, Jan Jancura, Petr Hamernik
*/
public abstract class CallableSystemAction extends SystemAction
    implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
    /** serialVersionUID */
    static final long serialVersionUID = 2339794599168944156L;

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return new Actions.MenuItem(this, true);
    }

    /* Returns a JMenuItem that presents the Action, that implements this
    * interface, in a Popup Menu.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getPopupPresenter() {
        return new Actions.MenuItem(this, false);
    }

    /* Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return new Actions.ToolbarButton (this);
    }

    /** Actually perform the action.
    * This is the method which should be called programmatically.
    * Presenters in {@link Actions} use this.
    */
    public abstract void performAction();

    /* Implementation of method of javax.swing.Action interface.
    * Delegates the execution to performAction method.
    *
    * @param ev ignored
    */
    public void actionPerformed (java.awt.event.ActionEvent ev) {
        performAction ();
    }
}

/*
 * Log
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/26/99  Ian Formanek    Actions cleanup
 *  6    Gandalf   1.5         3/26/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/1/99   Jaroslav Tulach Changed presenters.
 *  4    Gandalf   1.3         2/27/99  Jaroslav Tulach Shortcut changed to 
 *       Keymap
 *  3    Gandalf   1.2         2/11/99  Jaroslav Tulach SystemAction is 
 *       javax.swing.Action
 *  2    Gandalf   1.1         1/20/99  David Peroutka  
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 anonymous       implements the presenters interfaces
 *  0    Tuborg    0.21        --/--/98 anonymous       changed to reflect moving of presenter classes
 *  0    Tuborg    0.30        --/--/98 anonymous       added getter and setter for icon
 *  0    Tuborg    0.31        --/--/98 anonymous       added setter for moving in IndexedPanContext
 *  0    Tuborg    0.32        --/--/98 Jan Jancura     removed support for Indexed bla bla
 *  0    Tuborg    0.33        --/--/98 Petr Hamernik   bug fix
 *  0    Tuborg    0.35        --/--/98 Jaroslav Tulach minimal class
 *  0    Tuborg    0.36        --/--/98 Jan Formanek    default is empty icon
 *  0    Tuborg    0.37        --/--/98 Jan Formanek    added getPopupPresenter, implements PopupPresenter
 *  0    Tuborg    0.38        --/--/98 Jan Formanek    getPopupPresenter returns MenuItem without icon
 */
