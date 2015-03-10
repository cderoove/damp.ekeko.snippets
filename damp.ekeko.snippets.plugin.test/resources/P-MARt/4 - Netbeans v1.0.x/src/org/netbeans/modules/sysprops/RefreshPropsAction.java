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

package org.netbeans.modules.sysprops;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
public class RefreshPropsAction extends CallableSystemAction {
    private static final long serialVersionUID =-4288597556607349902L;
    public void performAction () {
        PropertiesNotifier.changed ();
    }
    public String getName () {
        return NbBundle.getBundle (RefreshPropsAction.class).getString ("LBL_RefreshProps");
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.sysprops");
    }
}
