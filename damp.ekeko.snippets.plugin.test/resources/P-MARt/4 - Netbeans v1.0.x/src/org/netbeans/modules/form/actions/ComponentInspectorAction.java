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

/** The ComponentInspector action. Displays the ComponentInspector.
*
* @author   Ian Formanek
*/
public class ComponentInspectorAction extends CallableSystemAction {
    static final long serialVersionUID =-3952196749375353118L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 9171231622495988045L;

    /** Human presentable name of the action. This should be presented
    * as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (ComponentInspectorAction.class).getString("ACT_ComponentInspector");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ComponentInspectorAction.class);
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
 *  11   Gandalf   1.10        1/5/00   Ian Formanek    NOI18N
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/10/99  Ian Formanek    Generated Serial Version
 *       UID
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/11/99  Ian Formanek    Build 318 version
 *  5    Gandalf   1.4         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  4    Gandalf   1.3         3/24/99  Ian Formanek    
 *  3    Gandalf   1.2         3/16/99  Ian Formanek    
 *  2    Gandalf   1.1         3/10/99  Ian Formanek    Gandalf updated
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
