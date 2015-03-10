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
import org.openide.util.actions.CallbackSystemAction;

/** The action which invoke next "jump" line in output window (like next error)
*
* @author Petr Hamernik
*/
public class NextOutJumpAction extends CallbackSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1402963220457972269L;

    /** Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/nextOutJump.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (NextOutJumpAction.class);
    }

    public String getName() {
        return NbBundle.getBundle(NextOutJumpAction.class).getString("NextOutJump");
    }
}

/*
 * Log
 *  11   Gandalf   1.10        2/4/00   Jesse Glick     Comment changed only.
 *  10   Gandalf   1.9         1/12/00  Ales Novak      i18n
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/26/99  Ian Formanek    Actions cleanup
 *  5    Gandalf   1.4         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  4    Gandalf   1.3         3/2/99   David Simonek   icons repair
 *  3    Gandalf   1.2         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
