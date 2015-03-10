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
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.core.output.OutputTab;

/** The action which shows standard IO component.
*
* @author Dafe Simonek
*/
public final class OutputWindowAction extends CallableSystemAction {

    static final long serialVersionUID =170685796983527017L;
    /** Opens std IO top component */
    public void performAction() {
        OutputTab output = OutputTab.getStdOutputTab();
        output.open();
        output.requestFocus();
    }

    public String getName() {
        return NbBundle.getBundle(OutputWindowAction.class).getString("OutputWindow");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (OutputWindowAction.class);
    }

    /**
    * @return resource for the action icon
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/frames/output.gif"; // NOI18N
    }

}

/*
* Log
*  10   Gandalf   1.9         1/12/00  Ales Novak      i18n
*  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  7    Gandalf   1.6         7/11/99  David Simonek   window system change...
*  6    Gandalf   1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  3    Gandalf   1.2         4/1/99   David Simonek   request focus added
*  2    Gandalf   1.1         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  1    Gandalf   1.0         3/24/99  Ian Formanek    
* $
*/
