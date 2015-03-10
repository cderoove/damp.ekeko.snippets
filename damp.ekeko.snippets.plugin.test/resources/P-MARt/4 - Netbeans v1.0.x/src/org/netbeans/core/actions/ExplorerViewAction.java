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

import org.openide.util.HelpCtx;
import org.netbeans.core.actions.OpenExplorerAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;

/** The action which opens explorer window, if it is not opened yet.
*
* @author Dafe Simonek, Ian Formanek
*/
public final class ExplorerViewAction extends CallableSystemAction {

    static final long serialVersionUID =-9185100672309218375L;
    public String getName() {
        return NbBundle.getBundle(ExplorerViewAction.class).getString("ExplorerView");
    }

    /** @return resource for the action icon */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/explorerView.gif"; // NOI18N
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (ExplorerViewAction.class);
    }

    /** Activates or opens tree view explorer component */
    public void performAction() {
        ((OpenExplorerAction)SystemAction.get(OpenExplorerAction.class)).
        performAction();
    }

}

/*
* Log
*  10   src-jtulach1.9         1/12/00  Ales Novak      i18n
*  9    src-jtulach1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    src-jtulach1.7         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  7    src-jtulach1.6         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  6    src-jtulach1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    src-jtulach1.4         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  4    src-jtulach1.3         3/30/99  David Simonek   now requests focus for 
*       component too
*  3    src-jtulach1.2         3/15/99  Ian Formanek    Fixed moving 
*       OpenExplorerAction to developer.impl
*  2    src-jtulach1.1         3/14/99  Ian Formanek    Modified to the new 
*       MainExplorer
*  1    src-jtulach1.0         3/12/99  David Simonek   
* $
*/
