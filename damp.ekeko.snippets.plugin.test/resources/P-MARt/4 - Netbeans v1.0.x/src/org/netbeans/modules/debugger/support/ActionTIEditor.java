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

import org.openide.util.NbBundle;

import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;


/**
* A property editor for set of actions on TraceInto method
* without source.
*
* @author   Jan Jancura
*/
public class ActionTIEditor extends ChoicePropertyEditor {

    public ActionTIEditor () {
        super (
            ((DebuggerSettings) DebuggerSettings.findObject (DebuggerSettings.class)).
            getActionTIConstants (),
            ((DebuggerSettings) DebuggerSettings.findObject (DebuggerSettings.class)).
            getActionTIComments ()
        );
    }
}

/*
 * Log
 *  1    Gandalf   1.0         1/17/00  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 anonymous       []
 */
