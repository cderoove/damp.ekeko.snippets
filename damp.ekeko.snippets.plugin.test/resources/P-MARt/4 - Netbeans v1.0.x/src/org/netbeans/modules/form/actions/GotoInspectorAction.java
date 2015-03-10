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
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.form.*;

/** The GotoInspector action. Displays the ComponentInspector.
*
* @author   Ian Formanek
*/
public class GotoInspectorAction extends CallableSystemAction {
    static final long serialVersionUID =8722913949602335270L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 9171231622495988045L;

    /** Human presentable name of the action. This should be presented
    * as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (GotoInspectorAction.class).getString("ACT_GotoInspector");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GotoInspectorAction.class);
    }

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction() {
        FormEditor.getComponentInspector().open ();
        FormEditor.getComponentInspector().requestFocus ();
    }

}

/*
 * Log
 *  4    Gandalf   1.3         1/5/00   Ian Formanek    NOI18N
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/2/99   Ian Formanek    
 * $
 */
