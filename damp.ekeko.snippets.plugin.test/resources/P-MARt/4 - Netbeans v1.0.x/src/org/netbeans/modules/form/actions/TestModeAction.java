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

/** TestModeAction action.
*
* @author   Ian Formanek
*/
public class TestModeAction extends BooleanStateAction {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 3453179495097484331L;

    /** The help context of this action */
    private static HelpCtx help;

    static final long serialVersionUID =6405790716032972989L;
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (TestModeAction.class).getString ("ACT_TestMode");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TestModeAction.class);
    }

    /** @return resource for the action icon */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/testMode.gif"; // NOI18N
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
        if ((!internalChange) && (manager != null)) {
            manager.setTestMode (value);
        }
    }

    public void setFormManager (FormManager2 manager) {
        this.manager = manager;
        setEnabled (manager != null);
        if (manager != null) {
            internalChange = true;
            setBooleanState (manager.isTestMode ());
            internalChange = false;
        }
    }

    private static boolean internalChange = false;

    private static FormManager2 manager;
}

/*
 * Log
 *  9    Gandalf   1.8         1/5/00   Ian Formanek    NOI18N
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/31/99  Ian Formanek    Design/Test Mode
 *  4    Gandalf   1.3         5/17/99  Ian Formanek    Disabled in Beta2 as it 
 *       does not work
 *  3    Gandalf   1.2         5/15/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  1    Gandalf   1.0         3/18/99  Ian Formanek    
 * $
 * Beta Change History:
 */
