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

package org.netbeans.modules.form.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.BooleanStateAction;
import org.netbeans.modules.form.FormManager2;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;

/** DesignModeAction action.
*
* @author   Ian Formanek
*/
public class DesignModeAction extends BooleanStateAction {
    static final long serialVersionUID =-5550915798602920820L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 8658373232217156035L;

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (ShowGridAction.class).getString ("ACT_DesignMode");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DesignModeAction.class);
    }

    /** @return resource for the action icon */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/designMode.gif"; // NOI18N
    }

    /** Setter for the BooleanState property.
    * The state is propagated to all the action "invokers" so that they
    * can change their visual look according to the new setting and/or
    * perform any actions to reflect the state change
    * @param action The "external" part of the action which invoked th state change
    * @param value the new value of the BooleanState property
    */
    public void setBooleanState (boolean value) {
        super.setBooleanState (value);
        if ((!internalChange) && (manager != null))
            manager.setMode (value ? DesignLayout.DESIGN_MODE:DesignLayout.REAL_MODE);
    }

    public void setFormManager (FormManager2 manager) {
        this.manager = manager;
        setEnabled ((manager != null) && (manager.isTestMode () != true));
        if (manager != null) {
            internalChange = true;
            setBooleanState (manager.getMode () == DesignLayout.DESIGN_MODE);
            internalChange = false;
        }
    }

    private static boolean internalChange = false;

    private static FormManager2 manager;
}

/*
 * Log
 *  10   Gandalf   1.9         1/5/00   Ian Formanek    NOI18N
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/31/99  Ian Formanek    Design/Test Mode
 *  5    Gandalf   1.4         5/17/99  Ian Formanek    Disabled in Beta2 as it 
 *       does not work
 *  4    Gandalf   1.3         5/15/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/21/99  Ian Formanek    Removed obsoleted line
 *  1    Gandalf   1.0         3/18/99  Ian Formanek    
 * $
 */
