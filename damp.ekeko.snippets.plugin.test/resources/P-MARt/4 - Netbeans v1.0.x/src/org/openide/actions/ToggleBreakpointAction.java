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

/** Toggle a breakpoint (e.g.<!-- --> in the Editor).
*
* @see org.openide.debugger.Debugger#findBreakpoint
* @author   Jan Jancura
*/
public class ToggleBreakpointAction extends CallbackSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2293420959009110628L;

    /* @return the action's icon */
    public String getName() {
        return ActionConstants.BUNDLE.getString("ToggleBreakpoint");
    }

    /* @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (ToggleBreakpointAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/toggleBreakpoint.gif"; // NOI18N
    }

}

/*
 * Log
 *  12   src-jtulach1.11        1/12/00  Ian Formanek    NOI18N
 *  11   src-jtulach1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   src-jtulach1.9         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  9    src-jtulach1.8         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    src-jtulach1.6         5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  6    src-jtulach1.5         5/2/99   Ian Formanek    Fixed last change
 *  5    src-jtulach1.4         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  4    src-jtulach1.3         3/26/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/26/99  Jesse Glick     [JavaDoc]
 *  2    src-jtulach1.1         3/2/99   David Simonek   icons repair
 *  1    src-jtulach1.0         2/18/99  David Simonek   
 * $
 */
