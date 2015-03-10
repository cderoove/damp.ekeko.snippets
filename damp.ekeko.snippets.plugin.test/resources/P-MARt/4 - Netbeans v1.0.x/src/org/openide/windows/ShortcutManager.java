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

package org.openide.windows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.openide.TopManager;
import org.openide.windows.Workspace;

/** Manager that takes care about selected window, assignes to it
* key strokes and when such a key stroke is pressed it starts the appropriate
* action by calling <CODE>distributeKey</CODE> method of current ShortcutContext.
* <P>
* The manager must keep reference to the current shortcut context and change
* it following directions returned from the <CODE>distributeKey</CODE> method.
* When <CODE>distributeKey</CODE> returns <CODE>null</CODE> the manager
* should set the root context as the current.
*
* @author Jaroslav Tulach, Petr Hamernik
*/
class ShortcutManager extends Object {
    /** base context */
    private static Keymap root = TopManager.getDefault ().getGlobalKeymap ();

    /**
    * Process keyStroke action. Current context will be set to return value
    * of distributeKey(key) method.
    * @param key Key to be processed.
    * @param ev an event to send
    * @return true if the key has been processed and false otherwise
    */
    static boolean processKeyStroke(KeyStroke key, ActionEvent ev) {
        Action a = root.getAction (key);
        //System.err.println ("Running in SM");
        if (a != null && a.isEnabled ()) {
            //System.err.println ("Performing action with event: " + ev);
            //System.err.println ("\tsource: " + ev.getSource ());
            //System.err.println ("\tID: " + ev.getID ());
            //System.err.println ("\tcommand: " + ev.getActionCommand ());
            a.actionPerformed (ev);
            return true;
        } else {
            return false;
        }
    }

    /** Checks to see if a given keystroke is bound to an action
    * which should function on all focused components.
    * This includes the Main Window, dialogs, popup menus, etc.
    * Otherwise only the Main Window and TopComponents will receive the keystroke.
    * By default, off, unless the action has a property named <code>OpenIDE-Transmodal-Action</code>
    * which is set to {@link Boolean#TRUE}.
    * @param key the keystroke to check
    * @return <code>true</code> if transmodal; <code>false</code> if a normal action, or
    * the key is not bound to anything in the global keymap
    */
    static boolean isTransmodalAction (KeyStroke key) {
        Action a = root.getAction (key);
        if (a == null) return false;
        Object val = a.getValue ("OpenIDE-Transmodal-Action"); // NOI18N
        return val != null && val.equals (Boolean.TRUE);
    }
}


/*
 * Log
 *  7    Gandalf   1.6         1/13/00  David Simonek   i18n
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/16/99  Jesse Glick     Processing keystrokes 
 *       with real ActionEvents, handling dialogs better too.
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         2/27/99  Jaroslav Tulach Shortcut changed to 
 *       Keymap
 *  2    Gandalf   1.1         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 */
