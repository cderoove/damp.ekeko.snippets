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

package org.netbeans.core.actions;

import org.netbeans.core.Main;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** TipOfTheDay action.
*
* @author   Ian Formanek (checked - [PENDING HelpCtx])
*/
public class TipOfTheDayAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2603339571253423517L;

    /** The help context of this action */
    private static HelpCtx help;

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(TipOfTheDayAction.class).getString("TipOfTheDay");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (TipOfTheDayAction.class);
    }

    /** Icon of this action.
    * @return name of the action icon
    */
    protected String iconResource() {
        return "/org/netbeans/core/resources/actions/tipOfTheDay.gif"; // NOI18N
    }

    public void performAction() {
        Main.showTipsOfTheDay ();
    }
}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Ales Novak      i18n
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/26/99  Ian Formanek    Cleaned up
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    TipsOfTheDay "Gandalfed"
 *  4    Gandalf   1.3         3/2/99   David Simonek   icons repair
 *  3    Gandalf   1.2         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
