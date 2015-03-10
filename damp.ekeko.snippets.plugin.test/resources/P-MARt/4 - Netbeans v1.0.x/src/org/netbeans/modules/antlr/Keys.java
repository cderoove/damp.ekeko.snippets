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

package org.netbeans.modules.antlr;

import javax.swing.text.Keymap;
import javax.swing.*;

import org.openide.*;
import org.openide.util.actions.*;
import org.openide.util.Utilities;

/** Defines key assignements for the IDE. Must be run internally in the IDE.
*/
public class Keys extends Object {
    /** key map to use */
    private static  Keymap map;

    /** Assigns a key to an action
    * @param key key name
    * @param action name of the action
    */
    private static void assign (String key, String action) throws ClassNotFoundException {
        KeyStroke str = Utilities.stringToKey (key);
        if (str == null) {
            System.err.println ("Not a valid key: " + key);
            // go on
            return;
        }

        Class actionClass = Class.forName (action);

        // create instance of the action
        SystemAction a = SystemAction.get (actionClass);

        map.addActionForKeyStroke (str, a);
    }


    public static void main (String args[]) {

        try {
            map = TopManager.getDefault ().getGlobalKeymap ();
        } catch (Throwable t) {
            System.err.println ("Must be executed inside the IDE by internal execution!");
            return;
        }


        // Open API actions
        try {
            assign ("C-F1", "org.netbeans.core.actions.GlobalPropertiesAction");
            assign ("C-F2", "org.openide.actions.NewTemplateAction");
            assign ("C-F3", "org.openide.actions.NewTemplateAction");

        } catch (ClassNotFoundException e) {
            // print and go on
            e.printStackTrace();
        }
    }
}
