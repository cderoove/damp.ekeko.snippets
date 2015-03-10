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

import org.openide.TopManager;
import org.openide.cookies.SaveCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Saves the currently opened project.
*
* @author   Ian Formanek
*/
public class SaveSettingsAction extends CallableSystemAction {
    static final long serialVersionUID =-842312534946404400L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 862817653968029273L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(SaveSettingsAction.class).getString("SaveSettings");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveSettingsAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/saveProject.gif"; // NOI18N
    }

    public void performAction() {
        SaveCookie sc = (SaveCookie)TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ().getCookie (SaveCookie.class);
        try {
            sc.save ();
        } catch (java.io.IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    public boolean isEnabled () {
        return (TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ().getCookie (SaveCookie.class) != null);
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ales Novak      i18n
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/12/99  Ian Formanek    
 * $
 */
