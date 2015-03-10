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
import org.netbeans.core.NbMainExplorer;

/** OpenExplorer action.
* @author   Ian Formanek
*/
public class OpenExplorerAction extends org.openide.util.actions.CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1992545903394299720L;

    {
        // enable the action
        setEnabled (true);
    }

    /** @return the action's icon */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(OpenExplorerAction.class).getString("OpenExplorer");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (OpenExplorerAction.class);
    }

    /** @return the action's icon */
    public String iconResource () {
        return "/org/netbeans/core/resources/actions/openExplorer.gif"; // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction () {
        NbMainExplorer explorer = NbMainExplorer.getExplorer();
        explorer.openRoots();
        // PENDING - what with focus?
        // explorer.requestFocus();
    }

}

/*
 * Log
 *  14   src-jtulach1.13        1/12/00  Ales Novak      i18n
 *  13   src-jtulach1.12        11/30/99 David Simonek   neccessary changes 
 *       needed to change main explorer to new UI style  (tabs are full top 
 *       components now, visual workspace added, layout of editing workspace 
 *       chnaged a bit)
 *  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  10   src-jtulach1.9         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  9    src-jtulach1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    src-jtulach1.7         3/30/99  David Simonek   now requests focus for 
 *       component too
 *  7    src-jtulach1.6         3/15/99  Ian Formanek    
 *  6    src-jtulach1.5         3/15/99  Ian Formanek    Fixed moving 
 *       OpenExplorerAction to developer.impl
 *  5    src-jtulach1.4         3/15/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/14/99  Ian Formanek    Modified to the new 
 *       MainExplorer
 *  3    src-jtulach1.2         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  2    src-jtulach1.1         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  1    src-jtulach1.0         2/8/99   Petr Hamernik   
 * $
 */
