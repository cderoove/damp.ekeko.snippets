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

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;

/** Go to a specific place (e.g.<!-- --> line in the editor).
*
* @author   Miloslav Metelka
*/
public class GotoAction extends CallbackSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7446663011056564646L;

    /* Construct new goto action */
    public GotoAction() {
        setSurviveFocusChange(false);
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("Goto");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GotoAction.class);
    }

    /* Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
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
 *  4    Gandalf   1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
