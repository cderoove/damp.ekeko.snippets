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
import org.openide.util.actions.*;

import org.netbeans.core.StatusLine;

/** Action that shows status text in toolbar.
*
* @author Ian Formanek
*/
public class StatusLineAction extends SystemAction implements Presenter.Toolbar {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6074126305723764618L;

    /** Creates the presenter
    */
    public java.awt.Component getToolbarPresenter () {
        return StatusLine.createLabel ();
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/core/resources/actions/about.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (StatusLineAction.class);
    }

    public String getName() {
        return NbBundle.getBundle (StatusLineAction.class).getString("StatusLine");
    }

    /** Do nothing. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {}

}

/*
 * Log
 *  8    src-jtulach1.7         1/12/00  Ales Novak      i18n
 *  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    src-jtulach1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  5    src-jtulach1.4         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  4    src-jtulach1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    src-jtulach1.2         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  2    src-jtulach1.1         3/26/99  Jesse Glick     SystemAction.actionPerformed(ActionEvent)
 *        is now abstract; you must explicitly provide an empty body if that is 
 *       desired.
 *  1    src-jtulach1.0         2/11/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 */
