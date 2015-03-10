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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.undo.CannotRedoException;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Redo an edit.
*
* @see UndoAction
* @author   Ian Formanek, Jaroslav Tulach
*/
public class RedoAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7791957449503504810L;

    /* Construct new redo action */
    public RedoAction() {
        UndoAction.initializeUndoRedo ();
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        MessageFormat mformat =  new MessageFormat(ActionConstants.BUNDLE.getString("Redo"));
        return mformat.format(new String[] {UndoAction.getUndoRedo ().getRedoPresentationName()});
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (RedoAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/redo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            UndoAction.getUndoRedo ().redo ();
        } catch (CannotRedoException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        UndoAction.updateStatus();
    }
}


/*
 * Log
 *  13   Gandalf   1.12        1/12/00  Ian Formanek    NOI18N
 *  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  7    Gandalf   1.6         5/2/99   Ian Formanek    Fixed last change
 *  6    Gandalf   1.5         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  5    Gandalf   1.4         3/26/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/10/99  Jaroslav Tulach UndoRedo object
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
