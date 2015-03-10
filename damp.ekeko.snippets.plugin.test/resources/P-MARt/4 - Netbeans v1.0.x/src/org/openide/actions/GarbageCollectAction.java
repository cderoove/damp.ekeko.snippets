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
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Perform a system garbage collection.
*
* @author   Ian Formanek
*/
public class GarbageCollectAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3365766607488094613L;

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(GarbageCollectAction.class).getString("CTL_GarbageCollect");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GarbageCollectAction.class);
    }

    public void performAction() {
        System.gc();
    }
}

/*
 * Log
 *  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    src-jtulach1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    src-jtulach1.6         5/26/99  Ian Formanek    Actions cleanup
 *  6    src-jtulach1.5         5/2/99   Ian Formanek    Fixed last change
 *  5    src-jtulach1.4         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  4    src-jtulach1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  2    src-jtulach1.1         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  1    src-jtulach1.0         1/21/99  David Simonek   
 * $
 */
